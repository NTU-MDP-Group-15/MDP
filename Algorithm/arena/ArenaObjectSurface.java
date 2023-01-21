package arena;

import java.awt.*;

public class ArenaObjectSurface {

    private Point pos;
    private ArenaDirections surface;

    // constructor

    public ArenaObjectSurface(Point pos, ArenaDirections surface) {
        this.pos = pos;
        this.surface = surface;
    }

    // overloading

    public ArenaObjectSurface(int row, int col, ArenaDirections surface) {
        this.pos = new Point(col, row);
        this.surface = surface;
    }

    /**
     * Getters
     **/

    public Point getPos() {
        return pos;
    }

    public ArenaDirections getSurface() {
        return surface;
    }

    public int getRow() {
        return this.pos.y;
    }

    public int getCol() {
        return this.pos.x;
    }

    /**
     * Setters
     **/

    public void setPos(Point pos) {
        this.pos = pos;
    }

    public void setPos(int col, int row) {
        this.pos = new Point(col, row);
    }

    public void setSurface(ArenaDirections surface) {
        this.surface = surface;
    }

    @Override
    public String toString() {
        return String.format("%d|%d|%s", this.pos.y, this.pos.x, this.surface.toString());   // row|col|surface
    }
}