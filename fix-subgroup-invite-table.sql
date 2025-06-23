-- 修复小组邀请表结构
-- 作者: 吴安然
-- 日期: 2025-06-23

USE dot_chat;

-- 检查当前表结构
DESC chat_subgroup_invite;

-- 如果缺少inviter_id和invitee_id字段，则添加它们
-- 注意：如果字段已存在，这些语句会报错，但不会影响数据

-- 添加邀请人ID字段
ALTER TABLE chat_subgroup_invite 
ADD COLUMN IF NOT EXISTS inviter_id INT NOT NULL COMMENT '邀请人ID' 
AFTER parent_group_id;

-- 添加被邀请人ID字段  
ALTER TABLE chat_subgroup_invite 
ADD COLUMN IF NOT EXISTS invitee_id INT NOT NULL COMMENT '被邀请人ID' 
AFTER inviter_id;

-- 添加索引以提高查询性能
ALTER TABLE chat_subgroup_invite 
ADD INDEX IF NOT EXISTS idx_invitee_status (invitee_id, status);

-- 添加外键约束（如果不存在）
ALTER TABLE chat_subgroup_invite 
ADD CONSTRAINT IF NOT EXISTS fk_invite_inviter 
FOREIGN KEY (inviter_id) REFERENCES chat_user(id) ON DELETE CASCADE;

ALTER TABLE chat_subgroup_invite 
ADD CONSTRAINT IF NOT EXISTS fk_invite_invitee 
FOREIGN KEY (invitee_id) REFERENCES chat_user(id) ON DELETE CASCADE;

-- 检查修复后的表结构
DESC chat_subgroup_invite;

-- 查看现有的邀请数据
SELECT * FROM chat_subgroup_invite ORDER BY invite_time DESC LIMIT 10;

-- 如果需要清理无效数据（邀请人或被邀请人不存在）
-- DELETE FROM chat_subgroup_invite 
-- WHERE inviter_id NOT IN (SELECT id FROM chat_user) 
--    OR invitee_id NOT IN (SELECT id FROM chat_user);

COMMIT; 