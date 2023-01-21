package robot;

import arena.*;
import network.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Robot {
    //Options
    private boolean simulation;        // simulation vs actual run

    //Robot
    private Point curLocation;         // the current location of the robot
    private ArenaDirections dir;         // the direction the robot is facing (UP, DOWN, LEFT, RIGHT)
    private String status;

    // MapDescriptor
    private ArenaDescriptor MDF = new ArenaDescriptor();

    /**
     * Getters
     **/

    public boolean getSimulation() {
        return this.simulation;
    }

    public Point getCurLocation() {
        return this.curLocation;
    }

    public String getStatus() {
        return this.status;
    }

    public ArenaDirections getDir() {
        return this.dir;
    }

    /**
     * Setters
     **/
    public void setSimulation(boolean simulation) {
        this.simulation = simulation;
    }
    
    public void setPos(int row, int col) {
        this.curLocation = new Point(col, row);
    }

    public void setCurLocation(Point curLocation) {
        this.curLocation = curLocation;
    }

    public void setDir(ArenaDirections dir) {
        this.dir = dir;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * ROBOT
     */

    /**
     * Constructor to create a Robot Object.
     *
     * @param simulation To check if this is a simulation or an actual run
     * @param fastestPath To check if the robot will be doing exploration or fastest path. //unnecessary??
     * @param col the x location (column) the robot
     * @param row the y location (row) of the robot
     * @param dir the direction the robot is facing
     */
    public Robot(boolean simulation, boolean fastestPath, int row, int col, ArenaDirections dir) {
        this.simulation = simulation;

        this.curLocation = new Point(col, row);
        this.dir = dir;

        this.status = String.format("Initialization completed.\n");
    }

    /**
     * Set the Robot position.
     *
     * @param row the location, Point y, of the robot.
     * @param col the location, Point x, of the robot.
     */
    public void setRobotPosition(int row, int col) {

        curLocation.setLocation(col, row);
    }
    
    /**
     * HELPERS
     */
    @Override
    public String toString() {
        String s = String.format("Robot at (%d, %d) facing %s\n", curLocation.x, curLocation.y, dir.toString());
        return s;
    }
    
    public JSONArray getRobotArray() throws JSONException {

        JSONArray robotArray = new JSONArray();
        JSONObject robotJson = new JSONObject()
                .put("x", curLocation.x + 1)
                .put("y", curLocation.y + 1)
                .put("direction", dir.toString().toLowerCase());
        robotArray.put(robotJson);
        return robotArray;
    }

    public String[] getArenaArray(Arena arena) throws JSONException {
        String[] arenaArray = MDF.generateMDFString(arena);
        return arenaArray;
    }

    public JSONArray getStatusArray() throws JSONException {
        JSONArray statusArray = new JSONArray();
        JSONObject statusJson = new JSONObject()
                .put("status", status.replaceAll("\\n", ""));
        statusArray.put(statusJson);
        return statusArray;
    }

    
}
