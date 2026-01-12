package com.study.common.redeem;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.study.common.base.AppBaseNum;
import com.study.common.base.Constants;
import com.study.common.redeem.LotteryAnalyzer.FilterRule;
import com.study.common.redeem.LotteryAnalyzer.LotteryType;
import com.study.common.redeem.LotteryAnalyzer.ValidationResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 彩票号码生成器
 * 支持智能筛选：奇偶比、大小号比例、冷热号、跨度、和值、连号等规则
 * 
 * @author yangxu
 * @since 2023-10-26
 * @update 2026-01-12 添加智能筛选功能
 */
public class DreamNumer extends AppBaseNum {

    private static final Random RANDOM = new Random();

    /**
     * 根据当前日期自动判断彩票类型并生成号码
     */
    public static void getDreamNum() {
        String zjType = getZjType();
        if (StrUtil.isEmpty(zjType)) {
            System.out.println("今天不是开奖日！");
            return;
        }
        getDreamNum(zjType);
    }

    /**
     * 指定类型生成号码
     * @param zjType tc=大乐透, fc=双色球
     */
    public static void getDreamNum(String zjType) {
        LotteryType type = LotteryType.fromCode(zjType);
        if (type == null) {
            System.out.println("无效类型！请使用 tc(大乐透) 或 fc(双色球)");
            return;
        }

        // 打印分析报告
        LotteryAnalyzer.printAnalysisReport(zjType);

        // 生成符合筛选规则的号码
        Set<Integer> redNumbers = generateSmartRedNumbers(type);
        Set<Integer> blueNumbers = generateNumbers(1, type.maxBlue, type.blueCount);

        // 打印筛选信息
        printFilterInfo(redNumbers, zjType);

        // 处理生成的号码
        processGeneratedNumbers(redNumbers, blueNumbers, type);
    }

    /**
     * 获取当前日期对应的彩票类型
     */
    private static String getZjType() {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        
        // 大乐透: 周一(1)、周三(3)、周五(5)、周六(6)
        if (currentDay == 1 || currentDay == 3 || currentDay == 5 || currentDay == 6) {
            return "tc";
        }
        // 双色球: 周二(2)、周四(4)、周日(0)
        if (currentDay == 2 || currentDay == 4 || currentDay == 0) {
            return "fc";
        }
        return "";
    }

    /**
     * 静默生成号码并返回（不需要用户交互，用于自动推送）
     * @return 生成的号码字符串，格式如 "01,05,11,22,28,32 08"
     */
    public static String generateAndReturnNumber() {
        String zjType = getZjType();
        if (StrUtil.isEmpty(zjType)) {
            return null;
        }
        return generateAndReturnNumber(zjType);
    }

    /**
     * 指定类型静默生成号码并返回
     * @param zjType tc=大乐透, fc=双色球
     * @return 生成的号码字符串
     */
    public static String generateAndReturnNumber(String zjType) {
        LotteryType type = LotteryType.fromCode(zjType);
        if (type == null) {
            return null;
        }

        String jsonFilePath = "tc".equals(type.code) ? Constants.getTcFilePath() : Constants.getFcFilePath();
        JSONObject historyData = filterJson(jsonFilePath);

        // 最多尝试10次生成不重复的号码
        for (int attempt = 0; attempt < 10; attempt++) {
            Set<Integer> redNumbers = generateSmartRedNumbers(type);
            Set<Integer> blueNumbers = generateNumbers(1, type.maxBlue, type.blueCount);
            
            String formattedNumbers = formatLotteryNumbers(redNumbers, blueNumbers);
            
            // 检查是否与历史重复
            if (!isNumberExists(formattedNumbers, historyData)) {
                String displayNumbers = formatDisplayNumbers(redNumbers, blueNumbers);
                // 写入历史记录
                WriteNum.writeMyNumber(displayNumbers);
                return displayNumbers;
            }
        }
        
        // 10次都重复，强制返回最后一次
        Set<Integer> redNumbers = generateSmartRedNumbers(type);
        Set<Integer> blueNumbers = generateNumbers(1, type.maxBlue, type.blueCount);
        String displayNumbers = formatDisplayNumbers(redNumbers, blueNumbers);
        WriteNum.writeMyNumber(displayNumbers);
        return displayNumbers;
    }

