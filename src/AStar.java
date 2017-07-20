import java.util.*;

/**
 * Created by Maximilian on 15.07.2017.
 */
public class AStar {
    private Graph<Vertex> graph;
    private Queue<Vertex> openList;
    private Set<Vertex> closedList;
    private Map<Vertex, Vertex> predecessors;
    private Map<Vertex, Double> fScore;
    private Map<Vertex, Double> gScore;
    private Vertex destination;

    public AStar() {}

    public List<Vertex> getShortestPath(Graph<Vertex> graph, Vertex start, Vertex end) {
        initAStar(graph, start, end);
        Vertex currentVertex;

        while (!openList.isEmpty()) {
            currentVertex = openList.poll();

            if (currentVertex.equals(end)) {
                return getPathToVertex(currentVertex);
            }
            closedList.add(currentVertex);
            expandVertex(currentVertex);
        }
        return null;
    }

    private void initAStar(Graph<Vertex> graph, Vertex start, Vertex destination) {
        //TODO: refactoring
        this.graph = graph;
        this.destination = destination;
        this.openList = new PriorityQueue<>(new FScoreComparator());
        this.closedList = new HashSet<>();
        this.predecessors = new HashMap<>();
        this.gScore = new HashMap<>();
        this.fScore = new HashMap<>();
        openList.add(start);
        gScore.put(start, 0.0);

        for (Vertex v : graph.getVertices()) {
            fScore.put(v, Double.MAX_VALUE);
        }
        fScore.put(start, (double) getHeuristik(start));
    }

    private void expandVertex(Vertex currentVertex) {
        Double tentativeG;
        Map<Vertex, Double> successors = graph.getNeighbours(currentVertex);
        for (Vertex successor : successors.keySet()) {
            if (closedList.contains(successor)) {
                continue;
            }
            tentativeG = gScore.get(currentVertex) + (successors.get(successor) == null ? Integer.MAX_VALUE : successors.get(successor));

            if (openList.contains(successor) && tentativeG <= gScore.get(successor)) {
                continue;
            }
            predecessors.put(successor, currentVertex);
            gScore.put(successor, tentativeG);
            fScore.put(successor, gScore.get(successor) + getHeuristik(successor));

            if (openList.contains(successor)) {
                openList.remove(successor);
            }
            openList.add(successor);
        }
    }

    private List<Vertex> getPathToVertex(Vertex currentVertex) {
        List<Vertex> path = new ArrayList<>();

        while (currentVertex != null) {
            path.add(currentVertex);
            currentVertex = predecessors.get(currentVertex);
        }
        Collections.reverse(path);
        return path;
    }

    private int getHeuristik(Vertex v) {
        return Math.abs(v.x - destination.x) + Math.abs(v.y - destination.y);
    }

    private class FScoreComparator implements Comparator<Vertex> {

        @Override
        public int compare(Vertex v1, Vertex v2) {
            return Double.compare(fScore.get(v1), fScore.get(v2));
        }
    }
}
