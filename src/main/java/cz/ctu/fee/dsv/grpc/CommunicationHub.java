package cz.ctu.fee.dsv.grpc;

import cz.ctu.fee.dsv.grpc.base.Address;
import cz.ctu.fee.dsv.grpc.base.DSNeighbours;
import cz.ctu.fee.dsv.grpc.base.NodeCommands;

public class CommunicationHub {
    private DSNeighbours actNeighbours = null;
    private Address myAddress = null;
    private NodeCommands myMessageReceiver = null;


//    public CommunicationHub (Node node) {
//        this.myAddress = node.getAddress();
//        this.actNeighbours = node.getNeighbours();
//        this.myMessageReceiver = node.getMessageReceiver();
//    }
//
//
//    public NodeCommands getNext() throws RemoteException {
//        return getRMIProxy(actNeighbours.next);
//    }
//
//
//    public NodeCommands getNNext() throws RemoteException {
//        return getRMIProxy(actNeighbours.nnext);
//    }
//
//
//    public NodeCommands getPrev() throws RemoteException {
//        return getRMIProxy(actNeighbours.prev);
//    }
//
////    public NodeCommands getLeader() throws RemoteException {
////        return getRMIProxy(actNeighbours.leader);
////    }
//
//    public NodeCommands getRMIProxy(Address address) throws RemoteException {
//        if (address.compareTo(myAddress) == 0 ) return myMessageReceiver;
//        else {
//            try {
//                Registry registry = LocateRegistry.getRegistry(address.hostname, address.port);
//                return (NodeCommands) registry.lookup(Node.COMM_INTERFACE_NAME);
//            } catch (NotBoundException nbe) {
//                // transitive RM exception
//                throw new RemoteException(nbe.getMessage());
//            }
//        }
//    }
//

    public void setActNeighbours(DSNeighbours actNeighbours) {
        this.actNeighbours = actNeighbours;
    }
}