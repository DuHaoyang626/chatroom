package com.dot.sys;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping("/check")
    public Map<String, Object> healthCheck() {
        log.info("🩺 [管理后台] 健康检查请求");
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("service", "dot-chat-admin");
        result.put("timestamp", LocalDateTime.now());
        result.put("message", "管理后台服务运行正常");
        
        log.info("✅ [管理后台] 健康检查通过");
        return result;
    }
} 