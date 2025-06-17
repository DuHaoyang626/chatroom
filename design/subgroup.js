/**
 * 群内小组聊天功能
 * @author: 吴安然
 * @date: 2024-12-XX
 */

// 小组状态管理
var subgroupState = {
    currentSubgroup: null,        // 当前参与的小组
    pendingInvites: [],          // 待处理的邀请
    isInSubgroupMode: false      // 是否在小组模式
};

/**
 * 初始化小组功能
 */
function initSubgroupFeatures() {
    // 添加小组管理按钮到群聊界面
    addSubgroupManagementButton();
    
    // 监听小组相关的WebSocket消息
    listenSubgroupMessages();
    
    // 加载用户当前的小组状态
    loadCurrentSubgroupStatus();
}

/**
 * 添加小组管理按钮
 */
function addSubgroupManagementButton() {
    const groupToolbar = document.querySelector('.group-toolbar');
    if (groupToolbar) {
        const subgroupBtn = document.createElement('button');
        subgroupBtn.innerHTML = '小组聊天';
        subgroupBtn.classList.add('subgroup-btn');
        subgroupBtn.onclick = showSubgroupManagement;
        groupToolbar.appendChild(subgroupBtn);
    }
}

/**
 * 显示小组管理面板
 */
function showSubgroupManagement() {
    const modal = `
        <div id="subgroupModal" class="modal">
            <div class="modal-content">
                <div class="modal-header">
                    <h3>小组聊天管理</h3>
                    <span class="close">&times;</span>
                </div>
                <div class="modal-body">
                    <div class="subgroup-tabs">
                        <button class="tab-btn active" onclick="showSubgroupTab('current')">当前小组</button>
                        <button class="tab-btn" onclick="showSubgroupTab('create')">创建小组</button>
                        <button class="tab-btn" onclick="showSubgroupTab('invites')">邀请处理</button>
                    </div>
                    
                    <!-- 当前小组Tab -->
                    <div id="currentSubgroupTab" class="tab-content active">
                        <div id="currentSubgroupInfo"></div>
                        <div id="subgroupChatArea"></div>
                    </div>
                    
                    <!-- 创建小组Tab -->
                    <div id="createSubgroupTab" class="tab-content">
                        <div class="form-group">
                            <label>小组名称:</label>
                            <input type="text" id="subgroupName" placeholder="请输入小组名称">
                        </div>
                        <div class="form-group">
                            <label>邀请成员:</label>
                            <div id="memberSelector"></div>
                        </div>
                        <button onclick="createSubgroup()">创建小组</button>
                    </div>
                    
                    <!-- 邀请处理Tab -->
                    <div id="invitesTab" class="tab-content">
                        <div id="invitesList"></div>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', modal);
    
    // 初始化模态框
    initSubgroupModal();
    loadCurrentSubgroupInfo();
}

/**
 * 创建小组
 */
function createSubgroup() {
    const name = document.getElementById('subgroupName').value;
    const selectedMembers = getSelectedMembers();
    
    if (!name) {
        layer.msg('请输入小组名称');
        return;
    }
    
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
        fetch('/api/msg/chat/subgroup/create', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                parentGroupId: currentGroupId,
                name: name,
                memberIds: selectedMembers
            })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                layer.msg('小组创建成功');
                closeSubgroupModal();
                loadCurrentSubgroupInfo();
            } else {
                layer.msg(data.message || '创建失败');
            }
        })
        .catch(error => {
            console.error('创建小组失败:', error);
            layer.msg('网络错误，请稍后重试');
        });
    });
}

/**
 * 处理小组邀请
 */
function handleSubgroupInvite(inviteId, action) {
    const url = action === 'accept' ? '/api/msg/chat/subgroup/accept' : '/api/msg/chat/subgroup/reject';
    
    fetch(url, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({inviteId: inviteId})
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            layer.msg(action === 'accept' ? '已接受邀请' : '已拒绝邀请');
            loadInvitesList();
            if (action === 'accept') {
                loadCurrentSubgroupInfo();
            }
        } else {
            layer.msg(data.message || '操作失败');
        }
    })
    .catch(error => {
        console.error('处理邀请失败:', error);
        layer.msg('网络错误，请稍后重试');
    });
}

/**
 * 发送小组消息
 */
function sendSubgroupMessage(msgType, content) {
    if (!subgroupState.currentSubgroup) {
        layer.msg('请先加入小组');
        return;
    }
    
    const message = {
        type: 'subgroup_message',
        subgroupId: subgroupState.currentSubgroup.id,
        msgType: msgType,
        content: content,
        timestamp: Date.now()
    };
    
    // 通过WebSocket发送消息
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify(message));
    } else {
        layer.msg('连接已断开，请刷新页面');
    }
}

/**
 * 监听小组相关WebSocket消息
 */
function listenSubgroupMessages() {
    // 扩展现有的WebSocket消息处理
    const originalOnMessage = ws.onmessage;
    
    ws.onmessage = function(event) {
        const data = JSON.parse(event.data);
        
        // 处理小组消息
        if (data.type === 'subgroup_message') {
            handleSubgroupMessage(data);
            return;
        }
        
        // 处理小组邀请通知
        if (data.type === 'subgroup_invite') {
            handleSubgroupInviteNotification(data);
            return;
        }
        
        // 处理其他消息类型
        if (originalOnMessage) {
            originalOnMessage.call(this, event);
        }
    };
}

/**
 * 处理接收到的小组消息
 */
function handleSubgroupMessage(data) {
    // 只有小组成员才能收到消息
    if (subgroupState.currentSubgroup && 
        subgroupState.currentSubgroup.id === data.subgroupId) {
        
        // 在小组聊天区域显示消息
        const chatArea = document.getElementById('subgroupChatArea');
        if (chatArea) {
            const messageElement = createMessageElement(data);
            chatArea.appendChild(messageElement);
            chatArea.scrollTop = chatArea.scrollHeight;
        }
        
        // 显示小组消息通知
        showSubgroupMessageNotification(data);
    }
}

/**
 * 切换小组聊天模式
 */
function toggleSubgroupMode() {
    subgroupState.isInSubgroupMode = !subgroupState.isInSubgroupMode;
    
    const chatContainer = document.querySelector('.chat-container');
    const subgroupIndicator = document.querySelector('.subgroup-indicator');
    
    if (subgroupState.isInSubgroupMode && subgroupState.currentSubgroup) {
        // 进入小组模式
        chatContainer.classList.add('subgroup-mode');
        subgroupIndicator.textContent = `小组模式: ${subgroupState.currentSubgroup.name}`;
        subgroupIndicator.style.display = 'block';
    } else {
        // 退出小组模式
        chatContainer.classList.remove('subgroup-mode');
        subgroupIndicator.style.display = 'none';
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
        fetch('/api/msg/chat/subgroup/leave', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                subgroupId: subgroupState.currentSubgroup.id
            })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                layer.msg('已退出小组');
                subgroupState.currentSubgroup = null;
                subgroupState.isInSubgroupMode = false;
                loadCurrentSubgroupInfo();
            } else {
                layer.msg(data.message || '退出失败');
            }
        })
        .catch(error => {
            console.error('退出小组失败:', error);
            layer.msg('网络错误，请稍后重试');
        });
    });
}

// 页面加载完成后初始化小组功能
document.addEventListener('DOMContentLoaded', function() {
    initSubgroupFeatures();
}); 