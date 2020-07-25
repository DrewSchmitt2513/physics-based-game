package com.example.physicsbasedgame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class EndActivity extends AppCompatActivity {

    private Button btnRestart;
    private Button btnMenu;

    private TextView txtScore;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        Bundle extras = getIntent().getExtras();

        btnRestart = findViewById(R.id.btnRestart);

        btnMenu = findViewById(R.id.btnMenu);

        txtScore = findViewById(R.id.text_final_score);

        @NonNull int score = extras.getInt("FINAL_SCORE");

        txtScore.setText(String.format("Your Score: %d", score));

        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EndActivity.this, GameActivity.class);

                startActivity(i);
                finish();
            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EndActivity.this, MainActivity.class);

                startActivity(i);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(EndActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
