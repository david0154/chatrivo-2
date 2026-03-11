/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat

import io.reactivex.disposables.CompositeDisposable

interface Base {
    val disposables:CompositeDisposable
}