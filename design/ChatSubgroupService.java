/**
 * 群内小组服务接口
 *
 * @author: 吴安然  
 * @date: 2024-12-XX
 */
public interface ChatSubgroupService {

    /**
     * 创建小组
     *
     * @param parentGroupId 父群组ID
     * @param name 小组名称
     * @param memberIds 邀请的成员ID列表
     * @return 小组ID
     */
    Integer createSubgroup(Integer parentGroupId, String name, List<Integer> memberIds);

    /**
     * 邀请成员加入小组
     *
     * @param subgroupId 小组ID
     * @param inviteeIds 被邀请人ID列表
     * @return 是否成功
     */
    Boolean inviteMembersToSubgroup(Integer subgroupId, List<Integer> inviteeIds);

    /**
     * 接受小组邀请
     *
     * @param inviteId 邀请ID
     * @return 是否成功
     */
    Boolean acceptSubgroupInvite(Integer inviteId);

    /**
     * 拒绝小组邀请
     *
     * @param inviteId 邀请ID
     * @return 是否成功
     */
    Boolean rejectSubgroupInvite(Integer inviteId);

    /**
     * 退出小组
     *
     * @param subgroupId 小组ID
     * @return 是否成功
     */
    Boolean leaveSubgroup(Integer subgroupId);

    /**
     * 发送小组消息
     *
     * @param subgroupId 小组ID
     * @param msgType 消息类型
     * @param content 消息内容
     * @return 是否成功
     */
    Boolean sendSubgroupMessage(Integer subgroupId, String msgType, String content);

    /**
     * 获取用户当前参与的小组
     *
     * @param userId 用户ID
     * @param parentGroupId 父群组ID
     * @return 当前参与的小组（只能有一个）
     */
    ChatSubgroup getCurrentSubgroup(Integer userId, Integer parentGroupId);

    /**
     * 获取用户收到的小组邀请
     *
     * @param userId 用户ID
     * @return 邀请列表
     */
    List<ChatSubgroupInvite> getUserSubgroupInvites(Integer userId);

    /**
     * 检查用户是否可以加入小组（不能同时加入两个小组）
     *
     * @param userId 用户ID
     * @param parentGroupId 父群组ID
     * @return 是否可以加入
     */
    Boolean canJoinSubgroup(Integer userId, Integer parentGroupId);
} 