package com.example.physicsbasedgame;

import android.graphics.Bitmap;
import android.graphics.RectF;

class Player extends RectF{

    private Bitmap bmp;

    Player(float left, float top, float right, float bottom, Bitmap bmp) {
        super(left, top, right, bottom);

        this.bmp = bmp;
    }


    //TODO: Add a lot for pixel-ish perfect collision detection using bitmask
    /**
     * checks to see if the player came into contact with a wall.
     * Does not check for boundary collisions from the left and right side of the screen
     * @param wall the obstacle that we are checking against
     * @return true or false
     */
    boolean hit(RectF wall) {
        return intersects(wall.left, wall.top, wall.right, wall.bottom);
    }

    Bitmap getPlayerImage() {
        return bmp;
    }


}
