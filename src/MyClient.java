import lenz.htw.kipifub.ColorChange;
import lenz.htw.kipifub.net.NetworkClient;

import java.util.concurrent.Callable;


/**
 * Created by Maximilian on 04.07.2017.
 */
public class MyClient implements Callable<Void> {

    private static final int FIELD_SIZE = 1024;
    private String hostName;
    private String teamName;
    private int myPlayerNr;

    public MyClient(String hostname, String teamName) {
        this.hostName = hostname;
        this.teamName = teamName;
    }

    @Override
    public Void call() {
        NetworkClient networkClient = new NetworkClient(hostName, teamName);
        myPlayerNr = networkClient.getMyPlayerNumber();
        Graph graph = initGraph(networkClient,20);
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

        ColorChange colorChange;
        while ((colorChange = networkClient.pullNextColorChange()) != null) {
            //verarbeiten von colorChange
            //colorChange.player, colorChange.bot, colorChange.x, colorChange.y
        }
        return null;
    }

    private Graph initGraph(NetworkClient networkClient, int blockSize) {
        Graph graph = new Graph();
        Vertex[][] vertexArray = getVertexArray(networkClient, blockSize);
        for (int y = 0; y < vertexArray.length; y++) {
            for (int x = 0; x < vertexArray.length; x++) {
                if (x < vertexArray.length - 1 && vertexArray[x][y] != null && vertexArray[x + 1][y] != null) {
                    graph.addEdge(vertexArray[x][y],vertexArray[x + 1][y]);
                }
                if (y < vertexArray.length - 1 && vertexArray[x][y] != null && vertexArray[x][y + 1] != null) {
                    graph.addEdge(vertexArray[x][y], vertexArray[x][y + 1]);
                }

                //diagogalen?
            }
        }

        return graph;
    }

    private Vertex[][] getVertexArray(NetworkClient networkClient, int blockSize) {
        int vertexGridSize = FIELD_SIZE % blockSize == 0 ? FIELD_SIZE / blockSize : FIELD_SIZE / blockSize + 1;
        Vertex[][] vertexArray = new Vertex[vertexGridSize][vertexGridSize];
        int xFrom, yFrom, xTo, yTo;
        int x = 0;
        int y = 0;

        for (yFrom = 0; yFrom < FIELD_SIZE; yFrom = yTo + 1, y++, x = 0) {
            yTo = yFrom + blockSize - 1;

            if (yTo >= FIELD_SIZE) {
                yTo = FIELD_SIZE - 1;
            }

            for (xFrom = 0; xFrom < FIELD_SIZE; xFrom = xTo + 1, x++) {
                xTo = xFrom + blockSize - 1;

                if (xTo >= FIELD_SIZE) {
                    xTo = FIELD_SIZE - 1;
                }
                if (isWalkableBlock(networkClient, xFrom, yFrom, xTo, yTo)) {
                    vertexArray[x][y] = new Vertex(xFrom, yFrom);
                }
            }
        }
        return vertexArray;
    }

    private boolean isWalkableBlock(NetworkClient networkClient, int xFrom, int yFrom, int xTo, int yTo) {
        for (int y = yFrom; y <= yTo; y++) {
            for (int x = xFrom; x <= xTo; x++) {
                if (!networkClient.isWalkable(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean[][] initWalkableField(NetworkClient networkClient) {
        boolean[][] walkableField = new boolean[1024][1024];
        for (int y = 0; y < walkableField.length; y++) {
            for (int x = 0; x < walkableField[0].length; x++) {
                walkableField[x][y] = networkClient.isWalkable(x, y);
            }
        }

        return walkableField;
    }

}
