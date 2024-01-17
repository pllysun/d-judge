package com.dong.djudge.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dong.djudge.mapper.LanguageConfigMapper;
import com.dong.djudge.mapper.LanguageInstallMapper;
import com.dong.djudge.mapper.SandBoxSettingMapper;
import com.dong.djudge.pojo.LanguageConfig;
import com.dong.djudge.pojo.LanguageInstall;
import com.dong.djudge.pojo.SandBoxSetting;
import com.dong.djudge.util.CommonUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

/**
 * 初始化SqlLite数据库
 */
@Component
@Slf4j
public class SqliteConfig {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("classpath:sql/init.sql")
    private Resource sqlFile;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LanguageConfigMapper languageConfigMapper;

    @Autowired
    private SandBoxSettingMapper sandBoxSettingMapper;

    @Autowired
    private LanguageInstallMapper languageInstallMapper;

    /**
     * 初始化SqlLite数据库
     */
    @PostConstruct
    public void init() {
        if (!isTableExist()) {
            try (Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
                ScriptUtils.executeSqlScript(conn, sqlFile);
                log.info("初始化数据库成功");
            } catch (SQLException e) {
                log.error("初始化数据库失败", e);
                throw new RuntimeException(e);
            }
        } else {
            log.info("数据库已经被初始化，如果要重新加载init.sql，可以删除db目录下的judge.db文件然后重新启动服务器。");
        }
        manageLanguageConfig();
    }

    private boolean isTableExist() {
        try (Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT 1 FROM " + "language_dictionary" + " LIMIT 1");
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 监测沙盒服务器内部是否存在相关语言环境
     */
    private void manageLanguageConfig(){
        List<SandBoxSetting> sandBoxSettings = sandBoxSettingMapper.selectList(null);
        for (SandBoxSetting sandBoxSetting : sandBoxSettings) {
            String installedPackages = CommonUtils.getInstalledPackages(restTemplate, sandBoxSetting);
            List<LanguageInstall> languageInstalls = languageInstallMapper.selectList(null);
            for (LanguageInstall languageInstall : languageInstalls) {
                if(installedPackages.contains(languageInstall.getPackageName())){
                    LambdaQueryWrapper<LanguageConfig> lambda = new QueryWrapper<LanguageConfig>().lambda();
                    lambda.eq(LanguageConfig::getLanguageId,languageInstall.getId());
                    lambda.eq(LanguageConfig::getServerId,sandBoxSetting.getId());
                    LanguageConfig lgc = languageConfigMapper.selectOne(lambda);
                    if(lgc==null){
                        LanguageConfig languageConfig = new LanguageConfig(sandBoxSetting.getId(), languageInstall.getId().toString());
                        languageConfigMapper.insert(languageConfig);
                    }
                }
            }
        }

    }




}