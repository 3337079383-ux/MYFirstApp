package com.example.myfirstapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.myfirstapp.ai.GomokuAI;

public class GobangView extends View {

    private Paint paint;
    private Paint linePaint;
    private Paint blackStonePaint;
    private Paint whiteStonePaint;
    private int gridSize = 15;
    private float cellWidth;
    private int[][] board = new int[15][15];
    private boolean isBlack = true;
    private boolean isGameOver = false;
    private String blackName = "Black";
    private String whiteName = "White";

    // AI game mode variables
    private MenuActivity.GameMode gameMode = MenuActivity.GameMode.PVP;
    private boolean isPlayerTurn = true;
    private Handler aiHandler = new Handler();
    private Random random = new Random();
    private GameActivity gameActivity = null;
    private boolean gameStarted = false;

    // Professional AI Engine
    private GomokuAI aiEngine;
    private GomokuAI.难度等级 aiDifficulty = GomokuAI.难度等级.困难;

    // Animation and sound effects
    private int lastMoveX = -1;
    private int lastMoveY = -1;
    private float animationScale = 1.0f;
    private Handler animationHandler = new Handler();

    // 音效生成器
    private ToneGenerator toneGenerator = null;

    public GobangView(Context context) {
        super(context);
        init();
    }

    public GobangView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStrokeWidth(3);
        paint.setAntiAlias(true);

        // Initialize line paint for board grid
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(1.5f);
        linePaint.setColor(Color.parseColor("#8B4513")); // Dark brown for wood texture

        // Initialize black stone paint with gradient
        blackStonePaint = new Paint();
        blackStonePaint.setAntiAlias(true);
        blackStonePaint.setStyle(Paint.Style.FILL);

        // Initialize white stone paint with gradient
        whiteStonePaint = new Paint();
        whiteStonePaint.setAntiAlias(true);
        whiteStonePaint.setStyle(Paint.Style.FILL);

