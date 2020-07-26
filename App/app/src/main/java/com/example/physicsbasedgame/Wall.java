package com.example.physicsbasedgame;

import android.graphics.RectF;

public class Wall extends RectF {

    private boolean passed;

    public Wall(float left, float top, float right, float bottom) {
        super(left, top, right, bottom);
    }

    void setPassedByPlayer(boolean passed) {
        this.passed = passed;
    }

    boolean getPassed() {
        return passed;
    }
}
