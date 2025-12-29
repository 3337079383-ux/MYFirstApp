package com.example.myfirstapp.ai;

import java.util.List;

/**
 * 增强版五子棋搜索引擎
 * 新增功能：空步裁剪、深度动态调整、威胁序列检测
 * 专门针对VCF/VCTS攻击进行优化
 */
public class EnhancedMinimaxSearch {

    private static final int 无穷大 = Integer.MAX_VALUE / 2;
    private static final int 默认基础深度 = 6;
    private static final int 最大扩展深度 = 12;
    private static final long 默认时间限制 = 3000; // 3秒

    private final TranspositionTable 置换表;
    private long 时间限制;
    private long 开始时间;
    private boolean 超时;
    private int 节点评估数;
    private int 基础深度;
    private int 剪枝次数;
    private int 空步裁剪次数;
    private int 威胁扩展次数;

    public EnhancedMinimaxSearch() {
        this.置换表 = new TranspositionTable();
        this.时间限制 = 默认时间限制;
        this.基础深度 = 默认基础深度;
    }

    public EnhancedMinimaxSearch(long 时间限制, int 基础深度) {
        this.置换表 = new TranspositionTable();
        this.时间限制 = 时间限制;
        this.基础深度 = 基础深度;
    }

    /**
     * 增强版最佳着法搜索 - 支持威胁扩展和动态深度
     */
    public 增强搜索结果 寻找最佳下法(GomokuBoard 棋盘, int 玩家) {
        开始时间 = System.currentTimeMillis();
        超时 = false;
        节点评估数 = 0;
        剪枝次数 = 0;
        空步裁剪次数 = 0;
        威胁扩展次数 = 0;

        GomokuEvaluator.Move 最佳着法 = null;
        int 最佳分数 = -无穷大;
        int 达到深度 = 0;

        // 第一阶段：强制防守检查
        GomokuEvaluator.Move 强制防守着法 = 检查强制防守(棋盘, 玩家);
        if (强制防守着法 != null) {
            return new 增强搜索结果(强制防守着法, 0, 1, 1, 0.0, 0, 0, 0, "强制防守");
        }

        // 第二阶段：VCF威胁序列检测
        GomokuEvaluator.Move VCF着法 = 检查VCF序列(棋盘, 玩家);
        if (VCF着法 != null) {
            return new 增强搜索结果(VCF着法, 无穷大/2, 基础深度*2, 100, 0.0, 0, 0, 1, "VCF必胜序列");
        }

        // 第三阶段：动态深度迭代搜索
        for (int 深度 = 1; 深度 <= 基础深度 && !超时; 深度++) {
            增强搜索结果 结果 = 搜索指定深度(棋盘, 玩家, 深度);

            if (!超时 && 结果.最佳着法 != null) {
                最佳着法 = 结果.最佳着法;
                最佳分数 = 结果.分数;
                达到深度 = 深度;

                // 如果发现必胜，不需要更深搜索
                if (最佳分数 > 无穷大 / 2) {
                    break;
                }
            }

            // 动态时间控制
            long 已用时间 = System.currentTimeMillis() - 开始时间;
            if (已用时间 > 时间限制 / 4) {
                break;
            }
        }

        // 第四阶段：威胁扩展搜索
        if (!超时 && 检测到关键威胁(棋盘, 玩家)) {
            增强搜索结果 扩展结果 = 威胁扩展搜索(棋盘, 玩家, 最佳着法, 达到深度);
            if (扩展结果.最佳着法 != null && 扩展结果.分数 > 最佳分数) {
                最佳着法 = 扩展结果.最佳着法;
                最佳分数 = 扩展结果.分数;
                达到深度 = Math.max(达到深度, 扩展结果.达到深度);
                威胁扩展次数++;
            }
        }

        String 分析 = 生成增强分析(最佳分数, 达到深度, 节点评估数, 剪枝次数, 空步裁剪次数, 威胁扩展次数);
        return new 增强搜索结果(最佳着法, 最佳分数, 达到深度, 节点评估数, 置换表.getHitRate(),
                             剪枝次数, 空步裁剪次数, 威胁扩展次数, 分析);
    }

