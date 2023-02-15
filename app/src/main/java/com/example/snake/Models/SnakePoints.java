package com.example.snake.Models;

public class SnakePoints {

    private int positionX, positionY;

    public SnakePoints(int positionX, int positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public int getPositionX() {
        return positionX;
    }

    public SnakePoints setPositionX(int positionX) {
        this.positionX = positionX;
        return this;
    }

    public int getPositionY() {
        return positionY;
    }

    public SnakePoints setPositionY(int positionY) {
        this.positionY = positionY;
        return this;
    }
}
