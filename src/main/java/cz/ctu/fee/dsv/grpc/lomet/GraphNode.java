package cz.ctu.fee.dsv.grpc.lomet;

public class GraphNode {
    private String id;

    public GraphNode(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
