import lenz.htw.kipifub.ColorChange;
import lenz.htw.kipifub.net.NetworkClient;

import java.util.*;


/**
 * Created by Maximilian on 04.07.2017.
 */
public class MyClient { //} implements Callable<Void> {

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
    private Vertex[] botDestinations;
    private Map<Integer, List<Vertex>> botPaths;

    /*public MyClient(String hostname, String teamName) {
        this.hostName = hostname;
        this.teamName = teamName;
    }*/

    public MyClient() {
        call();
    }

    //@Override
    public Void call() {
        //networkClient = new NetworkClient(hostName, teamName);
        networkClient = new NetworkClient(hostName, "Lyserg25");
        myPlayerNr = networkClient.getMyPlayerNumber();
        botPositions = new Position[3][3];
        botVertices = new Vertex[3][3];
        botDestinations = new Vertex[3];
        botPaths = new HashMap<>();

        vertexArray = initVertexArray();
        graph = initGraph(vertexArray);


        //networkClient.setMoveDirection(2, 1, 0);

//        int rgb = networkClient.getBoard(x, y); // 0-1023 ->
//        int b = rgb & 255;
//        int g = (rgb >> 8) & 255;
//        int r = (rgb >> 16) & 255;
//
//        networkClient.getInfluenceRadiusForBot(0); // -> 40
//
//        networkClient.getScore(0); // Punkte von rot
//
//        networkClient.isWalkable(x, y); // begehbar oder Hinderniss?
//
//        networkClient.setMoveDirection(0, 1, 0); // bot 0 nach rechts
//        networkClient.setMoveDirection(1, 0.23f, -0.52f); // bot 1 nach rechts unten
//

        Timer botDestinationTimer = new Timer();
        botDestinationTimer.schedule(new CalcBotDestinations(), 2000, 2000);

        Random random = new Random();
        ColorChange colorChange;

        while (botPositions[myPlayerNr][0] == null || botPositions[myPlayerNr][1] == null || botPositions[myPlayerNr][2] == null) {
            networkClient.setMoveDirection(0, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            networkClient.setMoveDirection(1, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            networkClient.setMoveDirection(2, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            if ((colorChange = networkClient.pullNextColorChange()) != null) { //firstColorChange = true;
                setBotPosition(colorChange);
            }
        }




        /*Thread t = new Thread(new calcBotDestinations(networkClient));
        t.start();*/


//        Timer slowDownTimer = new Timer();
//        slowDownTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                networkClient.setMoveDirection(0, 0, 0);
//                networkClient.setMoveDirection(1, 0, 0);
//            }
//        }, 5, 5);

        while (true) {

            for (int botNr = 0; botNr < 3; botNr++) {
                if (botVertices[myPlayerNr][botNr] == null) {
                    networkClient.setMoveDirection(botNr, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
                } else {
                    //TODO refactor this ->
                    if (botPaths.get(botNr) == null || isAtVertex(botNr, botDestinations[botNr])) {
                        Vertex v = getNextDestination(botNr);
                        botPaths.put(botNr, getPathToVertex(botNr, v));
                    }
                    if (botPaths.get(botNr) != null && isAtVertex(botNr, botPaths.get(botNr).get(0))) {
                        botPaths.get(botNr).remove(0);
                        if (botPaths.get(botNr).isEmpty()) {
                            Vertex v = getNextDestination(botNr);
                            botPaths.put(botNr, getPathToVertex(botNr, v));
                        }
                        moveToVertex(botNr, botPaths.get(botNr).get(0));
                    } else if (botPaths.get(botNr) != null && !isAtVertex(botNr, botPaths.get(botNr).get(0))) {
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

    private Vertex getNextDestination(int botNr) {
        for (int x = 20; x < 40; x++) {
            if (vertexArray[x][15] != null) return vertexArray[x][15];
        }
        for (int x = 20; x < 40; x++) {
            if (vertexArray[x][30] != null) return vertexArray[x][30];
        }
        return vertexArray[25][40];
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
            for (Object vertex : graph.getNeighbours(v).keySet()) {
                neighboursNeighbours.addAll((Set<Vertex>) graph.getNeighbours(((Vertex) vertex)).keySet());
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

//        if (botNr == 0) {
//            Timer slowDownTimer = new Timer();
//            slowDownTimer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    networkClient.setMoveDirection(0, 0, 0);
//                }
//            }, 4);
//        }
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

//    private class calcBotDestinations implements Runnable {
//
//        private NetworkClient networkClient;
//
//        public calcBotDestinations(NetworkClient networkClient) {
//            this.networkClient = networkClient;
//        }
//
//        public void run() {
//            ColorChange colorChange;
//            while (true) {
//                while ((colorChange = networkClient.pullNextColorChange()) != null) {
//                    botPositions[colorChange.player][colorChange.bot] = new Position(colorChange.x, colorChange.y);
//                }
//            }
//        }
//    }

    private class CalcBotDestinations extends TimerTask {

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            int[][] field = new int[FIELD_SIZE][FIELD_SIZE];

            for (int y = 0; y < FIELD_SIZE; y++) {
                for (int x = 0; x < FIELD_SIZE; x++) {
                    field[x][y] = networkClient.getBoard(x, y);
                }
            }
            QuadTreeNode quadTree = new QuadTreeNode(field, 0, 0, FIELD_SIZE, FIELD_SIZE);
            getBestLocations(quadTree);

            System.out.println(System.currentTimeMillis() - start);
        }

        private List<Vertex> getBestLocations(QuadTreeNode quadTree) {
            return null;
        }
    }
}
