package com.study.common.redeem;

import com.study.common.redeem.RedeemNum.RedeemResult;
import com.study.common.redeem.RedeemNum.RedeemResult.HistoricalWin;
import com.study.common.redeem.RedeemNum.RedeemResult.NumberResult;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 每日兑奖定时任务入口（带微信推送）
 *
 * 使用说明：
 * 1. 运行此类的 main 方法执行兑奖比对并推送结果到微信
 * 2. 配合 Windows 任务计划程序每天早上 9:30 执行
 *
 * @author yangxu
 * @since 2026/01/13
 */
public class DailyRedeemTask {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("彩票每日兑奖任务");
        System.out.println("执行时间: " + LocalDateTime.now().format(DATE_TIME_FORMATTER));
        System.out.println("========================================\n");

        try {
            LocalDate today = LocalDate.now();
            if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
                pushWeekendRedeemSummary(today);
            } else {
                pushSingleRedeemResult(RedeemNum.redeemWithPush(null));
                printDone();
            }
        } catch (Exception e) {
            System.out.println("兑奖任务执行失败: " + e.getMessage());
            e.printStackTrace();

            // 推送错误信息
            ServerChanPush.push("❌ 兑奖任务执行失败", "错误信息: " + e.getMessage());
        }
    }

    private static void pushWeekendRedeemSummary(LocalDate today) {
        LocalDate saturday = today.minusDays(2);
        LocalDate sunday = today.minusDays(1);

        System.out.println("今天是周一，补兑周末号码。");
        RedeemResult saturdayResult = RedeemNum.redeemWithPush(saturday.format(DATE_FORMATTER));
        RedeemResult sundayResult = RedeemNum.redeemWithPush(sunday.format(DATE_FORMATTER));

        System.out.println("\n正在推送兑奖结果到微信...");
        ServerChanPush.push(
                buildWeekendTitle(saturdayResult, sundayResult),
                buildWeekendContent(today, saturdayResult, sundayResult)
        );

        printDone();
    }

    private static void pushSingleRedeemResult(RedeemResult result) {
        System.out.println("\n正在推送兑奖结果到微信...");
        ServerChanPush.push(buildSingleTitle(result), buildSingleContent(result));
    }

    private static String buildWeekendTitle(RedeemResult saturdayResult, RedeemResult sundayResult) {
        if (hasError(saturdayResult) || hasError(sundayResult)) {
            return "⚠️ 周末兑奖汇总";
        }
        if (saturdayResult.hasWinning() || sundayResult.hasWinning()) {
            return "🎉 周末兑奖汇总";
        }
        return "📋 周末兑奖汇总";
    }

    private static String buildWeekendContent(LocalDate today, RedeemResult saturdayResult, RedeemResult sundayResult) {
        StringBuilder content = new StringBuilder();
        content.append("### 周一补兑汇总\n\n");
        content.append("> 执行日期: ").append(today.format(DATE_FORMATTER)).append("\n");
        content.append("> 执行时间: ").append(LocalDateTime.now().format(DATE_TIME_FORMATTER)).append("\n\n");

        appendResultSection(content, saturdayResult);
        content.append("\n");
        appendResultSection(content, sundayResult);
        return content.toString();
    }

    private static String buildSingleTitle(RedeemResult result) {
        if (result.getError() != null) {
            return "❌ 兑奖失败";
        }
        if (result.isNoPurchase()) {
            return "📋 " + result.getLotteryTypeName() + " 兑奖通知";
        }
        return result.hasWinning()
                ? "🎉 恭喜中奖！" + result.getLotteryTypeName()
                : "😢 " + result.getLotteryTypeName() + " 未中奖";
    }

    private static String buildSingleContent(RedeemResult result) {
        StringBuilder content = new StringBuilder();
        appendResultSection(content, result);
        return content.toString();
    }

    private static void appendResultSection(StringBuilder content, RedeemResult result) {
        if (result.getError() != null) {
            content.append("### ").append(getResultDisplayName(result)).append(" 兑奖失败\n\n");
            content.append("**错误信息**: ").append(result.getError()).append("\n");
            return;
        }

        if (result.isNoPurchase()) {
            content.append("### ").append(result.getLotteryTypeName()).append(" 开奖信息\n\n");
            content.append("> 开奖日期: ").append(result.getOpenDate()).append("\n");
            content.append("> 购买日期: ").append(result.getBuyDate()).append("\n\n");
            content.append("**开奖号码**: `").append(formatWinningNumber(result.getWinningNumber())).append("`\n\n");
            content.append("---\n");
            content.append("⚠️ **当天未购彩**，无需兑奖\n\n");
            appendHistoricalWins(content, result);
            return;
        }

        boolean hasWinning = result.hasWinning();
        content.append("### ").append(result.getLotteryTypeName()).append(" 兑奖结果\n\n");
        content.append("> 开奖日期: ").append(result.getOpenDate()).append("\n");
        content.append("> 购买日期: ").append(result.getBuyDate()).append("\n\n");
        content.append("**开奖号码**: `").append(formatWinningNumber(result.getWinningNumber())).append("`\n\n");
        content.append("---\n\n");
        content.append("### 📌 当天购买号码对比\n\n");

        int idx = 1;
        for (NumberResult nr : result.getNumberResults()) {
            content.append("**第 ").append(idx++).append(" 注**: `").append(nr.getNumber()).append("`\n");
            content.append("- 红球命中: ").append(nr.getRedMatch()).append("个");
            content.append(", 蓝球命中: ").append(nr.getBlueMatch()).append("个\n");

            if (nr.isWon()) {
                content.append("- **").append(nr.getPrize()).append("** 🎊\n");
            } else {
                content.append("- 未中奖 😢\n");
            }
            content.append("\n");
        }

        appendHistoricalWins(content, result);
        content.append("---\n");
        content.append(hasWinning ? "🎊 **恭喜发财！记得去兑奖！**" : "💪 *继续努力，下次一定中！*").append("\n");
    }

    private static boolean hasError(RedeemResult result) {
        return result.getError() != null;
    }

    private static String getResultDisplayName(RedeemResult result) {
        return result.getLotteryType() == null ? "彩票" : result.getLotteryTypeName();
    }

    private static void printDone() {
        System.out.println("\n========================================");
        System.out.println("兑奖任务执行完成！");
        System.out.println("========================================");
    }

    /**
     * 添加历史中奖信息到推送内容
     */
    private static void appendHistoricalWins(StringBuilder content, RedeemResult result) {
        List<HistoricalWin> historicalWins = result.getHistoricalWins();
        if (historicalWins.isEmpty()) {
            return;
        }

        content.append("\n### 📜 历史购买号码中奖情况\n\n");

        // 先显示真正中奖的
        List<HistoricalWin> wonList = historicalWins.stream()
                .filter(HistoricalWin::isWon)
                .collect(Collectors.toList());

        if (!wonList.isEmpty()) {
            content.append("#### 🎊 中奖号码\n\n");
            for (HistoricalWin hw : wonList) {
                content.append("- **").append(hw.getDate()).append("** `").append(hw.getNumber()).append("`\n");
                content.append("  - 红球命中: ").append(hw.getRedMatch()).append("个, 蓝球命中: ").append(hw.getBlueMatch()).append("个\n");
                content.append("  - **").append(hw.getPrize()).append("** 🎉\n\n");
            }
        }

        // 显示高相似度但未中奖的（红球>=4个）
        List<HistoricalWin> similarList = historicalWins.stream()
                .filter(hw -> !hw.isWon())
                .collect(Collectors.toList());

        if (!similarList.isEmpty() && similarList.size() <= 5) {
            content.append("#### 🔍 高相似度号码\n\n");
            for (HistoricalWin hw : similarList) {
                content.append("- ").append(hw.getDate()).append(" `").append(hw.getNumber()).append("` ");
                content.append("(红球").append(hw.getRedMatch()).append("个, 蓝球").append(hw.getBlueMatch()).append("个)\n");
            }
            content.append("\n");
        }
    }

    /**
     * 格式化开奖号码显示
     */
    private static String formatWinningNumber(String number) {
        if (number == null) return "";
        // 将 01|02|03|04|05|06|07 格式转换为 01,02,03,04,05,06 07 格式
        String[] parts = number.split("\\|");
        if (parts.length <= 1) return number;

        StringBuilder result = new StringBuilder();
        int redCount = parts.length >= 7 ? (parts.length == 7 ? 6 : 5) : parts.length - 1;

        for (int i = 0; i < parts.length; i++) {
            if (i == redCount) {
                result.append(" ");
            } else if (i > 0) {
                result.append(",");
            }
            result.append(parts[i]);
        }
        return result.toString();
    }
}
