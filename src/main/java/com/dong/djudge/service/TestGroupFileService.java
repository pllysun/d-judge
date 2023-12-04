package com.dong.djudge.service;

public interface TestGroupFileService {
    String uploadFile(String value) throws Exception;

    void deleteFile(String fileId) throws Exception;

    String getFile(String fileId);

    void updateFile(String fileId, String value) throws Exception;

}
