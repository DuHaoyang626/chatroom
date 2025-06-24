package com.dot.msg.chat.listener;

import com.dot.msg.chat.listener.event.ChatMsgSendEvent;
import com.dot.msg.chat.tio.service.ChatMsgSendService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;


/**
 * 群聊事件监听
 *
 * @author: BUPT Chatroom Teams(Du/Fu/Lu/Wu/Kang).
 * @date: Created in 2025/6/20 16:57
 */
@Slf4j
@Component
public class ChatMsgSendListener {

    @Resource
    private ChatMsgSendService chatMsgSendService;

    @TransactionalEventListener(ChatMsgSendEvent.class)
    public void onMsgSend(ChatMsgSendEvent event) {
        log.info("发送 {} 消息", event.getType());
        event.getMessageList().forEach(message -> {
            chatMsgSendService.sendAndSaveMsg(event.getChannelContext(), message);
        });
    }
}
