package com.example.physicsbasedgame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

class GameView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {

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
     * Slows down the observed movement of the Player
     */
    private static final float PLAYER_ACCELERATOR = .6f;
    /**
     * The maximum speed of the Player when moving left or right
     */
    private static final float MAX_PLAYER_SPEED = 35; //I put a random number in here... I need to play around with numbers to figure it out. Might not be needed.
    /**
     * Base speed of all of the Wall objects
     */
    private float wallSpeed = 3;
    /**
     * Value used for computing the corrected player
     * move speed to give it a smooth animation
     */
    private float time = 0.45f;
    /**
     * All values used in interpreting the movement of the device
     * to move the Player left or right (smoothly) across the screen
     */
    private float xVelocity, distanceTravelled, xAccel = 0.0f;
    /**
     * The location of the left side of the Player. We use it to set
     * the initial location and also to easily keep track of where
     * the Player is.
     */
    private long playerPos;
    /**
     * The rate at which a wall will spawn on the screen
     */
    private int rate = 1200;

    private float rateMultiplier = 1.2f;


    private GameThread thread;
    private Thread positionsThread;
    private Thread wallThread;
    private GameActivity gameActivity;

    private Player player;
    private Paint paintPlayer;
    private Paint paintWall;
    private ArrayList<Wall> walls;
    private SensorManager sensorManager;
    private Sensor sensor;
    private Timer timer;

    private int score = 0;

    private TextView scoreValue;

    private boolean hitWall = false;

    public GameView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        getHolder().addCallback(this);

        thread = new GameThread(getHolder(), this);
        setFocusable(true);
        this.postInvalidate();

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

        paintPlayer = new Paint();
        paintWall = new Paint();

        walls = new ArrayList<>();

    }

    public void setScoreView(TextView scoreValue, GameActivity gameActivity) {
        this.scoreValue = scoreValue;
        this.gameActivity = gameActivity;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();

        playerPos = 500;
        player = new Player(playerPos, getBottom() - 300, playerPos + 50, getBottom() - 250);

        paintPlayer.setColor(Color.RED);

        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                createWall();
                destroyWall();
            }
        };
        timer = new Timer();

        wallThread = new Thread() {
            @Override
            public void run() {

                //TODO: As the walls speed up, we need to find an efficient way to cancel and reschedule the timer task to make sure that the distance between walls does not grow significantly
//                timer.scheduleAtFixedRate(task, 0,
//                        rate);

                if (score == 0) timer.scheduleAtFixedRate(task, 0, rate);
                else if (score % 10 == 0 && wallSpeed < MAX_WALL_SPEED) {
                    timer.cancel();
                    rateMultiplier += .1f;
                    rate -= 200 * rateMultiplier;
                    timer.scheduleAtFixedRate(task, 0, rate);
                    Log.d("RATE", rate + "");
                }
            }
        };
        wallThread.run();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * Ends the thread and therefore the game. I guess it can
     * take multiple attempts to take down a thread, so
     * we've been advised to put it into this loop from a
     * tutorial.
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
        timer.cancel();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(Color.BLACK);

            paintWall.setColor(Color.GREEN);

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
        int i = ThreadLocalRandom.current().nextInt(10000);

        if (i % 8 == 0 || i % 7 == 0 || i % 3 == 0) {
            i = ThreadLocalRandom.current().nextInt(250, 650);
            Wall w = new Wall(0, 0, i, 25);
            walls.add(w);
        } else {
            i = ThreadLocalRandom.current().nextInt(200, 700);
            Wall w = new Wall(getRight() - i, 0, getRight(), 25);
            walls.add(w);
        }
        new ScoreAsyncTask(gameActivity, ++score).execute();
    }

    /**
     * Updates the positions of the walls and the players
     * without drawing them immediately. Called by draw
     */
    public void updatePositions() {

        positionsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (wallSpeed > MAX_WALL_SPEED) {
                    wallSpeed = MAX_WALL_SPEED;
                } else {
                    wallSpeed += wallSpeed * WALL_SPEED_ACCELERATOR;
                }

                xVelocity = xVelocity + (xAccel * time);
                if (xVelocity < MAX_PLAYER_SPEED * -1) {
                    xVelocity = MAX_PLAYER_SPEED * -1;
                }
                else if (xVelocity > MAX_PLAYER_SPEED) {
                    xVelocity = MAX_PLAYER_SPEED;
                }
                distanceTravelled = (xVelocity*time + (xAccel * (time * time))) * PLAYER_ACCELERATOR;

                playerPos -= distanceTravelled;

                //Checks and reacts to the player hitting the edges of the screen
                if (playerPos + 50 > getRight()) {
                    playerPos = getRight() - 50;
                    xVelocity = 0;
                }
                else if (playerPos < getLeft()) {
                    playerPos = getLeft();
                    xVelocity = 0;
                }

                player.offsetTo(playerPos, getBottom() - 300);

                for (Wall w : walls) {
                    w.offsetTo(w.left, w.top + wallSpeed);
                    if (player.hit(w)) {
                        hitWall = true;

                        paintPlayer.setColor(Color.BLUE);

                        thread.setRunning(false);

                        Intent i = new Intent(getContext(), MainActivity.class);
                        getContext().startActivity(i);
                        //TODO: Insert a fragment or card or something to indicate the user lost, show their score, and ask them if they want to restart. Should kill the canvas and all threads.
                    }
                }

            }
        });
        positionsThread.run();
    }

    /**
     * Deletes the first wall in the list to prevent the list from getting too large
     */
    public void destroyWall() {
        if (walls.size() > 8) {
            walls.remove(0);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //I'm making the assumption here that the phone will never be held at a 90degree angle
        //w.r.t. the horizon. In this situation, given that the phone couldn't be held perfectly,
        //it will switch (sometimes rapidly) between moving left and right. When I observed this,
        //I became disoriented, confused, and more confused in trying to figure out how to fix it.


        //X-Axis: Used to measure horizontal tilt of device
        xAccel = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void resume() {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
    }

    public void pause() {
        sensorManager.unregisterListener(this);

        thread.setRunning(false);
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * We only care about onPostExecute because the timer task
     * handles virtually everything and we only need this class
     * to gain access to the UI Thread.
     *
     * This was the only solution Drew found, but if there is a
     * better one we are open to updating the implementation.
     */
    private static class ScoreAsyncTask extends AsyncTask<String, Integer, Integer> {

        private WeakReference<GameActivity> app;
        private int score;

        ScoreAsyncTask(GameActivity context, int score) {
            app = new WeakReference<>(context);
            this.score = score;
        }

        protected void onPreExecute() {
        }

        protected Integer doInBackground(String... strings) {
            return 0;
        }

        protected void onProgressUpdate(Integer... values) {
        }

        protected void onPostExecute(Integer result) {
            GameActivity gameActivity = app.get();
            if (gameActivity == null || gameActivity.isFinishing()) return;

            TextView scoreValue = gameActivity.findViewById(R.id.score_value);
            String s = "Score: " + score;
            scoreValue.setText(s);
        }
    }
}