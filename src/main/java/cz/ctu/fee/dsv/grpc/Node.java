package cz.ctu.fee.dsv.grpc;

import com.google.protobuf.Empty;
import cz.ctu.fee.dsv.*;
import cz.ctu.fee.dsv.grpc.base.Address;
import cz.ctu.fee.dsv.grpc.base.DSNeighbours;
import cz.ctu.fee.dsv.grpc.exceptions.KilledNodeActsAsClientException;
import cz.ctu.fee.dsv.grpc.mappers.ProtobufMapper;
import cz.ctu.fee.dsv.grpc.resources.Resource;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Node implements Runnable {
    // Using logger is strongly recommended (log4j, ...)
    final Logger logger = LoggerFactory.getLogger(Node.class);

    // Name of our RMI "service"
    public static final String COMM_INTERFACE_NAME = "DSVNode";

    // This Node
    public static Node thisNode = null;

    // Initial configuration from commandline
    private String nickname = "Unknown";
    private String myIP = "127.0.0.1";
    private int myPort = 2010;
    private String otherNodeIP = "127.0.0.1";
    private int otherNodePort = 2010;

    // Node Id
    private long nodeId = 0;

    private Address myAddress;
    private DSNeighbours myNeighbours;
    private MessageReceiver myMessageReceiver;
    private Server grpcServer;
    private GrpcCommunicationHub myCommHub;
    private ConsoleHandler myConsoleHandler;
    private APIHandler myAPIHandler;

    private int delay = 0;

    boolean repairInProgress = false;

    private Resource resource;

    private NodeStatus nodeStatus = NodeStatus.ACTIVE;

    private HashMap<String, Resource> grantedResources = new HashMap<>();

    public Node (String[] args) {
        // handle commandline arguments
        if (args.length == 3) {
            nickname = args[0];
            myIP = otherNodeIP = args[1];
            myPort = otherNodePort = Integer.parseInt(args[2]);
            this.myMessageReceiver = new MessageReceiver(this);
        } else if (args.length == 5) {
            nickname = args[0];
            myIP = args[1];
            myPort = Integer.parseInt(args[2]);
            otherNodeIP = args[3];
            otherNodePort = Integer.parseInt(args[4]);
            this.myMessageReceiver = new MessageReceiver(this);
        } else {
            // something is wrong - use default values
            System.err.println("Wrong number of commandline parameters - using default values.");
        }
    }


    private long generateId(String address, int port) {
        // generates  <port><IPv4_dec1><IPv4_dec2><IPv4_dec3><IPv4_dec4>
        String[] array = myIP.split("\\.");
        long id = 0;
        long shift = 0, temp = 0;
        for(int i = 0 ; i < array.length; i++){
            temp = Long.parseLong(array[i]);
            id = (long) (id * 1000);
            id += temp;
        }
        if (id == 0) {
            // TODO problem with parsing address - handle it
            id = 666000666000l;
        }
        id = id + port*1000000000000l;
        return id;
    }


    private void startMessageReceiver() {
        try {
            // Create the gRPC server
            this.grpcServer = ServerBuilder.forPort(myAddress.port)
                    .addService(this.myMessageReceiver) // Register the service
                    .build()
                    .start();

            System.out.println("gRPC server started on " + myAddress.hostname + ":" + myAddress.port);

            // Add a shutdown hook to stop the server gracefully
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down gRPC server...");
                grpcServer.shutdown();
            }));
        } catch (Exception e) {
            System.err.println("Failed to start gRPC server: " + e.getMessage());
        }
    }

    private void stopMessageReceiver() {
        try {
            if (grpcServer != null) {
                grpcServer.shutdown();
            }
        } catch (Exception e) {
            // Something is wrong ...
            System.err.println("Stopping message listener - something is wrong: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public String toString() {
        return "Node[id:'"+nodeId+"', " +
                "nick:'"+nickname+"', " +
                "myIP:'"+myIP+"', " +
                "myPort:'"+myPort+"', " +
                "resource:"+resource.getId()+", " +
                "resourceIsFree:"+this.resource.isResourceFree()+", " +
                "nodeStatus:" + this.nodeStatus.toString() +
                "otherNodeIP:'"+otherNodeIP+"', " +
                "otherNodePort:'"+otherNodePort+"']";
    }


    public String getStatus() {
        return "Status: " + this + " with addres " + myAddress + "\n    with neighbours " + myNeighbours
                + "\n   with granted resources: " + grantedResourcesToString()
                + "\n with delayed requests: " +  delayedRequestsToString();
    }

    private String delayedRequestsToString() {
        StringBuilder stringBuilder = new StringBuilder();
        for(RequestResourceMessageProto requestResourceMessageProto : this.resource.getDelayedRequestsList()){
            stringBuilder
                    .append("[Requester: ")
                    .append(requestResourceMessageProto.getRequesterAddress())
                    .append("; resourceId: " + requestResourceMessageProto.getResourceId())
                    .append("], ");
        }
        return stringBuilder.toString();
    }

    private String grantedResourcesToString() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Resource> entry : grantedResources.entrySet()) {
            String key = entry.getKey();
            Resource value = entry.getValue();
            result.append(value.toString());
            result.append(", ");
        }
        return result.toString();
    }


    public void printStatus() {
        System.out.println(getStatus());
    }


    @Override
    public void run() {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        System.setProperty("java.rmi.server.useLocalHostname", "true");

        nodeId = generateId(myIP, myPort);
        resource = new Resource(getProcessId(myIP, myPort)+"_R", "data " + myPort);
        myAddress = new Address(myIP, myPort);
        myNeighbours = new DSNeighbours(myAddress);
        printStatus();
        startMessageReceiver();     // TODO null -> exit
        myCommHub = new GrpcCommunicationHub(this);   // TODO null -> exit
        myConsoleHandler = new ConsoleHandler(this);
        myAPIHandler = new APIHandler(this, 5000 + myAddress.port);
        System.out.println(5000 + myAddress.port);
//        if (! ((myIP == otherNodeIP) && (myPort == otherNodePort)) ) {
//            // all 5 parameters were filled
//            this.join(otherNodeIP, otherNodePort);
//        }
        myAPIHandler.start();
        new Thread(myConsoleHandler).run();
    }


    public void join(String otherNodeIP, int otherNodePort) throws KilledNodeActsAsClientException {
        if (!myCommHub.isAlive()) throw new KilledNodeActsAsClientException("Node is not alive. Try to revive it.");
        try {
            CommandsGrpc.CommandsBlockingStub tmpNode = myCommHub.getGrpcProxy(new Address(otherNodeIP, otherNodePort));
            myNeighbours = ProtobufMapper.fromProtoToDSNeighbours(
                    tmpNode.join(ProtobufMapper.AddressToProto(myAddress))
            );
            myCommHub.setActNeighbours(myNeighbours);
        } catch (Exception e) {
            e.printStackTrace();
            // TODO Exception -> exit
        }
        logger.info("Neighbours after JOIN " + myNeighbours);
    }

    public void stopGrpc() {
        stopMessageReceiver();
    }

    public void startGrpc() {
        startMessageReceiver();
    }

    public void repairTopology(Address missingNode) {
        logger.info("Repairing topology. Missing node " + missingNode);
        if (repairInProgress == false) {
            repairInProgress = true;
            {
                try {
                    myMessageReceiver.getNodeCommands().nodeMissing(ProtobufMapper.AddressToProto(missingNode));
                } catch (Exception e) {
                    // this should not happen
                    e.printStackTrace();
                }
                logger.info("Topology was repaired " + myNeighbours );
            }
            repairInProgress = false;

        }
    }

    public void sendHelloToNext() throws KilledNodeActsAsClientException {
        logger.info("Sending Hello to my Next ...");
        if (!myCommHub.isAlive()) throw new KilledNodeActsAsClientException("Node is not alive. Try to revive it.");
        try {
            myCommHub.getNext().hello(Empty.getDefaultInstance());
        } catch (Exception e) {
            repairTopology(myNeighbours.next);
        }
    }

    public void resetoNodeInTopology() {
        // reset info - start as I am only node
        logger.info("Reseting node " + this.getAddress() + " in topology");
        myNeighbours = new DSNeighbours(myAddress);
    }

    public void leave(){
        try {
            this.myMessageReceiver.getNodeCommands().nodeLeft(ProtobufMapper.AddressToProto(this.myAddress));
            this.resetoNodeInTopology();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void kill(){
        try {
            stopGrpc();
            this.myCommHub.setAlive(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }}

    public void revive(){
        try{
            startGrpc();
            this.myCommHub.setAlive(true);
            Address nodeToJoinWith = myNeighbours.prev;
            join(nodeToJoinWith.hostname, nodeToJoinWith.port);
        } catch (KilledNodeActsAsClientException e){ /* Will never happen */
            logger.info("Failed to revive node: " + e.getMessage());
        }
        catch (Exception e){
            logger.error("Failed to revive node: " + e.getMessage());
            throw e;
        }
    }

    public void sendPreliminaryRequest(String resourceId) {
        this.myCommHub.getGrpcProxy(this.myAddress)
                .preliminaryRequest(RequestResourceMessageProto.newBuilder()
                        .setResourceId(resourceId)
                        .setRequesterAddress(ProtobufMapper.AddressToProto(this.myAddress))
                        .build());
    }

    public void processPreliminaryRequest(RequestResourceMessageProto preliminaryRequestMessageProto) {
        resource.processPreliminaryRequest(preliminaryRequestMessageProto);
    }

    public void requestResource(RequestResourceMessageProto requestResourceMessageProto) {
        if(this.resource.processRequest(requestResourceMessageProto)){
            AcquireMessageProto messageWithGrant = AcquireMessageProto.newBuilder()
                    .setResource(ProtobufMapper.resourceToProto(this.resource))
                    .setRequesterAddress(requestResourceMessageProto.getRequesterAddress())
                    .build();
            /* sending message with the granted resource to the requester */
            this.myCommHub.getNext().acquireResource(messageWithGrant);
        } else {
            logger.info("Resource was not granted on request from {}", requestResourceMessageProto.getRequesterAddress());
        }
    }


    public void requestResource(String resourceId) {
        this.setStatus(NodeStatus.PASSIVE);
        this.getCommHub().getGrpcProxy(this.myAddress).requestResource(RequestResourceMessageProto
                .newBuilder()
                .setResourceId(resourceId)
                .setRequesterAddress(ProtobufMapper.AddressToProto(this.myAddress))
                .build());
    }

    public void releaseResource(String resourceId) {
        Resource resourceForRelease =  this.grantedResources.get(resourceId);
        this.grantedResources.remove(resourceId);
        this.myCommHub.getNext()
                .resourceWasReleased(ResourceProto.newBuilder()
                        .setId(resourceForRelease.getId())
                        .setData(resourceForRelease.getData())
                        .build());
    }

    public void processReleaseResource(ResourceProto resourceProto){
        logger.info("Resource was released request on {} by {}", this.getAddress(), this.resource.getLastPreliminaryRequesterId());

        RequestResourceMessageProto delayedRequest = this.resource.processReleasedResource(resourceProto);
        if (delayedRequest != null) {
            resource.processRequest(delayedRequest);
        }
    }

    public void acquireResource(AcquireMessageProto acquireMessageProto){
        logger.info(this.getAddress()+" acquired resource " + acquireMessageProto.getResource().getId());
        this.getGrantedResources().put(
                acquireMessageProto.getResource().getId(),
                new Resource(
                        acquireMessageProto.getResource().getId(),
                        acquireMessageProto.getResource().getData())
        );
        this.setStatus(NodeStatus.ACTIVE);
    }


    public HashMap<String, Resource> getGrantedResources() {
        return grantedResources;
    }

    public Resource getResource() {
        return resource;
    }

    public Address getAddress() {
        return myAddress;
    }


    public DSNeighbours getNeighbours() {
        return myNeighbours;
    }


    public MessageReceiver getMessageReceiver() {
        return myMessageReceiver;
    }


    public GrpcCommunicationHub getCommHub() {
        return myCommHub;
    }


    public long getNodeId() {
        return nodeId;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }


    public int getMyPort() {
        return myPort;
    }

    public void setMyPort(int myPort) {
        this.myPort = myPort;
    }

    public String getMyIP() {
        return myIP;
    }

    public void setMyIP(String myIP) {
        this.myIP = myIP;
    }

    public void setStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public static void main(String[] args) {
        thisNode = new Node(args);
        thisNode.run();
    }
}
