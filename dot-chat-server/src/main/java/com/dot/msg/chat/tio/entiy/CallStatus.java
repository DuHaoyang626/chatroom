package com.dot.msg.chat.tio.entiy;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通话状态
 *
 * @author: fusihan.
 * @date: Created in 2025/6/18 10:40
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CallStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 接听时间
     */
    private String acceptTime;

    /**
     * 挂断时间
     */
    private String hangupTime;

    /**
     * 通话时长(秒)
     */
    private int duration;

    /**
     * 状态(已接听,已挂断,已取消,已拒绝)
     */
    private String status;
}