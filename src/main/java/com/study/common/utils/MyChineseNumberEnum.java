package com.study.common.utils;/**
 * @Author yangx
 * @Description 描述
 * @Since create in
 * @Company 广州云趣信息科技有限公司
 */

import java.util.HashMap;
import java.util.Map;

/**
 * @author yangxu
 * @create 2024/3/26 9:55
 */
enum MyChineseNumberEnum {

    /**
     * 0
     */
    ZERO("0", "〇"),
    ZERO_DOUBLE("00", "〇〇"),

    /**
     * 1
     */
    ONE("1", "一"),

    /**
     * 2
     */
    TWO("2", "二"),

    /**
     * 3
     */
    THREE("3", "三"),

    /**
     * 4
     */
    FOUR("4", "四"),

    /**
     * 5
     */
    FIVE("5", "五"),

    /**
     * 6
     */
    SIX("6", "六"),

    /**
     * 7
     */
    SEVEN("7", "七"),

    /**
     * 8
     */
    EIGHT("8", "八"),

    /**
     * 9
     */
    NINE("9", "九"),

    /**
     * 10
     */
    TEN("10", "十"),

    /**
     *
     */
    ELEVEN("11", "十一"),

    /**
     *
     */
    TWELVE("12", "十二"),

    /**
     *
     */
    THIRTEEN("13", "十三"),

    /**
     *
     */
    FOURTEEN("14", "十四"),

    /**
     *
     */
    FIFTEEN("15", "十五"),

    /**
     *
     */
    SIXTEEN("16", "十六"),

    /**
     *
     */
    SEVENTEEN("17", "十七"),

    /**
     *
     */
    EIGHTEEN("18", "十八"),

    /**
     *
     */
    NINETEEN("19", "十九"),

    /**
     *
     */
    TWENTY("20", "二十"),

    /**
     *
     */
    TWENTY_ONE("21", "二十一"),

    /**
     *
     */
    TWENTY_TWO("22", "二十二"),

    /**
     *
     */
    TWENTY_THREE("23", "二十三"),

    /**
     *
     */
    TWENTY_FOUR("24", "二十四"),

    /**
     *
     */
    TWENTY_FIVE("25", "二十五"),

    /**
     *
     */
    TWENTY_SIX("26", "二十六"),

    /**
     *
     */
    TWENTY_SEVEN("27", "二十七"),

    /**
     *
     */
    TWENTY_EIGHT("28", "二十八"),

    /**
     *
     */
    TWENTY_NINE("29", "二十九"),

    /**
     *
     */
    THIRTY("30", "三十"),

    /**
     *
     */
    THIRTY_ONE("31", "三十一"),
    ;

    private final String numberCode;
    private final String chineseCode;

    MyChineseNumberEnum(String numberCode, String chineseCode) {
        this.numberCode = numberCode;
        this.chineseCode = chineseCode;
    }

    public String getNumberCode() {
        return numberCode;
    }

    public String getChineseCode() {
        return chineseCode;
    }

    static final Map<String, String> SEND_METHOD_MAP = new HashMap<>();

    static {
        for (MyChineseNumberEnum code : MyChineseNumberEnum.values()) {
            SEND_METHOD_MAP.put(code.getNumberCode(), code.getChineseCode());
        }
    }

    public static String getValue(String key) {
        return SEND_METHOD_MAP.getOrDefault(key, key);
    }
}
