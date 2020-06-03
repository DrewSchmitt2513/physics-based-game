package com.example.physicsbasedgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private static final float MAX_WALL_SPEED = 20;
    private static final float WALL_SPEED_ACCELERATOR = .0005f;
    private static final float PLAYER_MOVE_SPEED = 20;
    private static final float MAX_PLAYER_VELOCITy = 80; //I put a random number in here... I need to play around with numbers to figure it out. Might not be needed.

    private float wallSpeed = 3;
    private float time = 0.667f;
    private float velocity = 0.0f;
    private int rate = 1200;

    private GameThread thread;
    private Player player;
    private Paint paintPlayer;
    private Paint paintWall;
    private ArrayList<Wall> walls;
    private AccelerometerHandler accelerometerHandler;

    private long left;
    private long right;

    private boolean hitWall = false;

    public GameView(Context context) {
        super(context);

        getHolder().addCallback(this);

        thread = new GameThread(getHolder(), this);
        setFocusable(true);
        this.postInvalidate();

        player = new Player();
        left = 50;
        right = 100;

        player = new Player(left, 0, right, 0);
        paintPlayer = new Paint();
        paintWall = new Paint();

        walls = new ArrayList<>();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                createWall();
            }
        };
        Timer timer = new Timer();

        //TODO: As the walls speed up, we need to find an efficient way to cancel and reschedule the timer task to make sure that the distance between walls does not grow
        timer.scheduleAtFixedRate(task, 0,
                rate);

        Thread accelThread = new Thread() {
            @Override
            public void run() {
                accelerometerHandler = new AccelerometerHandler(getContext());
                destroyWall();
            }
        };
        accelThread.run();


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();

        left = 500;
        right = 550;
        player.set(left, getBottom() - 300, right, getBottom() - 250);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * Ends the thread and therefore the game. It can
     * take multiple attempts to take down a thread, so
     * we've been advised to put it into this loop.
     *
     * @param holder the surface holder of the canvas
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

        if (wallSpeed > MAX_WALL_SPEED) {
            System.out.println(wallSpeed);
            wallSpeed = MAX_WALL_SPEED;
        } else {
            wallSpeed += wallSpeed * WALL_SPEED_ACCELERATOR;
        }

        for (Wall w : walls) {
            w.offsetTo(w.left, w.top + wallSpeed);
            canvas.drawRect(w, paintWall);
        }

        if (!hitWall) {

            paintPlayer.setColor(Color.YELLOW);
            movePlayer();
            canvas.drawRect(player, paintPlayer);
        }
    }

    public void movePlayer() {

        final Thread t = new Thread() {
            @Override
            public void run() {
                float x = accelerometerHandler.getxAccel();
                velocity += x * time * PLAYER_MOVE_SPEED;

                float distanceTravelled = (velocity / 2) * time;
                player.offsetTo(left - distanceTravelled, getBottom() - 300);
            }
        };
        t.run();
    }

    public void createWall() {
        int i = ThreadLocalRandom.current().nextInt(100);
        if (i % 2 == 0) {
            i = ThreadLocalRandom.current().nextInt(200, 700);
            Wall w = new Wall(0, 0, i, 25);
            walls.add(w);
        } else {
            i = ThreadLocalRandom.current().nextInt(200, 700);
            Wall w = new Wall(getRight() - i, 0, getRight(), 25);
            walls.add(w);
        }
    }

    public void destroyWall() {
        for (Wall w : walls) {
            if (w.bottom >= getBottom()) walls.remove(w);
        }
    }
}