    // ==================== 智能号码生成 ====================

    /**
     * 生成符合筛选规则的红球号码
     */
    private static Set<Integer> generateSmartRedNumbers(LotteryType type) {
        Set<FilterRule> enabledRules = getEnabledFilterRules();
        
        for (int retry = 0; retry < Constants.MAX_RETRY_COUNT; retry++) {
            Set<Integer> numbers = generateNumbers(1, type.maxRed, type.redCount);
            ValidationResult result = LotteryAnalyzer.validateNumbers(numbers, type.code, enabledRules);
            
            if (result.isValid()) {
                if (retry > 0) {
                    System.out.println("经过 " + (retry + 1) + " 次筛选，生成符合规则的号码");
                }
                return numbers;
            }
        }
        
        // 超过重试次数，返回最后一次生成的号码（并给出提示）
        System.out.println("⚠️ 警告: 达到最大重试次数(" + Constants.MAX_RETRY_COUNT + ")，使用当前号码");
        return generateNumbers(1, type.maxRed, type.redCount);
    }

    /**
     * 获取已启用的筛选规则
     */
    private static Set<FilterRule> getEnabledFilterRules() {
        Set<FilterRule> rules = new HashSet<>();
        if (Constants.ENABLE_ODD_EVEN_FILTER) rules.add(FilterRule.ODD_EVEN);
        if (Constants.ENABLE_BIG_SMALL_FILTER) rules.add(FilterRule.BIG_SMALL);
        if (Constants.ENABLE_HOT_COLD_FILTER) rules.add(FilterRule.HOT_COLD);
        if (Constants.ENABLE_SPAN_FILTER) rules.add(FilterRule.SPAN);
        if (Constants.ENABLE_SUM_FILTER) rules.add(FilterRule.SUM);
        if (Constants.ENABLE_CONSECUTIVE_FILTER) rules.add(FilterRule.CONSECUTIVE);
        return rules;
    }

    /**
     * 生成指定范围内的随机号码
     */
    private static Set<Integer> generateNumbers(int min, int max, int count) {
        Set<Integer> numbers = new HashSet<>();
        while (numbers.size() < count) {
            int num = RANDOM.nextInt(max - min + 1) + min;
            numbers.add(num);
        }
        return numbers;
    }

    /**
     * 打印筛选信息
     */
    private static void printFilterInfo(Set<Integer> numbers, String lotteryType) {
        System.out.println("\n【生成号码筛选信息】");
        
        // 奇偶比
        int oddCount = (int) numbers.stream().filter(n -> n % 2 != 0).count();
        int evenCount = numbers.size() - oddCount;
        System.out.println("  奇偶比: " + oddCount + ":" + evenCount);
        
        // 大小号比例
        LotteryType type = LotteryType.fromCode(lotteryType);
        int smallCount = (int) numbers.stream().filter(n -> n <= type.smallMaxNum).count();
        int bigCount = numbers.size() - smallCount;
        System.out.println("  大小号比: " + smallCount + ":" + bigCount + " (小号≤" + type.smallMaxNum + ")");
        
        // 跨度
        int span = LotteryAnalyzer.calculateSpan(numbers);
        System.out.println("  跨度: " + span);
        
        // 和值
        int sum = LotteryAnalyzer.calculateSum(numbers);
        System.out.println("  和值: " + sum);
        
        // 连号组数
        int consecutive = LotteryAnalyzer.countConsecutiveGroups(numbers);
        System.out.println("  连号组数: " + consecutive);
        
        // 热号命中
        List<Integer> hotNumbers = LotteryAnalyzer.getHotNumbers(lotteryType, 15);
        int hotCount = (int) numbers.stream().filter(hotNumbers::contains).count();
        System.out.println("  热号命中: " + hotCount + "/" + numbers.size() + " (" + 
                String.format("%.1f", hotCount * 100.0 / numbers.size()) + "%)");
    }

