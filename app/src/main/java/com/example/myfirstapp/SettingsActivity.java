package com.example.myfirstapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String KEY_ANIMATION_ENABLED = "animation_enabled";

    private Button btnSoundToggle;
    private Button btnAnimationToggle;
    private SharedPreferences preferences;

    private boolean soundEnabled = true;
    private boolean animationEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        loadSettings();
        setClickListeners();
    }

    private void initViews() {
        Button btnBack = findViewById(R.id.btn_back);
        btnSoundToggle = findViewById(R.id.btn_sound_toggle);
        btnAnimationToggle = findViewById(R.id.btn_animation_toggle);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadSettings() {
        // 加载音效设置
        soundEnabled = preferences.getBoolean(KEY_SOUND_ENABLED, true);
        updateSoundButton();

        // 加载动画设置
        animationEnabled = preferences.getBoolean(KEY_ANIMATION_ENABLED, true);
        updateAnimationButton();
    }

    private void setClickListeners() {
        btnSoundToggle.setOnClickListener(v -> {
            soundEnabled = !soundEnabled;
            preferences.edit().putBoolean(KEY_SOUND_ENABLED, soundEnabled).apply();
            updateSoundButton();
            Toast.makeText(this, soundEnabled ? "音效已开启" : "音效已关闭", Toast.LENGTH_SHORT).show();
        });

        btnAnimationToggle.setOnClickListener(v -> {
            animationEnabled = !animationEnabled;
            preferences.edit().putBoolean(KEY_ANIMATION_ENABLED, animationEnabled).apply();
            updateAnimationButton();
            Toast.makeText(this, animationEnabled ? "动画已开启" : "动画已关闭", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateSoundButton() {
        btnSoundToggle.setText(soundEnabled ? "音效: 开启" : "音效: 关闭");
        btnSoundToggle.setBackgroundColor(getColor(soundEnabled ?
            android.R.color.holo_green_light : android.R.color.darker_gray));
    }

    private void updateAnimationButton() {
        btnAnimationToggle.setText(animationEnabled ? "动画: 开启" : "动画: 关闭");
        btnAnimationToggle.setBackgroundColor(getColor(animationEnabled ?
            android.R.color.holo_green_light : android.R.color.darker_gray));
    }

    // 静态方法供其他类使用
    public static boolean isSoundEnabled(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_SOUND_ENABLED, true);
    }

    public static boolean isAnimationEnabled(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ANIMATION_ENABLED, true);
    }
}