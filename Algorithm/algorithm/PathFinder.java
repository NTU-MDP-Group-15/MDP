package algorithm;

//Map
import arena.*;

//Network
import network.NetworkMain;
//Robot
import robot.Robot;

import java.awt.*;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

public class PathFinder {
	private static final Logger LOGGER = Logger.getLogger(PathFinder.class.getName());
	private boolean simulation;

	private HashSet<String> imageHashSet = new HashSet<String>();

	// Arena
	private Arena currentArena;

	// Robot
	private Robot robot;
	private Point startPos;

	PathCalculator pc;

	// Time
	private int timeLimit;
	private long startTime;
	private long stopTime;

	// Image
	private int totalImage;
	private int imagesTakenCounter = 0; // Count of number of images taken so far

	// network
	private NetworkMain rpi;
	private NetworkMain imageRec;

	// Constructor
	/**
	 * Construct the Pathfinder class
	 * 
	 * @param simulation    Whether run is live or simulated
	 * @param currentArena  current explored environment of the robot (but we have
	 *                      perfect into so it's just actual arena)
	 * @param robot         Robot used in run
	 * @param startPos      Starting position of robot
	 * @param timeLimit     Time given to complete run
	 * @param stepPerSecond Speed of robot in steps per second (I THINK)
	 * @param totalImage    Total number of images in arena
	 * @return Pathfinder object
	 */
	public PathFinder(boolean simulation, Arena currentArena, Robot robot, Point startPos, int timeLimit,
			/* int stepPerSecond, */ int totalImage, NetworkMain rpi) {
		this.simulation = simulation;
		this.currentArena = currentArena;
		this.robot = robot;
		this.startPos = startPos;
		this.timeLimit = timeLimit;
		this.totalImage = totalImage;
		this.rpi = rpi;
		this.imageRec = new NetworkMain("192.168.31.16", 12345, "ImageRec");
		this.pc = new PathCalculator(currentArena, robot);
	}

	// Getters and Setters
	public boolean isSimulation() {
		return simulation;
	}

	public void setSimulation(boolean simulation) {
		this.simulation = simulation;
	}

	public Arena getcurrentArena() {
		return currentArena;
	}

	public void setcurrentArena(Arena currentArena) {
		this.currentArena = currentArena;
	}

	public Robot getRobot() {
		return robot;
	}

	public void setRobot(Robot robot) {
		this.robot = robot;
	}

	public Point getStartPos() {
		return startPos;
	}

	public void setStartPos(Point startPos) {
		this.startPos = startPos;
	}

