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
 * 小组邀请表实体
 *
 * @author: 吴安然
 * @date: 2024-12-XX
 */
@Data
@TableName("chat_subgroup_invite")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatSubgroupInvite", description = "小组邀请表")
public class ChatSubgroupInvite implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "邀请ID")
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
     * 邀请人ID
     */
    @Schema(description = "邀请人ID")
    private Integer inviterId;

    /**
     * 被邀请人ID
     */
    @Schema(description = "被邀请人ID")
    private Integer inviteeId;

    /**
     * 状态(0:待处理,1:已接受,2:已拒绝)
     */
    @Schema(description = "状态(0:待处理,1:已接受,2:已拒绝)")
    private Integer status;

    /**
     * 邀请时间
     */
    @Schema(description = "邀请时间")
    private String inviteTime;

    /**
     * 处理时间
     */
    @Schema(description = "处理时间")
    private String handleTime;
} 