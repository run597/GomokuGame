package com.example.demo;

import javafx.util.Pair;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;

public class GomokuGameState implements Serializable {
    public static String filename = "state.dat";
    private static final long serialVersionUID = 1L;

    private final int BOARD_SIZE;
    private final MyColor[][] board;
    private final String blackName;
    private final String whiteName;
    private final boolean isBLACK;
    private final Pair<Integer, Integer> lastPos;

    public GomokuGameState(int BOARD_SIZE, MyColor[][] board, String blackName, String whiteName, boolean isBLACK, Pair<Integer, Integer> lastPos) {
        this.BOARD_SIZE = BOARD_SIZE;
        this.board = board;
        this.blackName = blackName;
        this.whiteName = whiteName;
        this.isBLACK = isBLACK;
        this.lastPos = lastPos;
    }

    public int getBOARD_SIZE() {
        return BOARD_SIZE;
    }

    public MyColor[][] getBoard() {
        return board;
    }

    public String getBlackName() {
        return blackName;
    }

    public String getWhiteName() {
        return whiteName;
    }

    public boolean isBLACK() {
        return isBLACK;
    }

    public Pair<Integer, Integer> getLastPos() {
        return lastPos;
    }

    // 将当前棋局保存到文件
    public void saveToFile() throws IOException {
        File file = new File(filename);
        // 如果文件已经存在，删除文件
        if (file.exists()) {
            file.delete();
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
        }
    }

    // 从文件中加载棋局状态
    public static GomokuGameState loadFromFile() throws IOException, ClassNotFoundException {
        File file = new File(filename);
        if (file.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
                return (GomokuGameState) in.readObject();
            }
        } else {
            return null;
        }
    }

    public static void deleteSave() {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GomokuGameState that = (GomokuGameState) o;
        return BOARD_SIZE == that.BOARD_SIZE &&
                isBLACK == that.isBLACK &&
                Objects.equals(blackName, that.blackName) &&
                Objects.equals(whiteName, that.whiteName) &&
                Objects.equals(lastPos, that.lastPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(BOARD_SIZE, blackName, whiteName, isBLACK, lastPos);
    }

    public static void main(String[] args) {
        MyColor[][] board = new MyColor[15][15];
        Arrays.stream(board).forEach(row -> Arrays.fill(row, MyColor.NOCOLOR));

        GomokuGameState state = new GomokuGameState(15, board, "黑方", "白方", true, new Pair<>(1, 1));
        try {
            state.saveToFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GomokuGameState newState = null;
        try {
            newState = GomokuGameState.loadFromFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (state.equals(newState)) {
            System.out.println("save successful");
        } else {
            System.out.println("save error");
        }
    }
}
