package com.dong.djudge.service.impl;

import com.dong.djudge.entity.FileEntity;
import com.dong.djudge.mapper.FileMapper;
import com.dong.djudge.service.FileService;
import com.dong.djudge.util.PathUrlUtils;
import com.dong.djudge.util.RandomUpperGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

@Service
@Slf4j(topic = "FileServiceImpl")
public class FileServiceImpl implements FileService {
    @Autowired
    private FileMapper fileMapper;
    @Override
    public String uploadFile(String value) {
        // 生成文件ID,注意:数据库限制，所以文件ID长度不能超过12位
        String fileID = RandomUpperGenerator.generateRandomUpperCaseWithPrefix("AD-", 12);

        // 获取文件路径
        String uri = new PathUrlUtils().getJarFilePath();
        Path path = Paths.get(uri);
        // 创建文件路径
        Path filePath = path.resolve(fileID + ".json");
        try {
            // 将内容写入文件
            Files.write(filePath, value.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            log.info("{}文件写入成功,文件路径:{}", fileID, filePath);
        } catch (IOException e) {
            log.error("文件写入失败");
        }
        fileMapper.insert(new FileEntity(fileID, filePath.toString()));
        return fileID;
    }
}