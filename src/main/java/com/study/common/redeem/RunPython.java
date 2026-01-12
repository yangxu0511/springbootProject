package com.study.common.redeem;

import cn.hutool.json.JSONTokener;
import com.alibaba.fastjson.JSONObject;
import com.study.common.base.Constants;
import com.study.common.utils.DateUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class RunPython {

    /*
     * @Author yangxu
     * @Description 爬虫生成最新的开奖号码
     * @Param:
     * @Return: void
     * @Since create in 2024/1/15 14:11
     * @Company 广州云趣信息科技有限公司
     */
    public static void run() {
        try {
            //判断今天有没有执行过Python
            File file = new File(Constants.ifRunPath);
            String date = DateUtils.getCurrentDate();

            DreamNumer instance = new DreamNumer();
            // 创建JSON对象并设置键值对
            JSONObject jsonObject = new JSONObject();
            if (file.exists()) { //文件已经存在就取出来然后重新写入
                jsonObject = instance.filterJson(Constants.ifRunPath);
                Boolean flag = jsonObject.getBoolean(date);
                if (flag != null && flag) {
                    System.out.println("今天已经执行过python脚本了不再执行爬数脚本。");
                    return;
                }
            }
            System.out.println("开始启动python脚本...");

            // 创建 ProcessBuilder 对象，设置要执行的命令
            List<String> commandList = new ArrayList<>();
            commandList.add(Constants.pythonexe);
            commandList.add(Constants.pythonScriptPath);
            ProcessBuilder pb = new ProcessBuilder(commandList);

            // 启动进程并等待脚本执行完毕
            Process process = pb.start();
            int exitCode = process.waitFor();

            // 获取脚本输出结果
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // 打印脚本执行结果
            if (exitCode == 0) {
                System.out.println("Python script executed successfully.");
                jsonObject.put(date, true);
                // 创建 FileWriter 对象
                FileWriter fileWriter = new FileWriter(Constants.ifRunPath);
                // 将 JSON 对象写入文件
                fileWriter.write(jsonObject.toJSONString());
                fileWriter.close();
                // Read the data from the bak file
                JSONObject bakDltObject = instance.filterJson(Constants.getTcBakFilePath());
                JSONObject dltObject = instance.filterJson(Constants.getTcFilePath());
                dltObject.putAll(bakDltObject);
                FileWriter fileWriter2 = new FileWriter(Constants.getTcFilePath());
                fileWriter2.write(dltObject.toJSONString());
                fileWriter2.close();

                // Read the data from the bak file
                JSONObject bakSsqObject = instance.filterJson(Constants.getFcBakFilePath());
                JSONObject ssqObject = instance.filterJson(Constants.getFcFilePath());
                ssqObject.putAll(bakSsqObject);
                FileWriter fileWriter3 = new FileWriter(Constants.getFcFilePath());
                fileWriter3.write(ssqObject.toJSONString());
                fileWriter3.close();
                System.out.println("Data from bak.json has been merged into dlt/ssq.json successfully.");
            } else {
                System.out.println("Python script execution failed with exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("执行Python脚本出错了……-->" + e.getMessage());
        }

    }

}
