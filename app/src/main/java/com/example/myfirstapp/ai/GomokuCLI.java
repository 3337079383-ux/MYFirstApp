package com.example.myfirstapp.ai;

import java.util.Scanner;

/**
 * 五子棋AI命令行测试界面
 * 提供交互式终端游戏，用于测试AI性能和棋力
 */
public class GomokuCLI {

    private static final String 重置 = "\u001B[0m";
    private static final String 黑色 = "\u001B[30m";
    private static final String 白色 = "\u001B[37m";
    private static final String 白色背景 = "\u001B[47m";
    private static final String 黑色背景 = "\u001B[40m";

    private final GomokuBoard 棋盘;
    private final GomokuAI ai引擎;
    private final Scanner 输入;
    private boolean 游戏运行中;
    private boolean 人类执黑;

    public GomokuCLI() {
        this.棋盘 = new GomokuBoard();
        this.ai引擎 = new GomokuAI(GomokuAI.难度等级.困难);
        this.输入 = new Scanner(System.in);
        this.人类执黑 = true;

        // 为CLI启用AI日志
        ai引擎.setLoggingEnabled(true);
    }

    /**
     * 主CLI循环
     */
    public void run() {
        打印欢迎信息();

        while (true) {
            显示主菜单();
            String 选择 = 输入.nextLine().trim();

            switch (选择.toLowerCase()) {
                case "1":
                case "开始游戏":
                    开始对局();
                    break;
                case "2":
                case "设置难度":
                    设置难度();
                    break;
                case "3":
                case "性能测试":
                    运行AI性能测试();
                    break;
                case "4":
                case "局面分析":
                    分析当前局面();
                    break;
                case "5":
                case "退出":
                case "q":
                    System.out.println("感谢使用五子棋AI测试程序！");
                    return;
                default:
                    System.out.println("无效选择，请重新输入。");
            }
        }
    }

    private void 打印欢迎信息() {
        System.out.println("========================================");
        System.out.println("     专业五子棋AI测试界面");
        System.out.println("========================================");
        System.out.println("当前AI等级: " + ai引擎.getDifficulty());
        System.out.println("搜索深度: 6步预测");
        System.out.println("响应时间: 3秒内");
        System.out.println();
    }

    private void 显示主菜单() {
        System.out.println("\n--- 主菜单 ---");
        System.out.println("1. 开始对局");
        System.out.println("2. 设置难度");
        System.out.println("3. AI性能测试");
        System.out.println("4. 局面分析");
        System.out.println("5. 退出程序");
        System.out.print("请选择: ");
    }

    private void 开始对局() {
        棋盘.clear();
        ai引擎.clearBoard();
        游戏运行中 = true;

        System.out.println("\n=== 开始新局 ===");
        System.out.print("请选择执子颜色 - 黑棋(先手)/白棋(后手) [B/w]: ");
        String 颜色 = 输入.nextLine().trim();
        人类执黑 = !颜色.toLowerCase().equals("w");

        System.out.println("您执" + (人类执黑 ? "黑棋(●)" : "白棋(○)"));
        System.out.println("输入着法坐标: x,y (0-14) 或输入'q'退出");

        打印棋盘();

        while (游戏运行中 && !棋盘.isFull()) {
            if (人类执黑 && 棋盘.getMoveCount() % 2 == 0 ||
                !人类执黑 && 棋盘.getMoveCount() % 2 == 1) {
                // 人类回合
                人类下子();
            } else {
                // AI回合
                AI下子();
            }

            打印棋盘();

            // 检查游戏结束
            if (检查对局结束()) {
                break;
            }
        }
    }

