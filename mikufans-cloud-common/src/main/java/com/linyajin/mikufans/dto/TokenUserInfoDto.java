package com.linyajin.mikufans.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TokenUserInfoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String userId;
    private String nickName;
    private String avatar;
    //token过期时间戳，单位：毫秒
    private Long expireTime;

    private String token;

    private Integer fansCount;
    private Integer currentCoinCount;
    private Integer focusCount;

}
