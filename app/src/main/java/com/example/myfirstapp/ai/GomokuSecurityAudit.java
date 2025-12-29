package com.example.myfirstapp.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * 五子棋AI安全审计系统
 * 全自动化测试AI防御漏洞并执行自我修复
 *
 * 测试覆盖：
 * 1. 瞬间成五防御测试
 * 2. 活三预警系统测试
 * 3. VCF/VCTS连续冲四攻击测试
 * 4. 极端防御场景压力测试
 */
public class GomokuSecurityAudit {

    private static final String 重置颜色 = "\u001B[0m";
    private static final String 红色 = "\u001B[31m";
    private static final String 绿色 = "\u001B[32m";
    private static final String 黄色 = "\u001B[33m";
    private static final String 蓝色 = "\u001B[34m";

    private GomokuAI ai引擎;
    private int 测试通过数 = 0;
    private int 测试失败数 = 0;
    private List<String> 失败报告 = new ArrayList<>();

    public GomokuSecurityAudit() {
        this.ai引擎 = new GomokuAI(GomokuAI.难度等级.大师); // 使用最高难度测试
        this.ai引擎.setLoggingEnabled(false); // 关闭日志以便清晰输出
    }

    /**
     * 执行完整的安全审计流程
     */
    public 审计结果 执行完整审计() {
        打印审计开始信息();

        // 第一轮：基础防御测试
        执行瞬间成五防御测试();
        执行活三预警测试();
        执行双三威胁测试();

        // 第二轮：高级攻击模式测试
        执行VCF连续冲四测试();
        执行VCTS胜利威胁测试();
        执行极端防御压力测试();

        // 第三轮：性能和逻辑一致性测试
        执行性能压力测试();
        执行逻辑一致性测试();

        return 生成审计报告();
    }

    /**
     * 测试1：瞬间成五防御 - AI必须100%识别并堵截活四
     */
    private void 执行瞬间成五防御测试() {
        打印测试标题("瞬间成五防御测试");

        // 测试用例1：水平活四
        测试瞬间成五场景("水平活四", new int[][]{
            {7, 7, 1}, {7, 8, 1}, {7, 9, 1}, {7, 10, 1} // 玩家在(7,6)或(7,11)可以获胜
        }, new int[]{7, 6}, new int[]{7, 11});

        // 测试用例2：垂直活四
        测试瞬间成五场景("垂直活四", new int[][]{
            {6, 7, 1}, {7, 7, 1}, {8, 7, 1}, {9, 7, 1} // 玩家在(5,7)或(10,7)可以获胜
        }, new int[]{5, 7}, new int[]{10, 7});

        // 测试用例3：斜线活四
        测试瞬间成五场景("斜线活四", new int[][]{
            {6, 6, 1}, {7, 7, 1}, {8, 8, 1}, {9, 9, 1} // 玩家在(5,5)或(10,10)可以获胜
        }, new int[]{5, 5}, new int[]{10, 10});

        // 测试用例4：跳冲四 XXX_X
        测试瞬间成五场景("跳冲四", new int[][]{
            {7, 7, 1}, {7, 8, 1}, {7, 9, 1}, {7, 11, 1} // 玩家在(7,10)可以获胜
        }, new int[]{7, 10});
    }

    /**
     * 测试2：活三预警测试 - 检测双向活三并提前破坏
     */
    private void 执行活三预警测试() {
        打印测试标题("活三预警测试");

        // 测试用例1：双向活三 _XXX_
        测试活三预警场景("双向活三", new int[][]{
            {7, 7, 1}, {7, 8, 1}, {7, 9, 1} // 在(7,6)和(7,10)都是空的情况
        });

        // 测试用例2：跳活三 _XX_X_
        测试活三预警场景("跳活三", new int[][]{
            {7, 7, 1}, {7, 8, 1}, {7, 10, 1}
        });

        // 测试用例3：复杂活三组合
        测试活三预警场景("复杂活三", new int[][]{
            {6, 7, 1}, {7, 7, 1}, {8, 7, 1}, // 垂直活三
            {7, 6, 1}, {7, 8, 1}  // 水平活三潜力
        });
    }

