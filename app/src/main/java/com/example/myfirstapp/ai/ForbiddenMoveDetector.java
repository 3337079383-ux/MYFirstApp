package com.example.myfirstapp.ai;

/**
 * 五子棋禁手规则检测器
 * 实现标准五子棋规则中的三三禁手、四四禁手、长连禁手
 *
 * 禁手规则说明：
 * 1. 三三禁手：黑棋（先手）不能同时形成两个或多个活三
 * 2. 四四禁手：黑棋不能同时形成两个或多个冲四或活四
 * 3. 长连禁手：黑棋不能形成六个或六个以上的连子
 * 4. 白棋（后手）不受禁手限制
 */
public class ForbiddenMoveDetector {

    /**
     * 检查指定位置是否为禁手
     * @param 棋盘 当前棋盘状态
     * @param x 要检查的x坐标
     * @param y 要检查的y坐标
     * @param 玩家 玩家（1=黑棋，2=白棋）
     * @return true表示是禁手，false表示合法
     */
    public static boolean 是否禁手(GomokuBoard 棋盘, int x, int y, int 玩家) {
        // 白棋不受禁手限制
        if (玩家 != GomokuBoard.BLACK) {
            return false;
        }

        // 位置必须为空
        if (!棋盘.isValidMove(x, y)) {
            return true;
        }

        // 模拟落子检查禁手
        棋盘.makeMove(x, y, 玩家);
        boolean 结果 = 检查所有禁手(棋盘, x, y, 玩家);
        棋盘.undoMove(x, y);

        return 结果;
    }

    /**
     * 检查所有类型的禁手
     */
    private static boolean 检查所有禁手(GomokuBoard 棋盘, int x, int y, int 玩家) {
        // 1. 检查长连禁手
        if (检查长连禁手(棋盘, x, y, 玩家)) {
            return true;
        }

        // 2. 检查四四禁手
        if (检查四四禁手(棋盘, x, y, 玩家)) {
            return true;
        }

        // 3. 检查三三禁手
        if (检查三三禁手(棋盘, x, y, 玩家)) {
            return true;
        }

        return false;
    }

