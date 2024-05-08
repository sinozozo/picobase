package com.picobase;

import com.picobase.console.PbConsoleManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 */
@SpringBootApplication
public class StartUpApplication {
	public static void main(String[] args) {
		SpringApplication.run(StartUpApplication.class, args);
		System.out.println("\n启动成功：PicoBase配置如下：" + PbConsoleManager.getConfig());
	}
}
