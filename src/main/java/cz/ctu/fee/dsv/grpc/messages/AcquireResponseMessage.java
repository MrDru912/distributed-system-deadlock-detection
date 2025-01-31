package cz.ctu.fee.dsv.grpc.messages;

import cz.ctu.fee.dsv.grpc.base.Address;
import cz.ctu.fee.dsv.grpc.resources.Resource;

public class AcquireResponseMessage {
    private Resource resource;
    private Address returnAddress;

    public AcquireResponseMessage(Resource resource, Address returnAddress) {
        this.resource = resource;
        this.returnAddress = returnAddress;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Address getReturnAddress() {
        return returnAddress;
    }

    public void setReturnAddress(Address returnAddress) {
        this.returnAddress = returnAddress;
    }
}
