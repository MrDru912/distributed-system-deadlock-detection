package cz.ctu.fee.dsv.grpc.messages;

public class AcquireMessage {
    private String resourceId;

    public AcquireMessage(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
