package com.dot.comm.utils.vo;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Http请求返回对象
 *
 * @author: BUPT Chatroom Teams(Du/Fu/Lu/Wu/Kang).
 * @date: Created in 2025/6/20 13:53
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class HttpResponseVo {
    
    private String httpBody;

    private byte[] bytesBody;
    
    private String method;
    
    private int status;
    
    private String code;
    
    private JSONObject jsonObject;
    
}