    private void 人类下子() {
        while (true) {
            System.out.print("您的着法 (x,y): ");
            String 输入内容 = 输入.nextLine().trim();

            if (输入内容.equals("q")) {
                游戏运行中 = false;
                return;
            }

            try {
                String[] 坐标 = 输入内容.split(",");
                int x = Integer.parseInt(坐标[0].trim());
                int y = Integer.parseInt(坐标[1].trim());

                if (棋盘.isValidMove(x, y)) {
                    int 玩家 = 人类执黑 ? GomokuBoard.BLACK : GomokuBoard.WHITE;
                    棋盘.makeMove(x, y, 玩家);
                    ai引擎.makeMove(x, y, 玩家);
                    break;
                } else {
                    System.out.println("无效着法！位置已被占用或超出边界。");
                }
            } catch (Exception e) {
                System.out.println("输入格式错误！请使用 x,y 格式 (例如: 7,7)");
            }
        }
    }

    private void AI下子() {
        System.out.println("\nAI思考中...");
        int ai玩家 = 人类执黑 ? GomokuBoard.WHITE : GomokuBoard.BLACK;

        long 开始时间 = System.currentTimeMillis();
        GomokuAI.AI着法 ai着法 = ai引擎.getBestMove(ai玩家);
        long 思考时间 = System.currentTimeMillis() - 开始时间;

        if (ai着法.isValidMove()) {
            棋盘.makeMove(ai着法.x, ai着法.y, ai玩家);

            System.out.println("AI落子: (" + ai着法.x + "," + ai着法.y + ")");
            System.out.println("分析: " + ai着法.分析);
            System.out.println("思考时间: " + 思考时间 + "毫秒");

            if (ai着法.搜索结果 != null) {
                System.out.println("搜索深度: " + ai着法.搜索结果.达到深度);
                System.out.println("评估节点: " + ai着法.搜索结果.节点评估数);
                System.out.printf("缓存命中率: %.1f%%\n", ai着法.搜索结果.缓存命中率 * 100);
            }
        } else {
            System.out.println("AI无法找到合适着法！");
            游戏运行中 = false;
        }
    }

    private void 设置难度() {
        System.out.println("\n=== 设置难度等级 ===");
        System.out.println("1. 简单   (3层深度, 1秒)");
        System.out.println("2. 中等   (4层深度, 2秒)");
        System.out.println("3. 困难   (6层深度, 3秒)");
        System.out.println("4. 大师   (8层深度, 5秒)");
        System.out.print("请选择: ");

        String 选择 = 输入.nextLine().trim();
        GomokuAI.难度等级[] 等级列表 = GomokuAI.难度等级.values();

        try {
            int 索引 = Integer.parseInt(选择) - 1;
            if (索引 >= 0 && 索引 < 等级列表.length) {
                ai引擎.setDifficulty(等级列表[索引]);
                System.out.println("难度已设置为: " + 等级列表[索引]);
            } else {
                System.out.println("无效选择。");
            }
        } catch (NumberFormatException e) {
            System.out.println("输入无效。");
        }
    }

    private void 运行AI性能测试() {
        System.out.println("\n=== AI性能测试 ===");
        System.out.print("输入测试局面数量 [10]: ");
        String 输入内容 = 输入.nextLine().trim();
        int 测试数量 = 输入内容.isEmpty() ? 10 : Integer.parseInt(输入内容);

        long 总时间 = 0;
        int 总节点 = 0;
        int 总深度 = 0;

        for (int i = 0; i < 测试数量; i++) {
            // 创建随机测试局面
            棋盘.clear();
            ai引擎.clearBoard();

            // 添加随机着法
            int 着法数 = 5 + (i % 10);
            for (int j = 0; j < 着法数; j++) {
                int x, y;
                do {
                    x = (int) (Math.random() * GomokuBoard.BOARD_SIZE);
                    y = (int) (Math.random() * GomokuBoard.BOARD_SIZE);
                } while (!棋盘.isValidMove(x, y));

                int 玩家 = (j % 2) == 0 ? GomokuBoard.BLACK : GomokuBoard.WHITE;
                棋盘.makeMove(x, y, 玩家);
                ai引擎.makeMove(x, y, 玩家);
            }

            // 测试AI性能
            System.out.print("测试 " + (i + 1) + "/" + 测试数量 + "... ");

            long 开始时间 = System.currentTimeMillis();
            GomokuAI.AI着法 着法 = ai引擎.getBestMove(GomokuBoard.WHITE);
            long 思考时间 = System.currentTimeMillis() - 开始时间;

            总时间 += 思考时间;
            if (着法.搜索结果 != null) {
                总节点 += 着法.搜索结果.节点评估数;
                总深度 += 着法.搜索结果.达到深度;
            }

            System.out.println(思考时间 + "毫秒");
        }

        // 打印结果
        System.out.println("\n=== 测试结果 ===");
        System.out.println("测试完成局面数: " + 测试数量);
        System.out.println("平均思考时间: " + (总时间 / 测试数量) + "毫秒");
        System.out.println("平均评估节点: " + (总节点 / 测试数量));
        System.out.println("平均搜索深度: " + (总深度 / 测试数量));
        System.out.println("总耗时: " + 总时间 + "毫秒");
        System.out.println("每秒节点数: " + (总节点 * 1000 / Math.max(总时间, 1)));
    }

