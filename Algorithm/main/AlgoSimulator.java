package main;

import helper.DisplayTimer;

import arena.*;
import algorithm.*;

import network.NetworkMain;

import robot.Robot;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class AlgoSimulator extends Application {

    private static final Logger LOGGER = Logger.getLogger(AlgoSimulator.class.getName());

    // Program Variables
    private Arena arena; // Loaded Arena for simulator
    private Arena newArena; // New Arena being generated for simulator
    private ArenaDescriptor arenaDescriptor = new ArenaDescriptor(); // Used to load and save Arenas
    private Point startPos = new Point(1, 1);
    private Robot robot;
    private boolean sim = true;
    private ArenaDirections newObsPos = ArenaDirections.UP; // Image position of new obstacles to be inserted

    private AnimationTimer animateTimer1;
    private AnimationTimer animateTimer2;
    public static DisplayTimer displayTimer = new DisplayTimer();

    private String defaultArenaPath = "defaultArena.txt";
    private int totalImages;
    
    private NetworkMain rpi = new NetworkMain("192.168.31.31", 51043, "RPI");

    private boolean setRobot = false;

    // initial task set to image recognition
    private String taskSelected = SimulatorConstants.IMAGE_REC;

    // GUI Components
    private Canvas arenaGrid; // Display of the arena
    private GraphicsContext gc;
    private Scene dialogScene;
    private Stage dialog;

    private Canvas newArenaGrid; // Display of the arena being generated
    private GraphicsContext newGC;

    // UI components
    private Button loadArenaBtn, newArenaBtn, saveArenaBtn, resetArenaBtn, startBtn, connectBtn,
    		setRobotBtn, cancelBtn, confirmBtn, obUpBtn, obDownBtn, obLeftBtn, obRightBtn;
    private RadioButton imageRB, simRB, realRB, upRB, downRB, leftRB, rightRB, obUpRB, obDownRB,
    		obLeftRB, obRightRB;
    private ToggleGroup mode, startDir, imagePos;
    private TextArea debugOutput;
    private ScrollBar timeLimitSB;
    private TextField startPosTxt, timeLimitTxt, arenaTxt;
    private Label runConfigLbl, simSetLbl, robotSetLbl, startPosLbl, startDirLbl, timeLimitLbl;
    private Label modeChoiceLbl, taskChoiceLbl, arenaChoiceLbl, informationLbl, timerLbl;
    private Label timerTextLbl;
    private FileChooser fileChooser;

    private Thread imageTask;
    private boolean taskStarted = false, taskPaused = false;
    private Thread startedTask = null;

    public void start(Stage primaryStage) {
    	// Initialisation for Map and Robot
    	arena = new Arena();
    	newArena = new Arena();

        // Default Location at the start zone
        robot = new Robot(sim, false, 1, 1, ArenaDirections.RIGHT);
        robot.setRobotPosition(robot.getCurLocation().y, robot.getCurLocation().x);

        // Threads

        // Setting the Title and Values for the Window
        primaryStage.setTitle("MDP Group 31");
        GridPane grid = new GridPane();
        GridPane controlGrid = new GridPane();
        // Grid Settings
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(5, 5, 5, 5));

        controlGrid.setAlignment(Pos.CENTER);
        controlGrid.setHgap(5);
        controlGrid.setVgap(5);

        // Drawing Component
        arenaGrid = new Canvas(ArenaConstants.ARENA_GRID_WIDTH + ArenaConstants.ARENA_OFFSET, ArenaConstants.ARENA_GRID_HEIGHT + ArenaConstants.ARENA_OFFSET);
        gc = arenaGrid.getGraphicsContext2D();
        //expMapDraw = !setObstacle;

        animateTimer1 = new AnimationTimer() {

            @Override
            public void start() {
                super.start();
            }

            @Override
            public void handle(long timestamp) {
                drawArena(gc, arena);
                drawRobot();
                debugOutput.setText(robot.getStatus() + "\n" + robot.toString());
                timerTextLbl.setText(displayTimer.getTimerLbl());
                if (startedTask != null) {
                    if (!startedTask.isAlive()) {
                        startBtn.setVisible(false);
                    }
                }
            }
        };
        animateTimer2 = new AnimationTimer() {

            @Override
            public void start() {
                super.start();
            }

            @Override
            public void handle(long timestamp) {
                drawArena(newGC, newArena);
            }
        };

        animateTimer1.start();

        // Canvas MouseEvent
        arenaGrid.setOnMouseClicked(ArenaClick);
        arenaGrid.setUserData("Main_Arena");

        // Label Initialisation
        runConfigLbl = new Label("Run Configurations");
        runConfigLbl.setAlignment(Pos.CENTER);
        runConfigLbl.setTextFill(Color.WHITE);
        runConfigLbl.setBackground(new Background(new BackgroundFill(Color.DIMGREY, CornerRadii.EMPTY, Insets.EMPTY)));
        runConfigLbl.setMaxWidth(SimulatorConstants.MAX_WIDTH);
        
        robotSetLbl = new Label("Robot Settings");
        robotSetLbl.setAlignment(Pos.CENTER);
        robotSetLbl.setTextFill(Color.WHITE);
        robotSetLbl.setBackground(new Background(new BackgroundFill(Color.DIMGREY, CornerRadii.EMPTY, Insets.EMPTY)));
        robotSetLbl.setMaxWidth(SimulatorConstants.MAX_WIDTH);
        
        simSetLbl = new Label("Simulator Settings");
        simSetLbl.setAlignment(Pos.CENTER);
        simSetLbl.setTextFill(Color.WHITE);
        simSetLbl.setBackground(new Background(new BackgroundFill(Color.DIMGREY, CornerRadii.EMPTY, Insets.EMPTY)));
        simSetLbl.setMaxWidth(SimulatorConstants.MAX_WIDTH);
        
        startPosLbl = new Label("Start Position: ");
        startDirLbl = new Label("Start Direction: ");
        startPosTxt = new TextField();
        startPosTxt.setText(String.format("(%d, %d)", robot.getCurLocation().x, robot.getCurLocation().y));
        startPosTxt.setMaxWidth(SimulatorConstants.MAX_WIDTH);
        startPosTxt.setDisable(true);
        
        timeLimitLbl = new Label("Time Limit: ");
       timeLimitTxt = new TextField();
        modeChoiceLbl = new Label("Mode:");
        taskChoiceLbl = new Label("Task:");
        timeLimitTxt.setDisable(true);
        timeLimitTxt.setMaxWidth(100);

        arenaChoiceLbl = new Label("Arena File: ");
        arenaTxt = new TextField();
        arenaTxt.setText(defaultArenaPath);
        arenaTxt.setDisable(true);
        arenaTxt.setMaxWidth(SimulatorConstants.MAX_WIDTH);

        informationLbl = new Label("Robot Information");
        informationLbl.setAlignment(Pos.CENTER);
        informationLbl.setTextFill(Color.WHITE);
        informationLbl.setBackground(new Background(new BackgroundFill(Color.DIMGREY, CornerRadii.EMPTY, Insets.EMPTY)));
        informationLbl.setMaxWidth(SimulatorConstants.MAX_WIDTH);

        timerLbl = new Label("Timer");
        timerLbl.setAlignment(Pos.CENTER);
        timerLbl.setBackground(new Background(new BackgroundFill(Color.DIMGREY, CornerRadii.EMPTY, Insets.EMPTY)));
        timerLbl.setTextFill(Color.WHITE);
        timerLbl.setMaxWidth(SimulatorConstants.MAX_WIDTH);

        // Buttons Initialisation
        connectBtn = new Button("Connect");
        startBtn = new Button("Start");
        loadArenaBtn = new Button("Load Arena");
        newArenaBtn = new Button("New Arena");
        saveArenaBtn = new Button("Save Arena");
        resetArenaBtn = new Button("Reset Arena");
        setRobotBtn = new Button("Reset Starting Position");
        setRobotBtn.setMaxWidth(SimulatorConstants.MAX_WIDTH);
        cancelBtn = new Button("Cancel");
        cancelBtn.setMaxWidth(SimulatorConstants.MAX_WIDTH);
        confirmBtn = new Button("Confirm");
        confirmBtn.setMaxWidth(SimulatorConstants.MAX_WIDTH);
        
        obUpBtn = new Button("Facing up");
        obUpBtn.setMaxWidth(SimulatorConstants.MAX_WIDTH);
        obDownBtn = new Button("Facing down");
        obDownBtn.setMaxWidth(SimulatorConstants.MAX_WIDTH);
        obLeftBtn = new Button("Facing left");
        obLeftBtn.setMaxWidth(SimulatorConstants.MAX_WIDTH);
        obRightBtn = new Button("Facing right");
        obRightBtn.setMaxWidth(SimulatorConstants.MAX_WIDTH);

        loadArenaBtn.setMaxWidth(SimulatorConstants.MAX_WIDTH);
        saveArenaBtn.setMaxWidth(SimulatorConstants.MAX_WIDTH);
        newArenaBtn.setMaxWidth(SimulatorConstants.MAX_WIDTH);

        // Radio Button Initialisation
        imageRB = new RadioButton(SimulatorConstants.IMAGE_REC);
        simRB = new RadioButton(SimulatorConstants.SIM);
        realRB = new RadioButton(SimulatorConstants.REAL);
        upRB = new RadioButton("UP");
        downRB = new RadioButton("DOWN");
        leftRB = new RadioButton("LEFT");
        rightRB = new RadioButton("RIGHT");
        obUpRB = new RadioButton("UP");
        obDownRB = new RadioButton("DOWN");
        obLeftRB = new RadioButton("LEFT");
        obRightRB = new RadioButton("RIGHT");

        // Toggle Group Initialisation
        mode = new ToggleGroup();
        simRB.setToggleGroup(mode);
        realRB.setToggleGroup(mode);
        simRB.setSelected(true);

        mode.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (simRB.isSelected()) {
            	rpi.disconnect();
                sim = true;
                arena.resetArena();
                arenaDescriptor.loadRealArena(arena, defaultArenaPath);
                arenaTxt.setText("defaultArena.txt");
            } else {
                System.out.println("Actual run selected, connecting to RPI");
                try {
                	rpi.connect();
                } catch (UnknownHostException e) {
    	        	LOGGER.warning("Connection Failed: UnknownHostException\n" + e.toString());
    	        	mode.selectToggle(simRB);
    	        	return;
    	        } catch (IOException e) {
    	        	LOGGER.warning("Connection Failed: IOException\n" + e.toString());
    	        	mode.selectToggle(simRB);
    	        	return;
    	        } catch (Exception e) {
    	        	LOGGER.warning("Connection Failed!\n" + e.toString());
    	        	mode.selectToggle(simRB);
    	            e.printStackTrace();
    	            return;
    	        }
                //TODO: handle obstacle generation
            	newArena.resetArena();
                newArenaGrid = new Canvas(ArenaConstants.ARENA_CELL_SIZE * ArenaConstants.ARENA_WIDTH + 1 + 2*ArenaConstants.ARENA_OFFSET,
                        ArenaConstants.ARENA_CELL_SIZE * ArenaConstants.ARENA_LENGTH + 1 + 2*ArenaConstants.ARENA_OFFSET);
                newGC = newArenaGrid.getGraphicsContext2D();
                String[] obstacle = rpi.receiveMessage().split(",");
                totalImages = Integer.parseInt(obstacle[0]);
                ArenaDirections newDir = ArenaDirections.valueOf("RIGHT");
                switch(obstacle[1]) {
                	case "N":
                		newDir = ArenaDirections.valueOf("UP");
                		break;
                	case "S":
                		newDir = ArenaDirections.valueOf("DOWN");
                		break;
                	case "E":
                		newDir = ArenaDirections.valueOf("RIGHT");
                		break;
                	case "W":
                		newDir = ArenaDirections.valueOf("LEFT");
                		break;
                }
                robot.setDir(newDir);
                for(int i = 0; i < totalImages; i++) {
                	obstacle = rpi.receiveMessage().split(",");
                	ArenaDirections newObsFace;
                	switch(obstacle[2]) {
                		case "N":
                			newObsFace = ArenaDirections.UP;
                			break;
                		case "S":
                			newObsFace = ArenaDirections.DOWN;
                			break;
                		case "W":
                			newObsFace = ArenaDirections.LEFT;
                			break;
                		case "E":
                			newObsFace = ArenaDirections.RIGHT;
                			break;
                		default:
                			newObsFace = ArenaDirections.UP;
                			System.out.println("Default value for obstacle face used!");
                	}
                    System.out.println(newArena.addObstacle(newObsFace, Integer.parseInt(obstacle[1]), Integer.parseInt(obstacle[0]))
                            ? "New Obstacle Added at row: " + obstacle[1] + " col: " + obstacle[0]
                            : "Obstacle at location already exists! Right click to remove.");
                }
                //save and load in the new arena
            	arenaDescriptor.saveRealArena(newArena, defaultArenaPath);
                arena.resetArena();
                arenaDescriptor.loadRealArena(arena, defaultArenaPath);
                animateTimer2.stop();
                animateTimer1.start();
                sim = false;
                
                imageTask = new Thread(new ImageTask());
                startedTask = imageTask;
                taskStarted = true;
                taskPaused = false;
                imageTask.start();
            }
        });
        
        imageRB.setSelected(true);

        //set buttons to not visible
        resetArenaBtn.setVisible(false);

        startDir = new ToggleGroup();
        upRB.setToggleGroup(startDir);
        downRB.setToggleGroup(startDir);
        leftRB.setToggleGroup(startDir);
        rightRB.setToggleGroup(startDir);
        rightRB.setSelected(true);
        startDir.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            RadioButton button = (RadioButton) startDir.getSelectedToggle();
            ArenaDirections newDir = ArenaDirections.valueOf(button.getText());
            robot.setDir(newDir);
        });
        
        imagePos = new ToggleGroup();
        obUpRB.setToggleGroup(imagePos);
        obDownRB.setToggleGroup(imagePos);
        obLeftRB.setToggleGroup(imagePos);
        obRightRB.setToggleGroup(imagePos);
        obUpRB.setSelected(true);
        imagePos.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            RadioButton button = (RadioButton) imagePos.getSelectedToggle();
            switch(button.getText()) {
            	case "UP":
            		newObsPos = ArenaDirections.UP;
                	break;
                case "DOWN":
                	newObsPos = ArenaDirections.DOWN;
                	break;
                case "LEFT":
                	newObsPos = ArenaDirections.LEFT;
                	break;
                case "RIGHT":
                	newObsPos = ArenaDirections.RIGHT;
                	break;
            }
        });

        // TextArea
        debugOutput = new TextArea();
        debugOutput.setMaxHeight(100);

        // File Chooser
        fileChooser = new FileChooser();

        // ScrollBar
        timeLimitSB = new ScrollBar();
        timeLimitSB.setMin(0);
        timeLimitSB.setMax(360);

        connectBtn.setMaxWidth(SimulatorConstants.MAX_WIDTH);
        startBtn.setMaxWidth(SimulatorConstants.MAX_WIDTH);
        loadArenaBtn.setMaxWidth(SimulatorConstants.MAX_WIDTH);
        resetArenaBtn.setMaxWidth(SimulatorConstants.MAX_WIDTH);

        //load default variables
        int timeLimit = 360000;
        timeLimitTxt.setText( timeLimit / 1000 + " s");
        timeLimitSB.setValue((double)(timeLimit/1000));

        // load default map from defaultMapPath
        arenaDescriptor.loadRealArena(arena, defaultArenaPath);     // to display when start the app

        // Button ActionListeners
        resetArenaBtn.setOnMouseClicked(resetMapBtnClick);
        startBtn.setOnMouseClicked(startBtnClick); 

        setRobotBtn.setOnMouseClicked(e -> {
            arena.resetArena();
            arenaDescriptor.loadRealArena(arena, defaultArenaPath);
            setRobot = !setRobot;
            if (!setRobot)
                setRobotBtn.setText("Reset Starting Position");
            else
                setRobotBtn.setText("Confirm Starting Position");
        });

        newArenaBtn.setOnMouseClicked(e -> {
            newArena.resetArena();
            dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(primaryStage);

            newArenaGrid = new Canvas(ArenaConstants.ARENA_CELL_SIZE * ArenaConstants.ARENA_WIDTH + 1 + 2*ArenaConstants.ARENA_OFFSET,
                    ArenaConstants.ARENA_CELL_SIZE * ArenaConstants.ARENA_LENGTH + 1 + 2*ArenaConstants.ARENA_OFFSET);
            newGC = newArenaGrid.getGraphicsContext2D();

            // Grid Settings for new map
            GridPane newGridForMap = new GridPane();
            GridPane buttonGrid = new GridPane();
            GridPane dirGrid = new GridPane();
            buttonGrid.setAlignment(Pos.CENTER);
            buttonGrid.setHgap(5);
            buttonGrid.setVgap(5);
            dirGrid.setAlignment(Pos.CENTER);
            dirGrid.setHgap(5);
            dirGrid.setVgap(5);
            newGridForMap.setAlignment(Pos.CENTER);
            newGridForMap.setHgap(5);
            newGridForMap.setVgap(5);
            newGridForMap.setPadding(new Insets(5, 5, 5, 5));

            VBox vBox = new VBox();
            vBox.setPrefWidth(100);

            cancelBtn.setMinWidth(vBox.getPrefWidth());
            confirmBtn.setMinWidth(vBox.getPrefWidth());

            vBox.getChildren().addAll(cancelBtn, confirmBtn);

            buttonGrid.add(cancelBtn, 3, 1);
            buttonGrid.add(confirmBtn, 4, 1);
            
            dirGrid.add(obUpRB, 1, 0);
            dirGrid.add(obDownRB, 1, 1);
            dirGrid.add(obLeftRB, 1, 2);
            dirGrid.add(obRightRB, 1, 3);
            
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(0);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPercentWidth(10);
            buttonGrid.getColumnConstraints().setAll(col1, col2);

            newGridForMap.add(newArenaGrid, 0, 0);
            newGridForMap.add(buttonGrid, 0, 1);
            newGridForMap.add(dirGrid, 1, 0);
            dialogScene = new Scene(newGridForMap, 700, 600);

            setRobot = false;

            animateTimer1.stop();
            animateTimer2.start();

            // Canvas MouseEvent
            newArenaGrid.setOnMouseClicked(NewArenaClick);
            newArenaGrid.setUserData("New_Map");
            dialog.setScene(dialogScene);
            dialog.show();
        });
       
        cancelBtn.setOnMouseClicked(e -> {
            dialog.close();
            animateTimer2.stop();
            animateTimer1.start();
        });
        
        confirmBtn.setOnMouseClicked(e -> {
            arenaDescriptor.saveRealArena(newArena, defaultArenaPath);
            arena.resetArena();
            arenaDescriptor.loadRealArena(arena, defaultArenaPath);
            dialog.close();
            animateTimer2.stop();
            animateTimer1.start();
        });
       
        loadArenaBtn.setOnMouseClicked(e -> {
            fileChooser.setTitle("Choose file to load Arena from");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                arena.resetArena();
                arenaDescriptor.loadRealArena(arena, file.getAbsolutePath());
                arenaTxt.setText(file.getName());
            }
        });
        
        saveArenaBtn.setOnMouseClicked(e -> {
            fileChooser.setTitle("Choose file to save Map to");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                arenaDescriptor.saveRealArena(arena, file.getAbsolutePath());
            }
        });

        timeLimitSB.valueProperty().addListener(change -> {
            timeLimitTxt.setText("" + (int) timeLimitSB.getValue() + " s");
        });

        // TIMER
        timerTextLbl = new Label();
        timerTextLbl.setTextFill(Color.RED);
        timerTextLbl.setStyle("-fx-font-size: 4em;");
        timerTextLbl.setMaxWidth(SimulatorConstants.MAX_WIDTH);
        timerTextLbl.setTextAlignment(TextAlignment.LEFT);
        timerTextLbl.setAlignment(Pos.CENTER);
        timerTextLbl.setText(displayTimer.getTimerLbl());
        
        // General Settings
        controlGrid.add(runConfigLbl, 0, 0, 5, 1);
        controlGrid.add(modeChoiceLbl, 0, 1);
        controlGrid.add(simRB, 1, 1);
        controlGrid.add(realRB, 2, 1);

        controlGrid.add(taskChoiceLbl, 0, 2);
        controlGrid.add(imageRB, 1, 2, 2, 1);
        
        controlGrid.add(startBtn, 1, 3);
        controlGrid.add(resetArenaBtn, 2, 3);
        
        // Arena Settings
        controlGrid.add(robotSetLbl, 0, 4, 5, 1);

        controlGrid.add(startPosLbl, 0, 5);
        controlGrid.add(startPosTxt, 1, 5, 2, 1);
        controlGrid.add(setRobotBtn, 3, 5, 2, 1);

        controlGrid.add(startDirLbl, 0, 6);
        controlGrid.add(upRB, 1, 6);
        controlGrid.add(downRB, 2, 6);
        controlGrid.add(leftRB, 3, 6);
        controlGrid.add(rightRB, 4, 6);
        
        // Simulator Settings
        controlGrid.add(simSetLbl, 0, 8, 5, 1);

        controlGrid.add(timeLimitLbl, 0, 9, 1, 1);
        controlGrid.add(timeLimitSB, 1, 9, 3, 1);
        controlGrid.add(timeLimitTxt, 4, 9, 1, 1);

        controlGrid.add(arenaChoiceLbl, 0, 10);
        controlGrid.add(arenaTxt, 1, 10);
        controlGrid.add(loadArenaBtn, 2, 10);
        controlGrid.add(newArenaBtn, 3, 10);
        controlGrid.add(saveArenaBtn, 4, 10);

        controlGrid.add(informationLbl, 0, 11, 3, 1);
        controlGrid.add(debugOutput, 0, 12, 3, 1);

        controlGrid.add(timerLbl, 3, 11, 2, 1);
        controlGrid.add(timerTextLbl, 3, 12, 2, 1);
        
        // Button Init

        // Choosing where to place components on the Grid

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(70);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(60);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(20);
        grid.getColumnConstraints().setAll(col1, col2);
        controlGrid.getColumnConstraints().setAll(col3, col3, col3, col3, col3);

        grid.add(arenaGrid, 0, 0);
        grid.add(controlGrid, 1, 0);

        // Font and Text Alignment

        // Dimensions of the Window
        Scene scene = new Scene(grid, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

    } // end of start

    // Draw the Map Graphics Cells
    private void drawArena(GraphicsContext gc, Arena arena) {
        // Basic Initialisation for the Cells
        gc.setStroke(ArenaConstants.CW_COLOR);
        gc.setLineWidth(2);

        // Draw the Cells on the Map Canvas
        for (int row = 0; row < ArenaConstants.ARENA_LENGTH; row++) {
            for (int col = 0; col < ArenaConstants.ARENA_WIDTH; col++) {
                // Select Color of the Cells
                if (row <= ArenaConstants.STARTZONE_ROW + 2 && col <= ArenaConstants.STARTZONE_COL + 2)
                    gc.setFill(ArenaConstants.SZ_COLOR);
                else {
                        if (arena.getGrid(row, col).isObstacles()) {
                            gc.setFill(ArenaConstants.OB_COLOR);
                        } else if (arena.getGrid(row, col).getPath())
                            gc.setFill(ArenaConstants.PH_COLOR);
                        else
                            gc.setFill(ArenaConstants.UE_COLOR);
                    }

                // Draw the Cell on the Arena based on the Position Indicated
                gc.strokeRect(col * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2,
                        (ArenaConstants.ARENA_CELL_SIZE - 1) * ArenaConstants.ARENA_LENGTH - row * ArenaConstants.ARENA_CELL_SIZE
                                + ArenaConstants.ARENA_OFFSET / 2,
                        ArenaConstants.ARENA_CELL_SIZE, ArenaConstants.ARENA_CELL_SIZE);
                gc.fillRect(col * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2,
                        (ArenaConstants.ARENA_CELL_SIZE - 1) * ArenaConstants.ARENA_LENGTH - row * ArenaConstants.ARENA_CELL_SIZE
                                + ArenaConstants.ARENA_OFFSET / 2,
                        ArenaConstants.ARENA_CELL_SIZE, ArenaConstants.ARENA_CELL_SIZE);
                
             // colouring image surface
                if (arena.getGrid(row, col).isObstacles()) {
                    gc.setStroke(ArenaConstants.IP_COLOR);
                    
                    switch(arena.getGrid(row, col).getImagePosition()) {
                    case UP:
                    	gc.strokeLine(col * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 5,
                            (ArenaConstants.ARENA_CELL_SIZE - 1) * ArenaConstants.ARENA_LENGTH - row * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 5, 
                                    col * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 20,
                            (ArenaConstants.ARENA_CELL_SIZE - 1) * ArenaConstants.ARENA_LENGTH - row * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 5);
                    	break;
                    
                    case DOWN:
                    	gc.strokeLine(col * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 5,
                            (ArenaConstants.ARENA_CELL_SIZE - 1) * ArenaConstants.ARENA_LENGTH - row * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 20, 
                                    col * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 20,
                            (ArenaConstants.ARENA_CELL_SIZE - 1) * ArenaConstants.ARENA_LENGTH - row * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 20);
                    	break;
                    
                    case LEFT:
                    	gc.strokeLine(col * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 5,
                            (ArenaConstants.ARENA_CELL_SIZE - 1) * ArenaConstants.ARENA_LENGTH - row * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 5, 
                                    col * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 5,
                            (ArenaConstants.ARENA_CELL_SIZE - 1) * ArenaConstants.ARENA_LENGTH - row * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 20);
                    	break;
                    	
                    case RIGHT:
                    	gc.strokeLine(col * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 20,
                            (ArenaConstants.ARENA_CELL_SIZE - 1) * ArenaConstants.ARENA_LENGTH - row * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 5, 
                                    col * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 20,
                            (ArenaConstants.ARENA_CELL_SIZE - 1) * ArenaConstants.ARENA_LENGTH - row * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 20);
                    	break;
                    
                    default:
                    	break;
                    };
                    gc.setStroke(ArenaConstants.CW_COLOR);
                }
                
                // putting coordinates
                if(row==0) {
                	gc.setFill(ArenaConstants.OB_COLOR);
                	gc.fillText(String.valueOf(col), col * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 + 5,
                          (ArenaConstants.ARENA_CELL_SIZE - 1) * ArenaConstants.ARENA_LENGTH - row * ArenaConstants.ARENA_CELL_SIZE
                          + ArenaConstants.ARENA_OFFSET / 2 + 35);
                }
                if(col==0) {
                	gc.setFill(ArenaConstants.OB_COLOR);
                	gc.fillText(String.valueOf(row), col * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2 - 12,
                            (ArenaConstants.ARENA_CELL_SIZE - 1) * ArenaConstants.ARENA_LENGTH - row * ArenaConstants.ARENA_CELL_SIZE
                            + ArenaConstants.ARENA_OFFSET / 2 + 20);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Mouse Event Handler for clicking and detecting Location
    private EventHandler<MouseEvent> ArenaClick = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {

            double mouseX = event.getX();
            double mouseY = event.getY();

            int selectedCol = (int) ((mouseX - ArenaConstants.ARENA_OFFSET / 2) / ArenaConstants.ARENA_CELL_SIZE);
            int selectedRow = (int) (ArenaConstants.ARENA_LENGTH
                    - (mouseY - ArenaConstants.ARENA_OFFSET / 2) / ArenaConstants.ARENA_CELL_SIZE);
            // Debug Text
//            System.out.println(arena.getGrid(selectedRow, selectedCol).toString() + " validCell:"
//                    + arena.canPassThrough(arena.getGrid(selectedRow, selectedCol)));

            if (setRobot)
                System.out.println(setRobotLocation(selectedRow, selectedCol) ? "Robot Position has changed"
                        : "Unable to put Robot at obstacle or virtual wall!");
        }

    };

    private EventHandler<MouseEvent> NewArenaClick = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {
            // setting the obstacle
            double mouseX = event.getX();
            double mouseY = event.getY();

            int selectedCol = (int) ((mouseX - ArenaConstants.ARENA_OFFSET / 2) / ArenaConstants.ARENA_CELL_SIZE);
            int selectedRow = (int) (ArenaConstants.ARENA_LENGTH
                    - (mouseY - ArenaConstants.ARENA_OFFSET / 2) / ArenaConstants.ARENA_CELL_SIZE);
            // Debug Text
//            System.out.println(newArena.getGrid(selectedRow, selectedCol).toString() + " validCell:"
//                    + newArena.canPassThrough(arena.getGrid(selectedRow, selectedCol)));

            if (event.getButton() == MouseButton.PRIMARY)
                System.out.println(newArena.addObstacle(newObsPos, selectedRow, selectedCol)
                        ? "New Obstacle Added at row: " + selectedRow + " col: " + selectedCol
                        : "Obstacle at location already exists! Right click to remove.");
            else
                System.out.println(newArena.removeObstacle(selectedRow, selectedCol)
                        ? "Obstacle removed at row: " + selectedRow + " col: " + selectedCol
                        : "Obstacle at location does not exist!");
        }
    };

    // Set Robot Location and Rotate
    private boolean setRobotLocation(int row, int col) {
        if (arena.canPassThrough(arena.getGrid(row, col))) {
            startPos.setLocation(col, row);
            startPosTxt.setText(String.format("(%d, %d)", col, row));
            robot.setRobotPosition(row, col);
            System.out.println("Robot moved to new position at row: " + row + " col:" + col);
            return true;
        }
        return false;
    }

    // Event Handler for StartExpButton
    // not for actual
    private EventHandler<MouseEvent> startBtnClick = new EventHandler<MouseEvent>() {
		@Override
        public void handle(MouseEvent event) {

            // a new task
            if (taskStarted == false && taskPaused == false) {
                startBtn.setText("Pause");
                resetArenaBtn.setVisible(true);
                displayTimer.stop();
                displayTimer.initialize();
                switch (taskSelected) {
                
                    case SimulatorConstants.IMAGE_REC:
                        // start thread
                        imageTask = new Thread(new ImageTask());
                        startedTask = imageTask;
                        taskStarted = true;
                        taskPaused = false;
                        imageTask.start();
                        displayTimer.start();
                        break;

                }
            } // end of if started a new task

            // pause a task
            else if (taskStarted == true && taskPaused == false) {
                startBtn.setText("Resume");
                startedTask.suspend();
                displayTimer.pause();
                taskStarted = false;
                taskPaused = true;
            }

            // resume a task
            else if (taskStarted == false && taskPaused == true) {
                startBtn.setText("Pause");
                startedTask.resume();
                displayTimer.resume();
                taskStarted = true;
                taskPaused = false;
            }
        }
    };
    
    class ImageTask extends Task<Integer> {
        @Override
        protected Integer call() throws Exception {
        	int timeLimit = (int) (timeLimitSB.getValue() * 1000);
        	PathFinder pathfinder = new PathFinder(sim, arena, robot, startPos, timeLimit, totalImages, rpi);
        	pathfinder.startImageRecognition();
            robot.setStatus("Done image task.\n");

            displayTimer.stop();
            return 1;
        }
    }

    // Event Handler for resetMapBtn
    private EventHandler<MouseEvent> resetMapBtnClick = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {
            resetArenaBtn.setVisible(false);
            internalHandleResetMap();
        }
    };

    private void internalHandleResetMap() {
        if (startedTask != null) {
            startedTask.stop();
            startedTask = null;
        }
        taskStarted = false;
        taskPaused = false;
        startBtn.setText("Start");
        displayTimer.stop();
        displayTimer.initialize();

        arena.resetArena();

        // reset the map and robot
        startBtn.setVisible(true);



        arenaDescriptor.loadRealArena(arena, defaultArenaPath);
        startPos.setLocation(1, 1);
        startPosTxt.setText(String.format("(%d, %d)", 1, 1));
        rightRB.setSelected(true);
        // TODO starting direction is right

        robot = new Robot(sim, false, 1, 1, ArenaDirections.RIGHT);
        robot.setStatus("Reset to start zone");
    }

    // Draw Method for Robot
    public void drawRobot() {
        gc.setStroke(SimulatorConstants.ROBOT_OUTLINE);
        gc.setLineWidth(2);

        gc.setFill(SimulatorConstants.ROBOT_BODY);

        int col = robot.getCurLocation().x - 1;
        int row = robot.getCurLocation().y + 1;
        int dirCol = 0, dirRow = 0;

        gc.strokeOval(col * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2,
                (ArenaConstants.ARENA_CELL_SIZE - 1) * ArenaConstants.ARENA_LENGTH - row * ArenaConstants.ARENA_CELL_SIZE
                        + ArenaConstants.ARENA_OFFSET / 2,
                3 * ArenaConstants.ARENA_CELL_SIZE, 3 * ArenaConstants.ARENA_CELL_SIZE);
        gc.fillOval(col * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2,
                (ArenaConstants.ARENA_CELL_SIZE - 1) * ArenaConstants.ARENA_LENGTH - row * ArenaConstants.ARENA_CELL_SIZE
                        + ArenaConstants.ARENA_OFFSET / 2,
                3 * ArenaConstants.ARENA_CELL_SIZE, 3 * ArenaConstants.ARENA_CELL_SIZE);

        gc.setFill(SimulatorConstants.ROBOT_DIRECTION);
        switch (robot.getDir()) {
            case UP:
                dirCol = robot.getCurLocation().x;
                dirRow = robot.getCurLocation().y + 1;
                break;
            case DOWN:
                dirCol = robot.getCurLocation().x;
                dirRow = robot.getCurLocation().y - 1;
                break;
            case LEFT:
                dirCol = robot.getCurLocation().x - 1;
                dirRow = robot.getCurLocation().y;
                break;
            case RIGHT:
                dirCol = robot.getCurLocation().x + 1;
                dirRow = robot.getCurLocation().y;
                break;
        }
        gc.fillOval(dirCol * ArenaConstants.ARENA_CELL_SIZE + ArenaConstants.ARENA_OFFSET / 2,
                (ArenaConstants.ARENA_CELL_SIZE - 1) * ArenaConstants.ARENA_LENGTH - dirRow * ArenaConstants.ARENA_CELL_SIZE
                        + ArenaConstants.ARENA_OFFSET / 2,
                ArenaConstants.ARENA_CELL_SIZE, ArenaConstants.ARENA_CELL_SIZE);
    }

}
