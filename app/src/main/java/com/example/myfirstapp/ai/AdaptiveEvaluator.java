package com.example.myfirstapp.ai;

/**
 * 自适应评估器 - 非线性权重系统和攻守态势感知
 * 根据局面特征动态调整评估权重，实现更加智能的棋力判断
 *
 * 核心特性:
 * 1. 非线性权重函数 - 根据威胁数量指数增长
 * 2. 攻守态势识别 - 自动切换攻击/防御模式
 * 3. 局面阶段感知 - 开局/中局/残局不同策略
 * 4. 威胁紧急度评估 - 多层次威胁优先级
 */
public class AdaptiveEvaluator {

    // 动态权重系数
    private static final double 开局中心权重 = 1.5;
    private static final double 中局威胁权重 = 2.0;
    private static final double 残局精确权重 = 3.0;

    // 攻守态势阈值
    private static final int 攻击模式阈值 = PatternEvaluator.活三;
    private static final int 防御模式阈值 = PatternEvaluator.冲四;
    private static final int 紧急防御阈值 = PatternEvaluator.活四;

    /**
     * 自适应局面评估 - 主入口函数
     */
    public static int 自适应评估局面(GomokuBoard 棋盘, int 当前玩家) {
        // 分析当前局面特征
        局面特征 特征 = 分析局面特征(棋盘, 当前玩家);

        // 计算基础评估分数
        int 基础分数 = PatternEvaluator.评估局面(棋盘, 当前玩家);

        // 应用自适应权重调整
        int 调整后分数 = 应用自适应权重(基础分数, 特征, 棋盘, 当前玩家);

        // 攻守态势修正
        调整后分数 = 应用攻守态势修正(调整后分数, 特征, 棋盘, 当前玩家);

        return 调整后分数;
    }

    /**
     * 分析当前局面的特征
     */
    private static 局面特征 分析局面特征(GomokuBoard 棋盘, int 当前玩家) {
        int 对手 = GomokuBoard.getOpponent(当前玩家);
        int 步数 = 棋盘.getMoveCount();

        // 局面阶段判断
        游戏阶段 阶段;
        if (步数 < 8) {
            阶段 = 游戏阶段.开局;
        } else if (步数 < 30) {
            阶段 = 游戏阶段.中局;
        } else {
            阶段 = 游戏阶段.残局;
        }

        // 威胁统计
        int 己方活三 = 统计威胁数量(棋盘, 当前玩家, PatternEvaluator.活三);
        int 己方冲四 = 统计威胁数量(棋盘, 当前玩家, PatternEvaluator.冲四);
        int 己方活四 = 统计威胁数量(棋盘, 当前玩家, PatternEvaluator.活四);

        int 对手活三 = 统计威胁数量(棋盘, 对手, PatternEvaluator.活三);
        int 对手冲四 = 统计威胁数量(棋盘, 对手, PatternEvaluator.冲四);
        int 对手活四 = 统计威胁数量(棋盘, 对手, PatternEvaluator.活四);

        // 态势判断
        攻守态势 态势 = 判断攻守态势(己方活三, 己方冲四, 己方活四, 对手活三, 对手冲四, 对手活四);

        // 紧急度计算
        威胁紧急度 紧急度 = 计算威胁紧急度(对手活三, 对手冲四, 对手活四);

        return new 局面特征(阶段, 态势, 紧急度, 己方活三, 己方冲四, 己方活四, 对手活三, 对手冲四, 对手活四);
    }

    /**
     * 应用自适应权重调整
     */
    private static int 应用自适应权重(int 基础分数, 局面特征 特征, GomokuBoard 棋盘, int 当前玩家) {
        double 权重乘数 = 1.0;

        // 游戏阶段权重调整
        switch (特征.阶段) {
            case 开局:
                // 开局重视中心控制和基础发展
                权重乘数 *= 开局中心权重;
                基础分数 += 计算开局位置奖励(棋盘, 当前玩家);
                break;

            case 中局:
                // 中局重视威胁创造和战术组合
                权重乘数 *= 中局威胁权重;
                基础分数 += 计算中局战术奖励(特征, 当前玩家);
                break;

            case 残局:
                // 残局重视精确计算和关键防御
                权重乘数 *= 残局精确权重;
                基础分数 += 计算残局精确奖励(特征, 当前玩家);
                break;
        }

        // 非线性威胁权重 - 威胁数量的指数增长效应
        基础分数 += 计算非线性威胁奖励(特征, 当前玩家);

        return (int)(基础分数 * 权重乘数);
    }

