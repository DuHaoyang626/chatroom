-- ===================================================
-- 小组聊天功能数据库表初始化脚本
-- 作者: 吴安然
-- 创建日期: 2024-12-22
-- 说明: 执行此脚本来创建小组聊天功能所需的所有数据库表
-- ===================================================

-- 使用数据库（请根据实际情况修改数据库名）
-- USE your_database_name;

-- 1. 群内小组表
DROP TABLE IF EXISTS chat_subgroup_msg;
DROP TABLE IF EXISTS chat_subgroup_invite;
DROP TABLE IF EXISTS chat_subgroup_member;
DROP TABLE IF EXISTS chat_subgroup;

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
    INDEX idx_creator_id (creator_id)
) COMMENT='群内小组表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 小组成员表
CREATE TABLE chat_subgroup_member (
    id INT AUTO_INCREMENT PRIMARY KEY,
    subgroup_id INT NOT NULL COMMENT '小组ID',
    user_id INT NOT NULL COMMENT '用户ID',
    parent_group_id INT NOT NULL COMMENT '父群组ID',
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(1:正常,0:已退出)',
    UNIQUE KEY uk_subgroup_user (subgroup_id, user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_subgroup_id (subgroup_id),
    INDEX idx_parent_group_id (parent_group_id),
    INDEX idx_user_parent_active (user_id, parent_group_id, status)
) COMMENT='小组成员表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 小组邀请表
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
    INDEX idx_parent_group_id (parent_group_id)
) COMMENT='小组邀请表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 小组消息表
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
    INDEX idx_send_user_id (send_user_id)
) COMMENT='小组消息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 验证表创建是否成功
SELECT 'chat_subgroup' as table_name, COUNT(*) as exists_count FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_subgroup'
UNION ALL
SELECT 'chat_subgroup_member' as table_name, COUNT(*) as exists_count FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_subgroup_member'
UNION ALL
SELECT 'chat_subgroup_invite' as table_name, COUNT(*) as exists_count FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_subgroup_invite'
UNION ALL
SELECT 'chat_subgroup_msg' as table_name, COUNT(*) as exists_count FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_subgroup_msg';

-- 表创建完成提示
SELECT '小组聊天功能数据库表创建完成！' AS message; 