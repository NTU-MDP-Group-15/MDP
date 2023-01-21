package arena;

import java.awt.Point;
import java.util.ArrayList;

public class Arena {

    private final ArenaGrid[][] grid;

    public Arena() {
        grid = new ArenaGrid[ArenaConstants.ARENA_LENGTH][ArenaConstants.ARENA_WIDTH];
        initArena();
    }

    private void initArena() {
        // Initialise grids in the arena
        for (int row = 0; row < ArenaConstants.ARENA_LENGTH; row++) {
            for (int col = 0; col < ArenaConstants.ARENA_WIDTH; col++) {
                grid[row][col] = new ArenaGrid(new Point(col, row));

                // TODO: safe/risk mode
                // Initialise virtual obstacles
                if (ArenaConstants.SAFE) {
                	if (row == 0 || col == 0 || row == ArenaConstants.ARENA_LENGTH-1 || col == ArenaConstants.ARENA_WIDTH-1) {
                        grid[row][col].setVirtualObstacle(true);
                    }
                }
            }
        }
    }

    public void resetArena() {
        initArena();
    }

    /**
     * Get Grid using row and col
     * @param row
     * @param col
     * @return
     */
    public ArenaGrid getGrid(int row, int col) {
    	if(!checkValidGrid(row, col)) {
    		return null;
    	}
    	return grid[row][col];
    }

    /**
     * Get Grid using Point(x, y)
     * @param pos
     * @return
     */
    public ArenaGrid getGrid(Point pos) {
    	if(!checkValidGrid(pos.y, pos.x)) {
    		return null;
    	}
        return grid[pos.y][pos.x];
    }

    /**
     * Check if the row and col is within the Map
     * @param row
     * @param col
     * @return
     */
    public boolean checkValidGrid(int row, int col) {
        return row >= 0 && col >= 0 && row < ArenaConstants.ARENA_LENGTH && col < ArenaConstants.ARENA_WIDTH;
    }

    /**
     * Checks whether robot can exist on the ArenaGrid, i.e. can it pass through without taking into account direction of move
     * @param grid ArenaGrid to be checked
     * @return boolean for passing through the ArenaGrid
     */
    public boolean canPassThrough(ArenaGrid grid) {
    	if(grid == null) {
    		return false;
    	}
        int row = grid.getPos().y;
        int col = grid.getPos().x;

        return grid.movableGrid() && checkValidGrid(row, col);
    }

    /**
     * Get Grid object of all neighbours that are valid to move to given current position and direction
     * <p>
     * Considers forwards, backwards, right-angle left turn and right turn
     * @param g Grid of current position
     * @param facing Direction robot is facing
     * @return neighbours ArrayList<Grid>
     */
    public ArrayList<ArenaGrid> getNeighbours(ArenaGrid g, ArenaDirections facing) {

        ArrayList<ArenaGrid> neighbours = new ArrayList<ArenaGrid>();
        Point forward;
        Point backward;
        Point leftTurn;
        Point rightTurn;
        double angle;
        
        switch (facing){
        	case UP:
        		angle = Math.PI/2;
        		break;
        	case DOWN:
        		angle = 3*Math.PI/2;
        		break;
        	case LEFT:
        		angle = Math.PI;
        		break;
        	case RIGHT:
        		angle = 0;
        		break;
        	default:
        		System.out.println("Error in getNeighbours");
        		return null;
        }
        
        // FORWARD
        boolean testLeft = true;
        boolean testRight = true;
        for (int i = 1; i <= 3; i++) {
        	forward = new Point(g.getPos().x+i*(int)Math.cos(angle), g.getPos().y+i*(int)Math.sin(angle));
        	if (!canPassThrough(getGrid(forward)) && !(!ArenaConstants.SAFE && i == 3 && getGrid(forward) == null)) {
        		break;
        	} else if (i == 1) {
        		neighbours.add(getGrid(forward));
        	}
        	// LEFT TURN
        	// Set final position
        	forward = new Point(g.getPos().x+ArenaConstants.FORWARD_DIST*(int)Math.cos(angle)-ArenaConstants.LEFTSIDE_DIST*(int)Math.sin(angle),
        			g.getPos().y+ArenaConstants.FORWARD_DIST*(int)Math.sin(angle)+ArenaConstants.LEFTSIDE_DIST*(int)Math.cos(angle));
        	for (int j = 1; j <= 3; j++) {
        		leftTurn = new Point(g.getPos().x+i*(int)Math.cos(angle)-j*(int)Math.sin(angle),
        	       		g.getPos().y+i*(int)Math.sin(angle)+j*(int)Math.cos(angle));
        		if(leftTurn.x == g.getPos().x+(int)Math.cos(angle)-3*(int)Math.sin(angle) && leftTurn.y == g.getPos().y+(int)Math.sin(angle)+3*(int)Math.cos(angle)) {
        			continue;
        			// TODO: safe/risk mode
        		} else if (!ArenaConstants.SAFE && (i == 3 || j == 3) && getGrid(leftTurn) == null) {
					continue;
				} else if (!canPassThrough(getGrid(leftTurn))) {
					testLeft = false;
					break;
				}
        	}
        	if (i == 3 && testLeft) {
    			neighbours.add(getGrid(forward));
    		}
        	// RIGHT TURN
        	// Set final position
        	forward = new Point(g.getPos().x+ArenaConstants.FORWARD_DIST*(int)Math.cos(angle)+ArenaConstants.RIGHTSIDE_DIST*(int)Math.sin(angle),
        			g.getPos().y+ArenaConstants.FORWARD_DIST*(int)Math.sin(angle)-ArenaConstants.RIGHTSIDE_DIST*(int)Math.cos(angle));
        	for (int j = 1; j <= 3; j++) {
        		rightTurn = new Point(g.getPos().x+i*(int)Math.cos(angle)+j*(int)Math.sin(angle),
        				g.getPos().y+i*(int)Math.sin(angle)-j*(int)Math.cos(angle));
        		if(rightTurn.x == g.getPos().x+(int)Math.cos(angle)+3*(int)Math.sin(angle) && rightTurn.y == g.getPos().y+(int)Math.sin(angle)-3*(int)Math.cos(angle)) {
        			continue;
        			// TODO: safe/risk mode
        		} else if (!ArenaConstants.SAFE && (i == 3 || j == 3) && getGrid(rightTurn) == null) {
					continue;
				} else if (!canPassThrough(getGrid(rightTurn))) {
					testRight = false;
					break;
				}
        	}
        	if (i == 3 && testRight) {
    			neighbours.add(getGrid(forward));
    		}
        }

        // BACKWARD
        backward = new Point(g.getPos().x-(int)Math.cos(angle), g.getPos().y-(int)Math.sin(angle));
        if (canPassThrough(getGrid(backward))) {
        	neighbours.add(getGrid(backward));
        }
        return neighbours;
    }
    
