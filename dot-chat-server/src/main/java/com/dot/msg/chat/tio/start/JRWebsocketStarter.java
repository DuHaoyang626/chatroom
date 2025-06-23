package com.dot.msg.chat.tio.start;

import com.dot.msg.chat.tio.config.JRTioConfig;
import com.dot.msg.chat.tio.handler.JRWsMsgHandler;
import com.dot.msg.chat.tio.listener.JRIpStatListener;
import com.dot.msg.chat.tio.listener.JRWsTioServerListener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tio.server.TioServerConfig;
import org.tio.websocket.server.WsServerStarter;

/**
 * 启动websocket服务
 */
@Slf4j
@Component
public class JRWebsocketStarter {

    @Resource
    private JRWsMsgHandler wsMsgHandler;

    @Resource
    private JRIpStatListener ipStatListener;

    @Resource
    private JRWsTioServerListener wsTioServerListener;

    @Resource
    private JRTioConfig jrTioConfig;


    /**
     * 启动websocket服务
     */
    @PostConstruct
    public void start() throws Exception {
        log.info("🚀 === WebSocket服务启动开始 ===");
        
        try {
            // 配置检查
            log.debug("📋 检查TIO配置:");
            log.debug("   端口: {}", jrTioConfig.getPort());
            log.debug("   协议名称: {}", jrTioConfig.getProtocolName());
            log.debug("   心跳超时: {}ms", jrTioConfig.getHeartbeatTimeout());
            log.debug("   字符集: {}", jrTioConfig.getCharset());
            log.debug("   服务器IP: {}", jrTioConfig.getServerIp());
            
            // SSL配置检查
            if (jrTioConfig.getSsl() != null) {
                log.debug("   SSL配置: 启用={}, KeyStore={}", 
                    jrTioConfig.getSsl().isEnable(), 
                    jrTioConfig.getSsl().getKeyStore());
            } else {
                log.debug("   SSL配置: 未配置 (null)");
            }
            
            // 检查依赖组件
            log.debug("🔧 检查依赖组件:");
            log.debug("   WsMsgHandler: {}", wsMsgHandler != null ? "✅ 已注入" : "❌ 未注入");
            log.debug("   IpStatListener: {}", ipStatListener != null ? "✅ 已注入" : "❌ 未注入");
            log.debug("   WsTioServerListener: {}", wsTioServerListener != null ? "✅ 已注入" : "❌ 未注入");
            
            // 创建WebSocket服务器
            log.info("🔧 创建WebSocket服务器，端口: {}", jrTioConfig.getPort());
            WsServerStarter wsServerStarter = new WsServerStarter(jrTioConfig.getPort(), wsMsgHandler);
            
            // 获取服务器配置
            log.debug("⚙️ 配置TIO服务器参数");
            TioServerConfig serverTioConfig = wsServerStarter.getTioServerConfig();
            serverTioConfig.setName(jrTioConfig.getProtocolName());
            log.debug("   设置协议名称: {}", jrTioConfig.getProtocolName());
            
            serverTioConfig.setTioServerListener(wsTioServerListener);
            log.debug("   设置服务器监听器: ✅");

            // 设置ip监控
            serverTioConfig.setIpStatListener(ipStatListener);
            log.debug("   设置IP统计监听器: ✅");
            
            // 设置ip统计时间段
            serverTioConfig.ipStats.addDurations(jrTioConfig.IPSTAT_DURATIONS);
            log.debug("   设置IP统计时间段: {} 个时间段", jrTioConfig.IPSTAT_DURATIONS.length);

            // 设置心跳超时时间
            serverTioConfig.setHeartbeatTimeout(jrTioConfig.getHeartbeatTimeout());
            log.debug("   设置心跳超时时间: {}ms", jrTioConfig.getHeartbeatTimeout());

            // SSL配置处理
            if (jrTioConfig.getSsl() != null && jrTioConfig.getSsl().isEnable()) {
                log.info("🔒 启用SSL配置");
                log.debug("   KeyStore: {}", jrTioConfig.getSsl().getKeyStore());
                log.debug("   TrustStore: {}", jrTioConfig.getSsl().getTrustStore());
                serverTioConfig.useSsl(jrTioConfig.getSsl().getKeyStore(), 
                                     jrTioConfig.getSsl().getTrustStore(), 
                                     jrTioConfig.getSsl().getPwd());
                log.info("✅ SSL配置完成");
            } else {
                log.debug("🔓 未启用SSL，使用普通WebSocket连接");
            }
            
            // 保存配置
            jrTioConfig.setTioConfig(serverTioConfig);
            log.debug("💾 TIO配置已保存到JRTioConfig");
            
            // 启动服务器
            log.info("🚀 启动WebSocket服务器...");
            wsServerStarter.start();
            log.info("✅ === WebSocket服务启动成功 ===");
            log.info("🌐 WebSocket服务器运行在: ws://{}:{}/websocket/", 
                jrTioConfig.getServerIp() != null ? jrTioConfig.getServerIp() : "0.0.0.0", 
                jrTioConfig.getPort());
            
        } catch (Exception e) {
            log.error("❌ === WebSocket服务启动失败 ===");
            log.error("异常类型: {}", e.getClass().getSimpleName());
            log.error("异常信息: {}", e.getMessage());
            
            // 详细异常信息
            if (e.getCause() != null) {
                log.error("根本原因: {} - {}", e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
            }
            
            // 特殊异常处理
            if (e.getMessage() != null) {
                if (e.getMessage().contains("Address already in use")) {
                    log.error("🚫 端口{}已被占用，请检查是否有其他程序正在使用该端口", jrTioConfig.getPort());
                } else if (e.getMessage().contains("NullPointerException")) {
                    log.error("🚫 配置错误，请检查application.yml中的tio.server配置");
                }
            }
            
            log.error("🔧 建议排查步骤:");
            log.error("   1. 检查端口{}是否被占用: netstat -an | findstr \"{}\"", jrTioConfig.getPort(), jrTioConfig.getPort());
            log.error("   2. 检查application.yml中tio.server配置是否完整");
            log.error("   3. 检查SSL证书文件是否存在(如果启用SSL)");
            
            throw e; // 重新抛出异常，让Spring Boot处理
        }
    }

}
