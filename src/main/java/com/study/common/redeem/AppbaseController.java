package com.study.common.redeem;

import cn.hutool.core.util.StrUtil;

public class AppbaseController {

    public static void main(String[] args) {
        //获取每天的号码 命运的齿轮开始转动
        //体彩截止时间晚上9点  福彩截止时间晚上8
//       getToday();
        //获取号码
//		action("2","fc");
//		自动兑奖
//        action("3");
        //指定日期兑奖=T-1 如果日期是周六=T-2
//		action("3","2025-10-11");
//        //写入号码 购买日期默认为昨天的日期 1当天 2昨天
//		action("4","06,09,10,30,33 05,10","2");
//		action("4","01,08,26,27,35 05,11","2");
//		action("4","02,03,14,17,34 09,12","2");
//		action("4","02,03,06,16,24 03,10","2");

		action("4","02,12,19,22,27,33 07","2");
		action("4","07,11,16,20,25,29 02","2");
		action("4","05,06,09,21,23,30 12","2");
//        action("4","02,17,20,24,27,29 10","1");
//        action("4","05,09,10,14,15,28 10","1");
        //把json文件排序
//      action("5");

    }


    /*
     * @Author yangxu
     * @Description 1：执行python脚本获取最新号码 2：获取今晚号码 3：号码比对
     * @Param: [type, flag]
     * @Return: void
     * @Since create in 2024/1/22 13:42
     * @Company 广州云趣信息科技有限公司
     */
    public static void action(String type, String params, String params2) {
        switch (type) {
            case "1": //执行Python脚本获取最新开奖号码
                RunPython.run();
                break;
            case "2": //获取今晚中奖号码
                if (StrUtil.isNotEmpty(params)) {
                    DreamNumer.getDreamNum(params);
                } else DreamNumer.getDreamNum();
                break;
            case "3": //兑奖
                RedeemNum.redeem(params);
                break;
            case "4": //写入号码
                WriteNum.writeMyNumber(params, Integer.parseInt(params2));
                break;
            case "5": //写入号码
                WriteNum.sortJson();
                break;
            default:
                break;
        }
    }

    public static void action(String type) {
        action(type, null, null);
    }

    public static void action(String type, String params) {
        action(type, params, null);
    }

    public static void getToday() {
        action("1");
        action("2", null, null);
    }

}
