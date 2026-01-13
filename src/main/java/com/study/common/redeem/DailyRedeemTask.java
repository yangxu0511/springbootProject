package com.study.common.redeem;

import com.study.common.redeem.RedeemNum.RedeemResult;
import com.study.common.redeem.RedeemNum.RedeemResult.HistoricalWin;
import com.study.common.redeem.RedeemNum.RedeemResult.NumberResult;

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

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("彩票每日兑奖任务");
        System.out.println("执行时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("========================================\n");

        try {
            // 执行兑奖并获取结果
            RedeemResult result = RedeemNum.redeemWithPush(null);
            
            // 构建推送内容
            String title;
            StringBuilder content = new StringBuilder();
            
            if (result.getError() != null) {
                title = "❌ 兑奖失败";
                content.append("**错误信息**: ").append(result.getError());
            } else if (result.isNoPurchase()) {
                title = "📋 " + result.getLotteryTypeName() + " 兑奖通知";
                content.append("### ").append(result.getLotteryTypeName()).append(" 开奖信息\n\n");
                content.append("> 开奖日期: ").append(result.getOpenDate()).append("\n\n");
                content.append("**开奖号码**: `").append(formatWinningNumber(result.getWinningNumber())).append("`\n\n");
                content.append("---\n");
                content.append("⚠️ **当天未购彩**，无需兑奖\n\n");
                
                // 添加历史中奖信息
                appendHistoricalWins(content, result);
            } else {
                boolean hasWinning = result.hasWinning();
                
                if (hasWinning) {
                    title = "🎉 恭喜中奖！" + result.getLotteryTypeName();
                } else {
                    title = "😢 " + result.getLotteryTypeName() + " 未中奖";
                }
                
                content.append("### ").append(result.getLotteryTypeName()).append(" 兑奖结果\n\n");
                content.append("> 开奖日期: ").append(result.getOpenDate()).append("\n");
                content.append("> 购买日期: ").append(result.getBuyDate()).append("\n\n");
                content.append("**开奖号码**: `").append(formatWinningNumber(result.getWinningNumber())).append("`\n\n");
                content.append("---\n\n");
                
                // 当天购买号码对比
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
                
                // 添加历史中奖信息
                appendHistoricalWins(content, result);
                
                if (hasWinning || hasHistoricalWinning) {
                    content.append("---\n");
                    content.append("🎊 **恭喜发财！记得去兑奖！**");
                } else {
                    content.append("---\n");
                    content.append("💪 *继续努力，下次一定中！*");
                }
            }
            
            // 推送到微信
            System.out.println("\n正在推送兑奖结果到微信...");
            ServerChanPush.push(title, content.toString());
            
            System.out.println("\n========================================");
            System.out.println("兑奖任务执行完成！");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.out.println("兑奖任务执行失败: " + e.getMessage());
            e.printStackTrace();
            
            // 推送错误信息
            ServerChanPush.push("❌ 兑奖任务执行失败", "错误信息: " + e.getMessage());
        }
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
