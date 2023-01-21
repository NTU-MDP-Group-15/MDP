package arena;

import java.awt.*;

public class ArenaGrid {

    // Position Variables
    private Point _pos;

    //Pathing variables
    private boolean obstacles = false;
    private boolean virtualObstacle = false;
    private boolean path = false;
    private String sensorName;
    
    // Image position variables
    private ArenaDirections imagePos = null;

    // constructor
    public ArenaGrid(Point pos) {
        this._pos = pos;
        this.sensorName = null;
    }

    //getters and setters
    public Point getPos() {
        return _pos;
    }

    public void setPos(Point pos) {
        this._pos = pos;
    }

    public boolean isObstacles() {
        return obstacles;
    }
    
    public void setObstacles(boolean obstacles) {
        this.obstacles = obstacles;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public boolean getVirtualObstacle() {
        return virtualObstacle;
    }

    public void setVirtualObstacle(boolean virtualObstacle) {
        this.virtualObstacle = virtualObstacle;
    }

    public boolean getPath() {
        return path;
    }

    public void setPath(boolean path) {
        this.path = path;
    }
    
    public ArenaDirections getImagePosition() {
    	return this.imagePos;
    }
    
    public void setImagePosition(ArenaDirections imagePosition) {
    	this.imagePos = imagePosition;
    }

    /***
     * Checks whether the grid is an obstacle or a virtual wall
     * @return Boolean for whether grid is clear to move through or not
     */
    public boolean movableGrid() {
        return !obstacles && !virtualObstacle;
    }

    @Override
    public String toString() {
        return "Grid [pos=" + _pos + ", obstacle=" + obstacles + ", virtualObstacle=" + virtualObstacle
                + ", path=" + path + "]";
    }
}