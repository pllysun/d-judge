package com.dong.djudge.service;

import java.io.IOException;

public interface TestGroupFileService {
    public String uploadFile(String value);

    void deleteFile(String fileId) throws IOException;

    String getFile(String fileId);

    void updateFile(String fileId,String value) throws IOException;

}
