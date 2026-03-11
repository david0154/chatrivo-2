/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aghajari.emojiview.AXEmojiManager
import com.aghajari.emojiview.listener.PopupListener
import com.aghajari.emojiview.search.AXEmojiSearchView
import com.aghajari.emojiview.view.AXEmojiPager
import com.aghajari.emojiview.view.AXEmojiPopup
import com.aghajari.emojiview.view.AXEmojiView
import com.teamxdevelopers.SuperChat.R
import com.teamxdevelopers.SuperChat.databinding.ActivityTextStatusBinding
import com.teamxdevelopers.SuperChat.model.realms.TextStatus
import com.teamxdevelopers.SuperChat.utils.IntentUtils


class TextStatusActivity : AppCompatActivity() {
    private lateinit var fontsNames: Array<String>
    private lateinit var colors: Array<String>
    private lateinit var emojiPopup: AXEmojiPopup

    var currentFontIndex = 0
    var currentBackgroundIndex = 0

    private lateinit var binding: ActivityTextStatusBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initFontsNames()
        setInitialTypeFace()
        initEmojiView()

        colors = resources.getStringArray(R.array.status_bg_colors)

        //set initial background randomly
        val randomColorIndex = colors.indexOf(colors.random())
        currentBackgroundIndex = randomColorIndex
        binding.root.setBackgroundColor(Color.parseColor(colors[currentBackgroundIndex]))


        binding.btnEmoji.setOnClickListener {
            emojiPopup.toggle()
        }

        binding.tvFont.setOnClickListener {
            changeTypeFace()
        }
        binding.btnBackground.setOnClickListener {
            changeBackground()
        }

        binding.fabSend.setOnClickListener {
            val textStatus = TextStatus("", binding.etStatus.text.toString(), fontsNames[currentFontIndex], colors[currentBackgroundIndex])
            val data = Intent().putExtra(IntentUtils.EXTRA_TEXT_STATUS, textStatus)
            setResult(Activity.RESULT_OK, data)
            finish()
        }

    }

    private fun initEmojiView() {
        val emojiPager = AXEmojiPager(this)
        val emojiView = AXEmojiView(this)
        emojiPager.addPage(emojiView, R.drawable.ic_insert_emoticon_white)
        // set target emoji edit text to emojiViewPager
        emojiPager.editText = binding.etStatus
        emojiPager.setSwipeWithFingerEnabled(true)
        emojiPager.setLeftIcon(R.drawable.ic_search)


        emojiPopup = AXEmojiPopup(emojiPager)
        emojiPopup.setPopupListener(object : PopupListener {
            override fun onDismiss() {
                binding.btnEmoji.setImageResource(R.drawable.ic_insert_emoticon_black)
            }

            override fun onShow() {
                binding.btnEmoji.setImageResource(R.drawable.ic_baseline_keyboard_24)
            }

            override fun onKeyboardOpened(height: Int) {}
            override fun onKeyboardClosed() {}
            override fun onViewHeightChanged(height: Int) {}
        })

        // SearchView
        if (AXEmojiManager.isAXEmojiView(emojiPager.getPage(0))) {
            emojiPopup.searchView = AXEmojiSearchView(this, emojiPager.getPage(0))
            emojiPager.setOnFooterItemClicked { view, leftIcon -> if (leftIcon) emojiPopup.showSearchView() }
        }
    }


    private fun changeTypeFace() {
        if (currentFontIndex + 1 > fontsNames.lastIndex) currentFontIndex = 0 else currentFontIndex++
        val typeface = Typeface.createFromAsset(assets, "fonts/${fontsNames[currentFontIndex]}")
        binding.tvFont.typeface = typeface
        binding.etStatus.typeface = typeface
    }

    private fun setInitialTypeFace() {
        if (fontsNames.isEmpty()) return
        val typeface = Typeface.createFromAsset(assets, "fonts/${fontsNames[0]}")
        binding.tvFont.typeface = typeface
        binding.etStatus.typeface = typeface
    }

    private fun changeBackground() {
        if (currentBackgroundIndex + 1 > colors.lastIndex) currentBackgroundIndex = 0 else currentBackgroundIndex++
        binding.root.setBackgroundColor(Color.parseColor(colors[currentBackgroundIndex]))
    }

    private fun initFontsNames() {
        fontsNames = assets.list("fonts") as Array<String>
    }


}