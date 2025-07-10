package com.study.common.redeem;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.study.common.base.AppBaseNum;
import com.study.common.base.Constants;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 彩票号码生成器
 */
public class DreamNumer extends AppBaseNum {

    private static final Random RANDOM = new Random();
    private static final Set<Integer> redSet = new HashSet<>();
    private static final Set<Integer> blueSet = new HashSet<>();

    /**
     * 彩票类型配置
     */
    private enum LotteryType {
        TC("tc", 35, 12, 5, 2), // 大乐透: 1-35选5 + 1-12选2
        FC("fc", 33, 16, 6, 1); // 双色球: 1-33选6 + 1-16选1

        private final String code;        // 彩票代码
        private final int maxRed;         // 红球最大值
        private final int maxBlue;        // 蓝球最大值
        private final int redCount;       // 需要选择的红球数量
        private final int blueCount;      // 需要选择的蓝球数量

        LotteryType(String code, int maxRed, int maxBlue, int redCount, int blueCount) {
            this.code = code;
            this.maxRed = maxRed;
            this.maxBlue = maxBlue;
            this.redCount = redCount;
            this.blueCount = blueCount;
        }

        static LotteryType fromCode(String code) {
            return Arrays.stream(values())
                    .filter(type -> type.code.equals(code))
                    .findFirst()
                    .orElse(null);
        }
    }

    //1-35 1-12 1 3 6 5+2 大乐透
    //1-33 1-16 2 4 7 6+1 双色球
    public static void getDreamNum() {
        String zjType = getZjType();
        getDreamNum(zjType);
    }

    //1-35 1-12 1 3 6 5+2 大乐透
    //1-33 1-16 2 4 7 6+1 双色球
    public static void getDreamNum(String zjType) {
        redSet.clear();
        blueSet.clear();
        if (StrUtil.isNotEmpty(zjType)) {
            if ("tc".equals(zjType)) {
                tcDays();
            } else if ("fc".equals(zjType)) {
                fcDays();
            } else {
                System.out.println("无效类型！");
                return;
            }
        }
        filterData(zjType);
    }

    private static String getZjType() {
        Calendar calendar = Calendar.getInstance();
        String zjType = "";
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (currentDay == 1 || currentDay == 3 || currentDay == 5 || currentDay == 6) {
            tcDays();
            zjType = "tc";
        }
        if (currentDay == 2 || currentDay == 4 || currentDay == 0) {
            fcDays();
            zjType = "fc";
        }
        return zjType;
    }

