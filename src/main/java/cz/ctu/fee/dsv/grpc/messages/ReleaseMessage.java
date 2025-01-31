package cz.ctu.fee.dsv.grpc.messages;

public class ReleaseMessage {
    private String resourceId;

    public ReleaseMessage(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
