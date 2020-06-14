package com.example.physicsbasedgame;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class GameActivity extends Activity {

    private GameView gameView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);

        gameView = new GameView(this);

        textView = findViewById(R.id.txt);

        textView.setTextColor(Color.WHITE);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected void onStop()
    {
        // Unregister the listener
        gameView.pause();
        super.onStop();
    }
}
