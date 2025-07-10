package com.study.common.controller.rocketMq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductByTemplateTest {

    @Test
    void sendMessage() {
        ProductByTemplate template = new ProductByTemplate(new RocketMQTemplate());
        JSONObject object = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("phone", "18842824295");
        data.put("messageId", "8888");
        data.put("sessionId", "12345678");
        data.put("content", "112");
        object.put("data", data);
        object.put("prcscd", "msgcet");
        object.put("messageId", "8888");
        object.put("service", "esb");
        object.put("sessionId", "local");
        template.sendMessage("myTopic", object);
    }
}