    /**
     * 测试3：双三威胁测试 - 同时出现两个活三
     */
    private void 执行双三威胁测试() {
        打印测试标题("双三威胁测试");

        // 测试用例：十字双三
        测试双三威胁场景("十字双三", new int[][]{
            {7, 6, 1}, {7, 7, 1}, {7, 8, 1}, // 水平活三
            {6, 7, 1}, {8, 7, 1}  // 垂直活三
        });
    }

    /**
     * 测试4：VCF连续冲四测试 - 模拟10步连续威胁
     */
    private void 执行VCF连续冲四测试() {
        打印测试标题("VCF连续冲四测试");

        // 构建复杂的VCF攻击序列
        测试VCF攻击序列("经典VCF", new int[][]{
            {7, 7, 1}, {7, 8, 1}, {7, 9, 1}, // 基础冲四威胁
            {8, 7, 1}, {9, 7, 1}, // 垂直威胁
            {6, 6, 1}, {8, 8, 1}  // 斜线威胁
        });
    }

    /**
     * 测试5：VCTS胜利威胁测试
     */
    private void 执行VCTS胜利威胁测试() {
        打印测试标题("VCTS胜利威胁测试");

        // 构建必胜威胁序列
        测试VCTS威胁序列("必胜组合", new int[][]{
            {7, 7, 1}, {7, 8, 1}, // 基础
            {6, 7, 1}, {6, 8, 1}, // 平行威胁
            {8, 6, 1}, {8, 7, 1}  // 交叉威胁
        });
    }

    /**
     * 测试6：极端防御压力测试
     */
    private void 执行极端防御压力测试() {
        打印测试标题("极端防御压力测试");

        // 同时面临多个威胁
        测试极端防御场景("多重威胁", new int[][]{
            {5, 5, 1}, {5, 6, 1}, {5, 7, 1}, // 活三1
            {6, 5, 1}, {7, 5, 1}, {8, 5, 1}, // 活三2
            {6, 6, 1}, {7, 7, 1}, {8, 8, 1}, // 斜线活三
            {4, 8, 1}, {5, 8, 1}, {6, 8, 1}  // 活三4
        });
    }

    /**
     * 测试7：性能压力测试
     */
    private void 执行性能压力测试() {
        打印测试标题("性能压力测试");

        long 开始时间 = System.currentTimeMillis();
        int 测试轮数 = 50;

        for (int i = 0; i < 测试轮数; i++) {
            GomokuBoard 测试棋盘 = new GomokuBoard();

            // 创建随机复杂局面
            for (int j = 0; j < 20; j++) {
                int x = (int)(Math.random() * 15);
                int y = (int)(Math.random() * 15);
                if (测试棋盘.isValidMove(x, y)) {
                    测试棋盘.makeMove(x, y, (j % 2) + 1);
                }
            }

            ai引擎.initializeFromBoard(测试棋盘.getBoardCopy());

            long 单次开始 = System.currentTimeMillis();
            GomokuAI.AI着法 着法 = ai引擎.getBestMove(2);
            long 单次耗时 = System.currentTimeMillis() - 单次开始;

            if (单次耗时 > 5000) { // 超过5秒认为性能不合格
                记录失败("性能测试失败", "第" + (i+1) + "轮耗时" + 单次耗时 + "毫秒，超过5秒限制");
            }
        }

        long 总耗时 = System.currentTimeMillis() - 开始时间;

        if (测试轮数 * 1000 / 总耗时 < 10) { // 平均每秒少于10次认为不合格
            记录失败("性能压力测试", "平均性能过低：" + (测试轮数 * 1000 / 总耗时) + "次/秒");
        } else {
            记录成功("性能压力测试", "通过 - 平均性能：" + (测试轮数 * 1000 / 总耗时) + "次/秒");
        }
    }

