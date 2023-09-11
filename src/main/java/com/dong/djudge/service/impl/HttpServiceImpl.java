package com.dong.djudge.service.impl;

import com.dong.djudge.service.HttpService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author 阿东
 * @since 2023/9/7 [4:16]
 */
@Service
public class HttpServiceImpl implements HttpService {
    private final RestTemplate restTemplate;

    public HttpServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getInputFileForOss(String url) {
        return restTemplate.getForObject(url, String.class);
    }
}
