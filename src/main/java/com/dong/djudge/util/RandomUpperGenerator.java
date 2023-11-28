package com.dong.djudge.util;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.dong.djudge.entity.TestCaseGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomUpperGenerator {


    public static String generateRandomUpperCaseWithPrefix(String prefix, int length) {
        String randomUUID = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return prefix + randomUUID.substring(0, Math.min(length - prefix.length(), 32));
    }

    public static void main(String[] args) {
        List<TestCaseGroup> testCaseGroups = new ArrayList<>();
        for(int i=0;i<5;i++){
            TestCaseGroup testCaseGroup = new TestCaseGroup();
            testCaseGroup.setId(i);
            List<String> input = new ArrayList<>();
            Random random = new Random();
            int numTestCases = random.nextInt(10) + 10; // Random number between 3 and 100
            for(int j=0;j<numTestCases;j++){
                int a = random.nextInt(100);
                int b = random.nextInt(100);
                input.add(a+" "+b);
                testCaseGroup.setInput(input);
            }
            testCaseGroups.add(testCaseGroup);
        }
        String jsonOutput = JSON.toJSONString(testCaseGroups);
        System.out.println(jsonOutput);
    }

}
