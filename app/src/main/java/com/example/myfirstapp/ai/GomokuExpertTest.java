package com.example.myfirstapp.ai;

/**
 * 五子棋AI专家级自动测试系统
 *
 * 测试范围：
 * 1. AI获胜逻辑修复验证 (Win-First Logic)
 * 2. 禁手规则实现验证 (三三、四四、长连)
 * 3. 白棋破解禁手战术验证
 * 4. 综合实战能力验证
 *
 * 所有测试输出为中文，自动生成详细分析报告
 */
public class GomokuExpertTest {

    private GomokuAI ai引擎;
    private GomokuBoard 测试棋盘;

    // 测试统计
    private int 总测试数 = 0;
    private int 通过数 = 0;
    private int 失败数 = 0;

    /**
     * 构造函数 - 初始化AI引擎
     */
    public GomokuExpertTest() {
        this.ai引擎 = new GomokuAI(GomokuAI.难度等级.大师);
        this.测试棋盘 = new GomokuBoard();
    }

    /**
     * 执行所有专家级测试
     */
    public TestResult 执行全部测试() {
        打印测试开始信息();

        // 核心测试套件
        testWinOverDefense();          // 胜利优先于防御测试
        testForbiddenThreeThree();     // 三三禁手测试
        testForbiddenFourFour();       // 四四禁手测试
        testWhiteBreakForbidden();     // 白棋破解禁手测试

        // 补充测试
        testLongConnectionForbidden(); // 长连禁手测试
        testForbiddenTacticalValue();  // 禁手战术价值测试
        testAIConsistency();           // AI一致性测试

        打印测试总结();
        return new TestResult(总测试数, 通过数, 失败数);
    }

    /**
     * 测试1：Win-First Logic - AI应优先选择获胜而非防御
     */
    public void testWinOverDefense() {
        开始测试("AI获胜逻辑优先级测试");

        // 构建测试局面：AI(白棋)既可获胜也可防御对手威胁
        设置测试局面_AI可获胜可防御();

        // 将测试棋盘状态复制到AI的内部棋盘
        复制棋盘状态到AI();

        // AI应该选择获胜手而不是防御手
        GomokuAI.AI着法 AI结果 = ai引擎.getBestMove(GomokuBoard.WHITE);

        // 验证AI是否选择了获胜位置 (7,6)
        boolean 选择获胜 = (AI结果.x == 7 && AI结果.y == 6);

        验证结果(选择获胜, "AI应优先选择获胜手 (7,6)",
                "期望: (7,6), 实际: (" + AI结果.x + "," + AI结果.y + ")");

        清理测试棋盘();
    }

    /**
     * 测试2：三三禁手检测
     */
    public void testForbiddenThreeThree() {
        开始测试("三三禁手规则检测测试");

        // 构建会形成三三禁手的局面
        设置测试局面_三三禁手();

        // 检查 (7,7) 位置对黑棋是否为禁手
        boolean 是禁手 = ForbiddenMoveDetector.是否禁手(测试棋盘, 7, 7, GomokuBoard.BLACK);
        String 禁手类型 = ForbiddenMoveDetector.获取禁手类型(测试棋盘, 7, 7, GomokuBoard.BLACK);

        验证结果(是禁手 && "三三禁手".equals(禁手类型),
                "位置 (7,7) 应被检测为三三禁手",
                "检测结果: " + (是禁手 ? "禁手" : "合法") + ", 类型: " + 禁手类型);

        // 验证白棋不受禁手限制
        boolean 白棋不受限 = !ForbiddenMoveDetector.是否禁手(测试棋盘, 7, 7, GomokuBoard.WHITE);
        验证结果(白棋不受限, "白棋不受三三禁手限制",
                "白棋在相同位置应该合法");

        清理测试棋盘();
    }

    /**
     * 测试3：四四禁手检测
     */
    public void testForbiddenFourFour() {
        开始测试("四四禁手规则检测测试");

        设置测试局面_四四禁手();

        boolean 是禁手 = ForbiddenMoveDetector.是否禁手(测试棋盘, 8, 8, GomokuBoard.BLACK);
        String 禁手类型 = ForbiddenMoveDetector.获取禁手类型(测试棋盘, 8, 8, GomokuBoard.BLACK);

        验证结果(是禁手 && "四四禁手".equals(禁手类型),
                "位置 (8,8) 应被检测为四四禁手",
                "检测结果: " + (是禁手 ? "禁手" : "合法") + ", 类型: " + 禁手类型);

        清理测试棋盘();
    }

