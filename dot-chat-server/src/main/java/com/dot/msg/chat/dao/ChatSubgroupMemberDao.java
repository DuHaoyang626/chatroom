package com.dot.msg.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dot.msg.chat.model.ChatSubgroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 小组成员 Mapper 接口
 *
 * @author: 吴安然
 * @date: 2024-12-XX
 */
@Mapper
public interface ChatSubgroupMemberDao extends BaseMapper<ChatSubgroupMember> {

    /**
     * 获取小组所有成员
     */
    @Select("SELECT sm.* " +
            "FROM chat_subgroup_member sm " +
            "WHERE sm.subgroup_id = #{subgroupId} AND sm.status = 1")
    List<ChatSubgroupMember> getSubgroupMembers(@Param("subgroupId") Integer subgroupId);

    /**
     * 获取用户在指定父群组中的当前小组
     */
    @Select("SELECT sm.* FROM chat_subgroup_member sm " +
            "INNER JOIN chat_subgroup sg ON sm.subgroup_id = sg.id " +
            "WHERE sm.user_id = #{userId} AND sm.parent_group_id = #{parentGroupId} " +
            "AND sm.status = 1 AND sg.is_active = 1")
    ChatSubgroupMember getUserCurrentSubgroup(@Param("userId") Integer userId, @Param("parentGroupId") Integer parentGroupId);

    /**
     * 检查用户是否已在指定父群组的某个小组中
     */
    @Select("SELECT COUNT(*) FROM chat_subgroup_member sm " +
            "INNER JOIN chat_subgroup sg ON sm.subgroup_id = sg.id " +
            "WHERE sm.user_id = #{userId} AND sm.parent_group_id = #{parentGroupId} " +
            "AND sm.status = 1 AND sg.is_active = 1")
    Integer getUserActiveSubgroupCount(@Param("userId") Integer userId, @Param("parentGroupId") Integer parentGroupId);

    /**
     * 获取小组成员ID列表
     */
    @Select("SELECT user_id FROM chat_subgroup_member WHERE subgroup_id = #{subgroupId} AND status = 1")
    List<Integer> getSubgroupMemberIds(@Param("subgroupId") Integer subgroupId);

    /**
     * 检查用户是否是小组成员
     */
    @Select("SELECT COUNT(*) FROM chat_subgroup_member WHERE subgroup_id = #{subgroupId} AND user_id = #{userId} AND status = 1")
    Integer isSubgroupMember(@Param("subgroupId") Integer subgroupId, @Param("userId") Integer userId);
} 