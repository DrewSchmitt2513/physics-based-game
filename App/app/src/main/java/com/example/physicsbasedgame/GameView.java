package com.example.physicsbasedgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread thread;
    private Player player;
    private Paint paint;

    private long left;
    private long right;
    private long top;
    private long bottom;

    private boolean hitBottom = false;
    private boolean hitTop = false;
    private boolean leftRight = true;

    private long deltaX = 25;
    private long deltaY = 600;

    private final double deceleration = -.5;

    private final double gravity = -.98;

    public GameView(Context context) {
        super(context);

        getHolder().addCallback(this);

        thread = new GameThread(getHolder(), this);
        setFocusable(true);
        this.postInvalidate();

        player = new Player();
        left = 50;
        right = 100;
        top = 50;
        bottom = 100;

        player = new Player(left, top, right, bottom);
        paint = new Paint();


    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();

        top = getBottom() - 100;
        bottom = getBottom() - 50;
        left = 500;
        right = 550;
        player.set(left, top, right, bottom);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * Ends the thread and therefore the game. It can
     * take multiple attempts to take down a thread, so
     * we've been advised to put it into this loop.
     *
     * @param holder
     */

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {

            canvas.drawColor(Color.BLACK);
            paint.setColor(Color.RED);


            canvas.drawRect(player, paint);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!hitBottom) {
//            System.out.println(leftRight + " " + hitTop);
            if (player.right >= getRight()) leftRight = false;
            else if (player.left <= getLeft()) leftRight = true;
            else if (player.bottom >= getBottom()) hitBottom = true;
            else if (player.top <= getTop()) hitTop = true;
            paint.setColor(Color.YELLOW);
            if (leftRight) {
                if (!hitTop) goUpRight();
                else goDownRight();
            } else {
                if (!hitTop) goUpLeft();
                else goDownLeft();
            }
            canvas.drawRect(player, paint);
        } else {
            player.offsetTo(left, getBottom() - 50);
            Paint paint = new Paint();
            paint.setColor(Color.BLUE);
            canvas.drawRect(player, paint);
        }
    }

    public void goUpRight() {
        left += deltaX + (deltaX * deceleration);
        top -= deltaY + (deltaY * gravity);

        player.offsetTo(left, top);
    }

    public void goUpLeft() {
        left -= deltaX + (deltaX * deceleration);
        top -= deltaY + (deltaY * gravity);

        player.offsetTo(left, top);
    }

    public void goDownLeft() {
        left -= deltaX + (deltaX * deceleration);
        top += deltaY + (deltaY * gravity);

        player.offsetTo(left, top);
    }

    public void goDownRight() {
        left += deltaX + (deltaX * deceleration);
        top += deltaY + (deltaY * gravity);

        player.offsetTo(left, top);
    }
}
