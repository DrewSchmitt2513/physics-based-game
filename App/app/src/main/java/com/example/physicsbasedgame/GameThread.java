package com.example.physicsbasedgame;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GameThread extends Thread {
    private GameView gameView;
    private final SurfaceHolder surfaceHolder;
    private boolean isRunning;
    private ArrayList<Wall> walls;
    private Player player;
    private static Canvas canvas;

    GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;

        walls = new ArrayList<>();
    }

    @Override
    public void run() {
        while (isRunning) {

            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized(surfaceHolder) {
                    this.gameView.draw(canvas);
                }
            } catch (Exception ignored) {} finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Continuously spawns wall objects with random lengths between 300 and 700 pixels
     * on randomly selected sides
     */
    void createWalls(int right) {

        boolean leftRight = ThreadLocalRandom.current().nextBoolean();
        int wallLength = ThreadLocalRandom.current().nextInt(300, 700);
        Wall w;

        if (leftRight) {
            w = new Wall(0, 0, wallLength, 25);
        }
        else {
            w = new Wall(right - wallLength, 0, right, 25);
        }

        walls.add(w);
    }

    ArrayList<Wall> getWalls() {
        return walls;
    }

    /**
     * Deletes the first wall in the list to prevent the list from getting too large
     */
    void destroyWall() {
        if (walls.size() > 12) {
            walls.remove(0);
        }
    }

    void setRunning(boolean running) {
        isRunning = running;
    }
}
