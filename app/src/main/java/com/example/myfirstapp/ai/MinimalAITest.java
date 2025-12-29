package com.example.myfirstapp.ai;

/**
 * 最简化的AI测试 - 仅测试核心功能
 */
public class MinimalAITest {

    public static void testBasicFunctionality() {
        System.out.println("开始基础功能测试...");

        try {
            // 1. 创建棋盘
            GomokuBoard board = new GomokuBoard();
            System.out.println("✅ 棋盘创建成功");

            // 2. 创建AI
            GomokuAI ai = new GomokuAI(GomokuAI.难度等级.简单);
            System.out.println("✅ AI创建成功");

            // 3. 测试基本方法
            ai.clearBoard();
            System.out.println("✅ clearBoard成功");

            // 4. 测试getBestMove
            GomokuAI.AI着法 result = ai.getBestMove(GomokuBoard.BLACK);
            if (result != null) {
                System.out.println("✅ getBestMove成功: (" + result.x + "," + result.y + ")");
            }

            // 5. 测试禁手检测
            boolean forbidden = ForbiddenMoveDetector.是否禁手(board, 7, 7, GomokuBoard.BLACK);
            System.out.println("✅ 禁手检测成功: " + forbidden);

            System.out.println("\n🎉 所有基础功能测试通过！");

        } catch (Exception e) {
            System.out.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        testBasicFunctionality();
    }
}