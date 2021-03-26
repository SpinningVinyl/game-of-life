package com.pavelurusov.conway;

import com.pavelurusov.squaregrid.SquareGrid;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    @FXML
    public BorderPane root;
    public SquareGrid display;
    @FXML
    public Button advanceButton;
    @FXML
    public Button startStopButton;
    @FXML
    public Label frameLabel;
    @FXML
    public Button rewindButton;

    private boolean[][] board;
    private final List<boolean[][]> frames = new ArrayList<>();

    private int currentFrame;
    private boolean isRunning = false;

    private AnimationTimer timer;

    private final Settings settings = Settings.getInstance();

    public void initialize() {
        board = new boolean[settings.ROWS][settings.COLUMNS];
        currentFrame = 0;

        display = new SquareGrid(settings.ROWS, settings.COLUMNS, settings.SIZE);
        display.setDefaultColor(settings.COLOR_DEAD);
        display.setGridColor(settings.COLOR_GRID);
        display.setAlwaysDrawGrid(true);
        display.setAutomaticRedraw(false);
        root.setCenter(display);

        display.setOnMouseClicked(this::setCells);
        display.setOnMouseDragged(this::setCells);

        timer = new AnimationTimer() {
            long lastFrameTime;
            @Override
            public void handle(long time) {
                if(time - lastFrameTime > settings.INTERVAL) {
                    updateBoard();
                    lastFrameTime = time;
                }
            }
        };

        startStopButton.setOnAction(e -> {
            isRunning = !isRunning;
            if (isRunning) {
                timer.start();
            } else {
                timer.stop();
            }
        });

    }

    private void visualize() {
        for (int row = 0; row < settings.ROWS; row++) {
            for (int column = 0; column < settings.COLUMNS; column++) {
                if(board[row][column]) {
                    display.setCellColor(row, column, settings.COLOR_ALIVE);
                } else {
                    display.setCellColor(row, column, null);
                }
            }
        }
    }

    private void updateFrameCounter() {
        frameLabel.setText("Frame: " + currentFrame);
    }

    private void setCells(MouseEvent e) {
        if(!isRunning) {
            int row = display.yToRow(e.getY());
            int column = display.xToColumn(e.getX());
            if (e.getButton() == MouseButton.PRIMARY) { // LMB + drag
                board[row][column] = true;
            } else if (e.getButton() == MouseButton.SECONDARY) { // RMB + drag
                board[row][column] = false;
            }
            visualize();
            display.redraw();
        }
    }

    public void rewind() {
        if (!isRunning && !frames.isEmpty()) {
            currentFrame = Math.max(currentFrame - 1, 0);
            board = frames.get(currentFrame);
            visualize();
            updateFrameCounter();
            display.redraw();
        }
    }

    public void updateBoard() {
        currentFrame++;
        if(currentFrame < frames.size() - 1) { // if we're not at the latest frame
            // discard all frames after the current one since the user may have added/removed cells
            frames.removeIf(frame -> frames.indexOf(frame) >= currentFrame);
        }
        frames.add(board);
        board = new boolean[settings.ROWS][settings.COLUMNS];
        for (int row = 0; row < settings.ROWS; row++) {
            for (int column = 0; column < settings.COLUMNS; column++) {
                // count living neighbours of the current cell
                int livingNeighbours = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0) {
                            continue; // don't count the cell itself
                        }
                        int neighbourRow = row + i;
                        int neighbourColumn = column + j;
                        if ((neighbourRow >= 0 // don't go outside of the board
                                && neighbourRow < settings.ROWS)
                                && (neighbourColumn >= 0
                                && neighbourColumn < settings.COLUMNS)) {
                            if (frames.get(frames.size() - 1)[neighbourRow][neighbourColumn]) {
                                livingNeighbours++;
                            }
                        }
                    }
                }
                if (frames.get(frames.size() - 1)[row][column]) {
                    board[row][column] = (livingNeighbours >= 2) && (livingNeighbours <= 3);
                } else {
                    board[row][column] = (livingNeighbours == 3);
                }
            }
        }
        visualize();
        updateFrameCounter();
        display.redraw();
    }

    public void saveBoard() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("jLife board", "*.jlbrd"));
        File saveFile = chooser.showSaveDialog(root.getScene().getWindow());
        if (saveFile != null) {
            try(FileWriter fw = new FileWriter(saveFile);
                BufferedWriter bw = new BufferedWriter(fw)) {
                for (int row = 0; row < settings.ROWS; row++) {
                    for (int column = 0; column < settings.COLUMNS; column++) {
                        if (board[row][column]) {
                            bw.write(row + "," + column + "\n");
                        }
                    }
                }
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
    } // end of saveBoard

    public void loadBoard() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("jLife board", "*.jlbrd"));
        File openFile = chooser.showOpenDialog(root.getScene().getWindow());
        if (openFile != null) {
            String input;
            boolean[][] tempBoard = new boolean[settings.ROWS][settings.COLUMNS];
            try(FileReader fr = new FileReader(openFile);
                BufferedReader br = new BufferedReader(fr)) {
                while((input = br.readLine()) != null) {
                    int row = -1, column = -1;
                    String[] splitInput = input.split(",");
                    if(splitInput.length == 2) {
                        try {
                            row = Integer.parseInt(splitInput[0]);
                            column = Integer.parseInt(splitInput[1]);
                        } catch(NumberFormatException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(row != -1 && column != -1) {
                        tempBoard[row][column] = true;
                    }
                }
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
            board = tempBoard;
            frames.removeIf(frame -> true);
            currentFrame = 0;
            updateFrameCounter();
            visualize();
            display.redraw();
        }
    } // end of loadBoard()

}
