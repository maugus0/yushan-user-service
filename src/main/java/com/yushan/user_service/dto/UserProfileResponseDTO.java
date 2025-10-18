package com.yushan.user_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yushan.user_service.enums.Gender;
import com.yushan.user_service.enums.UserStatus;
import lombok.Data;

import java.util.Date;

/**
 * DTO for exposing user profile via API (read model)
 */
@Data
public class UserProfileResponseDTO {
    private String uuid;
    private String email;
    private String username;
    private String avatarUrl;
    private String profileDetail;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date birthday;

    private Gender gender;
    private Boolean isAuthor;
    private Boolean isAdmin;
    private Integer level;
    private Float exp;
    private Float readTime;
    private Integer readBookNum;

    private UserStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private Date createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private Date updateTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private Date lastActive;

    public Date getBirthday() {
        return birthday != null ? new Date(birthday.getTime()) : null;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday != null ? new Date(birthday.getTime()) : null;
    }

    public Date getCreateTime() {
        return createTime != null ? new Date(createTime.getTime()) : null;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime != null ? new Date(createTime.getTime()) : null;
    }

    public Date getUpdateTime() {
        return updateTime != null ? new Date(updateTime.getTime()) : null;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime != null ? new Date(updateTime.getTime()) : null;
    }

    public Date getLastActive() {
        return lastActive != null ? new Date(lastActive.getTime()) : null;
    }

    public void setLastActive(Date lastActive) {
        this.lastActive = lastActive != null ? new Date(lastActive.getTime()) : null;
    }
}


