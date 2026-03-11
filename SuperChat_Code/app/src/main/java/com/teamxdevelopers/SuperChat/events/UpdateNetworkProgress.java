/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.events;

/**
 * Created by teamxdevelopers on 04/01/2018.
 */

public class UpdateNetworkProgress {
    private String id;
    private int progress;

    public UpdateNetworkProgress(String id, int progress) {
        this.id = id;
        this.progress = progress;
    }

    public String getId() {
        return id;
    }

    public int getProgress() {
        return progress;
    }
}
