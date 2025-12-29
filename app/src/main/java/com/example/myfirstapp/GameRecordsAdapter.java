package com.example.myfirstapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GameRecordsAdapter extends RecyclerView.Adapter<GameRecordsAdapter.ViewHolder> {

    private List<GameRecordManager.GameRecord> records;

    public GameRecordsAdapter(List<GameRecordManager.GameRecord> records) {
        this.records = records;
    }

    public void updateRecords(List<GameRecordManager.GameRecord> newRecords) {
        this.records = newRecords;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameRecordManager.GameRecord record = records.get(position);

        holder.tvTimestamp.setText(record.timestamp);
        holder.tvGameMode.setText(record.gameMode);

        // Format winner display and set color
        String winnerText;
        int textColor;

        if (record.winner.equals("平局")) {
            winnerText = "平局";
            textColor = Color.parseColor("#FF9800"); // Orange
        } else if (record.winner.equals("AI")) {
            winnerText = "AI 获胜";
            textColor = Color.parseColor("#F44336"); // Red
        } else if (record.winner.equals("玩家")) {
            winnerText = "玩家 获胜";
            textColor = Color.parseColor("#4CAF50"); // Green
        } else {
            winnerText = record.winner + " 获胜";
            textColor = Color.parseColor("#4CAF50"); // Green
        }

        holder.tvWinner.setText(winnerText);
        holder.tvWinner.setTextColor(textColor);
    }

    @Override
    public int getItemCount() {
        return records != null ? records.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimestamp;
        TextView tvGameMode;
        TextView tvWinner;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvGameMode = itemView.findViewById(R.id.tv_game_mode);
            tvWinner = itemView.findViewById(R.id.tv_winner);
        }
    }
}