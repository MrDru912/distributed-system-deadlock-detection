package cz.ctu.fee.dsv.grpc;

import cz.ctu.fee.dsv.grpc.exceptions.KilledNodeActsAsClientException;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIHandler {
    final Logger logger = LoggerFactory.getLogger(APIHandler.class);

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
                    logger.info("Setting delay: {}", ctx.pathParam("delay"));
                    myNode.setDelay(Integer.parseInt(ctx.pathParam("other_node_port")));
                    ctx.result("Delay was set up. ip: " + myNode.getMyIP() + " port: " + myNode.getMyPort()+"\n");
                })
                .get("/join/{other_node_ip}/{other_node_port}", ctx -> {
                    logger.info("Joining node: {}: {}", ctx.pathParam("other_node_ip"), ctx.pathParam("other_node_port"));
                    myNode.join(ctx.pathParam("other_node_ip"), Integer.parseInt(ctx.pathParam("other_node_port")));
                    ctx.result("Tried to join to: " + ctx.pathParam("other_node_ip") + " " + ctx.pathParam("other_node_port") + "\n");
                })
                .get("/leave", ctx -> {
                    logger.info("Node is leaving. ip: {} port: {}", myNode.getMyIP(), myNode.getMyPort());
                    myNode.leave();
                    ctx.result("Node left. ip: " + myNode.getMyIP() + " port: " + myNode.getMyPort()+"\n");
                })
                .get("/kill", ctx -> {
                    logger.info("Node is being killed. ip: {} port: {}", myNode.getMyIP(), myNode.getMyPort());
                    myNode.kill();
                    ctx.result("Node was killed. ip: " + myNode.getMyIP() + " port: " + myNode.getMyPort()+"\n");
                })
                .get("/revive", ctx -> {
                    logger.info("Node is being revived. ip: {} port: {}", myNode.getMyIP(), myNode.getMyPort());
                    try {
                        myNode.revive();
                        ctx.result("Node was revived. ip: " + myNode.getMyIP() + "port: " + myNode.getMyPort()+"\n");
                    } catch (Exception e){
                        ctx.result("Failed to revive node ip: " + myNode.getMyIP() + " port: " + myNode.getMyPort() + "\n");
                    }
                })
                .get("/preliminary_request/{resource_id}", ctx -> {
                    logger.info("Sending preliminary request to get {}. ip: {} port {}", ctx.pathParam("resource_id"), myNode.getMyIP(), myNode.getMyPort());
                    myNode.sendPreliminaryRequest(ctx.pathParam("resource_id"));
                })
                .get("/request_resource/{resource_id}", ctx -> {
                    logger.info("Sending request to get {}. ip: {} port: {}", ctx.pathParam("resource_id"), myNode.getMyIP(), myNode.getMyPort());
                    myNode.requestResource(ctx.pathParam("resource_id"));
                })
                .get("/release_resource/{resource_id}", ctx -> {
                    logger.info("Sending release source {}. ip: {} port: {}", ctx.pathParam("resource_id"), myNode.getMyIP(), myNode.getMyPort());
                    try{
                        myNode.releaseResource(ctx.pathParam("resource_id"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .get("/send_hello_next", ctx -> {
                    logger.info("Sending hello to next node");
                    try {
                        myNode.sendHelloToNext();
                        ctx.result("Hello sent\n");
                    } catch (KilledNodeActsAsClientException e){
                        ctx.result("Node is not alive. Try to revive it.\n").status(500);
                    }
                })
                .get("/get_status", ctx -> {
                    logger.info("Getting status of this node");
                    myNode.printStatus();
                    ctx.result(myNode.getStatus() + "\n");
//                    ctx.json(myNode);
                })
                .get("/start_grpc_server", ctx -> {
                    logger.info("Starting grpc server part.");
                    myNode.startGrpc();
                    ctx.result("grpc server started\n");
                })
                .get("/stop_grpc_server", ctx -> {
                    logger.info("Stopping grpc server part.");
                    myNode.stopGrpc();
                    myNode.resetoNodeInTopology();
                    ctx.result("grpc server + topology info reset\n");
                })
                // try to add command for starting election
                .start(this.port);
    }
}

