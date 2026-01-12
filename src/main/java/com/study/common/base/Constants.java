package com.study.common.base;/*
 * @Author yangx
 * @Description 常量类
 * @Since create in 2024-4-24 11:10:51
 * @Company 广州云趣信息科技有限公司
 */

import java.util.HashMap;
import java.util.Map;

/**
 * @author yangxu
 * @create 2024/4/24 11:10
 */

public class Constants {
    private static final String basePath = "D:\\idea-workspace\\springbootProject\\src\\main\\resources";
    private static final String hisFilePath = "D:\\idea-workspace\\springbootProject\\src\\main\\resources\\history.json";
    private static final String notBuyPath = "D:\\idea-workspace\\springbootProject\\src\\main\\resources\\notBuy.json";
    private static final String tcFilePath = "D:\\idea-workspace\\springbootProject\\dlt.json";
    private static final String tcBakFilePath = "D:\\idea-workspace\\springbootProject\\dlt_bak.json";
    private static final String fcFilePath = "D:\\idea-workspace\\springbootProject\\ssq.json";
    private static final String fcBakFilePath = "D:\\idea-workspace\\springbootProject\\ssq_bak.json";

    private static final String hisOutFilePath = "D:\\idea-workspace\\springbootProject\\src\\main\\resources\\history_sort.json";
    private static final String notBuyOutPath = "D:\\idea-workspace\\springbootProject\\src\\main\\resources\\notBuy_sort.json";
    private static final String tcFileOutPath = "D:\\idea-workspace\\springbootProject\\\\dlt_sort.json";
    private static final String fcFileOutPath = "D:\\idea-workspace\\springbootProject\\ssq_sort.json";


    public static final String pythonScriptPath = "D:\\idea-workspace\\springbootProject\\src\\main\\resources\\zj.py";
    public static final String pythonexe = "C:\\Users\\yangxu\\AppData\\Local\\Programs\\Python\\Python38\\python.exe";
    public static final String ifRunPath = "D:\\idea-workspace\\springbootProject\\src\\main\\resources\\runPython.json";
    public static final int similarSize = 5; //定义相似度个数
    public static final int sameRedSize = 4; //定义红球命中个数
    public static final int sameHisSize = 4; //历史命中数

    // ==================== 筛选规则配置 ====================
    
    // 奇偶比高频阈值（出现频率>=此值视为高频，百分比）
    public static final double ODD_EVEN_THRESHOLD = 15.0;
    
    // 大小号比例高频阈值（百分比）
    public static final double BIG_SMALL_THRESHOLD = 15.0;
    
    // 冷热号分析期数
    public static final int HOT_COLD_PERIOD = 30;
    
    // 双色球跨度范围（红球最大值-最小值）
    public static final int SSQ_SPAN_MIN = 18;
    public static final int SSQ_SPAN_MAX = 30;
    
    // 大乐透跨度范围
    public static final int DLT_SPAN_MIN = 22;
    public static final int DLT_SPAN_MAX = 32;
    
    // 双色球和值范围（6个红球之和）
    public static final int SSQ_SUM_MIN = 70;
    public static final int SSQ_SUM_MAX = 150;
    
    // 大乐透和值范围（5个红球之和）
    public static final int DLT_SUM_MIN = 60;
    public static final int DLT_SUM_MAX = 130;
    
    // 号码生成最大重试次数
    public static final int MAX_RETRY_COUNT = 100;
    
    // ==================== 筛选规则开关 ====================
    public static final boolean ENABLE_ODD_EVEN_FILTER = true;      // 奇偶比筛选
    public static final boolean ENABLE_BIG_SMALL_FILTER = true;     // 大小号筛选
    public static final boolean ENABLE_HOT_COLD_FILTER = true;      // 冷热号筛选
    public static final boolean ENABLE_SPAN_FILTER = true;          // 跨度筛选
    public static final boolean ENABLE_SUM_FILTER = true;           // 和值筛选
    public static final boolean ENABLE_CONSECUTIVE_FILTER = true;   // 连号筛选

    private static final Map<String, String> tcMap = new HashMap<>();
    private static final Map<String, String> fcMap = new HashMap<>();

    static {
        tcMap.put("5-2", "恭喜你成为百万富翁……历史性的一刻！！！中奖金额>=500万");
        tcMap.put("5-1", "恭喜中了二等奖！奖金≈30万");
        tcMap.put("5-0", "恭喜中了三等奖! 奖金=1万");
        tcMap.put("4-2", "恭喜中了四等奖！奖金=3000");
        tcMap.put("4-1", "恭喜中了五等奖！奖金=300");
        tcMap.put("3-2", "恭喜中了六等奖！奖金=200");
        tcMap.put("4-0", "恭喜中了七等奖！奖金=100");
        tcMap.put("3-1", "恭喜中了八等奖！奖金=15");
        tcMap.put("2-2", "恭喜中了八等奖！奖金=15");
        tcMap.put("3-0", "恭喜中了九等奖！奖金=5");
        tcMap.put("1-2", "恭喜中了九等奖！奖金=5");
        tcMap.put("2-1", "恭喜中了九等奖！奖金=5");
        tcMap.put("0-2", "恭喜中了九等奖！奖金=5");

        fcMap.put("6-1", "恭喜你成为百万富翁……历史性的一刻！！！中奖金额>=500万");
        fcMap.put("6-0", "恭喜中了二等奖！奖金≈30万");
        fcMap.put("5-1", "恭喜中了三等奖! 奖金=1万");
        fcMap.put("5-0", "恭喜中了四等奖！奖金=200");
        fcMap.put("4-1", "恭喜中了四等奖！奖金=200");
        fcMap.put("4-0", "恭喜中了五等奖！奖金=10");
        fcMap.put("3-1", "恭喜中了五等奖！奖金=10");
        fcMap.put("2-1", "恭喜中了六等奖！奖金=5");
        fcMap.put("1-1", "恭喜中了六等奖！奖金=5");
        fcMap.put("0-1", "恭喜中了六等奖！奖金=5");
    }

    public static Map<String, String> getTcMap() {
        return tcMap;
    }

    public static Map<String, String> getFcMap() {
        return fcMap;
    }

    public static String getBasePath() {
        return basePath;
    }

    public static String getHisFilePath() {
        return hisFilePath;
    }

    public static String getTcFilePath() {
        return tcFilePath;
    }

    public static String getTcBakFilePath() {
        return tcBakFilePath;
    }

    public static String getFcFilePath() {
        return fcFilePath;
    }

    public static String getFcBakFilePath() {
        return fcBakFilePath;
    }

    public static String getNotBuyPath() {
        return notBuyPath;
    }

    public static String getHisOutFilePath() {
        return hisOutFilePath;
    }

    public static String getTcFileOutPath() {
        return tcFileOutPath;
    }

    public static String getFcFileOutPath() {
        return fcFileOutPath;
    }

    public static String getNotBuyOutPath() {
        return notBuyOutPath;
    }


}
