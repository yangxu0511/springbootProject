package com.study.common.thread;

/*
  @Author yangx
 * @Description 描述
 * @Since create in
 * @Company 广州云趣信息科技有限公司
 */

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.study.common.utils.Sm4Util;
import com.yq.busi.common.util.DateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.easitline.common.utils.calendar.EasyDate;
import org.easitline.common.utils.kit.RandomKit;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yangxu
 * @create 2024/1/16 16:15
 */
public class TestMain {
    public TestMain() throws IOException {
    }


    /**
     * 获取指定月份的最后一天
     * @param monthStr 月份字符串，格式如：202506
     * @return 该月最后一天，格式如：20250630
     */
    private static String getMonthLastDay(String monthStr) {
        try {
            // 解析年月：202506 -> 2025-06
            String yearStr = monthStr.substring(0, 4);
            String monthStrPart = monthStr.substring(4, 6);

            // 构建该月第一天
            LocalDate firstDay = LocalDate.of(Integer.parseInt(yearStr), Integer.parseInt(monthStrPart), 1);

            // 获取该月最后一天
            LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

            // 格式化为yyyyMMdd格式
            return lastDay.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            // 默认返回月末31日（可能不准确，但作为兜底）
            return monthStr + "31";
        }
    }

    public static void main(String[] args) throws Exception {
        String createTime = "2024/11/19 16:48:04";
        System.out.println(createTime = createTime.trim().replaceAll("[^0-9]", ""));
        System.out.println(RandomKit.uniqueStr());

        System.out.println( DateUtil.getCurrentDateStr("yyyyMMdd-HHmmss"));

        System.out.println(EasyDate.getCurrentDateString());
        System.out.println(getMonthLastDay("202502"));


        String reports = "JT5";
        if (reports.contains("1") || reports.contains("2") || reports.contains("3") || reports.contains("5")) {
            System.out.println("通过");
        }
        String date1 = "20240101";
        System.out.println(date1.substring(0, 4));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1); // 设置为本月第一天
        Date firstDay = calendar.getTime();

        // 获取当前月份的最后一天
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date lastDay = calendar.getTime();

        System.out.println(firstDay+","+lastDay);

        String acd = "a|b";
        System.out.println(acd.split("\\|").length);


        String dateIdss = "20250122";
        String fileName = "JS2-20250122-result.zip";
        String endsWith = "-" + dateIdss + "-result.zip";
        String entCode = "2";
        String tariffProvinceCode = "";

        String startWith = tariffProvinceCode + entCode + "-"; // 返回省份代码和企业代码的组合

        if (!fileName.endsWith(endsWith) || !fileName.startsWith(startWith)) {
            System.out.println("不满足===");
        } else {
            System.out.println("满足==");
        }

        String feeName = "    【省】校园基础权益卡\n" +
                "（AI智慧体育）   ";
        String formattedFeeName = formatFeeName(feeName);
        System.out.println(formattedFeeName);  // 输出: XX套餐（国内）（香港）资费

        // 构建 List<JSONObject>
        List<JSONObject> result = new ArrayList<>();
        List<JSONObject> auditResult = new ArrayList<>();

        // 模拟数据
        String date = "2024-12-03";
        int telecom = 42;
        int mobile = 0;
        int unicom = 216;
        int broad = 0;
        int allTotal = 258;
        // 创建 JSON 对象
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("DATE_ID", date);
        jsonObject.put("TELECOM", telecom);
        jsonObject.put("MOBILE", mobile);
        jsonObject.put("UNICOM", unicom);
        jsonObject.put("BROAD", broad);
        jsonObject.put("MAX_DATE", date);
        jsonObject.put("MIN_DATE", date);
        jsonObject.put("ALLTOTAL", allTotal);

        System.out.println("dateId=" + jsonObject.containsKey("date_id") + ",DATE_ID=" + jsonObject.containsKey("DATE_ID"));

        // 创建 JSON 对象
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("DATE_ID", "平均(去重)");
        jsonObject2.put("TELECOM", 42);
        jsonObject2.put("MOBILE", 0);
        jsonObject2.put("UNICOM", 216);
        jsonObject2.put("BROAD", 0);
        jsonObject2.put("MAX_DATE", "2024-12-03");
        jsonObject2.put("MIN_DATE", "2024-12-03");
        jsonObject2.put("ALLTOTAL", 258);
        // 添加到结果列表
        result.add(jsonObject);
        result.add(jsonObject2);

        JSONObject jsonObject3 = new JSONObject();
        jsonObject3.put("ENT", 1);
        jsonObject3.put("TARIFF_AUDIT_DATE", "20241203");

        JSONObject jsonObject4 = new JSONObject();
        jsonObject4.put("ENT", 2);
        jsonObject4.put("TARIFF_AUDIT_DATE", "20241203");

        JSONObject jsonObject5 = new JSONObject();
        jsonObject5.put("ENT", 3);
        jsonObject5.put("TARIFF_AUDIT_DATE", "20241203");
        auditResult.add(jsonObject3);
        auditResult.add(jsonObject4);
        auditResult.add(jsonObject5);

        for (JSONObject obj : result) {
            if (obj == null) continue;
            // 批量处理所有字段
            String[] fields = {"MOBILE", "UNICOM", "TELECOM", "BROAD", "ALLTOTAL"};
            for (String field : fields) {
                Integer value = obj.getInteger(field);
                obj.put(field, (value == null || value == 0) ? "" : value);
            }
        }

        // 预处理 auditResult 数据,构建查找Map
        Map<String, Map<String, String>> auditMap = new HashMap<>();
        for (JSONObject audit : auditResult) {
            String auditDate = audit.getString("TARIFF_AUDIT_DATE");
            String ent = audit.getString("ENT");
            auditMap.computeIfAbsent(auditDate, k -> new HashMap<>()).put(ent, "0");
        }

        // 使用Map优化查找和更新
        for (JSONObject resultItem : result) {
            String dateId = resultItem.getString("DATE_ID");
            if ("平均(去重)".equals(dateId)) {
                continue;
            }

            String maxDate = resultItem.getString("MAX_DATE");
            if (StringUtils.isBlank(maxDate)) {
                continue;
            }

            String formattedMaxDate = maxDate.replaceAll("-", "");
            Map<String, String> entMap = auditMap.get(formattedMaxDate);

            if (entMap != null) {
                // 使用Map直接映射字段名
                Map<String, String> fieldMap = new HashMap<>();
                fieldMap.put("1", "TELECOM");
                fieldMap.put("2", "MOBILE");
                fieldMap.put("3", "UNICOM");
                fieldMap.put("5", "BROAD");

                // 批量更新字段
                entMap.forEach((ent, value) -> {
                    String field = fieldMap.get(ent);
                    if (field != null && "".equals(resultItem.getString(field))) {
                        resultItem.put(field, 0);
                    }
                });
            }
        }

        for (JSONObject obj : result) {
            System.out.println(obj.toJSONString());
        }

    }


    public static boolean isValidDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setLenient(false);  // 设置为不宽松的日期解析
        try {
            sdf.parse(dateStr);  // 尝试解析日期
            return true;  // 如果没有异常，日期有效
        } catch (ParseException e) {
            return false;  // 如果抛出异常，说明日期无效
        }
    }

    public static String formatFeeName(String feeName) {
        return feeName
                .replace("\r", "")   // 移除回车符
                .replace("\n", "")   // 移除换行符
                .replace("（", "(")   // 中文左括号转英文
                .replace("）", ")").trim();  // 中文右括号转英文

    }

}
