package com.example.demo;

import javafx.scene.shape.Circle;
public class MyCircle extends Circle {
    private int x;
    private int y;
    private MyColor color = MyColor.NOCOLOR;

    public MyCircle(int x, int y, double v, double v1, double v2) {
        super(v, v1, v2);
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public MyColor getColor() {
        return color;
    }
    public void setColor(MyColor color) {
        this.color = color;
    }
}
