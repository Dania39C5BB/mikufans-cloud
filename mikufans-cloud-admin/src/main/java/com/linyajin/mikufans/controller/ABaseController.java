package com.linyajin.mikufans.controller;
import com.linyajin.mikufans.config.AppConfig;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.redis.RedisComponent;
import com.linyajin.mikufans.redis.RedisUtils;
import feign.Response;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class ABaseController {

    @Resource
    protected RedisUtils<Object> redisUtils;

    @Resource
    protected RedisComponent redisComponent;

    @Resource
    protected AppConfig appConfig;

    protected static final String STATUC_SUCCESS = "success";

    protected static final String STATUC_ERROR = "error";

    protected <T> ResponseVO getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    protected <T> ResponseVO getBusinessErrorResponseVO(BusinessException e, T t) {
        ResponseVO vo = new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        if (e.getCode() == null) {
            vo.setCode(ResponseCodeEnum.CODE_600.getCode());
        } else {
            vo.setCode(e.getCode());
        }
        vo.setInfo(e.getMessage());
        vo.setData(t);
        return vo;
    }

    protected <T> ResponseVO getServerErrorResponseVO(T t) {
        ResponseVO vo = new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        vo.setData(t);
        return vo;
    }

    protected String getIpAddr() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }


    // 保存token到cookie中
    protected void saveTokenCookie(HttpServletResponse response , String token) {
        Cookie cookie = new Cookie(Constants.TOKEN_ADMIN, token);
        //1天过期时间 readyAdminConfig.getAdminJwtExpiresIn() / 1000
        // -1 代表会话期间是有效的，浏览器关闭就失效(全部浏览器关闭才可以失效)
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    //清除Cookie
    protected void cleanCookie(HttpServletResponse response) {
        //获取请求头信息
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                //清除token和Cookie
                if (Constants.TOKEN_ADMIN.equals(cookie.getName())) {
                    //清除token
                    redisComponent.deleteToken(cookie.getValue());
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    //清除完之后重新设置Cookie ，不然无法清除Cookie
                    response.addCookie(cookie);
                }
            }
        }
    }


    public void convertFileResponseToStream(HttpServletResponse servletResponse , Response response) {
        // 从 Feign Response 中获取文件输入流
        Response.Body body = response.body();
        // 使用 try-with-resources 自动关闭流（防止资源泄漏）

        try (
                InputStream fileInputStream  = body.asInputStream(); // 远程文件的输入流
                OutputStream outputStream = servletResponse.getOutputStream() // HTTP 响应输出流
        ){
            //定义缓冲区（1KB），用于分块读取文件
            byte[] bytes = new byte[1024];
            int len;
            //循环读取远程文件流，并写入 HTTP 响应流
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len); // 写入实际读取的字节数
            }
        } catch (Exception  e) {
            log.error("读取文件失败", e);
        }
    }



}
