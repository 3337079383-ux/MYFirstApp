package com.example.myfirstapp.ai;

/**
 * 五子棋专业棋型评估系统
 * 实现中国五子棋术语的棋型识别和战略权重分配
 */
public class PatternEvaluator {

    // 棋型权重分配 - 基于中国五子棋理论
    public static final int 连五 = 1000000;        // 五连珠（必胜）
    public static final int 活四 = 100000;         // 活四（下一手必胜）
    public static final int 冲四 = 50000;          // 冲四（四子一端被堵）
    public static final int 死四 = 5000;           // 死四（四子两端被堵）
    public static final int 活三 = 10000;          // 活三（可形成活四）
    public static final int 眠三 = 2000;           // 眠三（一端被堵的三）
    public static final int 死三 = 500;            // 死三（两端被堵）
    public static final int 活二 = 1000;           // 活二（可形成活三）
    public static final int 眠二 = 100;            // 眠二（一端被堵的二）
    public static final int 死二 = 10;             // 死二（两端被堵）
    public static final int 活一 = 50;             // 活一
    public static final int 眠一 = 5;              // 眠一

    // 特殊战术组合加分
    public static final int 双三胜 = 80000;        // 双活三必胜
    public static final int 四三胜 = 90000;        // 四三必胜
    public static final int 双四胜 = 95000;        // 双冲四必胜

    // 防御权重倍数 - 大幅加强防御
    public static final double 防御权重 = 1.5;      // 防御分数乘以1.5倍

    /**
     * 主评估函数 - 支持标准和自适应模式
     */
    public static int 评估局面(GomokuBoard 棋盘, int 当前玩家) {
        return 评估局面(棋盘, 当前玩家, false); // 默认使用标准评估
    }

    /**
     * 评估局面 - 可选择评估模式
     * @param 使用自适应 true=自适应模式, false=标准模式
     */
    public static int 评估局面(GomokuBoard 棋盘, int 当前玩家, boolean 使用自适应) {
        if (使用自适应) {
            // 使用自适应评估系统
            return AdaptiveEvaluator.自适应评估局面(棋盘, 当前玩家);
        } else {
            // 使用传统评估系统
            return 传统评估局面(棋盘, 当前玩家);
        }
    }

    /**
     * 传统评估算法 - 加强防御逻辑
     */
    private static int 传统评估局面(GomokuBoard 棋盘, int 当前玩家) {
        int 总分 = 0;
        int 对手 = GomokuBoard.getOpponent(当前玩家);

        // 计算攻击分数
        int 攻击分 = 计算棋型分数(棋盘, 当前玩家);

        // 计算防御分数（权重更高）
        int 防御分 = (int)(计算棋型分数(棋盘, 对手) * 防御权重);

        // 检查关键威胁
        int 威胁分 = 检查关键威胁(棋盘, 当前玩家, 对手);

        总分 = 攻击分 - 防御分 + 威胁分;

        return 总分;
    }

