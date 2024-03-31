package com.example.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;


public class HelloApplication extends Application {
    private boolean isBLACK = true;
    private int BOARD_SIZE = 15;
    private BorderPane pane;
    private Pair<Integer, Integer> lastPos = null;
    private MyColor[][] board = new MyColor[BOARD_SIZE][BOARD_SIZE];
    private String blackName = "黑方";
    private String whiteName = "白方";
    private Label currentUserLable = null;
    private int maxX = 0;
    private int minX = BOARD_SIZE;
    private int maxY = 0;
    private int minY = BOARD_SIZE;
    private boolean canRedo = false;
    private boolean canUndo = false;
    private Button redoButton = null;
    private Button undoButton = null;


    @Override
    public void start(Stage primaryStage) {
        Arrays.stream(board).forEach(row -> Arrays.fill(row, MyColor.NOCOLOR));
        checkLoad();
        pane = new BorderPane();
        primaryStage.setScene(new Scene(pane, 1000, 800));
        primaryStage.setTitle("Gomoku Board");
        primaryStage.show();
        drawBoard();

        primaryStage.widthProperty().addListener((obs, oldWidth, newWidth) -> drawBoard());
        primaryStage.heightProperty().addListener((obs, oldHeight, newHeight) -> drawBoard());
    }

