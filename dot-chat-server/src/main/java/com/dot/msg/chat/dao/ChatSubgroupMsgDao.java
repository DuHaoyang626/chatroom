package com.dot.msg.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dot.msg.chat.model.ChatSubgroupMsg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 小组消息 Mapper 接口
 *
 * @author: 吴安然
 * @date: 2024-12-XX
 */
@Mapper
public interface ChatSubgroupMsgDao extends BaseMapper<ChatSubgroupMsg> {

    /**
     * 获取小组消息列表（最新的消息）
     */
    @Select("SELECT msg.* " +
            "FROM chat_subgroup_msg msg " +
            "WHERE msg.subgroup_id = #{subgroupId} " +
            "ORDER BY msg.send_time DESC LIMIT #{limit}")
    List<ChatSubgroupMsg> getSubgroupMessages(@Param("subgroupId") Integer subgroupId, @Param("limit") Integer limit);

    /**
     * 获取小组消息历史（分页）
     */
    @Select("SELECT msg.* " +
            "FROM chat_subgroup_msg msg " +
            "WHERE msg.subgroup_id = #{subgroupId} AND msg.send_time < #{beforeTime} " +
            "ORDER BY msg.send_time DESC LIMIT #{limit}")
    List<ChatSubgroupMsg> getSubgroupMessageHistory(@Param("subgroupId") Integer subgroupId, 
                                                     @Param("beforeTime") String beforeTime, 
                                                     @Param("limit") Integer limit);

    /**
     * 获取用户在小组中发送的消息数量
     */
    @Select("SELECT COUNT(*) FROM chat_subgroup_msg WHERE subgroup_id = #{subgroupId} AND send_user_id = #{userId}")
    Integer getUserMessageCount(@Param("subgroupId") Integer subgroupId, @Param("userId") Integer userId);

    /**
     * 获取小组总消息数量
     */
    @Select("SELECT COUNT(*) FROM chat_subgroup_msg WHERE subgroup_id = #{subgroupId}")
    Integer getSubgroupMessageCount(@Param("subgroupId") Integer subgroupId);

    /**
     * 获取指定时间范围内的小组消息
     */
    @Select("SELECT * FROM chat_subgroup_msg WHERE subgroup_id = #{subgroupId} " +
            "AND send_time BETWEEN #{startTime} AND #{endTime} ORDER BY send_time ASC")
    List<ChatSubgroupMsg> getSubgroupMessagesByTimeRange(@Param("subgroupId") Integer subgroupId,
                                                          @Param("startTime") String startTime,
                                                          @Param("endTime") String endTime);

    /**
     * 根据消息类型获取小组消息
     */
    @Select("SELECT * FROM chat_subgroup_msg WHERE subgroup_id = #{subgroupId} AND msg_type = #{msgType} ORDER BY send_time DESC LIMIT #{limit}")
    List<ChatSubgroupMsg> getSubgroupMessagesByType(@Param("subgroupId") Integer subgroupId,
                                                     @Param("msgType") String msgType,
                                                     @Param("limit") Integer limit);

    /**
     * 删除小组所有消息（当小组解散时）
     */
    @Delete("DELETE FROM chat_subgroup_msg WHERE subgroup_id = #{subgroupId}")
    Integer deleteSubgroupMessages(@Param("subgroupId") Integer subgroupId);

    /**
     * 搜索小组消息内容
     */
    @Select("SELECT msg.* " +
            "FROM chat_subgroup_msg msg " +
            "WHERE msg.subgroup_id = #{subgroupId} AND msg.msg LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY msg.send_time DESC LIMIT #{limit}")
    List<ChatSubgroupMsg> searchSubgroupMessages(@Param("subgroupId") Integer subgroupId,
                                                  @Param("keyword") String keyword,
                                                  @Param("limit") Integer limit);
} 