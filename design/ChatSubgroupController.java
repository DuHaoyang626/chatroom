/**
 * 群内小组管理控制器
 *
 * @author: 吴安然
 * @date: 2024-12-XX
 */
@RestController
@RequestMapping("api/msg/chat/subgroup")
@Tag(name = "群内小组管理")
public class ChatSubgroupController {

    @Resource
    private ChatSubgroupService chatSubgroupService;

    /**
     * 创建小组
     */
    @PostMapping("/create")
    @Operation(summary = "创建小组")
    public ResultBean<Integer> createSubgroup(
            @RequestParam("parentGroupId") @NotNull(message = "群组ID不能为空") Integer parentGroupId,
            @RequestParam("name") @NotBlank(message = "小组名称不能为空") String name,
            @RequestParam("memberIds") @NotEmpty(message = "成员ID列表不能为空") List<Integer> memberIds) {
        return ResultBean.success(chatSubgroupService.createSubgroup(parentGroupId, name, memberIds));
    }

    /**
     * 邀请成员加入小组
     */
    @PostMapping("/invite")
    @Operation(summary = "邀请成员加入小组")
    public ResultBean<Boolean> inviteMembers(
            @RequestParam("subgroupId") @NotNull(message = "小组ID不能为空") Integer subgroupId,
            @RequestParam("inviteeIds") @NotEmpty(message = "被邀请人ID列表不能为空") List<Integer> inviteeIds) {
        return ResultBean.result(chatSubgroupService.inviteMembersToSubgroup(subgroupId, inviteeIds));
    }

    /**
     * 接受小组邀请
     */
    @PostMapping("/accept")
    @Operation(summary = "接受小组邀请")
    public ResultBean<Boolean> acceptInvite(
            @RequestParam("inviteId") @NotNull(message = "邀请ID不能为空") Integer inviteId) {
        return ResultBean.result(chatSubgroupService.acceptSubgroupInvite(inviteId));
    }

    /**
     * 拒绝小组邀请
     */
    @PostMapping("/reject")
    @Operation(summary = "拒绝小组邀请")
    public ResultBean<Boolean> rejectInvite(
            @RequestParam("inviteId") @NotNull(message = "邀请ID不能为空") Integer inviteId) {
        return ResultBean.result(chatSubgroupService.rejectSubgroupInvite(inviteId));
    }

    /**
     * 退出小组
     */
    @PostMapping("/leave")
    @Operation(summary = "退出小组")
    public ResultBean<Boolean> leaveSubgroup(
            @RequestParam("subgroupId") @NotNull(message = "小组ID不能为空") Integer subgroupId) {
        return ResultBean.result(chatSubgroupService.leaveSubgroup(subgroupId));
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
        return ResultBean.result(chatSubgroupService.sendSubgroupMessage(subgroupId, msgType, content));
    }

    /**
     * 获取当前参与的小组
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前参与的小组")
    public ResultBean<ChatSubgroup> getCurrentSubgroup(
            @RequestParam("parentGroupId") @NotNull(message = "群组ID不能为空") Integer parentGroupId) {
        ChatUserResponse currentUser = chatUserService.getCurrentChatUser();
        return ResultBean.success(chatSubgroupService.getCurrentSubgroup(currentUser.getId(), parentGroupId));
    }

    /**
     * 获取小组邀请列表
     */
    @GetMapping("/invites")
    @Operation(summary = "获取小组邀请列表")
    public ResultBean<List<ChatSubgroupInvite>> getInvites() {
        ChatUserResponse currentUser = chatUserService.getCurrentChatUser();
        return ResultBean.success(chatSubgroupService.getUserSubgroupInvites(currentUser.getId()));
    }
} 