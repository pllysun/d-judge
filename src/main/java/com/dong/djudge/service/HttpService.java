package com.dong.djudge.service;

/**
 * @author 阿东
 * @since 2023/9/7 [4:16]
 */
public interface HttpService {
    /**
     * 通过url获取文件
     *
     * @param url oss的url
     * @return 文件内容
     */
    String getInputFileForOss(String url);
}
