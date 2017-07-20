/**
 * Created by Maximilian on 11.07.2017.
 */
public class Vertex {
    public int x;
    public int y;

    public Vertex(int x, int y) {
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
