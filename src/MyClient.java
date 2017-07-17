import lenz.htw.kipifub.ColorChange;
import lenz.htw.kipifub.net.NetworkClient;

import java.util.*;
import java.util.concurrent.Callable;


/**
 * Created by Maximilian on 04.07.2017.
 */
public class MyClient { //} implements Callable<Void> {

    private static final int FIELD_SIZE = 1024;
    private static final int BLOCK_SIZE = 20;
    private String hostName;
    private String teamName;
    private int myPlayerNr;
    private Position botPositions[][];
    private NetworkClient networkClient;
    private Graph graph;
    private Vertex[][] vertexArray;
    private Vertex[] botDestinations;
    private Vertex[] nextBotStopover;
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
        botDestinations = new Vertex[3];
        nextBotStopover = new Vertex[3];
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
        Random random = new Random();
        ColorChange colorChange;
        boolean firstColorChange = false;

        while (!firstColorChange) {
            networkClient.setMoveDirection(0, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            networkClient.setMoveDirection(1, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            networkClient.setMoveDirection(2, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            if (networkClient.pullNextColorChange() != null) firstColorChange = true;
        }

        while (true) {
            networkClient.setMoveDirection(0, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            networkClient.setMoveDirection(1, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);

            if (botPositions[myPlayerNr][0] != null && botPositions[myPlayerNr][1] != null && botPositions[myPlayerNr][2] != null) {

                int botNr = 2;
                if (botPaths.get(botNr) == null || isAtVertex(botNr, botDestinations[botNr])) {
                    Vertex v = vertexArray[30][15] == null ? vertexArray[30][15] : vertexArray[25][20]; //TODO: calculate actual destination for bots
                    botPaths.put(botNr, getPathToVertex(botNr, v));
                }
                if (botPaths.get(botNr) != null && isAtVertex(botNr, botPaths.get(botNr).get(0))) {
                    botPaths.get(botNr).remove(0);
                    if (botPaths.get(botNr).isEmpty()) {
                        Vertex v = vertexArray[30][15] == null ? vertexArray[30][15] : vertexArray[25][20]; //TODO: calculate actual destination for bots
                        botPaths.put(botNr, getPathToVertex(botNr, v));
                    }
                    moveToVertex(botNr, botPaths.get(botNr).get(0));
                } else if (botPaths.get(botNr) != null && !isAtVertex(botNr, botPaths.get(botNr).get(0))) {
                    networkClient.setMoveDirection(botNr, 0, 0);
                    moveToVertex(botNr, botPaths.get(botNr).get(0));
                }
            }

            while ((colorChange = networkClient.pullNextColorChange()) != null) {
                botPositions[colorChange.player][colorChange.bot] = new Position(colorChange.x, colorChange.y);
            }

            //System.out.println("stop");
        }
        //return null;
    }

    private boolean isAtVertex(int botNr, Vertex v) {
        if (botPositions[myPlayerNr][botNr] == null) {
            return false;
        }
        Vertex botVertex = getBotVertex(botNr);

        if (botVertex == null) {
            moveRandom(botNr);
        }

        if (botVertex != null && botVertex.equals(v)) {
            return true;
        }
        return false;
    }

    private void moveRandom(int botNr) {
        Random random = new Random();
        networkClient.setMoveDirection(botNr, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
        System.out.println("botVertex null");
    }

    private Vertex getBotVertex(int botNr) {
        Position botPos = botPositions[myPlayerNr][botNr];
        return vertexArray[botPos.x / BLOCK_SIZE][botPos.y / BLOCK_SIZE];
    }

    private List<Vertex> getPathToVertex(int botNr, Vertex destination) {
        AStar aStar = new AStar();
        Position botPosition = botPositions[myPlayerNr][botNr];
        Vertex start = vertexArray[botPosition.x / BLOCK_SIZE][botPosition.y / BLOCK_SIZE];
        return aStar.getShortestPath(graph, start, destination);
    }

    private void moveToVertex(int botNr, Vertex v) {
        Vertex botVertex = getBotVertex(botNr);
        if (botVertex == null) {
            moveRandom(botNr);
        } else {
            float x, y;
            if (v.x - botVertex.x == 0) {
                x = 0;
            } else if (v.x - botVertex.x < 0) {
                x = (float) Math.max(-1, v.x - botVertex.x);
            } else {
                x = (float) Math.min(1, v.x - botVertex.x);
            }
        /*if (botVertex.y - v.y == 0) {
            y = 0;
        } else if (botVertex.y - v.y < 0) {
            y = (float) Math.max(-1, botVertex.y - v.y);
        } else {
            y = (float) Math.min(1, botVertex.y - v.y);
        }*/
            if (v.y - botVertex.y == 0) {
                y = 0;
            } else if (v.y - botVertex.y < 0) {
                y = (float) Math.max(-1, v.y - botVertex.y);
            } else {
                y = (float) Math.min(1, v.y - botVertex.y);
            }
            networkClient.setMoveDirection(botNr, x, y);
            System.out.println("move to x= " + x + " y= " + y);
        }
    }

    private Graph initGraph(Vertex[][] vertexArray) {
        Graph graph = new Graph();
        for (int y = 0; y < vertexArray.length; y++) {
            for (int x = 0; x < vertexArray.length; x++) {
                if (x < vertexArray.length - 1 && vertexArray[x][y] != null && vertexArray[x + 1][y] != null) {
                    graph.addEdge(vertexArray[x][y],vertexArray[x + 1][y]);
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
}
