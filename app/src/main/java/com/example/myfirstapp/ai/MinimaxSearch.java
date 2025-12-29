package com.example.myfirstapp.ai;

import java.util.List;

/**
 * 五子棋专业搜索引擎 - Minimax算法 + Alpha-Beta剪枝
 * 优化版本：6步预测深度，3秒内快速响应
 */
public class MinimaxSearch {

    private static final int 无穷大 = Integer.MAX_VALUE / 2;
    private static final int 默认最大深度 = 6;
    private static final long 默认时间限制 = 3000; // 3秒

    private final TranspositionTable 置换表;
    private long 时间限制;
    private long 开始时间;
    private boolean 超时;
    private int 节点评估数;
    private int 最大深度;
    private int 剪枝次数;  // Alpha-Beta剪枝统计

    public MinimaxSearch() {
        this.置换表 = new TranspositionTable();
        this.时间限制 = 默认时间限制;
        this.最大深度 = 默认最大深度;
    }

    public MinimaxSearch(long 时间限制, int 最大深度) {
        this.置换表 = new TranspositionTable();
        this.时间限制 = 时间限制;
        this.最大深度 = 最大深度;
    }

    /**
     * 寻找最佳下法 - 迭代加深搜索，确保3秒内响应
     */
    public 搜索结果 寻找最佳下法(GomokuBoard 棋盘, int 玩家) {
        开始时间 = System.currentTimeMillis();
        超时 = false;
        节点评估数 = 0;
        剪枝次数 = 0;

        GomokuEvaluator.Move 最佳着法 = null;
        int 最佳分数 = -无穷大;
        int 达到深度 = 0;

        // 强制防守检查 - 优先级最高
        GomokuEvaluator.Move 强制防守着法 = 检查强制防守(棋盘, 玩家);
        if (强制防守着法 != null) {
            return new 搜索结果(强制防守着法, 0, 1, 1, 0.0, "强制防守");
        }

        // 迭代加深搜索 - 确保在时间内找到最佳解
        for (int 深度 = 1; 深度 <= 最大深度 && !超时; 深度++) {
            搜索结果 结果 = 搜索指定深度(棋盘, 玩家, 深度);

            if (!超时 && 结果.最佳着法 != null) {
                最佳着法 = 结果.最佳着法;
                最佳分数 = 结果.分数;
                达到深度 = 深度;

                // 如果发现必胜着法，直接返回
                if (最佳分数 > 无穷大 / 2) {
                    break;
                }
            }

            // 时间控制 - 预估下次迭代时间
            long 已用时间 = System.currentTimeMillis() - 开始时间;
            if (已用时间 > 时间限制 / 3) { // 如果已用1/3时间，停止更深搜索
                break;
            }
        }

        String 分析 = 生成着法分析(最佳分数, 达到深度, 节点评估数, 剪枝次数);
        return new 搜索结果(最佳着法, 最佳分数, 达到深度, 节点评估数, 置换表.getHitRate(), 分析);
    }

