package com.dot.msg.chat.controller;

import com.dot.comm.entity.ResultBean;
import com.dot.comm.exception.ApiException;
import com.dot.msg.chat.model.ChatSubgroup;
import com.dot.msg.chat.model.ChatSubgroupInvite;
import com.dot.msg.chat.model.ChatSubgroupMember;
import com.dot.msg.chat.model.ChatSubgroupMsg;
import com.dot.msg.chat.response.ChatUserResponse;
import com.dot.msg.chat.service.ChatSubgroupService;
import com.dot.msg.chat.service.ChatUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 群内小组管理控制器
 *
 * @author: 吴安然
 * @date: 2024-12-XX
 */
@Slf4j
@RestController
@RequestMapping("api/msg/chat/subgroup")
@Tag(name = "群内小组管理")
public class ChatSubgroupController {

    @Resource
    private ChatSubgroupService chatSubgroupService;

    @Resource
    private ChatUserService chatUserService;

    /**
     * 创建小组
     */
    @PostMapping("/create")
    @Operation(summary = "创建小组")
    public ResultBean<Integer> createSubgroup(
            @RequestParam("parentGroupId") @NotNull(message = "群组ID不能为空") Integer parentGroupId,
            @RequestParam("name") @NotBlank(message = "小组名称不能为空") String name,
            @RequestParam("memberIds") @NotEmpty(message = "成员ID列表不能为空") List<Integer> memberIds) {
        
        ChatUserResponse currentUser = chatUserService.getCurrentChatUser();
        Integer subgroupId = chatSubgroupService.createSubgroup(parentGroupId, name, currentUser.getId(), memberIds);
        return ResultBean.success(subgroupId);
    }

    /**
     * 邀请成员加入小组
     */
    @PostMapping("/invite")
    @Operation(summary = "邀请成员加入小组")
    public ResultBean<Boolean> inviteMembers(
            @RequestParam("subgroupId") @NotNull(message = "小组ID不能为空") Integer subgroupId,
            @RequestParam("inviteeIds") @NotEmpty(message = "被邀请人ID列表不能为空") List<Integer> inviteeIds) {
        
        ChatUserResponse currentUser = chatUserService.getCurrentChatUser();
        Boolean result = chatSubgroupService.inviteMembersToSubgroup(subgroupId, currentUser.getId(), inviteeIds);
        return ResultBean.result(result);
    }

    /**
     * 接受小组邀请
     */
    @PostMapping("/accept")
    @Operation(summary = "接受小组邀请")
    public ResultBean<Boolean> acceptInvite(@RequestParam("inviteId") Integer inviteId) {
        log.info("【控制器-接受小组邀请】请求开始: inviteId={}", inviteId);
        
        try {
            // 1. 参数验证
            if (inviteId == null) {
                log.warn("【控制器-接受小组邀请】参数错误: inviteId为空");
                return ResultBean.validateFailed("邀请ID不能为空");
            }
            
            // 2. 用户身份验证
            ChatUserResponse currentUser = chatUserService.getCurrentChatUser();
            if (currentUser == null) {
                log.warn("【控制器-接受小组邀请】用户未登录: inviteId={}", inviteId);
                return ResultBean.unauthorized();
            }
            
            log.info("【控制器-接受小组邀请】开始处理: inviteId={}, userId={}", inviteId, currentUser.getId());
            
            // 3. 调用服务层处理
            Boolean result = chatSubgroupService.acceptSubgroupInvite(inviteId, currentUser.getId());
            
            if (result != null && result) {
                log.info("【控制器-接受小组邀请】处理成功: inviteId={}, userId={}", inviteId, currentUser.getId());
                return ResultBean.success(true, "成功加入小组");
            } else {
                log.warn("【控制器-接受小组邀请】处理失败: inviteId={}, userId={}, result={}", 
                    inviteId, currentUser.getId(), result);
                return ResultBean.failed("加入小组失败");
            }
            
        } catch (ApiException e) {
            // 业务异常，返回具体错误信息
            log.warn("【控制器-接受小组邀请】业务异常: inviteId={}, error={}", inviteId, e.getMessage());
            return ResultBean.failed(e.getMessage());
        } catch (Exception e) {
            // 系统异常，返回通用错误信息
            log.error("【控制器-接受小组邀请】系统异常: inviteId={}, error={}", inviteId, e.getMessage(), e);
            return ResultBean.failed("系统繁忙，请稍后重试");
        }
    }

