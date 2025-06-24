package com.dot.deepseek.service;

import com.dot.deepseek.request.DSChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * DeepSeek AI 服务接口
 *
 * @author: BUPT Chatroom Teams(Du/Fu/Lu/Wu/Kang).
 * @date: Created in 2025/6/20 16:53
 */
public interface DSChatService {

    String generateChatMessage(DSChatRequest request);

    SseEmitter generateChatMessageForStream(DSChatRequest request);
    /**
     * 中断sse连接
     */
    boolean closeSse();
}
