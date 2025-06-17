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
 * 群内小组表实体
 *
 * @author: 吴安然
 * @date: 2024-12-XX
 */
@Data
@TableName("chat_subgroup")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatSubgroup", description = "群内小组表")
public class ChatSubgroup implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "小组ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 父群组ID
     */
    @Schema(description = "父群组ID")
    private Integer parentGroupId;

    /**
     * 小组名称
     */
    @Schema(description = "小组名称")
    private String name;

    /**
     * 创建者ID
     */
    @Schema(description = "创建者ID")
    private Integer creatorId;

    /**
     * 小组成员数
     */
    @Schema(description = "小组成员数")
    private Integer memberCount;

    /**
     * 是否活跃
     */
    @Schema(description = "是否活跃")
    private Boolean isActive;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private String updateTime;
} 