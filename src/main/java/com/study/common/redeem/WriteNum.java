package com.study.common.redeem;/*
  @Author yangx
 * @Description 写号码
 * @Since create in 2024-4-24 11:27:34
 * @Company 广州云趣信息科技有限公司
 */

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.study.common.base.AppBaseNum;
import com.study.common.base.Constants;
import com.study.common.utils.DateUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author yangxu
 * @create 2024/4/24 11:27
 */
public class WriteNum extends AppBaseNum {
    private static final Logger logger = Logger.getLogger(WriteNum.class.getName());
    private static final Gson gson = new Gson();

    public static void writeMyNumber(String number) {
        writeMyNumber(number, 1);
    }

    /**
     * @Author yangxu
     * @Description 把生成的号码写入到history.json
     * @Param: [number, type]
     * @Return: void
     * @Since create in 2024/1/17 14:02
     * @Company 广州云趣信息科技有限公司
     */
    public static void writeMyNumber(String number, int type) {
        String date = (type == 2) ? DateUtils.getYesterdayDate() : DateUtils.getCurrentDate();
        writeNumberToFile(number, date, Constants.getHisFilePath(), "历史号码");
    }

    /*
     * @Author yangxu
     * @Description 启用的号码也保存一下
     * @Param: [number]
     * @Return: void
     * @Since create in 2024/5/14 14:54
     * @Company 广州云趣信息科技有限公司
     */
    public static void writeNotBuyNumber(String number) {
        writeNumberToFile(number, DateUtils.getCurrentDate(), Constants.getNotBuyPath(), "未买号码");
    }

    private static void writeNumberToFile(String number, String date, String filePath, String fileType) {
        File file = new File(filePath);
        JSONObject jsonObject = file.exists() ?
                updateExistingJson(file, date, number) :
                createNewJson(date, number);

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonObject.toJSONString());
            logger.info("号码已成功写入" + fileType + "文件");
        } catch (IOException e) {
            logger.severe("写入" + fileType + "文件失败: " + e.getMessage());
        }
    }

    private static JSONObject updateExistingJson(File file, String date, String number) {
        JSONObject jsonObject = filterJson(file.getPath());
        if (jsonObject == null) {
            return createNewJson(date, number);
        }

        String existingNum = jsonObject.getString(date);
        if (StrUtil.isNotEmpty(existingNum)) {
            if (existingNum.contains(number)) {
                logger.info("该号码已经存在");
                return jsonObject;
            }
            jsonObject.put(date, existingNum + "|" + number);
        } else {
            jsonObject.put(date, number);
        }
        return jsonObject;
    }

    private static JSONObject createNewJson(String date, String number) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(date, number);
        return jsonObject;
    }

    /**
     * @Author yangxu
     * @Description 给json文件里面的内容排序
     * @Param: []
     * @Return: void
     * @Since create in 2024/8/6 11:10
     * @Company 广州云趣信息科技有限公司
     */
    public static void sortJson() {
        List<String> paths = Arrays.asList(
                Constants.getHisFilePath(),
                Constants.getTcFilePath(),
                Constants.getFcFilePath(),
                Constants.getNotBuyPath()
        );

        List<String> outPaths = Arrays.asList(
                Constants.getHisOutFilePath(),
                Constants.getTcFileOutPath(),
                Constants.getFcFileOutPath(),
                Constants.getNotBuyOutPath()
        );

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat fileDate = new SimpleDateFormat("yyyyMMdd");
        String todayDate = fileDate.format(new Date());

        for (int i = 0; i < paths.size(); i++) {
            sortAndRenameFile(paths.get(i), outPaths.get(i), sdf, todayDate);
        }
    }

    private static void sortAndRenameFile(String inputPath, String outputPath, SimpleDateFormat sdf, String todayDate) {
        try (FileReader reader = new FileReader(inputPath)) {
            Type mapType = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> data = gson.fromJson(reader, mapType);

            Map<String, String> sortedData = sortMapByDate(data, sdf);

            try (FileWriter writer = new FileWriter(outputPath)) {
                gson.toJson(sortedData, writer);
            }

            renameFiles(inputPath, outputPath, todayDate);
        } catch (IOException e) {
            logger.severe("文件处理失败: " + e.getMessage());
        }
    }

    private static Map<String, String> sortMapByDate(Map<String, String> data, SimpleDateFormat sdf) {
        return data.entrySet().stream()
                .sorted((e1, e2) -> {
                    try {
                        return sdf.parse(e2.getKey()).compareTo(sdf.parse(e1.getKey()));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        Map::putAll);
    }

    private static void renameFiles(String inputPath, String outputPath, String todayDate) {
        File oriFile = new File(inputPath);
        File outFile = new File(outputPath);

        File newFile = new File(Constants.getBasePath(),
                oriFile.getName().split("\\.")[0] + "_" + todayDate + ".json");
        File newOutFile = new File(Constants.getBasePath(), oriFile.getName());

        if (!oriFile.renameTo(newFile)) {
            logger.warning("Failed to rename " + oriFile.getName());
        }
        if (!outFile.renameTo(newOutFile)) {
            logger.warning("Failed to rename " + outFile.getName());
        }
    }
}
