package com.linyajin.mikufans.controller;
import com.linyajin.mikufans.config.AppConfig;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.dto.UserInfoDto;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.redis.RedisComponent;
import com.linyajin.mikufans.redis.RedisUtils;
import com.linyajin.mikufans.utils.StringTools;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/account")
public class AccountController extends ABaseController {

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private RedisUtils<Object> redisUtils;


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
//    @PostMapping("/register")
//    public ResponseVO register(@RequestBody @Valid UserInfoDto userInfoDto) {
//
//        try {
//            //如果验证码不存在，抛出异常
//            if (!userInfoDto.getCheckCode().equalsIgnoreCase((String) redisUtils.get(userInfoDto.getCheckCodeKey()))) {
//                throw new BusinessException("验证码错误");
//            }
//            //注册账号信息
//            userInfoService.register(userInfoDto);
//            return getSuccessResponseVO(null);
//        } finally {
//            redisUtils.delete(userInfoDto.getCheckCodeKey());
//        }
//    }

    //登录账号
    @RequestMapping ("/login")
    public ResponseVO login(HttpServletRequest request, HttpServletResponse response,
                            @NotEmpty String account,
                            @NotEmpty String password,
                            @NotEmpty String checkCodeKey,
                            @NotEmpty String checkCode) {

        try {
            //如果验证码不存在，抛出异常
//            if (!loginUserInfoDto.getCheckCode().equalsIgnoreCase((String) redisUtils.get(loginUserInfoDto.getCheckCodeKey()))) {
//                throw new BusinessException("验证码错误");
//            }

            String password2 = StringTools.encodeByMd5(password);
            //验证帐号密码是否正确
                if (!account.equals(appConfig.getAdminAccount()) || !password2.equals(StringTools.encodeByMd5(appConfig.getAdminPassword()))){
                throw new BusinessException("帐号或密码错误");
            }

            HashMap<String, Object> claims = new HashMap<>();
            claims.put("userId", account);
            //保存token信息到redis缓存中，并生成token
            String token = redisComponent.saveTokenInfoAdmin(account, claims);
            saveTokenCookie(response, token);

            return getSuccessResponseVO(token);
        } finally {
            redisUtils.delete(checkCodeKey);
            //获取Cookie中的上一个token并且在redis缓存中清除
            Cookie[] cookies = request.getCookies();
            String token = null;
           if (cookies != null) {
               for (Cookie cookie : cookies) {
                   if (cookie.getName().equals(Constants.TOKEN_ADMIN)) {
                       token = cookie.getValue();
                   }
               }
           }
            //清除上一个token
            if (!StringTools.isEmpty(token)) {
                redisComponent.deleteAdminToken(token);
            }
        }
    }


    //退出登录接口
    @GetMapping("/logout")
    public ResponseVO logout(HttpServletResponse response) {
        //把Cookie和redis中的token都清除掉
        cleanCookie(response);
        return getSuccessResponseVO(null);
    }
}
