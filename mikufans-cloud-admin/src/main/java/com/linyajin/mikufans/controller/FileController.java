package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.api.consumer.ResourceClient;
import com.linyajin.mikufans.config.AppConfig;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.dto.TokenUserInfoDto;
import com.linyajin.mikufans.dto.VideoPlayInfoDto;
import com.linyajin.mikufans.entity.enums.DateTimePatternEnum;
import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.entity.po.VideoInfoFile;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.utils.DateUtil;
import com.linyajin.mikufans.utils.FFmpegUtils;
import com.linyajin.mikufans.utils.StringTools;
import feign.Response;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Date;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController extends ABaseController {

    @Resource
    private ResourceClient resourceClient;

    /**
     * 上传图片
     * @param file 文件
     * @param cover 是否生成缩略图
     * @return ResponseVO 响应对象
     */
    @PostMapping("/uploadImage")
    public ResponseVO uploadImage(@NotNull MultipartFile file , @NotNull Boolean cover) {

        return getSuccessResponseVO(resourceClient.uploadImage(file, cover));

    }

    //获取资源
    @GetMapping("/getResource")
    public ResponseVO getResource(HttpServletResponse servletResponse , @NotNull String sourceName) throws IOException {

//        客户端请求 → 调用 getResource（通过 Feign 获取远程文件）。
//        Feign 返回 Response（包含文件流，但尚未发送给客户端）。
//        convertFileResponse2Stream 的作用：
//          从 Response 读取输入流（body.asInputStream()）。
//          写入 HttpServletResponse 的输出流（servletResponse.getOutputStream()）。
//          完成数据流转：远程文件流 → 当前服务 → 客户端

        // Feign 返回 Response 对象，其中包含文件流等信息。
        Response response = resourceClient.getResource(sourceName);
        // 将 Response 对象中的文件流转换为 HTTP 响应，并发送给客户端。
        convertFileResponseToStream(servletResponse, response);

        return getSuccessResponseVO(null);

    }


    //读取文件
    private void readFile(HttpServletResponse response, String filePath) throws IOException {
        File file = new File(appConfig.getProjectFolder() + Constants.FILE_DIR + filePath);
        if (!file.exists()) {
            return;
        }
        //OutputStream out：HTTP 响应的输出流，用于向客户端发送数据。
        //InputStream in：本地文件的输入流，用于读取文件内容。
        try(OutputStream out = response.getOutputStream(); InputStream in = new FileInputStream(file)){
            // 缓冲区（每次读取 1KB）
            byte[] byteData = new byte[1024];
            int len = 0;
            //循环读取文件数据到缓冲区，并写入到输出流中
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush(); // 强制刷新缓冲区，确保数据发送
        } catch (Exception e) {
            log.error("读取文件失败", e);
        }
    }

    /**
     * 根据 fileId 获取视频资源
     * @param servletResponse HttpServletResponse 响应对象
     * @param fileId 视频文件ID
     */
    @GetMapping("/getVideoResource/{fileId}")
    public void getVideoResource(HttpServletResponse servletResponse , @PathVariable @NotEmpty String fileId)   {

        convertFileResponseToStream(servletResponse, resourceClient.getVideoResource(fileId));

    }


    /**
     * 根据 fileId 获取视频资源ts片段
     * @param servletResponse HttpServletResponse 响应对象
     * @param fileId 视频文件ID
     * @param ts 时间戳
     */
    @GetMapping("/getVideoResourceTs/{fileId}/{ts}")
    public void getVideoResourceTs(HttpServletResponse servletResponse , @PathVariable @NotEmpty String fileId , @PathVariable @NotEmpty String ts)   {

        convertFileResponseToStream(servletResponse, resourceClient.getVideoResourceTs(fileId ,ts));

    }


}