    /**
     * 计算非线性威胁奖励 - 威胁组合的指数增长价值
     */
    private static int 计算非线性威胁奖励(局面特征 特征, int 当前玩家) {
        int 奖励 = 0;

        // 己方威胁组合奖励 (非线性增长)
        if (特征.己方活三 >= 2) {
            // 双活三 - 指数奖励
            奖励 += PatternEvaluator.活三 * Math.pow(特征.己方活三, 1.5);
        }

        if (特征.己方冲四 >= 1 && 特征.己方活三 >= 1) {
            // 四三组合 - 极高价值
            奖励 += PatternEvaluator.冲四 * 2;
        }

        if (特征.己方活四 >= 1) {
            // 活四威胁 - 近乎必胜
            奖励 += PatternEvaluator.活四 * 3;
        }

        // 对手威胁防御惩罚 (非线性惩罚)
        if (特征.对手活三 >= 2) {
            奖励 -= PatternEvaluator.活三 * Math.pow(特征.对手活三, 1.8); // 更严重的惩罚
        }

        if (特征.对手冲四 >= 1) {
            奖励 -= PatternEvaluator.冲四 * 2;
        }

        if (特征.对手活四 >= 1) {
            奖励 -= PatternEvaluator.活四 * 5; // 极重惩罚
        }

        return 奖励;
    }

    /**
     * 应用攻守态势修正
     */
    private static int 应用攻守态势修正(int 分数, 局面特征 特征, GomokuBoard 棋盘, int 当前玩家) {
        switch (特征.态势) {
            case 强攻击:
                // 强攻击时，提升攻击权重
                分数 = (int)(分数 * 1.3);
                break;

            case 均势攻击:
                // 轻微攻击倾向
                分数 = (int)(分数 * 1.1);
                break;

            case 均势:
                // 保持平衡
                break;

            case 均势防御:
                // 轻微防御倾向，降低冒险性攻击
                分数 = (int)(分数 * 0.9);
                break;

            case 强防御:
                // 强防御时，大幅降低攻击性，专注阻挡
                分数 = (int)(分数 * 0.7);
                break;

            case 紧急防御:
                // 生死关头，仅考虑防御
                分数 = 重新计算紧急防御分数(棋盘, 当前玩家);
                break;
        }

        return 分数;
    }

