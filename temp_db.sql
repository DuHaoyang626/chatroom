create table chat_friend
(
    id          int auto_increment
        primary key,
    user_id     int                                  not null comment '用户ID',
    friend_id   int                                  not null comment '好友ID',
    remark      varchar(128)                         null comment '好友备注',
    is_top      tinyint(1) default 0                 null comment '是否置顶',
    label       varchar(256)                         null comment '标签(多个标签用英文逗号分割)',
    initial     varchar(1) default '#'               not null comment '昵称或备注首字母',
    source      varchar(64)                          null comment '来源',
    create_time datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint user_id_friend_id_uq
        unique (user_id, friend_id)
)
    comment '聊天室好友表';

create index is_top_initial_idx
    on chat_friend (is_top desc, initial asc);

create index user_id_idx
    on chat_friend (user_id);

create table chat_friend_apply
(
    id            int auto_increment
        primary key,
    apply_user_id int                                not null comment '申请用户ID',
    friend_id     int                                not null comment '好友ID',
    status        tinyint                            null comment '申请状态(0:申请中;1:同意;)',
    source        varchar(64)                        null comment '来源',
    apply_reason  varchar(128)                       null comment '申请理由',
    apply_reply   varchar(128)                       null comment '申请回复',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint apply_user_id_friend_id_idx
        unique (apply_user_id, friend_id)
)
    comment '聊天室新好友申请表';

create index apply_user_id_idx
    on chat_friend_apply (apply_user_id);

create index status_idx
    on chat_friend_apply (status);

create table chat_friend_apply_user_rel
(
    id           int auto_increment
        primary key,
    apply_id     int                                  not null comment '申请ID',
    user_id      int                                  not null comment '用户ID',
    friend_id    int                                  not null comment '好友ID',
    remark       varchar(32)                          null comment '好友备注',
    label        varchar(128)                         null comment '标签',
    unread_count tinyint(1) default 0                 not null comment '未读数',
    create_time  datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '聊天室新好友申请和用户关联表';

create index apply_id_idx
    on chat_friend_apply_user_rel (apply_id);

create index friend_id_idx
    on chat_friend_apply_user_rel (friend_id);

create index unread_count_apply_id_user_id_idx
    on chat_friend_apply_user_rel (apply_id, user_id, unread_count);

create index user_id_is_read_idx
    on chat_friend_apply_user_rel (user_id, unread_count);

create table chat_group
(
    id              int auto_increment
        primary key,
    name            varchar(128)                         null comment '群名称',
    avatar          varchar(256)                         null comment '群头像',
    member_count    int        default 0                 null comment '群成员数',
    invite_cfm      tinyint(1) default 0                 null comment '邀请进群是否需要群主或管理员确认(true:需要,false:不需要)',
    group_leader_id int                                  not null comment '群主用户ID',
    managers        varchar(128)                         null comment '群管理员用户ID,多个用逗号分割,最多3个',
    remark          varchar(256)                         null comment '备注',
    notice          text                                 null comment '群公告',
    is_dissolve     tinyint(1) default 0                 null comment '是否解散',
    dissolve_time   datetime                             null comment '解散时间',
    create_time     datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time     datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '聊天室群组表';

create index group_leader_id_idx
    on chat_group (group_leader_id);
