package cz.ctu.fee.dsv.grpc.messages;

public class RequestMessage {
    private String resourceId;

    public RequestMessage(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
