package com.dot.msg.chat.em;

/**
 * 群成员来源枚举
 *
 * @author: BUPT Chatroom Teams(Du/Fu/Lu/Wu/Kang).
 * @date: Created in 2025/6/20 10:43
 */
public enum GroupSourceEm {
    SEARCH("搜索入群"),
    CARD("名片入群"),
    INVITE("邀请入群"),
    CREATE("群聊创建者"),
    QRCODE("二维码入群");


    private String desc;

    GroupSourceEm(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static GroupSourceEm get(String name) {
        for (GroupSourceEm sourceEm : GroupSourceEm.values()) {
            if (sourceEm.name().equals(name)) {
                return sourceEm;
            }
        }
        return null;
    }

    public static String getDesc(String type) {
        for (GroupSourceEm sourceEm : GroupSourceEm.values()) {
            if (sourceEm.name().equals(type)) {
                return sourceEm.getDesc();
            }
        }
        return null;
    }
}
