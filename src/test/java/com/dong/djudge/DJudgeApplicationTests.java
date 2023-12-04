package com.dong.djudge;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.Resource;
import com.dong.djudge.util.CommonUtils;
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
		String path = file.getPath();
		System.out.println(path.substring(1, path.length() - 1).replace("test-classes", "").replace("target/", "")+ "file/");
	}

	@Test
	void test1() throws Exception {
		System.out.println(CommonUtils.getFilePath());
	}

}
