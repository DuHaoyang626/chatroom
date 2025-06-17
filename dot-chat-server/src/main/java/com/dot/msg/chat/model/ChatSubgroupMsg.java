package com.dot.msg.chat.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 小组消息表实体
 *
 * @author: 吴安然
 * @date: 2024-12-XX
 */
@Data
@TableName("chat_subgroup_msg")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatSubgroupMsg", description = "小组消息表")
public class ChatSubgroupMsg implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "消息ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 小组ID
     */
    @Schema(description = "小组ID")
    private Integer subgroupId;

    /**
     * 父群组ID
     */
    @Schema(description = "父群组ID")
    private Integer parentGroupId;

    /**
     * 发送用户ID
     */
    @Schema(description = "发送用户ID")
    private Integer sendUserId;

    /**
     * 消息类型
     */
    @Schema(description = "消息类型")
    private String msgType;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private String msg;

    /**
     * 发送时间
     */
    @Schema(description = "发送时间")
    private String sendTime;

    /**
     * 时间戳
     */
    @Schema(description = "时间戳")
    private Long timestamp;
} 