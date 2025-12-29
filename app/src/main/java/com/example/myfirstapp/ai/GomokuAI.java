package com.example.myfirstapp.ai;

/**
 * 高性能五子棋AI引擎
 * 协调所有AI组件：棋盘、评估器、模式识别和搜索
 * 优化版本：6步预测，3秒响应，强化防御
 */
public class GomokuAI {

    private final GomokuBoard 棋盘;
    private final MinimaxSearch 搜索引擎;
    private 难度等级 难度;
    private boolean 启用日志;
    private boolean 启用自适应评估; // 新增: 自适应评估模式开关

    // 难度设置
    public enum 难度等级 {
        简单(3, 1000),      // 3层深度，1秒
        中等(4, 2000),      // 4层深度，2秒
        困难(6, 3000),      // 6层深度，3秒（默认）
        大师(8, 5000);      // 8层深度，5秒

        private final int 最大深度;
        private final long 时间限制;

        难度等级(int 最大深度, long 时间限制) {
            this.最大深度 = 最大深度;
            this.时间限制 = 时间限制;
        }

        public int 获取最大深度() { return 最大深度; }
        public long 获取时间限制() { return 时间限制; }
    }

    public GomokuAI() {
        this(难度等级.困难);
    }

    public GomokuAI(难度等级 难度) {
        this.棋盘 = new GomokuBoard();
        this.难度 = 难度;
        this.搜索引擎 = new MinimaxSearch(难度.获取时间限制(), 难度.获取最大深度());
        this.启用日志 = false;
        this.启用自适应评估 = false; // 默认关闭自适应评估
    }

    /**
     * 用当前游戏状态初始化AI
     */
    public void initializeFromBoard(int[][] 游戏棋盘) {
        棋盘.initFromArray(游戏棋盘);

        if (启用日志) {
            System.out.println("AI已初始化，棋盘状态：");
            System.out.println(棋盘.toString());
            System.out.println("已下步数: " + 棋盘.getMoveCount());
        }
    }

    /**
     * 获取AI的最佳着法
     */
    public AI着法 getBestMove(int AI玩家) {
        if (棋盘.isFull()) {
            return new AI着法(-1, -1, "棋盘已满", null);
        }

        long 开始时间 = System.currentTimeMillis();

        // 快速威胁检测，立即响应
        AI着法 紧急着法 = 检查紧急威胁(AI玩家);
        if (紧急着法 != null) {
            if (启用日志) {
                System.out.println("检测到紧急威胁: " + 紧急着法);
            }
            return 紧急着法;
        }

        // 使用完整minimax搜索寻找最佳着法
        MinimaxSearch.搜索结果 结果 = 搜索引擎.寻找最佳下法(棋盘, AI玩家);
        long 思考时间 = System.currentTimeMillis() - 开始时间;

        if (结果.最佳着法 == null) {
            // 搜索失败时的回退着法
            return 获取回退着法();
        }

        String 分析 = 生成着法分析(结果, AI玩家, 思考时间);

        if (启用日志) {
            System.out.println("AI分析: " + 分析);
            System.out.println("搜索结果: " + 结果);
        }

        return new AI着法(结果.最佳着法.x, 结果.最佳着法.y, 分析, 结果);
    }

