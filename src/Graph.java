import java.util.*;

/**
 * Created by Maximilian on 11.07.2017.
 */
public class Graph<Vertex> {
    private Map<Vertex, Map<Vertex, Double>>  graph;
    private Set<Vertex> vertices;
    private static final Double DEFAULT_WEIGHT = 1.0;

    public Graph() {
        this.graph = new HashMap<>();
        this.vertices = new HashSet<>();
    }

    public void addVertex(Vertex v) {
        if (!vertices.contains(v)) {
            vertices.add(v);
            graph.put(v, new HashMap<>());
        }
    }

    public void addEdge(Vertex v1, Vertex v2) {
        addEdge(v1, v2, DEFAULT_WEIGHT);
    }

    public void addEdge(Vertex v1, Vertex v2, Double weight) {
        addVertex(v1);
        addVertex(v2);
        graph.get(v1).put(v2, weight);
        graph.get(v2).put(v1, weight);
    }

    public Double getWeight(Vertex v1, Vertex v2) {
        return graph.get(v1).get(v2);
    }

    public Map<Vertex, Double> getNeighbours(Vertex v) {
        return Collections.unmodifiableMap(graph.get(v));
    }

    public Map<Vertex, Map<Vertex, Double>> getGraph() {
        return Collections.unmodifiableMap(graph);
    }

    public List<Vertex> getVertices() {
        List<Vertex> vertexList = new ArrayList<>(vertices);
        return vertexList;
    }
}

