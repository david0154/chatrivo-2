/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.events;

/**
 * Created by teamxdevelopers on 05/10/2017.
 */

public class HeadsetStateChanged {
    private int state;

    public HeadsetStateChanged(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
