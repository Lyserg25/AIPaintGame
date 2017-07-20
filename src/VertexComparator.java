import java.util.Comparator;

/**
 * Created by Maximilian on 20.07.2017.
 */

public class VertexComparator implements Comparator<Vertex> {

    @Override
    public int compare(Vertex v1, Vertex v2) {
        return Double.compare(v1.getScore(), v2.getScore());
    }

}
