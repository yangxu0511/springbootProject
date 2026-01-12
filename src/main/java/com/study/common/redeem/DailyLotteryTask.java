package com.study.common.redeem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 定时任务入口 - 每日自动生成号码并推送到微信
 * 
 * 使用说明：
 * 1. 运行此类的 main 方法即可生成当天号码并推送
 * 2. 配合 Windows 任务计划程序实现定时执行
 * 
 * @author yangxu
 * @since 2026/01/12
 */
public class DailyLotteryTask {

    // 每天生成的注数
    private static final int GENERATE_COUNT = 2;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("彩票号码每日推送任务");
        System.out.println("执行时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("========================================\n");

        // 1. 获取今天的彩票类型
        String lotteryType = getTodayLotteryType();
        if (lotteryType == null) {
            System.out.println("今天不是开奖日，无需生成号码。");
            return;
        }

        String typeName = "tc".equals(lotteryType) ? "大乐透" : "双色球";
        System.out.println("今日开奖项目: " + typeName);

        // 2. 先执行 Python 脚本更新最新开奖数据
        System.out.println("\n正在更新最新开奖数据...");
        try {
            RunPython.run();
            System.out.println("开奖数据更新完成！\n");
        } catch (Exception e) {
            System.out.println("更新开奖数据失败: " + e.getMessage());
            // 继续执行，不影响号码生成
        }

        // 3. 生成指定数量的号码
        System.out.println("正在生成 " + GENERATE_COUNT + " 注号码...\n");
        List<String> numbers = new ArrayList<>();
        for (int i = 0; i < GENERATE_COUNT; i++) {
            String number = DreamNumer.generateAndReturnNumber(lotteryType);
            if (number != null) {
                numbers.add(number);
                System.out.println("第 " + (i + 1) + " 注: " + number);
            }
        }

        if (numbers.isEmpty()) {
            System.out.println("号码生成失败！");
            return;
        }

        // 4. 推送到微信
        System.out.println("\n正在推送消息到微信...");
        ServerChanPush.pushLotteryNumbers(lotteryType, numbers);

        System.out.println("\n========================================");
        System.out.println("任务执行完成！");
        System.out.println("========================================");
    }

    /**
     * 获取今天的彩票类型
     * @return tc=大乐透, fc=双色球, null=不是开奖日
     */
    private static String getTodayLotteryType() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        // Calendar.DAY_OF_WEEK: 1=周日, 2=周一, ..., 7=周六
        switch (dayOfWeek) {
            case Calendar.MONDAY:    // 周一 - 大乐透
            case Calendar.WEDNESDAY: // 周三 - 大乐透
            case Calendar.FRIDAY:    // 周五 - 大乐透
            case Calendar.SATURDAY:  // 周六 - 大乐透
                return "tc";
            case Calendar.TUESDAY:   // 周二 - 双色球
            case Calendar.THURSDAY:  // 周四 - 双色球
            case Calendar.SUNDAY:    // 周日 - 双色球
                return "fc";
            default:
                return null;
        }
    }

    /**
     * 测试推送功能（不生成号码，只推送测试消息）
     */
    public static void testPush() {
        List<String> testNumbers = new ArrayList<>();
        testNumbers.add("01,08,15,22,28,33 05");
        testNumbers.add("03,11,17,24,30,32 12");
        ServerChanPush.pushLotteryNumbers("fc", testNumbers);
    }
}
