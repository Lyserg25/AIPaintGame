import lenz.htw.kipifub.ColorChange;
import lenz.htw.kipifub.net.NetworkClient;

import java.util.Random;
import java.util.concurrent.Callable;


/**
 * Created by Maximilian on 04.07.2017.
 */
/*public class MyClientNoMove implements Callable<Void> {

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
        return null;
    }
}*/

public class MyClientNoMove {

    private String hostName;
    private String teamName;
    protected int myPlayerNr;

    public MyClientNoMove(String hostname, String teamName) {
        NetworkClient networkClient = new NetworkClient(hostname, teamName);
        Random random = new Random();
        for (; ; ) {
            networkClient.setMoveDirection(0, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            networkClient.setMoveDirection(1, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            networkClient.setMoveDirection(2, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
        }
    }

    public static void main(String[] args) {
        NetworkClient networkClient = new NetworkClient(null, "Grillhaehnsche");
        Random random = new Random();
        ColorChange colorChange;
        boolean firstColorChange = false;

        while (!firstColorChange) {
            networkClient.setMoveDirection(0, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            networkClient.setMoveDirection(1, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            networkClient.setMoveDirection(2, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            if (networkClient.pullNextColorChange() != null) firstColorChange = true;
        }


        /*for (; ; ) {
            networkClient.setMoveDirection(0, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            networkClient.setMoveDirection(1, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            networkClient.setMoveDirection(2, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            while ((colorChange = networkClient.pullNextColorChange()) != null) {
                System.out.println("colorchange");

            }
        }*/
    }
}
