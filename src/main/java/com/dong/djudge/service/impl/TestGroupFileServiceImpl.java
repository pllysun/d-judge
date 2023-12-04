package com.dong.djudge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dong.djudge.entity.TestGroupEntity;
import com.dong.djudge.mapper.TestGroupMapper;
import com.dong.djudge.service.TestGroupFileService;
import com.dong.djudge.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j(topic = "TestGroupFileServiceImpl")
public class TestGroupFileServiceImpl implements TestGroupFileService {
    @Autowired
    private TestGroupMapper fileMapper;

    @Override
    public String uploadFile(String value) throws Exception {
        return getFileId(null, value, true);
    }

    private String getFileId(String fileId, String value, boolean isUpload) throws Exception {
        String fileID;
        if(isUpload){
            // 生成文件ID,注意:数据库限制，所以文件ID长度不能超过12位
             fileID = CommonUtils.generateRandomUpperCaseWithPrefix("AD-", 12);
        }else{
            fileID=fileId;
        }
        String path = CommonUtils.writeFile(fileID, value);
        if(isUpload){
            fileMapper.insert(new TestGroupEntity(fileID, path));
        }
        return fileID;
    }



    @Override
    public void deleteFile(String fileId) throws Exception {
        fileMapper.delete(new LambdaQueryWrapper<TestGroupEntity>().eq(TestGroupEntity::getTestGroupId, fileId));
        // 获取文件路径
        String uri = CommonUtils.getFilePath();
        Path path = Paths.get(uri);
        Path filePath = path.resolve(fileId + ".json");
        Files.delete(filePath);
    }

    @Override
    public String getFile(String fileId) {
        return getEntity(fileId).getTestGroupPath();
    }

    @Override
    public void updateFile(String fileId, String value) throws Exception {
        getFileId(fileId, value, false);
    }


    private TestGroupEntity getEntity(String fileId) {
        return fileMapper.selectList(new QueryWrapper<TestGroupEntity>().lambda().eq(TestGroupEntity::getTestGroupId, fileId)).get(0);
    }
}
