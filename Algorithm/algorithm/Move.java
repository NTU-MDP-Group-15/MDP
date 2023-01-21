package algorithm;

import java.lang.Math;
import java.awt.Point;
import arena.*;

public class Move implements Comparable<Move> {

	private Move parentMove;
	private Point pos;
	private ArenaDirections robotDir;
	private double g;
	private double f;

	// Constructors
	/**
	 * Constructor for ArenaMove
	 * 
	 * @param Pos Point to move to
	 */
	public Move(Point pos, ArenaDirections robotDir) {
		this.parentMove = null;
		this.pos = pos;
		this.robotDir = robotDir;
		this.g = 0;
	}

	/**
	 * Constructor for ArenaMove
	 * 
	 * @param pos        Point to move to
	 * @param robotDir   Direction of robot after taking this move
	 * @param parentMove Previous move
	 */
	public Move(Point pos, ArenaDirections robotDir, Move parentMove) {
		this.parentMove = parentMove;
		this.pos = pos;
		this.robotDir = robotDir;
		if (parentMove.getRobotDir() == robotDir) {
			this.g = parentMove.getG() + 1;
		} else {
			this.g = parentMove.getG() + 4;
		}
	}

	// Getters and setters
	public Move getParent() {
		return this.parentMove;
	}

	public void setParent(Move parentMove) {
		this.parentMove = parentMove;
	}

	public Point getPos() {
		return this.pos;
	}

	public void setPos(Point pos) {
		this.pos = pos;
	}

	public ArenaDirections getRobotDir() {
		return this.robotDir;
	}

	public void setRobotDir(ArenaDirections robotDir) {
		this.robotDir = robotDir;
	}

	public double getG() {
		return this.g;
	}

	public void setG(double g) {
		this.g = g;
	}

	public double getF() {
		return this.f;
	}

	public void setF(double f) {
		this.f = f;
	}

	/**
	 * Calculates and sets the f value of the move under A* search
	 * 
	 * @param goal The current end point to be reached
	 */
	public void calculateF(Point goal) {
		double h;
		// h = this.pos.distance(goal); //Euclidean
		h = Math.abs(this.pos.x - goal.x) + Math.abs(this.pos.y - goal.y); // Manhattan
		this.f = this.g + h;
	}

	@Override
	public int compareTo(Move move) {
		if (this.f < move.getF()) {
			return -1;
		} else if (this.f > move.getF()) {
			return 1;
		}
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Move) {
			Move move = (Move) o;
			if (pos.equals(move.getPos()) && robotDir.equals(move.getRobotDir())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + (pos == null ? 0 : pos.hashCode());
		hash = 31 * hash + (robotDir == null ? 0 : robotDir.hashCode());
		return hash;
	}
}
