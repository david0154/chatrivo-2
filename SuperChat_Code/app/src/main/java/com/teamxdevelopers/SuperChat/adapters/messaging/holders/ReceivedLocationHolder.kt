/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.adapters.messaging.holders

import android.content.Context
import android.view.View
import android.widget.TextView
import com.teamxdevelopers.SuperChat.adapters.messaging.holders.base.BaseReceivedHolder
import com.teamxdevelopers.SuperChat.model.realms.Message
import com.teamxdevelopers.SuperChat.model.realms.User
import com.teamxdevelopers.SuperChat.utils.Util
import com.teamxdevelopers.SuperChat.R
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import de.hdodenhof.circleimageview.CircleImageView

class ReceivedLocationHolder(context: Context, itemView: View) : BaseReceivedHolder(context, itemView)
        , OnMapReadyCallback {

    private var mGoogleMap: GoogleMap? = null
    private var mMapLocation: LatLng? = null
    private val placeName: TextView
    private val placeAddress: TextView
    private val mapView: MapView
    private var userImgToolbarChatAct: CircleImageView =itemView.findViewById(R.id.user_img_chat)

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        MapsInitializer.initialize(context)
        googleMap.uiSettings.isMapToolbarEnabled = false


        // If we have mapView data, update the mapView content.
        if (mMapLocation != null) {
            updateMapContents()
        }


    }

    fun setMapLocation(location: LatLng?) {
        mMapLocation = location

        // If the mapView is ready, update its content.
        if (mGoogleMap != null) {
            updateMapContents()
        }
    }

    override fun bind(message: Message, user: User) {
        super.bind(message, user)
        if (message.location == null){
            return
        }
        if (user.isGroupBool) (
                userImgToolbarChatAct.setVisibility(View.VISIBLE)
                )else{
            if (user.thumbImg != null)
                userImgToolbarChatAct.setVisibility(View.GONE)
        }

        val latlng = message.location.latlng
        placeAddress.text = message.location.address
        if (!Util.isNumeric(message.location.name)) {
            placeName.text = message.location.name
            placeName.visibility = View.VISIBLE
        } else placeName.visibility = View.GONE

        setMapLocation(latlng)




    }

    protected fun updateMapContents() {
        // Since the mapView is re-used, need to remove pre-existing mapView features.
        mGoogleMap?.clear()

        // Update the mapView feature data and camera position.
        mMapLocation?.let { mapLocation ->


            mGoogleMap?.addMarker(MarkerOptions().position(mapLocation))
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(mapLocation, 17f)
            mGoogleMap?.moveCamera(cameraUpdate)
        }
    }

    init {
        mapView = itemView.findViewById(R.id.map_view)
        placeName = itemView.findViewById(R.id.place_name)
        placeAddress = itemView.findViewById(R.id.place_address)
        mapView.onCreate(null)
        mapView.getMapAsync(this)
    }


}