    private void checkLoad() {
        File file = new File(GomokuGameState.filename);
        if (file.exists()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("是否恢复棋局");
            alert.setHeaderText("检测到存在已保存的棋局，是否需要恢复？");

            ButtonType yesButton = new ButtonType("是");
            ButtonType noButton = new ButtonType("否");

            alert.getButtonTypes().setAll(yesButton, noButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == yesButton) {
                // 用户选择是，从保存的文件中恢复棋局
                try {
                    GomokuGameState gameState = GomokuGameState.loadFromFile();
                    isBLACK = gameState.isBLACK();
                    BOARD_SIZE = gameState.getBOARD_SIZE();
                    lastPos = gameState.getLastPos();
                    board = gameState.getBoard();
                    blackName = gameState.getBlackName();
                    whiteName = gameState.getWhiteName();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private VBox createSettingsBox(String playerType, String nameLabel, String setNameButtonLabel, String modifyNameButtonLabel, int settingType) {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.CENTER);

        Label label = new Label(nameLabel + playerType);
        TextField nameTextField = new TextField();
        Button setNameButton = new Button(setNameButtonLabel);
        Button modifyNameButton = new Button(modifyNameButtonLabel);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(setNameButton, modifyNameButton);

        vbox.getChildren().addAll(label, nameTextField, buttonBox);

        // 设置按钮点击事件处理
        setNameButton.setOnAction(e -> {
            String text = nameTextField.getText();
            if (!text.isEmpty()) {
                if (settingType == 0) {
                    label.setText(nameLabel + text);
                    blackName = text;
                } else if (settingType == 1) {
                    label.setText(nameLabel + text);
                    whiteName = text;
                } else {
                    try {
                        int newBoardSize = Integer.parseInt(text);
                        int width = maxX - minX + 1;
                        int height = maxY - minY + 1;
                        int minBoardSize = Math.max(width, height);
                        if (newBoardSize != BOARD_SIZE && newBoardSize >= minBoardSize && newBoardSize >= 5) {
                            MyColor[][] newBoard = new MyColor[newBoardSize][newBoardSize];
                            int middle = newBoardSize / 2;
                            int startX = middle - (maxX - minX + 1) / 2;
                            int startY = middle - (maxY - minY + 1) / 2;
                            Arrays.stream(newBoard).forEach(row -> Arrays.fill(row, MyColor.NOCOLOR));
                            for (int i = 0; i <= maxX - minX; i++) {
                                for (int j = 0; j <= maxY - minY; j++) {
                                    newBoard[startX + i][startY + j] = board[i + minX][j + minY];
                                }
                            }
                            lastPos = new Pair<>(lastPos.getKey() - minX + startX, lastPos.getValue() - minY + startY);
                            board = newBoard;
                            BOARD_SIZE = newBoardSize;
                            minX = startX;
                            minY = startY;
                            maxX = startX + width - 1;
                            maxY = startY + height - 1;
                            GomokuGameState.deleteSave();
                            drawBoard();
                        }
                    } catch (NumberFormatException exception) {
                        nameTextField.clear();
                    }
                }
                currentUserLable.setText("当前用户：" + (isBLACK ? blackName : whiteName));
            }
        });

        modifyNameButton.setOnAction(e -> {
            nameTextField.clear();
            nameTextField.requestFocus();
        });

        return vbox;
    }

    private void drawBoard() {
        pane.getChildren().clear();
        double boardSize = Math.min(pane.getWidth() * 0.8, pane.getHeight());
        Pane boardPane = getBoardPane(boardSize);
        VBox settingBox = getSettingBox();
        pane.setLeft(boardPane);
        pane.setRight(settingBox);
    }

    private Pane getBoardPane(double boardSize) {
        Pane boardPane = new Pane();
        double cellSize = boardSize / (BOARD_SIZE + 1);

        boardPane.getChildren().clear();
        boardPane.setBackground(new Background(new BackgroundFill(Color.BISQUE, null, null)));
        for (int i = 0; i < BOARD_SIZE; i++) {
            Line colline = new Line(cellSize * (i + 1), cellSize, cellSize * (i + 1), boardSize - cellSize);
            Line rowline = new Line(cellSize, cellSize * (i + 1), boardSize - cellSize, cellSize * (i + 1));
            colline.toBack();
            rowline.toBack();
            boardPane.getChildren().addAll(colline, rowline);
        }
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                MyCircle circle = new MyCircle(i, j, cellSize * (j + 1), cellSize * (i + 1), 0.33 * cellSize);
                if (board[i][j] == MyColor.NOCOLOR) {
                    circle.setFill(Color.TRANSPARENT);
                } else if (board[i][j] == MyColor.BLACK) {
                    circle.setFill(Color.BLACK);
                    circle.setColor(MyColor.BLACK);
                } else {
                    circle.setFill(Color.WHITE);
                    circle.setColor(MyColor.WHITE);
                }
                circle.toFront();
                circle.setStroke(null);
                circle.setOnMouseClicked(e -> handleCircleClick(circle));
                boardPane.getChildren().add(circle);
            }
        }
        return boardPane;
    }

    private void reset() {
        isBLACK = true;
        Arrays.stream(board).forEach(row -> Arrays.fill(row, MyColor.NOCOLOR));
        lastPos = null;
        canUndo = false;
        canRedo = false;
        maxX = 0;
        minX = BOARD_SIZE;
        maxY = 0;
        minY = BOARD_SIZE;
        drawBoard();
        GomokuGameState.deleteSave();
    }

    private VBox getSettingBox() {
        VBox currentUserVBox = new VBox(10);
        currentUserVBox.setPadding(new Insets(10));
        currentUserVBox.setAlignment(Pos.CENTER);
        currentUserLable = new Label("当前用户：" + (isBLACK ? blackName : whiteName));
        currentUserVBox.getChildren().addAll(currentUserLable);

        VBox blackBox = createSettingsBox(blackName, "黑方用户名：", "设置", "修改", 0);
        VBox whiteBox = createSettingsBox(whiteName, "白方用户名：", "设置", "修改", 1);
        VBox boardSetting = createSettingsBox(String.valueOf(BOARD_SIZE), "当前BOARD_SIZE：", "设置", "修改", 2);

        HBox hBox = new HBox(10);
        Button saveButton = new Button("保存");
        saveButton.setOnAction(e -> {
            GomokuGameState state = new GomokuGameState(BOARD_SIZE, board, blackName, whiteName, isBLACK, lastPos);
            try {
                if (lastPos != null) {
                    state.saveToFile();
                }
            } catch (IOException ex) {
                System.out.println("save failed");
                throw new RuntimeException(ex);
            }
        });
        Button resetButton = new Button("重置");
        resetButton.setOnAction(e -> {
            reset();
        });
        redoButton = new Button("悔棋");
        redoButton.setOnAction(e -> {
            if (canRedo) {
                board[lastPos.getKey()][lastPos.getValue()] = MyColor.NOCOLOR;
                isBLACK = !isBLACK;
                currentUserLable.setText("当前用户：" + (isBLACK ? blackName : whiteName));
                canRedo = false;
                canUndo = true;
                redoButton.setDisable(canUndo);
                undoButton.setDisable(canRedo);
                drawBoard();
            }
        });
        redoButton.setDisable(true);
        undoButton = new Button("撤销悔棋");
        undoButton.setOnAction(e -> {
            if (canUndo) {
                board[lastPos.getKey()][lastPos.getValue()] = isBLACK ? MyColor.BLACK : MyColor.WHITE;
                isBLACK = !isBLACK;
                currentUserLable.setText("当前用户：" + (isBLACK ? blackName : whiteName));
                canRedo = true;
                canUndo = false;
                redoButton.setDisable(canUndo);
                undoButton.setDisable(canRedo);
                drawBoard();
            }
        });
        undoButton.setDisable(true);
        hBox.getChildren().addAll(saveButton, resetButton, redoButton, undoButton);

        // 将黑方和白方设置框放在一个HBox中
        VBox settingsBox = new VBox(30);
        settingsBox.getChildren().addAll(currentUserVBox, blackBox, whiteBox, boardSetting, hBox);
        return settingsBox;
    }

    private void handleCircleClick(MyCircle circle) {
        if (circle.getColor() == MyColor.NOCOLOR) {
            circle.setFill(isBLACK ? Color.BLACK : Color.WHITE);
            circle.setColor(isBLACK ? MyColor.BLACK : MyColor.WHITE);
            board[circle.getX()][circle.getY()] = isBLACK ? MyColor.BLACK : MyColor.WHITE;
            boolean result = checkWin(circle.getX(), circle.getY(), circle.getColor());
            if (result) {
                showAlert("win", (isBLACK ? blackName : whiteName) + "win");
                reset();
            } else {
                isBLACK = !isBLACK;
                circle.toFront();
                currentUserLable.setText("当前用户：" + (isBLACK ? blackName : whiteName));
                lastPos = new Pair<>(circle.getX(), circle.getY());
                maxX = Math.max(maxX, circle.getX());
                minX = Math.min(minX, circle.getX());
                maxY = Math.max(maxY, circle.getY());
                minY = Math.min(minY, circle.getY());
                canRedo = true;
                redoButton.setDisable(false);
                undoButton.setDisable(true);
            }
        }
    }

    public boolean checkWin(int row, int col, MyColor color) {
        return checkHorizontal(row, col, color) ||
                checkVertical(row, col, color) ||
                checkDiagonalLeft(row, col, color) ||
                checkDiagonalRight(row, col, color);
    }

    private boolean checkHorizontal(int row, int col, MyColor color) {
        int count = 0;
        for (int c = Math.max(0, col - 4); c <= Math.min(BOARD_SIZE - 1, col + 4); c++) {
            if (board[row][c] == color) {
                count++;
                if (count == 5) {
                    return true;
                }
            } else {
                count = 0;
            }
        }
        return false;
    }

    private boolean checkVertical(int row, int col, MyColor color) {
        int count = 0;
        for (int r = Math.max(0, row - 4); r <= Math.min(BOARD_SIZE - 1, row + 4); r++) {
            if (board[r][col] == color) {
                count++;
                if (count == 5) {
                    return true;
                }
            } else {
                count = 0;
            }
        }
        return false;
    }

    private boolean checkDiagonalLeft(int row, int col, MyColor color) {
        int count = 0;
        for (int d = -4; d <= 4; d++) {
            int r = row + d;
            int c = col + d;
            if (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE) {
                if (board[r][c] == color) {
                    count++;
                    if (count == 5) {
                        return true;
                    }
                } else {
                    count = 0;
                }
            }
        }
        return false;
    }

    private boolean checkDiagonalRight(int row, int col, MyColor color) {
        int count = 0;
        for (int d = -4; d <= 4; d++) {
            int r = row + d;
            int c = col - d;
            if (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE) {
                if (board[r][c] == color) {
                    count++;
                    if (count == 5) {
                        return true;
                    }
                } else {
                    count = 0;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}