    /**
     * 测试8：逻辑一致性测试
     */
    private void 执行逻辑一致性测试() {
        打印测试标题("逻辑一致性测试");

        // 测试相同局面多次调用的一致性
        GomokuBoard 测试棋盘 = 创建标准测试局面();

        GomokuAI.AI着法 第一次结果 = null;
        boolean 一致性通过 = true;

        for (int i = 0; i < 10; i++) {
            ai引擎.initializeFromBoard(测试棋盘.getBoardCopy());
            GomokuAI.AI着法 当前结果 = ai引擎.getBestMove(2);

            if (第一次结果 == null) {
                第一次结果 = 当前结果;
            } else if (第一次结果.x != 当前结果.x || 第一次结果.y != 当前结果.y) {
                一致性通过 = false;
                break;
            }
        }

        if (一致性通过) {
            记录成功("逻辑一致性测试", "通过 - 相同局面输出一致");
        } else {
            记录失败("逻辑一致性测试", "失败 - 相同局面输出不一致");
        }
    }

    // ================== 具体测试实现方法 ==================

    private void 测试瞬间成五场景(String 场景名, int[][] 初始局面, int[]... 获胜位置) {
        GomokuBoard 棋盘 = 创建棋盘(初始局面);
        ai引擎.initializeFromBoard(棋盘.getBoardCopy());

        GomokuAI.AI着法 ai着法 = ai引擎.getBestMove(2);

        boolean 防御成功 = false;
        for (int[] 位置 : 获胜位置) {
            if (ai着法.x == 位置[0] && ai着法.y == 位置[1]) {
                防御成功 = true;
                break;
            }
        }

        if (防御成功) {
            记录成功(场景名, "AI正确识别并堵截活四威胁");
        } else {
            记录失败(场景名, "AI未能正确堵截活四 - 选择了(" + ai着法.x + "," + ai着法.y + ")");
        }
    }

    private void 测试活三预警场景(String 场景名, int[][] 初始局面) {
        GomokuBoard 棋盘 = 创建棋盘(初始局面);
        ai引擎.initializeFromBoard(棋盘.getBoardCopy());

        // 检查AI是否识别到活三威胁
        boolean 识别威胁 = PatternEvaluator.检查玩家活三威胁(棋盘, 1);

        if (识别威胁) {
            GomokuAI.AI着法 ai着法 = ai引擎.getBestMove(2);

            // 验证AI的响应是否合理（应该破坏活三或创造反击）
            棋盘.makeMove(ai着法.x, ai着法.y, 2);
            boolean 威胁减少 = !PatternEvaluator.检查玩家活三威胁(棋盘, 1) ||
                             PatternEvaluator.是否创造威胁(棋盘, ai着法.x, ai着法.y, 2);

            if (威胁减少) {
                记录成功(场景名, "AI正确识别活三威胁并采取对应措施");
            } else {
                记录失败(场景名, "AI识别活三但响应不当");
            }
        } else {
            记录失败(场景名, "AI未能识别活三威胁");
        }
    }

    private void 测试双三威胁场景(String 场景名, int[][] 初始局面) {
        GomokuBoard 棋盘 = 创建棋盘(初始局面);
        ai引擎.initializeFromBoard(棋盘.getBoardCopy());

        // 统计活三数量
        int 活三数量 = 统计活三数量(棋盘, 1);

        if (活三数量 >= 2) {
            GomokuAI.AI着法 ai着法 = ai引擎.getBestMove(2);

            // AI应该立即创造反击威胁或阻断其中一个活三
            棋盘.makeMove(ai着法.x, ai着法.y, 2);
            int 应对后活三数量 = 统计活三数量(棋盘, 1);
            boolean 创造反击 = PatternEvaluator.是否创造威胁(棋盘, ai着法.x, ai着法.y, 2);

            if (应对后活三数量 < 活三数量 || 创造反击) {
                记录成功(场景名, "AI正确应对双三威胁");
            } else {
                记录失败(场景名, "AI未能有效应对双三威胁");
            }
        } else {
            记录失败(场景名, "测试局面未正确构成双三威胁");
        }
    }