        // Initialize sound effects using ToneGenerator for realistic stone sounds
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80);
        } catch (RuntimeException e) {
            // ToneGenerator can fail on some devices
            toneGenerator = null;
        }

        // Initialize professional AI engine
        initializeAI();
    }

    /**
     * Initialize the professional AI engine
     */
    private void initializeAI() {
        aiEngine = new GomokuAI(aiDifficulty);
        // Enable logging for development (can be controlled via settings)
        aiEngine.setLoggingEnabled(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        cellWidth = width / gridSize;

        // Draw wood texture background
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#DEB887")); // Burlywood
        canvas.drawRect(0, 0, width, width, backgroundPaint);

        // Draw grid lines with enhanced appearance
        for (int i = 0; i < gridSize; i++) {
            float pos = i * cellWidth + cellWidth / 2;

            // Add shadow effect to lines
            Paint shadowPaint = new Paint(linePaint);
            shadowPaint.setColor(Color.parseColor("#CD853F"));
            shadowPaint.setStrokeWidth(2f);

            // Draw shadow lines (slightly offset)
            canvas.drawLine(cellWidth / 2 + 1, pos + 1, width - cellWidth / 2 + 1, pos + 1, shadowPaint);
            canvas.drawLine(pos + 1, cellWidth / 2 + 1, pos + 1, width - cellWidth / 2 + 1, shadowPaint);

            // Draw main lines
            canvas.drawLine(cellWidth / 2, pos, width - cellWidth / 2, pos, linePaint);
            canvas.drawLine(pos, cellWidth / 2, pos, width - cellWidth / 2, linePaint);
        }

        // Draw star points (traditional go board markers)
        drawStarPoints(canvas);

        // Draw stones with enhanced 3D effect
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (board[i][j] != 0) {
                    float cx = i * cellWidth + cellWidth / 2;
                    float cy = j * cellWidth + cellWidth / 2;
                    float radius = cellWidth / 2 * 0.85f;

                    // Apply animation scale to the last placed stone
                    if (i == lastMoveX && j == lastMoveY) {
                        radius *= animationScale;
                    }

                    if (board[i][j] == 1) {
                        drawBlackStone(canvas, cx, cy, radius);
                    } else {
                        drawWhiteStone(canvas, cx, cy, radius);
                    }
                }
            }
        }
    }

    private void drawStarPoints(Canvas canvas) {
        Paint starPaint = new Paint();
        starPaint.setAntiAlias(true);
        starPaint.setStyle(Paint.Style.FILL);
        starPaint.setColor(Color.parseColor("#654321"));

        float radius = 4f;

        // Traditional 9 star points for 15x15 board
        int[] starPositions = {3, 7, 11}; // 4th, 8th, 12th lines (0-indexed)

        for (int x : starPositions) {
            for (int y : starPositions) {
                float cx = x * cellWidth + cellWidth / 2;
                float cy = y * cellWidth + cellWidth / 2;
                canvas.drawCircle(cx, cy, radius, starPaint);
            }
        }
    }

    private void drawBlackStone(Canvas canvas, float cx, float cy, float radius) {
        // Create radial gradient for 3D effect
        RadialGradient gradient = new RadialGradient(
            cx - radius * 0.3f, cy - radius * 0.3f, radius,
            Color.parseColor("#666666"), // Light gray highlight
            Color.parseColor("#111111"), // Deep black shadow
            Shader.TileMode.CLAMP
        );
        blackStonePaint.setShader(gradient);

        // Draw shadow
        Paint shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.parseColor("#44000000"));
        canvas.drawCircle(cx + 3, cy + 3, radius, shadowPaint);

        // Draw main stone
        canvas.drawCircle(cx, cy, radius, blackStonePaint);
    }

    private void drawWhiteStone(Canvas canvas, float cx, float cy, float radius) {
        // Create radial gradient for 3D effect
        RadialGradient gradient = new RadialGradient(
            cx - radius * 0.3f, cy - radius * 0.3f, radius,
            Color.parseColor("#FFFFFF"), // Pure white highlight
            Color.parseColor("#E0E0E0"), // Light gray shadow
            Shader.TileMode.CLAMP
        );
        whiteStonePaint.setShader(gradient);

        // Draw shadow
        Paint shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.parseColor("#33000000"));
        canvas.drawCircle(cx + 2, cy + 2, radius, shadowPaint);

        // Draw main stone
        canvas.drawCircle(cx, cy, radius, whiteStonePaint);

        // Add subtle border for definition
        Paint borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1f);
        borderPaint.setColor(Color.parseColor("#CCCCCC"));
        canvas.drawCircle(cx, cy, radius, borderPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGameOver || !gameStarted) return true;

        // In AI mode, only allow player moves when it's player's turn
        if (gameMode == MenuActivity.GameMode.AI && !isPlayerTurn) {
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            float touchX = event.getX();
            float touchY = event.getY();

            // Enhanced click detection with larger tolerance area
            int bestX = -1, bestY = -1;
            float minDistance = Float.MAX_VALUE;

            // Find the closest grid intersection within reasonable distance
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    float gridX = i * cellWidth + cellWidth / 2;
                    float gridY = j * cellWidth + cellWidth / 2;

                    float distance = (float) Math.sqrt(
                        Math.pow(touchX - gridX, 2) + Math.pow(touchY - gridY, 2)
                    );

                    // Increase tolerance to cellWidth * 0.4 (from previous implicit smaller tolerance)
                    if (distance < cellWidth * 0.4f && distance < minDistance && board[i][j] == 0) {
                        minDistance = distance;
                        bestX = i;
                        bestY = j;
                    }
                }
            }

            // Place stone at the best position found
            if (bestX >= 0 && bestY >= 0) {
                makeMove(bestX, bestY);
            }
        }
        return true;
    }

    private void makeMove(int x, int y) {
        board[x][y] = isBlack ? 1 : 2;

        // Sync move with AI engine for consistent board state
        if (aiEngine != null) {
            aiEngine.makeMove(x, y, isBlack ? 1 : 2);
        }

        // Set last move position for animation
        lastMoveX = x;
        lastMoveY = y;

        // Play move sound
        playMoveSound();

        // Start animation
        startPlaceAnimation();

        if (checkWin(x, y)) {
            isGameOver = true;
            String winner = gameActivity != null ?
                gameActivity.getPlayerName(isBlack) + " 获胜!" :
                (isBlack ? (blackName + " wins!") : (whiteName + " wins!"));

            // Play win sound
            playWinSound();

            // Save game result
            saveGameResult(gameActivity != null ? gameActivity.getPlayerName(isBlack) : (isBlack ? blackName : whiteName));

            if (gameActivity != null) {
                gameActivity.updateGameStatus(winner);
                gameActivity.onGameEnd();
            }
        } else if (isBoardFull()) {
            // Save draw result
            saveGameResult("平局");

            if (gameActivity != null) {
                gameActivity.updateGameStatus("和棋!");
                gameActivity.onGameEnd();
            }
            restartGame(false);
        } else {
            isBlack = !isBlack;

            // Update status to show whose turn it is
            updateTurnStatus();

            // If it's AI mode and now it's AI's turn, make AI move
            // In AI mode: Player is always black (first), AI is always white (second)
            if (gameMode == MenuActivity.GameMode.AI && !isGameOver && !isBlack) {
                isPlayerTurn = false;
                if (gameActivity != null) {
                    gameActivity.updateGameStatus("AI 思考中...");
                }
                scheduleAIMove();
            } else if (gameMode == MenuActivity.GameMode.AI) {
                isPlayerTurn = true;
            }
        }
        invalidate();
    }

    // Animation and sound methods
    private void startPlaceAnimation() {
        // Check if animations are enabled in settings
        if (!SettingsActivity.isAnimationEnabled(getContext())) {
            return;
        }

        // Reset animation scale
        animationScale = 0.5f;

        // Create faster scale animation from 0.5 to 1.15 to 1.0
        animationHandler.post(new Runnable() {
            private int animationStep = 0;
            private final int maxSteps = 8; // Reduced from 12 to 8 for faster animation

            @Override
            public void run() {
                if (animationStep < maxSteps) {
                    if (animationStep < 5) {
                        // Scale up phase (faster)
                        animationScale = 0.5f + (0.65f * animationStep / 5f);
                    } else {
                        // Scale down phase (smoother)
                        animationScale = 1.15f - (0.15f * (animationStep - 5) / 3f);
                    }

                    animationStep++;
                    invalidate();
                    animationHandler.postDelayed(this, 30); // Reduced from 50ms to 30ms per frame
                } else {
                    animationScale = 1.0f;
                    invalidate();
                }
            }
        });
    }

    private void playMoveSound() {
        // Check if sound is enabled in settings
        if (!SettingsActivity.isSoundEnabled(getContext())) {
            return;
        }

        try {
            if (toneGenerator != null) {
                // Create a realistic stone-on-board sound using multiple tones
                // Low frequency for the "thud" of stone hitting wood
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Main impact sound - short, crisp
                            toneGenerator.startTone(ToneGenerator.TONE_DTMF_2, 60);
                            Thread.sleep(40);
                            // Subtle resonance - lower pitch, very brief
                            toneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 20);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            // Ignore sound errors
        }
    }

    private void playWinSound() {
        // Check if sound is enabled in settings
        if (!SettingsActivity.isSoundEnabled(getContext())) {
            return;
        }

        try {
            if (toneGenerator != null) {
                // Victory sound - ascending musical pattern
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            toneGenerator.startTone(ToneGenerator.TONE_DTMF_4, 120);
                            Thread.sleep(100);
                            toneGenerator.startTone(ToneGenerator.TONE_DTMF_6, 120);
                            Thread.sleep(100);
                            toneGenerator.startTone(ToneGenerator.TONE_DTMF_8, 150);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            // Ignore sound errors
        }
    }

    private void saveGameResult(String winner) {
        if (getContext() != null) {
            String gameModeForRecord;
            String winnerForRecord = winner;

            // Create more descriptive game mode and winner information
            if (gameMode == MenuActivity.GameMode.AI) {
                gameModeForRecord = "人机对战";
            } else {
                // For PvP mode, include player names in the game mode
                if (gameActivity != null) {
                    String blackPlayerName = gameActivity.getPlayerName(true);
                    String whitePlayerName = gameActivity.getPlayerName(false);
                    gameModeForRecord = "双人对战 (" + blackPlayerName + " vs " + whitePlayerName + ")";

                    // For PvP, keep the winner as the actual player name instead of "玩家1", "玩家2"
                    // The winner is already the correct player name from the calling code
                } else {
                    gameModeForRecord = "双人对战";
                }
            }

            GameRecordManager.getInstance(getContext()).addGameRecord(gameModeForRecord, winnerForRecord);
        }
    }

    private void updateTurnStatus() {
        if (gameActivity != null && !isGameOver) {
            String currentPlayer = gameActivity.getPlayerName(isBlack);
            gameActivity.updateGameStatus(currentPlayer + " 的回合");
        }
    }

    private void scheduleAIMove() {
        // Reduced delay for faster AI response while maintaining natural feel
        int delay = 300;
        aiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                makeAIMove();
            }
        }, delay);
    }

    private void makeAIMove() {
        if (isGameOver) return;

        // 同步棋盘状态到AI引擎
        aiEngine.initializeFromBoard(board);

        // 获取AI最佳着法（AI执白棋 = 2）
        GomokuAI.AI着法 ai着法 = aiEngine.getBestMove(2);

        if (ai着法.isValidMove()) {
            // 更新AI引擎棋盘状态
            aiEngine.makeMove(ai着法.x, ai着法.y, 2);

            // 在UI棋盘上下子
            makeMove(ai着法.x, ai着法.y);

            // 显示AI思考过程（可选）
            if (gameActivity != null && ai着法.分析 != null) {
                // 将AI分析信息显示在状态栏
                gameActivity.updateGameStatus("AI: " + ai着法.分析);
            }

            isPlayerTurn = true;
        } else {
            // 如果专业AI失败，使用简单AI作为后备
            int[] 后备着法 = findBestMove();
            if (后备着法 != null) {
                makeMove(后备着法[0], 后备着法[1]);
                isPlayerTurn = true;
            }
        }
    }

    private boolean checkWin(int x, int y) {
        int color = board[x][y];
        if (color == 0) return false; // No stone at this position

        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            int count = 1; // Count the current stone

            // Count stones in positive direction
            for (int i = 1; i < 5; i++) {
                int nextX = x + dir[0] * i;
                int nextY = y + dir[1] * i;
                if (isValid(nextX, nextY) && board[nextX][nextY] == color) {
                    count++;
                } else {
                    break;
                }
            }

            // Count stones in negative direction
            for (int i = 1; i < 5; i++) {
                int nextX = x - dir[0] * i;
                int nextY = y - dir[1] * i;
                if (isValid(nextX, nextY) && board[nextX][nextY] == color) {
                    count++;
                } else {
                    break;
                }
            }

            if (count >= 5) {
                return true;
            }
        }
        return false;
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < gridSize && y >= 0 && y < gridSize;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public void restartGame(boolean showToast) {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                board[i][j] = 0;
            }
        }
        isBlack = true;
        isGameOver = false;
        isPlayerTurn = true;
        gameStarted = true;

        // Reset professional AI engine
        if (aiEngine != null) {
            aiEngine.clearBoard();
        }

        // Reset animation state
        lastMoveX = -1;
        lastMoveY = -1;
        animationScale = 1.0f;

        invalidate();

        // Update status for first player's turn
        if (gameActivity != null) {
            updateTurnStatus();
        }

        if (showToast) {
            Toast.makeText(getContext(), "Game reset.", Toast.LENGTH_SHORT).show();
        }
    }

    public void restartGame() {
        restartGame(true);
    }

    public void setPlayerNames(String black, String white) {
        blackName = sanitizeName(black, "Black");
        whiteName = sanitizeName(white, "White");
    }

    public void setGameMode(MenuActivity.GameMode mode) {
        this.gameMode = mode;
        if (mode == MenuActivity.GameMode.AI) {
            blackName = "Player";
            whiteName = "AI";
        }
    }

    public void setAIDifficulty(GomokuAI.难度等级 难度) {
        this.aiDifficulty = 难度;
        if (aiEngine != null) {
            aiEngine.setDifficulty(难度);
        }
    }

    public void setGameActivity(GameActivity activity) {
        this.gameActivity = activity;
    }

    private String sanitizeName(String name, String fallback) {
        if (name == null) return fallback;
        String trimmed = name.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    // AI Methods - 简化版本
    private int[] findBestMove() {
        return findSimpleAIMove();
    }

    // 简化AI移动 - 只使用基本策略
    private int[] findSimpleAIMove() {
        int aiColor = 2;      // AI is always white
        int playerColor = 1;  // Player is always black

        // Priority 1: 立即获胜
        int[] winMove = findImmediateWinMove(aiColor);
        if (winMove != null) return winMove;

        // Priority 2: 阻止对手立即获胜
        int[] blockMove = findImmediateWinMove(playerColor);
        if (blockMove != null) return blockMove;

        // Priority 3: 随机移动
        return findRandomMove();
    }

    // 只检查立即获胜的移动（5连）
    private int[] findImmediateWinMove(int color) {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (board[i][j] == 0) {
                    board[i][j] = color;
                    if (checkWin(i, j)) {
                        board[i][j] = 0;
                        return new int[]{i, j};
                    }
                    board[i][j] = 0;
                }
            }
        }
        return null;
    }

    // 简单随机移动
    private int[] findRandomMove() {
        List<int[]> availableMoves = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (board[i][j] == 0) {
                    availableMoves.add(new int[]{i, j});
                }
            }
        }

        if (!availableMoves.isEmpty()) {
            return availableMoves.get(random.nextInt(availableMoves.size()));
        }
        return null;
    }

    // 清理音效资源
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
}