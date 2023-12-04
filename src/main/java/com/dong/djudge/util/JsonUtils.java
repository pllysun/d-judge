package com.dong.djudge.util;

import com.alibaba.fastjson2.JSON;
import com.dong.djudge.entity.TestCaseGroupRoot;

import java.util.List;

public class JsonUtils {
    public static boolean isValidJson(String jsonString) {
        try {
            JSON.parseArray(jsonString, TestCaseGroupRoot.class);
            return false;
        } catch (Exception e) {
            // 解析失败，说明不是有效的JSON
            return true;
        }
    }

    public static List<TestCaseGroupRoot> getTestCaseGroupList(String jsonString) {
        return JSON.parseArray(jsonString, TestCaseGroupRoot.class);
    }


}
