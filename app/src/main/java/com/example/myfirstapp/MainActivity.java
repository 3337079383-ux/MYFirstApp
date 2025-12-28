package com.example.myfirstapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GobangView gobangView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gobangView = findViewById(R.id.my_gobang_view);
        Button btnRestart = findViewById(R.id.btn_restart);
        EditText blackNameInput = findViewById(R.id.et_black_name);
        EditText whiteNameInput = findViewById(R.id.et_white_name);

        if (gobangView != null && btnRestart != null) {
            btnRestart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String blackName = blackNameInput != null ? blackNameInput.getText().toString() : null;
                    String whiteName = whiteNameInput != null ? whiteNameInput.getText().toString() : null;
                    gobangView.setPlayerNames(blackName, whiteName);
                    gobangView.restartGame();
                }
            });
        }
    }
}
