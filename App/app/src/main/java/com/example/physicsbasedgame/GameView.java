package com.example.physicsbasedgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private static final int MAX_SPEED = 1000;
    private static final float SPEED_ACCELERATOR = .5f;
    private static final float SPEED = 3;

    private GameThread thread;
    private Player player;
    private Paint paintPlayer;
    private Paint paintWall;
    private ArrayList<Wall> walls;
    private Timer timer;
    private Random random;

    private long left;
    private long right;
    private long top;
    private long bottom;

    private boolean hitWall = false;
    private boolean hitTop = false;
    private boolean leftRight = true;

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
        paintPlayer = new Paint();
        paintWall = new Paint();

        walls = new ArrayList<>();
        random = new Random();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                createWall();
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 0,
                1200);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();

        top = getBottom() - 300;
        bottom = getBottom() - 250;
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
    public void draw(final Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(Color.BLACK);

            paintWall.setColor(Color.GREEN);

            if (hitWall) paintPlayer.setColor(Color.BLUE);
            else paintPlayer.setColor(Color.RED);

            canvas.drawRect(player, paintPlayer);

            for (Wall w : walls) {
                canvas.drawRect(w, paintWall);
            }
        }
    }

    @Override
    public void onDraw(final Canvas canvas) {

        for (Wall w : walls) {
            w.offsetTo(w.left, w.top + SPEED + SPEED * SPEED_ACCELERATOR);
            canvas.drawRect(w, paintWall);
        }

        if (!hitWall) {

            paintPlayer.setColor(Color.YELLOW);
            canvas.drawRect(player, paintPlayer);
        }
    }

    public void createWall() {
        int i = ThreadLocalRandom.current().nextInt(100);
        if (i % 2 == 0) {
            i = ThreadLocalRandom.current().nextInt(200, 700);
            Wall w = new Wall(0, 0, i, 25);
            walls.add(w);
        }
        else {
            i = ThreadLocalRandom.current().nextInt(200, 700);
            Wall w = new Wall(getRight() - i, 0, getRight(), 25);
            walls.add(w);
        }
        System.out.println(walls.size());
    }
}