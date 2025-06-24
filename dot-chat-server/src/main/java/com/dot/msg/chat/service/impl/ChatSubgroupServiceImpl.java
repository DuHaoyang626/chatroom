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
import com.dot.msg.chat.dao.ChatUserDao;
import com.dot.msg.chat.model.ChatSubgroup;
import com.dot.msg.chat.model.ChatSubgroupInvite;
import com.dot.msg.chat.model.ChatSubgroupMember;
import com.dot.msg.chat.model.ChatSubgroupMsg;
import com.dot.msg.chat.model.ChatUser;
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
import java.util.Map;

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
    private ChatUserDao chatUserDao;

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
        log.info("【接受邀请】V2 - 开始: inviteId={}, userId={}", inviteId, userId);

        // 1. 获取并校验邀请
        ChatSubgroupInvite invite = chatSubgroupInviteDao.selectById(inviteId);
        if (invite == null || !invite.getInviteeId().equals(userId)) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "邀请不存在或不属于您");
        }
        if (invite.getStatus() != 0) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "邀请已被处理，请勿重复操作");
        }

        // 2. 检查小组和群组状态
        ChatSubgroup subgroup = this.getById(invite.getSubgroupId());
        if (subgroup == null || !subgroup.getIsActive()) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "小组不存在或已解散");
        }

        // 3. 检查用户是否已在其他小组
        if (!canJoinSubgroup(userId, invite.getParentGroupId())) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "您已加入该群的其他小组，无法再加入");
        }
        
        // 4. 更新邀请状态为"已接受" (使用自定义的、可靠的更新方法)
        Integer updatedRows = chatSubgroupInviteDao.updateInviteStatus(inviteId, userId, 1, DateUtil.now());
        if (updatedRows == null || updatedRows <= 0) {
             log.warn("【接受邀请】V2 - 更新邀请状态失败，可能已被并发处理: inviteId={}", inviteId);
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "操作失败，邀请可能已被处理");
        }

        // 5. 将用户加入小组成员表
        ChatSubgroupMember member = new ChatSubgroupMember();
        member.setSubgroupId(invite.getSubgroupId());
        member.setUserId(userId);
        member.setParentGroupId(invite.getParentGroupId());
        member.setStatus(1); // 活跃成员
        member.setJoinTime(DateUtil.now());
        if (chatSubgroupMemberDao.insert(member) <= 0) {
            log.error("【接受邀请】V2 - 插入成员记录失败: subgroupId={}, userId={}", invite.getSubgroupId(), userId);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "加入小组失败，请稍后重试");
        }

        // 6. 更新小组的成员计数
        chatSubgroupDao.incrementMemberCount(invite.getSubgroupId());

        log.info("【接受邀请】V2 - 成功: userId={} 加入了 subgroupId={}", userId, invite.getSubgroupId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rejectSubgroupInvite(Integer inviteId, Integer userId) {
        log.info("【拒绝邀请】V2 - 开始: inviteId={}, userId={}", inviteId, userId);

        // 1. 获取并校验邀请
        ChatSubgroupInvite invite = chatSubgroupInviteDao.selectById(inviteId);
        if (invite == null || !invite.getInviteeId().equals(userId)) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "邀请不存在或不属于您");
        }
        if (invite.getStatus() != 0) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "邀请已被处理，请勿重复操作");
        }

        // 2. 更新邀请状态为"已拒绝"
        Integer updatedRows = chatSubgroupInviteDao.updateInviteStatus(inviteId, userId, 2, DateUtil.now());
        if (updatedRows == null || updatedRows <= 0) {
            log.warn("【拒绝邀请】V2 - 更新邀请状态失败，可能已被并发处理: inviteId={}", inviteId);
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "操作失败，邀请可能已被处理");
        }

        log.info("【拒绝邀请】V2 - 成功: userId={} 拒绝了来自 subgroupId={} 的邀请", userId, invite.getSubgroupId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean leaveSubgroup(Integer subgroupId, Integer userId) {
        log.info("【退出小组】V2 - 开始: subgroupId={}, userId={}", subgroupId, userId);

        // 1. 校验小组和成员是否存在
        ChatSubgroup subgroup = this.getById(subgroupId);
        if (subgroup == null) {
            log.warn("【退出小组】V2 - 小组不存在，视为退出成功: subgroupId={}", subgroupId);
            return true;
        }

        ChatSubgroupMember member = chatSubgroupMemberDao.getMemberInfo(subgroupId, userId);
        if (member == null) {
            log.warn("【退出小组】V2 - 用户并非小组成员，视为退出成功: subgroupId={}, userId={}", subgroupId, userId);
            return true;
        }

        // 2. 判断退出者身份
        boolean isCreator = userId.equals(subgroup.getCreatorId());

        if (isCreator) {
            // 如果是组长退出，则直接解散整个小组
            log.info("【退出小组】V2 - 组长退出，执行解散操作: subgroupId={}, creatorId={}", subgroupId, userId);
            return this.dissolveSubgroup(subgroupId, userId);
        } else {
            // 如果是普通成员退出
            // a. 删除该成员的记录
            if (chatSubgroupMemberDao.deleteById(member.getId()) <= 0) {
                log.error("【退出小组】V2 - 删除成员记录失败: memberId={}", member.getId());
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "退出小组失败");
            }

            // b. 更新小组成员数
            chatSubgroupDao.decrementMemberCount(subgroup.getId());
            log.info("【退出小组】V2 - 普通成员退出成功: subgroupId={}, userId={}", subgroupId, userId);
            return true;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean dissolveSubgroup(Integer subgroupId, Integer userId) {
        log.info("【解散小组】V3 - 开始: subgroupId={}, operatorId={}", subgroupId, userId);

        // 1. 校验小组是否存在
        ChatSubgroup subgroup = this.getById(subgroupId);
        if (subgroup == null) {
            log.warn("【解散小组】V3 - 小组已不存在，操作成功: subgroupId={}", subgroupId);
            return true;
        }

        // 2. 校验操作人是否为组长
        if (!userId.equals(subgroup.getCreatorId())) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "只有组长才能解散小组");
        }

        // 3. 直接从数据库中删除小组记录 (硬删除)
        // 数据库的 ON DELETE CASCADE 会自动清理所有关联的成员、邀请和消息
        if (this.removeById(subgroupId)) {
            log.info("【解散小组】V3 - 成功: 小组及所有关联数据已被彻底删除, subgroupId={}", subgroupId);
            return true;
        } else {
            log.error("【解散小组】V3 - 失败: 从数据库删除记录失败, subgroupId={}", subgroupId);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "解散小组失败，请稍后重试");
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
        // 直接调用DAO获取实体列表，MyBatis会自动完成映射
        List<ChatSubgroupInvite> invites = chatSubgroupInviteDao.getUserPendingInvites(userId);

        if (CollUtil.isEmpty(invites)) {
            return new ArrayList<>();
        }

        // 循环填充关联的显示名称
        for (ChatSubgroupInvite invite : invites) {
            // 填充小组名称
            ChatSubgroup subgroup = this.getById(invite.getSubgroupId());
            if (subgroup != null) {
                invite.setSubgroupName(subgroup.getName());
            } else {
                invite.setSubgroupName("未知小组");
            }

            // 填充邀请人昵称
            ChatUser inviter = chatUserDao.selectById(invite.getInviterId());
            if (inviter != null) {
                invite.setInviterNickname(inviter.getNickname());
            } else {
                invite.setInviterNickname("未知用户");
            }
        }
        return invites;
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