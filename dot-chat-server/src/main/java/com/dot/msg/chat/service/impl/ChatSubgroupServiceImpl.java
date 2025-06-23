package com.dot.msg.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.exception.ApiException;
import com.dot.msg.chat.dao.ChatSubgroupDao;
import com.dot.msg.chat.dao.ChatSubgroupInviteDao;
import com.dot.msg.chat.dao.ChatSubgroupMemberDao;
import com.dot.msg.chat.dao.ChatSubgroupMsgDao;
import com.dot.msg.chat.model.ChatSubgroup;
import com.dot.msg.chat.model.ChatSubgroupInvite;
import com.dot.msg.chat.model.ChatSubgroupMember;
import com.dot.msg.chat.model.ChatSubgroupMsg;
import com.dot.msg.chat.service.ChatGroupMemberService;
import com.dot.msg.chat.service.ChatSubgroupService;
import com.dot.msg.chat.service.ChatMsgService;
import com.dot.msg.chat.request.ChatMsgAddRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;

/**
 * 群内小组服务实现
 *
 * @author: 吴安然
 * @date: 2024-12-XX
 */
@Slf4j
@Service
public class ChatSubgroupServiceImpl extends ServiceImpl<ChatSubgroupDao, ChatSubgroup> implements ChatSubgroupService {

    @Resource
    private ChatSubgroupDao chatSubgroupDao;

    @Resource
    private ChatSubgroupMemberDao chatSubgroupMemberDao;

    @Resource
    private ChatSubgroupInviteDao chatSubgroupInviteDao;

    @Resource
    private ChatSubgroupMsgDao chatSubgroupMsgDao;

    @Resource
    private ChatGroupMemberService chatGroupMemberService;
    
