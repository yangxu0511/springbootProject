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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        String text = "无在网要求1个是爱人是的";
        String[] patterns = new String[5];
        patterns[0] = "无在网要求";
        patterns[1] = ".*要求";


        for (String pattern : patterns) {
            if (StringUtils.isBlank(pattern)) {
                continue;
            }

            try {
                if (pattern.contains("*")) {
                    String regex = pattern.replace("*", ".*");                    // 包含 *：使用正则包含匹配 (find)
                    // 注意：这里的正则就是 pattern 本身，不需要像您原方法中那样额外加 [1-9][0-9]*，
                    // 因为这里配的值如 ".*个月" 已经包含了匹配逻辑。

                    Pattern compiledPattern = Pattern.compile(pattern);
                    Matcher matcher = compiledPattern.matcher(text);

                    if (matcher.find()) {
                        System.out.println("true"+pattern);
                    }
                } else {
                    // 不包含 *：要求完全等于 (equals)
                    if (text.trim().equals(pattern.trim())) {
                        System.out.println("true");

                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }

}
