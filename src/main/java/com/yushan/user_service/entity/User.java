package com.yushan.user_service.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID uuid;

    private String email;

    private String username;

    private String hashPassword;

    private String avatarUrl;

    private String profileDetail;

    private Date birthday;

    private Integer gender;

    private Integer status;

    private Boolean isAuthor;

    private Boolean isAdmin;

    private Date createTime;

    private Date updateTime;

    private Date lastLogin;

    private Date lastActive;

    public User(UUID uuid, String email, String username, String hashPassword, String avatarUrl, String profileDetail, Date birthday, Integer gender, Integer status, Boolean isAuthor, Boolean isAdmin, Date createTime, Date updateTime, Date lastLogin, Date lastActive) {
        this.uuid = uuid;
        this.email = email;
        this.username = username;
        this.hashPassword = hashPassword;
        this.avatarUrl = avatarUrl;
        this.profileDetail = profileDetail;
        this.birthday = birthday != null ? new Date(birthday.getTime()) : null;
        this.gender = gender;
        this.status = status;
        this.isAuthor = isAuthor;
        this.isAdmin = isAdmin;
        this.createTime = createTime != null ? new Date(createTime.getTime()) : null;
        this.updateTime = updateTime != null ? new Date(updateTime.getTime()) : null;
        this.lastLogin = lastLogin != null ? new Date(lastLogin.getTime()) : null;
        this.lastActive = lastActive != null ? new Date(lastActive.getTime()) : null;
    }

    public User() {
        super();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public String getHashPassword() {
        return hashPassword;
    }

    public void setHashPassword(String hashPassword) {
        this.hashPassword = hashPassword == null ? null : hashPassword.trim();
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl == null ? null : avatarUrl.trim();
    }

    public String getProfileDetail() {
        return profileDetail;
    }

    public void setProfileDetail(String profileDetail) {
        this.profileDetail = profileDetail == null ? null : profileDetail.trim();
    }

    public Date getBirthday() {
        return birthday != null ? new Date(birthday.getTime()) : null;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday != null ? new Date(birthday.getTime()) : null;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Boolean getIsAuthor() {
        return isAuthor;
    }

    public void setIsAuthor(Boolean isAuthor) {
        this.isAuthor = isAuthor;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
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

    public Date getLastLogin() {
        return lastLogin != null ? new Date(lastLogin.getTime()) : null;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin != null ? new Date(lastLogin.getTime()) : null;
    }

    public Date getLastActive() {
        return lastActive != null ? new Date(lastActive.getTime()) : null;
    }

    public void setLastActive(Date lastActive) {
        this.lastActive = lastActive != null ? new Date(lastActive.getTime()) : null;
    }
}