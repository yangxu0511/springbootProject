package com.study.common.controller.common;

import com.study.common.model.TestModel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("hello")
public class HelloController {

    @ResponseBody
    @RequestMapping("/hello")
    public String hello() {
        return "hello world";
    }

    public static void main(String[] args) {
        String file1Path = "C:\\Users\\yangxu\\Desktop\\3.xlsx";
        String file2Path = "C:\\Users\\yangxu\\Desktop\\4.xlsx";
        String resultPath = "C:\\Users\\yangxu\\Desktop\\result2.xlsx";
        List<Map<String, String>> result = new ArrayList<>();

        try (FileInputStream fis1 = new FileInputStream(file1Path);
             FileInputStream fis2 = new FileInputStream(file2Path);
             XSSFWorkbook workbook1 = new XSSFWorkbook(fis1);
             XSSFWorkbook workbook2 = new XSSFWorkbook(fis2)) {

            XSSFSheet sheet1 = workbook1.getSheetAt(0);
            XSSFSheet sheet2 = workbook2.getSheetAt(0);

            // 获取表头索引
            Map<String, Integer> headers1 = getHeaderIndexMap(sheet1.getRow(0));
            Map<String, Integer> headers2 = getHeaderIndexMap(sheet2.getRow(0));

            System.out.println("开始比对数据...");

            // 遍历第一个文件的所有行
            for (int i = 1; i <= sheet1.getLastRowNum(); i++) {
                XSSFRow row1 = sheet1.getRow(i);
                if (row1 == null) continue;

                // 获取当前行的关键值
                String name1 = getCellValue(row1.getCell(headers1.get("资费名称")));
                String reportNo1 = getCellValue(row1.getCell(headers1.get("方案编号")));
                String provinceName = getCellValue(row1.getCell(headers1.get("订购省份")));
                String entName = getCellValue(row1.getCell(headers1.get("企业")));
                String firstDate = getCellValue(row1.getCell(headers1.get("首次出现日期")));
                String endDate = getCellValue(row1.getCell(headers1.get("最后出现日期")));

                // 遍历第二个文件寻找匹配
                for (int j = 1; j <= sheet2.getLastRowNum(); j++) {
                    XSSFRow row2 = sheet2.getRow(j);
                    if (row2 == null) continue;

                    String name2 = getCellValue(row2.getCell(headers2.get("资费名称")));
                    String reportNo2 = getCellValue(row2.getCell(headers2.get("方案编号")));
                    String provinceName1 = getCellValue(row2.getCell(headers2.get("订购省份")));
                    String entName1 = getCellValue(row2.getCell(headers2.get("企业")));
                    String onlineDay = getCellValue(row2.getCell(headers2.get("上线日期")));
                    String offlineDay = getCellValue(row2.getCell(headers2.get("下线日期")));
                    // 检查是否满足匹配条件
                    if (reportNo1.equals(reportNo2) ||
                            (name1.equals(name2) && provinceName.equals(provinceName1) && entName.equals(entName1))
                    ) {
                        Map<String, String> matchedRow = new HashMap<>();
                        matchedRow.put("name", name1);
                        matchedRow.put("province_name", provinceName1);
                        matchedRow.put("ent_name", entName1);
                        matchedRow.put("REPORT_NO", reportNo1);
                        matchedRow.put("ONLINE_DAY", onlineDay);
                        matchedRow.put("OFFLINE_DAY", offlineDay);
                        matchedRow.put("firstDate", firstDate);
                        matchedRow.put("endDate", endDate);
                        result.add(matchedRow);

                        // 打印匹配的记录
//                        System.out.println("找到匹配记录：" +
//                            "名称=" + name1 +
//                            ", 省份=" + provinceName1 +
//                            ", 企业名称=" + entName1 +
//                            ", 报告编号=" + reportNo1);
                        break;
                    }
                }
            }

            System.out.println("比对完成，共找到 " + result.size() + " 条匹配记录");

            // 创建新的工作簿保存结果
            try (XSSFWorkbook resultWorkbook = new XSSFWorkbook()) {
                XSSFSheet resultSheet = resultWorkbook.createSheet("匹配结果");

                // 创建表头
                XSSFRow headerRow = resultSheet.createRow(0);
                String[] headers = {"资费名称", "省份", "企业", "方案编号", "上线日期", "下线日期", "首次出现日期", "最后出现日期"};
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                // 写入数据
                for (int i = 0; i < result.size(); i++) {
                    XSSFRow dataRow = resultSheet.createRow(i + 1);
                    Map<String, String> rowData = result.get(i);

                    dataRow.createCell(0).setCellValue(rowData.get("name"));
                    dataRow.createCell(1).setCellValue(rowData.get("province_name"));
                    dataRow.createCell(2).setCellValue(rowData.get("ent_name"));
                    dataRow.createCell(3).setCellValue(rowData.get("REPORT_NO"));
                    dataRow.createCell(4).setCellValue(rowData.get("ONLINE_DAY"));
                    dataRow.createCell(5).setCellValue(rowData.get("OFFLINE_DAY"));
                    dataRow.createCell(6).setCellValue(rowData.get("firstDate"));
                    dataRow.createCell(7).setCellValue(rowData.get("endDate"));
                }

                // 自动调整列宽
                for (int i = 0; i < headers.length; i++) {
                    resultSheet.autoSizeColumn(i);
                }

                // 保存文件
                try (FileOutputStream fileOut = new FileOutputStream(resultPath)) {
                    resultWorkbook.write(fileOut);
                    System.out.println("结果已保存到：" + resultPath);
                }
            }

        } catch (IOException e) {
            System.err.println("处理Excel文件时发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Map<String, Integer> getHeaderIndexMap(XSSFRow headerRow) {
        Map<String, Integer> headers = new HashMap<>();
        if (headerRow != null) {
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                String headerName = getCellValue(headerRow.getCell(i));
                headers.put(headerName, i);
            }
        }
        return headers;
    }

    private static String getCellValue(XSSFCell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BLANK:
                return "";
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
