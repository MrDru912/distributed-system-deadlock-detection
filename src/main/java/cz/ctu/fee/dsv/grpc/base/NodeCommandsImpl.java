package cz.ctu.fee.dsv.grpc.base;

import cz.ctu.fee.dsv.*;
import cz.ctu.fee.dsv.grpc.Node;
import cz.ctu.fee.dsv.grpc.mappers.ProtobufMapper;
import cz.ctu.fee.dsv.grpc.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeCommandsImpl implements NodeCommands{
    final Logger logger = LoggerFactory.getLogger(NodeCommandsImpl.class);
    private Node myNode = null;


    public NodeCommandsImpl(Node node) {
        this.myNode = node;
    }

    @Override
    public DSNeighboursProto join(AddressProto protoAddr) {
        Address addr = ProtobufMapper.fromProtoToAddress(protoAddr);
        logger.info("JOIN was called ...");
        if (addr.compareTo(myNode.getAddress()) == 0) {
            logger.info("I am the first and leader");
            return ProtobufMapper.DSNeighboursToProto(myNode.getNeighbours());
        } else {
            logger.info("Someone is joining ...");
            DSNeighbours myNeighbours = myNode.getNeighbours();
            Address myInitialNext = new Address(myNeighbours.next);     // because of 2 nodes config
            Address myInitialPrev = new Address(myNeighbours.prev);     // because of 2 nodes config
            DSNeighbours tmpNeighbours = new DSNeighbours(myNeighbours.next,
                    myNeighbours.nnext,
                    myNode.getAddress(),
                    myNeighbours.leader);
            // to my (initial) next send msg ChPrev to addr
            myNode.getCommHub().getNext().chngPrev(protoAddr);
            // to my (initial) prev send msg ChNNext addr
            myNode.getCommHub().getGrpcProxy(myInitialPrev).chngNNext(protoAddr);
            tmpNeighbours.nnext = myNeighbours.nnext;
            // handle myself
            myNeighbours.nnext = myInitialNext;
            myNeighbours.next = addr;
            return ProtobufMapper.DSNeighboursToProto(tmpNeighbours);
        }
    }


    @Override
    public void chngNNext(AddressProto addrProto) {
        logger.info("ChngNNext was called ...");
        myNode.getNeighbours().nnext = ProtobufMapper.fromProtoToAddress(addrProto);
    }

    @Override
    public void chngNext(AddressProto addrProto) {
        logger.info("chngNext was called ...");
        myNode.getNeighbours().next = ProtobufMapper.fromProtoToAddress(addrProto);
    }



    @Override
    public AddressProto chngPrev(AddressProto addrProto) {
        logger.info("ChngPrev was called ...");
        myNode.getNeighbours().prev = ProtobufMapper.fromProtoToAddress(addrProto);
        return ProtobufMapper.AddressToProto(myNode.getNeighbours().next);
    }

    @Override
    public void chngNNextOfPrev(AddressProto addrProto) {
        logger.info("chngNNextOfPrev was called ...");
        myNode.getCommHub().getPrev().chngNNext(addrProto);
    }

    @Override
    public void nodeMissing(AddressProto addrProto) {
        Address addr = ProtobufMapper.fromProtoToAddress(addrProto);
        logger.info("NodeMissing was called with {}", addr);
        if (addr.compareTo(myNode.getNeighbours().next) == 0) {
            // its for me
            DSNeighbours myNeighbours = myNode.getNeighbours();
            // to my nnext send msg ChPrev with myaddr -> my nnext = next
            myNeighbours.next = myNeighbours.nnext;
            myNeighbours.nnext = ProtobufMapper.fromProtoToAddress(
                    myNode.getCommHub().getNNext().chngPrev(ProtobufMapper.AddressToProto(myNode.getAddress()))
            );
            // to my prev send msg ChNNext to my.next
            myNode.getCommHub().getPrev().chngNNext(ProtobufMapper.AddressToProto(myNeighbours.next));
            logger.info("NodeMissing DONE");
        } else {
            // send to next node
            myNode.getCommHub().getNext().nodeMissing(addrProto);
        }
    }

    public void nodeLeft(AddressProto addrProto) {
        Address addr = ProtobufMapper.fromProtoToAddress(addrProto);
        logger.info("NodeLeft was called with " + addr);
        DSNeighbours myNeighbours = myNode.getNeighbours();
        /* 2 nodes cycle */
        if (myNode.getNeighbours().prev.compareTo(myNode.getNeighbours().next) == 0) {
            myNode.getCommHub().getPrev().chngNext(ProtobufMapper.AddressToProto(myNeighbours.next));
            myNode.getCommHub().getPrev().chngPrev(ProtobufMapper.AddressToProto(myNeighbours.next));
        }
        /* 3 nodes cycle */
        else if (myNode.getNeighbours().prev.compareTo(myNode.getNeighbours().nnext) == 0) {
            myNode.getCommHub().getNext().chngNNext(ProtobufMapper.AddressToProto(myNeighbours.next));
            myNode.getCommHub().getNext().chngPrev(ProtobufMapper.AddressToProto(myNeighbours.prev));

            myNode.getCommHub().getPrev().chngNext(ProtobufMapper.AddressToProto(myNeighbours.next));
            myNode.getCommHub().getPrev().chngNNext(ProtobufMapper.AddressToProto(myNeighbours.prev));
        }
        /* more than 3 nodes cycle */
        else {
            myNode.getCommHub().getPrev().chngNext(ProtobufMapper.AddressToProto(myNeighbours.next));
            myNode.getCommHub().getPrev().chngNNext(ProtobufMapper.AddressToProto(myNeighbours.nnext));

            myNode.getCommHub().getNext().chngPrev(ProtobufMapper.AddressToProto(myNeighbours.prev));

            myNode.getCommHub().getPrev().chngNNextOfPrev(ProtobufMapper.AddressToProto(myNeighbours.next));
        }
        logger.info("NodeMissing DONE");
    }




//    @Override
//    public void SendMsg(String toNickName, String fromNickName, String message) {
//
//    }




    @Override
    public void hello() {
        logger.info("Hello method called!");

        // Send an empty response
//        responseObserver.onNext(Empty.getDefaultInstance());
//        // Signal that the call is complete
//        responseObserver.onCompleted();
    }

    @Override
    public void preliminaryRequest(RequestResourceMessageProto preliminaryRequestMessageProto) {
        logger.info("Preliminary request. {} {}.", myNode.getMyIP(), myNode.getMyPort());
        if (preliminaryRequestMessageProto.getResourceId().equals(this.myNode.getResource().getId())) {
            myNode.processPreliminaryRequest(preliminaryRequestMessageProto);
        } else {
            this.myNode.getCommHub().getNext().preliminaryRequest(preliminaryRequestMessageProto);
        }
    }

    @Override
    public void requestResource(RequestResourceMessageProto requestResourceMessageProto) {
        logger.info("Request resource. Current node: ip {}; port {} resourceId {}. requested resource id: {}",
                myNode.getMyIP(), myNode.getResource().getId(), myNode.getMyPort(), requestResourceMessageProto.getResourceId());
        if (requestResourceMessageProto.getResourceId().equals(this.myNode.getResource().getId())){
            myNode.requestResource(requestResourceMessageProto);
        } else {
            this.myNode.getCommHub().getNext().requestResource(requestResourceMessageProto);
        }
    }

    @Override
    public void acquireResource(AcquireMessageProto acquireMessageProto) {
        logger.info("Acquire resource. Current node: ip {}; port {}; resourceId {}. Granted to: {}",
                myNode.getMyIP(), myNode.getMyPort(), myNode.getResource().getId(), ProtobufMapper.fromProtoToAddress(acquireMessageProto.getRequesterAddress()));
        if (this.myNode.getAddress().hostname.equals(acquireMessageProto.getRequesterAddress().getHostname())
        && this.myNode.getAddress().port == acquireMessageProto.getRequesterAddress().getPort()) {
            this.myNode.acquireResource(acquireMessageProto);
        } else {
            this.myNode.getCommHub().getNext().acquireResource(acquireMessageProto);
        }
    }

    @Override
    public void resourceWasReleased(ResourceProto resourceProto) {
        if (this.myNode.getResource().getId().equals(resourceProto.getId())){
            this.myNode.processReleaseResource(resourceProto);
        } else {
            logger.info("Resource was released request on {}. Redirecting to the next node", myNode.getAddress());
            this.myNode.getCommHub().getNext().resourceWasReleased(resourceProto);
        }
    }
}
