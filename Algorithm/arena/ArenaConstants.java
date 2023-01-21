package arena;

import javafx.scene.paint.Color;

public class ArenaConstants {
    public static final short GRID_SIZE = 10;
    public static final short ARENA_LENGTH = 20;
    public static final short ARENA_WIDTH = 20;
    public static final short STARTZONE_ROW = 1;
    public static final short STARTZONE_COL = 1;

    public static final int ARENA_CELL_SIZE = 25;
    public static final int ARENA_OFFSET = 25;

    // Simulator Constants
    public static final int ARENA_GRID_WIDTH = ARENA_CELL_SIZE * ARENA_WIDTH + 1 + ARENA_OFFSET;
    public static final int ARENA_GRID_HEIGHT = ARENA_CELL_SIZE * ARENA_LENGTH + 1 + ARENA_OFFSET;

    // Movement Constants
    // TODO: finalise robot movement
    public static final boolean SAFE = false;
    public static final int FORWARD_DIST = 1;
    public static final int RIGHTSIDE_DIST = 2;
    public static final int LEFTSIDE_DIST = 1;

    // Graphic Constants
    public static final javafx.scene.paint.Color SZ_COLOR = javafx.scene.paint.Color.GOLD; // Start Zone Color
    public static final javafx.scene.paint.Color OB_COLOR = javafx.scene.paint.Color.BLACK; // Obstacle Color
    public static final javafx.scene.paint.Color CW_COLOR = javafx.scene.paint.Color.LIGHTGRAY; // Cell Border Color
    public static final javafx.scene.paint.Color PH_COLOR = Color.PINK; // Path Color
    public static final javafx.scene.paint.Color UE_COLOR = Color.WHITE; // Unexplored Color
    public static final javafx.scene.paint.Color IP_COLOR = Color.YELLOW; // Image Position Color
}