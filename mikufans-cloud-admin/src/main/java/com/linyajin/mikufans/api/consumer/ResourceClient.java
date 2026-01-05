package com.linyajin.mikufans.api.consumer;

import com.alibaba.nacos.common.http.param.MediaType;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "mikufans-cloud-resource")
public interface ResourceClient {

    @PostMapping(value = "/innerApi/admin/file/uploadImage",consumes = MediaType.MULTIPART_FORM_DATA)
    public String uploadImage(@RequestPart MultipartFile file, @RequestParam("createThumbnail")Boolean createThumbnail);

    @GetMapping("/innerApi/admin/file/getResource")
    public Response getResource(@RequestParam("sourceName") String sourceName);

    @GetMapping("/innerApi/admin/file/getVideoResource/{fileId}")
    public Response getVideoResource(@PathVariable String fileId);

    @GetMapping("/innerApi/admin/file/getVideoResourceTs/{fileId}/{ts}")
    public Response getVideoResourceTs(@PathVariable String fileId , @PathVariable String ts);





}
