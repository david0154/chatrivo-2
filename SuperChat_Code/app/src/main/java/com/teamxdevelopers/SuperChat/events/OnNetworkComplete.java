/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.events;

/**
 * Created by teamxdevelopers on 06/01/2018.
 */

public class OnNetworkComplete {
    private String id;

    public OnNetworkComplete(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
