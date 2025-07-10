package com.study.common.redeem;
/*
 * @Author yangx
 * @Description 描述
 * @Since create in 2024-4-24 11:34:05
 * @Company 广州云趣信息科技有限公司
 */

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.study.common.base.AppBaseNum;
import com.study.common.base.Constants;
import com.study.common.utils.DateUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yangxu
 * @create 2024/4/24 11:34
 */
public class RedeemNum extends AppBaseNum {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * @Author yangxu
     * @Description 号码比对
     * @Param: [params]
     * @Return: void
     * @Since create in 2024/1/22 13:57
     * @Company 广州云趣信息科技有限公司
     */
    public static void redeem(String params) {
        RunPython.run();

        // 初始化彩票上下文
        LotteryContext context = initializeLotteryContext(params);
        if (!context.isValid()) {
            System.out.println("无效的彩票上下文,退出处理...");
            return;
        }

        // 处理开奖号码
        if (!processWinningNumbers(context)) {
            return;
        }

        // 处理已购买号码
        processPlayerNumbers(context);

        // 处理未购买号码
        processUnplayedNumbers(context);

        // 处理历史号码
        processHistoricalNumbers(context);

        // 比对历史开奖号码
        compareWithHistoricalWinningNumbers(context);
    }

    /**
     * 初始化彩票上下文
     */
    private static LotteryContext initializeLotteryContext(String params) {
        LotteryContext context = new LotteryContext();

        if (StrUtil.isEmpty(params)) {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            context.setOpenDate(DateUtils.getYesterdayDate());
            context.setBuyDate(DateUtils.getYesterdayDate());
            context.setCurrentDay(yesterday.getDayOfWeek().getValue());
        } else {
            context.setOpenDate(params);
            context.setBuyDate(params);
            context.setCurrentDay(LocalDate.parse(params, DATE_FORMATTER).getDayOfWeek().getValue());
        }

        configureLotteryType(context);
        adjustBuyDateForWeekend(context);

        return context;
    }

    /**
     * 配置彩票类型
     */
    private static void configureLotteryType(LotteryContext context) {
        int currentDay = context.getCurrentDay();
        if (currentDay == 1 || currentDay == 3 || currentDay == 5 || currentDay == 6) {
            context.setLotteryType("tc");
            context.setFilePath(Constants.getTcFilePath());
            context.setBlueSize(2);
            context.setRedSize(5);
        } else if (currentDay == 2 || currentDay == 4 || currentDay == 0 || currentDay == 7) {
            context.setLotteryType("fc");
            context.setFilePath(Constants.getFcFilePath());
            context.setBlueSize(1);
            context.setRedSize(6);
        }
    }

    /**
     * 调整周末购买日期
     */
    private static void adjustBuyDateForWeekend(LotteryContext context) {
        if (("tc".equals(context.getLotteryType()) && context.getCurrentDay() == 6) ||
                ("fc".equals(context.getLotteryType()) && context.getCurrentDay() == 7)) {
            LocalDate buyDate = LocalDate.parse(context.getBuyDate(), DATE_FORMATTER);
            int daysToSubtract = context.getCurrentDay() == 6 ? 1 : 2;
            context.setBuyDate(buyDate.minusDays(daysToSubtract).format(DATE_FORMATTER));
        }
    }

    /**
     * 处理开奖号码
     */
    private static boolean processWinningNumbers(LotteryContext context) {
        JSONObject openData = filterJson(context.getFilePath());
        String openNumber = openData.getString(context.getOpenDate());

        if (StrUtil.isEmpty(openNumber)) {
            System.out.println("未获取到当天的中奖号码,执行失败...\n");
            return false;
        }

        String lotteryName = "tc".equals(context.getLotteryType()) ? "大乐透" : "双色球";
        System.out.println("开奖奖项是：" + lotteryName + "号码为：" + openNumber);

        parseWinningNumbers(context, openNumber);
        return true;
    }

    /**
     * 解析开奖号码
     */
    private static void parseWinningNumbers(LotteryContext context, String openNumber) {
        String[] numbers = openNumber.split("\\|");

        // 解析红球
        context.setOpenRedNumbers(Arrays.stream(numbers)
                .limit(context.getRedSize())
                .map(Integer::parseInt)
                .collect(Collectors.toList()));

        // 解析蓝球
        context.setOpenBlueNumbers(Arrays.stream(numbers)
                .skip(numbers.length - context.getBlueSize())
                .map(Integer::parseInt)
                .collect(Collectors.toList()));
    }

    /**
     * 处理已购买号码
     */
    private static void processPlayerNumbers(LotteryContext context) {
        JSONObject hisJson = filterJson(Constants.getHisFilePath());
        String boughtNumbers = hisJson.getString(context.getBuyDate());

        if (StrUtil.isEmpty(boughtNumbers)) {
            System.out.println("当天尚未购彩----");
            return;
        }

        System.out.println("\n开始对比当天购买的号码---->start");
        processNumberSet(context, boughtNumbers, true);
        System.out.println("当天号码对比结束---->end\n");
    }

    /**
     * 处理未购买号码
     */
    private static void processUnplayedNumbers(LotteryContext context) {
        JSONObject notBuyJson = filterJson(Constants.getNotBuyPath());
        String notBuyNumbers = notBuyJson.getString(context.getBuyDate());

        if (StrUtil.isNotEmpty(notBuyNumbers)) {
            System.out.println("开始对比未购买的号码---->start");
            processNumberSet(context, notBuyNumbers, false);
        }
    }

