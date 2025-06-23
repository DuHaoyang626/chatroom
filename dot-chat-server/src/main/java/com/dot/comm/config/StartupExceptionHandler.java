package com.dot.comm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 启动异常处理器
 */
@Slf4j
@Component
public class StartupExceptionHandler implements ApplicationListener<ApplicationFailedEvent> {

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        log.error("=== 应用启动失败 ===");
        
        Throwable exception = event.getException();
        log.error("异常类型: {}", exception.getClass().getSimpleName());
        log.error("异常信息: {}", exception.getMessage());
        
        // 递归打印所有原因
        Throwable cause = exception.getCause();
        int level = 1;
        while (cause != null && level <= 5) {
            log.error("原因{}: {} - {}", level, cause.getClass().getSimpleName(), cause.getMessage());
            cause = cause.getCause();
            level++;
        }
        
        // 打印完整堆栈
        log.error("完整异常堆栈:", exception);
        
        // 根据异常类型给出建议
        provideSuggestions(exception);
    }
    
    private void provideSuggestions(Throwable exception) {
        String exceptionName = exception.getClass().getSimpleName();
        String message = exception.getMessage();
        
        log.error("=== 问题诊断建议 ===");
        
        if (exceptionName.contains("DataSource") || (message != null && message.contains("database"))) {
            log.error("🔍 数据库连接问题:");
            log.error("  1. 检查MySQL服务是否启动 (端口3306)");
            log.error("  2. 检查数据库用户名密码: dot_chat/war2023");
            log.error("  3. 检查数据库是否存在: dot_chat");
            log.error("  4. 运行: netstat -an | findstr 3306");
        }
        
        if (exceptionName.contains("Redis") || (message != null && message.contains("redis"))) {
            log.error("🔍 Redis连接问题:");
            log.error("  1. 检查Redis服务是否启动 (端口6379)");
            log.error("  2. 运行: netstat -an | findstr 6379");
        }
        
        if (exceptionName.contains("Port") || exceptionName.contains("Address") || 
            (message != null && (message.contains("port") || message.contains("address")))) {
            log.error("🔍 端口占用问题:");
            log.error("  1. 检查8089端口是否被占用");
            log.error("  2. 检查9326端口是否被占用");
            log.error("  3. 运行: netstat -an | findstr \"8089\\|9326\"");
        }
        
        if (exceptionName.contains("Bean") || exceptionName.contains("Autowired")) {
            log.error("🔍 Bean注入问题:");
            log.error("  1. 检查组件扫描路径是否正确");
            log.error("  2. 检查依赖注入配置");
        }
        
        log.error("=== 建议完毕 ===");
    }
} 