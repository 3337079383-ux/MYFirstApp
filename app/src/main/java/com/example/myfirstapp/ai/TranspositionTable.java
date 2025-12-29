package com.example.myfirstapp.ai;

/**
 * Transposition Table for caching evaluated positions
 * Uses Zobrist hashing for position identification
 */
public class TranspositionTable {

    private static final int DEFAULT_SIZE = 1 << 20; // 1M entries
    private static final int MASK = DEFAULT_SIZE - 1;

    private final Entry[] table;
    private int hits = 0;
    private int misses = 0;

    public TranspositionTable() {
        this(DEFAULT_SIZE);
    }

    public TranspositionTable(int size) {
        // Ensure size is power of 2
        int actualSize = 1;
        while (actualSize < size) {
            actualSize <<= 1;
        }
        table = new Entry[actualSize];
    }

    /**
     * Store evaluation result
     */
    public void store(long zobristHash, int depth, int score, int flag, GomokuEvaluator.Move bestMove) {
        int index = getIndex(zobristHash);

        Entry entry = table[index];
        if (entry == null || entry.depth <= depth) {
            table[index] = new Entry(zobristHash, depth, score, flag, bestMove);
        }
    }

    /**
     * Lookup evaluation result
     */
    public Entry lookup(long zobristHash) {
        int index = getIndex(zobristHash);
        Entry entry = table[index];

        if (entry != null && entry.zobristHash == zobristHash) {
            hits++;
            return entry;
        }

        misses++;
        return null;
    }

    /**
     * Clear transposition table
     */
    public void clear() {
        for (int i = 0; i < table.length; i++) {
            table[i] = null;
        }
        hits = 0;
        misses = 0;
    }

    /**
     * Get hit rate for performance monitoring
     */
    public double getHitRate() {
        int total = hits + misses;
        return total > 0 ? (double) hits / total : 0.0;
    }

    private int getIndex(long zobristHash) {
        return (int) (zobristHash & (table.length - 1));
    }

    /**
     * Transposition table entry
     */
    public static class Entry {
        public final long zobristHash;
        public final int depth;
        public final int score;
        public final int flag;
        public final GomokuEvaluator.Move bestMove;

        public Entry(long zobristHash, int depth, int score, int flag, GomokuEvaluator.Move bestMove) {
            this.zobristHash = zobristHash;
            this.depth = depth;
            this.score = score;
            this.flag = flag;
            this.bestMove = bestMove;
        }
    }

    // Flag constants for entry types
    public static final int EXACT = 0;      // Exact score
    public static final int LOWER_BOUND = 1; // Alpha cutoff (fail-high)
    public static final int UPPER_BOUND = 2; // Beta cutoff (fail-low)
}