    /**
     * 拒绝小组邀请
     */
    @PostMapping("/reject")
    @Operation(summary = "拒绝小组邀请")
    public ResultBean<Boolean> rejectInvite(@RequestParam("inviteId") Integer inviteId) {
        log.info("【控制器-拒绝小组邀请】请求开始: inviteId={}", inviteId);
        
        try {
            // 1. 参数验证
            if (inviteId == null) {
                log.warn("【控制器-拒绝小组邀请】参数错误: inviteId为空");
                return ResultBean.validateFailed("邀请ID不能为空");
            }
            
            // 2. 用户身份验证
            ChatUserResponse currentUser = chatUserService.getCurrentChatUser();
            if (currentUser == null) {
                log.warn("【控制器-拒绝小组邀请】用户未登录: inviteId={}", inviteId);
                return ResultBean.unauthorized();
            }
            
            log.info("【控制器-拒绝小组邀请】开始处理: inviteId={}, userId={}", inviteId, currentUser.getId());
            
            // 3. 调用服务层处理
            Boolean result = chatSubgroupService.rejectSubgroupInvite(inviteId, currentUser.getId());
            
            if (result != null && result) {
                log.info("【控制器-拒绝小组邀请】处理成功: inviteId={}, userId={}", inviteId, currentUser.getId());
                return ResultBean.success(true, "已拒绝邀请");
            } else {
                log.warn("【控制器-拒绝小组邀请】处理失败: inviteId={}, userId={}, result={}", 
                    inviteId, currentUser.getId(), result);
                return ResultBean.failed("拒绝邀请失败");
            }
            
        } catch (ApiException e) {
            // 业务异常，返回具体错误信息
            log.warn("【控制器-拒绝小组邀请】业务异常: inviteId={}, error={}", inviteId, e.getMessage());
            return ResultBean.failed(e.getMessage());
        } catch (Exception e) {
            // 系统异常，返回通用错误信息
            log.error("【控制器-拒绝小组邀请】系统异常: inviteId={}, error={}", inviteId, e.getMessage(), e);
            return ResultBean.failed("系统繁忙，请稍后重试");
        }
    }

    /**
     * 退出小组
     */
    @PostMapping("/leave")
    @Operation(summary = "退出小组")
    public ResultBean<Boolean> leaveSubgroup(
            @RequestParam("subgroupId") @NotNull(message = "小组ID不能为空") Integer subgroupId) {
        
        ChatUserResponse currentUser = chatUserService.getCurrentChatUser();
        Boolean result = chatSubgroupService.leaveSubgroup(subgroupId, currentUser.getId());
        return ResultBean.result(result);
    }

    /**
     * 解散小组（仅组长可操作）
     */
    @PostMapping("/dissolve")
    @Operation(summary = "解散小组")
    public ResultBean<Boolean> dissolveSubgroup(
            @RequestParam("subgroupId") @NotNull(message = "小组ID不能为空") Integer subgroupId) {
        
        ChatUserResponse currentUser = chatUserService.getCurrentChatUser();
        Boolean result = chatSubgroupService.dissolveSubgroup(subgroupId, currentUser.getId());
        return ResultBean.result(result);
    }

    /**
     * 发送小组消息
     */
    @PostMapping("/sendMessage")
    @Operation(summary = "发送小组消息")
    public ResultBean<Boolean> sendMessage(
            @RequestParam("subgroupId") @NotNull(message = "小组ID不能为空") Integer subgroupId,
            @RequestParam("msgType") @NotBlank(message = "消息类型不能为空") String msgType,
            @RequestParam("content") @NotBlank(message = "消息内容不能为空") String content) {
        
        ChatUserResponse currentUser = chatUserService.getCurrentChatUser();
        Boolean result = chatSubgroupService.sendSubgroupMessage(subgroupId, currentUser.getId(), msgType, content);
        return ResultBean.result(result);
    }

