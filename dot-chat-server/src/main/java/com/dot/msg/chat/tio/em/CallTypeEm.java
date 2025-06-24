package com.dot.msg.chat.tio.em;

/**
 * 通话消息类型枚举
 *
 * @author: BUPT Chatroom Teams(Du/Fu/Lu/Wu/Kang).
 * @date: Created in 2025/6/20 10:43
 */
public enum CallTypeEm {
    /**
     * 邀请
     */
    invite("邀请", "邀请你进行通话"),
    /**
     * 接听
     */
    accept("接听", "已接听"),
    /**
     * 挂断
     */
    hangup("挂断", "通话结束"),

    /**
     * 取消
     */
    cancel("取消", "已取消"),
    /**
     * 拒绝
     */
    reject("拒绝", "已拒绝"),
    /**
     * 占线中
     */
    busying("对方正忙", "忙线未接听"), // 忙线未接听
    /**
     * 通话中断
     */
    dropped("通话中断", "通话中断"), // 一个人异常下线通话中断
    offer("邀请信令", "邀请信令"),
    answer("应答信令", "应答信令"),
    candidate1("候选者1", "候选者1"),
    candidate2("候选者2", "候选者2"),
    no_answer("对方无应答", "未应答"), // 无人接听
    ;

    private final String type;
    private final String desc;

    CallTypeEm(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static CallTypeEm getByNme(String name) {
        for (CallTypeEm typeEm : CallTypeEm.values()) {
            if (typeEm.name().equals(name)) {
                return typeEm;
            }
        }
        return null;
    }

    public static String getDescByName(String name) {
        for (CallTypeEm mstType : CallTypeEm.values()) {
            if (mstType.name().equals(name)) {
                return mstType.getDesc();
            }
        }
        return null;
    }
}
