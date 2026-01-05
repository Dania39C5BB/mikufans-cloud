package com.linyajin.mikufans.api.provider;

import com.linyajin.mikufans.annotation.GlobalInterceptor;
import com.linyajin.mikufans.controller.FileController;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/innerApi/admin/file")
@Validated
public class ResourceApi {

    @Resource
    private FileController fileController;

    @PostMapping("/uploadImage")
    public String uploadImage(@NotNull MultipartFile file, @NotNull Boolean createThumbnail) throws IOException {
        return fileController.uploadImageInner(file, createThumbnail);
    }


    @GetMapping("/getResource")
    public void getResource(HttpServletResponse response, @NotEmpty String sourceName) throws IOException {
        fileController.getResource(response,sourceName);
    }

    @GetMapping("/getVideoResource/{fileId}")
    public void getVideoResource(HttpServletResponse response, @PathVariable @NotEmpty String fileId) {
        fileController.getVideoResource(response,fileId);
    }

    @GetMapping("/getVideoResourceTs/{fileId}/{ts}")
    public void getVideoResourceTs(HttpServletResponse response, @PathVariable @NotEmpty String fileId , @PathVariable @NotEmpty String ts) {
        fileController.getVideoResourseTs(response,fileId,ts);
    }

}