    /**
     * Gets closest passable(as per Arena.canPassThrough) Point adjacent to an obstacle image
     * @param pos position of the obstacle
     * @param surfDir Surface direction
     * @return Point adjacent to the surfDir of the pos provided
     */
    public Point getAdjacentPoint(Point pos, ArenaDirections surfDir) {

        Point n = null;

        switch (surfDir) {
            case UP:
                n = new Point(pos.x , pos.y + 3);
                break;
            case LEFT:
                n = new Point(pos.x - 3, pos.y);
                break;
            case DOWN:
                n = new Point(pos.x, pos.y - 3);
                break;
            case RIGHT:
                n = new Point(pos.x + 3, pos.y);
                break;
        }
        if(canPassThrough(getGrid(n))) {
        	return n;
        }
        return null;
    }
    
    /**
     * Scan arena for all obstacle images
     * @return obstacleImages Hashmap of all obstacle images to be captured by robot
     */
    public ArrayList<ArenaObjectSurface> getObstacleImages(){
    	ArrayList<ArenaObjectSurface> obstacleImages = new ArrayList<ArenaObjectSurface>();
    	ArenaObjectSurface tempSurface;
    	for(int r = 0; r < ArenaConstants.ARENA_LENGTH; r++) {
    		for(int c = 0; c < ArenaConstants.ARENA_WIDTH; c++) {
    			if(grid[r][c].isObstacles()) {
    				tempSurface = new ArenaObjectSurface(grid[r][c].getPos(), grid[r][c].getImagePosition());
    				obstacleImages.add(tempSurface);
    			}
    		}
    	}
    	return obstacleImages;
    }
    

    public ArrayList<ArenaObjectSurface> getOtherSurfaces(ArenaObjectSurface image){
    	ArrayList<ArenaObjectSurface> otherSurfaces = new ArrayList<ArenaObjectSurface>();
    	otherSurfaces.add(new ArenaObjectSurface(image.getPos(), ArenaDirections.getAntiClockwise(image.getSurface())));
    	otherSurfaces.add(new ArenaObjectSurface(image.getPos(), ArenaDirections.getOpposite(image.getSurface())));
    	otherSurfaces.add(new ArenaObjectSurface(image.getPos(), ArenaDirections.getClockwise(image.getSurface())));
    	return otherSurfaces;
    }