    /**
     * 测试4：白棋破解禁手战术
     */
    public void testWhiteBreakForbidden() {
        开始测试("白棋破解黑棋禁手战术测试");

        设置测试局面_白棋破禁();

        // 将测试棋盘状态复制到AI的内部棋盘
        复制棋盘状态到AI();

        // 白棋AI应该能识别并利用黑棋的禁手弱点
        GomokuAI.AI着法 白棋结果 = ai引擎.getBestMove(GomokuBoard.WHITE);

        // 评估该手是否有禁手战术价值
        测试棋盘.makeMove(白棋结果.x, 白棋结果.y, GomokuBoard.WHITE);

        // 检查黑棋周围是否有禁手点被创造
        boolean 创造禁手陷阱 = 检查周围禁手陷阱(白棋结果.x, 白棋结果.y);

        验证结果(创造禁手陷阱, "白棋应利用黑棋禁手规则创造战术优势",
                "白棋选择: (" + 白棋结果.x + "," + 白棋结果.y + "), 创造禁手陷阱: " + (创造禁手陷阱 ? "是" : "否"));

        测试棋盘.undoMove(白棋结果.x, 白棋结果.y);
        清理测试棋盘();
    }

    /**
     * 测试5：长连禁手检测
     */
    private void testLongConnectionForbidden() {
        开始测试("长连禁手规则检测测试");

        设置测试局面_长连禁手();

        boolean 是禁手 = ForbiddenMoveDetector.是否禁手(测试棋盘, 7, 3, GomokuBoard.BLACK);
        String 禁手类型 = ForbiddenMoveDetector.获取禁手类型(测试棋盘, 7, 3, GomokuBoard.BLACK);

        验证结果(是禁手 && "长连禁手".equals(禁手类型),
                "位置 (7,3) 应被检测为长连禁手",
                "检测结果: " + (是禁手 ? "禁手" : "合法") + ", 类型: " + 禁手类型);

        清理测试棋盘();
    }

    /**
     * 测试6：禁手战术价值评估
     */
    private void testForbiddenTacticalValue() {
        开始测试("禁手战术价值评估测试");

        设置测试局面_禁手战术();

        // 生成白棋的候选着法并检查是否正确评估禁手战术价值
        java.util.List<GomokuEvaluator.Move> 着法列表 = GomokuEvaluator.generateOrderedMoves(测试棋盘, GomokuBoard.WHITE, 4);

        boolean 有战术价值 = false;
        for (GomokuEvaluator.Move 着法 : 着法列表) {
            if (着法.score > PatternEvaluator.活三 / 10) { // 包含禁手战术分数
                有战术价值 = true;
                break;
            }
        }

        验证结果(有战术价值, "AI应正确评估禁手战术价值",
                "最高着法分数: " + (着法列表.isEmpty() ? "0" : String.valueOf(着法列表.get(0).score)));

        清理测试棋盘();
    }

    /**
     * 测试7：AI决策一致性
     */
    private void testAIConsistency() {
        开始测试("AI决策一致性测试");

        设置测试局面_一致性测试();

        // 将测试棋盘状态复制到AI的内部棋盘
        复制棋盘状态到AI();

        // 连续3次询问AI，应得到相同结果
        GomokuAI.AI着法 第一次 = ai引擎.getBestMove(GomokuBoard.WHITE);

        // 重新设置棋盘状态（因为AI可能修改了内部状态）
        复制棋盘状态到AI();
        GomokuAI.AI着法 第二次 = ai引擎.getBestMove(GomokuBoard.WHITE);

        复制棋盘状态到AI();
        GomokuAI.AI着法 第三次 = ai引擎.getBestMove(GomokuBoard.WHITE);

        boolean 决策一致 = (第一次.x == 第二次.x && 第一次.y == 第二次.y) &&
                          (第二次.x == 第三次.x && 第二次.y == 第三次.y);

        验证结果(决策一致, "AI在相同局面应做出一致决策",
                "第一次: (" + 第一次.x + "," + 第一次.y + "), 第二次: (" + 第二次.x + "," + 第二次.y + "), 第三次: (" + 第三次.x + "," + 第三次.y + ")");

        清理测试棋盘();
    }

    // ================== 测试局面设置方法 ==================

