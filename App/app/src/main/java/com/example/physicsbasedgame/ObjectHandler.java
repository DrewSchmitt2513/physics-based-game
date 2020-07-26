package com.example.physicsbasedgame;

public class ObjectHandler {

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

    //TODO: Move object manipulation into this class to simplify the GameView
}
