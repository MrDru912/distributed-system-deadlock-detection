package cz.ctu.fee.dsv.grpc.exceptions;

public class ResourceNotFound extends Exception{

    public ResourceNotFound(String errorMessage) {
        super(errorMessage);
    }
}