    private void 分析当前局面() {
        System.out.println("\n=== 局面分析 ===");
        打印棋盘();

        System.out.println("\n棋盘状态:");
        System.out.println(棋盘.toString());

        if (棋盘.getMoveCount() > 0) {
            // 分析双方局面
            int 黑方分数 = PatternEvaluator.评估局面(棋盘, GomokuBoard.BLACK);
            int 白方分数 = PatternEvaluator.评估局面(棋盘, GomokuBoard.WHITE);

            System.out.println("静态评估:");
            System.out.println("黑方分数: " + 黑方分数);
            System.out.println("白方分数: " + 白方分数);
            System.out.println("局面平衡: " + (黑方分数 - 白方分数));

            // 显示AI推荐着法
            System.out.println("\nAI推荐着法:");
            GomokuAI.AI着法 着法 = ai引擎.getBestMove(GomokuBoard.WHITE);
            if (着法.isValidMove()) {
                System.out.println("推荐位置: (" + 着法.x + "," + 着法.y + ")");
                System.out.println("分析: " + 着法.分析);
            }
        }
    }

    private void 打印棋盘() {
        System.out.println();
        System.out.print("   ");
        for (int j = 0; j < GomokuBoard.BOARD_SIZE; j++) {
            System.out.printf("%2d", j);
        }
        System.out.println();

        for (int i = 0; i < GomokuBoard.BOARD_SIZE; i++) {
            System.out.printf("%2d ", i);
            for (int j = 0; j < GomokuBoard.BOARD_SIZE; j++) {
                int 棋子 = 棋盘.getStone(i, j);
                char 显示字符 = 棋子 == GomokuBoard.BLACK ? '●' :
                             棋子 == GomokuBoard.WHITE ? '○' :
                             ((i + j) % 2 == 0) ? '·' : '·';
                System.out.print(" " + 显示字符);
            }
            System.out.println();
        }
        System.out.println();
    }

    private boolean 检查对局结束() {
        // 检查获胜
        for (int i = 0; i < GomokuBoard.BOARD_SIZE; i++) {
            for (int j = 0; j < GomokuBoard.BOARD_SIZE; j++) {
                int 棋子 = 棋盘.getStone(i, j);
                if (棋子 != GomokuBoard.EMPTY && 棋盘.isWinningMove(i, j, 棋子)) {
                    String 获胜者 = 棋子 == GomokuBoard.BLACK ? "黑棋" : "白棋";
                    if (!人类执黑) 获胜者 = 获胜者.equals("黑棋") ? "AI" : "人类";
                    else 获胜者 = 获胜者.equals("黑棋") ? "人类" : "AI";

                    System.out.println("*** " + 获胜者 + " 获胜！ ***");
                    游戏运行中 = false;
                    return true;
                }
            }
        }

        if (棋盘.isFull()) {
            System.out.println("*** 和棋！ ***");
            游戏运行中 = false;
            return true;
        }

        return false;
    }

    /**
     * 主方法运行CLI
     */
    public static void main(String[] args) {
        GomokuCLI cli = new GomokuCLI();
        cli.run();
    }
}