    /**
     * 检查紧急威胁（获胜、防守、活三威胁）
     */
    private AI着法 检查紧急威胁(int 玩家) {
        int 对手 = GomokuBoard.getOpponent(玩家);

        // 第一优先级：立即获胜
        for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
            for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                if (棋盘.isValidMove(x, y) &&
                    PatternEvaluator.是否获胜手(棋盘, x, y, 玩家)) {
                    return new AI着法(x, y, "获胜着法！", null);
                }
            }
        }

        // 第二优先级：阻止对手获胜
        for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
            for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                if (棋盘.isValidMove(x, y) &&
                    PatternEvaluator.是否获胜手(棋盘, x, y, 对手)) {
                    return new AI着法(x, y, "阻止对手获胜", null);
                }
            }
        }

        // 第三优先级：强制防守活三威胁
        if (PatternEvaluator.检查玩家活三威胁(棋盘, 对手)) {
            for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
                for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                    if (棋盘.isValidMove(x, y)) {
                        // 测试此着法是否能阻止活三威胁
                        棋盘.makeMove(x, y, 玩家);
                        boolean 阻止成功 = !PatternEvaluator.检查玩家活三威胁(棋盘, 对手);
                        棋盘.undoMove(x, y);

                        if (阻止成功) {
                            return new AI着法(x, y, "强制防守活三", null);
                        }
                    }
                }
            }
        }

        // 第四优先级：创造威胁
        for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
            for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                if (棋盘.isValidMove(x, y) &&
                    PatternEvaluator.是否创造威胁(棋盘, x, y, 玩家)) {
                    return new AI着法(x, y, "创造威胁", null);
                }
            }
        }

        return null; // 未发现紧急威胁
    }

    /**
     * 搜索失败时的回退着法
     */
    private AI着法 获取回退着法() {
        // 优先尝试天元位置
        int 天元 = GomokuBoard.BOARD_SIZE / 2;
        if (棋盘.isValidMove(天元, 天元)) {
            return new AI着法(天元, 天元, "天元着法（回退）", null);
        }

        // 寻找天元附近的有效位置
        for (int 半径 = 1; 半径 < GomokuBoard.BOARD_SIZE / 2; 半径++) {
            for (int dx = -半径; dx <= 半径; dx++) {
                for (int dy = -半径; dy <= 半径; dy++) {
                    int x = 天元 + dx;
                    int y = 天元 + dy;
                    if (棋盘.isValidMove(x, y)) {
                        return new AI着法(x, y, "天元附近（回退）", null);
                    }
                }
            }
        }

        return new AI着法(-1, -1, "无有效着法", null);
    }

    /**
     * 生成人类可读的着法分析
     */
    private String 生成着法分析(MinimaxSearch.搜索结果 结果, int 玩家, long 思考时间) {
        StringBuilder 分析 = new StringBuilder();

        // 基本着法信息
        分析.append(String.format("着法 (%d,%d) ", 结果.最佳着法.x, 结果.最佳着法.y));

        // 分数解读
        if (结果.分数 > PatternEvaluator.连五 / 2) {
            分析.append("- 必胜局面！");
        } else if (结果.分数 > PatternEvaluator.活四 / 2) {
            分析.append("- 强势威胁");
        } else if (结果.分数 > PatternEvaluator.活三 / 2) {
            分析.append("- 创造机会");
        } else if (结果.分数 > 0) {
            分析.append("- 局面有利");
        } else if (结果.分数 < -PatternEvaluator.连五 / 2) {
            分析.append("- 防守着法");
        } else {
            分析.append("- 均势局面");
        }

        // 性能信息
        分析.append(String.format(" (深度:%d 节点:%d 时间:%dms)",
                       结果.达到深度, 结果.节点评估数, 思考时间));

        return 分析.toString();
    }

    /**
     * 更新棋盘状态
     */
    public boolean makeMove(int x, int y, int 玩家) {
        return 棋盘.makeMove(x, y, 玩家);
    }

    /**
     * 清空棋盘
     */
    public void clearBoard() {
        棋盘.clear();
        搜索引擎.清空缓存();
    }

    /**
     * 设置难度等级
     */
    public void setDifficulty(难度等级 难度) {
        this.难度 = 难度;
        搜索引擎.设置最大深度(难度.获取最大深度());
        搜索引擎.设置时间限制(难度.获取时间限制());
    }

    /**
     * 启用/禁用调试日志
     */
    public void setLoggingEnabled(boolean 启用) {
        this.启用日志 = 启用;
    }

    /**
     * 启用/禁用自适应评估模式
     */
    public void setAdaptiveEvaluationEnabled(boolean 启用自适应) {
        this.启用自适应评估 = 启用自适应;

        if (启用日志) {
            System.out.println("自适应评估模式: " + (启用自适应 ? "启用" : "禁用"));
        }
    }

    /**
     * 获取自适应评估模式状态
     */
    public boolean isAdaptiveEvaluationEnabled() {
        return 启用自适应评估;
    }

    /**
     * 获取当前棋盘状态，用于调试
     */
    public String getBoardState() {
        return 棋盘.toString();
    }

    /**
     * 检查游戏是否结束
     */
    public boolean isGameOver() {
        return 棋盘.isFull() ||
               GomokuEvaluator.评估位置(棋盘, GomokuBoard.BLACK) > PatternEvaluator.连五 / 2 ||
               GomokuEvaluator.评估位置(棋盘, GomokuBoard.WHITE) > PatternEvaluator.连五 / 2;
    }

    /**
     * AI着法结果，包含分析信息
     */
    public static class AI着法 {
        public final int x, y;
        public final String 分析;
        public final MinimaxSearch.搜索结果 搜索结果;

        public AI着法(int x, int y, String 分析, MinimaxSearch.搜索结果 搜索结果) {
            this.x = x;
            this.y = y;
            this.分析 = 分析;
            this.搜索结果 = 搜索结果;
        }

        public boolean isValidMove() {
            return x >= 0 && y >= 0 && x < GomokuBoard.BOARD_SIZE && y < GomokuBoard.BOARD_SIZE;
        }

        @Override
        public String toString() {
            return String.format("AI着法(%d,%d): %s", x, y, 分析);
        }
    }

    /**
     * 获取难度等级
     */
    public 难度等级 getDifficulty() {
        return 难度;
    }

    /**
     * 获取当前棋盘，用于外部访问
     */
    public GomokuBoard getBoard() {
        return 棋盘;
    }
}