    /**
     * 重新计算紧急防御模式下的分数
     */
    private static int 重新计算紧急防御分数(GomokuBoard 棋盘, int 当前玩家) {
        int 对手 = GomokuBoard.getOpponent(当前玩家);
        int 最高防御价值 = -PatternEvaluator.连五;

        // 寻找最关键的防御点
        for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
            for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                if (棋盘.isValidMove(x, y)) {
                    // 检查是否能阻止对手获胜
                    if (PatternEvaluator.是否获胜手(棋盘, x, y, 对手)) {
                        return PatternEvaluator.连五 / 2; // 必须防御
                    }

                    // 检查防御价值
                    棋盘.makeMove(x, y, 当前玩家);
                    int 防御后分数 = -PatternEvaluator.评估局面(棋盘, 对手);
                    棋盘.undoMove(x, y);

                    最高防御价值 = Math.max(最高防御价值, 防御后分数);
                }
            }
        }

        return 最高防御价值;
    }

    // ================== 辅助计算方法 ==================

    private static int 计算开局位置奖励(GomokuBoard 棋盘, int 当前玩家) {
        // 开局重视中心区域控制
        int 奖励 = 0;
        int 中心 = GomokuBoard.BOARD_SIZE / 2;

        for (int x = 中心-2; x <= 中心+2; x++) {
            for (int y = 中心-2; y <= 中心+2; y++) {
                if (棋盘.isInBounds(x, y) && 棋盘.getStone(x, y) == 当前玩家) {
                    int 距离中心 = Math.abs(x - 中心) + Math.abs(y - 中心);
                    奖励 += Math.max(0, 50 - 距离中心 * 10);
                }
            }
        }
        return 奖励;
    }

    private static int 计算中局战术奖励(局面特征 特征, int 当前玩家) {
        // 中局重视战术组合
        int 奖励 = 0;

        if (特征.己方活三 >= 1 && 特征.己方冲四 >= 1) {
            奖励 += PatternEvaluator.活四; // 四三组合
        }

        if (特征.己方活三 >= 2) {
            奖励 += PatternEvaluator.活三; // 双三威胁
        }

        return 奖励;
    }

    private static int 计算残局精确奖励(局面特征 特征, int 当前玩家) {
        // 残局重视精确防御和必胜攻击
        int 奖励 = 0;

        if (特征.紧急度 == 威胁紧急度.极高) {
            奖励 -= PatternEvaluator.连五 / 4; // 严重警告
        }

        return 奖励;
    }

    private static 攻守态势 判断攻守态势(int 己方活三, int 己方冲四, int 己方活四,
                                      int 对手活三, int 对手冲四, int 对手活四) {
        // 紧急防御判断
        if (对手活四 > 0 || (对手冲四 >= 2)) {
            return 攻守态势.紧急防御;
        }

        // 强防御判断
        if (对手冲四 > 0 || 对手活三 >= 2) {
            return 攻守态势.强防御;
        }

        // 强攻击判断
        if (己方活四 > 0 || (己方冲四 >= 1 && 己方活三 >= 1)) {
            return 攻守态势.强攻击;
        }

        // 计算攻守平衡
        int 己方威胁值 = 己方活三 * 3 + 己方冲四 * 10 + 己方活四 * 50;
        int 对手威胁值 = 对手活三 * 3 + 对手冲四 * 10 + 对手活四 * 50;

        if (己方威胁值 > 对手威胁值 + 10) {
            return 攻守态势.均势攻击;
        } else if (对手威胁值 > 己方威胁值 + 10) {
            return 攻守态势.均势防御;
        } else {
            return 攻守态势.均势;
        }
    }

    private static 威胁紧急度 计算威胁紧急度(int 对手活三, int 对手冲四, int 对手活四) {
        if (对手活四 > 0) {
            return 威胁紧急度.极高;
        }
        if (对手冲四 >= 2 || (对手冲四 >= 1 && 对手活三 >= 1)) {
            return 威胁紧急度.高;
        }
        if (对手活三 >= 2 || 对手冲四 >= 1) {
            return 威胁紧急度.中;
        }
        if (对手活三 >= 1) {
            return 威胁紧急度.低;
        }
        return 威胁紧急度.无;
    }

    private static int 统计威胁数量(GomokuBoard 棋盘, int 玩家, int 威胁类型) {
        // 简化实现 - 使用PatternEvaluator的现有功能
        switch (威胁类型) {
            case PatternEvaluator.活三:
                return PatternEvaluator.检查玩家活三威胁(棋盘, 玩家) ? 1 : 0;
            case PatternEvaluator.冲四:
                return PatternEvaluator.检查玩家冲四威胁(棋盘, 玩家) ? 1 : 0;
            case PatternEvaluator.活四:
                return PatternEvaluator.检查玩家活四威胁(棋盘, 玩家) ? 1 : 0;
            default:
                return 0;
        }
    }

    // ================== 数据类定义 ==================

    /**
     * 局面特征数据类
     */
    private static class 局面特征 {
        final 游戏阶段 阶段;
        final 攻守态势 态势;
        final 威胁紧急度 紧急度;
        final int 己方活三, 己方冲四, 己方活四;
        final int 对手活三, 对手冲四, 对手活四;

        局面特征(游戏阶段 阶段, 攻守态势 态势, 威胁紧急度 紧急度,
                int 己方活三, int 己方冲四, int 己方活四,
                int 对手活三, int 对手冲四, int 对手活四) {
            this.阶段 = 阶段;
            this.态势 = 态势;
            this.紧急度 = 紧急度;
            this.己方活三 = 己方活三;
            this.己方冲四 = 己方冲四;
            this.己方活四 = 己方活四;
            this.对手活三 = 对手活三;
            this.对手冲四 = 对手冲四;
            this.对手活四 = 对手活四;
        }
    }

    /**
     * 游戏阶段枚举
     */
    private enum 游戏阶段 {
        开局, 中局, 残局
    }

    /**
     * 攻守态势枚举
     */
    private enum 攻守态势 {
        强攻击, 均势攻击, 均势, 均势防御, 强防御, 紧急防御
    }

    /**
     * 威胁紧急度枚举
     */
    private enum 威胁紧急度 {
        无, 低, 中, 高, 极高
    }
}