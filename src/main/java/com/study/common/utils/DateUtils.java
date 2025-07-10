package com.study.common.utils;
/**
 * @Author yangx
 * @Description 描述
 * @Since create in 2024-3-26
 * @Company 广州云趣信息科技有限公司
 */

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author yangxu
 * @create 2024/3/26 9:54
 */
public class DateUtils {

    // 获取当前日期并格式化为 "yyyy-MM-dd" 格式
    public static String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date();
        return dateFormat.format(currentDate);
    }

    /**
     * @Author yangxu
     * @Description 获取昨天的日期
     * @Param:
     * @Return: java.lang.String
     * @Since create in 2024/2/20 9:11
     * @Company 广州云趣信息科技有限公司
     */
    public static String getYesterdayDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        Date currentDate = new Date();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterdayDate = calendar.getTime();
        return dateFormat.format(yesterdayDate);
    }
}