    /**
     * Finds the closest obstacle image from the robot's current position by Euclidean distance
     * @param loc Reference position for distance
     * @param notYetTaken Obstacle images yet to be taken
     * @return
     */
    public ArenaObjectSurface nearestObsSurface(Point loc, ArrayList<ArenaObjectSurface> notYetTaken) {
        double dist = 1000, tempDist;
        Point tempPos;
        ArenaObjectSurface nearest = null;
        ArenaObjectSurface obstacle = null;

        int i = 0;
        while (i<notYetTaken.size()) {
        	obstacle = notYetTaken.get(i);
        	// get neighbour grid of that surface
            tempPos = getAdjacentPoint(obstacle.getPos(), obstacle.getSurface());
            if(tempPos == null) {
            	System.out.println("Unreachable goal found, removing obstacle from list of targets");
            	notYetTaken.remove(obstacle);
            }
            else {
            	tempDist = loc.distance(tempPos);
                if (tempDist < dist) {
                	dist = tempDist;
                    nearest = obstacle;
                }
                i++;
            }
        }
        return nearest;
    }

    /**
     * Remove existing grid with path
     */
    public void removeAllPaths() {
        for (int r = 0; r < ArenaConstants.ARENA_LENGTH; r++) {
            for (int c = 0; c < ArenaConstants.ARENA_WIDTH; c++) {
                grid[r][c].setPath(false);
            }
        }
    }

    /**
     * Get the moving direction from point A to point B. (provided A or B has same x or y)
     * Assuming A and B are not the same point.
     * @param A
     * @param B
     * @return
     */
    public ArenaDirections getGridDir(Point A, Point B) {
        if (A.y - B.y > 0) {
            return ArenaDirections.DOWN;
        }
        else if (A.y - B.y < 0) {
            return ArenaDirections.UP;
        }
        else if (A.x - B.x > 0) {
            return ArenaDirections.LEFT;
        }
        else {
            return ArenaDirections.RIGHT;
        }
    }
    
    /**
     * Place obstacle at location provided
     * @param newObsPos ArenaDirections of the image on the obstacle
     * @param row Y-coordinate of ArenaGrid the obstacle is located in
     * @param col X-coordinate of ArenaGrid the obstacle is located in
     * @return boolean for whether obstacle was successfully added
     */
    // Place Obstacle at Location
    public boolean addObstacle(ArenaDirections newObsPos, int row, int col) {
        // Check to make sure the cell is valid and is not an existing obstacle
        if (checkValidGrid(row, col) && !getGrid(row, col).isObstacles()) {
            getGrid(row, col).setObstacles(true);
            getGrid(row, col).setImagePosition(newObsPos);

            // Set the virtual wall around the obstacle
            updateVirtualObstacles(getGrid(row, col), true);
            return true;
        }
        return false;
    }

    /**
     * Remove obstacle at location provided
     * @param row Y-coordinate of ArenaGrid the obstacle is located in
     * @param col X-coordinate of ArenaGrid the obstacle is located in
     * @return boolean for whether obstacle was successfully added
     */
    public boolean removeObstacle(int row, int col) {
        // Check to make sure the cell is valid and is an existing obstacle
        if (checkValidGrid(row, col) && getGrid(row, col).isObstacles()) {
            getGrid(row, col).setObstacles(false);
            getGrid(row, col).setImagePosition(null);

            // Set the virtual wall around the obstacle
            updateVirtualObstacles(getGrid(row, col), false);
            reinitVirtualWall();
            return true;
        }
        return false;
    }
    
    /**
     * Create or remove virtual obstacles around added or removed obstacles
     * @param obstacle ArenaGrid object of new obstacles
     * @param isVirtualObstacle boolean of whether obstacle was added or removed
     */
    private void updateVirtualObstacles(ArenaGrid obstacle, boolean isVirtualObstacle) {
        for (int row = obstacle.getPos().y - 1; row <= obstacle.getPos().y + 1; row++) {
            for (int col = obstacle.getPos().x - 1; col <= obstacle.getPos().x + 1; col++) {
                if(checkValidGrid(row, col)) {
                    grid[row][col].setVirtualObstacle(isVirtualObstacle);
                }
            }
        }
    }

    /**
     * Reinitialise virtual walls and obstacles in the Arena
     */
    public void reinitVirtualWall() {
        for (int row = 0; row < ArenaConstants.ARENA_LENGTH; row++) {
            for (int col = 0; col < ArenaConstants.ARENA_WIDTH; col++) {
                // Init Virtual wall
            	// TODO: safe/risk mode
            	if (ArenaConstants.SAFE) {
            		if (row == 0 || col == 0 || row == ArenaConstants.ARENA_LENGTH - 1 || col == ArenaConstants.ARENA_WIDTH - 1) {
            			grid[row][col].setVirtualObstacle(true);
                    }
            	}
                if (grid[row][col].isObstacles()) {
                    updateVirtualObstacles(grid[row][col], true);
                }
            }
        }
    }
}