    /**
     * 计算所有棋型的分数
     */
    private static int 计算棋型分数(GomokuBoard 棋盘, int 玩家) {
        int 分数 = 0;
        int[][] 方向 = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
            for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                for (int[] 方向向量 : 方向) {
                    分数 += 评估位置棋型(棋盘, x, y, 方向向量[0], 方向向量[1], 玩家);
                }
            }
        }

        // 检查双三、四三等组合
        分数 += 检查战术组合(棋盘, 玩家);

        return 分数;
    }

    /**
     * 评估特定位置和方向的棋型
     */
    private static int 评估位置棋型(GomokuBoard 棋盘, int 起始x, int 起始y,
                                  int dx, int dy, int 玩家) {
        // 检查连五
        if (检查连五(棋盘, 起始x, 起始y, dx, dy, 玩家)) {
            return 连五;
        }

        // 检查各种四子棋型
        int 四子分数 = 检查四子棋型(棋盘, 起始x, 起始y, dx, dy, 玩家);
        if (四子分数 > 0) return 四子分数;

        // 检查各种三子棋型
        int 三子分数 = 检查三子棋型(棋盘, 起始x, 起始y, dx, dy, 玩家);
        if (三子分数 > 0) return 三子分数;

        // 检查各种二子棋型
        int 二子分数 = 检查二子棋型(棋盘, 起始x, 起始y, dx, dy, 玩家);
        if (二子分数 > 0) return 二子分数;

        // 检查单子
        return 检查单子棋型(棋盘, 起始x, 起始y, dx, dy, 玩家);
    }

    /**
     * 检查连五棋型
     */
    private static boolean 检查连五(GomokuBoard 棋盘, int x, int y, int dx, int dy, int 玩家) {
        if (!棋盘.isInBounds(x + dx * 4, y + dy * 4)) return false;

        for (int i = 0; i < 5; i++) {
            if (棋盘.getStone(x + dx * i, y + dy * i) != 玩家) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查四子相关棋型（活四、冲四、死四）
     */
    private static int 检查四子棋型(GomokuBoard 棋盘, int x, int y, int dx, int dy, int 玩家) {
        // 活四：_XXXX_
        if (检查模式(棋盘, x, y, dx, dy, new int[]{0, 玩家, 玩家, 玩家, 玩家, 0})) {
            return 活四;
        }

        // 冲四：XXXX_、_XXXX、EXXXX_、_XXXXE
        if (检查模式(棋盘, x, y, dx, dy, new int[]{玩家, 玩家, 玩家, 玩家, 0}) ||
            检查模式(棋盘, x, y, dx, dy, new int[]{0, 玩家, 玩家, 玩家, 玩家})) {
            return 冲四;
        }

        // 跳冲四：XXX_X、X_XXX
        if (检查模式(棋盘, x, y, dx, dy, new int[]{玩家, 玩家, 玩家, 0, 玩家}) ||
            检查模式(棋盘, x, y, dx, dy, new int[]{玩家, 0, 玩家, 玩家, 玩家})) {
            return 冲四;
        }

        return 0;
    }

    /**
     * 检查三子相关棋型（活三、眠三、死三）
     */
    private static int 检查三子棋型(GomokuBoard 棋盘, int x, int y, int dx, int dy, int 玩家) {
        // 活三：_XXX_（两端都空）
        if (检查模式(棋盘, x, y, dx, dy, new int[]{0, 玩家, 玩家, 玩家, 0})) {
            return 活三;
        }

        // 跳活三：_XX_X_、_X_XX_
        if (检查模式(棋盘, x, y, dx, dy, new int[]{0, 玩家, 玩家, 0, 玩家, 0}) ||
            检查模式(棋盘, x, y, dx, dy, new int[]{0, 玩家, 0, 玩家, 玩家, 0})) {
            return 活三;
        }

        // 眠三：XXX_、_XXX（一端被堵）
        if (检查模式(棋盘, x, y, dx, dy, new int[]{玩家, 玩家, 玩家, 0}) ||
            检查模式(棋盘, x, y, dx, dy, new int[]{0, 玩家, 玩家, 玩家})) {
            return 眠三;
        }

        // 跳眠三
        if (检查模式(棋盘, x, y, dx, dy, new int[]{玩家, 玩家, 0, 玩家}) ||
            检查模式(棋盘, x, y, dx, dy, new int[]{玩家, 0, 玩家, 玩家})) {
            return 眠三;
        }

        return 0;
    }

    /**
     * 检查二子棋型
     */
    private static int 检查二子棋型(GomokuBoard 棋盘, int x, int y, int dx, int dy, int 玩家) {
        // 活二：_XX_
        if (检查模式(棋盘, x, y, dx, dy, new int[]{0, 玩家, 玩家, 0})) {
            return 活二;
        }

        // 跳活二：_X_X_
        if (检查模式(棋盘, x, y, dx, dy, new int[]{0, 玩家, 0, 玩家, 0})) {
            return 活二;
        }

        // 眠二：XX_、_XX
        if (检查模式(棋盘, x, y, dx, dy, new int[]{玩家, 玩家, 0}) ||
            检查模式(棋盘, x, y, dx, dy, new int[]{0, 玩家, 玩家})) {
            return 眠二;
        }

        return 0;
    }

    /**
     * 检查单子棋型
     */
    private static int 检查单子棋型(GomokuBoard 棋盘, int x, int y, int dx, int dy, int 玩家) {
        if (检查模式(棋盘, x, y, dx, dy, new int[]{0, 玩家, 0})) {
            return 活一;
        }
        if (检查模式(棋盘, x, y, dx, dy, new int[]{玩家, 0}) ||
            检查模式(棋盘, x, y, dx, dy, new int[]{0, 玩家})) {
            return 眠一;
        }
        return 0;
    }

    /**
     * 检查战术组合（双三、四三等）
     */
    private static int 检查战术组合(GomokuBoard 棋盘, int 玩家) {
        int 组合分数 = 0;

        // 统计各种威胁数量
        int 活四数 = 统计棋型数量(棋盘, 玩家, 活四);
        int 冲四数 = 统计棋型数量(棋盘, 玩家, 冲四);
        int 活三数 = 统计棋型数量(棋盘, 玩家, 活三);

        // 双活三必胜
        if (活三数 >= 2) {
            组合分数 += 双三胜;
        }

        // 四三必胜
        if ((活四数 >= 1 || 冲四数 >= 1) && 活三数 >= 1) {
            组合分数 += 四三胜;
        }

        // 双冲四必胜
        if (冲四数 >= 2) {
            组合分数 += 双四胜;
        }

        return 组合分数;
    }

    /**
     * 检查关键威胁 - 强制防守逻辑
     */
    private static int 检查关键威胁(GomokuBoard 棋盘, int 当前玩家, int 对手) {
        int 威胁分 = 0;

        // 检查对手是否有活三威胁
        if (检查玩家活三威胁(棋盘, 对手)) {
            威胁分 -= 活三 * 3; // 强制防守活三
        }

        // 检查对手是否有冲四威胁
        if (检查玩家冲四威胁(棋盘, 对手)) {
            威胁分 -= 冲四 * 2; // 强制防守冲四
        }

        return 威胁分;
    }

    /**
     * 检查玩家是否有活三威胁（需要强制防守）
     */
    public static boolean 检查玩家活三威胁(GomokuBoard 棋盘, int 玩家) {
        // 检查是否存在活三棋型
        return 统计棋型数量(棋盘, 玩家, 活三) > 0;
    }

    /**
     * 检查玩家是否有冲四威胁
     */
    public static boolean 检查玩家冲四威胁(GomokuBoard 棋盘, int 玩家) {
        return 统计棋型数量(棋盘, 玩家, 冲四) > 0 || 统计棋型数量(棋盘, 玩家, 活四) > 0;
    }

    /**
     * 检查玩家是否有活四威胁 (专门检测活四)
     */
    public static boolean 检查玩家活四威胁(GomokuBoard 棋盘, int 玩家) {
        return 统计棋型数量(棋盘, 玩家, 活四) > 0;
    }

    /**
     * 统计特定棋型的数量
     */
    private static int 统计棋型数量(GomokuBoard 棋盘, int 玩家, int 目标分数) {
        int 数量 = 0;
        int[][] 方向 = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
            for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                for (int[] 方向向量 : 方向) {
                    int 分数 = 评估位置棋型(棋盘, x, y, 方向向量[0], 方向向量[1], 玩家);
                    if (分数 == 目标分数) {
                        数量++;
                    }
                }
            }
        }
        return 数量;
    }

    /**
     * 检查棋型模式匹配
     */
    private static boolean 检查模式(GomokuBoard 棋盘, int x, int y, int dx, int dy, int[] 模式) {
        // 检查模式是否超出边界
        if (!棋盘.isInBounds(x + dx * (模式.length - 1), y + dy * (模式.length - 1))) {
            return false;
        }

        // 检查模式匹配
        for (int i = 0; i < 模式.length; i++) {
            int 期望值 = 模式[i];
            int 实际值 = 棋盘.getStone(x + dx * i, y + dy * i);

            if (期望值 == 0) {
                // 期望为空位
                if (实际值 != GomokuBoard.EMPTY) return false;
            } else {
                // 期望为特定玩家的棋子
                if (实际值 != 期望值) return false;
            }
        }
        return true;
    }

    /**
     * 快速获胜检测
     */
    public static boolean 是否获胜手(GomokuBoard 棋盘, int x, int y, int 玩家) {
        // 临时下子
        棋盘.makeMove(x, y, 玩家);
        boolean 获胜 = 棋盘.isWinningMove(x, y, 玩家);
        棋盘.undoMove(x, y);
        return 获胜;
    }

    /**
     * 检查是否创造威胁
     */
    public static boolean 是否创造威胁(GomokuBoard 棋盘, int x, int y, int 玩家) {
        棋盘.makeMove(x, y, 玩家);

        // 检查是否形成活四或双活三等威胁
        boolean 威胁 = 检查玩家冲四威胁(棋盘, 玩家) ||
                     统计棋型数量(棋盘, 玩家, 活三) >= 2;

        棋盘.undoMove(x, y);
        return 威胁;
    }
}