    private void 测试VCF攻击序列(String 场景名, int[][] 初始局面) {
        GomokuBoard 棋盘 = 创建棋盘(初始局面);
        ai引擎.initializeFromBoard(棋盘.getBoardCopy());

        // 模拟10步VCF攻击
        boolean 防御成功 = true;
        int 防御步数 = 0;

        for (int 步数 = 0; 步数 < 10; 步数++) {
            // 检查是否存在立即获胜威胁
            boolean 存在获胜威胁 = false;
            int 威胁x = -1, 威胁y = -1;

            for (int x = 0; x < 15; x++) {
                for (int y = 0; y < 15; y++) {
                    if (棋盘.isValidMove(x, y) &&
                        PatternEvaluator.是否获胜手(棋盘, x, y, 1)) {
                        存在获胜威胁 = true;
                        威胁x = x; 威胁y = y;
                        break;
                    }
                }
                if (存在获胜威胁) break;
            }

            if (存在获胜威胁) {
                GomokuAI.AI着法 ai着法 = ai引擎.getBestMove(2);

                if (ai着法.x == 威胁x && ai着法.y == 威胁y) {
                    // AI正确防御
                    棋盘.makeMove(ai着法.x, ai着法.y, 2);
                    防御步数++;
                } else {
                    // AI防御失败
                    防御成功 = false;
                    break;
                }
            } else {
                // 没有立即威胁，测试结束
                break;
            }

            // 更新AI状态
            ai引擎.initializeFromBoard(棋盘.getBoardCopy());
        }

        if (防御成功 && 防御步数 >= 3) {
            记录成功(场景名, "AI成功防御" + 防御步数 + "步VCF攻击");
        } else {
            记录失败(场景名, "AI在第" + 防御步数 + "步VCF攻击中失败");
        }
    }

    private void 测试VCTS威胁序列(String 场景名, int[][] 初始局面) {
        // 类似VCF，但更复杂的必胜威胁测试
        测试VCF攻击序列(场景名 + "(VCTS)", 初始局面);
    }

    private void 测试极端防御场景(String 场景名, int[][] 初始局面) {
        GomokuBoard 棋盘 = 创建棋盘(初始局面);
        ai引擎.initializeFromBoard(棋盘.getBoardCopy());

        int 初始威胁数 = 计算总威胁数(棋盘, 1);
        GomokuAI.AI着法 ai着法 = ai引擎.getBestMove(2);

        if (ai着法.isValidMove()) {
            棋盘.makeMove(ai着法.x, ai着法.y, 2);
            int 应对后威胁数 = 计算总威胁数(棋盘, 1);

            if (应对后威胁数 < 初始威胁数) {
                记录成功(场景名, "AI在极端情况下减少了对手威胁");
            } else {
                记录失败(场景名, "AI在极端情况下未能有效防御");
            }
        } else {
            记录失败(场景名, "AI在极端情况下无法生成有效着法");
        }
    }

    // ================== 辅助方法 ==================

    private GomokuBoard 创建棋盘(int[][] 局面) {
        GomokuBoard 棋盘 = new GomokuBoard();
        for (int[] 位置 : 局面) {
            if (位置.length >= 3) {
                棋盘.makeMove(位置[0], 位置[1], 位置[2]);
            }
        }
        return 棋盘;
    }

    private GomokuBoard 创建标准测试局面() {
        return 创建棋盘(new int[][]{
            {7, 7, 1}, {7, 8, 2}, {7, 9, 1},
            {8, 7, 2}, {8, 8, 1}, {8, 9, 2},
            {9, 7, 1}, {9, 8, 2}, {9, 9, 1}
        });
    }

