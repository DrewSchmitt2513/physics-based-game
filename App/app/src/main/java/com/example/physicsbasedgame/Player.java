package com.example.physicsbasedgame;

import android.graphics.RectF;

public class Player extends RectF {
    public Player() {
        super();
    }
    public Player(float left, float top, float right, float bottom) {
        super(left, top, right, bottom);
    }
    public Player(RectF r) {
        super(r);
    }
    public void setTop(float top){
        this.top = top;
    }
    public void setBottom(float bottom){
        this.bottom = bottom;
    }
    public void setRight(float right){
        this.right = right;
    }
    public void setLeft(float left){
        this.left = left;
    }


}
