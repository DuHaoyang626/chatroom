package com.dot.msg.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dot.msg.chat.dto.ChatUserSimDto;
import com.dot.msg.chat.model.ChatUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 聊天室用户表(关联管理员表和企业用户表) Mapper 接口
 * </p>
 * 
 * @author Mybatis-Plus-Generator
 * @since 2023-06-07
 */
@Mapper
public interface ChatUserDao extends BaseMapper<ChatUser> {

    /**
     * 查询聊天室用户列表(左链接好友)
     *
     * @return 聊天室用户列表
     */
    @Select("select cu.id, if(length(cf.remark)>0,cf.remark,cu.nickname) as nickname, cu.nickname as olnickname, cu.avatar, cu.is_online, if(cf.id is null,true,false) isFriend " +
            "from chat_user cu " +
            "left join chat_friend cf on cf.friend_id=cu.id and cf.user_id=#{currentUserId} " +
            "where cu.id in(${chatUserIds})")
    List<ChatUserSimDto> selectChatUserLeftFriendSimList(@Param("chatUserIds") String chatUserIds, @Param("currentUserId") Integer currentUserId);

    @Select("select cu.id, cu.nickname as nickname, cu.nickname as olnickname, cu.avatar, cu.is_online, if(cf.id is null,true,false) isFriend " +
            "from chat_user cu " +
            "left join chat_friend cf on cu.id = cf.friend_id and cf.user_id = #{userId} " +
            "where cu.phone like #{phone}")
    List<ChatUser> getFriendListByPhone(@Param("userId") Integer userId, @Param("phone") String phone);

}
