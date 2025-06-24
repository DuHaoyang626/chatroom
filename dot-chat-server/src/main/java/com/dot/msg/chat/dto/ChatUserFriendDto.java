package com.dot.msg.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室用户好友对象(精简)
 * 
 * @author BUPT Chatroom Teams(Du/Fu/Lu/Wu/Kang)
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatUserFriendDto", description="聊天室用户好友对象(精简)")
public class ChatUserFriendDto implements Serializable {

	@Serial
	private static final long serialVersionUID =  9191778674935372365L;

	@Schema(description = "好友ID")
	private Integer friendId;

	/**
	 * 用户昵称
	 */
	@Schema(description = "用户昵称")
	private String nickname;

	/**
	 * 用户头像
	 */
	@Schema(description = "用户头像")
	private String avatar;

	/**
	 * 用户性别
	 */
	@Schema(description = "用户性别")
	private Integer sex;

	/**
	 * 个性签名
	 */
	@Schema(description = "个性签名")
	private String signature;

	/**
	 * 好友备注
	 */
	@Schema(description = "好友备注")
	private String remark;

	/**
	 * 是否置顶
	 */
	@Schema(description = "是否置顶")
	private Boolean isTop;

	/**
	 * 首字母
	 */
	@Schema(description = "首字母")
	private String initial;

	/**
	 * 是否在线
	 */
	@Schema(description = "是否在线")
	private Boolean isOnline;

	/**
	 * 好友来源
	 */
	@Schema(description = "好友来源")
	private String source;
}
