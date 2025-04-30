package com.linyajin.mikufans.entity.enums;


import lombok.Getter;
import lombok.Setter;

@Getter
public enum UserSexEnum {

    WONMAN(0, "女"),
    MAN(1, "男"),
    SECRECY(2, "保密");

    private Integer type;
    private String desc;

    UserSexEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static UserSexEnum getByType(Integer type) {
        for (UserSexEnum userSex : UserSexEnum.values()) {
            if (userSex.type == type) {
                return userSex;
            }
        }
        return null;
    }

}