	public int getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getStopTime() {
		return stopTime;
	}

	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}

	public int getTotalImage() {
		return totalImage;
	}

	public void setTotalImage(int totalImage) {
		this.totalImage = totalImage;
	}

	// Methods
	/**
	 * Starts pathing to all the obstacle images in the arena
	 * 
	 * @throws InterruptedException
	 */
	public void startImageRecognition() throws InterruptedException {
		startTime = System.currentTimeMillis();
		stopTime = startTime + timeLimit;

		ArenaObjectSurface closestImage = null;
		ArrayList<ArenaObjectSurface> notTakenImages = currentArena.getObstacleImages(); // Get UNEXPLORED obstacle
																							// SURFACES
		ArrayList<ArenaObjectSurface> backupImages = null; // Store the other 3 image surfaces of an obstacle
		if (!simulation) {
			try {
				imageRec.connect();
			} catch (UnknownHostException e) {
				LOGGER.warning("Connection Failed: UnknownHostException\n" + e.toString());
				return;
			} catch (IOException e) {
				LOGGER.warning("Connection Failed: IOException\n" + e.toString());
				return;
			} catch (Exception e) {
				LOGGER.warning("Connection Failed!\n" + e.toString());
				e.printStackTrace();
				return;
			}
		}

		while (System.currentTimeMillis() < stopTime + 60000 && !notTakenImages.isEmpty()) {
			// path to ClosestImage
			closestImage = currentArena.nearestObsSurface(robot.getCurLocation(), notTakenImages);
			String messageToRPI = pathToImage(closestImage);

			if (messageToRPI != null) {
				if (!simulation) {
					System.out.println("The message sequence is: " + messageToRPI);
					rpi.sendMessage("stm-" + messageToRPI + '3'); // send message to stm
					String messageFromImage = imageRec.receiveMessage();
					// TODO: Check string from image, write file, wait check image string, etc
					if (messageFromImage.equals("0")) { // failed to take image
						// System.out.println("Image recognition error!");
						// messageFromImage = null;
						// backupImages = currentArena.getOtherSurfaces(closestImage);
						// for(int i=0; i<backupImages.size(); i++) {
						// messageToRPI = pathToImage(backupImages.get(i));
						// System.out.println("The backup sequence is: "+messageToRPI);
						// rpi.sendMessage("stm-"+messageToRPI+'3'); //send message to stm
						// messageFromImage = imageRec.receiveMessage(); //receive new msg string from
						// picture
						// if(!messageFromImage.equals("0")) { //success condition
						// rpi.sendMessage("android-TARGET,"+closestImage.getPos().x+","+closestImage.getPos().y+","+messageFromImage);
						// imageHashSet.add(messageFromImage);
						// imagesTakenCounter++;
						// break;
						// }
						// }
					} else {
						// System.out.println(""+closestImage.getPos().x+","+closestImage.getPos().y+","+messageFromImage);
						rpi.sendMessage("android-TARGET," + closestImage.getPos().x + "," + closestImage.getPos().y
								+ "," + messageFromImage);
						imageHashSet.add(messageFromImage);
						imagesTakenCounter++;
						if (notTakenImages.size() != 1) {
							imageRec.sendMessage("shag");
						}
					}
				} else {
					System.out.println(messageToRPI);
					imagesTakenCounter++;
				}
			}
			// remove ClosestImage
			notTakenImages.remove(closestImage);
		}

		if (!simulation) {
			rpi.sendMessage("android-Completed");
			imageRec.sendMessage("yay");
		}
		System.out.println(imagesTakenCounter + " images found.");
		System.out.println("Image Hash Set:" + imageHashSet);
		if (!simulation) {
			imageRec.disconnect();
		}
		System.out.println("END");
	}

	/**
	 * Paths from the current robot position to the target
	 * 
	 * @param closestImage Current targeted obstacle image to path towards
	 * @throws InterruptedException
	 */
	private String pathToImage(ArenaObjectSurface closestImage) throws InterruptedException {
		ArrayList<Move> currentPath = null;
		if (!pc.setGoal(closestImage)) {
			System.out.println("Current target at " + closestImage.getPos().toString() + " is invalid");
			return null;
		}
		if (pc.calcRoute()) {
			currentPath = pc.getRoute();
			pc.displayFastestPath(currentPath, true);

			Move temp;
			Move prevTemp;
			String cmd = "";
			String message = "";

			for (int i = 1; i < currentPath.size(); i++) {
				prevTemp = currentPath.get(i - 1);
				temp = currentPath.get(i);
				if (!simulation) {
					double angle = 0;
					switch (prevTemp.getRobotDir()) {
						case UP:
							angle = Math.PI / 2;
							break;
						case DOWN:
							angle = 3 * Math.PI / 2;
							break;
						case LEFT:
							angle = Math.PI;
							break;
						case RIGHT:
							angle = 0;
							break;
					}

					if (prevTemp.getPos().x + (int) Math.cos(angle) == temp.getPos().x
							&& prevTemp.getPos().y + (int) Math.sin(angle) == temp.getPos().y) {
						// FORWARD
						cmd = "W";
					} else if (prevTemp.getPos().x + ArenaConstants.FORWARD_DIST * (int) Math.cos(angle)
							- ArenaConstants.LEFTSIDE_DIST * (int) Math.sin(angle) == temp.getPos().x
							&& prevTemp.getPos().y + ArenaConstants.FORWARD_DIST * (int) Math.sin(angle)
									+ ArenaConstants.LEFTSIDE_DIST * (int) Math.cos(angle) == temp.getPos().y) {
						// LEFT TURN
						cmd = "A";
					} else if (prevTemp.getPos().x + ArenaConstants.FORWARD_DIST * (int) Math.cos(angle)
							+ ArenaConstants.RIGHTSIDE_DIST * (int) Math.sin(angle) == temp.getPos().x
							&& prevTemp.getPos().y + ArenaConstants.FORWARD_DIST * (int) Math.sin(angle)
									- ArenaConstants.RIGHTSIDE_DIST * (int) Math.cos(angle) == temp.getPos().y) {
						// RIGHT TURN
						cmd = "D";
					} else {
						// BACKWARD
						cmd = "S";
					}
					if (cmd != "") {
						robot.setStatus(cmd);
						message = message + cmd;
					}
				} else {
					Thread.sleep(500); // Simulated robot movement delay
				}
				robot.setCurLocation(temp.getPos());
				robot.setDir(temp.getRobotDir());
			}
			Thread.sleep(1000);
			// clean up path
			pc.displayFastestPath(currentPath, false);
			return message;
		} else {
			System.out.println("Unable to path to current target at " + closestImage.getPos().toString()
					+ " after trying all options");
			return null;
		}
	}
}
