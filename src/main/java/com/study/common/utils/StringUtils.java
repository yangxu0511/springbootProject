package com.study.common.utils;/**
 * @Author yangx
 * @Description 描述
 * @Since create in
 * @Company 广州云趣信息科技有限公司
 */

/**
 * @author yangxu
 * @create 2024/3/26 9:55
 */
class StringUtils {
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }

    ;

    public static String replace(String str, String oldStr) {
        if (isEmpty(str)) return str;
        return str.replace(oldStr, "");
    }

    ;

    public static String replace(String str, String oldStr, String newStr) {
        if (isEmpty(str)) return str;
        return str.replace(oldStr, newStr);
    }

    ;

    public static boolean containsIgnoreCase(String str, String containsStr) {
        if (isEmpty(str)) return false;
        return str.toLowerCase().contains(containsStr.toLowerCase());
    }

    ;

    public static boolean equals(String str, String anObject) {
        if (isEmpty(str)) return false;
        return str.equals(anObject);
    }

    ;

    public static boolean equalsIgnoreCase(String str, String anObject) {
        if (isEmpty(str)) return false;
        return str.equalsIgnoreCase(anObject);
    }

    ;

    public static boolean contains(String str, String anObject) {
        if (isEmpty(str)) return false;
        return str.contains(anObject);
    }

    ;

    public static String substring(String str, int beginIndex, int endIndex) {
        if (isEmpty(str)) return str;
        return str.substring(beginIndex, endIndex);
    }

    ;
}
