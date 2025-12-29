package com.example.myfirstapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class RecordsActivity extends AppCompatActivity {

    private TextView tvTotalGames;
    private TextView tvPvpStats;
    private TextView tvAiStats;
    private RecyclerView rvRecentGames;
    private GameRecordsAdapter adapter;
    private GameRecordManager recordManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        recordManager = GameRecordManager.getInstance(this);

        initViews();
        loadStatistics();
        loadRecentGames();
    }

    private void initViews() {
        Button btnBack = findViewById(R.id.btn_back);
        Button btnClear = findViewById(R.id.btn_clear);
        tvTotalGames = findViewById(R.id.tv_total_games);
        tvPvpStats = findViewById(R.id.tv_pvp_stats);
        tvAiStats = findViewById(R.id.tv_ai_stats);
        rvRecentGames = findViewById(R.id.rv_recent_games);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearDialog();
            }
        });

        // Setup RecyclerView
        rvRecentGames.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadStatistics() {
        GameRecordManager.GameStatistics stats = recordManager.getStatistics();

        tvTotalGames.setText(String.valueOf(stats.totalGames));

        // PvP stats
        tvPvpStats.setText(String.format(Locale.getDefault(), "%d胜%d平",
            stats.pvpWins, stats.pvpDraws));

        // AI stats
        int aiWinRate = (int) (stats.getAiWinRate() * 100);
        tvAiStats.setText(String.format(Locale.getDefault(), "%d胜%d负 (胜率 %d%%)",
            stats.aiWins, stats.aiLosses, aiWinRate));
    }

    private void loadRecentGames() {
        List<GameRecordManager.GameRecord> recentGames = recordManager.getRecentGames();

        if (adapter == null) {
            adapter = new GameRecordsAdapter(recentGames);
            rvRecentGames.setAdapter(adapter);
        } else {
            adapter.updateRecords(recentGames);
        }
    }

    private void showClearDialog() {
        new AlertDialog.Builder(this)
            .setTitle("清空记录")
            .setMessage("确定要清空所有比赛记录吗？此操作不可恢复。")
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    recordManager.clearAllRecords();
                    loadStatistics();
                    loadRecentGames();
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadStatistics();
        loadRecentGames();
    }
}