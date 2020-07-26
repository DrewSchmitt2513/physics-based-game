package com.example.physicsbasedgame;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

class ObjectHandler {

    /**
     * Maximum speed of the walls. Once they reach this, the game has
     * reached its peak difficulty, for now...
     */
    private static final float MAX_WALL_SPEED = 20;
    /**
     * Accelerates the speed of the walls over time until the speed
     * reaches the maximum
     */
    private static final float WALL_SPEED_ACCELERATOR = .0005f;
    /**
     * Base speed of all of the Wall objects
     */
    private float wallSpeed = 3;
    /**
     * Value used for computing the corrected player
     * move speed to give it a smooth animation
     */
    private float time = 0.8f;
    /**
     * All values used in interpreting the movement of the device
     * to move the Player left or right (smoothly) across the screen
     */
    private float xVelocity, distanceTravelled, xAccel, prevXVelocity = 0.0f;
    /**
     * The location of the left side of the Player. We use it to set
     * the initial location and also to easily keep track of where
     * the Player is.
     */
    private long playerPos;

    private int score;

    private Player player;
    private GameThread thread;

    private Handler handler;

    private TextView scoreValue;

    //TODO: Make an adapter that communicates with the GameView
    private GameView gameView;

    ObjectHandler(GameThread thread, GameView gameView) {
        this.thread = thread;
        this.gameView = gameView;

        playerPos = 500;
        player = new Player(playerPos, gameView.getBottom() - 300, playerPos + 50, gameView.getBottom() - 250);

        handler = new Handler(Looper.getMainLooper());
    }

    void updatePositions() {
        if (wallSpeed > MAX_WALL_SPEED) {
            wallSpeed = MAX_WALL_SPEED;
        } else {
            wallSpeed += wallSpeed * WALL_SPEED_ACCELERATOR;
        }

        prevXVelocity = xVelocity;
        xVelocity = (0.5f * xVelocity + (xAccel * time));

                /*
                Smooths over the player movement.
                First two conditions check for a change of direction,
                and prevent a jelly-like movement as the velocity catches
                up with the movement of the phone. They reset the velocity
                to instantaneously catch it up with the phone's movement.
                 */
        if (prevXVelocity < 0 && xVelocity > 0) {
            distanceTravelled = 0;
        } else if (prevXVelocity > 0 && xVelocity < 0) {
            distanceTravelled = 0;
        } else distanceTravelled = xVelocity * time + (xAccel * (time * time));

        playerPos -= distanceTravelled;

        //Checks and reacts to the player hitting the edges of the screen
        if (playerPos + 50 > gameView.getRight()) {
            playerPos = gameView.getRight() - 50;
            xVelocity = 0;
        } else if (playerPos < gameView.getLeft()) {
            playerPos = gameView.getLeft();
            xVelocity = 0;
        }

        player.offsetTo(playerPos, gameView.getBottom() - 300);

        for (Wall w : thread.getWalls()) {
            w.offsetTo(w.left, w.top + wallSpeed);
            if (player.hit(w)) {
                gameView.endGame();
            }
            //If the player passes a wall successfully, add 1 to their score.
            if (w.top >= player.top - 25 && !w.getPassed()) {
                w.setPassedByPlayer(true);

                gameView.setScore(++score);

            }
        }

    }

    void setxAccel(float xAccel) {
        this.xAccel = xAccel;
    }


    Player getPlayer() {
        return player;
    }

    int getScore() {
        return score;
    }


    //TODO: Move object manipulation into this class to simplify the GameView
}
