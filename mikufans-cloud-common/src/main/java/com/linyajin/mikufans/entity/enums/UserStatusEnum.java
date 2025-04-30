package com.linyajin.mikufans.entity.enums;

public enum UserStatusEnum {
    ENABLE(1,"启用"),
    DISABLE(0,"禁用");
    private Integer status;
    private String desc;
    UserStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
    public static UserStatusEnum getStatus(Integer status) {
        for (UserStatusEnum e : UserStatusEnum.values()) {
            if (e.getStatus() == status) {
                return e;
            }
        }
        return null;
    }
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }

}
