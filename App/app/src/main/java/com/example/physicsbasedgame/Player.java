package com.example.physicsbasedgame;

import android.graphics.RectF;

public class Player extends RectF{
    public Player(float left, float top, float right, float bottom) {
        super(left, top, right, bottom);
    }

    /**
     * checks to see if the player came into contact with a wall.
     * Does not check for boundary collisions from the left and right side of the screen
     * @param wall
     * @return
     */
    public boolean hit(RectF wall) {
        if (intersects(wall.left, wall.top, wall.right, wall.bottom)) return true;
        return false;
    }
}
