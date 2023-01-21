package arena;

import java.io.*;
import java.util.logging.*;

public class ArenaDescriptor {

    private static final Logger LOGGER = Logger.getLogger(ArenaDescriptor.class.getName());

    private String[] mapString = new String[ArenaConstants.ARENA_LENGTH];
    private String filename;

    public ArenaDescriptor() {
        filename = "";
    }

    /**
     * Construct Arena descriptor with given real Map text file
     */
    public ArenaDescriptor(String filename) throws IOException {
        setMapStr(filename);
    }
    
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setMapStr(String filename) throws IOException {
        this.filename = filename;

        FileReader file = new FileReader(filename);
        BufferedReader buf = new BufferedReader(file);
        
        for (int r = 0; r < ArenaConstants.ARENA_LENGTH; r++) {
        	mapString[r] = buf.readLine();
        }
        
        System.out.println("mapString:");
        for (int r = 0; r < ArenaConstants.ARENA_LENGTH; r++) {
        	System.out.println(mapString[r]);
        }

        buf.close();
    }
    
    public String[] generateMDFString(Arena arena) {
    	String[] MDFcreator = new String[ArenaConstants.ARENA_LENGTH];
    	for (int r = 0; r < ArenaConstants.ARENA_LENGTH; r++) {
    		MDFcreator[r] = "";
            for (int c = 0; c < ArenaConstants.ARENA_WIDTH; c++) {
            	ArenaGrid grid = arena.getGrid(r, c);
            	if (grid.isObstacles()) {
                    switch(grid.getImagePosition()) {
                    case UP:
                    	MDFcreator[r] = MDFcreator[r] + 'U';
                    	break;
                    case DOWN:
                    	MDFcreator[r] = MDFcreator[r] + 'D';
                    	break;
                    case LEFT:
                    	MDFcreator[r] = MDFcreator[r] + 'L';
                    	break;
                    case RIGHT:
                    	MDFcreator[r] = MDFcreator[r] + 'R';
                    	break;
                    default:
                    	MDFcreator[r] = MDFcreator[r] + '0';
                    	break;
                    }
            	} else {
            		MDFcreator[r] = MDFcreator[r] + '0';
            	}
            }
        }
    	return MDFcreator;
    }
    
    public void loadMDFString(Arena arena) {
    	for (int r = 0; r < ArenaConstants.ARENA_LENGTH; r++) {
            for (int c = 0; c < ArenaConstants.ARENA_WIDTH; c++) {
            	if (mapString[r].charAt(c) != '0') {
                    switch(mapString[r].charAt(c)) {
                    case 'U':
                    	arena.addObstacle(ArenaDirections.UP, r, c);
                    	break;
                    case 'D':
                    	arena.addObstacle(ArenaDirections.DOWN, r, c);
                    	break;
                    case 'L':
                    	arena.addObstacle(ArenaDirections.LEFT, r, c);
                    	break;
                    case 'R':
                    	arena.addObstacle(ArenaDirections.RIGHT, r, c);
                    	break;
                    default:
                    	break;
                    }
            	}
            }
        }
    }


    /**
     * Load real Map for simulator
     * @param map initialized empty
     */
    public void loadRealArena(Arena arena) {
        if(filename == "") {
            LOGGER.warning("No MDF found! Map not loaded!\n");
        }
        else {
            loadMDFString(arena);
        }
    }

    public void loadRealArena(Arena arena, String filename) {
    	this.filename = filename;
        try {
            setMapStr(filename);
        } catch (IOException e) {
            LOGGER.warning("IOException");
            e.printStackTrace();
        }
        loadMDFString(arena);
    }

    public void saveRealArena(Arena arena, String filename) {
    	try {
            FileWriter file = new FileWriter(filename);

            BufferedWriter buf = new BufferedWriter(file);
            String[] mapDes = generateMDFString(arena);
            for (int r = 0; r < ArenaConstants.ARENA_LENGTH; r++) {
            	buf.write(mapDes[r]);
            	buf.newLine();
            }
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}