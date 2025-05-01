package com.linyajin.mikufans.utils;
import com.linyajin.mikufans.exception.BusinessException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class StringTools {

    public static void checkParam(Object param) {
        try {
            Field[] fields = param.getClass().getDeclaredFields();
            boolean notEmpty = false;
            for (Field field : fields) {
                String methodName = "get" + StringTools.upperCaseFirstLetter(field.getName());
                Method method = param.getClass().getMethod(methodName);
                Object object = method.invoke(param);
                if (object != null && object instanceof String && !StringTools.isEmpty(object.toString())
                        || object != null && !(object instanceof String)) {
                    notEmpty = true;
                    break;
                }
            }
            if (!notEmpty) {
                throw new BusinessException("多参数更新，删除，必须有非空条件");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("校验参数是否为空失败");
        }
    }

    public static String upperCaseFirstLetter(String field) {
        if (isEmpty(field)) {
            return field;
        }
        //如果第二个字母是大写，第一个字母不大写
        if (field.length() > 1 && Character.isUpperCase(field.charAt(1))) {
            return field;
        }
        return field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    public static boolean isEmpty(String str) {
        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }

    //生成随机ID
    public static String getRandomStrUID(Integer count) {
        return RandomStringUtils.random(count, true, true);
    }

    public static String getRandomIntUID(Integer count) {
        return RandomStringUtils.random(count, false, true);
    }

    //MD5加密密码
    public static String encodeByMd5(String password) {
        return password == null ? "" : DigestUtils.md5DigestAsHex(password.getBytes());
    }

    public static boolean pathIsOk(String path) {
        if (StringTools.isEmpty(path)) {
            return true;
        }
        if (path.contains("../") || path.contains("..\\")) {
            return false;
        }
        return true;
    }

    //获取文件后缀
    public static String getFileSuffix(String fileName) {
        if (StringTools.isEmpty(fileName) || !fileName.contains(".")) {
            return null;
        }
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        return suffix;
    }
}
