/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.events;

public class GroupActiveStateChanged {
    private String groupId;
    private boolean isActive;

    public GroupActiveStateChanged(String groupId, boolean isActive) {
        this.groupId = groupId;
        this.isActive = isActive;
    }

    public String getGroupId() {
        return groupId;
    }

    public boolean isActive() {
        return isActive;
    }
}
