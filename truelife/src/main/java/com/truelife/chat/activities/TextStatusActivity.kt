package com.truelife.chat.activities

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
import com.aghajari.emojiview.view.AXEmojiPopupLayout
import com.aghajari.emojiview.view.AXEmojiView
import  com.truelife.R
import com.truelife.chat.model.realms.TextStatus
import com.truelife.chat.utils.IntentUtils
import kotlinx.android.synthetic.main.activity_text_status.*
import kotlinx.android.synthetic.main.typing_layout.*


class TextStatusActivity : AppCompatActivity() {
    private lateinit var fontsNames: Array<String>
    private lateinit var colors: Array<String>
    private lateinit var emojiPopup: AXEmojiPopup

    var currentFontIndex = 0
    var currentBackgroundIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_status)

        initFontsNames()
        setInitialTypeFace()
        initEmojiView()

        colors = resources.getStringArray(R.array.status_bg_colors)

        //set initial background randomly
        val randomColorIndex = colors.indexOf(colors.random())
        currentBackgroundIndex = randomColorIndex
        root.setBackgroundColor(Color.parseColor(colors[currentBackgroundIndex]))


        btn_emoji.setOnClickListener {
            emojiPopup.toggle()
        }

        tv_font.setOnClickListener {
            changeTypeFace()
        }
        btn_background.setOnClickListener {
            changeBackground()
        }

        fab_send.setOnClickListener {
            val textStatus = TextStatus("", et_status.text.toString(), fontsNames[currentFontIndex], colors[currentBackgroundIndex])
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
        emojiPager.editText = et_status
        emojiPager.setSwipeWithFingerEnabled(true)
        emojiPager.setLeftIcon(R.drawable.ic_search)

        //TODO ENABLE IN NEXT VERSION.
        /*
        AXStickerView stickerView = new AXStickerView(this, "stickers", new FireStickerProvider(this));
        emojiPager.addPage(stickerView, R.drawable.ic_sticker);

//        add sticker click listener
        stickerView.setOnStickerActionsListener(new OnStickerActions() {
            @Override
            public void onClick(View view, Sticker sticker, boolean fromRecent) {
                try {
                    String path = ((String) sticker.getData());
                    if (new File(path).exists()) {
                        sendSticker(path);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public boolean onLongClick(View view, Sticker sticker, boolean fromRecent) {
                return false;
            }
        });

         */
        emojiPopup = AXEmojiPopup(emojiPager)
        emojiPopup.setPopupListener(object : PopupListener {
            override fun onDismiss() {
                btn_emoji.setImageResource(R.drawable.ic_insert_emoticon_black)
            }

            override fun onShow() {
                btn_emoji.setImageResource(R.drawable.ic_baseline_keyboard_24)
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
        tv_font.typeface = typeface
        et_status.typeface = typeface
    }

    private fun setInitialTypeFace() {
        if (fontsNames.isEmpty()) return
        val typeface = Typeface.createFromAsset(assets, "fonts/${fontsNames[0]}")
        tv_font.typeface = typeface
        et_status.typeface = typeface
    }

    private fun changeBackground() {
        if (currentBackgroundIndex + 1 > colors.lastIndex) currentBackgroundIndex = 0 else currentBackgroundIndex++
        root.setBackgroundColor(Color.parseColor(colors[currentBackgroundIndex]))
    }

    private fun initFontsNames() {
        fontsNames = assets.list("fonts") as Array<String>
    }


}
