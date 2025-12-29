package com.example.myfirstapp.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 五子棋启发式评估器
 * 提供局面评估、着法生成和排序功能
 */
public class GomokuEvaluator {

    private static final int 无穷大 = Integer.MAX_VALUE;
    private static final int 胜利门槛 = PatternEvaluator.连五 / 2;

    /**
     * 从当前玩家角度评估棋盘位置
     */
    public static int 评估位置(GomokuBoard 棋盘, int 玩家) {
        // 优先检查终局位置
        if (是否游戏结束(棋盘, 玩家)) {
            return 无穷大 - 棋盘.getMoveCount(); // 更快获胜更好
        }

        if (是否游戏结束(棋盘, GomokuBoard.getOpponent(玩家))) {
            return -无穷大 + 棋盘.getMoveCount(); // 延缓失败更好
        }

        // 使用模式评估器进行非终局位置评估
        return PatternEvaluator.评估局面(棋盘, 玩家);
    }

    /**
     * 生成并排序候选着法，用于搜索优化
     */
    public static List<Move> generateOrderedMoves(GomokuBoard 棋盘, int 玩家, int 深度) {
        List<Move> 着法列表 = new ArrayList<>();

        // 开局启发式 - 天元有利
        if (棋盘.getMoveCount() == 0) {
            int 天元 = GomokuBoard.BOARD_SIZE / 2;
            着法列表.add(new Move(天元, 天元, PatternEvaluator.活一));
            return 着法列表;
        }

        // 生成有子周围的候选位置
        boolean[][] 已考虑 = new boolean[GomokuBoard.BOARD_SIZE][GomokuBoard.BOARD_SIZE];

        for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
            for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                if (棋盘.getStone(x, y) != GomokuBoard.EMPTY) {
                    // 在现有棋子周围添加空位
                    添加周围着法(棋盘, x, y, 着法列表, 已考虑);
                }
            }
        }

        // 评估并给每个着法打分 - 增强版优先级系统 + 禁手检查
        int 对手 = GomokuBoard.getOpponent(玩家);

        for (Move 着法 : 着法列表) {
            // 禁手检查 - 如果是黑棋的禁手，设置极低分数
            if (ForbiddenMoveDetector.是否禁手(棋盘, 着法.x, 着法.y, 玩家)) {
                着法.score = -无穷大; // 禁手着法绝对不能选择
                continue;
            }

            int 分数 = 计算增强优先级分数(棋盘, 着法.x, 着法.y, 玩家, 对手, 深度);
            着法.score = 分数;
        }

        // 按分数降序排列
        Collections.sort(着法列表, new Comparator<Move>() {
            @Override
            public int compare(Move a, Move b) {
                return Integer.compare(b.score, a.score);
            }
        });

        // 限制着法数量以控制分支因子
        int 最大着法数 = Math.max(10, 30 - 深度 * 2);
        if (着法列表.size() > 最大着法数) {
            着法列表 = 着法列表.subList(0, 最大着法数);
        }

        return 着法列表;
    }

    /**
     * 在现有棋子周围添加候选着法
     */
    private static void 添加周围着法(GomokuBoard 棋盘, int 中心x, int 中心y,
                                   List<Move> 着法列表, boolean[][] 已考虑) {
        // 搜索半径
        int 半径 = 2;

        for (int dx = -半径; dx <= 半径; dx++) {
            for (int dy = -半径; dy <= 半径; dy++) {
                int x = 中心x + dx;
                int y = 中心y + dy;

                if (棋盘.isInBounds(x, y) &&
                    棋盘.getStone(x, y) == GomokuBoard.EMPTY &&
                    !已考虑[x][y]) {

                    着法列表.add(new Move(x, y, 0));
                    已考虑[x][y] = true;
                }
            }
        }
    }

    /**
     * 计算靠近中心和现有棋子的位置奖励
     */
    private static int 获取位置奖励(int x, int y) {
        // 中心位置奖励
        int 中心 = GomokuBoard.BOARD_SIZE / 2;
        int 距离中心 = Math.abs(x - 中心) + Math.abs(y - 中心);
        int 中心奖励 = Math.max(0, 15 - 距离中心);

        return 中心奖励;
    }

    /**
     * 检查游戏是否结束（有人获胜）
     */
    private static boolean 是否游戏结束(GomokuBoard 棋盘, int 玩家) {
        // 快速检查 - 寻找任何五连珠
        for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
            for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                if (棋盘.getStone(x, y) == 玩家 &&
                    棋盘.isWinningMove(x, y, 玩家)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查是否为残局阶段（棋子较多）
     */
    public static boolean 是否残局(GomokuBoard 棋盘) {
        return 棋盘.getMoveCount() > GomokuBoard.BOARD_SIZE * GomokuBoard.BOARD_SIZE * 0.7;
    }

    /**
     * 检查是否为开局阶段
     */
    public static boolean 是否开局(GomokuBoard 棋盘) {
        return 棋盘.getMoveCount() < 6;
    }

    /**
     * 检查是否存在需要立即响应的关键威胁
     */
    public static boolean 有关键威胁(GomokuBoard 棋盘, int 玩家) {
        int 对手 = GomokuBoard.getOpponent(玩家);

        // 检查对手是否可以一步获胜
        for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
            for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                if (棋盘.isValidMove(x, y) &&
                    PatternEvaluator.是否获胜手(棋盘, x, y, 对手)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 简单着法表示，包含评估分数
     */
    public static class Move {
        public final int x, y;
        public int score;

        public Move(int x, int y, int score) {
            this.x = x;
            this.y = y;
            this.score = score;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ") 分数:" + score;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Move)) return false;
            Move other = (Move) obj;
            return x == other.x && y == other.y;
        }

        @Override
        public int hashCode() {
            return x * 31 + y;
        }
    }

    /**
     * 增强版优先级计算 - 多层次威胁分析
     */
    private static int 计算增强优先级分数(GomokuBoard 棋盘, int x, int y, int 玩家, int 对手, int 深度) {
        int 基础分数 = 0;

        // 第1优先级: 立即获胜 (绝对最高优先级) - Win-First Logic 修复
        if (PatternEvaluator.是否获胜手(棋盘, x, y, 玩家)) {
            // 确保获胜分数绝对是最高的，远超任何防御分数
            return 无穷大 - 深度; // 浅层获胜更优，但确保绝对优先级
        }

        // 第2优先级: 阻止对手立即获胜 (防御优先级)
        if (PatternEvaluator.是否获胜手(棋盘, x, y, 对手)) {
            return 无穷大 / 2 - 深度 * 100; // 必须防御，但低于己方获胜
        }

        // 模拟落子进行威胁分析
        棋盘.makeMove(x, y, 玩家);

        try {
            // 第3优先级: 创造活四威胁 (强攻击)
            if (PatternEvaluator.检查玩家活四威胁(棋盘, 玩家)) {
                基础分数 += PatternEvaluator.活四 * 2;
            }

            // 第4优先级: 创造冲四威胁 (中攻击)
            if (PatternEvaluator.检查玩家冲四威胁(棋盘, 玩家)) {
                基础分数 += PatternEvaluator.冲四;
            }

            // 第5优先级: 创造活三威胁 (威胁建立)
            if (PatternEvaluator.检查玩家活三威胁(棋盘, 玩家)) {
                基础分数 += PatternEvaluator.活三;
            }

            // 第6优先级: 整体局面评估
            int 局面分数 = PatternEvaluator.评估局面(棋盘, 玩家);
            基础分数 += 局面分数 / 2; // 降权处理

            // 第7优先级: 双重威胁检测 (组合攻击)
            int 威胁数量 = 统计威胁数量(棋盘, 玩家);
            if (威胁数量 >= 2) {
                基础分数 += PatternEvaluator.活四 / 2 * 威胁数量; // 双三、三四等组合
            }

            // 第8优先级: 破坏对手威胁 (防御加分)
            棋盘.undoMove(x, y);
            棋盘.makeMove(x, y, 对手);

            boolean 破坏对手威胁 = false;
            if (PatternEvaluator.检查玩家活三威胁(棋盘, 对手) ||
                PatternEvaluator.检查玩家冲四威胁(棋盘, 对手)) {
                破坏对手威胁 = true;
            }

            棋盘.undoMove(x, y);
            棋盘.makeMove(x, y, 玩家); // 恢复原状态

            if (破坏对手威胁) {
                基础分数 += PatternEvaluator.活三 / 3; // 破坏威胁奖励
            }

            // 第8.5优先级: 利用对手禁手 (仅对黑棋对手有效)
            if (对手 == GomokuBoard.BLACK) {
                int 禁手战术分数 = 评估禁手战术价值(棋盘, x, y, 玩家, 对手);
                基础分数 += 禁手战术分数;
            }

        } finally {
            棋盘.undoMove(x, y);
        }

        // 第9优先级: 位置价值 (棋盘控制)
        基础分数 += 计算位置战略价值(x, y, 深度);

        // 第10优先级: 深度调整 (搜索效率)
        基础分数 -= 深度 * 10; // 浅层优先考虑

        return 基础分数;
    }

    /**
     * 统计指定玩家的威胁数量
     */
    private static int 统计威胁数量(GomokuBoard 棋盘, int 玩家) {
        int 威胁计数 = 0;

        // 简化威胁统计 - 检查关键模式
        if (PatternEvaluator.检查玩家活三威胁(棋盘, 玩家)) 威胁计数++;
        if (PatternEvaluator.检查玩家冲四威胁(棋盘, 玩家)) 威胁计数++;

        // 可扩展: 添加更多威胁类型检测

        return 威胁计数;
    }

    /**
     * 计算位置的战略价值 - 考虑控制中心、边角劣势等
     */
    private static int 计算位置战略价值(int x, int y, int 深度) {
        int 中心 = GomokuBoard.BOARD_SIZE / 2;
        int 价值 = 0;

        // 中心控制价值 (开局重要)
        int 距离中心 = Math.abs(x - 中心) + Math.abs(y - 中心);
        价值 += Math.max(0, 20 - 距离中心 * 2);

        // 边角惩罚 (避免过于靠边)
        if (x <= 1 || x >= GomokuBoard.BOARD_SIZE - 2 ||
            y <= 1 || y >= GomokuBoard.BOARD_SIZE - 2) {
            价值 -= 15;
        }

        // 深度调整 - 深层搜索时位置价值降低
        价值 = 价值 * (10 - Math.min(深度, 8)) / 10;

        return 价值;
    }

    /**
     * 评估禁手战术价值 - 检测能否利用对手禁手获得优势
     * 仅对黑棋对手有效，白棋无禁手规则
     */
    private static int 评估禁手战术价值(GomokuBoard 棋盘, int x, int y, int 玩家, int 对手) {
        int 战术分数 = 0;

        // 检查周围8个方向是否能创造对手禁手局面
        int[][] 方向 = {{-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};

        for (int[] 方向向量 : 方向) {
            int 邻位x = x + 方向向量[0];
            int 邻位y = y + 方向向量[1];

            if (棋盘.isInBounds(邻位x, 邻位y) && 棋盘.getStone(邻位x, 邻位y) == GomokuBoard.EMPTY) {
                // 检查对手在该邻位落子是否会形成禁手
                if (ForbiddenMoveDetector.是否禁手(棋盘, 邻位x, 邻位y, 对手)) {
                    String 禁手类型 = ForbiddenMoveDetector.获取禁手类型(棋盘, 邻位x, 邻位y, 对手);

                    // 根据禁手类型给予不同战术分数
                    switch (禁手类型) {
                        case "三三禁手":
                            战术分数 += PatternEvaluator.活三 / 5; // 适中奖励
                            break;
                        case "四四禁手":
                            战术分数 += PatternEvaluator.冲四 / 8; // 较高奖励
                            break;
                        case "长连禁手":
                            战术分数 += PatternEvaluator.活四 / 10; // 最高奖励
                            break;
                    }
                }
            }
        }

        // 特殊战术：双重威胁+禁手组合
        // 如果本手既能创造威胁，又能限制对手因禁手无法有效反击
        棋盘.undoMove(x, y);
        棋盘.makeMove(x, y, 玩家);

        boolean 我方有威胁 = PatternEvaluator.检查玩家活三威胁(棋盘, 玩家) ||
                         PatternEvaluator.检查玩家冲四威胁(棋盘, 玩家);

        if (我方有威胁 && 战术分数 > 0) {
            战术分数 = (int)(战术分数 * 1.5); // 组合战术奖励
        }

        棋盘.undoMove(x, y);
        棋盘.makeMove(x, y, 玩家); // 恢复状态

        return 战术分数;
    }
}