package com.example.myfirstapp;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GameRecordManager {
    private static final String PREFS_NAME = "game_records";
    private static final String KEY_TOTAL_GAMES = "total_games";
    private static final String KEY_PVP_WINS = "pvp_wins";
    private static final String KEY_PVP_DRAWS = "pvp_draws";
    private static final String KEY_AI_WINS = "ai_wins";
    private static final String KEY_AI_LOSSES = "ai_losses";
    private static final String KEY_RECENT_GAMES = "recent_games";

    private static GameRecordManager instance;
    private SharedPreferences prefs;

    private GameRecordManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static GameRecordManager getInstance(Context context) {
        if (instance == null) {
            instance = new GameRecordManager(context.getApplicationContext());
        }
        return instance;
    }

    public void addGameRecord(String gameMode, String winner) {
        SharedPreferences.Editor editor = prefs.edit();

        // Increment total games
        int totalGames = prefs.getInt(KEY_TOTAL_GAMES, 0) + 1;
        editor.putInt(KEY_TOTAL_GAMES, totalGames);

        // Update specific game mode statistics
        if (gameMode.startsWith("双人对战")) {
            if (winner.equals("平局")) {
                int draws = prefs.getInt(KEY_PVP_DRAWS, 0) + 1;
                editor.putInt(KEY_PVP_DRAWS, draws);
            } else {
                int wins = prefs.getInt(KEY_PVP_WINS, 0) + 1;
                editor.putInt(KEY_PVP_WINS, wins);
            }
        } else if (gameMode.equals("人机对战")) {
            if (winner.equals("玩家")) {
                int wins = prefs.getInt(KEY_AI_WINS, 0) + 1;
                editor.putInt(KEY_AI_WINS, wins);
            } else if (winner.equals("AI")) {
                int losses = prefs.getInt(KEY_AI_LOSSES, 0) + 1;
                editor.putInt(KEY_AI_LOSSES, losses);
            }
        }

        // Save recent game record
        String timestamp = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(new Date());
        String gameRecord = timestamp + "|" + gameMode + "|" + winner;

        String recentGames = prefs.getString(KEY_RECENT_GAMES, "");
        if (!recentGames.isEmpty()) {
            recentGames = gameRecord + ";" + recentGames;
        } else {
            recentGames = gameRecord;
        }

        // Keep only last 20 games
        String[] games = recentGames.split(";");
        if (games.length > 20) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 20; i++) {
                if (i > 0) sb.append(";");
                sb.append(games[i]);
            }
            recentGames = sb.toString();
        }

        editor.putString(KEY_RECENT_GAMES, recentGames);
        editor.apply();
    }

    public GameStatistics getStatistics() {
        return new GameStatistics(
            prefs.getInt(KEY_TOTAL_GAMES, 0),
            prefs.getInt(KEY_PVP_WINS, 0),
            prefs.getInt(KEY_PVP_DRAWS, 0),
            prefs.getInt(KEY_AI_WINS, 0),
            prefs.getInt(KEY_AI_LOSSES, 0)
        );
    }

    public List<GameRecord> getRecentGames() {
        String recentGamesStr = prefs.getString(KEY_RECENT_GAMES, "");
        List<GameRecord> gameRecords = new ArrayList<>();

        if (!recentGamesStr.isEmpty()) {
            String[] games = recentGamesStr.split(";");
            for (String game : games) {
                String[] parts = game.split("\\|");
                if (parts.length == 3) {
                    gameRecords.add(new GameRecord(parts[0], parts[1], parts[2]));
                }
            }
        }

        return gameRecords;
    }

    public void clearAllRecords() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public static class GameStatistics {
        public final int totalGames;
        public final int pvpWins;
        public final int pvpDraws;
        public final int aiWins;
        public final int aiLosses;

        public GameStatistics(int totalGames, int pvpWins, int pvpDraws,
                            int aiWins, int aiLosses) {
            this.totalGames = totalGames;
            this.pvpWins = pvpWins;
            this.pvpDraws = pvpDraws;
            this.aiWins = aiWins;
            this.aiLosses = aiLosses;
        }

        public int getPvpTotal() {
            return pvpWins + pvpDraws;
        }

        public int getAiTotal() {
            return aiWins + aiLosses;
        }

        public double getAiWinRate() {
            int total = getAiTotal();
            return total > 0 ? (double) aiWins / total : 0.0;
        }
    }

    public static class GameRecord {
        public final String timestamp;
        public final String gameMode;
        public final String winner;

        public GameRecord(String timestamp, String gameMode, String winner) {
            this.timestamp = timestamp;
            this.gameMode = gameMode;
            this.winner = winner;
        }
    }
}