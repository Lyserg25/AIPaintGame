import lenz.htw.kipifub.net.NetworkClient;

import java.util.concurrent.Callable;


/**
 * Created by Maximilian on 04.07.2017.
 */
public class MyClientNoMove implements Callable<Void> {

    private String hostName;
    private String teamName;
    protected int myPlayerNr;

    public MyClientNoMove(String hostname, String teamName) {
        this.hostName = hostname;
        this.teamName = teamName;
    }

    @Override
    public Void call() {
        NetworkClient networkClient = new NetworkClient(hostName, teamName);
        myPlayerNr = networkClient.getMyPlayerNumber();
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
//        ColorChange colorChange;
//        while ((colorChange = networkClient.pullNextColorChange()) != null) {
//            //verarbeiten von colorChange
//            colorChange.player, colorChange.bot, colorChange.x, colorChange.y
//        }
        return null;
    }

}
