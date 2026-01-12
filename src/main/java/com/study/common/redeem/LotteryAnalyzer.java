package com.study.common.redeem;

import com.alibaba.fastjson.JSONObject;
import com.study.common.base.AppBaseNum;
import com.study.common.base.Constants;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 彩票号码分析器
 * 提供奇偶比、大小号比例、冷热号、跨度、和值等统计分析功能
 * 
 * @author yangxu
 * @since 2026/01/12
 */
public class LotteryAnalyzer extends AppBaseNum {

    /**
     * 彩票类型配置
     */
    public enum LotteryType {
        TC("tc", 35, 12, 5, 2, 17),  // 大乐透: 1-35选5 + 1-12选2, 18-35为大号
        FC("fc", 33, 16, 6, 1, 16);  // 双色球: 1-33选6 + 1-16选1, 17-33为大号

        public final String code;
        public final int maxRed;
        public final int maxBlue;
        public final int redCount;
        public final int blueCount;
        public final int smallMaxNum;  // 小号最大值（含）

        LotteryType(String code, int maxRed, int maxBlue, int redCount, int blueCount, int smallMaxNum) {
            this.code = code;
            this.maxRed = maxRed;
            this.maxBlue = maxBlue;
            this.redCount = redCount;
            this.blueCount = blueCount;
            this.smallMaxNum = smallMaxNum;
        }

        public static LotteryType fromCode(String code) {
            return Arrays.stream(values())
                    .filter(type -> type.code.equals(code))
                    .findFirst()
                    .orElse(null);
        }
    }

    // ==================== 奇偶比分析 ====================

