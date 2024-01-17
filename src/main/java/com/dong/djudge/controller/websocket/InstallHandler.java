package com.dong.djudge.controller.websocket;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dong.djudge.dto.InstallDTO;
import com.dong.djudge.dto.InstallVo;
import com.dong.djudge.dto.LanguageWebSocketDTO;
import com.dong.djudge.mapper.LanguageConfigMapper;
import com.dong.djudge.mapper.LanguageInstallMapper;
import com.dong.djudge.mapper.SandBoxSettingMapper;
import com.dong.djudge.mapper.StandardCodeMapper;
import com.dong.djudge.pojo.LanguageConfig;
import com.dong.djudge.pojo.LanguageInstall;
import com.dong.djudge.pojo.SandBoxSetting;
import com.dong.djudge.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Configuration
@Slf4j
public class InstallHandler extends TextWebSocketHandler {

    private final LanguageConfigMapper languageConfigMapper = SpringUtil.getBean(LanguageConfigMapper.class);
    private final LanguageInstallMapper languageInstallMapper = SpringUtil.getBean(LanguageInstallMapper.class);
    private final RestTemplate restTemplate = SpringUtil.getBean(RestTemplate.class);
    private final SandBoxSettingMapper sandBoxSettingMapper = SpringUtil.getBean(SandBoxSettingMapper.class);

    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        LanguageWebSocketDTO languageWebSocketDTO = null;
        try {
            languageWebSocketDTO = JSON.parseObject(message.getPayload(), LanguageWebSocketDTO.class);
        } catch (Exception e) {
            session.sendMessage(jsonMessage("error", "JSON格式错误"));
            return;
        }
        if (languageWebSocketDTO == null) {
            session.sendMessage(jsonMessage("error", "JSON格式错误"));
            return;
        }
        if (languageWebSocketDTO.getLanguageId() == null) {
            session.sendMessage(jsonMessage("error", "languageId不能为空"));
        }
        LambdaQueryWrapper<LanguageInstall> lambda = new QueryWrapper<LanguageInstall>().lambda();
        lambda.eq(LanguageInstall::getId, languageWebSocketDTO.getLanguageId());
        LanguageInstall languageInstall = languageInstallMapper.selectOne(lambda);
        LambdaQueryWrapper<SandBoxSetting> lambda1 = new QueryWrapper<SandBoxSetting>().lambda();
        lambda1.ne(SandBoxSetting::getState, 2);
        List<SandBoxSetting> sandBoxSettings = sandBoxSettingMapper.selectList(lambda1);
        for (SandBoxSetting sandBoxSetting : sandBoxSettings) {
            String installedPackages = CommonUtils.getInstalledPackages(restTemplate, sandBoxSetting);
            if (installedPackages.contains(languageInstall.getPackageName())) {
                session.sendMessage(jsonMessage("success", sandBoxSetting.getName() + "已安装" + languageInstall.getLanguage() + "的语言环境:" + languageInstall.getPackageName()));
                continue;
            }
            WebSocketSession webSocketSession = getWebSocketSession(session, sandBoxSetting, languageInstall);
            String command = "{\"command\":" + "\"" + languageInstall.getPackageName() + "\" }";
            System.out.println(command);
            webSocketSession.sendMessage(new TextMessage(command));
            //串行化执行安装步骤
            while (webSocketSession.isOpen()) {
                Thread.sleep(1000);
            }
        }
        session.close();
    }

    private WebSocketSession getWebSocketSession(WebSocketSession session, SandBoxSetting sandBoxSetting, LanguageInstall languageInstall) {
        WebSocketClient client = new StandardWebSocketClient();
        String baseUrl = sandBoxSetting.getBaseUrl();
        String wsUrl = "ws://" + baseUrl.substring(7);
        WebSocketHandler webSocketHandler = new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession ses, TextMessage message) throws Exception {
                InstallDTO installDTO = JSON.parseObject(message.getPayload(), InstallDTO.class);
                if (installDTO.getState().equalsIgnoreCase("end")) {
                    String installedPackages = CommonUtils.getInstalledPackages(restTemplate, sandBoxSetting);
                    if (installedPackages.contains(languageInstall.getPackageName())) {
                        session.sendMessage(jsonMessage("success", sandBoxSetting.getName() + "安装" + languageInstall.getLanguage() + "的语言环境:" + languageInstall.getPackageName() + "已安装成功"));
                        LanguageConfig languageConfig = new LanguageConfig(sandBoxSetting.getId(), languageInstall.getId().toString());
                        languageConfigMapper.insert(languageConfig);
                    } else {
                        session.sendMessage(jsonMessage("error", sandBoxSetting.getName() + "安装" + languageInstall.getLanguage() + "的语言环境:" + languageInstall.getPackageName() + "安装失败\n失败原因:未找到已安装的环境"));
                    }
                    ses.close();
                    return;
                }
                session.sendMessage(jsonMessage(installDTO.getState(), installDTO.getMessage()));
            }
        };
        CompletableFuture<WebSocketSession> execute = client.execute(webSocketHandler, wsUrl + "/install");
        // 阻塞等待 WebSocket 连接建立
        WebSocketSession webSocketSession = execute.join();
        return webSocketSession;
    }


    private TextMessage jsonMessage(String level, String message) {
        InstallVo installVo = new InstallVo(level, message);
        String jsonString = JSON.toJSONString(installVo);
        return new TextMessage(jsonString);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("连接到服务器");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("连接关闭：" + status.getReason());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("传输错误：" + exception.getMessage());
    }


}