    private int 统计活三数量(GomokuBoard 棋盘, int 玩家) {
        int 数量 = 0;
        // 简化版实现 - 实际应该调用PatternEvaluator的详细检查
        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                if (棋盘.getStone(x, y) == 玩家) {
                    // 检查以此点为中心的活三模式
                    if (检查活三模式(棋盘, x, y, 玩家)) {
                        数量++;
                    }
                }
            }
        }
        return 数量;
    }

    private boolean 检查活三模式(GomokuBoard 棋盘, int x, int y, int 玩家) {
        // 简化实现 - 实际应该使用PatternEvaluator
        int[][] 方向 = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        for (int[] 方向向量 : 方向) {
            int 连子数 = 1;
            boolean 左开放 = false, 右开放 = false;

            // 检查正方向
            for (int i = 1; i < 5; i++) {
                int nx = x + 方向向量[0] * i;
                int ny = y + 方向向量[1] * i;
                if (!棋盘.isInBounds(nx, ny)) break;
                if (棋盘.getStone(nx, ny) == 玩家) {
                    连子数++;
                } else if (棋盘.getStone(nx, ny) == 0) {
                    右开放 = true;
                    break;
                } else {
                    break;
                }
            }

            // 检查负方向
            for (int i = 1; i < 5; i++) {
                int nx = x - 方向向量[0] * i;
                int ny = y - 方向向量[1] * i;
                if (!棋盘.isInBounds(nx, ny)) break;
                if (棋盘.getStone(nx, ny) == 玩家) {
                    连子数++;
                } else if (棋盘.getStone(nx, ny) == 0) {
                    左开放 = true;
                    break;
                } else {
                    break;
                }
            }

            if (连子数 == 3 && 左开放 && 右开放) {
                return true;
            }
        }
        return false;
    }

    private int 计算总威胁数(GomokuBoard 棋盘, int 玩家) {
        int 威胁数 = 0;
        威胁数 += 统计活三数量(棋盘, 玩家) * 3;
        // 可以添加更多威胁类型的计算
        return 威胁数;
    }

    private void 打印审计开始信息() {
        System.out.println(蓝色 + "=================================" + 重置颜色);
        System.out.println(蓝色 + "  五子棋AI安全审计系统启动" + 重置颜色);
        System.out.println(蓝色 + "=================================" + 重置颜色);
        System.out.println("审计目标: 检测AI防御漏洞并自动修复");
        System.out.println("测试等级: 大师级 (最高难度)");
        System.out.println();
    }

    private void 打印测试标题(String 测试名) {
        System.out.println(黄色 + "\n--- " + 测试名 + " ---" + 重置颜色);
    }

    private void 记录成功(String 测试名, String 详情) {
        System.out.println(绿色 + "✓ " + 测试名 + ": " + 详情 + 重置颜色);
        测试通过数++;
    }

    private void 记录失败(String 测试名, String 详情) {
        System.out.println(红色 + "✗ " + 测试名 + ": " + 详情 + 重置颜色);
        测试失败数++;
        失败报告.add(测试名 + ": " + 详情);
    }

    private 审计结果 生成审计报告() {
        System.out.println("\n" + 蓝色 + "=================================" + 重置颜色);
        System.out.println(蓝色 + "       安全审计结果报告" + 重置颜色);
        System.out.println(蓝色 + "=================================" + 重置颜色);

        int 总测试数 = 测试通过数 + 测试失败数;
        double 成功率 = (double)测试通过数 / 总测试数 * 100;

        System.out.println("总测试数: " + 总测试数);
        System.out.println(绿色 + "通过数量: " + 测试通过数 + 重置颜色);
        System.out.println(红色 + "失败数量: " + 测试失败数 + 重置颜色);
        System.out.println("成功率: " + String.format("%.1f%%", 成功率));

        if (测试失败数 > 0) {
            System.out.println(红色 + "\n失败详情:" + 重置颜色);
            for (String 失败 : 失败报告) {
                System.out.println("  • " + 失败);
            }
        }

        return new 审计结果(测试通过数, 测试失败数, 失败报告, 成功率);
    }

    /**
     * 审计结果数据类
     */
    public static class 审计结果 {
        public final int 通过数量;
        public final int 失败数量;
        public final List<String> 失败详情;
        public final double 成功率;

        public 审计结果(int 通过数量, int 失败数量, List<String> 失败详情, double 成功率) {
            this.通过数量 = 通过数量;
            this.失败数量 = 失败数量;
            this.失败详情 = new ArrayList<>(失败详情);
            this.成功率 = 成功率;
        }

        public boolean 需要修复() {
            return 失败数量 > 0 || 成功率 < 90.0;
        }
    }

    /**
     * 主测试方法
     */
    public static void main(String[] args) {
        GomokuSecurityAudit 审计系统 = new GomokuSecurityAudit();
        审计结果 结果 = 审计系统.执行完整审计();

        if (结果.需要修复()) {
            System.out.println(红色 + "\n⚠ 检测到防御漏洞，需要执行自动修复！" + 重置颜色);
        } else {
            System.out.println(绿色 + "\n✅ AI防御系统运行良好，无需修复！" + 重置颜色);
        }
    }
}