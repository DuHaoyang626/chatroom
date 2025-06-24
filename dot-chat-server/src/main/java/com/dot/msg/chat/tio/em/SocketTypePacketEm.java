package com.dot.msg.chat.tio.em;

import lombok.Getter;

/**
 * socket消息类型枚举
 *
 * @author: fusihan.
 * @date: Created in 2025/6/20 10:40
 */
@Getter
public enum SocketTypePacketEm {

  /**
   * 聊天消息
   */
  CHAT((byte) 1, "聊天消息"),

  /**
   * 心跳监测
   */
  HEART_BEAT((byte) 2, "心跳监测");

  private final byte type;
  private final String desc;

  SocketTypePacketEm(byte type, String desc) {
    this.type = type;
    this.desc = desc;
  }
}