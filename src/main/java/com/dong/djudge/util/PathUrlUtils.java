package com.dong.djudge.util;

import org.springframework.boot.system.ApplicationHome;

import java.io.File;

public class PathUrlUtils {
    public String getJarFilePath() {
        ApplicationHome home = new ApplicationHome(getClass());
        File jarFile = home.getSource();
        return jarFile.getParentFile().toString();
    }

}
