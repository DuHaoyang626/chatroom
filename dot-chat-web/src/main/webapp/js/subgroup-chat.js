/**
 * 群内小组聊天功能
 * @author: 吴安然
 * @date: 2024-12-XX
 */

// 小组状态管理
var subgroupState = {
    currentSubgroup: null,        // 当前参与的小组
    pendingInvites: [],          // 待处理的邀请
    isInSubgroupMode: false,     // 是否在小组模式
    currentGroupId: null         // 当前群组ID
};

/**
 * 初始化小组功能
 */
function initSubgroupFeatures(groupId) {
    subgroupState.currentGroupId = groupId;
    
    // 添加小组管理按钮到群聊界面
    addSubgroupManagementButton();
    
    // 加载用户当前的小组状态
    loadCurrentSubgroupStatus();
    
    // 加载待处理的邀请
    loadPendingInvites();
}

/**
 * 添加小组管理按钮
 */
function addSubgroupManagementButton() {
    const groupToolbar = document.querySelector('.group-toolbar');
    if (groupToolbar && !document.querySelector('.subgroup-btn')) {
        const subgroupBtn = document.createElement('button');
        subgroupBtn.innerHTML = '<i class="fa fa-users"></i> 小组聊天';
        subgroupBtn.classList.add('subgroup-btn', 'btn', 'btn-info', 'btn-sm');
        subgroupBtn.onclick = showSubgroupManagement;
        groupToolbar.appendChild(subgroupBtn);
    }
}

/**
 * 显示小组管理面板
 */
