package com.example.myfirstapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private GobangView gobangView;
    private TextView tvCurrentMode;
    private TextView tvGameStatus;
    private LinearLayout llPlayerNames;
    private EditText blackNameInput;
    private EditText whiteNameInput;
    private Button btnRestart;
    private Button btnBackMenu;

    private MenuActivity.GameMode gameMode;
    private MenuActivity.AIDifficulty aiDifficulty;
    private boolean gameStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Get game mode from intent
        int modeOrdinal = getIntent().getIntExtra("game_mode", 0);
        int difficultyOrdinal = getIntent().getIntExtra("ai_difficulty", 0);

        gameMode = MenuActivity.GameMode.values()[modeOrdinal];
        aiDifficulty = MenuActivity.AIDifficulty.values()[difficultyOrdinal];

        initViews();
        setupGameMode();
        setClickListeners();
    }

    private void initViews() {
        gobangView = findViewById(R.id.my_gobang_view);
        tvCurrentMode = findViewById(R.id.tv_current_mode);
        tvGameStatus = findViewById(R.id.tv_game_status);
        llPlayerNames = findViewById(R.id.ll_player_names);
        blackNameInput = findViewById(R.id.et_black_name);
        whiteNameInput = findViewById(R.id.et_white_name);
        btnRestart = findViewById(R.id.btn_restart);
        btnBackMenu = findViewById(R.id.btn_back_menu);

        // Settings button
        findViewById(R.id.btn_settings).setOnClickListener(v -> {
            Intent intent = new Intent(GameActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Set GameActivity reference in GobangView for status updates
        gobangView.setGameActivity(this);
    }

    private void setupGameMode() {
        String modeText = "";

        switch (gameMode) {
            case PVP:
                modeText = "当前模式: 双人对战";
                llPlayerNames.setVisibility(View.VISIBLE);
                break;
            case AI:
                modeText = "当前模式: 人机对战";
                llPlayerNames.setVisibility(View.GONE);
                break;
        }

        tvCurrentMode.setText(modeText);
        updateGameStatus("请点击开始");

        // Set game mode in GobangView
        gobangView.setGameMode(gameMode);
    }

    private void setClickListeners() {
        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameMode == MenuActivity.GameMode.PVP) {
                    String blackName = blackNameInput != null ? blackNameInput.getText().toString() : null;
                    String whiteName = whiteNameInput != null ? whiteNameInput.getText().toString() : null;
                    gobangView.setPlayerNames(blackName, whiteName);
                }
                gobangView.restartGame();
                gameStarted = true;
                updateButtonText();
                // Don't set status here, let GobangView handle it when game actually starts
            }
        });

        btnBackMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Return to MenuActivity
            }
        });
    }

    public void updateGameStatus(String status) {
        tvGameStatus.setText("当前状态: " + status);
    }

    public void updateButtonText() {
        if (gameStarted) {
            btnRestart.setText("重新开始");
        } else {
            btnRestart.setText("开始");
        }
    }

    public void onGameEnd() {
        // Keep gameStarted as true so button shows "重新开始"
        updateButtonText();
    }

    public MenuActivity.GameMode getGameMode() {
        return gameMode;
    }

    public String getPlayerName(boolean isBlack) {
        if (gameMode == MenuActivity.GameMode.AI) {
            return isBlack ? "玩家" : "AI";
        } else {
            String blackName = blackNameInput != null ? blackNameInput.getText().toString().trim() : "";
            String whiteName = whiteNameInput != null ? whiteNameInput.getText().toString().trim() : "";

            if (isBlack) {
                return blackName.isEmpty() ? "黑棋玩家" : blackName;
            } else {
                return whiteName.isEmpty() ? "白棋玩家" : whiteName;
            }
        }
    }
}