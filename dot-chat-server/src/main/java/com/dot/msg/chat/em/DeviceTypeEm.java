package com.dot.msg.chat.em;

/**
 * 设备类型枚举
 *
 * @author: BUPT Chatroom Teams(Du/Fu/Lu/Wu/Kang).
 * @date: Created in 2025/6/20 10:43
 */
public enum DeviceTypeEm {

    PC("PC端"),
    MOBILE("移动端"),
    SYS("系统消息");


    private String desc;

    DeviceTypeEm(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static DeviceTypeEm getDeviceType(String name) {
        for (DeviceTypeEm typeEm : DeviceTypeEm.values()) {
            if (typeEm.name().equals(name)) {
                return typeEm;
            }
        }
        return null;
    }

    public static String getDeviceTypeDesc(String type) {
        for (DeviceTypeEm typeEm : DeviceTypeEm.values()) {
            if (typeEm.getDesc().equals(type)) {
                return typeEm.getDesc();
            }
        }
        return null;
    }
}
