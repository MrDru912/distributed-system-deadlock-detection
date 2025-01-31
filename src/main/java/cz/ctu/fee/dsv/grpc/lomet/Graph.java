package cz.ctu.fee.dsv.grpc.lomet;

import java.util.*;

public class Graph {
    private final Map<String, List<String>> adjacencyList;

    // Constructor to initialize the adjacency list
    public Graph() {
        adjacencyList = new HashMap<>();
    }

    // Method to add a new vertex to the graph
    public void addVertex(String vertex) {
        adjacencyList.put(vertex, new ArrayList<>());
    }

    // Method to add an edge between two vertices
    public void addEdge(String source, String destination) {
        adjacencyList.get(source).add(destination);
    }

    // Method to remove a vertex from the graph
    public void removeVertex(String vertex) {
        adjacencyList.remove(vertex);
        // Remove the vertex from the neighbors of other vertices
        for (List<String> neighbors : adjacencyList.values()) {
            neighbors.remove(vertex);
        }
    }

    // Method to remove an edge between two vertices
    public void removeEdge(String source, String destination) {
        adjacencyList.get(source).remove(destination);

        // For undirected graph, uncomment below line
        // adjacencyList.get(destination).remove(source);
    }

    // Method to get the neighbors of a vertex
    public List<String> getNeighbors(String vertex) {
        return adjacencyList.get(vertex);
    }

    // Method to print the graph
    public void printGraph() {
        for (Map.Entry<String, List<String>> entry : adjacencyList.entrySet()) {
            System.out.print(entry.getKey() + " -> ");
            for (String neighbor : entry.getValue()) {
                System.out.print(neighbor + " ");
            }
            System.out.println();
        }
    }

    // Method to detect cycles in the graph
    public boolean hasCycle() {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        // Check for cycles starting from each unvisited vertex
        for (String node : adjacencyList.keySet()) {
            if (hasCycleUtil(node, visited, recursionStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCycleUtil(String node, Set<String> visited, Set<String> recursionStack) {
        // If node is already in recursion stack, we found a cycle
        if (recursionStack.contains(node)) {
            return true;
        }

        // If node is already visited and not in recursion stack, no cycle through this path
        if (visited.contains(node)) {
            return false;
        }

        // Add the node to both visited and recursion stack
        visited.add(node);
        recursionStack.add(node);

        // Visit all neighbors
        for (String neighbor : adjacencyList.get(node)) {
            if (hasCycleUtil(neighbor, visited, recursionStack)) {
                return true;
            }
        }

        // Remove the node from recursion stack as we're done exploring all paths through it
        recursionStack.remove(node);
        return false;
    }

}