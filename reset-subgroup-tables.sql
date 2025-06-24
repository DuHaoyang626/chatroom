-- 重置小组聊天数据和表结构
-- 作者: 吴安然
-- 日期: 2025-06-23

USE dot_chat;

-- 1. 删除所有小组相关数据
SET FOREIGN_KEY_CHECKS = 0;

-- 删除小组消息数据
DROP TABLE IF EXISTS chat_subgroup_msg;

-- 删除小组邀请数据
DROP TABLE IF EXISTS chat_subgroup_invite;

-- 删除小组成员数据
DROP TABLE IF EXISTS chat_subgroup_member;

-- 删除小组数据
DROP TABLE IF EXISTS chat_subgroup;

SET FOREIGN_KEY_CHECKS = 1;

-- 2. 重新创建表结构

-- 群内小组表
CREATE TABLE chat_subgroup (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '小组ID',
    parent_group_id INT NOT NULL COMMENT '父群组ID',
    name VARCHAR(128) NOT NULL COMMENT '小组名称',
    creator_id INT NOT NULL COMMENT '创建者ID',
    member_count INT DEFAULT 0 COMMENT '小组成员数',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否活跃',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_group_id (parent_group_id),
    INDEX idx_creator_id (creator_id),
    FOREIGN KEY (parent_group_id) REFERENCES chat_group(id) ON DELETE CASCADE,
    FOREIGN KEY (creator_id) REFERENCES chat_user(id) ON DELETE CASCADE
) COMMENT='群内小组表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 小组成员表
CREATE TABLE chat_subgroup_member (
    id INT AUTO_INCREMENT PRIMARY KEY,
    subgroup_id INT NOT NULL COMMENT '小组ID',
    user_id INT NOT NULL COMMENT '用户ID',
    parent_group_id INT NOT NULL COMMENT '父群组ID',
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(1:正常,0:已退出)',
    UNIQUE KEY uk_subgroup_user (subgroup_id, user_id),
    UNIQUE KEY uk_user_parent_active (user_id, parent_group_id, status) USING BTREE,
    INDEX idx_user_id (user_id),
    INDEX idx_subgroup_id (subgroup_id),
    INDEX idx_parent_group_id (parent_group_id),
    FOREIGN KEY (subgroup_id) REFERENCES chat_subgroup(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES chat_user(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_group_id) REFERENCES chat_group(id) ON DELETE CASCADE
) COMMENT='小组成员表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 小组邀请表
CREATE TABLE chat_subgroup_invite (
    id INT AUTO_INCREMENT PRIMARY KEY,
    subgroup_id INT NOT NULL COMMENT '小组ID',
    parent_group_id INT NOT NULL COMMENT '父群组ID',
    inviter_id INT NOT NULL COMMENT '邀请人ID',
    invitee_id INT NOT NULL COMMENT '被邀请人ID',
    status TINYINT(1) DEFAULT 0 COMMENT '状态(0:待处理,1:已接受,2:已拒绝)',
    invite_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '邀请时间',
    handle_time DATETIME NULL COMMENT '处理时间',
    INDEX idx_invitee_status (invitee_id, status),
    INDEX idx_subgroup_id (subgroup_id),
    INDEX idx_parent_group_id (parent_group_id),
    FOREIGN KEY (subgroup_id) REFERENCES chat_subgroup(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_group_id) REFERENCES chat_group(id) ON DELETE CASCADE,
    FOREIGN KEY (inviter_id) REFERENCES chat_user(id) ON DELETE CASCADE,
    FOREIGN KEY (invitee_id) REFERENCES chat_user(id) ON DELETE CASCADE
) COMMENT='小组邀请表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 小组消息表
CREATE TABLE chat_subgroup_msg (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    subgroup_id INT NOT NULL COMMENT '小组ID',
    parent_group_id INT NOT NULL COMMENT '父群组ID',
    send_user_id INT NOT NULL COMMENT '发送用户ID',
    msg_type VARCHAR(16) NOT NULL COMMENT '消息类型',
    msg TEXT NOT NULL COMMENT '消息内容',
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    timestamp BIGINT COMMENT '时间戳',
    INDEX idx_subgroup_id (subgroup_id),
    INDEX idx_parent_group_id (parent_group_id),
    INDEX idx_send_time (send_time),
    INDEX idx_send_user_id (send_user_id),
    FOREIGN KEY (subgroup_id) REFERENCES chat_subgroup(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_group_id) REFERENCES chat_group(id) ON DELETE CASCADE,
    FOREIGN KEY (send_user_id) REFERENCES chat_user(id) ON DELETE CASCADE
) COMMENT='小组消息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 验证表结构
SHOW TABLES LIKE 'chat_subgroup%';

-- 检查每个表的结构
DESC chat_subgroup;
DESC chat_subgroup_member;
DESC chat_subgroup_invite;
DESC chat_subgroup_msg;

COMMIT; 