    /**
     * 分析历史开奖号码的奇偶比分布
     * @param lotteryType 彩票类型 tc/fc
     * @return Map<String, Double> 奇偶比 -> 出现频率（百分比）
     */
    public static Map<String, Double> analyzeOddEvenRatio(String lotteryType) {
        LotteryType type = LotteryType.fromCode(lotteryType);
        if (type == null) return Collections.emptyMap();

        String filePath = "tc".equals(lotteryType) ? Constants.getTcFilePath() : Constants.getFcFilePath();
        JSONObject historyData = filterJson(filePath);
        if (historyData == null || historyData.isEmpty()) return Collections.emptyMap();

        Map<String, Integer> ratioCount = new HashMap<>();
        int totalCount = 0;

        for (String key : historyData.keySet()) {
            String value = historyData.getString(key);
            List<Integer> redNumbers = parseRedNumbers(value, type.redCount);
            String ratio = calculateOddEvenRatio(redNumbers);
            ratioCount.merge(ratio, 1, Integer::sum);
            totalCount++;
        }

        final int total = totalCount;
        return ratioCount.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Math.round(e.getValue() * 10000.0 / total) / 100.0
                ));
    }

    /**
     * 获取高频奇偶比（出现频率>=阈值的奇偶比）
     */
    public static List<String> getHighFrequencyOddEvenRatios(String lotteryType) {
        Map<String, Double> ratioMap = analyzeOddEvenRatio(lotteryType);
        double threshold = Constants.ODD_EVEN_THRESHOLD;
        return ratioMap.entrySet().stream()
                .filter(e -> e.getValue() >= threshold)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 验证号码的奇偶比是否为高频比例
     */
    public static boolean isValidOddEvenRatio(Set<Integer> numbers, String lotteryType) {
        List<String> highFreqRatios = getHighFrequencyOddEvenRatios(lotteryType);
        if (highFreqRatios.isEmpty()) return true; // 没有统计数据时放行

        String ratio = calculateOddEvenRatio(new ArrayList<>(numbers));
        return highFreqRatios.contains(ratio);
    }

    /**
     * 计算奇偶比
     * @return 格式如 "3:3" 或 "2:3"
     */
    private static String calculateOddEvenRatio(List<Integer> numbers) {
        int oddCount = (int) numbers.stream().filter(n -> n % 2 != 0).count();
        int evenCount = numbers.size() - oddCount;
        return oddCount + ":" + evenCount;
    }

    // ==================== 大小号比例分析 ====================

    /**
     * 分析历史开奖号码的大小号比例分布
     */
    public static Map<String, Double> analyzeBigSmallRatio(String lotteryType) {
        LotteryType type = LotteryType.fromCode(lotteryType);
        if (type == null) return Collections.emptyMap();

        String filePath = "tc".equals(lotteryType) ? Constants.getTcFilePath() : Constants.getFcFilePath();
        JSONObject historyData = filterJson(filePath);
        if (historyData == null || historyData.isEmpty()) return Collections.emptyMap();

        Map<String, Integer> ratioCount = new HashMap<>();
        int totalCount = 0;

        for (String key : historyData.keySet()) {
            String value = historyData.getString(key);
            List<Integer> redNumbers = parseRedNumbers(value, type.redCount);
            String ratio = calculateBigSmallRatio(redNumbers, type.smallMaxNum);
            ratioCount.merge(ratio, 1, Integer::sum);
            totalCount++;
        }

        final int total = totalCount;
        return ratioCount.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Math.round(e.getValue() * 10000.0 / total) / 100.0
                ));
    }

    /**
     * 获取高频大小号比例
     */
    public static List<String> getHighFrequencyBigSmallRatios(String lotteryType) {
        Map<String, Double> ratioMap = analyzeBigSmallRatio(lotteryType);
        double threshold = Constants.BIG_SMALL_THRESHOLD;
        return ratioMap.entrySet().stream()
                .filter(e -> e.getValue() >= threshold)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 验证号码的大小号比例是否为高频比例
     */
    public static boolean isValidBigSmallRatio(Set<Integer> numbers, String lotteryType) {
        LotteryType type = LotteryType.fromCode(lotteryType);
        if (type == null) return true;

        List<String> highFreqRatios = getHighFrequencyBigSmallRatios(lotteryType);
        if (highFreqRatios.isEmpty()) return true;

        String ratio = calculateBigSmallRatio(new ArrayList<>(numbers), type.smallMaxNum);
        return highFreqRatios.contains(ratio);
    }

    /**
     * 计算大小号比例
     * @return 格式如 "小:大" = "3:3"
     */
    private static String calculateBigSmallRatio(List<Integer> numbers, int smallMaxNum) {
        int smallCount = (int) numbers.stream().filter(n -> n <= smallMaxNum).count();
        int bigCount = numbers.size() - smallCount;
        return smallCount + ":" + bigCount;
    }

    // ==================== 冷热号分析 ====================

    /**
     * 获取热号（最近N期出现频率高的号码）
     * @param lotteryType 彩票类型
     * @param topN 返回前N个热号
     * @return 热号列表
     */
    public static List<Integer> getHotNumbers(String lotteryType, int topN) {
        LotteryType type = LotteryType.fromCode(lotteryType);
        if (type == null) return Collections.emptyList();

        String filePath = "tc".equals(lotteryType) ? Constants.getTcFilePath() : Constants.getFcFilePath();
        JSONObject historyData = filterJson(filePath);
        if (historyData == null || historyData.isEmpty()) return Collections.emptyList();

        // 按日期排序，取最近N期
        List<String> sortedKeys = historyData.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .limit(Constants.HOT_COLD_PERIOD)
                .collect(Collectors.toList());

        Map<Integer, Integer> numberCount = new HashMap<>();
        for (String key : sortedKeys) {
            String value = historyData.getString(key);
            List<Integer> redNumbers = parseRedNumbers(value, type.redCount);
            for (Integer num : redNumbers) {
                numberCount.merge(num, 1, Integer::sum);
            }
        }

        return numberCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取冷号（最近N期出现频率低或未出现的号码）
     */
    public static List<Integer> getColdNumbers(String lotteryType, int topN) {
        LotteryType type = LotteryType.fromCode(lotteryType);
        if (type == null) return Collections.emptyList();

        String filePath = "tc".equals(lotteryType) ? Constants.getTcFilePath() : Constants.getFcFilePath();
        JSONObject historyData = filterJson(filePath);
        if (historyData == null || historyData.isEmpty()) return Collections.emptyList();

        List<String> sortedKeys = historyData.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .limit(Constants.HOT_COLD_PERIOD)
                .collect(Collectors.toList());

        Map<Integer, Integer> numberCount = new HashMap<>();
        // 初始化所有号码为0
        for (int i = 1; i <= type.maxRed; i++) {
            numberCount.put(i, 0);
        }

        for (String key : sortedKeys) {
            String value = historyData.getString(key);
            List<Integer> redNumbers = parseRedNumbers(value, type.redCount);
            for (Integer num : redNumbers) {
                numberCount.merge(num, 1, Integer::sum);
            }
        }

        return numberCount.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 验证号码中热号和冷号的比例是否合理
     * 热号应占60-70%，冷号占30-40%
     */
    public static boolean isValidHotColdRatio(Set<Integer> numbers, String lotteryType) {
        List<Integer> hotNumbers = getHotNumbers(lotteryType, 15);
        if (hotNumbers.isEmpty()) return true;

        int hotCount = (int) numbers.stream().filter(hotNumbers::contains).count();
        double hotRatio = (double) hotCount / numbers.size();

        // 热号占比应在40%-80%之间（范围稍宽，避免过度限制）
        return hotRatio >= 0.4 && hotRatio <= 0.8;
    }

    // ==================== 跨度分析 ====================

    /**
     * 计算号码跨度
     */
    public static int calculateSpan(Collection<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) return 0;
        int max = Collections.max(numbers);
        int min = Collections.min(numbers);
        return max - min;
    }

    /**
     * 验证跨度是否在合理范围内
     */
    public static boolean isValidSpan(Set<Integer> numbers, String lotteryType) {
        int span = calculateSpan(numbers);
        if ("tc".equals(lotteryType)) {
            return span >= Constants.DLT_SPAN_MIN && span <= Constants.DLT_SPAN_MAX;
        } else {
            return span >= Constants.SSQ_SPAN_MIN && span <= Constants.SSQ_SPAN_MAX;
        }
    }

    // ==================== 和值分析 ====================

    /**
     * 计算号码和值
     */
    public static int calculateSum(Collection<Integer> numbers) {
        return numbers.stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * 验证和值是否在合理范围内
     */
    public static boolean isValidSum(Set<Integer> numbers, String lotteryType) {
        int sum = calculateSum(numbers);
        if ("tc".equals(lotteryType)) {
            return sum >= Constants.DLT_SUM_MIN && sum <= Constants.DLT_SUM_MAX;
        } else {
            return sum >= Constants.SSQ_SUM_MIN && sum <= Constants.SSQ_SUM_MAX;
        }
    }

    // ==================== 连号分析 ====================

    /**
     * 计算连号组数
     */
    public static int countConsecutiveGroups(Set<Integer> numbers) {
        List<Integer> sorted = numbers.stream().sorted().collect(Collectors.toList());
        int groups = 0;
        boolean inGroup = false;

        for (int i = 0; i < sorted.size() - 1; i++) {
            if (sorted.get(i + 1) - sorted.get(i) == 1) {
                if (!inGroup) {
                    groups++;
                    inGroup = true;
                }
            } else {
                inGroup = false;
            }
        }
        return groups;
    }

    /**
     * 验证连号数量是否合理（0-2组连号）
     */
    public static boolean isValidConsecutive(Set<Integer> numbers) {
        int groups = countConsecutiveGroups(numbers);
        return groups <= 2;
    }

    // ==================== 综合验证 ====================

    /**
     * 综合验证号码是否符合所有筛选规则
     * @param numbers 红球号码
     * @param lotteryType 彩票类型
     * @param enabledRules 启用的规则集合
     * @return 验证结果
     */
    public static ValidationResult validateNumbers(Set<Integer> numbers, String lotteryType, Set<FilterRule> enabledRules) {
        ValidationResult result = new ValidationResult();

        if (enabledRules.contains(FilterRule.ODD_EVEN)) {
            if (!isValidOddEvenRatio(numbers, lotteryType)) {
                result.addFailedRule(FilterRule.ODD_EVEN, "奇偶比不在高频范围内");
            }
        }

        if (enabledRules.contains(FilterRule.BIG_SMALL)) {
            if (!isValidBigSmallRatio(numbers, lotteryType)) {
                result.addFailedRule(FilterRule.BIG_SMALL, "大小号比例不在高频范围内");
            }
        }

        if (enabledRules.contains(FilterRule.HOT_COLD)) {
            if (!isValidHotColdRatio(numbers, lotteryType)) {
                result.addFailedRule(FilterRule.HOT_COLD, "热号比例不在合理范围(40%-80%)");
            }
        }

        if (enabledRules.contains(FilterRule.SPAN)) {
            if (!isValidSpan(numbers, lotteryType)) {
                result.addFailedRule(FilterRule.SPAN, "跨度不在合理范围内");
            }
        }

        if (enabledRules.contains(FilterRule.SUM)) {
            if (!isValidSum(numbers, lotteryType)) {
                result.addFailedRule(FilterRule.SUM, "和值不在合理范围内");
            }
        }

        if (enabledRules.contains(FilterRule.CONSECUTIVE)) {
            if (!isValidConsecutive(numbers)) {
                result.addFailedRule(FilterRule.CONSECUTIVE, "连号组数超过2组");
            }
        }

        return result;
    }

    // ==================== 统计报告 ====================

    /**
     * 输出历史数据的完整统计分析报告
     */
    public static void printAnalysisReport(String lotteryType) {
        String typeName = "tc".equals(lotteryType) ? "大乐透" : "双色球";
        System.out.println("\n========== " + typeName + " 历史数据统计分析报告 ==========\n");

        // 奇偶比统计
        System.out.println("【奇偶比分布】");
        Map<String, Double> oddEvenRatio = analyzeOddEvenRatio(lotteryType);
        oddEvenRatio.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(e -> System.out.printf("  %s : %.2f%%\n", e.getKey(), e.getValue()));

        List<String> highFreqOddEven = getHighFrequencyOddEvenRatios(lotteryType);
        System.out.println("  高频奇偶比 (>=" + Constants.ODD_EVEN_THRESHOLD + "%): " + highFreqOddEven);

        // 大小号比例统计
        System.out.println("\n【大小号比例分布】");
        Map<String, Double> bigSmallRatio = analyzeBigSmallRatio(lotteryType);
        bigSmallRatio.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(e -> System.out.printf("  %s : %.2f%%\n", e.getKey(), e.getValue()));

        List<String> highFreqBigSmall = getHighFrequencyBigSmallRatios(lotteryType);
        System.out.println("  高频大小号比例 (>=" + Constants.BIG_SMALL_THRESHOLD + "%): " + highFreqBigSmall);

        // 冷热号统计
        System.out.println("\n【冷热号分析】(最近" + Constants.HOT_COLD_PERIOD + "期)");
        List<Integer> hotNumbers = getHotNumbers(lotteryType, 10);
        List<Integer> coldNumbers = getColdNumbers(lotteryType, 10);
        System.out.println("  热号(前10): " + hotNumbers);
        System.out.println("  冷号(前10): " + coldNumbers);

        System.out.println("\n==================================================\n");
    }

    // ==================== 辅助方法 ====================

    private static List<Integer> parseRedNumbers(String value, int redSize) {
        String[] parts = value.split("\\|");
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < Math.min(parts.length, redSize); i++) {
            numbers.add(Integer.parseInt(parts[i]));
        }
        return numbers;
    }

    /**
     * 筛选规则枚举
     */
    public enum FilterRule {
        ODD_EVEN("奇偶比"),
        BIG_SMALL("大小号比例"),
        HOT_COLD("冷热号"),
        SPAN("跨度"),
        SUM("和值"),
        CONSECUTIVE("连号");

        private final String name;

        FilterRule(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final Map<FilterRule, String> failedRules = new LinkedHashMap<>();

        public void addFailedRule(FilterRule rule, String reason) {
            failedRules.put(rule, reason);
        }

        public boolean isValid() {
            return failedRules.isEmpty();
        }

        public Map<FilterRule, String> getFailedRules() {
            return failedRules;
        }

        @Override
        public String toString() {
            if (isValid()) {
                return "验证通过";
            }
            StringBuilder sb = new StringBuilder("验证失败: ");
            failedRules.forEach((rule, reason) -> 
                sb.append("\n  - ").append(rule.getName()).append(": ").append(reason)
            );
            return sb.toString();
        }
    }
}
