package com.dong.djudge.util;

import com.alibaba.fastjson2.JSON;
import com.dong.djudge.entity.TestCaseGroup;

public class JsonUtils {
    public static boolean isValidJson(String jsonString) {
        try {
            JSON.parseObject(jsonString, TestCaseGroup.class);
            return true;
        } catch (Exception e) {
            // 解析失败，说明不是有效的JSON
            return false;
        }
    }

}
