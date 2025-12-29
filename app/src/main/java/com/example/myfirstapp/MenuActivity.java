package com.example.myfirstapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    private Button btnPvp;
    private Button btnAi;
    private Button btnRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        initViews();
        setClickListeners();
    }

    private void initViews() {
        btnPvp = findViewById(R.id.btn_pvp);
        btnAi = findViewById(R.id.btn_ai);
        btnRecords = findViewById(R.id.btn_records);
    }

    private void setClickListeners() {
        btnPvp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(GameMode.PVP, AIDifficulty.NONE);
            }
        });

        btnAi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(GameMode.AI, AIDifficulty.NONE);
            }
        });

        btnRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, RecordsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void startGame(GameMode mode, AIDifficulty difficulty) {
        Intent intent = new Intent(MenuActivity.this, GameActivity.class);
        intent.putExtra("game_mode", mode.ordinal());
        intent.putExtra("ai_difficulty", difficulty.ordinal());
        startActivity(intent);
    }

    // Enums to define game modes and AI difficulty
    public enum GameMode {
        PVP, AI
    }

    public enum AIDifficulty {
        NONE
    }
}