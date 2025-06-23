package com.dot.msg.chat.tio.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.dot.comm.constants.CommConstant;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.exception.ApiException;
import com.dot.comm.utils.RedisUtil;
import com.dot.msg.chat.em.ChatTypeEm;
import com.dot.msg.chat.model.ChatGroupMember;
import com.dot.msg.chat.model.ChatMsg;
import com.dot.msg.chat.model.ChatRoom;
import com.dot.msg.chat.model.ChatRoomUserRel;
import com.dot.msg.chat.request.ChatMsgAddRequest;
import com.dot.msg.chat.request.ChatRoomAddRequest;
import com.dot.msg.chat.service.ChatGroupMemberService;
import com.dot.msg.chat.service.ChatMsgService;
import com.dot.msg.chat.service.ChatRoomService;
import com.dot.msg.chat.tio.em.CallTypeEm;
import com.dot.msg.chat.tio.em.MsgTypeEm;
import com.dot.msg.chat.tio.entiy.MessageCall;
import com.dot.msg.chat.tio.entiy.TioMessage;
import com.dot.msg.chat.tio.util.TioUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.TioConfig;
import org.tio.websocket.common.WsResponse;
import com.dot.msg.chat.tio.entiy.CallStatus;
import com.dot.msg.chat.tio.em.SocketTypePacketEm;
import com.dot.msg.chat.tio.entiy.TioPacket;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 聊天信息发送服务
 *
 * @author: Dao-yang.
 * @date: Created in 2024/2/6 14:28
 */
@Slf4j
@Service
public class ChatMsgSendService {

    @Resource
    private ChatGroupMemberService chatGroupMemberService;

    @Resource
    private ChatRoomService chatRoomService;

    @Resource
    private ChatMsgService chatMsgService;

    @Resource
    private RedisUtil redisUtil;

    public void sendAndSaveMsg(ChannelContext channelContext, TioMessage message) {
        // 当聊天室ID为空时添加聊天室
        addChatRoomAndSetChatId(message);
        Integer msgId = saveOrUpdateRetMsgId(channelContext, message);
        if (msgId != null) {
            message.setId(msgId);
            log.info("开始发送聊天信息,chatId:{},msgId:{}", message.getChatId(), message.getId());
            // 发送信息
            sendMsg(channelContext, message);
        } else {
            log.error("添加聊天记录失败,message:{}", message);
        }
    }

    private void addChatRoomAndSetChatId(TioMessage message) {
        String chatId = getChatId(message);
        ChatRoom chatRoom = chatRoomService.getByChatId(chatId);
        if (ObjectUtil.isNull(chatRoom)) {
            addChatRoom(message);
        } else {
            // 获取没有聊天室的用户id(可能删除掉了)
            List<Integer> noChatRoomUserIds = getNoChatRoomUserIds(message, chatId);
            if (CollUtil.isNotEmpty(noChatRoomUserIds)) {
                chatRoomService.addChatRoomUserRel(chatId, noChatRoomUserIds);
            }
        }
    }

    private String getChatId(TioMessage message) {
        String chatId = TioUtil.generateChatId(message.getSendUserId(), message.getToUserId());
        if (ChatTypeEm.GROUP == message.getChatType()) {
            chatId = TioUtil.generateChatId(message.getToUserId());
        }
        message.setChatId(chatId);
        return chatId;
    }