    /**
     * 检查强制防守情况
     */
    private GomokuEvaluator.Move 检查强制防守(GomokuBoard 棋盘, int 当前玩家) {
        int 对手 = GomokuBoard.getOpponent(当前玩家);

        // 1. 检查立即获胜威胁
        for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
            for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                if (棋盘.isValidMove(x, y)) {
                    if (PatternEvaluator.是否获胜手(棋盘, x, y, 对手)) {
                        return new GomokuEvaluator.Move(x, y, PatternEvaluator.连五);
                    }
                }
            }
        }

        // 2. 检查活三威胁
        if (PatternEvaluator.检查玩家活三威胁(棋盘, 对手)) {
            for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
                for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                    if (棋盘.isValidMove(x, y)) {
                        棋盘.makeMove(x, y, 当前玩家);
                        boolean 阻止威胁 = !PatternEvaluator.检查玩家活三威胁(棋盘, 对手);
                        棋盘.undoMove(x, y);

                        if (阻止威胁) {
                            return new GomokuEvaluator.Move(x, y, PatternEvaluator.活三);
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * VCF威胁序列检测 - 检查是否存在连续冲四必胜序列
     */
    private GomokuEvaluator.Move 检查VCF序列(GomokuBoard 棋盘, int 玩家) {
        // 寻找可以开始VCF攻击的着法
        for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
            for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                if (棋盘.isValidMove(x, y)) {
                    棋盘.makeMove(x, y, 玩家);

                    if (模拟VCF序列(棋盘, 玩家, 0, 6)) { // 检查6步内VCF
                        棋盘.undoMove(x, y);
                        return new GomokuEvaluator.Move(x, y, PatternEvaluator.连五);
                    }

                    棋盘.undoMove(x, y);
                }
            }
        }
        return null;
    }

    /**
     * 模拟VCF序列 - 递归检查连续冲四攻击
     */
    private boolean 模拟VCF序列(GomokuBoard 棋盘, int 攻击者, int 当前深度, int 最大深度) {
        if (当前深度 >= 最大深度) return false;

        int 防守者 = GomokuBoard.getOpponent(攻击者);

        // 检查攻击者是否已经获胜
        for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
            for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                if (棋盘.isValidMove(x, y) && PatternEvaluator.是否获胜手(棋盘, x, y, 攻击者)) {
                    return true; // VCF成功
                }
            }
        }

        // 寻找攻击者的冲四着法
        for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
            for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                if (棋盘.isValidMove(x, y)) {
                    棋盘.makeMove(x, y, 攻击者);

                    // 检查是否形成冲四威胁
                    boolean 形成冲四 = PatternEvaluator.检查玩家冲四威胁(棋盘, 攻击者);

                    if (形成冲四) {
                        // 让防守者选择最佳防守
                        boolean VCF成功 = true;

                        for (int dx = 0; dx < GomokuBoard.BOARD_SIZE && VCF成功; dx++) {
                            for (int dy = 0; dy < GomokuBoard.BOARD_SIZE && VCF成功; dy++) {
                                if (棋盘.isValidMove(dx, dy) && PatternEvaluator.是否获胜手(棋盘, dx, dy, 攻击者)) {
                                    // 防守者必须堵这个点
                                    棋盘.makeMove(dx, dy, 防守者);

                                    // 递归检查攻击者的下一轮攻击
                                    if (!模拟VCF序列(棋盘, 攻击者, 当前深度 + 1, 最大深度)) {
                                        VCF成功 = false;
                                    }

                                    棋盘.undoMove(dx, dy);
                                }
                            }
                        }

                        棋盘.undoMove(x, y);
                        if (VCF成功) return true;
                    } else {
                        棋盘.undoMove(x, y);
                    }
                }
            }
        }

        return false;
    }

    /**
     * 指定深度搜索
     */
    private 增强搜索结果 搜索指定深度(GomokuBoard 棋盘, int 玩家, int 深度) {
        GomokuEvaluator.Move 最佳着法 = null;
        int 最佳分数 = -无穷大;

        List<GomokuEvaluator.Move> 候选着法 = GomokuEvaluator.generateOrderedMoves(棋盘, 玩家, 0);

        // 优化：限制搜索宽度
        int 最大宽度 = Math.max(8, 25 - 深度 * 3);
        if (候选着法.size() > 最大宽度) {
            候选着法 = 候选着法.subList(0, 最大宽度);
        }

        for (GomokuEvaluator.Move 着法 : 候选着法) {
            if (超时) break;

            棋盘.makeMove(着法.x, 着法.y, 玩家);

            int 分数 = -增强alphabeta(棋盘, GomokuBoard.getOpponent(玩家), 深度 - 1,
                                   -无穷大, 无穷大, false, false);

            棋盘.undoMove(着法.x, 着法.y);

            if (分数 > 最佳分数 && !超时) {
                最佳分数 = 分数;
                最佳着法 = 着法;
            }
        }

        return new 增强搜索结果(最佳着法, 最佳分数, 深度, 节点评估数, 置换表.getHitRate(),
                             剪枝次数, 空步裁剪次数, 威胁扩展次数, "");
    }

    /**
     * 增强版Alpha-Beta搜索 - 支持空步裁剪
     */
    private int 增强alphabeta(GomokuBoard 棋盘, int 玩家, int 深度, int alpha, int beta,
                          boolean 最大化, boolean 允许空步) {
        // 时间检查
        if (System.currentTimeMillis() - 开始时间 > 时间限制) {
            超时 = true;
            return 0;
        }

        节点评估数++;

        // 置换表查询
        long zobrist = 棋盘.getZobristHash();
        TranspositionTable.Entry 缓存条目 = 置换表.lookup(zobrist);

        if (缓存条目 != null && 缓存条目.depth >= 深度) {
            switch (缓存条目.flag) {
                case TranspositionTable.EXACT:
                    return 缓存条目.score;
                case TranspositionTable.LOWER_BOUND:
                    alpha = Math.max(alpha, 缓存条目.score);
                    break;
                case TranspositionTable.UPPER_BOUND:
                    beta = Math.min(beta, 缓存条目.score);
                    break;
            }

            if (alpha >= beta) {
                剪枝次数++;
                return 缓存条目.score;
            }
        }

        // 威胁扩展检查
        boolean 需要威胁扩展 = 检测到关键威胁(棋盘, 玩家) || 检测到关键威胁(棋盘, GomokuBoard.getOpponent(玩家));

        // 终端节点检查
        if ((深度 == 0 && !需要威胁扩展) || 棋盘.isFull()) {
            int 分数 = PatternEvaluator.评估局面(棋盘, 最大化 ? 玩家 : GomokuBoard.getOpponent(玩家));
            置换表.store(zobrist, 0, 分数, TranspositionTable.EXACT, null);
            return 分数;
        }

        // 威胁扩展：如果检测到关键威胁，增加搜索深度
        if (需要威胁扩展 && 深度 == 0) {
            深度 = 2; // 扩展2层
            威胁扩展次数++;
        }

        // 空步裁剪 (Null Move Pruning)
        if (允许空步 && 深度 >= 3 && !需要威胁扩展 && beta - alpha == 1) {
            int 简化深度 = 深度 - 3; // R=2的空步裁剪
            int 空步分数 = -增强alphabeta(棋盘, GomokuBoard.getOpponent(玩家), 简化深度,
                                      -beta, -beta + 1, !最大化, false);

            if (空步分数 >= beta) {
                空步裁剪次数++;
                return beta; // 空步剪枝
            }
        }

        // 生成有序着法列表
        int 当前玩家 = 最大化 ? 玩家 : GomokuBoard.getOpponent(玩家);
        List<GomokuEvaluator.Move> 着法列表 = GomokuEvaluator.generateOrderedMoves(棋盘, 当前玩家, 基础深度 - 深度);

        // 动态搜索宽度控制
        int 搜索宽度 = 计算搜索宽度(深度, 需要威胁扩展);
        if (着法列表.size() > 搜索宽度) {
            着法列表 = 着法列表.subList(0, 搜索宽度);
        }

        int 原始alpha = alpha;
        GomokuEvaluator.Move 最佳着法 = null;

        if (最大化) {
            int 最大评估 = -无穷大;

            for (GomokuEvaluator.Move 着法 : 着法列表) {
                if (超时) break;

                棋盘.makeMove(着法.x, 着法.y, 玩家);

                int 评估值 = 增强alphabeta(棋盘, 玩家, 深度 - 1, alpha, beta, false, 允许空步);

                棋盘.undoMove(着法.x, 着法.y);

                if (评估值 > 最大评估) {
                    最大评估 = 评估值;
                    最佳着法 = 着法;
                }

                alpha = Math.max(alpha, 评估值);

                if (beta <= alpha) {
                    剪枝次数++;
                    break;
                }
            }

            // 存储到置换表
            int 标志 = 最大评估 <= 原始alpha ? TranspositionTable.UPPER_BOUND :
                      最大评估 >= beta ? TranspositionTable.LOWER_BOUND :
                      TranspositionTable.EXACT;

            置换表.store(zobrist, 深度, 最大评估, 标志, 最佳着法);
            return 最大评估;

        } else {
            int 最小评估 = 无穷大;

            for (GomokuEvaluator.Move 着法 : 着法列表) {
                if (超时) break;

                棋盘.makeMove(着法.x, 着法.y, GomokuBoard.getOpponent(玩家));

                int 评估值 = 增强alphabeta(棋盘, 玩家, 深度 - 1, alpha, beta, true, 允许空步);

                棋盘.undoMove(着法.x, 着法.y);

                if (评估值 < 最小评估) {
                    最小评估 = 评估值;
                    最佳着法 = 着法;
                }

                beta = Math.min(beta, 评估值);

                if (beta <= alpha) {
                    剪枝次数++;
                    break;
                }
            }

            int 标志 = 最小评估 <= 原始alpha ? TranspositionTable.UPPER_BOUND :
                      最小评估 >= beta ? TranspositionTable.LOWER_BOUND :
                      TranspositionTable.EXACT;

            置换表.store(zobrist, 深度, 最小评估, 标志, 最佳着法);
            return 最小评估;
        }
    }

    /**
     * 检测关键威胁 - 判断是否需要威胁扩展
     */
    private boolean 检测到关键威胁(GomokuBoard 棋盘, int 玩家) {
        // 检查活三威胁
        if (PatternEvaluator.检查玩家活三威胁(棋盘, 玩家)) {
            return true;
        }

        // 检查冲四威胁
        if (PatternEvaluator.检查玩家冲四威胁(棋盘, 玩家)) {
            return true;
        }

        // 检查是否存在立即获胜机会
        for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
            for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                if (棋盘.isValidMove(x, y) && PatternEvaluator.是否获胜手(棋盘, x, y, 玩家)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 威胁扩展搜索 - 对关键威胁进行更深搜索
     */
    private 增强搜索结果 威胁扩展搜索(GomokuBoard 棋盘, int 玩家, GomokuEvaluator.Move 基础着法, int 基础深度) {
        if (基础着法 == null) return null;

        int 扩展深度 = Math.min(基础深度 + 4, 最大扩展深度);

        棋盘.makeMove(基础着法.x, 基础着法.y, 玩家);

        int 扩展分数 = -增强alphabeta(棋盘, GomokuBoard.getOpponent(玩家), 扩展深度,
                                -无穷大, 无穷大, false, true);

        棋盘.undoMove(基础着法.x, 基础着法.y);

        return new 增强搜索结果(基础着法, 扩展分数, 扩展深度, 节点评估数, 置换表.getHitRate(),
                             剪枝次数, 空步裁剪次数, 威胁扩展次数, "威胁扩展");
    }

    /**
     * 动态计算搜索宽度
     */
    private int 计算搜索宽度(int 深度, boolean 威胁情况) {
        if (威胁情况) {
            return Math.max(8, 30 - 深度 * 2); // 威胁情况下宽搜索
        } else {
            return Math.max(6, 20 - 深度 * 3); // 正常情况窄搜索
        }
    }

    /**
     * 生成增强版分析
     */
    private String 生成增强分析(int 分数, int 深度, int 节点数, int 剪枝数, int 空步数, int 威胁数) {
        StringBuilder 分析 = new StringBuilder();

        if (分数 > PatternEvaluator.连五 / 2) {
            分析.append("必胜序列！");
        } else if (分数 > PatternEvaluator.活四 / 2) {
            分析.append("强势攻击");
        } else if (分数 > PatternEvaluator.活三 / 2) {
            分析.append("威胁创造");
        } else if (分数 > 0) {
            分析.append("局面有利");
        } else if (分数 < -PatternEvaluator.连五 / 2) {
            分析.append("防守模式");
        } else {
            分析.append("均势局面");
        }

        分析.append(String.format(" (深度:%d 节点:%d 剪枝:%d 空步:%d 威胁:%d)",
                深度, 节点数, 剪枝数, 空步数, 威胁数));
        return 分析.toString();
    }

    // Getters and Setters
    public void 清空缓存() { 置换表.clear(); }
    public void 设置时间限制(long 时间限制) { this.时间限制 = 时间限制; }
    public void 设置基础深度(int 基础深度) { this.基础深度 = 基础深度; }

    /**
     * 增强版搜索结果
     */
    public static class 增强搜索结果 {
        public final GomokuEvaluator.Move 最佳着法;
        public final int 分数;
        public final int 达到深度;
        public final int 节点评估数;
        public final double 缓存命中率;
        public final int 剪枝次数;
        public final int 空步裁剪次数;
        public final int 威胁扩展次数;
        public final String 分析;

        public 增强搜索结果(GomokuEvaluator.Move 最佳着法, int 分数, int 达到深度, int 节点评估数,
                        double 缓存命中率, int 剪枝次数, int 空步裁剪次数, int 威胁扩展次数, String 分析) {
            this.最佳着法 = 最佳着法;
            this.分数 = 分数;
            this.达到深度 = 达到深度;
            this.节点评估数 = 节点评估数;
            this.缓存命中率 = 缓存命中率;
            this.剪枝次数 = 剪枝次数;
            this.空步裁剪次数 = 空步裁剪次数;
            this.威胁扩展次数 = 威胁扩展次数;
            this.分析 = 分析;
        }

        @Override
        public String toString() {
            return String.format("着法: %s, 分数: %d, 深度: %d, 节点: %d, 缓存: %.1f%%, 剪枝: %d, 空步: %d, 威胁: %d, 分析: %s",
                    最佳着法, 分数, 达到深度, 节点评估数, 缓存命中率 * 100, 剪枝次数, 空步裁剪次数, 威胁扩展次数, 分析);
        }
    }
}