    // ==================== 号码处理 ====================

    /**
     * 处理生成的号码
     */
    private static void processGeneratedNumbers(Set<Integer> redNumbers, Set<Integer> blueNumbers, LotteryType type) {
        String jsonFilePath = "tc".equals(type.code) ? Constants.getTcFilePath() : Constants.getFcFilePath();

        String formattedNumbers = formatLotteryNumbers(redNumbers, blueNumbers);
        String displayNumbers = formatDisplayNumbers(redNumbers, blueNumbers);

        try {
            JSONObject historyData = filterJson(jsonFilePath);
            if (isNumberExists(formattedNumbers, historyData)) {
                System.out.println("号码跟历史开奖重复，重新生成... ^^ " + displayNumbers);
                getDreamNum(type.code);
                return;
            }

            System.out.println("\n✨ 今晚的中奖号码历史未出现，请查收您的一千万中奖号码^^ " + displayNumbers);

            // 转换号码格式用于比较
            List<Integer> redList = new ArrayList<>(redNumbers);
            List<Integer> blueList = new ArrayList<>(blueNumbers);
            Collections.sort(redList);
            Collections.sort(blueList);

            // 检查相似号码
            Map<String, String> similarNumber = comparisonNum(redList, blueList, type.redCount, type.blueCount, historyData);
            if (!similarNumber.isEmpty()) {
                similarNumber.forEach((key, value) -> {
                    System.out.println("存在相似个数: " + key + ", 号码: " + value);
                });
                handleUserInput(displayNumbers, redList, blueList, type.redCount, type.blueCount, historyData, type.code);
            } else {
                WriteNum.writeMyNumber(displayNumbers);
                comparisonOpenNum(redList, blueList, type.redCount, type.blueCount, historyData);
            }

        } catch (Exception e) {
            System.out.println("系统出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理用户输入
     */
    private static void handleUserInput(String numbers, List<Integer> redList, List<Integer> blueList,
                                        int redSize, int blueSize, JSONObject historyData, String lotteryType) {
        System.out.print("请在控制台输入yes/y(需要）或no/n(不需要）来确定是否需要这注号码：");
        Scanner scanner = new Scanner(System.in);
        boolean validInput = false;

        while (!validInput) {
            String input = scanner.nextLine().toLowerCase();
            if (input.matches("no|n|不需要")) {
                System.out.println("您输入了no，重新生成号码...");
                WriteNum.writeNotBuyNumber(numbers);
                getDreamNum(lotteryType);
                validInput = true;
            } else if (input.matches("yes|y|需要")) {
                System.out.println("您输入了yes，不再重新生成号码...");
                WriteNum.writeMyNumber(numbers);
                comparisonOpenNum(redList, blueList, redSize, blueSize, historyData);
                validInput = true;
            } else {
                System.out.println("输入无效，请重新输入！");
            }
        }
    }

    // ==================== 格式化工具 ====================

    /**
     * 格式化号码用于存储（使用|分隔）
     */
    private static String formatLotteryNumbers(Set<Integer> redNumbers, Set<Integer> blueNumbers) {
        String redPart = redNumbers.stream()
                .sorted()
                .map(num -> String.format("%02d", num))
                .collect(Collectors.joining("|"));

        String bluePart = blueNumbers.stream()
                .sorted()
                .map(num -> String.format("%02d", num))
                .collect(Collectors.joining("|"));

        return redPart + "|" + bluePart;
    }

    /**
     * 格式化号码用于显示（使用逗号和空格分隔）
     */
    private static String formatDisplayNumbers(Set<Integer> redNumbers, Set<Integer> blueNumbers) {
        String redPart = redNumbers.stream()
                .sorted()
                .map(num -> String.format("%02d", num))
                .collect(Collectors.joining(","));

        String bluePart = blueNumbers.stream()
                .sorted()
                .map(num -> String.format("%02d", num))
                .collect(Collectors.joining(","));

        return redPart + " " + bluePart;
    }

    /**
     * 检查号码是否存在于历史记录中
     */
    private static boolean isNumberExists(String numbers, JSONObject historyData) {
        if (historyData == null) return false;
        return historyData.values().stream()
                .anyMatch(value -> numbers.equals(value.toString()));
    }

    // ==================== 比较方法 ====================

    /**
     * 比较号码相似度
     */
    public static Map<String, String> comparisonNum(List<Integer> redList, List<Integer> blueList,
                                                    int redSize, int blueSize, JSONObject historyData) {
        Map<String, String> result = new HashMap<>();
        if (historyData == null) return result;

        for (String key : historyData.keySet()) {
            String value = historyData.getString(key);
            String[] parts = value.split("\\|");

            List<Integer> historyRed = new ArrayList<>();
            List<Integer> historyBlue = new ArrayList<>();

            for (int i = 0; i < parts.length; i++) {
                int num = Integer.parseInt(parts[i]);
                if (i < redSize) {
                    historyRed.add(num);
                } else {
                    historyBlue.add(num);
                }
            }

            int redMatch = (int) redList.stream().filter(historyRed::contains).count();
            int blueMatch = (int) blueList.stream().filter(historyBlue::contains).count();

            if (redMatch + blueMatch >= Constants.similarSize) {
                String formattedDate = formatDateDisplay(key);
                result.put("总共" + (redMatch + blueMatch) + "个相同号码, 红球相同" + redMatch + "个" + (blueMatch > 0 ? ",蓝球相同" + blueMatch + "个" : ""),
                        formatHistoryNumbers(historyRed, historyBlue) + " (日期: " + formattedDate + ")");
            }
        }
        return result;
    }

    /**
     * 格式化日期显示
     */
    private static String formatDateDisplay(String dateStr) {
        if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return dateStr;
        } else if (dateStr.matches("\\d{8}")) {
            return dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8);
        }
        return dateStr;
    }

    /**
     * 格式化历史号码
     */
    private static String formatHistoryNumbers(List<Integer> red, List<Integer> blue) {
        String redPart = red.stream()
                .map(num -> String.format("%02d", num))
                .collect(Collectors.joining(","));
        String bluePart = blue.stream()
                .map(num -> String.format("%02d", num))
                .collect(Collectors.joining(","));
        return redPart + " " + bluePart;
    }

    /**
     * 比较开奖号码
     */
    public static void comparisonOpenNum(List<Integer> redList, List<Integer> blueList,
                                         int redSize, int blueSize, JSONObject historyData) {
        if (historyData == null) return;

        String latestKey = historyData.keySet().stream()
                .max(Comparator.naturalOrder())
                .orElse(null);

        if (latestKey != null) {
            String latestValue = historyData.getString(latestKey);
            String[] parts = latestValue.split("\\|");

            List<Integer> openRed = new ArrayList<>();
            List<Integer> openBlue = new ArrayList<>();

            for (int i = 0; i < parts.length; i++) {
                int num = Integer.parseInt(parts[i]);
                if (i < redSize) {
                    openRed.add(num);
                } else {
                    openBlue.add(num);
                }
            }

            int redMatch = (int) redList.stream().filter(openRed::contains).count();
            int blueMatch = (int) blueList.stream().filter(openBlue::contains).count();

            String formattedDate = formatDateDisplay(latestKey);
            
            System.out.println("\n【与上期开奖号码对比】");
            System.out.println("上期开奖: " + formatHistoryNumbers(openRed, openBlue) + " (" + formattedDate + ")");
            System.out.println("红球匹配: " + redMatch + "个, 蓝球匹配: " + blueMatch + "个");
        }
    }
}
