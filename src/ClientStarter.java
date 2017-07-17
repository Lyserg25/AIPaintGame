import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Maximilian on 04.07.2017.
 */
public class ClientStarter {
    private static final String HOST_NAME = null;
    private static final String[] TEAM_NAME = {"Lyserg25", "Grillhaehnsche", "MrKitty",};

    /* start server:
     * cd "C:\Users\Wayne\IdeaProjects\AIPaintGame\server"
     * cd "C:\Users\Maximilian\IdeaProjects\AIPaintGame\server"
     * java -jar kipifub.jar
     */
    public static void main(String[] args) {
        new MyClient();
        List<Callable<Void>> clients = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        //clients.add(new MyClient(HOST_NAME, TEAM_NAME[0]));
        for (int i = 1; i < 3; i++) {
            //clients.add(new MyClientNoMove(HOST_NAME, TEAM_NAME[i]));
        }
        try {
            executor.invokeAll(clients);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