    /**
     * 设置AI可获胜可防御的测试局面
     */
    private void 设置测试局面_AI可获胜可防御() {
        // 白棋(AI)连四，一手获胜：_WWWW_
        测试棋盘.makeMove(7, 2, GomokuBoard.WHITE); // W
        测试棋盘.makeMove(7, 3, GomokuBoard.WHITE); // W
        测试棋盘.makeMove(7, 4, GomokuBoard.WHITE); // W
        测试棋盘.makeMove(7, 5, GomokuBoard.WHITE); // W
        // 获胜手在 (7,6) 或 (7,1)

        // 黑棋连三，需要防御：_BBB_
        测试棋盘.makeMove(9, 2, GomokuBoard.BLACK); // B
        测试棋盘.makeMove(9, 3, GomokuBoard.BLACK); // B
        测试棋盘.makeMove(9, 4, GomokuBoard.BLACK); // B
        // 防御手在 (9,5) 或 (9,1)
    }

    /**
     * 设置三三禁手测试局面
     */
    private void 设置测试局面_三三禁手() {
        // 构造会在 (7,7) 形成三三禁手的局面

        // 第一个三：水平方向
        测试棋盘.makeMove(7, 5, GomokuBoard.BLACK);
        测试棋盘.makeMove(7, 6, GomokuBoard.BLACK);
        // 在 (7,7) 落子后形成 _BB*_ (* = 要测试的位置)

        // 第二个三：垂直方向
        测试棋盘.makeMove(5, 7, GomokuBoard.BLACK);
        测试棋盘.makeMove(6, 7, GomokuBoard.BLACK);
        // 在 (7,7) 落子后形成 _BB*_

        // 确保两端开放
        // 水平方向: (7,4)和(7,8)为空
        // 垂直方向: (4,7)和(8,7)为空
    }

    /**
     * 设置四四禁手测试局面
     */
    private void 设置测试局面_四四禁手() {
        // 构造会在 (8,8) 形成四四禁手的局面

        // 第一个四：水平方向 BBBB_
        测试棋盘.makeMove(8, 4, GomokuBoard.BLACK);
        测试棋盘.makeMove(8, 5, GomokuBoard.BLACK);
        测试棋盘.makeMove(8, 6, GomokuBoard.BLACK);
        测试棋盘.makeMove(8, 7, GomokuBoard.BLACK);
        // 在 (8,8) 落子形成冲四

        // 第二个四：垂直方向 BBBB_
        测试棋盘.makeMove(4, 8, GomokuBoard.BLACK);
        测试棋盘.makeMove(5, 8, GomokuBoard.BLACK);
        测试棋盘.makeMove(6, 8, GomokuBoard.BLACK);
        测试棋盘.makeMove(7, 8, GomokuBoard.BLACK);
        // 在 (8,8) 落子形成第二个冲四
    }

    /**
     * 设置白棋破禁测试局面
     */
    private void 设置测试局面_白棋破禁() {
        // 设置一个白棋可以利用黑棋禁手的局面
        测试棋盘.makeMove(6, 6, GomokuBoard.BLACK);
        测试棋盘.makeMove(6, 7, GomokuBoard.BLACK);
        测试棋盘.makeMove(7, 5, GomokuBoard.BLACK);
        测试棋盘.makeMove(8, 5, GomokuBoard.BLACK);

        测试棋盘.makeMove(5, 5, GomokuBoard.WHITE);
        测试棋盘.makeMove(9, 9, GomokuBoard.WHITE);
    }

    /**
     * 设置长连禁手测试局面
     */
    private void 设置测试局面_长连禁手() {
        // 构造6连的局面
        测试棋盘.makeMove(7, 4, GomokuBoard.BLACK);
        测试棋盘.makeMove(7, 5, GomokuBoard.BLACK);
        测试棋盘.makeMove(7, 6, GomokuBoard.BLACK);
        测试棋盘.makeMove(7, 7, GomokuBoard.BLACK);
        测试棋盘.makeMove(7, 8, GomokuBoard.BLACK);
        // 在 (7,3) 或 (7,9) 落子会形成6连禁手
    }

    /**
     * 设置禁手战术测试局面
     */
    private void 设置测试局面_禁手战术() {
        // 设置白棋可通过战术利用黑棋禁手规则的局面
        测试棋盘.makeMove(7, 7, GomokuBoard.BLACK);
        测试棋盘.makeMove(7, 8, GomokuBoard.BLACK);
        测试棋盘.makeMove(8, 6, GomokuBoard.BLACK);
        测试棋盘.makeMove(9, 6, GomokuBoard.BLACK);

        测试棋盘.makeMove(6, 6, GomokuBoard.WHITE);
        测试棋盘.makeMove(5, 8, GomokuBoard.WHITE);
    }