function showSubgroupManagement() {
    const modal = `
        <div id="subgroupModal" class="modal fade" tabindex="-1" role="dialog">
            <div class="modal-dialog modal-lg" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">小组聊天管理</h5>
                        <button type="button" class="close" data-dismiss="modal">
                            <span>&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <ul class="nav nav-tabs" role="tablist">
                            <li class="nav-item">
                                <a class="nav-link active" data-toggle="tab" href="#currentSubgroupTab">当前小组</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" data-toggle="tab" href="#createSubgroupTab">创建小组</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" data-toggle="tab" href="#invitesTab">邀请处理</a>
                            </li>
                        </ul>
                        
                        <div class="tab-content mt-3">
                            <!-- 当前小组Tab -->
                            <div id="currentSubgroupTab" class="tab-pane fade show active">
                                <div id="currentSubgroupInfo">
                                    <div class="text-center">
                                        <div class="spinner-border" role="status">
                                            <span class="sr-only">加载中...</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            
                            <!-- 创建小组Tab -->
                            <div id="createSubgroupTab" class="tab-pane fade">
                                <div class="form-group">
                                    <label for="subgroupName">小组名称:</label>
                                    <input type="text" class="form-control" id="subgroupName" placeholder="请输入小组名称">
                                </div>
                                <div class="form-group">
                                    <label>邀请成员:</label>
                                    <div id="memberSelector" class="border p-3" style="max-height: 200px; overflow-y: auto;">
                                        <div class="text-center">
                                            <div class="spinner-border" role="status">
                                                <span class="sr-only">加载中...</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <button class="btn btn-primary" onclick="createSubgroup()">创建小组</button>
                            </div>
                            
                            <!-- 邀请处理Tab -->
                            <div id="invitesTab" class="tab-pane fade">
                                <div id="invitesList">
                                    <div class="text-center">
                                        <div class="spinner-border" role="status">
                                            <span class="sr-only">加载中...</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">关闭</button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    // 移除已存在的模态框
    if (document.getElementById('subgroupModal')) {
        document.getElementById('subgroupModal').remove();
    }
    
    document.body.insertAdjacentHTML('beforeend', modal);
    
    // 显示模态框
    $('#subgroupModal').modal('show');
    
    // 初始化模态框内容
    initSubgroupModalContent();
}

/**
 * 初始化模态框内容
 */
function initSubgroupModalContent() {
    loadCurrentSubgroupInfo();
    loadGroupMembers();
    loadInvitesList();
}

/**
 * 加载当前小组信息
 */
function loadCurrentSubgroupInfo() {
    $.ajax({
        url: '/api/msg/chat/subgroup/current',
        method: 'GET',
        data: { parentGroupId: subgroupState.currentGroupId },
        success: function(response) {
            if (response.success && response.data) {
                subgroupState.currentSubgroup = response.data;
                displayCurrentSubgroupInfo(response.data);
            } else {
                displayNoSubgroupInfo();
            }
        },
        error: function() {
            displayNoSubgroupInfo();
        }
    });
}

/**
 * 显示当前小组信息
 */
function displayCurrentSubgroupInfo(subgroup) {
    const html = `
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">${subgroup.name}</h5>
                <p class="card-text">
                    <small class="text-muted">成员数量: ${subgroup.memberCount}</small><br>
                    <small class="text-muted">创建时间: ${subgroup.createTime}</small>
                </p>
                <div class="btn-group">
                    <button class="btn btn-sm btn-success" onclick="toggleSubgroupMode()">
                        ${subgroupState.isInSubgroupMode ? '退出小组模式' : '进入小组模式'}
                    </button>
                    <button class="btn btn-sm btn-info" onclick="loadSubgroupMembers(${subgroup.id})">查看成员</button>
                    <button class="btn btn-sm btn-warning" onclick="leaveSubgroup()">退出小组</button>
                </div>
            </div>
        </div>
    `;
    document.getElementById('currentSubgroupInfo').innerHTML = html;
}

/**
 * 显示无小组信息
 */
function displayNoSubgroupInfo() {
    const html = `
        <div class="alert alert-info">
            <h5>您当前没有参与任何小组</h5>
            <p>您可以创建新小组或等待其他人邀请您加入小组。</p>
            <button class="btn btn-primary" onclick="$('#createSubgroupTab').tab('show')">创建小组</button>
        </div>
    `;
    document.getElementById('currentSubgroupInfo').innerHTML = html;
}

/**
 * 加载群成员列表
 */
function loadGroupMembers() {
    $.ajax({
        url: '/api/msg/chat/group/members',
        method: 'GET',
        data: { groupId: subgroupState.currentGroupId },
        success: function(response) {
            if (response.success && response.data) {
                displayGroupMembers(response.data);
            } else {
                document.getElementById('memberSelector').innerHTML = '<div class="alert alert-warning">加载群成员失败</div>';
            }
        },
        error: function() {
            document.getElementById('memberSelector').innerHTML = '<div class="alert alert-danger">加载群成员失败</div>';
        }
    });
}

/**
 * 显示群成员列表
 */
function displayGroupMembers(members) {
    let html = '<div class="row">';
    members.forEach(member => {
        html += `
            <div class="col-md-6 mb-2">
                <div class="custom-control custom-checkbox">
                    <input type="checkbox" class="custom-control-input member-checkbox" 
                           id="member-${member.userId}" value="${member.userId}">
                    <label class="custom-control-label" for="member-${member.userId}">
                        <img src="${member.avatar || '/images/default-avatar.png'}" 
                             class="rounded-circle" width="24" height="24">
                        ${member.nickname || member.username}
                    </label>
                </div>
            </div>
        `;
    });
    html += '</div>';
    document.getElementById('memberSelector').innerHTML = html;
}

/**
 * 创建小组
 */
function createSubgroup() {
    const name = document.getElementById('subgroupName').value.trim();
    if (!name) {
        layer.msg('请输入小组名称');
        return;
    }
    
    const selectedMembers = getSelectedMembers();
    if (selectedMembers.length === 0) {
        layer.msg('请选择至少一个成员');
        return;
    }
    
    // 检查是否已经在其他小组中
    checkCanJoinSubgroup().then(canJoin => {
        if (!canJoin) {
            layer.msg('您已经在其他小组中，无法创建新小组');
            return;
        }
        
        // 调用API创建小组
        $.ajax({
            url: '/api/msg/chat/subgroup/create',
            method: 'POST',
            data: {
                parentGroupId: subgroupState.currentGroupId,
                name: name,
                memberIds: selectedMembers
            },
            success: function(response) {
                if (response.success) {
                    layer.msg('小组创建成功');
                    $('#subgroupModal').modal('hide');
                    loadCurrentSubgroupStatus();
                } else {
                    layer.msg(response.message || '创建失败');
                }
            },
            error: function() {
                layer.msg('网络错误，请稍后重试');
            }
        });
    });
}

/**
 * 获取选中的成员
 */
function getSelectedMembers() {
    const checkboxes = document.querySelectorAll('.member-checkbox:checked');
    return Array.from(checkboxes).map(cb => parseInt(cb.value));
}

/**
 * 检查是否可以加入小组
 */
function checkCanJoinSubgroup() {
    return new Promise((resolve) => {
        $.ajax({
            url: '/api/msg/chat/subgroup/canJoin',
            method: 'GET',
            data: { parentGroupId: subgroupState.currentGroupId },
            success: function(response) {
                resolve(response.success && response.data);
            },
            error: function() {
                resolve(false);
            }
        });
    });
}

/**
 * 加载待处理邀请
 */
function loadInvitesList() {
    $.ajax({
        url: '/api/msg/chat/subgroup/invites',
        method: 'GET',
        success: function(response) {
            if (response.success) {
                displayInvitesList(response.data || []);
            } else {
                document.getElementById('invitesList').innerHTML = '<div class="alert alert-warning">加载邀请失败</div>';
            }
        },
        error: function() {
            document.getElementById('invitesList').innerHTML = '<div class="alert alert-danger">加载邀请失败</div>';
        }
    });
}

/**
 * 显示邀请列表
 */
function displayInvitesList(invites) {
    if (invites.length === 0) {
        document.getElementById('invitesList').innerHTML = '<div class="alert alert-info">暂无待处理邀请</div>';
        return;
    }
    
    let html = '<div class="list-group">';
    invites.forEach(invite => {
        html += `
            <div class="list-group-item">
                <div class="d-flex w-100 justify-content-between">
                    <h6 class="mb-1">邀请加入小组: ${invite.subgroupName || '未知小组'}</h6>
                    <small>${invite.inviteTime}</small>
                </div>
                <div class="mt-2">
                    <button class="btn btn-sm btn-success" onclick="handleSubgroupInvite(${invite.id}, 'accept')">
                        接受
                    </button>
                    <button class="btn btn-sm btn-secondary ml-2" onclick="handleSubgroupInvite(${invite.id}, 'reject')">
                        拒绝
                    </button>
                </div>
            </div>
        `;
    });
    html += '</div>';
    document.getElementById('invitesList').innerHTML = html;
}

/**
 * 处理小组邀请
 */
function handleSubgroupInvite(inviteId, action) {
    const url = action === 'accept' ? '/api/msg/chat/subgroup/accept' : '/api/msg/chat/subgroup/reject';
    
    $.ajax({
        url: url,
        method: 'POST',
        data: { inviteId: inviteId },
        success: function(response) {
            if (response.success) {
                layer.msg(action === 'accept' ? '已接受邀请' : '已拒绝邀请');
                loadInvitesList();
                if (action === 'accept') {
                    loadCurrentSubgroupInfo();
                }
            } else {
                layer.msg(response.message || '操作失败');
            }
        },
        error: function() {
            layer.msg('网络错误，请稍后重试');
        }
    });
}

/**
 * 切换小组聊天模式
 */
function toggleSubgroupMode() {
    if (!subgroupState.currentSubgroup) {
        layer.msg('请先加入小组');
        return;
    }
    
    subgroupState.isInSubgroupMode = !subgroupState.isInSubgroupMode;
    
    // 更新界面显示
    updateSubgroupModeUI();
    
    // 更新按钮文本
    loadCurrentSubgroupInfo();
}

/**
 * 更新小组模式UI
 */
function updateSubgroupModeUI() {
    const chatContainer = document.querySelector('.chat-container');
    let subgroupIndicator = document.querySelector('.subgroup-indicator');
    
    if (subgroupState.isInSubgroupMode && subgroupState.currentSubgroup) {
        // 进入小组模式
        if (chatContainer) {
            chatContainer.classList.add('subgroup-mode');
        }
        
        if (!subgroupIndicator) {
            subgroupIndicator = document.createElement('div');
            subgroupIndicator.className = 'subgroup-indicator alert alert-info';
            subgroupIndicator.style.position = 'fixed';
            subgroupIndicator.style.top = '10px';
            subgroupIndicator.style.right = '10px';
            subgroupIndicator.style.zIndex = '9999';
            document.body.appendChild(subgroupIndicator);
        }
        
        subgroupIndicator.innerHTML = `
            <strong>小组模式</strong><br>
            当前小组: ${subgroupState.currentSubgroup.name}
            <button class="btn btn-sm btn-secondary ml-2" onclick="toggleSubgroupMode()">退出</button>
        `;
        subgroupIndicator.style.display = 'block';
    } else {
        // 退出小组模式
        if (chatContainer) {
            chatContainer.classList.remove('subgroup-mode');
        }
        
        if (subgroupIndicator) {
            subgroupIndicator.style.display = 'none';
        }
    }
}

/**
 * 退出小组
 */
function leaveSubgroup() {
    if (!subgroupState.currentSubgroup) {
        layer.msg('您当前不在任何小组中');
        return;
    }
    
    layer.confirm('确定要退出当前小组吗？', {
        btn: ['确定', '取消']
    }, function() {
        $.ajax({
            url: '/api/msg/chat/subgroup/leave',
            method: 'POST',
            data: { subgroupId: subgroupState.currentSubgroup.id },
            success: function(response) {
                if (response.success) {
                    layer.msg('已退出小组');
                    subgroupState.currentSubgroup = null;
                    subgroupState.isInSubgroupMode = false;
                    updateSubgroupModeUI();
                    loadCurrentSubgroupInfo();
                } else {
                    layer.msg(response.message || '退出失败');
                }
            },
            error: function() {
                layer.msg('网络错误，请稍后重试');
            }
        });
    });
}

/**
 * 加载当前小组状态
 */
function loadCurrentSubgroupStatus() {
    loadCurrentSubgroupInfo();
    loadPendingInvites();
}

/**
 * 加载待处理邀请
 */
function loadPendingInvites() {
    $.ajax({
        url: '/api/msg/chat/subgroup/invites',
        method: 'GET',
        success: function(response) {
            if (response.success && response.data) {
                subgroupState.pendingInvites = response.data;
                updateInviteNotification();
            }
        }
    });
}

/**
 * 更新邀请通知
 */
function updateInviteNotification() {
    const count = subgroupState.pendingInvites.length;
    const btn = document.querySelector('.subgroup-btn');
    
    if (btn) {
        if (count > 0) {
            btn.innerHTML = `<i class="fa fa-users"></i> 小组聊天 <span class="badge badge-danger">${count}</span>`;
        } else {
            btn.innerHTML = '<i class="fa fa-users"></i> 小组聊天';
        }
    }
}

// 页面加载完成后初始化小组功能
$(document).ready(function() {
    // 如果在群聊页面，初始化小组功能
    if (typeof currentGroupId !== 'undefined' && currentGroupId) {
        initSubgroupFeatures(currentGroupId);
    }
}); 