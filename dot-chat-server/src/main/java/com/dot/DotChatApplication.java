package com.dot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ComponentScan(basePackages = {"com.dot.msg", "com.dot.sys","com.dot.deepseek", "com.dot.comm.config", "com.dot.comm.manager", "com.dot.comm.exception"})
@MapperScan(basePackages = {"com.dot.msg.*.dao", "com.dot.deepseek.dao"})
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class DotChatApplication {

    public static void main(String[] args) {
        try {
            log.info("=== 星星点点聊天服务器启动开始 ===");
            log.info("Java版本: {}", System.getProperty("java.version"));
            log.info("工作目录: {}", System.getProperty("user.dir"));
            log.info("启动参数: {}", String.join(" ", args));
            
            SpringApplication.run(DotChatApplication.class, args);
            
            log.info("=== 星星点点聊天服务器启动成功 ===");
            log.info("服务端口: 8089");
            log.info("WebSocket端口: 9326");
            log.info("访问地址: http://localhost:8089");
        } catch (Exception e) {
            log.error("=== 星星点点聊天服务器启动失败 ===", e);
            log.error("错误类型: {}", e.getClass().getSimpleName());
            log.error("错误信息: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("根本原因: {}", e.getCause().getMessage());
            }
            System.exit(1);
        }
    }
}
