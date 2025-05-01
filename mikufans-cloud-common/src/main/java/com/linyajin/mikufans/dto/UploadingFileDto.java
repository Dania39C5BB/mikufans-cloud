package com.linyajin.mikufans.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 上传文件信息DTO
 * chunkIndex 分块索引(当前分块是第几块)
 * chunks 总分块数
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UploadingFileDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String uploadId;
    private String fileName;
    private Integer chunkIndex;
    private Integer chunks;
    private Long fileSize = 0L;
    private String filePath;

}