    /**
     * 获取当前参与的小组
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前参与的小组")
    public ResultBean<ChatSubgroup> getCurrentSubgroup(
            @RequestParam("parentGroupId") @NotNull(message = "群组ID不能为空") Integer parentGroupId) {
        
        ChatUserResponse currentUser = chatUserService.getCurrentChatUser();
        ChatSubgroup subgroup = chatSubgroupService.getCurrentSubgroup(currentUser.getId(), parentGroupId);
        return ResultBean.success(subgroup);
    }

    /**
     * 获取小组邀请列表
     */
    @GetMapping("/invites")
    @Operation(summary = "获取小组邀请列表")
    public ResultBean<List<ChatSubgroupInvite>> getInvites() {
        System.out.println("=== 获取小组邀请API调用 ===");
        
        try {
            ChatUserResponse currentUser = chatUserService.getCurrentChatUser();
            if (currentUser == null) {
                System.out.println("用户未登录");
                return ResultBean.success(new ArrayList<>());
            }
            
            System.out.println("当前登录用户: ID=" + currentUser.getId() + ", 昵称=" + currentUser.getNickname());
            
            List<ChatSubgroupInvite> invites = chatSubgroupService.getUserSubgroupInvites(currentUser.getId());
            System.out.println("控制器收到结果: " + (invites != null ? invites.size() : 0) + "条邀请");
            
            return ResultBean.success(invites != null ? invites : new ArrayList<>());
            
        } catch (Exception e) {
            System.out.println("控制器异常: " + e.getMessage());
            e.printStackTrace();
            return ResultBean.success(new ArrayList<>());
        }
    }

    /**
     * 获取小组成员列表
     */
    @GetMapping("/members")
    @Operation(summary = "获取小组成员列表")
    public ResultBean<List<ChatSubgroupMember>> getSubgroupMembers(
            @RequestParam("subgroupId") @NotNull(message = "小组ID不能为空") Integer subgroupId) {
        
        List<ChatSubgroupMember> members = chatSubgroupService.getSubgroupMembers(subgroupId);
        return ResultBean.success(members);
    }

    /**
     * 检查是否可以加入小组
     */
    @GetMapping("/canJoin")
    @Operation(summary = "检查是否可以加入小组")
    public ResultBean<Boolean> canJoinSubgroup(
            @RequestParam("parentGroupId") @NotNull(message = "群组ID不能为空") Integer parentGroupId) {
        
        ChatUserResponse currentUser = chatUserService.getCurrentChatUser();
        Boolean canJoin = chatSubgroupService.canJoinSubgroup(currentUser.getId(), parentGroupId);
        return ResultBean.success(canJoin);
    }

    /**
     * 获取小组消息列表
     */
    @GetMapping("/messages")
    @Operation(summary = "获取小组消息列表")
    public ResultBean<List<ChatSubgroupMsg>> getSubgroupMessages(
            @RequestParam("subgroupId") @NotNull(message = "小组ID不能为空") Integer subgroupId,
            @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        
        List<ChatSubgroupMsg> messages = chatSubgroupService.getSubgroupMessages(subgroupId, limit);
        return ResultBean.success(messages);
    }

    /**
     * 获取小组消息历史（分页）
     */
    @GetMapping("/messages/history")
    @Operation(summary = "获取小组消息历史")
    public ResultBean<List<ChatSubgroupMsg>> getSubgroupMessageHistory(
            @RequestParam("subgroupId") @NotNull(message = "小组ID不能为空") Integer subgroupId,
            @RequestParam("beforeTime") @NotBlank(message = "时间点不能为空") String beforeTime,
            @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        
        List<ChatSubgroupMsg> messages = chatSubgroupService.getSubgroupMessageHistory(subgroupId, beforeTime, limit);
        return ResultBean.success(messages);
    }

    /**
     * 搜索小组消息
     */
    @GetMapping("/messages/search")
    @Operation(summary = "搜索小组消息")
    public ResultBean<List<ChatSubgroupMsg>> searchSubgroupMessages(
            @RequestParam("subgroupId") @NotNull(message = "小组ID不能为空") Integer subgroupId,
            @RequestParam("keyword") @NotBlank(message = "搜索关键词不能为空") String keyword,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit) {
        
        List<ChatSubgroupMsg> messages = chatSubgroupService.searchSubgroupMessages(subgroupId, keyword, limit);
        return ResultBean.success(messages);
    }

    /**
     * 获取小组消息统计
     */
    @GetMapping("/messages/count")
    @Operation(summary = "获取小组消息统计")
    public ResultBean<Integer> getSubgroupMessageCount(
            @RequestParam("subgroupId") @NotNull(message = "小组ID不能为空") Integer subgroupId) {
        
        Integer count = chatSubgroupService.getSubgroupMessageCount(subgroupId);
        return ResultBean.success(count);
    }
} 