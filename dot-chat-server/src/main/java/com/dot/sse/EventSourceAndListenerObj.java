package com.dot.sse;

import com.dot.sse.listener.BaseEventSourceListener;
import okhttp3.sse.EventSource;

/**
 * EventSource和Listener封装对象
 *
 * @author: BUPT Chatroom Teams(Du/Fu/Lu/Wu/Kang).
 * @date: Created in 2025/6/20 17:09
 */
public record EventSourceAndListenerObj(EventSource eventSource, BaseEventSourceListener eventSourceListener) {
}
