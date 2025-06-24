package com.dot.sse.listener;

import okhttp3.sse.EventSourceListener;

/**
 * 基础事件监听器
 *
 * @author: BUPT Chatroom Teams(Du/Fu/Lu/Wu/Kang).
 * @date: Created in 2025/6/20 16:47
 */
public abstract class BaseEventSourceListener extends EventSourceListener {

    private volatile boolean isCancelled = false;

    public void cancel() {
        isCancelled = true;
    }

    public boolean isCancelled() {
        return isCancelled;
    }
    public void connected() {
        isCancelled = false;
    }
}