    private void addChatRoom(TioMessage message) {
        ChatRoomAddRequest chatRoomAddRequest = new ChatRoomAddRequest();
        chatRoomAddRequest.setChatType(message.getChatType().name());
        chatRoomAddRequest.setSendUserId(message.getSendUserId());
        chatRoomAddRequest.setToUserId(message.getToUserId());
        String chatId = chatRoomService.addChatRoom(chatRoomAddRequest);
        if (StringUtils.isBlank(chatId)) {
            log.error("添加聊天室失败,message:{}", message);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "消息发送失败");
        }
    }

    private List<Integer> getNoChatRoomUserIds(TioMessage message, String chatId) {
        List<Integer> noChatRoomUserIds = new ArrayList<>();
        if (ChatTypeEm.SINGLE == message.getChatType()) {
            List<ChatRoomUserRel> chatRoomUserRelList = chatRoomService.getChatRoomUserRelListByChatId(chatId);
            Optional<ChatRoomUserRel> first = chatRoomUserRelList.stream()
                    .filter(rel -> rel.getUserId().equals(message.getSendUserId())).findFirst();
            if (first.isEmpty()) {
                noChatRoomUserIds.add(message.getSendUserId());
            }
            Optional<ChatRoomUserRel> first2 = chatRoomUserRelList.stream()
                    .filter(rel -> rel.getUserId().equals(message.getToUserId())).findFirst();
            if (first2.isEmpty()) {
                noChatRoomUserIds.add(message.getToUserId());
            }
        } else {
            List<ChatGroupMember> groupMemberList = chatGroupMemberService
                    .getChatGroupMemberListByGroupId(message.getToUserId());
            List<ChatRoomUserRel> chatRoomUserRelList = chatRoomService.getChatRoomUserRelListByChatId(chatId);
            groupMemberList.forEach(groupMember -> {
                Optional<ChatRoomUserRel> first = chatRoomUserRelList.stream()
                        .filter(rel -> rel.getUserId().equals(groupMember.getUserId())).findFirst();
                if (first.isEmpty()) {
                    noChatRoomUserIds.add(groupMember.getUserId());
                }
            });
        }
        return noChatRoomUserIds;
    }

    private Integer saveOrUpdateRetMsgId(ChannelContext channelContext, TioMessage message) {
        Integer msgId = null;
        if (message.getMsgType() == MsgTypeEm.VIDEO_CALL || message.getMsgType() == MsgTypeEm.AUDIO_CALL
                || message.getMsgType() == MsgTypeEm.GROUP_AUDIO_CALL
                || message.getMsgType() == MsgTypeEm.GROUP_VIDEO_CALL) {
            MessageCall messageCall = JSON.parseObject(message.getMsg(), MessageCall.class);
            if (messageCall.getMsgId() != null) { // 消息ID不为空时,更新聊天记录内容
                msgId = messageCall.getMsgId();
                setDurationAndUpdateChatMsg(channelContext, message, messageCall);
            } else if (messageCall.getCallType() == CallTypeEm.invite) { // 接到通话邀请时判断是否忙线中
                msgId = getNewMsgId(channelContext, message, messageCall);
            }
        }
        if (msgId == null) {
            // 保存聊天记录
            msgId = saveChatMsg(message);
        }
        return msgId;
    }

    private Integer getNewMsgId(ChannelContext channelContext, TioMessage message, MessageCall messageCall) {
        Integer msgId;
        TioPacket packet = new TioPacket(SocketTypePacketEm.CHAT.getType(),
                message.toString().getBytes(StandardCharsets.UTF_8));
        if (ChatTypeEm.GROUP == message.getChatType()) {
            List<Integer> memberIds = chatGroupMemberService.getChatGroupMemberIdListByGroupId(message.getToUserId());
            // 移除发送者
            memberIds = memberIds.stream().filter(id -> !id.equals(message.getSendUserId()))
                    .collect(Collectors.toList());
            if (isBusy(memberIds)) {
                Tio.sendToUser(channelContext.getTioConfig(), String.valueOf(message.getSendUserId()),
                        WsResponse.fromText("对方有人正在通话中,请稍后再试", StandardCharsets.UTF_8.name()));
                return null;
            }
            // 保存聊天记录
            msgId = saveChatMsg(message);
            messageCall.setMsgId(msgId);
            message.setMsg(JSON.toJSONString(messageCall));
            // 设置所有人为拨号中
            for (Integer memberId : memberIds) {
                redisUtil.set(CommConstant.CHAT_MSG_CALLING_KEY + memberId, String.valueOf(msgId), 60L);
            }
        } else {
            if (isBusy(message.getToUserId())) {
                Tio.sendToUser(channelContext.getTioConfig(), String.valueOf(message.getSendUserId()),
                        WsResponse.fromText("对方正在通话中,请稍后再试", StandardCharsets.UTF_8.name()));
                return null;
            }
            // 保存聊天记录
            msgId = saveChatMsg(message);
            messageCall.setMsgId(msgId);
            message.setMsg(JSON.toJSONString(messageCall));
            redisUtil.set(CommConstant.CHAT_MSG_CALLING_KEY + message.getToUserId(), String.valueOf(msgId), 60L);
        }
        redisUtil.set(CommConstant.CHAT_MSG_CALLING_KEY + message.getSendUserId(), String.valueOf(msgId), 60L);
        return msgId;
    }

    private void updateLastChatMsg(TioMessage message, ChatMsg lastChatMsg, int lastMsgId) {
        if (lastChatMsg.getSendUserId().equals(message.getSendUserId())) {
            // 更新最后一条聊天记录
            lastChatMsg.setMsg(message.getMsg());
            chatMsgService.updateById(lastChatMsg);
            WsResponse wsResponse = WsResponse.fromText("更新通话信令成功", StandardCharsets.UTF_8.name());
            Tio.sendToUser(null, String.valueOf(message.getSendUserId()), wsResponse);
        } else {
            WsResponse wsResponse = WsResponse.fromText("对方正在通话中,请稍后再试", StandardCharsets.UTF_8.name());
            Tio.sendToUser(null, String.valueOf(message.getSendUserId()), wsResponse);
        }
    }

    private void removeRedisCache(TioMessage message) {
        redisUtil.remove(CommConstant.CHAT_MSG_CALLING_KEY + message.getToUserId());
        redisUtil.remove(CommConstant.CHAT_MSG_CALLING_KEY + message.getSendUserId());
    }

    private void cacheMsgIdToRedis(TioMessage message, Integer msgId) {
        if (msgId == null) {
            return;
        }
        redisUtil.set(CommConstant.CHAT_MSG_CALLING_KEY + message.getToUserId(), msgId.toString());
        redisUtil.set(CommConstant.CHAT_MSG_CALLING_KEY + message.getSendUserId(), msgId.toString());
    }

    private void setDurationAndUpdateChatMsg(ChannelContext channelContext, TioMessage message,
            MessageCall messageCall) {
        Integer msgId = messageCall.getMsgId();
        ChatMsg chatMsg = chatMsgService.getById(msgId);
        MessageCall oldCall = JSON.parseObject(chatMsg.getMsg(), MessageCall.class);
        oldCall.setCallType(messageCall.getCallType());
        oldCall.setMsgId(msgId);

        // 群聊分支
        if (ChatTypeEm.GROUP.name().equals(chatMsg.getChatType())) {
            List<Integer> memberIds = chatGroupMemberService.getChatGroupMemberIdListByGroupId(chatMsg.getGroupId());
            Map<String, CallStatus> memberStatusMap = oldCall.getMemberStatusMap();
            if (memberStatusMap == null) {
                memberStatusMap = new java.util.HashMap<>();
                for (Integer memberId : memberIds) {
                    memberStatusMap.put(String.valueOf(memberId), new CallStatus());
                }
            }
            CallStatus status = memberStatusMap.get(message.getSendUserId());
            if (status == null) {
                status = new CallStatus();
            }
            if (messageCall.getCallType() == CallTypeEm.accept) {
                status.setStatus("已接听");
                status.setAcceptTime(DateUtil.now());
            } else if (messageCall.getCallType() == CallTypeEm.hangup
                    || messageCall.getCallType() == CallTypeEm.dropped) {
                status.setStatus("已挂断");
                status.setHangupTime(DateUtil.now());
                if (org.apache.commons.lang3.StringUtils.isNotBlank(status.getAcceptTime())) {
                    status.setDuration((int) ((DateUtil.date().getTime()
                            - DateUtil.parseDateTime(status.getAcceptTime()).getTime()) / 1000));
                }
            } else if (messageCall.getCallType() == CallTypeEm.cancel) {
                status.setStatus("已取消");
            } else if (messageCall.getCallType() == CallTypeEm.reject) {
                status.setStatus("已拒绝");
            }

            memberStatusMap.put(String.valueOf(message.getSendUserId()), status);
            oldCall.setMemberStatusMap(memberStatusMap);

            boolean allHandled = memberStatusMap.values().stream()
                    .allMatch(s -> s.getStatus() != null && (s.getStatus().equals("已挂断") || s.getStatus().equals("已取消")
                            || s.getStatus().equals("已拒绝")));

            if (allHandled) {
                for (Integer memberId : memberIds) {
                    redisUtil.remove(CommConstant.CHAT_MSG_CALLING_KEY + memberId);
                }
            }

            updateChatMsg(msgId, JSON.toJSONString(oldCall));
            return;
        }

        // 私聊原有逻辑
        if (oldCall.getCallType() == CallTypeEm.accept) {
            oldCall.setAcceptTime(DateUtil.now());
            if (TioUtil.isOffline(channelContext.tioConfig, message.getToUserId().toString())) {
                oldCall.setDuration(0);
                oldCall.setHangupTime(DateUtil.now());
                oldCall.setCallType(CallTypeEm.dropped);
                messageCall.setCallType(CallTypeEm.dropped);
                message.setMsg(JSON.toJSONString(messageCall));
            }
        } else if (oldCall.getCallType() == CallTypeEm.hangup || oldCall.getCallType() == CallTypeEm.dropped) {
            DateTime now = DateUtil.date();
            oldCall.setHangupTime(now.toString());
            if (org.apache.commons.lang3.StringUtils.isNotBlank(oldCall.getAcceptTime())) {
                oldCall.setDuration(
                        (int) ((now.getTime() - DateUtil.parseDateTime(oldCall.getAcceptTime()).getTime()) / 1000));
            } else {
                log.warn("挂断时通话接通时间为空,oldCall:{}", oldCall);
            }
            oldCall.setDroppedUserId(messageCall.getDroppedUserId());
            messageCall.setDuration(oldCall.getDuration());
            message.setMsg(JSON.toJSONString(messageCall));
            message.setMsgType(MsgTypeEm.getMsgType(chatMsg.getMsgType()));
        }
        if (oldCall.getCallType() == CallTypeEm.hangup
                || oldCall.getCallType() == CallTypeEm.reject
                || oldCall.getCallType() == CallTypeEm.cancel
                || oldCall.getCallType() == CallTypeEm.no_answer
                || oldCall.getCallType() == CallTypeEm.dropped) {
            redisUtil.remove(CommConstant.CHAT_MSG_CALLING_KEY + chatMsg.getSendUserId());
            redisUtil.remove(CommConstant.CHAT_MSG_CALLING_KEY + chatMsg.getToUserId());
            if (!message.getSendUserId().equals(chatMsg.getSendUserId())) {
                message.setSendUserId(chatMsg.getSendUserId());
                message.setToUserId(chatMsg.getSendUserId());
            }
        }
        message.setId(msgId);
        updateChatMsg(msgId, JSON.toJSONString(oldCall));
    }

    private void updateChatMsg(Integer msgId, String msgContent) {
        boolean ret = chatMsgService.updateMsgContent(msgId, msgContent);
        if (!ret) {
            log.error("更新聊天记录内容失败,msgId:{},msgContent:{}", msgId, msgContent);
        }
    }

    private Integer saveChatMsg(TioMessage message) {
        ChatMsgAddRequest msgAddRequest = getChatMsgAddRequest(message);
        // 保存聊天消息记录
        return chatMsgService.saveMsg(msgAddRequest);
    }

    private void sendMsg(ChannelContext channelContext, TioMessage message) {
        if (ChatTypeEm.SINGLE == message.getChatType()) {
            boolean isOnline = TioUtil.isOnline(channelContext.tioConfig, message.getToUserId().toString());
            // 好友处于在线状态时发送消息给好友,自己给自己发的时候只发一次
            if (isOnline && !Objects.equals(channelContext.userid, message.getToUserId().toString())) {
                // 发给好友
                sendToUser(channelContext.tioConfig, message);
            }
            // 发给自己
            message.setToUserId(Integer.parseInt(channelContext.userid));
            sendToUser(channelContext.tioConfig, message);
        } else if (ChatTypeEm.GROUP == message.getChatType()) {
            sendToGroup(channelContext.tioConfig, message);
        }
    }

    private ChatMsgAddRequest getChatMsgAddRequest(TioMessage message) {
        ChatMsgAddRequest msgAddRequest = new ChatMsgAddRequest();
        msgAddRequest.setChatId(message.getChatId());
        msgAddRequest.setChatType(message.getChatType().name());
        msgAddRequest.setMsgType(message.getMsgType().name());
        msgAddRequest.setMsg(message.getMsg());
        msgAddRequest.setSendUserId(message.getSendUserId());
        msgAddRequest.setToUserId(message.getToUserId());
        msgAddRequest.setDeviceType(message.getDeviceType());
        if (ChatTypeEm.GROUP == message.getChatType()) {
            msgAddRequest.setGroupId(message.getToUserId());
        }
        return msgAddRequest;
    }

    public void sendToUser(TioConfig tioConfig, TioMessage message) {
        WsResponse meResponse = WsResponse.fromText(message.toString(), StandardCharsets.UTF_8.name());
        Tio.sendToUser(tioConfig, message.getToUserId().toString(), meResponse);
    }

    public void sendToGroup(TioConfig tioConfig, TioMessage message) {
        WsResponse meResponse = WsResponse.fromText(message.toString(), StandardCharsets.UTF_8.name());
        Tio.sendToGroup(tioConfig, message.getToUserId().toString(), meResponse);
    }

    private boolean isBusy(Integer userId) {
        return redisUtil.exists(CommConstant.CHAT_MSG_CALLING_KEY + userId);
    }

    private boolean isBusy(List<Integer> userIds) {
        for (Integer userId : userIds) {
            if (redisUtil.exists(CommConstant.CHAT_MSG_CALLING_KEY + userId)) {
                return true;
            }
        }
        return false;
    }
}
