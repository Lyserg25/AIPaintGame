import java.awt.*;

/**
 * Created by Maximilian on 11.07.2017.
 */
public class Vertex {
    public int x;
    public int y;
    private Color averageColor;
    private int red;
    private int green;
    private int blue;
    private double score;

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public Vertex(int x, int y) {
        this.x = x;
        this.y = y;
        this.score = 0.0;
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

    public void setAverageColor(Color averageColor) {
        this.averageColor = averageColor;
    }

    public Color getAverageColor() {
        return averageColor;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }


}