    /**
     * 检查是否需要强制防守（活三、冲四威胁）
     */
    private GomokuEvaluator.Move 检查强制防守(GomokuBoard 棋盘, int 当前玩家) {
        int 对手 = GomokuBoard.getOpponent(当前玩家);

        // 1. 检查对手是否有获胜威胁
        for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
            for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                if (棋盘.isValidMove(x, y)) {
                    if (PatternEvaluator.是否获胜手(棋盘, x, y, 对手)) {
                        return new GomokuEvaluator.Move(x, y, PatternEvaluator.连五);
                    }
                }
            }
        }

        // 2. 检查对手活三威胁 - 必须防守
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
     * 指定深度搜索
     */
    private 搜索结果 搜索指定深度(GomokuBoard 棋盘, int 玩家, int 深度) {
        GomokuEvaluator.Move 最佳着法 = null;
        int 最佳分数 = -无穷大;

        List<GomokuEvaluator.Move> 候选着法 = GomokuEvaluator.generateOrderedMoves(棋盘, 玩家, 0);

        // 优化：限制搜索宽度以提高速度
        int 最大宽度 = Math.max(8, 25 - 深度 * 3);
        if (候选着法.size() > 最大宽度) {
            候选着法 = 候选着法.subList(0, 最大宽度);
        }

        for (GomokuEvaluator.Move 着法 : 候选着法) {
            if (超时) break;

            棋盘.makeMove(着法.x, 着法.y, 玩家);

            int 分数 = -alphabeta(棋盘, GomokuBoard.getOpponent(玩家), 深度 - 1,
                                -无穷大, 无穷大, false);

            棋盘.undoMove(着法.x, 着法.y);

            if (分数 > 最佳分数 && !超时) {
                最佳分数 = 分数;
                最佳着法 = 着法;
            }
        }

        return new 搜索结果(最佳着法, 最佳分数, 深度, 节点评估数, 置换表.getHitRate(), "");
    }

    /**
     * Alpha-Beta剪枝算法 - 强化版本
     */
    private int alphabeta(GomokuBoard 棋盘, int 玩家, int 深度, int alpha, int beta, boolean 最大化) {
        // 时间检查
        if (System.currentTimeMillis() - 开始时间 > 时间限制) {
            超时 = true;
            return 0;
        }

        节点评估数++;

        // 置换表查询 - 优化性能
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

        // 终端节点检查
        if (深度 == 0 || 棋盘.isFull()) {
            int 分数 = PatternEvaluator.评估局面(棋盘, 最大化 ? 玩家 : GomokuBoard.getOpponent(玩家));
            置换表.store(zobrist, 0, 分数, TranspositionTable.EXACT, null);
            return 分数;
        }

        // 快速胜负判断
        int 当前玩家 = 最大化 ? 玩家 : GomokuBoard.getOpponent(玩家);
        int 评估 = PatternEvaluator.评估局面(棋盘, 当前玩家);

        // 如果局面已经胜负已分，立即返回
        if (Math.abs(评估) > 无穷大 / 2) {
            置换表.store(zobrist, 深度, 评估, TranspositionTable.EXACT, null);
            return 评估;
        }

        // 生成有序着法列表
        List<GomokuEvaluator.Move> 着法列表 = GomokuEvaluator.generateOrderedMoves(棋盘, 当前玩家, 最大深度 - 深度);

        // 动态剪枝：根据深度调整搜索宽度
        int 搜索宽度 = Math.max(6, 20 - (最大深度 - 深度) * 2);
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

                int 评估值 = alphabeta(棋盘, 玩家, 深度 - 1, alpha, beta, false);

                棋盘.undoMove(着法.x, 着法.y);

                if (评估值 > 最大评估) {
                    最大评估 = 评估值;
                    最佳着法 = 着法;
                }

                alpha = Math.max(alpha, 评估值);

                // Alpha-Beta剪枝
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

                int 评估值 = alphabeta(棋盘, 玩家, 深度 - 1, alpha, beta, true);

                棋盘.undoMove(着法.x, 着法.y);

                if (评估值 < 最小评估) {
                    最小评估 = 评估值;
                    最佳着法 = 着法;
                }

                beta = Math.min(beta, 评估值);

                // Alpha-Beta剪枝
                if (beta <= alpha) {
                    剪枝次数++;
                    break;
                }
            }

            // 存储到置换表
            int 标志 = 最小评估 <= 原始alpha ? TranspositionTable.UPPER_BOUND :
                      最小评估 >= beta ? TranspositionTable.LOWER_BOUND :
                      TranspositionTable.EXACT;

            置换表.store(zobrist, 深度, 最小评估, 标志, 最佳着法);
            return 最小评估;
        }
    }

    /**
     * 生成着法分析说明
     */
    private String 生成着法分析(int 分数, int 深度, int 节点数, int 剪枝数) {
        StringBuilder 分析 = new StringBuilder();

        if (分数 > PatternEvaluator.连五 / 2) {
            分析.append("必胜局面！");
        } else if (分数 > PatternEvaluator.活四 / 2) {
            分析.append("强势进攻");
        } else if (分数 > PatternEvaluator.活三 / 2) {
            分析.append("创造威胁");
        } else if (分数 > 0) {
            分析.append("局面略优");
        } else if (分数 < -PatternEvaluator.连五 / 2) {
            分析.append("防守反击");
        } else {
            分析.append("均势局面");
        }

        分析.append(String.format(" (深度:%d 节点:%d 剪枝:%d)", 深度, 节点数, 剪枝数));
        return 分析.toString();
    }

    /**
     * 清空置换表
     */
    public void 清空缓存() {
        置换表.clear();
    }

    /**
     * 设置时间限制
     */
    public void 设置时间限制(long 时间限制) {
        this.时间限制 = 时间限制;
    }

    /**
     * 设置最大搜索深度
     */
    public void 设置最大深度(int 最大深度) {
        this.最大深度 = 最大深度;
    }

    /**
     * 搜索结果类
     */
    public static class 搜索结果 {
        public final GomokuEvaluator.Move 最佳着法;
        public final int 分数;
        public final int 达到深度;
        public final int 节点评估数;
        public final double 缓存命中率;
        public final String 分析;

        public 搜索结果(GomokuEvaluator.Move 最佳着法, int 分数, int 达到深度,
                      int 节点评估数, double 缓存命中率, String 分析) {
            this.最佳着法 = 最佳着法;
            this.分数 = 分数;
            this.达到深度 = 达到深度;
            this.节点评估数 = 节点评估数;
            this.缓存命中率 = 缓存命中率;
            this.分析 = 分析;
        }

        @Override
        public String toString() {
            return String.format("着法: %s, 分数: %d, 深度: %d, 节点: %d, 缓存: %.1f%%, 分析: %s",
                    最佳着法, 分数, 达到深度, 节点评估数, 缓存命中率 * 100, 分析);
        }
    }
}