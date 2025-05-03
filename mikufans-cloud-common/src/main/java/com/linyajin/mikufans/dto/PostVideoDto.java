package com.linyajin.mikufans.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostVideoDto {

    private String videoId;
    @NotEmpty
    private String videoCover;
    @NotEmpty
    @Size(max = 50)
    private String videoName;
    @JsonProperty("pCategoryId")
    @NotNull
    private Integer pCategoryId;
    Integer categoryId;
    @NotNull
    private Integer postType;
    @NotEmpty
    @Size(max = 200)
    private String tags;
    @Size(max = 230)
    private String introduction;
    @Size(max = 5)
    private String interaction;
    @NotEmpty
    private String uploadFileList;
}
