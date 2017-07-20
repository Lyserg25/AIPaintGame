import java.awt.*;

/**
 * Created by Maximilian on 20.07.2017.
 */
public class QuadTreeNode {
    QuadTreeNode children[];
    private int x, y;
    private int width, height;
    private Color averageColor;
    private int red;
    private int green;
    private int blue;
    private boolean isSameColor;

    QuadTreeNode(int[][] field, int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.red = 0;
        this.green = 0;
        this.blue = 0;

        calcAverageColor(field);

        if (!isSameColor) {
            children = new QuadTreeNode[4];
            children[0] = new QuadTreeNode(field, x, y, width / 2, height / 2);
            children[1] = new QuadTreeNode(field, x + width / 2, y, width / 2, height / 2);
            children[2] = new QuadTreeNode(field, x, y + height / 2, width / 2, height / 2);
            children[3] = new QuadTreeNode(field, x + width / 2, y + height / 2,width / 2, height / 2);
        }
    }

    private void calcAverageColor(int[][] field) {
        isSameColor = true;
        int color = field[0][0];
        for (int y = this.y; y < this.y + height; y++) {
            for (int x = this.x; x < this.x + width; x++) {
                int rgb = field[x][y];
                if (isSameColor == true && rgb != color) {
                    isSameColor = false;
                }
                red += (rgb >> 16) & 255;
                green += (rgb >> 8) & 255;
                blue += rgb & 255;
            }
        }
        if (isSameColor) {
            this.averageColor = new Color(color);
        } else {
            int size = width * height;
            this.averageColor = new Color(red / size, green / size, blue / size);
        }
    }

    boolean isLeaf() {
        return children == null;
    }

    public Color getAverageColor() {
        return averageColor;
    }

    public boolean isSameColor() {
        return isSameColor;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }
}
