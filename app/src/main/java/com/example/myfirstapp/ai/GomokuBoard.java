package com.example.myfirstapp.ai;

import java.util.Arrays;

/**
 * Professional Gomoku Board representation for AI engine
 * Handles game state, move validation, and board utilities
 */
public class GomokuBoard {
    public static final int BOARD_SIZE = 15;
    public static final int EMPTY = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;

    private final int[][] board;
    private int moveCount;
    private long zobristHash;

    // Zobrist hashing table for position caching
    private static final long[][][] zobristTable = new long[BOARD_SIZE][BOARD_SIZE][3];

    static {
        // Initialize Zobrist hash table with random values
        java.util.Random random = new java.util.Random(12345); // Fixed seed for reproducibility
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                for (int k = 0; k < 3; k++) {
                    zobristTable[i][j][k] = random.nextLong();
                }
            }
        }
    }

    public GomokuBoard() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        moveCount = 0;
        zobristHash = 0;
    }

    public GomokuBoard(GomokuBoard other) {
        this.board = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(other.board[i], 0, this.board[i], 0, BOARD_SIZE);
        }
        this.moveCount = other.moveCount;
        this.zobristHash = other.zobristHash;
    }

    /**
     * Make a move on the board
     */
    public boolean makeMove(int x, int y, int player) {
        if (!isValidMove(x, y)) {
            return false;
        }

        board[x][y] = player;
        moveCount++;

        // Update Zobrist hash
        zobristHash ^= zobristTable[x][y][player];

        return true;
    }

    /**
     * Undo a move (for search algorithms)
     */
    public void undoMove(int x, int y) {
        if (board[x][y] != EMPTY) {
            int player = board[x][y];
            board[x][y] = EMPTY;
            moveCount--;

            // Update Zobrist hash
            zobristHash ^= zobristTable[x][y][player];
        }
    }

    /**
     * Check if a move is valid
     */
    public boolean isValidMove(int x, int y) {
        return x >= 0 && x < BOARD_SIZE &&
               y >= 0 && y < BOARD_SIZE &&
               board[x][y] == EMPTY;
    }

    /**
     * Check for win condition starting from a position
     */
    public boolean isWinningMove(int x, int y, int player) {
        if (board[x][y] != player) return false;

        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            int count = 1; // Count the current stone

            // Count stones in positive direction
            for (int i = 1; i < 5; i++) {
                int nextX = x + dir[0] * i;
                int nextY = y + dir[1] * i;
                if (isInBounds(nextX, nextY) && board[nextX][nextY] == player) {
                    count++;
                } else {
                    break;
                }
            }

            // Count stones in negative direction
            for (int i = 1; i < 5; i++) {
                int nextX = x - dir[0] * i;
                int nextY = y - dir[1] * i;
                if (isInBounds(nextX, nextY) && board[nextX][nextY] == player) {
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

    /**
     * Check if coordinates are within board bounds
     */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    /**
     * Get the stone at a position
     */
    public int getStone(int x, int y) {
        if (!isInBounds(x, y)) {
            return -1; // Out of bounds
        }
        return board[x][y];
    }

    /**
     * Check if board is full
     */
    public boolean isFull() {
        return moveCount >= BOARD_SIZE * BOARD_SIZE;
    }

    /**
     * Get number of moves made
     */
    public int getMoveCount() {
        return moveCount;
    }

    /**
     * Get Zobrist hash for position caching
     */
    public long getZobristHash() {
        return zobristHash;
    }

    /**
     * Get copy of board state
     */
    public int[][] getBoardCopy() {
        int[][] copy = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, BOARD_SIZE);
        }
        return copy;
    }

    /**
     * Initialize board from existing 2D array
     */
    public void initFromArray(int[][] sourceBoard) {
        moveCount = 0;
        zobristHash = 0;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = sourceBoard[i][j];
                if (board[i][j] != EMPTY) {
                    moveCount++;
                    zobristHash ^= zobristTable[i][j][board[i][j]];
                }
            }
        }
    }

    /**
     * Clear the board
     */
    public void clear() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            Arrays.fill(board[i], EMPTY);
        }
        moveCount = 0;
        zobristHash = 0;
    }

    /**
     * Get opponent color
     */
    public static int getOpponent(int player) {
        return player == BLACK ? WHITE : BLACK;
    }

    /**
     * Convert board to string for debugging
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  ");
        for (int j = 0; j < BOARD_SIZE; j++) {
            sb.append(String.format("%2d", j));
        }
        sb.append("\n");

        for (int i = 0; i < BOARD_SIZE; i++) {
            sb.append(String.format("%2d", i));
            for (int j = 0; j < BOARD_SIZE; j++) {
                char c = board[i][j] == BLACK ? 'X' :
                        board[i][j] == WHITE ? 'O' : '.';
                sb.append(" ").append(c);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}