    /**
     * 设置一致性测试局面
     */
    private void 设置测试局面_一致性测试() {
        // 设置一个有明确最优解的局面
        测试棋盘.makeMove(7, 7, GomokuBoard.BLACK);
        测试棋盘.makeMove(7, 8, GomokuBoard.WHITE);
        测试棋盘.makeMove(8, 7, GomokuBoard.BLACK);
        测试棋盘.makeMove(8, 8, GomokuBoard.WHITE);
        测试棋盘.makeMove(9, 7, GomokuBoard.BLACK);
        // 白棋应该在 (6,7) 防守
    }

    // ================== 辅助方法 ==================

    /**
     * 将测试棋盘状态复制到AI的内部棋盘
     */
    private void 复制棋盘状态到AI() {
        int[][] 棋盘状态 = 测试棋盘.getBoardCopy();
        ai引擎.initializeFromBoard(棋盘状态);
    }

    /**
     * 检查指定位置周围是否有禁手陷阱
     */
    private boolean 检查周围禁手陷阱(int x, int y) {
        int[][] 方向 = {{-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};

        for (int[] 方向向量 : 方向) {
            int 邻位x = x + 方向向量[0];
            int 邻位y = y + 方向向量[1];

            if (测试棋盘.isInBounds(邻位x, 邻位y) &&
                测试棋盘.getStone(邻位x, 邻位y) == GomokuBoard.EMPTY) {

                if (ForbiddenMoveDetector.是否禁手(测试棋盘, 邻位x, 邻位y, GomokuBoard.BLACK)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void 开始测试(String 测试名称) {
        总测试数++;
        System.out.println("\n--- " + 测试名称 + " ---");
    }

    private void 验证结果(boolean 通过, String 期望描述, String 实际结果) {
        if (通过) {
            通过数++;
            System.out.println("✓ " + 期望描述);
        } else {
            失败数++;
            System.out.println("✗ " + 期望描述);
            System.out.println("  详情: " + 实际结果);
        }
    }

    private void 清理测试棋盘() {
        测试棋盘 = new GomokuBoard();
        ai引擎.clearBoard(); // 同时清理AI的棋盘
    }

    private void 打印测试开始信息() {
        System.out.println("============================================================");
        System.out.println("         五子棋AI专家级自动测试系统");
        System.out.println("============================================================");
        System.out.println("测试目标: Win-First Logic + 禁手规则 + 战术智能");
        System.out.println("AI等级: 大师级 (8层搜索深度)");
        System.out.println();
    }

    private void 打印测试总结() {
        System.out.println("\n" + "============================================================");
        System.out.println("                测试结果总结");
        System.out.println("============================================================");
        System.out.println("总测试数: " + 总测试数);
        System.out.println("通过数量: " + 通过数);
        System.out.println("失败数量: " + 失败数);
        System.out.println("成功率: " + (总测试数 > 0 ? String.valueOf((double)通过数 / 总测试数 * 100) : "0") + "%");

        if (失败数 == 0) {
            System.out.println("\n🎉 所有测试通过！AI系统运行完美！");
        } else if (通过数 >= 总测试数 * 0.8) {
            System.out.println("\n✅ 大部分测试通过，系统基本健康");
        } else {
            System.out.println("\n⚠ 存在较多问题，建议进行修复");
        }
    }

    /**
     * 测试结果数据类
     */
    public static class TestResult {
        public final int 总测试数;
        public final int 通过数;
        public final int 失败数;
        public final double 成功率;

        public TestResult(int 总测试数, int 通过数, int 失败数) {
            this.总测试数 = 总测试数;
            this.通过数 = 通过数;
            this.失败数 = 失败数;
            this.成功率 = 总测试数 > 0 ? (double)通过数 / 总测试数 * 100 : 0;
        }

        public boolean 需要修复() {
            return 失败数 > 0;
        }
    }

    /**
     * 主测试方法
     */
    public static void main(String[] args) {
        System.out.println("启动五子棋AI专家级自动测试...\n");

        GomokuExpertTest 测试器 = new GomokuExpertTest();
        TestResult 结果 = 测试器.执行全部测试();

        // 如果有失败的测试，建议运行自动修复
        if (结果.需要修复()) {
            System.out.println("\n" + "============================================================");
            System.out.println("检测到问题，建议运行自动修复工作流:");
            System.out.println("java -cp . com.example.myfirstapp.ai.AutoRepairWorkflow");
            System.out.println("============================================================");
        }
    }
}