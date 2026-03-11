/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.placespicker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.teamxdevelopers.SuperChat.R
import com.teamxdevelopers.SuperChat.databinding.RowPlaceBinding

class NearbyPlacesAdapter(private val context: Context, private val places: List<Place>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<NearbyPlacesAdapter.NearbyPlacesHolder>() {
    var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): NearbyPlacesHolder {
        val row = LayoutInflater.from(parent.context).inflate(R.layout.row_place, parent, false)
        return NearbyPlacesHolder(row)
    }

    override fun getItemCount(): Int = places.size

    override fun onBindViewHolder(holder: NearbyPlacesHolder, position: Int) {
        holder.bind(places[position])
    }

    inner class NearbyPlacesHolder(itemView: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val binding = RowPlaceBinding.bind(itemView)
        fun bind(place: Place) {
            binding.tvPlaceName.text = place.name
            binding.tvPlaceAddress.text = place.address
            Glide.with(context).load(place.iconUrl).into(binding.iconLocation)

            itemView.setOnClickListener {
                onClickListener?.onClick(it, place)
            }
        }
    }

    interface OnClickListener {
        fun onClick(view: View, place: Place)
    }
}