// 简化的编译测试类
package com.example.myfirstapp.ai;

public class SimpleCompileTest {

    public static void main(String[] args) {
        System.out.println("编译测试开始");

        // 测试基本类实例化
        try {
            GomokuBoard board = new GomokuBoard();
            System.out.println("GomokuBoard 创建成功");

            GomokuAI ai = new GomokuAI(GomokuAI.难度等级.大师);
            System.out.println("GomokuAI 创建成功");

            // 测试基本方法调用
            ai.clearBoard();
            System.out.println("clearBoard 调用成功");

            System.out.println("✅ 基本功能编译测试通过！");

        } catch (Exception e) {
            System.out.println("❌ 编译测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}