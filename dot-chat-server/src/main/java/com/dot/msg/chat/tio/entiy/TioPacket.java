package com.dot.msg.chat.tio.entiy;

import lombok.Data;
import org.tio.core.intf.Packet;

import java.io.Serializable;

/**
 * socket消息包
 *
 * @author: fusihan.
 * @date: Created in 2025/6/18 10:40
 */
@Data
public class TioPacket extends Packet implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 消息类型
   */
  private byte type;

  /**
   * 消息内容
   */
  private byte[] body;

  public TioPacket() {
  }

  public TioPacket(byte type, byte[] body) {
    this.type = type;
    this.body = body;
  }
}