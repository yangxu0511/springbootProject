/**
 * 文件名：CommonController
 * Autor：YangXu
 * 时间：2021/5/9 21:20
 * 描述：
 */
package com.study.common.controller.common;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.stereotype.Controller;

import javax.servlet.http.Part;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Controller("/common")
public class CommonController {


//    /**
//     * @api {post} /dg-etl-mgr/servlet/excelSource?action=excelUpload 1:导入EXCEL数据源
//     * @apiDescription 导入EXCEL数据源
//     * @apiVersion 1.0.0
//     * @apiName 1:导入EXCEL数据源
//     * @apiGroup EXCEL数据源
//     * @apiParam {File} file Excel文件
//     * @apiParamExample {File file} 请求示例
//     * {
//     *		File:file
//     * }
//     *
//     * @apiSuccessExample {json} 成功返回示例
//     * {
//     *     "state":1,
//     *     "msg":"操作成功"
//     * }
//     *
//     * @Author yangxu
//     * @Description excel数据源导入
//     * @Param: []
//     * @Return: org.easitline.common.core.web.EasyResult
//     * @Since create in 2023/10/25 16:12
//     * @Company 广州云趣信息科技有限公司
//     */
//    public void actionForExcelUpload() {
//        EasyResult result = new EasyResult();
//        try {
//            Part part = getRequest().getPart("file");
//            MyExcelListener listener = new MyExcelListener();
//            listener.setStartRowNum(1);
//            listener.setStartColumnNum(1);
//            listener.setNameAndTypeFlag(true);
//            EasyExcel.read(part.getInputStream(), listener).sheet().doRead();
//
//            String fileId = RandomKit.uuid();
//            //保存数据源
//            String dsName = getFilename(part);
//            String dsId = RandomKit.uuid();
//
//            JSONObject record = new JSONObject();
//            record.put("SRC_DS_ID",dsId);
//            record.put("SRC_DS_TYPE", Constants.DS_TYPE_EXCEL);
//            record.put("SRC_DS_NAME",dsName);
//            record.put("CREATE_TIME",EasyDate.getCurrentDateString());
//            RedissonUtil.set("import_data_DG_SRC_DS"+fileId,record);
//
//
//            //保存excel文件数据
//            Map<String, String> headValAndTypeMap = listener.getHeadValAndTypeMap();
//            Map<Integer, String> headMap = listener.getHeadMap();
//            Map<Integer, String> headLableMap = listener.getHeadLableMap();
//
//            List<Map<Integer, String>> valList = listener.getValList();
//            JSONObject headInfo = new JSONObject();
//
//            //保存excel文件
//            JSONObject excelRecord = new JSONObject();
//            excelRecord.put("DS_ID", dsId);
//            excelRecord.put("FILE_NAME",getFilename(part));
//            excelRecord.put("UPLOAD_TIME", EasyDate.getCurrentDateString());
//            excelRecord.put("ROW_COUNT",valList.size());
//            RedissonUtil.set("import_data_DG_EXCEL_FILE"+fileId,excelRecord);
//
//            //添加表头数据
//            JSONObject headRecord = new JSONObject();
//            headRecord.put("ROW_ID",RandomKit.orderId());
//
//            String[] sortedValues = new String[headMap.size()];
//            JSONArray headConfigArr = new JSONArray();
//            JSONObject headConfig = new JSONObject();
//
//            for (int i = 0; i < headMap.size(); i++) {
//                sortedValues[i] = headLableMap.get(i); //改用别名
//                headConfig.put("columnLabel",headMap.get(i));
//                headConfig.put("column",headLableMap.get(i));
//                headConfig.put("columnType",headValAndTypeMap.get(String.valueOf(i)+"_type"));
//                headConfig.put("show",false);
//
//                //当一个对象包含另一个对象时，fastjson就会把该对象解析成引用。引用是通过$ref表示 需要采用禁止循环引用
//                String jsonTmp = JSON.toJSONString(headConfig, SerializerFeature.DisableCircularReferenceDetect);
//                headConfigArr.add(JSONObject.parseObject(jsonTmp));
//            }
//            headInfo.put("headArr", sortedValues);
//            headInfo.put("headConfig",headConfigArr);
//            headRecord.put("ROW_JSON",headInfo);
//            headRecord.put("IS_TITLE","1");
//
//            RedissonUtil.set("import_data_HEADRECORD"+fileId,headRecord);
//            List<JSONObject> dataList = new ArrayList<>();
//            //添加excel数据
////			EasyRecord dataRecord  = new EasyRecord("DG_EXCEL_FILE_DATA","ROW_ID");
//
//            JSONObject dataJson = new JSONObject();
//            List<JSONObject> data = new ArrayList<JSONObject>();
//            for(int i =0;i<valList.size();i++){
//                Map<Integer, String> map = valList.get(i);
//                for (Integer key : map.keySet()) {
//                    String value = map.get(key);
//                    dataJson.put(headLableMap.get(key),value);
//                }
//                JSONObject dataRecord = new JSONObject();
//                dataRecord.put("IS_TITLE",0);
//                dataRecord.put("ROW_ID",RandomKit.orderId());
//                dataRecord.put("ROW_JSON",dataJson.toJSONString());
//
//                //当一个对象包含另一个对象时，fastjson就会把该对象解析成引用。引用是通过$ref表示 需要采用禁止循环引用
//                String jsonTmp = JSON.toJSONString(dataJson, SerializerFeature.DisableCircularReferenceDetect);
//                if(dataList.size()<=20){
//                    data.add(JSONObject.parseObject(jsonTmp));
//                }
//                dataList.add(dataRecord);
//            }
//            RedissonUtil.set("import_data_DATALIST"+fileId,dataList);
//            JSONObject resJson = new JSONObject();
//            resJson.put("fileId",fileId);
//            resJson.put("headArr",sortedValues );
//            resJson.put("headConfig",headConfigArr);
//            resJson.put("dataList",data);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private String getFilename(Part part) {
        String contentDispositionHeader = part.getHeader("content-disposition");
        String[] elements = contentDispositionHeader.split(";");
        for (String element : elements) {
            if (element.trim().startsWith("filename")) {
                return element.substring(element.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }


    public static void main(String[] args) {
        String expression = "aaa*BBB"; // 前端传过来的公式
        // 使用正则表达式校验公式
        if (isValidExpression(expression)) {
            System.out.println("公式 " + expression + " 符合基本算术运算规则");
        } else {
            System.err.println("公式 " + expression + " 不符合基本算术运算规则");
        }


    }

    public static boolean isValidExpression(String expression) {
        // 使用正则表达式匹配公式
        String pattern = "^\\s*\\(\\s*[a-zA-Z]+\\s*(?:[+\\-*/]\\s*[a-zA-Z]+\\s*)*\\)\\s*/\\s*[a-zA-Z]+\\s*$";
        return Pattern.matches(pattern, expression);
    }


}
