package cz.ctu.fee.dsv.grpc.utils;

import cz.ctu.fee.dsv.AddressProto;

public class Utils {
    public static String getProcessId(String hostname, int port){
        return hostname + ":" + port;
    }

    public static String getProcessId(AddressProto addressProto){
        return addressProto.getHostname() + ":" + addressProto.getPort();
    }

}
