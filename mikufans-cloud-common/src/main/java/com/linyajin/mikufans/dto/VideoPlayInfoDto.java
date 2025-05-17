package com.linyajin.mikufans.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class VideoPlayInfoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String videoId;
    private String userId;
    private Integer fileIndex;

}
