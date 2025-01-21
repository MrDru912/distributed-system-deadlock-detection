package cz.ctu.fee.dsv.grpc;

import com.google.protobuf.Empty;
import cz.ctu.fee.dsv.AddressProto;
import cz.ctu.fee.dsv.CommandsGrpc;
import cz.ctu.fee.dsv.DSNeighboursProto;
import cz.ctu.fee.dsv.grpc.base.NodeCommands;
import cz.ctu.fee.dsv.grpc.base.NodeCommandsImpl;
import io.grpc.stub.StreamObserver;

public class MessageReceiver extends CommandsGrpc.CommandsImplBase {
    private Node myNode = null;

    public NodeCommands getNodeCommands() {
        return nodeCommands;
    }

    private NodeCommands nodeCommands;

    public MessageReceiver(Node node) {
        this.myNode = node;
        nodeCommands = new NodeCommandsImpl(this.myNode);
    }

    @Override
    public void join(AddressProto protoAddr,
                             io.grpc.stub.StreamObserver<cz.ctu.fee.dsv.DSNeighboursProto> responseObserver) {
        DSNeighboursProto reply = nodeCommands.join(protoAddr);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
        }

    @Override
    public void chngNNextOfPrev(AddressProto addrProto,
                          StreamObserver<Empty> responseObserver) {
        nodeCommands.chngNNextOfPrev(addrProto);
        // Send an empty response
        responseObserver.onNext(Empty.getDefaultInstance());
        // Signal that the call is complete
        responseObserver.onCompleted();
    }

    @Override
    public void chngNNext(AddressProto addrProto,
                          StreamObserver<Empty> responseObserver) {
        nodeCommands.chngNNext(addrProto);
        // Send an empty response
        responseObserver.onNext(Empty.getDefaultInstance());
        // Signal that the call is complete
        responseObserver.onCompleted();
    }

    @Override
    public void chngNext(AddressProto addrProto,
                          StreamObserver<Empty> responseObserver) {
        nodeCommands.chngNext(addrProto);
        // Send an empty response
        responseObserver.onNext(Empty.getDefaultInstance());
        // Signal that the call is complete
        responseObserver.onCompleted();
    }


    @Override
    public void chngPrev(AddressProto addrProto,
            io.grpc.stub.StreamObserver<cz.ctu.fee.dsv.AddressProto> responseObserver) {
        AddressProto reply = nodeCommands.chngPrev(addrProto);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
//        return myNode.getNeighbours().next;
    }


    @Override
    public void nodeMissing(AddressProto addrProto, StreamObserver<Empty> responseObserver) {
        nodeCommands.nodeMissing(addrProto);
        // Send an empty response
        responseObserver.onNext(Empty.getDefaultInstance());
        // Signal that the call is complete
        responseObserver.onCompleted();
    }


//
//    @Override
//    public void SendMsg(String toNickName, String fromNickName, String message) {
//
//    }


    @Override
    public void hello(Empty request, StreamObserver<Empty> responseObserver) {
//        // Perform any logic you need (e.g., logging, updating state)
        nodeCommands.hello();
        // Send an empty response
        responseObserver.onNext(Empty.getDefaultInstance());
        // Signal that the call is complete
        responseObserver.onCompleted();
    }
}
