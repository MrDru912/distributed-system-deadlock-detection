package cz.ctu.fee.dsv.grpc.base;

import cz.ctu.fee.dsv.AddressProto;
import cz.ctu.fee.dsv.DSNeighboursProto;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeCommands extends Remote {
        DSNeighboursProto join(AddressProto addr);
        void chngNNext(AddressProto addr);
        void chngNext(AddressProto addr);
        AddressProto chngPrev(AddressProto addr);
        void chngNNextOfPrev(AddressProto addr);
        void nodeMissing(AddressProto addr);
        void nodeLeft(AddressProto addrProto);
        void hello();
}