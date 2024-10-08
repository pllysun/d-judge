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
        // 初始化语言WebSocket数据传输对象
        LanguageWebSocketDTO languageWebSocketDTO = null;
        try {
            // 尝试从消息中解析出语言WebSocket数据传输对象
            languageWebSocketDTO = JSON.parseObject(message.getPayload(), LanguageWebSocketDTO.class);
        } catch (Exception e) {
            // 如果解析出错，向客户端发送错误消息并返回
            session.sendMessage(jsonMessage("系统错误", "error", "JSON格式错误"));
            return;
        }
        // 如果解析出的对象为空，向客户端发送错误消息并返回
        if (languageWebSocketDTO == null) {
            session.sendMessage(jsonMessage("系统错误", "error", "JSON格式错误"));
            return;
        }
        // 如果语言ID为空，向客户端发送错误消息并返回
        if (languageWebSocketDTO.getLanguageId() == null) {
            session.sendMessage(jsonMessage("系统错误", "error", "languageId不能为空"));
        }
        // 创建查询条件，查询语言安装信息
        LambdaQueryWrapper<LanguageInstall> lambda = new QueryWrapper<LanguageInstall>().lambda();
        lambda.eq(LanguageInstall::getId, languageWebSocketDTO.getLanguageId());
        LanguageInstall languageInstall = languageInstallMapper.selectOne(lambda);
        // 创建查询条件，查询沙箱设置信息
        LambdaQueryWrapper<SandBoxSetting> lambda1 = new QueryWrapper<SandBoxSetting>().lambda();
        lambda1.ne(SandBoxSetting::getState, 2);
        List<SandBoxSetting> sandBoxSettings = sandBoxSettingMapper.selectList(lambda1);
        // 遍历沙箱设置信息
        for (SandBoxSetting sandBoxSetting : sandBoxSettings) {
            // 获取已安装的包
            String installedPackages = CommonUtils.getInstalledPackages(restTemplate, sandBoxSetting);
            // 如果已安装的包中包含当前语言的包名，向客户端发送成功消息并继续下一次循环
            if (installedPackages.contains(languageInstall.getPackageName())) {
                session.sendMessage(jsonMessage(sandBoxSetting.getName(), "success", sandBoxSetting.getName() + "已安装" + languageInstall.getLanguage() + "的语言环境:" + languageInstall.getPackageName()));
                continue;
            }
            // 获取WebSocket会话
            WebSocketSession webSocketSession = getWebSocketSession(session, sandBoxSetting, languageInstall);
            // 创建命令
            String command = "{\"command\":" + "\"" + languageInstall.getPackageName() + "\" }";
            System.out.println(command);
            // 向WebSocket会话发送命令
            webSocketSession.sendMessage(new TextMessage(command));
            // 串行化执行安装步骤
            while (webSocketSession.isOpen()) {
                Thread.sleep(1000);
            }
        }
        // 关闭会话
        session.close();
    }

    private WebSocketSession getWebSocketSession(WebSocketSession session, SandBoxSetting sandBoxSetting, LanguageInstall languageInstall) {
        // 创建WebSocket客户端
        WebSocketClient client = new StandardWebSocketClient();
        // 获取基础URL
        String baseUrl = sandBoxSetting.getBaseUrl();
        // 创建WebSocket URL
        String wsUrl = "ws://" + baseUrl.substring(7);
        // 创建WebSocket处理器
        WebSocketHandler webSocketHandler = new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession ses, TextMessage message) throws Exception {
                // 从消息中解析出安装数据传输对象
                InstallDTO installDTO = JSON.parseObject(message.getPayload(), InstallDTO.class);
                // 如果安装状态为结束
                if (installDTO.getState().equalsIgnoreCase("end")) {
                    // 获取已安装的包
                    String installedPackages = CommonUtils.getInstalledPackages(restTemplate, sandBoxSetting);
                    // 如果已安装的包中包含当前语言的包名，向客户端发送成功消息并插入语言配置
                    if (installedPackages.contains(languageInstall.getPackageName())) {
                        session.sendMessage(jsonMessage(sandBoxSetting.getName(), "success", sandBoxSetting.getName() + "安装" + languageInstall.getLanguage() + "的语言环境:" + languageInstall.getPackageName() + "已安装成功"));
                        LanguageConfig languageConfig = new LanguageConfig(sandBoxSetting.getId(), languageInstall.getId().toString());
                        languageConfigMapper.insert(languageConfig);
                    } else {
                        // 否则，向客户端发送失败消息
                        session.sendMessage(jsonMessage(sandBoxSetting.getName(), "error", sandBoxSetting.getName() + "安装" + languageInstall.getLanguage() + "的语言环境:" + languageInstall.getPackageName() + "安装失败\n失败原因:未找到已安装的环境"));
                    }
                    // 关闭会话
                    ses.close();
                    return;
                }
                // 向客户端发送消息
                session.sendMessage(jsonMessage(sandBoxSetting.getName(), installDTO.getState(), installDTO.getMessage()));
            }
        };
        // 执行WebSocket连接
        CompletableFuture<WebSocketSession> execute = client.execute(webSocketHandler, wsUrl + "/install");
        // 阻塞等待 WebSocket 连接建立
        WebSocketSession webSocketSession = execute.join();
        return webSocketSession;
    }

    private TextMessage jsonMessage(String name, String level, String message) {
        InstallVo installVo = new InstallVo(name, level, message);
        String jsonString = JSON.toJSONString(installVo);
        return new TextMessage(jsonString);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("安装语言环境websocket连接到服务器");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("安装语言环境websocket断开连接");
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("连接异常", exception);
    }


}
