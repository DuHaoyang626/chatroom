package com.dot.msg.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dot.msg.chat.model.ChatSubgroupInvite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 小组邀请 Mapper 接口
 *
 * @author: 吴安然
 * @date: 2024-12-XX
 */
@Mapper
public interface ChatSubgroupInviteDao extends BaseMapper<ChatSubgroupInvite> {

    /**
     * 获取用户收到的小组邀请 - 终极简化版本
     */
    @Select("SELECT i.* FROM chat_subgroup_invite i " +
            "INNER JOIN chat_subgroup s ON i.subgroup_id = s.id AND s.is_active = 1 " +
            "INNER JOIN chat_group g ON i.parent_group_id = g.id AND g.is_dissolve = 0 " +
            "WHERE i.invitee_id = #{userId} AND i.status = 0 " +
            "ORDER BY i.invite_time DESC")
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

    /**
     * 更新邀请状态
     */
    @Update("UPDATE chat_subgroup_invite SET status = #{status}, handle_time = #{handleTime} " +
            "WHERE id = #{inviteId} AND invitee_id = #{inviteeId} AND status = 0")
    Integer updateInviteStatus(@Param("inviteId") Integer inviteId, 
                             @Param("inviteeId") Integer inviteeId,
                             @Param("status") Integer status,
                             @Param("handleTime") String handleTime);

    /**
     * 获取所有邀请记录的详细信息
     */
    @Select("SELECT i.*, " +
            "u1.nickname as inviter_nickname, " +
            "u2.nickname as invitee_nickname, " +
            "s.name as subgroup_name, " +
            "g.name as parent_group_name " +
            "FROM chat_subgroup_invite i " +
            "LEFT JOIN chat_user u1 ON i.inviter_id = u1.id " +
            "LEFT JOIN chat_user u2 ON i.invitee_id = u2.id " +
            "LEFT JOIN chat_subgroup s ON i.subgroup_id = s.id " +
            "LEFT JOIN chat_group g ON i.parent_group_id = g.id " +
            "ORDER BY i.invite_time DESC")
    List<Map<String, Object>> getAllInvitesWithDetails();

    /**
     * 取消用户在指定群组下的所有待处理邀请
     */
    @Update("UPDATE chat_subgroup_invite SET status = 3 WHERE invitee_id = #{inviteeId} AND parent_group_id = #{parentGroupId} AND status = 0")
    void cancelOldPendingInvites(@Param("inviteeId") Integer inviteeId, @Param("parentGroupId") Integer parentGroupId);
} 