package com.study.common.base;/*
  @Author yangx
 * @Description 描述
 * @Since create in
 * @Company 广州云趣信息科技有限公司
 */

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yangxu
 * @create 2024/4/24 11:28
 */
public class AppBaseNum {

    public static int i = 1;

    /**
     * @Author yangxu
     * @Description 解析json数据
     * @Param: [jsonFilePath]
     * @Return: com.alibaba.fastjson.JSONObject
     * @Since create in 2024/1/15 11:15
     * @Company 广州云趣信息科技有限公司
     */
    public static JSONObject filterJson(String jsonFilePath) {
        Path path = Paths.get(jsonFilePath);
        byte[] jsonData;
        try {
            jsonData = Files.readAllBytes(path);
            String jsonString = new String(jsonData);
            // 使用Jackson库解析JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return JSONObject.parseObject(jsonNode.toString());
        } catch (Exception e) {
            System.out.println("系统崩溃了……" + e.getMessage());
        }
        return null;
    }


    /**
     * @Author yangxu
     * @Description 开始比对生成的号码在历史中奖信息中相似度
     * @Param: [zjNum]
     * @Return: void
     * @Since create in 2024/1/17 14:08
     * @Company 广州云趣信息科技有限公司
     */
    public static Map<String, String> comparisonNum(List<Integer> a_redArr, List<Integer> a_blueArr, int redSize, int blueSize, JSONObject openData) {
        Map<String, String> similarNumber = new HashMap<>();
        //跟所有的公开数据对比

        for (String key : openData.keySet()) {
            int count = 0;
            String data = openData.getString(key);
            String redNum = Arrays.stream(data.split("\\|"))
                    .limit(redSize)
                    .collect(Collectors.joining("|"));
            List<Integer> redArr = Arrays.stream(redNum.split("\\|"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            for (Integer num : a_redArr) {
                if (redArr.contains(num)) {
                    count++;
                }
            }
            String blueNum = Arrays.stream(data.split("\\|"))
                    .skip(Math.max(0, data.split("\\|").length - blueSize))
                    .collect(Collectors.joining("|"));
            List<Integer> blueArr = Arrays.stream(blueNum.split("\\|"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            for (Integer num : a_blueArr) {
                if (blueArr.contains(num)) {
                    count++;
                }
            }
            if (count >= Constants.similarSize) {
                similarNumber.put(count + "_" + i, data);
                i++;
            }
        }
        return similarNumber;
    }


    /**
     * @Author yangxu
     * @Description 统计开奖号码历史重复数
     * @Param: [redArr, blueArr, redSize, blueSize,openData]
     * @Return: void
     * @Since create in 2024/4/15 15:42
     * @Company 广州云趣信息科技有限公司
     */
    public static void comparisonOpenNum(List<Integer> redArr, List<Integer> blueArr, int redSize, int blueSize, JSONObject openData) {
        //跟所有的公开数据对比
        Map<Integer, Integer> tmp = new HashMap<>();
        for (String key : openData.keySet()) {
            String openNumber = openData.getString(key);
            int redCount = 0;
            int blueCount = 0;
            String redNum = Arrays.stream(openNumber.split("\\|"))
                    .limit(redSize)
                    .collect(Collectors.joining("|"));
            List<Integer> openRedArr = Arrays.stream(redNum.split("\\|"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            String blueNum = Arrays.stream(openNumber.split("\\|"))
                    .skip(Math.max(0, openNumber.split("\\|").length - blueSize))
                    .collect(Collectors.joining("|"));
            List<Integer> openBlueArr = Arrays.stream(blueNum.split("\\|"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            for (Integer num : openRedArr) {
                if (redArr.contains(num)) {
                    redCount++;
                }
            }
            for (Integer num : openBlueArr) {
                if (blueArr.contains(num)) {
                    blueCount++;
                }
            }
            if (redCount + blueCount == 7) {
                continue;
            }
            if (redCount + blueCount > Constants.sameHisSize) {
                int count = redCount + blueCount;
                Integer num = tmp.get(count);
                if (num == null) {
                    tmp.put(count, 1);
                } else {
                    tmp.put(count, num + 1);
                }
                System.out.println("该号码在" + key + "已开奖的号码重复数量:" + (redCount + blueCount) + " 其中重复" + redCount + "个红球和" + blueCount + "个蓝球---->" + openNumber);
            }
        }
        tmp.forEach((key, value) -> {
            // 在这里处理每个键值对
            System.out.println("该号码在历史中奖信息中对比重复" + key + "个的号码数量" + value + "个");
        });
    }


}
