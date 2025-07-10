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
enum MyExcelTypeEnum {

    DATE_EN_SHORTHAND_1("d-mmm", "d-MMM"),
    DATE_EN_SHORTHAND_2("d-mmm-yy", "d-MMM-yy"),
    DATE_EN_SHORTHAND_3("dd\\-mmm\\-yy", "d-MMM-yy"),
    DATE_EN_SHORTHAND_4("mmm-yy", "MMM-yy"),
    DATE_EN_SHORTHAND_5("mmmm\\-yy", "MMMM-yy"),
    DATE_EN_SHORTHAND_6("mmmm-yy", "MMMM-yy"),
    DATE_EN_SHORTHAND_7("mmmmm", "MMMMM"),
    DATE_EN_SHORTHAND_8("mmmmm\\-yy", "MMMMM-yy"),

    DATE_TIME_1("h\"时\"mm\"分\"", "H时mm分"),
    DATE_TIME_2("h\"时\"mm\"分\"ss\"秒\"", "H时mm分ss秒"),
    DATE_TIME_3("上午/下午h\"时\"mm\"分\"", "aah时mm分"),
    DATE_TIME_4("上午/下午h\"时\"mm\"分\"ss\"秒\"", "aah时mm分ss秒"),

    DATE_EN_1("yyyy/m/d\\ h:mm\\ AM/PM", "yyyy/M/d h:mm aa"),
    DATE_EN_2("h:mm\\ AM/PM", "h:mm aa"),
    DATE_EN_3("h:mm:ss\\ AM/PM", "h:mm:ss aa");


    String excelType;
    String formatType;

    MyExcelTypeEnum(String excelType, String formatType) {
        this.excelType = excelType;
        this.formatType = formatType;
    }

    public static String getFormatType(String excelType) {
        for (MyExcelTypeEnum excelTypeEnum : MyExcelTypeEnum.values()) {
            if (excelTypeEnum.excelType.equals(excelType)) {
                return excelTypeEnum.formatType;
            }
        }
        return null;
    }

    public static String getFormatKey(String excelType) {
        for (MyExcelTypeEnum excelTypeEnum : MyExcelTypeEnum.values()) {
            if (excelTypeEnum.excelType.equals(excelType)) {
                return excelTypeEnum.name();
            }
        }
        return excelType;
    }
}
