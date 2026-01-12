package com.study.common.redeem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Server酱微信推送工具类
 * 文档: https://sct.ftqq.com/
 * 
 * @author yangxu
 * @since 2026/01/12
 */
public class ServerChanPush {
    
    private static final Logger logger = Logger.getLogger(ServerChanPush.class.getName());
    
    // Server酱 SendKey（请替换为你自己的Key）
    private static final String SEND_KEY = "SCT309242TsXnEa4nrgdrPYCK1osBwUfIO";
    
    // Server酱 API 地址
    private static final String API_URL = "https://sctapi.ftqq.com/" + SEND_KEY + ".send";
    
    /**
     * 发送消息到微信
     * @param title 消息标题（必填）
     * @param content 消息内容（选填，支持Markdown）
     * @return 是否发送成功
     */
    public static boolean push(String title, String content) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            // 构建请求参数
            String params = "title=" + URLEncoder.encode(title, "UTF-8");
            if (content != null && !content.isEmpty()) {
                params += "&desp=" + URLEncoder.encode(content, "UTF-8");
            }
            
            // 发送请求
            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            
            // 读取响应
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    String result = response.toString();
                    logger.info("Server酱推送成功: " + result);
                    System.out.println("✅ 消息已推送到微信！");
                    return true;
                }
            } else {
                logger.warning("Server酱推送失败，状态码: " + responseCode);
                System.out.println("❌ 推送失败，状态码: " + responseCode);
                return false;
            }
        } catch (Exception e) {
            logger.severe("Server酱推送异常: " + e.getMessage());
            System.out.println("❌ 推送异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 推送彩票号码
     * @param lotteryType 彩票类型 tc/fc
     * @param numbers 号码列表
     */
    public static void pushLotteryNumbers(String lotteryType, java.util.List<String> numbers) {
        String typeName = "tc".equals(lotteryType) ? "🎰 大乐透" : "🔵 双色球";
        String title = typeName + " 今日推荐号码";
        
        StringBuilder content = new StringBuilder();
        content.append("### ").append(typeName).append(" 推荐号码\n\n");
        content.append("> 生成时间: ").append(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        for (int i = 0; i < numbers.size(); i++) {
            content.append("**第 ").append(i + 1).append(" 注**: `").append(numbers.get(i)).append("`\n\n");
        }
        
        content.append("---\n");
        content.append("⚠️ *温馨提示：理性购彩，切勿沉迷*");
        
        push(title, content.toString());
    }
    
    /**
     * 测试推送
     */
    public static void main(String[] args) {
        push("测试推送", "这是一条测试消息，来自彩票号码生成系统。");
    }
}