    /**
     * @param zjType 中奖的开奖类型
     * @Author yangx
     * @Description 筛选历史中奖信息
     * @Since create in 2023年10月26日
     * @Company 广州云趣信息科技有限公司
     */
    private static void filterData(String zjType) {
        String jsonFilePath;
        int redSize;
        int blueSize;
        if ("tc".equals(zjType)) {
            jsonFilePath = Constants.getTcFilePath();
            redSize = 5;
            blueSize = 2;
        } else if ("fc".equals(zjType)) {
            jsonFilePath = Constants.getFcFilePath();
            redSize = 6;
            blueSize = 1;
        } else {
            System.out.println("无效类型！");
            return;
        }
        TreeSet<Integer> set = new TreeSet<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer t1, Integer t2) {
                //t1.compareTo(t2)  是从小到大正序排序,同理t2 . t1 就是倒序排序;
                return t1.compareTo(t2);
            }
        });
        set.addAll(redSet);
        String zjNum = "";
        zjNum = set.stream()
                .map(num -> {
                    return (num < 10) ? "0" + num : String.valueOf(num);
                })
                .collect(Collectors.joining("|"));
        set.clear();
        set.addAll(blueSet);
        zjNum = zjNum + "|" + set.stream()
                .map(num -> {
                    return (num < 10) ? "0" + num : String.valueOf(num);
                })
                .collect(Collectors.joining("|"));
        try {
            JSONObject historyData = filterJson(jsonFilePath);
            boolean isExist = false;
            for (String key : historyData.keySet()) {
                if (zjNum.equals(historyData.get(key))) {
                    isExist = true;
                    break;
                }
            }
            if (isExist) {
                System.out.println("号码跟中奖重复啦，晚生成一步，我重新生成一个新的中奖号码 ^^ " + zjNum);
                getDreamNum(zjType);
            } else {
                set.clear();
                set.addAll(redSet);
                zjNum = set.stream()
                        .map(num -> {
                            return (num < 10) ? "0" + num : String.valueOf(num);
                        })
                        .collect(Collectors.joining(","));
                set.clear();
                set.addAll(blueSet);
                int index = 0;
                Iterator<Integer> blueIt2 = set.iterator();
                while (blueIt2.hasNext()) {
                    Integer num = blueIt2.next();
                    String txt = "";
                    if (num < 10) {
                        txt = "0" + num;
                    } else {
                        txt = num + "";
                    }
                    if (index == 0) {
                        zjNum = zjNum + " " + txt;
                    } else {
                        zjNum = zjNum + "," + txt;
                    }
                    index++;
                }
            }

            String a_redNum = zjNum.split("\\s")[0];
            String a_blueNum = zjNum.split("\\s")[1];
            // 从第一个字符串中提取数字
            List<Integer> a_redArr = Arrays.stream(a_redNum.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            List<Integer> a_blueArr = Arrays.stream(a_blueNum.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            System.out.println("今晚的中奖号码历史未出现 请查收您的一千万中奖号码^^ " + zjNum);
            //现在开始执行比对生成的号码在历史中奖信息中相似度
            Map<String, String> similarNumber = comparisonNum(a_redArr, a_blueArr, redSize, blueSize, historyData);
            if (!similarNumber.isEmpty()) {
                similarNumber.forEach((key, value) -> {
                    System.out.println("存在相似个数: " + key + ", 号码: " + value);
                });
                System.out.print("请在控制台输入yes/y(需要）或no/n(不需要）来确定是否需要这注号码：");
                boolean validInput = false;
                Scanner scanner = new Scanner(System.in);

                while (!validInput) {
                    String input = scanner.nextLine();
                    if ("no".equals(input) || "n".equals(input) || "N".equals(input) || "不需要".equals(input)) {
                        System.out.println("您输入了no，重新生成号码...");
                        WriteNum.writeNotBuyNumber(zjNum);
                        getDreamNum(zjType);
                        validInput = true; // 输入有效，退出循环
                    } else if ("yes".equals(input) || "y".equals(input) || "Y".equals(input) || "需要".equals(input)) {
                        System.out.println("您输入了yes，不再重新生成号码...");
                        // 把号码写入历史文件
                        WriteNum.writeMyNumber(zjNum);
                        comparisonOpenNum(a_redArr, a_blueArr, redSize, blueSize, historyData);
                        validInput = true; // 输入有效，退出循环
                    } else {
                        System.out.println("输入无效，请重新输入！");
                        // 继续循环等待有效输入
                    }
                }
            } else {
                WriteNum.writeMyNumber(zjNum);
                comparisonOpenNum(a_redArr, a_blueArr, redSize, blueSize, historyData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("系统崩溃了……" + e.getMessage());
        }
    }

    /**
     * 大乐透
     */
    private static void tcDays() {
        while (redSet.size() == 5 ? false : true) {
            int num = RANDOM.nextInt(36); ////1-35 1-12 1 3 6 5+2 大乐透
            if (num == 0) {
                continue;
            }
            redSet.add(num);
        }
        while (blueSet.size() == 2 ? false : true) {
            int num = RANDOM.nextInt(13);
            if (num == 0) {
                continue;
            }
            blueSet.add(num);
        }

    }

    /**
     * 双色球
     */
    private static void fcDays() {
        while (redSet.size() == 6 ? false : true) {//1-33 1-16 2 4 7 6+1 双色球
            int num = RANDOM.nextInt(34);
            if (num == 0) {
                continue;
            }
            redSet.add(num);
        }
        while (blueSet.size() == 1 ? false : true) {
            int num = RANDOM.nextInt(17);
            if (num == 0) {
                continue;
            }
            blueSet.add(num);
        }
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
     * 处理生成的号码
     */
    private static void processGeneratedNumbers(Set<Integer> redNumbers, Set<Integer> blueNumbers, LotteryType type) {
        String jsonFilePath = "tc".equals(type.code) ? Constants.getTcFilePath() : Constants.getFcFilePath();

        String formattedNumbers = formatLotteryNumbers(redNumbers, blueNumbers);
        String displayNumbers = formatDisplayNumbers(redNumbers, blueNumbers);

        try {
            JSONObject historyData = filterJson(jsonFilePath);
            if (isNumberExists(formattedNumbers, historyData)) {
                System.out.println("号码跟中奖重复啦，晚生成一步，我重新生成一个新的中奖号码 ^^ " + displayNumbers);
                getDreamNum(type.code);
                return;
            }

            System.out.println("今晚的中奖号码历史未出现 请查收您的一千万中奖号码^^ " + displayNumbers);

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
                handleUserInput(displayNumbers, redList, blueList, type.redCount, type.blueCount, historyData);
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
                                        int redSize, int blueSize, JSONObject historyData) {
        System.out.print("请在控制台输入yes/y(需要）或no/n(不需要）来确定是否需要这注号码：");
        Scanner scanner = new Scanner(System.in);
        boolean validInput = false;

        while (!validInput) {
            String input = scanner.nextLine().toLowerCase();
            if (input.matches("no|n|不需要")) {
                System.out.println("您输入了no，重新生成号码...");
                WriteNum.writeNotBuyNumber(numbers);
                getDreamNum();
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
        return historyData.values().stream()
                .anyMatch(value -> numbers.equals(value.toString()));
    }

    /**
     * 比较号码相似度
     */
    public static Map<String, String> comparisonNum(List<Integer> redList, List<Integer> blueList,
                                                    int redSize, int blueSize, JSONObject historyData) {
        Map<String, String> result = new HashMap<>();
        for (String key : historyData.keySet()) {
            String value = historyData.getString(key);
            String[] parts = value.split("\\|");

            List<Integer> historyRed = new ArrayList<>();
            List<Integer> historyBlue = new ArrayList<>();

            // 分离红球和蓝球
            for (int i = 0; i < parts.length; i++) {
                int num = Integer.parseInt(parts[i]);
                if (i < redSize) {
                    historyRed.add(num);
                } else {
                    historyBlue.add(num);
                }
            }

            // 计算相同号码数量
            int redMatch = (int) redList.stream().filter(historyRed::contains).count();
            int blueMatch = (int) blueList.stream().filter(historyBlue::contains).count();

            // 如果相似度较高，添加到结果中
            if (redMatch + blueMatch >= Constants.similarSize) {
                // 格式化日期显示
                String formattedDate = formatDateDisplay(key);
                result.put("总共" + (redMatch + blueMatch) + "个相同号码, 红球相同" + redMatch + "个" + (blueMatch > 0 ? ",蓝球相同" + blueMatch + "个" : ""),
                        formatHistoryNumbers(historyRed, historyBlue) + " (日期: " + formattedDate + ", 期号: " + key + ")");
            }
        }
        return result;
    }

    /**
     * 格式化日期显示
     */
    private static String formatDateDisplay(String dateStr) {
        // 尝试识别常见的日期格式
        if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            // 已经是标准格式 yyyy-MM-dd
            return dateStr;
        } else if (dateStr.matches("\\d{8}")) {
            // 格式为 yyyyMMdd
            return dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8);
        } else {
            // 其他格式或无法识别为日期的情况
            return dateStr;
        }
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
        // 获取最新的开奖号码
        String latestKey = historyData.keySet().stream()
                .max(Comparator.naturalOrder())
                .orElse(null);

        if (latestKey != null) {
            String latestValue = historyData.getString(latestKey);
            String[] parts = latestValue.split("\\|");

            List<Integer> openRed = new ArrayList<>();
            List<Integer> openBlue = new ArrayList<>();

            // 分离红球和蓝球
            for (int i = 0; i < parts.length; i++) {
                int num = Integer.parseInt(parts[i]);
                if (i < redSize) {
                    openRed.add(num);
                } else {
                    openBlue.add(num);
                }
            }

            // 计算匹配数量
            int redMatch = (int) redList.stream().filter(openRed::contains).count();
            int blueMatch = (int) blueList.stream().filter(openBlue::contains).count();

            // 格式化日期显示
            String formattedDate = formatDateDisplay(latestKey);
            
            // 输出匹配结果
            System.out.println("上一期开奖号码: " + formatHistoryNumbers(openRed, openBlue) + " (日期: " + formattedDate + ", 期号: " + latestKey + ")");
            System.out.println("红球匹配: " + redMatch + "个");
            System.out.println("蓝球匹配: " + blueMatch + "个");
        }
    }
}
