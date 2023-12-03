package com.dong.djudge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dong.djudge.entity.TestGroupEntity;
import com.dong.djudge.mapper.TestGroupMapper;
import com.dong.djudge.service.TestGroupFileService;
import com.dong.djudge.util.TestGroupUtils;
import com.dong.djudge.util.RandomUpperGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@Slf4j(topic = "TestGroupFileServiceImpl")
public class TestGroupFileServiceImpl implements TestGroupFileService {
    @Autowired
    private TestGroupMapper fileMapper;

    @Override
    public String uploadFile(String value) {
        return getFileId(null, value, true);
    }

    private String getFileId(String fileId, String value, boolean isUpload) {
        String fileID;
        if(isUpload){
            // 生成文件ID,注意:数据库限制，所以文件ID长度不能超过12位
             fileID = RandomUpperGenerator.generateRandomUpperCaseWithPrefix("AD-", 12);
        }else{
            fileID=fileId;
        }
        // 获取文件路径
        String uri = new TestGroupUtils().getJarFilePath();
        Path path = Paths.get(uri);
        // 创建文件路径
        Path filePath = path.resolve(fileID + ".json");
        try {
            // 将内容写入文件
            // 创建一个FileWriter对象，用于写入文件
            FileWriter fileWriter = new FileWriter(filePath.toString());

            // 创建一个BufferedWriter对象，用于提高写入性能
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(value);

            // 关闭流
            bufferedWriter.close();
            fileWriter.close();

            System.out.println("内容已成功写入文件。");
            log.info("{}文件写入成功,文件路径:{}", fileID, filePath);
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error("文件写入失败");
        }
        if(isUpload){
            fileMapper.insert(new TestGroupEntity(fileID, filePath.toString()));
        }
        return fileID;
    }

    @Override
    public void deleteFile(String fileId) throws IOException {
        fileMapper.delete(new LambdaQueryWrapper<TestGroupEntity>().eq(TestGroupEntity::getTestGroupId, fileId));
        // 获取文件路径
        String uri = new TestGroupUtils().getJarFilePath();
        Path path = Paths.get(uri);
        Path filePath = path.resolve(fileId + ".json");
        Files.delete(filePath);
    }

    @Override
    public String getFile(String fileId) {
        return getEntity(fileId).getTestGroupPath();
    }

    @Override
    public void updateFile(String fileId, String value) throws IOException {
        getFileId(fileId, value, false);
    }


    private TestGroupEntity getEntity(String fileId) {
        return fileMapper.selectList(new QueryWrapper<TestGroupEntity>().lambda().eq(TestGroupEntity::getTestGroupId, fileId)).get(0);
    }
}
