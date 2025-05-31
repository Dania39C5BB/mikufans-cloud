package com.linyajin.mikufans.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
public class JsonUtils {
    public static SerializerFeature[] serializerFeatures = new SerializerFeature[] {SerializerFeature.WriteMapNullValue};

    public static String covertObj2Json(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteDateUseDateFormat,
                SerializerFeature.SkipTransientField);
    }

    public static <T> T covertJson2Obj(String json, Class<T> clazz) {
        try {
            return JSONObject.parseObject(json, clazz);
        } catch (Exception e) {
            log.info("covertJson2Obj 异常：{}" , json);
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    public static <T> List<T> covertJsonArray2List(String json, Class<T> clazz) {
        try {
            return JSONArray.parseArray(json, clazz);
        } catch (Exception e) {
            log.info("covertJsonArray2List 异常：{}" , json);
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }
}
