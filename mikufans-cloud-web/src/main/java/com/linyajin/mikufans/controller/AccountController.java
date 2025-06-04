package com.linyajin.mikufans.controller;


import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.dto.LoginUserDto;
import com.linyajin.mikufans.dto.TokenUserInfoDto;
import com.linyajin.mikufans.dto.UserInfoDto;
import com.linyajin.mikufans.entity.po.UserInfo;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.dto.UserCountInfoDto;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.redis.RedisComponent;
import com.linyajin.mikufans.redis.RedisUtils;
import com.linyajin.mikufans.service.UserInfoService;
import com.linyajin.mikufans.utils.StringTools;
//import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/account")
public class AccountController extends ABaseController {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private RedisUtils<Object> redisUtils;

    @Resource
    private UserInfoService userInfoService;


    //获取验证码
    @GetMapping("/checkCode")
    public ResponseVO checkCode() {

        //生成验证码对象，设置宽高
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(140, 42);

        //生成验证码之后计算的结果
        String code = captcha.text();

        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String checkCodeKey = Constants.REDIS_KEY_CHECK_CODE + uuid;
        //往redis存储验证码，有效期2分钟
        redisUtils.setex(checkCodeKey, code, 1000 * 60 * 2);

        //将验证码图片转为base64编码返回前端
        String base64 = captcha.toBase64();

        HashMap<String, String> result = new HashMap<>();
        result.put("checkCode" , base64);
        result.put("checkCodeKey" , checkCodeKey);

        return getSuccessResponseVO(result);
    }

    //注册账号
    @PostMapping("/register")
    public ResponseVO register(@RequestBody @Valid UserInfoDto userInfoDto) {

        try {
            //如果验证码不存在，抛出异常
            if (!userInfoDto.getCheckCode().equalsIgnoreCase((String) redisUtils.get(userInfoDto.getCheckCodeKey()))) {
                throw new BusinessException("验证码错误");
            }
            //注册账号信息
            userInfoService.register(userInfoDto);
            return getSuccessResponseVO(null);
        } finally {
            redisUtils.delete(userInfoDto.getCheckCodeKey());
        }
    }

    //登录账号
    @PostMapping("/login")
    public ResponseVO login(HttpServletRequest request,HttpServletResponse response, @RequestBody @Valid LoginUserDto loginUserInfoDto) {

        try {
            //如果验证码不存在，抛出异常
//            if (!loginUserInfoDto.getCheckCode().equalsIgnoreCase((String) redisUtils.get(loginUserInfoDto.getCheckCodeKey()))) {
//                throw new BusinessException("验证码错误");
//            }

            //拿到用户登录的IP
            String ip = getIpAddr();

            TokenUserInfoDto tokenUserInfoDto = userInfoService.login(loginUserInfoDto, ip);

            saveTokenCookie(response, tokenUserInfoDto.getToken());
            return getSuccessResponseVO(tokenUserInfoDto);
        } finally {
            redisUtils.delete(loginUserInfoDto.getCheckCodeKey());
            //获取Cookie中的上一个token并且在redis缓存中清除
            Cookie[] cookies = request.getCookies();
            String token = null;
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(Constants.TOKEN_WEB)) {
                        token = cookie.getValue();
                    }
                }
            }

            //清除上一个token
            if (!StringTools.isEmpty(token)) {
                redisComponent.deleteToken(token);
            }
        }
    }

    // 自动登录接口：用于检查当前Token状态，并在临近过期时自动续期
    @GetMapping("/autoLogin")
    public ResponseVO autoLogin(HttpServletResponse response) {
        //从当前请求中获取已登录的用户信息（从Token解析或Redis中读取）
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //如果未获取到用户信息（未登录或Token无效），返回null提示前端需要重新登录
        if (tokenUserInfoDto == null) {
            return getSuccessResponseVO(null);
        }

        //检查Token是否临近过期（距离过期时间小于1天）
        if (tokenUserInfoDto.getExpireTime() - System.currentTimeMillis()<Constants.REDIS_KEY_EXPIRES_ONE_DAY) {
           //如果即将过期，重新生成Token（续期逻辑）
            HashMap<String, Object> claims = new HashMap<>();
            claims.put("userId" , tokenUserInfoDto.getUserId());
            claims.put("nickName" , tokenUserInfoDto.getNickName());
            //将新Token和用户信息保存到Redis（更新缓存）
            redisComponent.saveTokenUserInfo(tokenUserInfoDto , claims);
            //将新Token写入Cookie（响应给前端）
            saveTokenCookie(response, tokenUserInfoDto.getToken());
        }
//        //无论是否续期，最终都确保Token写入Cookie（防止边界情况遗漏）
//        saveTokenCookie(response, tokenUserInfoDto.getToken());
        return getSuccessResponseVO(tokenUserInfoDto);
    }

    //退出登录接口
    @GetMapping("/logout")
    public ResponseVO logout(HttpServletResponse response) {
        //把Cookie和redis中的token都清除掉
        cleanCookie(response);
        return getSuccessResponseVO(null);
    }


    /**
     * 获取用户的硬币数 粉丝数 关注数等信息
     * @return ResponseVO
     */
    @GetMapping("/getUserCountInfo")
    public ResponseVO getUserCountInfo() {

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();

        UserCountInfoDto userCountInfoDto = userInfoService.getUserCountInfo(tokenUserInfoDto.getUserId());

        return getSuccessResponseVO(userCountInfoDto);
    }
}
