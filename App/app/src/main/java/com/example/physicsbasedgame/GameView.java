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
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

class GameView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {
    /**
     * The rate at which a wall will spawn on the screen
     */
    private int rate = 1200;

    private float rateMultiplier = 1.2f;


    /**
     * Controls the game.
     */
    private GameThread thread;
    private Thread positionsThread;
    private Thread wallThread;
    private GameActivity gameActivity;
    private ObjectController objectController;

    private Player player;
    private Paint paintPlayer;
    private Paint paintWall;
    private ArrayList<Wall> walls;
    private SensorManager sensorManager;
    private Sensor sensor;
    private Timer timer;

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

        objectController = new ObjectController(thread, this);

    }

    public void setScoreView(TextView scoreValue, GameActivity gameActivity) {
        this.scoreValue = scoreValue;
        this.gameActivity = gameActivity;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();


        paintPlayer.setColor(Color.BLUE);

        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                thread.createWalls(getRight());
                thread.destroyWall();
            }
        };
        timer = new Timer();

        wallThread = new Thread() {
            @Override
            public void run() {

                //TODO: As the walls speed up, we need to find an efficient way to cancel and reschedule the timer task to make sure that the distance between walls does not grow significantly
//                timer.scheduleAtFixedRate(task, 0,
//                        rate);

                timer.scheduleAtFixedRate(task, 0, 1000);

//                if (score == 0) timer.scheduleAtFixedRate(task, 0, rate);
//                else if (score % 10 == 0 && wallSpeed < MAX_WALL_SPEED) {
//                    timer.cancel();
//                    rateMultiplier += .1f;
//                    rate -= 200 * rateMultiplier;
//                    timer.scheduleAtFixedRate(task, 0, rate);
//                    Log.d("RATE", rate + "");
//                }
            }
        };
        wallThread.run();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * Ends the thread and therefore the game.
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
                //updatePositions();
                objectController.updatePositions();

                canvas.drawBitmap(objectController.getPlayer().getPlayerImage(), objectController.getPlayer().left + 15, objectController.getPlayer().top, null);
                for (Wall w : thread.getWalls()) {
                    canvas.drawRect(w, paintWall);
                }
            }
        }
    }

    void setScore(final int score) {
        gameActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String s = "Score: " + score;
                scoreValue.setText(s);
            }
        });
    }

    void endGame() {
        hitWall = true;

        paintPlayer.setColor(Color.RED);

        thread.setRunning(false);

        timer.cancel();

        //Spins up the EndActivity
        Intent i = new Intent(gameActivity, EndActivity.class);
        i.putExtra("FINAL_SCORE", objectController.getScore());
        getContext().startActivity(i);

        gameActivity.finish();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        /*I'm making the assumption here that the phone will never be held at a 90degree angle
        w.r.t. the horizon. I still need to add in a check for if the device is titled down or up
        because that reverses the tilt direction. */


        //X-Axis: Used to measure horizontal tilt of device
        if (objectController != null) objectController.setxAccel(event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void resume() {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
    }

    public void pause() {
        sensorManager.unregisterListener(this);

        timer.cancel();

        thread.setRunning(false);
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}