package cz.ctu.fee.dsv.grpc;

import cz.ctu.fee.dsv.grpc.exceptions.KilledNodeActsAsClientException;
import io.javalin.Javalin;

public class APIHandler {

    private int port = 7000;
    private Node myNode = null;
    private Javalin app = null;


    public APIHandler(Node myNode, int port) {
        this.myNode = myNode;
        this.port = port;
    }

    public APIHandler(Node myNode) {
        this(myNode, 7000);
    }


    public void start() {
        this.app = Javalin.create()
                .get("/setDelay/{delay}", ctx -> {
                    System.out.println("Setting delay: " + ctx.pathParam("delay"));
                    myNode.setDelay(Integer.parseInt(ctx.pathParam("other_node_port")));
                    ctx.result("Delay was set up. ip: " + myNode.getMyIP() + " port" + myNode.getMyPort()+"\n");
                })
                .get("/join/{other_node_ip}/{other_node_port}", ctx -> {
                    System.out.println("Joining node: " + ctx.pathParam("other_node_ip") + ":" + ctx.pathParam("other_node_port"));
                    myNode.join(ctx.pathParam("other_node_ip"), Integer.parseInt(ctx.pathParam("other_node_port")));
                    ctx.result("Tried to join to: " + ctx.pathParam("other_node_ip") + " " + ctx.pathParam("other_node_port") + "\n");
                })
                .get("/leave", ctx -> {
                    System.out.println("Node is leaving. ip: " + myNode.getMyIP() + " port" + myNode.getMyPort());
                    myNode.leave();
                    ctx.result("Node left. ip: " + myNode.getMyIP() + " port" + myNode.getMyPort()+"\n");
                })
                .get("/kill", ctx -> {
                    System.out.println("Node is being killed. ip: " + myNode.getMyIP() + " port" + myNode.getMyPort());
                    myNode.kill();
                    ctx.result("Node was killed. ip: " + myNode.getMyIP() + " port" + myNode.getMyPort()+"\n");
                })
                .get("/revive", ctx -> {
                    System.out.println("Node is being revived. ip: " + myNode.getMyIP() + " port" + myNode.getMyPort());
                    try {
                        myNode.revive();
                        ctx.result("Node was revived. ip: " + myNode.getMyIP() + " port" + myNode.getMyPort()+"\n");
                    } catch (Exception e){
                        ctx.result("Failed to revive node ip: " + myNode.getMyIP() + " port" + myNode.getMyPort() + "\n");
                    }
                })
                .get("/send_hello_next", ctx -> {
                    System.out.println("Sending hello to next node");
                    try {
                        myNode.sendHelloToNext();
                        ctx.result("Hello sent\n");
                    } catch (KilledNodeActsAsClientException e){
                        ctx.result("Node is not alive. Try to revive it.\n").status(500);
                    }
                })
                .get("/get_status", ctx -> {
                    System.out.println("Getting status of this node");
                    myNode.printStatus();
                    ctx.result(myNode.getStatus() + "\n");
//                    ctx.json(myNode);
                })
                .get("/start_grpc_server", ctx -> {
                    System.out.println("Starting grpc server part.");
                    myNode.startGrpc();
                    ctx.result("grpc server started\n");
                })
                .get("/stop_grpc_server", ctx -> {
                    System.out.println("Stopping grpc server part.");
                    myNode.stopGrpc();
                    myNode.resetoNodeInTopology();
                    ctx.result("grpc server + topology info reset\n");
                })
                // try to add command for starting election
                .start(this.port);
    }
}

