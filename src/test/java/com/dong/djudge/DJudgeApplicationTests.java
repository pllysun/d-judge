package com.dong.djudge;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.Resource;
import org.apache.tika.Tika;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.net.URL;

@SpringBootTest
class DJudgeApplicationTests {

	@Test
	void contextLoads() {
		Resource resource = new ClassPathResource("");
		URL file = resource.getUrl();
		System.out.println(file.getPath().replace("test-classes/", "").replace("target/", "").replace("/G", "G") + "file/");

	}

}
