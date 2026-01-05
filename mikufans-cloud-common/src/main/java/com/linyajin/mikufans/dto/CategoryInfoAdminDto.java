package com.linyajin.mikufans.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryInfoAdminDto {
    @JsonProperty("pCategoryId")
    @NotNull
    private Integer pCategoryId;
    private Integer categoryId;
    @NotEmpty
        private String categoryCode;
    @NotEmpty
    private String categoryName;
    private String icon;
    private String background;
}