    /**
     * 检查长连禁手 - 六个或以上连子
     */
    private static boolean 检查长连禁手(GomokuBoard 棋盘, int x, int y, int 玩家) {
        int[][] 方向 = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        for (int[] 方向向量 : 方向) {
            int 连子数 = 1; // 包含当前落子

            // 正方向计算连子
            for (int i = 1; i < GomokuBoard.BOARD_SIZE; i++) {
                int nx = x + 方向向量[0] * i;
                int ny = y + 方向向量[1] * i;
                if (!棋盘.isInBounds(nx, ny) || 棋盘.getStone(nx, ny) != 玩家) {
                    break;
                }
                连子数++;
            }

            // 负方向计算连子
            for (int i = 1; i < GomokuBoard.BOARD_SIZE; i++) {
                int nx = x - 方向向量[0] * i;
                int ny = y - 方向向量[1] * i;
                if (!棋盘.isInBounds(nx, ny) || 棋盘.getStone(nx, ny) != 玩家) {
                    break;
                }
                连子数++;
            }

            // 长连禁手：6个或以上连子
            if (连子数 >= 6) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查四四禁手 - 同时形成两个或以上的四
     */
    private static boolean 检查四四禁手(GomokuBoard 棋盘, int x, int y, int 玩家) {
        int 四的数量 = 0;

        // 检查所有方向的四型
        int[][] 方向 = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        for (int[] 方向向量 : 方向) {
            if (检查单方向四型(棋盘, x, y, 方向向量[0], 方向向量[1], 玩家)) {
                四的数量++;
            }
        }

        // 四四禁手：两个或以上的四
        return 四的数量 >= 2;
    }

    /**
     * 检查单方向四型（活四或冲四）
     */
    private static boolean 检查单方向四型(GomokuBoard 棋盘, int x, int y, int dx, int dy, int 玩家) {
        // 检查以(x,y)为中心的各种四型模式

        // 活四模式：_XXXX_ (两端都开放)
        if (检查活四模式(棋盘, x, y, dx, dy, 玩家)) {
            return true;
        }

        // 冲四模式：XXXX_ 或 _XXXX (一端开放)
        if (检查冲四模式(棋盘, x, y, dx, dy, 玩家)) {
            return true;
        }

        return false;
    }

    /**
     * 检查活四模式：_XXXX_
     */
    private static boolean 检查活四模式(GomokuBoard 棋盘, int x, int y, int dx, int dy, int 玩家) {
        // 寻找连续4个己方棋子，两端都是空位的模式
        for (int 起始偏移 = -3; 起始偏移 <= 0; 起始偏移++) {
            boolean 模式匹配 = true;

            // 检查前置空位
            int 前位x = x + (起始偏移 - 1) * dx;
            int 前位y = y + (起始偏移 - 1) * dy;
            if (!棋盘.isInBounds(前位x, 前位y) || 棋盘.getStone(前位x, 前位y) != GomokuBoard.EMPTY) {
                continue;
            }

            // 检查4个连续棋子
            for (int i = 0; i < 4; i++) {
                int 检查x = x + (起始偏移 + i) * dx;
                int 检查y = y + (起始偏移 + i) * dy;
                if (!棋盘.isInBounds(检查x, 检查y) || 棋盘.getStone(检查x, 检查y) != 玩家) {
                    模式匹配 = false;
                    break;
                }
            }

            if (!模式匹配) continue;

            // 检查后置空位
            int 后位x = x + (起始偏移 + 4) * dx;
            int 后位y = y + (起始偏移 + 4) * dy;
            if (棋盘.isInBounds(后位x, 后位y) && 棋盘.getStone(后位x, 后位y) == GomokuBoard.EMPTY) {
                return true; // 找到活四
            }
        }

        return false;
    }

    /**
     * 检查冲四模式：XXXX_ 或 _XXXX
     */
    private static boolean 检查冲四模式(GomokuBoard 棋盘, int x, int y, int dx, int dy, int 玩家) {
        // 寻找连续4个己方棋子，一端开放的模式
        for (int 起始偏移 = -3; 起始偏移 <= 0; 起始偏移++) {
            boolean 模式匹配 = true;

            // 检查4个连续棋子
            for (int i = 0; i < 4; i++) {
                int 检查x = x + (起始偏移 + i) * dx;
                int 检查y = y + (起始偏移 + i) * dy;
                if (!棋盘.isInBounds(检查x, 检查y) || 棋盘.getStone(检查x, 检查y) != 玩家) {
                    模式匹配 = false;
                    break;
                }
            }

            if (!模式匹配) continue;

            // 检查是否至少一端开放
            int 前位x = x + (起始偏移 - 1) * dx;
            int 前位y = y + (起始偏移 - 1) * dy;
            int 后位x = x + (起始偏移 + 4) * dx;
            int 后位y = y + (起始偏移 + 4) * dy;

            boolean 前端开放 = 棋盘.isInBounds(前位x, 前位y) && 棋盘.getStone(前位x, 前位y) == GomokuBoard.EMPTY;
            boolean 后端开放 = 棋盘.isInBounds(后位x, 后位y) && 棋盘.getStone(后位x, 后位y) == GomokuBoard.EMPTY;

            // 冲四：至少一端开放，但不能两端都开放（那是活四）
            if ((前端开放 && !后端开放) || (!前端开放 && 后端开放)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查三三禁手 - 同时形成两个或以上的活三
     */
    private static boolean 检查三三禁手(GomokuBoard 棋盘, int x, int y, int 玩家) {
        int 活三数量 = 0;

        // 检查所有方向的活三
        int[][] 方向 = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        for (int[] 方向向量 : 方向) {
            if (检查单方向活三(棋盘, x, y, 方向向量[0], 方向向量[1], 玩家)) {
                活三数量++;
            }
        }

        // 三三禁手：两个或以上的活三
        return 活三数量 >= 2;
    }

    /**
     * 检查单方向活三：_XXX_
     */
    private static boolean 检查单方向活三(GomokuBoard 棋盘, int x, int y, int dx, int dy, int 玩家) {
        // 寻找连续3个己方棋子，两端都是空位的模式
        for (int 起始偏移 = -2; 起始偏移 <= 0; 起始偏移++) {
            boolean 模式匹配 = true;

            // 检查前置空位
            int 前位x = x + (起始偏移 - 1) * dx;
            int 前位y = y + (起始偏移 - 1) * dy;
            if (!棋盘.isInBounds(前位x, 前位y) || 棋盘.getStone(前位x, 前位y) != GomokuBoard.EMPTY) {
                continue;
            }

            // 检查3个连续棋子
            for (int i = 0; i < 3; i++) {
                int 检查x = x + (起始偏移 + i) * dx;
                int 检查y = y + (起始偏移 + i) * dy;
                if (!棋盘.isInBounds(检查x, 检查y) || 棋盘.getStone(检查x, 检查y) != 玩家) {
                    模式匹配 = false;
                    break;
                }
            }

            if (!模式匹配) continue;

            // 检查后置空位
            int 后位x = x + (起始偏移 + 3) * dx;
            int 后位y = y + (起始偏移 + 3) * dy;
            if (棋盘.isInBounds(后位x, 后位y) && 棋盘.getStone(后位x, 后位y) == GomokuBoard.EMPTY) {
                // 进一步检查是否能形成活四（确保是真正的活三）
                if (能形成活四(棋盘, 前位x, 前位y, dx, dy, 玩家) ||
                    能形成活四(棋盘, 后位x, 后位y, dx, dy, 玩家)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 检查在指定位置落子后是否能形成活四
     */
    private static boolean 能形成活四(GomokuBoard 棋盘, int x, int y, int dx, int dy, int 玩家) {
        if (!棋盘.isInBounds(x, y) || 棋盘.getStone(x, y) != GomokuBoard.EMPTY) {
            return false;
        }

        // 模拟落子
        棋盘.makeMove(x, y, 玩家);
        boolean 结果 = 检查活四模式(棋盘, x, y, dx, dy, 玩家);
        棋盘.undoMove(x, y);

        return 结果;
    }

    /**
     * 获取禁手类型描述
     */
    public static String 获取禁手类型(GomokuBoard 棋盘, int x, int y, int 玩家) {
        if (玩家 != GomokuBoard.BLACK) {
            return "";
        }

        if (!棋盘.isValidMove(x, y)) {
            return "";
        }

        棋盘.makeMove(x, y, 玩家);

        String 禁手类型 = "";
        if (检查长连禁手(棋盘, x, y, 玩家)) {
            禁手类型 = "长连禁手";
        } else if (检查四四禁手(棋盘, x, y, 玩家)) {
            禁手类型 = "四四禁手";
        } else if (检查三三禁手(棋盘, x, y, 玩家)) {
            禁手类型 = "三三禁手";
        }

        棋盘.undoMove(x, y);
        return 禁手类型;
    }

    /**
     * 检查整个棋盘的所有禁手点（用于调试）
     */
    public static void 显示所有禁手点(GomokuBoard 棋盘, int 玩家) {
        System.out.println("当前" + (玩家 == GomokuBoard.BLACK ? "黑棋" : "白棋") + "的禁手点：");

        for (int x = 0; x < GomokuBoard.BOARD_SIZE; x++) {
            for (int y = 0; y < GomokuBoard.BOARD_SIZE; y++) {
                if (是否禁手(棋盘, x, y, 玩家)) {
                    String 类型 = 获取禁手类型(棋盘, x, y, 玩家);
                    System.out.println("(" + x + "," + y + ") - " + 类型);
                }
            }
        }
    }
}