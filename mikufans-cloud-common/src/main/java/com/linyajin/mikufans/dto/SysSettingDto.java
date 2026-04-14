package com.linyajin.mikufans.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统设置DTO
 *
 */

@Data
public class SysSettingDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer registerCoinCount = 10;
    private Integer postVideoCoinCount = 2;
    private Integer videoSize = 500;
    private Integer videoPCount = 10;
    private Integer videoCount = 10;
    private Integer commentCount = 20;
    private Integer danmuCount = 20;
}
