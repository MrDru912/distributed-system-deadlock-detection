package cz.ctu.fee.dsv.grpc.mappers;

import cz.ctu.fee.dsv.AddressProto;
import cz.ctu.fee.dsv.DSNeighboursProto;
import cz.ctu.fee.dsv.grpc.base.Address;
import cz.ctu.fee.dsv.grpc.base.DSNeighbours;

public class ProtobufMapper {

    public static AddressProto AddressToProto(Address address) {
        return AddressProto.newBuilder()
                .setHostname(address.hostname)
                .setPort(address.port)
                .build();
    }

    public static Address fromProtoToAddress(AddressProto addressProto) {
        return new Address(
                addressProto.getHostname(),
                addressProto.getPort()
        );
    }

    public static DSNeighboursProto DSNeighboursToProto(DSNeighbours dsNeighbours) {
        return DSNeighboursProto.newBuilder()
                .setNext(AddressToProto(dsNeighbours.next))
                .setNnext(AddressToProto(dsNeighbours.nnext))
                .setPrev(AddressToProto(dsNeighbours.prev))
                .setLeader(AddressToProto(dsNeighbours.leader))
                .build();
    }

    public static DSNeighbours fromProtoToDSNeighbours(DSNeighboursProto dsNeighboursProto) {
        return new DSNeighbours(
                fromProtoToAddress(dsNeighboursProto.getNext()),
                fromProtoToAddress(dsNeighboursProto.getNnext()),
                fromProtoToAddress(dsNeighboursProto.getPrev()),
                fromProtoToAddress(dsNeighboursProto.getLeader())
                );
    }

}
