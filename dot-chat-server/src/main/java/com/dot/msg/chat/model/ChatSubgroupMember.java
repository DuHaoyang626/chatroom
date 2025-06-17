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
 * 小组成员表实体
 *
 * @author: 吴安然
 * @date: 2024-12-XX
 */
@Data
@TableName("chat_subgroup_member")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatSubgroupMember", description = "小组成员表")
public class ChatSubgroupMember implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "成员记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 小组ID
     */
    @Schema(description = "小组ID")
    private Integer subgroupId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Integer userId;

    /**
     * 父群组ID
     */
    @Schema(description = "父群组ID")
    private Integer parentGroupId;

    /**
     * 加入时间
     */
    @Schema(description = "加入时间")
    private String joinTime;

    /**
     * 状态(1:正常,0:已退出)
     */
    @Schema(description = "状态(1:正常,0:已退出)")
    private Integer status;
} 