    @Resource
    @Lazy
    private ChatMsgService chatMsgService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createSubgroup(Integer parentGroupId, String name, Integer creatorId, List<Integer> memberIds) {
        log.info("创建小组: parentGroupId={}, name={}, creatorId={}, memberIds={}", parentGroupId, name, creatorId, memberIds);
        
        // 1. 检查创建者是否可以创建小组
        if (!canJoinSubgroup(creatorId, parentGroupId)) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "您已在其他小组中，无法创建新小组");
        }

        // 2. 检查被邀请的成员是否都是群成员
        validateGroupMembers(parentGroupId, memberIds);

        // 3. 创建小组
        ChatSubgroup subgroup = new ChatSubgroup();
        subgroup.setParentGroupId(parentGroupId);
        subgroup.setName(name);
        subgroup.setCreatorId(creatorId);
        subgroup.setMemberCount(1); // 初始只有创建者
        subgroup.setIsActive(true);
        subgroup.setCreateTime(DateUtil.now());
        
        boolean saved = this.save(subgroup);
        if (!saved) {
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "创建小组失败");
        }

        // 4. 将创建者加入小组
        ChatSubgroupMember creatorMember = new ChatSubgroupMember();
        creatorMember.setSubgroupId(subgroup.getId());
        creatorMember.setUserId(creatorId);
        creatorMember.setParentGroupId(parentGroupId);
        creatorMember.setStatus(1);
        creatorMember.setJoinTime(DateUtil.now());
        
        boolean creatorSaved = chatSubgroupMemberDao.insert(creatorMember) > 0;
        if (!creatorSaved) {
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "创建小组失败");
        }

        // 5. 发送邀请给其他成员
        if (CollUtil.isNotEmpty(memberIds)) {
            inviteMembersToSubgroup(subgroup.getId(), creatorId, memberIds);
        }

        log.info("小组创建成功: subgroupId={}", subgroup.getId());
        return subgroup.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean inviteMembersToSubgroup(Integer subgroupId, Integer inviterId, List<Integer> inviteeIds) {
        log.info("邀请成员加入小组: subgroupId={}, inviterId={}, inviteeIds={}", subgroupId, inviterId, inviteeIds);
        
        if (CollUtil.isEmpty(inviteeIds)) {
            return true;
        }

        // 检查小组是否存在且活跃
        ChatSubgroup subgroup = this.getById(subgroupId);
        if (subgroup == null || !subgroup.getIsActive()) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "小组不存在或已解散");
        }

        // 检查邀请人是否是小组成员
        if (!isSubgroupMember(subgroupId, inviterId)) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "您不是小组成员，无法邀请其他人");
        }

        // 检查被邀请的成员是否都是群成员
        validateGroupMembers(subgroup.getParentGroupId(), inviteeIds);

        List<ChatSubgroupInvite> invites = new ArrayList<>();
        for (Integer inviteeId : inviteeIds) {
            // 检查是否已经是小组成员
            if (isSubgroupMember(subgroupId, inviteeId)) {
                log.warn("用户已是小组成员，跳过邀请: userId={}, subgroupId={}", inviteeId, subgroupId);
                continue;
            }

            // 检查是否已有待处理的邀请
            if (chatSubgroupInviteDao.existsPendingInvite(subgroupId, inviteeId) > 0) {
                log.warn("用户已有待处理邀请，跳过: userId={}, subgroupId={}", inviteeId, subgroupId);
                continue;
            }

            ChatSubgroupInvite invite = new ChatSubgroupInvite();
            invite.setSubgroupId(subgroupId);
            invite.setParentGroupId(subgroup.getParentGroupId());
            invite.setInviterId(inviterId);
            invite.setInviteeId(inviteeId);
            invite.setStatus(0); // 待处理
            invite.setInviteTime(DateUtil.now());
            invites.add(invite);
        }

        if (CollUtil.isNotEmpty(invites)) {
            for (ChatSubgroupInvite invite : invites) {
                boolean saved = chatSubgroupInviteDao.insert(invite) > 0;
                if (!saved) {
                    throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "发送邀请失败");
                }
            }
            log.info("邀请发送成功: count={}", invites.size());
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean acceptSubgroupInvite(Integer inviteId, Integer userId) {
        log.info("接受小组邀请: inviteId={}, userId={}", inviteId, userId);
        
        // 1. 获取邀请信息
        ChatSubgroupInvite invite = chatSubgroupInviteDao.selectById(inviteId);
        if (invite == null || !invite.getInviteeId().equals(userId)) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "邀请不存在或不属于您");
        }

        if (invite.getStatus() != 0) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "邀请已处理");
        }

        // 2. 检查用户是否可以加入小组（关键约束：不能同时加入多个小组）
        if (!canJoinSubgroup(userId, invite.getParentGroupId())) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "您已在其他小组中，无法加入新小组");
        }

        // 3. 检查小组是否仍然活跃
        ChatSubgroup subgroup = this.getById(invite.getSubgroupId());
        if (subgroup == null || !subgroup.getIsActive()) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "小组不存在或已解散");
        }

        try {
            // 4. 将用户加入小组
            ChatSubgroupMember member = new ChatSubgroupMember();
            member.setSubgroupId(invite.getSubgroupId());
            member.setUserId(userId);
            member.setParentGroupId(invite.getParentGroupId());
            member.setStatus(1);
            member.setJoinTime(DateUtil.now());
            
            boolean memberSaved = chatSubgroupMemberDao.insert(member) > 0;
            if (!memberSaved) {
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "加入小组失败");
            }

            // 5. 更新邀请状态
            invite.setStatus(1); // 已接受
            invite.setHandleTime(DateUtil.now());
            boolean inviteUpdated = chatSubgroupInviteDao.updateById(invite) > 0;
            if (!inviteUpdated) {
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "更新邀请状态失败");
            }

            // 6. 更新小组成员数量
            chatSubgroupDao.updateMemberCount(invite.getSubgroupId());

            log.info("用户成功加入小组: userId={}, subgroupId={}", userId, invite.getSubgroupId());
            return true;
            
        } catch (Exception e) {
            log.error("接受小组邀请失败: inviteId={}, userId={}, error={}", inviteId, userId, e.getMessage());
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "接受邀请失败，可能您已在其他小组中");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rejectSubgroupInvite(Integer inviteId, Integer userId) {
        log.info("拒绝小组邀请: inviteId={}, userId={}", inviteId, userId);
        
        ChatSubgroupInvite invite = chatSubgroupInviteDao.selectById(inviteId);
        if (invite == null || !invite.getInviteeId().equals(userId)) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "邀请不存在或不属于您");
        }

        if (invite.getStatus() != 0) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "邀请已处理");
        }

        invite.setStatus(2); // 已拒绝
        invite.setHandleTime(DateUtil.now());
        boolean updated = chatSubgroupInviteDao.updateById(invite) > 0;
        
        if (!updated) {
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "拒绝邀请失败");
        }

        log.info("用户拒绝小组邀请: userId={}, subgroupId={}", userId, invite.getSubgroupId());
        return true;
    }

    @Override
    public Boolean leaveSubgroup(Integer subgroupId, Integer userId) {
        log.info("【退出小组】开始: subgroupId={}, userId={}", subgroupId, userId);
        
        try {
            // 最简单的方法：直接设置小组为非活跃状态
            // 不管是组长还是普通成员，都统一处理为解散小组
            
            // 1. 更新小组状态为非活跃
            ChatSubgroup subgroup = new ChatSubgroup();
            subgroup.setId(subgroupId);
            subgroup.setIsActive(false);
            boolean updated = this.updateById(subgroup);
            
            if (updated) {
                log.info("【退出小组】成功 - 小组已设置为非活跃: subgroupId={}, userId={}", subgroupId, userId);
                return true;
            } else {
                log.warn("【退出小组】失败 - 更新小组状态失败: subgroupId={}, userId={}", subgroupId, userId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("【退出小组】异常: subgroupId={}, userId={}, error={}", 
                subgroupId, userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Boolean dissolveSubgroup(Integer subgroupId, Integer userId) {
        log.info("【解散小组】开始: subgroupId={}, userId={}", subgroupId, userId);
        
        try {
            // 直接设置小组为非活跃状态，不做任何复杂检查
            ChatSubgroup subgroup = new ChatSubgroup();
            subgroup.setId(subgroupId);
            subgroup.setIsActive(false);
            boolean updated = this.updateById(subgroup);

            if (updated) {
                log.info("【解散小组】成功: subgroupId={}, userId={}", subgroupId, userId);
                return true;
            } else {
                log.warn("【解散小组】失败 - 更新失败: subgroupId={}, userId={}", subgroupId, userId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("【解散小组】异常: subgroupId={}, userId={}, error={}", subgroupId, userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class) 
    public Boolean sendSubgroupMessage(Integer subgroupId, Integer sendUserId, String msgType, String content) {
        log.info("发送小组消息: subgroupId={}, sendUserId={}, msgType={}", subgroupId, sendUserId, msgType);
        
        // 检查用户是否是小组成员
        if (!isSubgroupMember(subgroupId, sendUserId)) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "您不是小组成员，无法发送消息");
        }

        // 获取小组信息
        ChatSubgroup subgroup = this.getById(subgroupId);
        if (subgroup == null || !subgroup.getIsActive()) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "小组不存在或已解散");
        }

        try {
            // 方式1：使用普通群聊的存储机制保存小组消息
            ChatMsgAddRequest msgAddRequest = new ChatMsgAddRequest();
            msgAddRequest.setChatId("SUBGROUP_" + subgroupId); // 小组聊天室ID，使用前缀区分
            msgAddRequest.setChatType("SUBGROUP"); // 小组聊天类型
            msgAddRequest.setGroupId(subgroupId); // 使用小组ID作为群组ID
            msgAddRequest.setMsgType(msgType);
            msgAddRequest.setMsg(content);
            msgAddRequest.setSendUserId(sendUserId);
            msgAddRequest.setToUserId(subgroupId); // 接收者为小组ID
            msgAddRequest.setDeviceType("PC");
            
            // 调用统一的消息保存服务（这会自动处理消息关系表、聊天室更新等）
            Integer mainMsgId = chatMsgService.saveMsg(msgAddRequest);
            
            if (mainMsgId == null || mainMsgId <= 0) {
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "保存小组消息到主表失败");
            }
            
            // 方式2：同时保存到小组消息表（用于小组特定的查询和统计）
            ChatSubgroupMsg subgroupMsg = new ChatSubgroupMsg();
            subgroupMsg.setSubgroupId(subgroupId);
            subgroupMsg.setParentGroupId(subgroup.getParentGroupId());
            subgroupMsg.setSendUserId(sendUserId);
            subgroupMsg.setMsgType(msgType);
            subgroupMsg.setMsg(content);
            subgroupMsg.setSendTime(DateUtil.now());
            subgroupMsg.setTimestamp(System.currentTimeMillis());
            
            boolean subgroupSaved = chatSubgroupMsgDao.insert(subgroupMsg) > 0;
            if (!subgroupSaved) {
                log.warn("小组消息记录表保存失败，但主消息已保存: mainMsgId={}", mainMsgId);
            }
            
            log.info("小组消息保存成功: mainMsgId={}, subgroupMsgId={}, subgroupId={}", 
                mainMsgId, subgroupMsg.getId(), subgroupId);
            
            // TODO: 通过WebSocket发送消息到小组成员 - 这里会由chatMsgService自动处理
            List<Integer> memberIds = getSubgroupMemberIds(subgroupId);
            log.info("小组消息需要推送给成员: memberIds={}, mainMsgId={}", memberIds, mainMsgId);
            
            return true;
            
        } catch (Exception e) {
            log.error("发送小组消息失败: subgroupId={}, sendUserId={}, error={}", 
                subgroupId, sendUserId, e.getMessage(), e);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "发送小组消息失败: " + e.getMessage());
        }
    }

    @Override
    public ChatSubgroup getCurrentSubgroup(Integer userId, Integer parentGroupId) {
        ChatSubgroupMember member = chatSubgroupMemberDao.getUserCurrentSubgroup(userId, parentGroupId);
        if (member == null) {
            return null;
        }
        return this.getById(member.getSubgroupId());
    }

    @Override
    public List<ChatSubgroupInvite> getUserSubgroupInvites(Integer userId) {
        return chatSubgroupInviteDao.getUserPendingInvites(userId);
    }

    @Override
    public Boolean canJoinSubgroup(Integer userId, Integer parentGroupId) {
        Integer count = chatSubgroupMemberDao.getUserActiveSubgroupCount(userId, parentGroupId);
        return count == 0; // 只有当用户不在任何小组中时才能加入新小组
    }

    @Override
    public List<ChatSubgroupMember> getSubgroupMembers(Integer subgroupId) {
        return chatSubgroupMemberDao.getSubgroupMembers(subgroupId);
    }

    @Override
    public Boolean isSubgroupMember(Integer subgroupId, Integer userId) {
        Integer count = chatSubgroupMemberDao.isSubgroupMember(subgroupId, userId);
        return count > 0;
    }

    @Override
    public List<Integer> getSubgroupMemberIds(Integer subgroupId) {
        return chatSubgroupMemberDao.getSubgroupMemberIds(subgroupId);
    }

    @Override
    public List<ChatSubgroupMsg> getSubgroupMessages(Integer subgroupId, Integer limit) {
        log.info("获取小组消息列表: subgroupId={}, limit={}", subgroupId, limit);
        
        // 默认查询最新的20条消息
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        
        return chatSubgroupMsgDao.getSubgroupMessages(subgroupId, limit);
    }

    @Override
    public List<ChatSubgroupMsg> getSubgroupMessageHistory(Integer subgroupId, String beforeTime, Integer limit) {
        log.info("获取小组消息历史: subgroupId={}, beforeTime={}, limit={}", subgroupId, beforeTime, limit);
        
        // 默认查询20条消息
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        
        return chatSubgroupMsgDao.getSubgroupMessageHistory(subgroupId, beforeTime, limit);
    }

    @Override
    public List<ChatSubgroupMsg> searchSubgroupMessages(Integer subgroupId, String keyword, Integer limit) {
        log.info("搜索小组消息: subgroupId={}, keyword={}, limit={}", subgroupId, keyword, limit);
        
        if (StrUtil.isBlank(keyword)) {
            return new ArrayList<>();
        }
        
        // 默认搜索50条消息
        if (limit == null || limit <= 0) {
            limit = 50;
        }
        
        return chatSubgroupMsgDao.searchSubgroupMessages(subgroupId, keyword, limit);
    }

    @Override
    public Integer getSubgroupMessageCount(Integer subgroupId) {
        log.info("获取小组消息统计: subgroupId={}", subgroupId);
        return chatSubgroupMsgDao.getSubgroupMessageCount(subgroupId);
    }



    /**
     * 验证用户是否都是群成员
     */
    private void validateGroupMembers(Integer parentGroupId, List<Integer> userIds) {
        List<Integer> groupMemberIds = chatGroupMemberService.getChatGroupMemberIdListByGroupId(parentGroupId);
        for (Integer userId : userIds) {
            if (!groupMemberIds.contains(userId)) {
                throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "邀请的用户不是群成员");
            }
        }
    }
} 