package com.dot.comm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.boot.web.context.WebServerInitializedEvent;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.sql.Connection;

/**
 * 应用启动监听器 - 用于调试启动过程
 */
@Slf4j
@Component
public class StartupListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("=== 应用启动完成事件触发 ===");
        
        // 测试数据库连接
        testDatabaseConnection();
        
        // 测试Redis连接
        testRedisConnection();
        
        // 检查端口绑定
        checkPortBinding();
        
        log.info("=== 启动检查完成 ===");
    }

    private void testDatabaseConnection() {
        try {
            if (dataSource != null) {
                Connection connection = dataSource.getConnection();
                log.info("✅ 数据库连接测试成功: {}", connection.getMetaData().getURL());
                connection.close();
            } else {
                log.warn("⚠️ 数据源未配置或初始化失败");
            }
        } catch (Exception e) {
            log.error("❌ 数据库连接测试失败", e);
        }
    }

    private void testRedisConnection() {
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set("startup_test", "success");
                String result = redisTemplate.opsForValue().get("startup_test");
                if ("success".equals(result)) {
                    log.info("✅ Redis连接测试成功");
                    redisTemplate.delete("startup_test");
                } else {
                    log.warn("⚠️ Redis连接测试返回异常结果: {}", result);
                }
            } else {
                log.warn("⚠️ Redis模板未配置或初始化失败");
            }
        } catch (Exception e) {
            log.error("❌ Redis连接测试失败", e);
        }
    }

    private void checkPortBinding() {
        try {
            // 这里可以添加端口检查逻辑
            log.info("✅ 端口绑定检查 - 请手动验证8089和9326端口");
        } catch (Exception e) {
            log.error("❌ 端口绑定检查失败", e);
        }
    }
} 