package com.dot.msg.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dot.msg.chat.model.ChatSubgroupInvite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 小组邀请 Mapper 接口
 *
 * @author: 吴安然
 * @date: 2024-12-XX
 */
@Mapper
public interface ChatSubgroupInviteDao extends BaseMapper<ChatSubgroupInvite> {

    /**
     * 获取用户收到的小组邀请
     */
    @Select("SELECT si.*, sg.name as subgroup_name, " +
            "inviter.nickname as inviter_nickname, inviter.avatar as inviter_avatar " +
            "FROM chat_subgroup_invite si " +
            "INNER JOIN chat_subgroup sg ON si.subgroup_id = sg.id " +
            "LEFT JOIN chat_user inviter ON si.inviter_id = inviter.id " +
            "WHERE si.invitee_id = #{userId} AND si.status = 0 AND sg.is_active = 1")
    List<ChatSubgroupInvite> getUserPendingInvites(@Param("userId") Integer userId);

    /**
     * 获取小组的所有邀请记录
     */
    @Select("SELECT * FROM chat_subgroup_invite WHERE subgroup_id = #{subgroupId}")
    List<ChatSubgroupInvite> getSubgroupInvites(@Param("subgroupId") Integer subgroupId);

    /**
     * 检查是否已存在相同的邀请
     */
    @Select("SELECT COUNT(*) FROM chat_subgroup_invite " +
            "WHERE subgroup_id = #{subgroupId} AND invitee_id = #{inviteeId} AND status = 0")
    Integer existsPendingInvite(@Param("subgroupId") Integer subgroupId, @Param("inviteeId") Integer inviteeId);
} 