    /**
     * 处理号码集合
     */
    private static void processNumberSet(LotteryContext context, String numbers, boolean isBoughtNumbers) {
        String[] numberArray = numbers.split("\\|");
        boolean hasWinning = false;

        for (String number : numberArray) {
            if (!isValidNumberFormat(number, context.getBlueSize())) {
                continue;
            }

            MatchResult result = calculateMatchResult(context, number);
            String prize = getPrizeInfo(context.getLotteryType(), result);

            if (prize != null) {
                hasWinning = true;
                System.out.println((isBoughtNumbers ? "购买号码：" : "未购买号码：") + number + prize);
            }
        }

        if (isBoughtNumbers && !hasWinning) {
            System.out.println("当天号码未中奖o(╥﹏╥)o");
        }
    }

    /**
     * 处理历史号码
     */
    private static void processHistoricalNumbers(LotteryContext context) {
        System.out.println("开始统计历史购彩记录有无中奖信息（只统计红球>=" + Constants.sameRedSize + ")--->start");

        JSONObject hisJson = filterJson(Constants.getHisFilePath());
        for (String date : hisJson.keySet()) {
            String numbers = hisJson.getString(date);
            for (String number : numbers.split("\\|")) {
                if (!isValidNumberFormat(number, context.getBlueSize())) {
                    continue;
                }

                MatchResult result = calculateMatchResult(context, number);
                if (result.getRedCount() >= Constants.sameRedSize) {
                    outputHistoricalMatch(context.getLotteryType(), result, date, number);
                }
            }
        }

        System.out.println("历史号码统计结束---->end\n");
    }

    /**
     * 比对历史开奖号码
     */
    private static void compareWithHistoricalWinningNumbers(LotteryContext context) {
        System.out.println("统计开奖号码跟历史开奖号码相似程度-->");
        comparisonOpenNum(
                context.getOpenRedNumbers(),
                context.getOpenBlueNumbers(),
                context.getRedSize(),
                context.getBlueSize(),
                filterJson(context.getFilePath())
        );
    }

    /**
     * 彩票上下文类
     */
    private static class LotteryContext {
        private String openDate;
        private String buyDate;
        private String filePath;
        private String lotteryType;
        private int currentDay;
        private int blueSize;
        private int redSize;
        private List<Integer> openRedNumbers;
        private List<Integer> openBlueNumbers;

        // Getters and Setters
        public String getOpenDate() {
            return openDate;
        }

        public void setOpenDate(String openDate) {
            this.openDate = openDate;
        }

        public String getBuyDate() {
            return buyDate;
        }

        public void setBuyDate(String buyDate) {
            this.buyDate = buyDate;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getLotteryType() {
            return lotteryType;
        }

        public void setLotteryType(String lotteryType) {
            this.lotteryType = lotteryType;
        }

        public int getCurrentDay() {
            return currentDay;
        }

        public void setCurrentDay(int currentDay) {
            this.currentDay = currentDay;
        }

        public int getBlueSize() {
            return blueSize;
        }

        public void setBlueSize(int blueSize) {
            this.blueSize = blueSize;
        }

        public int getRedSize() {
            return redSize;
        }

        public void setRedSize(int redSize) {
            this.redSize = redSize;
        }

        public List<Integer> getOpenRedNumbers() {
            return openRedNumbers;
        }

        public void setOpenRedNumbers(List<Integer> openRedNumbers) {
            this.openRedNumbers = openRedNumbers;
        }

        public List<Integer> getOpenBlueNumbers() {
            return openBlueNumbers;
        }

        public void setOpenBlueNumbers(List<Integer> openBlueNumbers) {
            this.openBlueNumbers = openBlueNumbers;
        }

        public boolean isValid() {
            return StrUtil.isNotEmpty(filePath) && StrUtil.isNotEmpty(lotteryType);
        }
    }

    /**
     * 匹配结果类
     */
    private static class MatchResult {
        private final int redCount;
        private final int blueCount;

        public MatchResult(int redCount, int blueCount) {
            this.redCount = redCount;
            this.blueCount = blueCount;
        }

        public int getRedCount() {
            return redCount;
        }

        public int getBlueCount() {
            return blueCount;
        }

        public String getKey() {
            return redCount + "-" + blueCount;
        }
    }

    /**
     * 验证号码格式
     */
    private static boolean isValidNumberFormat(String number, int expectedBlueSize) {
        return number.split("\\s")[1].split(",").length == expectedBlueSize;
    }

    /**
     * 计算匹配结果
     */
    private static MatchResult calculateMatchResult(LotteryContext context, String number) {
        String[] parts = number.split("\\s");
        List<Integer> redNumbers = Arrays.stream(parts[0].split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        List<Integer> blueNumbers = Arrays.stream(parts[1].split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        int redCount = (int) redNumbers.stream()
                .filter(context.getOpenRedNumbers()::contains)
                .count();
        int blueCount = (int) blueNumbers.stream()
                .filter(context.getOpenBlueNumbers()::contains)
                .count();

        return new MatchResult(redCount, blueCount);
    }

    /**
     * 获取奖项信息
     */
    private static String getPrizeInfo(String lotteryType, MatchResult result) {
        Map<String, String> prizeMap = "tc".equals(lotteryType) ?
                Constants.getTcMap() : Constants.getFcMap();
        return prizeMap.get(result.getKey());
    }

    /**
     * 输出历史匹配信息
     */
    private static void outputHistoricalMatch(String lotteryType, MatchResult result, String date, String number) {
        String prize = getPrizeInfo(lotteryType, result);
        if (prize != null) {
            System.out.println("你曾经在" + date + "购买的这注彩票 目前已经出奖！-->" + number);
            System.out.println(prize);
        }
    }
}
