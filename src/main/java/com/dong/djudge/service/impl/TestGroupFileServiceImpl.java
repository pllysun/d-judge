package com.dong.djudge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dong.djudge.entity.FileEntity;
import com.dong.djudge.mapper.FileMapper;
import com.dong.djudge.service.TestGroupFileService;
import com.dong.djudge.util.PathUrlUtils;
import com.dong.djudge.util.RandomUpperGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@Slf4j(topic = "TestGroupFileServiceImpl")
public class TestGroupFileServiceImpl implements TestGroupFileService {
    @Autowired
    private FileMapper fileMapper;

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
        String uri = new PathUrlUtils().getJarFilePath();
        Path path = Paths.get(uri);
        // 创建文件路径
        Path filePath = path.resolve(fileID + ".json");
        try {
            // 将内容写入文件
            Files.write(filePath, value.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("{}文件写入成功,文件路径:{}", fileID, filePath);
        } catch (IOException e) {
            log.error("文件写入失败");
        }
        if(isUpload){
            fileMapper.insert(new FileEntity(fileID, filePath.toString()));
        }
        return fileID;
    }

    @Override
    public void deleteFile(String fileId) throws IOException {
        fileMapper.delete(new LambdaQueryWrapper<FileEntity>().eq(FileEntity::getFileId, fileId));
        // 获取文件路径
        String uri = new PathUrlUtils().getJarFilePath();
        Path path = Paths.get(uri);
        Path filePath = path.resolve(fileId + ".json");
        Files.delete(filePath);
    }

    @Override
    public String getFile(String fileId) {
        return getEntity(fileId).getFilePath();
    }

    @Override
    public void updateFile(String fileId, String value) throws IOException {
        getFileId(fileId, value, false);
    }


    private FileEntity getEntity(String fileId) {
        return fileMapper.selectList(new QueryWrapper<FileEntity>().lambda().eq(FileEntity::getFileId, fileId)).get(0);
    }
}
