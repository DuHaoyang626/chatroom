package com.dot.msg.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dot.msg.chat.model.ChatSubgroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 群内小组 Mapper 接口
 *
 * @author: 吴安然
 * @date: 2024-12-XX
 */
@Mapper
public interface ChatSubgroupDao extends BaseMapper<ChatSubgroup> {

    /**
     * 根据父群组ID获取所有小组
     */
    @Select("SELECT * FROM chat_subgroup WHERE parent_group_id = #{parentGroupId} AND is_active = 1")
    List<ChatSubgroup> getSubgroupsByParentGroupId(@Param("parentGroupId") Integer parentGroupId);

    /**
     * 获取用户创建的小组
     */
    @Select("SELECT * FROM chat_subgroup WHERE creator_id = #{creatorId} AND is_active = 1")
    List<ChatSubgroup> getSubgroupsByCreatorId(@Param("creatorId") Integer creatorId);

    /**
     * 更新小组成员数量
     */
    @Update("UPDATE chat_subgroup SET member_count = (SELECT COUNT(*) FROM chat_subgroup_member WHERE subgroup_id = #{subgroupId} AND status = 1) WHERE id = #{subgroupId}")
    void updateMemberCount(@Param("subgroupId") Integer subgroupId);
} 