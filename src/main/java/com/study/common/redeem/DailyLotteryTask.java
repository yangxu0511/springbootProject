package com.study.common.redeem;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("彩票号码每日推送任务");
        System.out.println("执行时间: " + LocalDateTime.now().format(DATE_TIME_FORMATTER));
        System.out.println("========================================\n");

        // 1. 先执行 Python 脚本更新最新开奖数据
        System.out.println("\n正在更新最新开奖数据...");
        try {
            RunPython.run();
            System.out.println("开奖数据更新完成！\n");
        } catch (Exception e) {
            System.out.println("更新开奖数据失败: " + e.getMessage());
            // 继续执行，不影响号码生成
        }

        LocalDate today = LocalDate.now();
        if (today.getDayOfWeek() == DayOfWeek.FRIDAY) {
            pushWeekendNumbers(today);
            return;
        }

        // 2. 获取今天的彩票类型
        String lotteryType = getLotteryType(today);
        if (lotteryType == null) {
            System.out.println("今天不是开奖日，无需生成号码。");
            return;
        }

        pushSingleDayNumbers(lotteryType);
    }

    private static void pushSingleDayNumbers(String lotteryType) {
        String typeName = "tc".equals(lotteryType) ? "大乐透" : "双色球";
        System.out.println("今日开奖项目: " + typeName);

        // 生成指定数量的号码
        System.out.println("正在生成 " + GENERATE_COUNT + " 注号码...\n");
        List<String> numbers = generateNumbers(lotteryType, GENERATE_COUNT);

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
     * 周五提前推送周末号码
     */
    private static void pushWeekendNumbers(LocalDate friday) {
        LocalDate saturday = friday.plusDays(1);
        LocalDate sunday = friday.plusDays(2);

        System.out.println("今天是周五，提前生成周末号码。");
        System.out.println("周六(" + saturday.format(DATE_FORMATTER) + ") 大乐透 2 注");
        System.out.println("周日(" + sunday.format(DATE_FORMATTER) + ") 双色球 2 注\n");

        List<String> tcNumbers = generateNumbers("tc", GENERATE_COUNT);
        List<String> fcNumbers = generateNumbers("fc", GENERATE_COUNT);

        if (tcNumbers.isEmpty() && fcNumbers.isEmpty()) {
            System.out.println("周末号码生成失败！");
            return;
        }

        System.out.println("\n正在推送周末号码到微信...");
        ServerChanPush.push(
                "周末彩票推荐号码",
                buildWeekendPushContent(friday, saturday, sunday, tcNumbers, fcNumbers)
        );

        System.out.println("\n========================================");
        System.out.println("任务执行完成！");
        System.out.println("========================================");
    }

    private static List<String> generateNumbers(String lotteryType, int count) {
        List<String> numbers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String number = DreamNumer.generateAndReturnNumber(lotteryType);
            if (number != null) {
                numbers.add(number);
                System.out.println(getTypeName(lotteryType) + " 第 " + (i + 1) + " 注: " + number);
            }
        }
        return numbers;
    }

    private static String buildWeekendPushContent(LocalDate friday, LocalDate saturday, LocalDate sunday,
                                                  List<String> tcNumbers, List<String> fcNumbers) {
        StringBuilder content = new StringBuilder();
        content.append("### 周五提前生成的周末号码\n\n");
        content.append("> 生成日期: ").append(friday.format(DATE_FORMATTER)).append("\n");
        content.append("> 生成时间: ").append(LocalDateTime.now().format(DATE_TIME_FORMATTER)).append("\n\n");

        appendNumberSection(content, "大乐透", saturday, tcNumbers);
        appendNumberSection(content, "双色球", sunday, fcNumbers);

        content.append("---\n");
        content.append("⚠️ *温馨提示：理性购彩，切勿沉迷*");
        return content.toString();
    }

    private static void appendNumberSection(StringBuilder content, String typeName, LocalDate targetDate,
                                            List<String> numbers) {
        content.append("### ").append(targetDate.format(DATE_FORMATTER))
                .append(" ").append(typeName).append("\n\n");

        if (numbers.isEmpty()) {
            content.append("生成失败，请手动补充。\n\n");
            return;
        }

        for (int i = 0; i < numbers.size(); i++) {
            content.append("**第 ").append(i + 1).append(" 注**: `")
                    .append(numbers.get(i)).append("`\n\n");
        }
    }

    /**
     * 获取指定日期的彩票类型
     * @return tc=大乐透, fc=双色球, null=不是开奖日
     */
    private static String getLotteryType(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY:
            case WEDNESDAY:
            case FRIDAY:
            case SATURDAY:
                return "tc";
            case TUESDAY:
            case THURSDAY:
            case SUNDAY:
                return "fc";
            default:
                return null;
        }
    }

    private static String getTypeName(String lotteryType) {
        return "tc".equals(lotteryType) ? "大乐透" : "双色球";
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
