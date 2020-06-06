package com.example.physicsbasedgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

class GameView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {

    private static final float MAX_WALL_SPEED = 20;
    private static final float WALL_SPEED_ACCELERATOR = .0005f;
    private static final float PLAYER_MOVE_SPEED = 20;
    private static final float MAX_PLAYER_VELOCITY = 80; //I put a random number in here... I need to play around with numbers to figure it out. Might not be needed.

    /**
     * Base speed of all of the Wall objects
     */
    private float wallSpeed = 3;

    /**
     * Value used for computing the corrected player
     * move speed to give it a smooth animation
     */
    private float time = 0.667f;
    private float velocity = 0.0f;
    private float distanceTravelled = 0.0f;
    private float xAccel;
    private int rate = 1200;
    private long left;
    private long right;

    private GameThread thread;
    private Player player;
    private Paint paintPlayer;
    private Paint paintWall;
    private ArrayList<Wall> walls;
    private SensorManager sensorManager;
    private Sensor sensor;

    private boolean hitWall = false;

    public GameView(Context context) {
        super(context);

        getHolder().addCallback(this);

        thread = new GameThread(getHolder(), this);
        setFocusable(true);
        this.postInvalidate();

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

        left = 50;
        right = 100;

        player = new Player(left, 0, right, 0);
        paintPlayer = new Paint();
        paintWall = new Paint();

        walls = new ArrayList<>();

        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                createWall();
                destroyWall();
            }
        };
        final Timer timer = new Timer();

        Thread wallThread = new Thread() {
            @Override
            public void run() {

                //TODO: As the walls speed up, we need to find an efficient way to cancel and reschedule the timer task to make sure that the distance between walls does not grow significantly
                timer.scheduleAtFixedRate(task, 0,
                        rate);
            }
        };
        wallThread.run();

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
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(Color.BLACK);

            paintWall.setColor(Color.GREEN);

            if (hitWall) paintPlayer.setColor(Color.BLUE);
            else paintPlayer.setColor(Color.RED);

            if (!hitWall) {
                updatePositions();
                canvas.drawRect(player, paintPlayer);
                for (Wall w : walls) {
                    canvas.drawRect(w, paintWall);
                }
            }
        }
    }

    /**
     * Continuously spawns wall objects with random lengths between 200 and 700 pixels
     * on randomly selected sides
     */
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

    /**
     * Updates the positions of the walls and the players
     * without drawing them immediately. Called by draw
     */
    public void updatePositions() {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (wallSpeed > MAX_WALL_SPEED) {
                    wallSpeed = MAX_WALL_SPEED;
                } else {
                    wallSpeed += wallSpeed * WALL_SPEED_ACCELERATOR;
                }

                for (Wall w : walls) {
                    w.offsetTo(w.left, w.top + wallSpeed);
                }

                velocity += xAccel * time * PLAYER_MOVE_SPEED;
                distanceTravelled = (velocity / 2) * time;

                player.offsetTo(left - distanceTravelled, getBottom() - 300);
            }
        });
        t.run();

    }

    /**
     * Checks through all of the wall objects to decide whether or not to destroy them
     * to keep the memory from overflowing
     */
    public void destroyWall() {
        if (walls.size() > 8) {
            walls.remove(0);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        xAccel = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //TODO: MIGHT NOT NEED.
    //IDEA: USE SURFACEDESTROYED METHOD TO STORE VITAL DATA
//    public void pause() {
//        if (!thread.isInterrupted()) thread.interrupt();
//    }
//    public void resume() {
//        if (thread.isInterrupted()) thread.start();
//    }
}