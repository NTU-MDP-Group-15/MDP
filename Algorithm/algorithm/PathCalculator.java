package algorithm;

import java.awt.Point;
import java.util.ArrayList;
import java.util.PriorityQueue;

import arena.Arena;
import arena.ArenaDirections;
import arena.ArenaGrid;
import arena.ArenaObjectSurface;
import robot.Robot;

public class PathCalculator {
	private Arena arena;
	private Move goalMove; // Move adjacent to target image
	private Robot robot;

	private int timeLimit = 10 * 1000; // 10s time limit to find path
	private long startTime;
	private long stopTime;

	private PriorityQueue<Move> openList = new PriorityQueue<Move>();
	private ArrayList<Move> closedList = new ArrayList<Move>();

	// private static final NetworkManager netMgr = NetworkManager.getInstance();

	public PathCalculator(Arena arena, Robot robot) {
		this.arena = arena;
		this.robot = robot;
	}

	/**
	 * Set the current target for robot to path towards
	 * 
	 * @param currentGoal the obstacle image being targeted
	 * @return whether goal is valid
	 */
	public boolean setGoal(ArenaObjectSurface currentGoal) {
		if (arena.getAdjacentPoint(currentGoal.getPos(), currentGoal.getSurface()) == null) {
			return false;
		}
		this.goalMove = new Move(arena.getAdjacentPoint(currentGoal.getPos(), currentGoal.getSurface()),
				ArenaDirections.getOpposite(currentGoal.getSurface()));
		initPQ(robot.getCurLocation(), robot.getDir());
		return true;
	}

	/***
	 * Initialise the open list, a priority queue of all the moves available
	 */
	private void initPQ(Point pos, ArenaDirections robotDir) {
		openList.clear();
		closedList.clear();
		Move temp = new Move(pos, robotDir);
		temp.calculateF(goalMove.getPos());
		openList.add(temp);
	}

	/**
	 * Obtains the route from current position of robot to the end goal if it
	 * exists, or null otherwise
	 * 
	 * @return the sequence of moves including start pos of robot
	 */
	public ArrayList<Move> getRoute() {
		ArrayList<Move> path = new ArrayList<Move>();
		Move temp, tempParent;

		if (closedList.indexOf(goalMove) == -1) {
			return null;
		}
		temp = closedList.get(closedList.indexOf(goalMove));
		do {
			tempParent = temp.getParent();
			path.add(0, temp);
			temp = tempParent;
		} while (temp != null);
		return path;
	}

	/**
	 * A* search, populating closedList with the sequence of moves reaching the goal
	 * (amongst other unnecessary moves)
	 * 
	 * @return success of search
	 */
	public boolean calcRoute() {
		startTime = System.currentTimeMillis();
		stopTime = startTime + timeLimit;
		Move currentMove, childMove, dupMove = null;
		ArenaGrid currentGrid, childGrid;
		ArrayList<ArenaGrid> neighbours;

		do {
			currentMove = openList.poll();
			currentGrid = arena.getGrid(currentMove.getPos());
			// Generate successor nodes
			neighbours = arena.getNeighbours(currentGrid, currentMove.getRobotDir());
			// Iterate over successors
			for (int i = 0; i < neighbours.size(); i++) {
				childGrid = neighbours.get(i);
				childMove = new Move(childGrid.getPos(), directionChange(currentMove, childGrid.getPos()), currentMove);
				childMove.calculateF(goalMove.getPos());

				// Check for end condition
				if (childMove.equals(goalMove)) {
					closedList.add(currentMove);
					closedList.add(childMove);
					return true;
				}

				if (openList.contains(childMove)) {
					// Get ArenaMove if it already exists in queue
					Object[] openArray = openList.toArray();
					for (int j = 0; j < openArray.length; j++) {
						if (childMove.equals(openArray[j])) {
							dupMove = (Move) openArray[j];
							break;
						}
					}
					// Skip successor if it does not have better value
					if (childMove.getF() >= dupMove.getF()) {
						continue;
					}
				}
				if (closedList.contains(childMove)) {
					// Get ArenaMove if it already exists in queue
					// Move[] openArray = (Move[]) closedList.toArray();
					dupMove = closedList.get(closedList.indexOf(childMove));
					// Skip successor if it does not have better value
					if (childMove.getF() >= dupMove.getF()) {
						continue;
					} else {
						openList.add(childMove);
					}
				} else {
					openList.add(childMove);
				}
			}
			closedList.add(currentMove);
		} while (!openList.isEmpty() && System.currentTimeMillis() < stopTime);
		return false;
	}

	/**
	 * Finds the changed direction of the robot after a move
	 * 
	 * @param pos    Position and direction of the robot before the move
	 * @param endPos Position of the robot after the move
	 * @return the end direction of the robot after the move
	 */
	public ArenaDirections directionChange(Move pos, Point endPos) {
		switch (pos.getRobotDir()) {
			case UP:
				if (endPos.y > pos.getPos().y) {
					if (endPos.x > pos.getPos().x) {
						return ArenaDirections.RIGHT;
					} else if (endPos.x < pos.getPos().x) {
						return ArenaDirections.LEFT;
					}
				}
				return ArenaDirections.UP;
			case DOWN:
				if (endPos.y < pos.getPos().y) {
					if (endPos.x > pos.getPos().x) {
						return ArenaDirections.RIGHT;
					} else if (endPos.x < pos.getPos().x) {
						return ArenaDirections.LEFT;
					}
				}
				return ArenaDirections.DOWN;
			case LEFT:
				if (endPos.x < pos.getPos().x) {
					if (endPos.y > pos.getPos().y) {
						return ArenaDirections.UP;
					} else if (endPos.y < pos.getPos().y) {
						return ArenaDirections.DOWN;
					}
				}
				return ArenaDirections.LEFT;
			case RIGHT:
				if (endPos.x > pos.getPos().x) {
					if (endPos.y > pos.getPos().y) {
						return ArenaDirections.UP;
					} else if (endPos.y < pos.getPos().y) {
						return ArenaDirections.DOWN;
					}
				}
				return ArenaDirections.RIGHT;
			default:
				System.out.println("Direction error in PathCalculator.directionChange()");
				return null;
		}
	}

	/**
	 * To display or hide the fastest path found on the simulator
	 * 
	 * @param path    Sequence of moves to take
	 * @param display Display or hide the path
	 */
	public void displayFastestPath(ArrayList<Move> path, boolean display) {
		Move temp;
		if (display) {
			System.out.println("Path:");
		}
		for (int i = 0; i < path.size(); i++) {
			temp = path.get(i);
			// Set the path cells to display as path on the Sim
			arena.getGrid(temp.getPos()).setPath(display);
			// System.out.println(arena.getGrid(temp.getPos()).toString());

			// Output Path on console
			if (display) {
				if (i != (path.size() - 1))
					System.out.print("(" + temp.getPos().x + ", " + temp.getPos().y + ") --> ");
				else
					System.out.println("(" + temp.getPos().x + ", " + temp.getPos().y + ")\n");
			}
		}
	}

	/**
	 * Reset all values of the calculator
	 * 
	 * @param arena
	 * @param robot
	 */
	public void resetPathCalculator(Arena arena, Robot robot) {
		this.arena = arena;
		this.goalMove = null;
		this.robot = robot;
		openList.clear();
		closedList.clear();
	}
}