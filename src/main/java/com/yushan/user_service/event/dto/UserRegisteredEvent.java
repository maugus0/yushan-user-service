package com.yushan.user_service.event.dto;

import java.util.Date;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID uuid,
        String username,
        String email,
        Date createTime,
        Date updateTime,
        Date lastLoginTime,
        Date lastActiveTime
) {
    public UserRegisteredEvent {
    if (createTime != null) {
        createTime = new Date(createTime.getTime());
    }
    if (updateTime != null) {
        updateTime = new Date(updateTime.getTime());
    }
    if (lastLoginTime != null) {
        lastLoginTime = new Date(lastLoginTime.getTime());
    }
    if (lastActiveTime != null) {
        lastActiveTime = new Date(lastActiveTime.getTime());
    }
}

    public Date createTime() {
        return createTime != null ? new Date(createTime.getTime()) : null;
    }

    public Date updateTime() {
        return updateTime != null ? new Date(updateTime.getTime()) : null;
    }

    public Date lastLoginTime() {
        return lastLoginTime != null ? new Date(lastLoginTime.getTime()) : null;
    }

    public Date lastActiveTime() {
        return lastActiveTime != null ? new Date(lastActiveTime.getTime()) : null;
    }
}
