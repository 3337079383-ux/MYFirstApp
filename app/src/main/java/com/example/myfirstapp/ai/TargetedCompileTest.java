// 针对性的编译测试
package com.example.myfirstapp.ai;

public class TargetedCompileTest {

    private GomokuAI ai引擎;
    private GomokuBoard 测试棋盘;

    public TargetedCompileTest() {
        // 测试构造函数
        try {
            this.ai引擎 = new GomokuAI(GomokuAI.难度等级.大师);
            this.测试棋盘 = new GomokuBoard();
            System.out.println("✅ 构造函数测试通过");
        } catch (Exception e) {
            System.out.println("❌ 构造函数测试失败: " + e.getMessage());
        }
    }

    public void testBoardCopy() {
        try {
            // 测试棋盘复制
            int[][] 棋盘状态 = 测试棋盘.getBoardCopy();
            ai引擎.initializeFromBoard(棋盘状态);
            System.out.println("✅ 棋盘复制测试通过");
        } catch (Exception e) {
            System.out.println("❌ 棋盘复制测试失败: " + e.getMessage());
        }
    }

    public void testGetBestMove() {
        try {
            // 测试getBestMove方法
            GomokuAI.AI着法 结果 = ai引擎.getBestMove(GomokuBoard.WHITE);
            System.out.println("✅ getBestMove测试通过: " + 结果.toString());
        } catch (Exception e) {
            System.out.println("❌ getBestMove测试失败: " + e.getMessage());
        }
    }

    public void testForbiddenMove() {
        try {
            // 测试禁手检测
            boolean 禁手 = ForbiddenMoveDetector.是否禁手(测试棋盘, 7, 7, GomokuBoard.BLACK);
            System.out.println("✅ 禁手检测测试通过: " + 禁手);
        } catch (Exception e) {
            System.out.println("❌ 禁手检测测试失败: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("开始针对性编译测试...\n");

        TargetedCompileTest test = new TargetedCompileTest();
        test.testBoardCopy();
        test.testGetBestMove();
        test.testForbiddenMove();

        System.out.println("\n针对性编译测试完成！");
    }
}