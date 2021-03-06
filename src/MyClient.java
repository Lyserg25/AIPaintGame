import lenz.htw.kipifub.ColorChange;
import lenz.htw.kipifub.net.NetworkClient;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;


/**
 * Created by Maximilian on 04.07.2017.
 */
public class MyClient implements Callable<Void> {

    private static final int FIELD_SIZE = 1024;
    private static final int BLOCK_SIZE = 16;
    private String hostName;
    private String teamName;
    private int myPlayerNr;
    private Position botPositions[][];
    private Vertex botVertices[][];
    private NetworkClient networkClient;
    private Graph graph;
    private Vertex[][] vertexArray;
    private Map<Integer, List<Vertex>> botPaths;
    private Queue<Vertex> bestLocations;

    public MyClient(String hostname, String teamName) {
        this.hostName = hostname;
        this.teamName = teamName;
    }


    @Override
    public Void call() {
        networkClient = new NetworkClient(hostName, teamName);
        myPlayerNr = networkClient.getMyPlayerNumber();
        botPositions = new Position[3][3];
        botVertices = new Vertex[3][3];
        botPaths = new HashMap<>();
        bestLocations = new PriorityQueue<>(new VertexComparator());
        vertexArray = initVertexArray();
        graph = initGraph(vertexArray);
        Random random = new Random();
        ColorChange colorChange;

        while (botPositions[myPlayerNr][0] == null || botPositions[myPlayerNr][1] == null || botPositions[myPlayerNr][2] == null) {
            networkClient.setMoveDirection(0, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            networkClient.setMoveDirection(1, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            networkClient.setMoveDirection(2, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            if ((colorChange = networkClient.pullNextColorChange()) != null) {
                setBotPosition(colorChange);
            }
        }

        Timer botDestinationTimer = new Timer();
        botDestinationTimer.schedule(new CalcBotDestinations(), 0, 1000);

        while (true) {

            for (int botNr = 0; botNr < 3; botNr++) {
                if (botVertices[myPlayerNr][botNr] == null || bestLocations.isEmpty()) {
                    networkClient.setMoveDirection(botNr, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
                } else {
                    if (botPaths.get(botNr) == null) {
                        botPaths.put(botNr, getPathToVertex(botNr, bestLocations.poll()));
                    } else if (isAtVertex(botNr, botPaths.get(botNr).get(0))) {
                        botPaths.get(botNr).remove(0);
                        if (botPaths.get(botNr).isEmpty()) {
                            botPaths.put(botNr, getPathToVertex(botNr, bestLocations.poll()));
                        }
                        moveToVertex(botNr, botPaths.get(botNr).get(0));
                    } else {
                        Position botPos = botPositions[myPlayerNr][botNr];
                        if (vertexArray[botPos.x / BLOCK_SIZE][botPos.y / BLOCK_SIZE] == null) {
                            networkClient.setMoveDirection(botNr, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
                        } else {
                            moveToVertex(botNr, botPaths.get(botNr).get(0));
                        }
                    }
                }
            }
            while ((colorChange = networkClient.pullNextColorChange()) != null) {
                setBotPosition(colorChange);
            }
        }
    }

    private boolean isAtVertex(int botNr, Vertex v) {
        if (v == null) {
            return false;
        }
        Vertex botVertex = botVertices[myPlayerNr][botNr];
        if (botVertex.equals(v)) {
            return true;
        }
        if (graph.getNeighbours(v).containsKey(botVertex)) {
            return true;
        }
        if (botNr == 0) {
            Set<Vertex> neighboursNeighbours = new HashSet<>();
            for (Vertex vertex : graph.getNeighbours(v).keySet()) {
                neighboursNeighbours.addAll(graph.getNeighbours(vertex).keySet());
            }
            if (neighboursNeighbours.contains(botVertex)) {
                return true;
            }
        }
        return false;
    }

    private List<Vertex> getPathToVertex(int botNr, Vertex destination) {
        AStar aStar = new AStar();
        Vertex start = botVertices[myPlayerNr][botNr];
        List<Vertex> path = aStar.getShortestPath(graph, start, destination);
        return removeIntermediateVertices(path);
    }

    private List<Vertex> removeIntermediateVertices(List<Vertex> path) {
        if (path == null || path.size() < 3) {
            return path;
        }
        List<Vertex> shortenedPath = new ArrayList<>();
        Vertex lastAdded = path.get(0);
        shortenedPath.add(lastAdded);

        for (int i = 1; i < path.size(); i++) {
            Vertex v = path.get(i);
            if (v.x != lastAdded.x && v.y != lastAdded.y) {
                lastAdded = path.get(i - 1);
                shortenedPath.add(lastAdded);
            }
        }
        shortenedPath.add(path.get(path.size() - 1));
        return shortenedPath;
    }

    private void moveToVertex(int botNr, Vertex v) {
        Vertex botVertex = botVertices[myPlayerNr][botNr];
        float x = getMovementValue(v.x, botVertex.x);
        float y = getMovementValue(v.y, botVertex.y);
        networkClient.setMoveDirection(botNr, x, y);
    }

    private float getMovementValue(int value1, int value2) {
        if (value1 - value2 == 0) {
            return 0;
        } else if (value1 - value2 < 0) {
            return (float) Math.max(-1, value1 - value2);
        } else {
            return (float) Math.min(1, value1 - value2);
        }
    }

    private Graph initGraph(Vertex[][] vertexArray) {
        Graph graph = new Graph();
        for (int y = 0; y < vertexArray.length; y++) {
            for (int x = 0; x < vertexArray.length; x++) {
                if (x < vertexArray.length - 1 && vertexArray[x][y] != null && vertexArray[x + 1][y] != null) {
                    graph.addEdge(vertexArray[x][y], vertexArray[x + 1][y]);
                }
                if (y < vertexArray.length - 1 && vertexArray[x][y] != null && vertexArray[x][y + 1] != null) {
                    graph.addEdge(vertexArray[x][y], vertexArray[x][y + 1]);
                }
            }
        }
        return graph;
    }

    private Vertex[][] initVertexArray() {
        int vertexGridSize = FIELD_SIZE % BLOCK_SIZE == 0 ? FIELD_SIZE / BLOCK_SIZE : FIELD_SIZE / BLOCK_SIZE + 1;
        Vertex[][] vertexArray = new Vertex[vertexGridSize][vertexGridSize];
        int xFrom, yFrom, xTo, yTo;
        int x = 0;
        int y = 0;

        for (yFrom = 0; yFrom < FIELD_SIZE; yFrom = yTo + 1, y++, x = 0) {
            yTo = yFrom + BLOCK_SIZE - 1;

            if (yTo >= FIELD_SIZE) {
                yTo = FIELD_SIZE - 1;
            }
            for (xFrom = 0; xFrom < FIELD_SIZE; xFrom = xTo + 1, x++) {
                xTo = xFrom + BLOCK_SIZE - 1;

                if (xTo >= FIELD_SIZE) {
                    xTo = FIELD_SIZE - 1;
                }
                if (isWalkableBlock(xFrom, yFrom, xTo, yTo)) {
                    vertexArray[x][y] = new Vertex(xFrom, yFrom);
                }
            }
        }
        return vertexArray;
    }

    private boolean isWalkableBlock(int xFrom, int yFrom, int xTo, int yTo) {
        for (int y = yFrom; y <= yTo; y++) {
            for (int x = xFrom; x <= xTo; x++) {
                if (!networkClient.isWalkable(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }


    public void setBotPosition(ColorChange colorChange) {
        Position botPos = botPositions[colorChange.player][colorChange.bot] = new Position(colorChange.x, colorChange.y);
        Vertex botVertex = vertexArray[botPos.x / BLOCK_SIZE][botPos.y / BLOCK_SIZE];
        if (botVertex != null) {
            botVertices[colorChange.player][colorChange.bot] = botVertex;
        }
    }


    private class Position {
        public final int x;
        public final int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (!Vertex.class.isAssignableFrom(o.getClass())) {
                return false;
            }
            final Vertex v = (Vertex) o;
            return this.x == v.x && this.y == v.y;
        }
    }

    private class CalcBotDestinations extends TimerTask {

        private List<Vertex> enemyVertices;
        private List<Vertex> enemyFirstDegreeNeighbourVertices;
        private List<Vertex> enemySecondDegreeNeighbourVertices;
        private List<Vertex> enemyThirdDegreeNeighbourVertices;

        @Override
        public void run() {
            //long start = System.currentTimeMillis();
            setVertexColors();
            getEnemyVertices();
            setVertexScores();
            graph.recalculateWeights();
            setBestLocations();
            //System.out.println(System.currentTimeMillis() - start);
            //System.out.println("x=" + bestLocations.peek().x + " y=" + bestLocations.peek().y);
        }

        private void setVertexScores() {
            Vertex v;
            double score;
            for (int y = 0; y < vertexArray.length; y++) {
                for (int x = 0; x < vertexArray.length; x++) {
                    v = vertexArray[x][y];
                    if (v == null) {
                    } else if (enemyVertices.contains(v)) {
                        v.setScore(50);
                    } else {
                        double colorSum = v.getRed() + v.getGreen() + v.getBlue();
                        if (myPlayerNr == 0) {
                            score = 1 - v.getRed() / colorSum;
                        } else if (myPlayerNr == 1) {
                            score = 1 - v.getGreen() / colorSum;
                        } else {
                            score = 1 - v.getBlue() / colorSum;
                        }
                        for (Vertex enemyNeighbourVertex : enemyFirstDegreeNeighbourVertices) {
                            if (v.equals(enemyNeighbourVertex)) {
                                score = score * 0.25;
                            }
                        }
                        for (Vertex enemyNeighbourNeighbourVertex : enemySecondDegreeNeighbourVertices) {
                            if (v.equals(enemyNeighbourNeighbourVertex)) {
                                score = score * 0.5;
                            }
                        }
                        for (Vertex enemyNeighbourNeighbourVertex : enemyThirdDegreeNeighbourVertices) {
                            if (v.equals(enemyNeighbourNeighbourVertex)) {
                                score = score * 0.75;
                            }
                        }
                        score = (1 - score) * 50;
                        v.setScore(score);
                    }
                }
            }
        }

        private void setBestLocations() {
            Queue<Vertex> verticesSorted = new PriorityQueue<>(new VertexComparator());
            verticesSorted.addAll(graph.getVertices());
            bestLocations = verticesSorted;
        }

        private void setVertexColors() {
            int xFrom, yFrom, xTo, yTo;
            int x = 0;
            int y = 0;

            for (yFrom = 0; yFrom < FIELD_SIZE; yFrom = yTo + 1, y++, x = 0) {
                yTo = yFrom + BLOCK_SIZE - 1;

                if (yTo >= FIELD_SIZE) {
                    yTo = FIELD_SIZE - 1;
                }
                for (xFrom = 0; xFrom < FIELD_SIZE; xFrom = xTo + 1, x++) {
                    xTo = xFrom + BLOCK_SIZE - 1;

                    if (xTo >= FIELD_SIZE) {
                        xTo = FIELD_SIZE - 1;
                    }
                    if (vertexArray[x][y] != null) {
                        setVertexColorSums(vertexArray[x][y], xFrom, yFrom, xTo, yTo);
                    }
                }
            }
        }

        private void setVertexColorSums(Vertex v, int xFrom, int yFrom, int xTo, int yTo) {
            int red = 0;
            int green = 0;
            int blue = 0;
            for (int y = yFrom; y < yTo; y++) {
                for (int x = xFrom; x < xTo; x++) {
                    int rgb = networkClient.getBoard(x, y);
                    red += (rgb >> 16) & 255;
                    green += (rgb >> 8) & 255;
                    blue += rgb & 255;
                }
            }
            int size = BLOCK_SIZE * BLOCK_SIZE;
            v.setRed(red);
            v.setGreen(green);
            v.setBlue(blue);
            v.setAverageColor(new Color(red / size, green / size, blue / size));
        }

        private void getEnemyVertices() {
            enemyVertices = new ArrayList<>();
            enemyFirstDegreeNeighbourVertices = new ArrayList<>();
            enemySecondDegreeNeighbourVertices = new ArrayList<>();
            enemyThirdDegreeNeighbourVertices = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                for (int i = 0; i < 3; i++) {
                    if (j != myPlayerNr && botVertices[j][i] != null) {
                        Vertex v = botVertices[j][i];
                        enemyVertices.add(v);
                        enemyFirstDegreeNeighbourVertices.addAll(graph.getNeighbours(v).keySet());

                        for (Vertex vertex : enemyFirstDegreeNeighbourVertices) {
                            enemySecondDegreeNeighbourVertices.addAll(graph.getNeighbours(vertex).keySet());
                        }
                        enemySecondDegreeNeighbourVertices.removeAll(enemyVertices);
                        enemySecondDegreeNeighbourVertices.removeAll(enemyFirstDegreeNeighbourVertices);

                        for (Vertex vertex : enemySecondDegreeNeighbourVertices) {
                            enemyThirdDegreeNeighbourVertices.addAll(graph.getNeighbours(vertex).keySet());
                        }
                        enemyThirdDegreeNeighbourVertices.removeAll(enemyVertices);
                        enemyThirdDegreeNeighbourVertices.removeAll(enemyFirstDegreeNeighbourVertices);
                        enemyThirdDegreeNeighbourVertices.removeAll(enemySecondDegreeNeighbourVertices);
                    }
                }
            }
        }
    }
}
