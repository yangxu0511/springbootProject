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

    // ==================== 带微信推送的兑奖方法 ====================

    /**
     * 兑奖并推送结果到微信
     * @param params 日期参数，为空则使用昨天日期
     * @return 兑奖结果摘要
     */
    public static RedeemResult redeemWithPush(String params) {
        RunPython.run();

        RedeemResult redeemResult = new RedeemResult();

        // 初始化彩票上下文
        LotteryContext context = initializeLotteryContext(params);
        if (!context.isValid()) {
            redeemResult.setError("无效的彩票上下文");
            return redeemResult;
        }

        redeemResult.setLotteryType(context.getLotteryType());
        redeemResult.setOpenDate(context.getOpenDate());
        redeemResult.setBuyDate(context.getBuyDate());

        // 处理开奖号码
        JSONObject openData = filterJson(context.getFilePath());
        String openNumber = openData.getString(context.getOpenDate());

        if (StrUtil.isEmpty(openNumber)) {
            redeemResult.setError("未获取到当天的中奖号码");
            return redeemResult;
        }

        redeemResult.setWinningNumber(openNumber);
        parseWinningNumbers(context, openNumber);

        // 处理已购买号码（当天）
        JSONObject hisJson = filterJson(Constants.getHisFilePath());
        String boughtNumbers = hisJson.getString(context.getBuyDate());

        if (StrUtil.isEmpty(boughtNumbers)) {
            redeemResult.setNoPurchase(true);
        } else {
            processNumberSetWithResult(context, boughtNumbers, redeemResult);
        }

        // 处理历史号码中大奖检查
        processHistoricalNumbersWithResult(context, hisJson, redeemResult);

        return redeemResult;
    }

    /**
     * 处理历史号码并收集中大奖信息（只收集4等奖以上）
     */
    private static void processHistoricalNumbersWithResult(LotteryContext context, JSONObject hisJson, RedeemResult redeemResult) {
        for (String date : hisJson.keySet()) {
            // 跳过当天购买的号码（已经在上面处理过了）
            if (date.equals(context.getBuyDate())) {
                continue;
            }

            String numbers = hisJson.getString(date);
            for (String number : numbers.split("\\|")) {
                if (!isValidNumberFormat(number, context.getBlueSize())) {
                    continue;
                }

                MatchResult matchResult = calculateMatchResult(context, number);
                String prize = getPrizeInfo(context.getLotteryType(), matchResult);

                // 只收集4等奖以上的历史号码
                if (prize != null && isFourthPrizeOrAbove(context.getLotteryType(), matchResult)) {
                    RedeemResult.HistoricalWin hw = new RedeemResult.HistoricalWin();
                    hw.setDate(date);
                    hw.setNumber(number);
                    hw.setRedMatch(matchResult.getRedCount());
                    hw.setBlueMatch(matchResult.getBlueCount());
                    hw.setPrize(prize);
                    hw.setWon(true);
                    redeemResult.addHistoricalWin(hw);
                }
            }
        }
    }

    /**
     * 判断是否为4等奖以上
     * 大乐透: 一等奖(5-2), 二等奖(5-1), 三等奖(5-0), 四等奖(4-2)
     * 双色球: 一等奖(6-1), 二等奖(6-0), 三等奖(5-1), 四等奖(5-0, 4-1)
     */
    private static boolean isFourthPrizeOrAbove(String lotteryType, MatchResult result) {
        int red = result.getRedCount();
        int blue = result.getBlueCount();
        
        if ("tc".equals(lotteryType)) {
            // 大乐透4等奖以上: 5-2, 5-1, 5-0, 4-2
            return (red == 5) || (red == 4 && blue == 2);
        } else {
            // 双色球4等奖以上: 6-1, 6-0, 5-1, 5-0, 4-1
            return (red >= 5) || (red == 4 && blue == 1);
        }
    }

    /**
     * 处理号码集合并收集结果
     */
    private static void processNumberSetWithResult(LotteryContext context, String numbers, RedeemResult redeemResult) {
        String[] numberArray = numbers.split("\\|");

        for (String number : numberArray) {
            if (!isValidNumberFormat(number, context.getBlueSize())) {
                continue;
            }

            MatchResult matchResult = calculateMatchResult(context, number);
            String prize = getPrizeInfo(context.getLotteryType(), matchResult);

            RedeemResult.NumberResult nr = new RedeemResult.NumberResult();
            nr.setNumber(number);
            nr.setRedMatch(matchResult.getRedCount());
            nr.setBlueMatch(matchResult.getBlueCount());
            nr.setPrize(prize);
            nr.setWon(prize != null);

            redeemResult.addNumberResult(nr);
        }
    }

    /**
     * 兑奖结果类
     */
    public static class RedeemResult {
        private String lotteryType;
        private String openDate;
        private String buyDate;
        private String winningNumber;
        private String error;
        private boolean noPurchase = false;
        private List<NumberResult> numberResults = new ArrayList<>();
        private List<HistoricalWin> historicalWins = new ArrayList<>();

        public boolean hasWinning() {
            return numberResults.stream().anyMatch(NumberResult::isWon);
        }

        public boolean hasHistoricalWinning() {
            return historicalWins.stream().anyMatch(HistoricalWin::isWon);
        }

        public String getLotteryTypeName() {
            return "tc".equals(lotteryType) ? "大乐透" : "双色球";
        }

        // Getters and Setters
        public String getLotteryType() { return lotteryType; }
        public void setLotteryType(String lotteryType) { this.lotteryType = lotteryType; }
        public String getOpenDate() { return openDate; }
        public void setOpenDate(String openDate) { this.openDate = openDate; }
        public String getBuyDate() { return buyDate; }
        public void setBuyDate(String buyDate) { this.buyDate = buyDate; }
        public String getWinningNumber() { return winningNumber; }
        public void setWinningNumber(String winningNumber) { this.winningNumber = winningNumber; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public boolean isNoPurchase() { return noPurchase; }
        public void setNoPurchase(boolean noPurchase) { this.noPurchase = noPurchase; }
        public List<NumberResult> getNumberResults() { return numberResults; }
        public void addNumberResult(NumberResult nr) { this.numberResults.add(nr); }
        public List<HistoricalWin> getHistoricalWins() { return historicalWins; }
        public void addHistoricalWin(HistoricalWin hw) { this.historicalWins.add(hw); }

        /**
         * 单注号码结果
         */
        public static class NumberResult {
            private String number;
            private int redMatch;
            private int blueMatch;
            private String prize;
            private boolean won;

            public String getNumber() { return number; }
            public void setNumber(String number) { this.number = number; }
            public int getRedMatch() { return redMatch; }
            public void setRedMatch(int redMatch) { this.redMatch = redMatch; }
            public int getBlueMatch() { return blueMatch; }
            public void setBlueMatch(int blueMatch) { this.blueMatch = blueMatch; }
            public String getPrize() { return prize; }
            public void setPrize(String prize) { this.prize = prize; }
            public boolean isWon() { return won; }
            public void setWon(boolean won) { this.won = won; }
        }

        /**
         * 历史号码中奖信息
         */
        public static class HistoricalWin {
            private String date;
            private String number;
            private int redMatch;
            private int blueMatch;
            private String prize;
            private boolean won;

            public String getDate() { return date; }
            public void setDate(String date) { this.date = date; }
            public String getNumber() { return number; }
            public void setNumber(String number) { this.number = number; }
            public int getRedMatch() { return redMatch; }
            public void setRedMatch(int redMatch) { this.redMatch = redMatch; }
            public int getBlueMatch() { return blueMatch; }
            public void setBlueMatch(int blueMatch) { this.blueMatch = blueMatch; }
            public String getPrize() { return prize; }
            public void setPrize(String prize) { this.prize = prize; }
            public boolean isWon() { return won; }
            public void setWon(boolean won) { this.won = won; }
        }
    }
}

