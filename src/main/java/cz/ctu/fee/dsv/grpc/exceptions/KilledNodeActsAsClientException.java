package cz.ctu.fee.dsv.grpc.exceptions;

public class KilledNodeActsAsClientException extends Exception{

    public KilledNodeActsAsClientException(String errorMessage) {
        super(errorMessage);
    }
}
