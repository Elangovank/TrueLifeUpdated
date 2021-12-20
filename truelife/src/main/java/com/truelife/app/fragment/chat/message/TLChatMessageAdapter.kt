package com.truelife.app.fragment.chat.message

import android.annotation.SuppressLint
import android.graphics.Outline
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.truelife.R
import com.truelife.app.constants.TLConstant.DATE_FORMAT_FROM_SERVER
import com.truelife.app.constants.TLConstant.HH_MM_AA
import com.truelife.app.model.Chat.Chats
import com.truelife.app.model.User
import com.truelife.app.viewer.TLDocFileViewer
import com.truelife.base.TLChatFragmentManager
import com.truelife.storage.LocalStorageSP.getLoginUser
import com.truelife.util.DateUtil.convertDateFormat
import com.truelife.util.Helper.getDisplayChatGroupMonthName
import com.truelife.util.TLDownloadPdf
import com.truelife.util.Utility
import com.truelife.util.Utility.getPixelsFromDPs
import com.truelife.util.Utility.loadImage
import com.truelife.util.Utility.loadPlaceHolder
import de.hdodenhof.circleimageview.CircleImageView
import org.apache.commons.lang3.StringEscapeUtils
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter
import java.util.*

/**
 * Created by Kathirvel on 26-12-2017.
 */
class TLChatMessageAdapter(
    context: FragmentActivity,
    vehiclesInfos: ArrayList<Chats>,
    var mCallback: Callback
) : BaseAdapter(), ChatAudioDialogFragment.Listener, StickyListHeadersAdapter {
    var list: ArrayList<Chats>
        private set
    private val inflater: LayoutInflater
    private val myContext: FragmentActivity
    private val myDownloadPDF: TLDownloadPdf
    private val myFragmentMananager: TLChatFragmentManager
    private val myUserInfo: User
    private val myImageListonly = ArrayList<String>()
    var myInComingAudioPlayer: MediaPlayer? = null
    var myOutGoingAudioPlayer: MediaPlayer? = null
    var dialogFragment: ChatAudioDialogFragment? = null
    private var myDeleteChatMsglistShow = false
    var selectedIds: SparseBooleanArray
        private set

    fun update(info: ArrayList<Chats>) {
        list.addAll(info)
        notifyDataSetChanged()
    }

    fun updateContent(position: Int, chats: ArrayList<Chats>) {
        list[position] = chats[0]
        notifyDataSetChanged()
    }

    fun updateFirst(info: ArrayList<Chats>) {
        list.addAll(0, info)
        notifyDataSetChanged()
    }

    fun addAll(info: ArrayList<Chats>) {
        list.clear()
        list.addAll(info)
        notifyDataSetChanged()
    }

    fun updateChecked(checked: Boolean) {
        for (i in list.indices) {
            list[i].isSelectedStatus = checked
        }
        notifyDataSetChanged()
    }

    fun getAll(): ArrayList<Chats> {
        return list
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    @SuppressLint("NewApi")
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        try {
            val holder: ViewHolder
            val chatInfo = list[position]

            //  Incoming Messages
            if (chatInfo.messagefrom != myUserInfo.mUserId) {
                when {
                    // Image
                    chatInfo.imageUrl.isNotEmpty() -> {
                        holder = ViewHolder()
                        convertView =
                            inflater.inflate(R.layout.item_incoming_image_message, parent, false)
                        holder.myTime = convertView.findViewById(R.id.messageTime)
                        holder.myImage =
                            convertView.findViewById(R.id.messageText)
                        holder.myStatus = convertView.findViewById(R.id.message_status)
                        holder.myDeleteChk = convertView.findViewById(R.id.delete_text_checkbox)

                        convertView.tag = holder
                        convertView.outlineProvider = object : ViewOutlineProvider() {
                            override fun getOutline(
                                view: View,
                                outline: Outline
                            ) {
                                outline.setRect(50, 50, view.width, view.height)
                            }
                        }

                        holder.myTime!!.text = getTime(chatInfo.createdAt)
                        loadImage(
                            chatInfo.imageUrl[0],
                            holder.myImage!!,
                            R.drawable.new_feed_image_place_holder
                        )
                        val myVideoListOnly =
                            ArrayList<String>()
                        val aMeadiaType =
                            ArrayList<String>()
                        holder.myImage!!.setOnClickListener(object : View.OnClickListener {
                            override fun onClick(v: View) {
                                if (checkInternet()) {
                                    aMeadiaType.clear()
                                    myImageListonly.clear()
                                    var aImgPosition = 0
                                    for (i in list.indices) {
                                        if (!list[i].imageUrl.isEmpty()) {
                                            aMeadiaType.add("image")
                                            myImageListonly.addAll(list[i].imageUrl)
                                        } else if (!list[i].videoUrl.isEmpty()) {
                                            run {
                                                aMeadiaType.add("video")
                                                myImageListonly.addAll(list.get(i).videoThumbUrl)
                                                myVideoListOnly.addAll(list.get(i).videoUrl)
                                            }
                                        }
                                    }
                                    for (i in myImageListonly.indices) {
                                        if ((chatInfo.imageUrl[0] == myImageListonly[i])) {
                                            aImgPosition = i
                                        }
                                    }
                                    val aBundle = Bundle()
                                    val gson = Gson()
                                    val json = gson.toJson(myImageListonly)
                                    val jsonMediaType = gson.toJson(aMeadiaType)
                                    aBundle.putString("feed_images", json)
                                    aBundle.putString("feed_images_Type", jsonMediaType)
                                    aBundle.putInt("Position", aImgPosition)
                                    myFragmentMananager.addContent(
                                        TLScreenFeedPhotoView(),
                                        TLScreenFeedPhotoView.TAG,
                                        aBundle
                                    )
                                }
                            }
                        })
                    }

                    // Video
                    chatInfo.videoUrl.isNotEmpty() -> {
                        holder = ViewHolder()
                        convertView =
                            inflater.inflate(R.layout.item_incoming_video_message, parent, false)
                        holder.myTime = convertView.findViewById(R.id.messageTime)
                        holder.myImage =
                            convertView.findViewById(R.id.messageText)
                        holder.myStatus = convertView.findViewById(R.id.message_status)
                        holder.myPlayIMG = convertView.findViewById(R.id.img_play)
                        holder.myDeleteChk = convertView.findViewById(R.id.delete_text_checkbox)
                        convertView.tag = holder

                        holder.myTime!!.text = getTime(chatInfo.createdAt)

                        if (chatInfo.videoThumbUrl.isNotEmpty())
                            loadImage(
                                chatInfo.videoThumbUrl[0],
                                holder.myImage!!,
                                R.drawable.new_feed_video_place_holder
                            )

                        holder.myImage!!.setOnClickListener {
                            if (checkInternet()) {
                                val aBundle = Bundle()
                                aBundle.putString("Video_url", chatInfo.videoUrl[0])
                                myFragmentMananager.addContent(
                                    TLFeedDetailsVideo(),
                                    TLFeedDetailsVideo.TAG,
                                    aBundle
                                )
                            }
                        }
                    }

                    // Documents
                    chatInfo.documentUrl.isNotEmpty() -> {
                        holder = ViewHolder()
                        convertView =
                            inflater.inflate(R.layout.item_incoming_document_message, parent, false)
                        holder.myChat = convertView.findViewById(R.id.messageText)
                        holder.myTime = convertView.findViewById(R.id.messageTime)
                        holder.myDocumentIMG =
                            convertView.findViewById(R.id.message_document)
                        holder.myDeleteChk = convertView.findViewById(R.id.delete_text_checkbox)
                        convertView.tag = holder

                        holder.myTime!!.text = getTime(chatInfo.createdAt)
                        holder.myDocumentIMG!!.setOnClickListener {
                            try {
                                if (checkInternet()) {
                                    val aBundle = Bundle()
                                    aBundle.putString("FILE_PATH", chatInfo.documentUrl[0])
                                    myFragmentMananager.addContent(
                                        TLDocFileViewer(),
                                        TLDocFileViewer.TAG,
                                        aBundle
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    // Audio
                    chatInfo.audioUrl.isNotEmpty() -> {
                        holder = ViewHolder()
                        convertView =
                            inflater.inflate(R.layout.item_incoming_audio_message, parent, false)
                        holder.myChat = convertView.findViewById(R.id.messageText)
                        holder.myTime = convertView.findViewById(R.id.messageTime)
                        holder.myDeleteChk = convertView.findViewById(R.id.delete_text_checkbox)
                        holder.myIncomingAudioView =
                            convertView.findViewById(R.id.incoming_audio_view)
                        holder.myIncomingAudioPlayBtn =
                            convertView.findViewById(R.id.audio_play_img_btn)
                        holder.myInComingAudioPauseBtn =
                            convertView.findViewById(R.id.audio_play_pause_img_btn)
                        holder.myAudioDurationTXT = convertView.findViewById(R.id.audio_duration)
                        convertView.tag = holder

                        holder.myIncomingAudioView!!.setOnClickListener {
                            if (checkInternet()) {
                                val bundle = Bundle()
                                bundle.putString("Chat_Audio_URL", chatInfo.audioUrl[0])
                                myFragmentMananager.addContent(
                                    TLChatAudio(),
                                    TLChatAudio.TAG,
                                    bundle
                                )
                            }
                        }
                        holder.myInComingAudioPauseBtn!!.setOnClickListener {
                            myInComingAudioPlayer!!.pause()
                            IncomeAudiopause = true
                            holder.myInComingAudioPauseBtn!!.visibility = View.GONE
                            holder.myIncomingAudioPlayBtn!!.visibility = View.VISIBLE
                            holder.myIncomingAudioPlayBtn!!.isEnabled = true
                        }
                        holder.myTime!!.text = getTime(chatInfo.createdAt)
                    }

                    // Text
                    else -> {
                        holder = ViewHolder()
                        convertView =
                            inflater.inflate(R.layout.item_incoming_text_message, parent, false)
                        holder.myChat = convertView.findViewById(R.id.messageText)
                        holder.myTime = convertView.findViewById(R.id.messageTime)
                        holder.myAvadharImage = convertView.findViewById(R.id.messageUserAvatar)
                        holder.myStatus = convertView.findViewById(R.id.message_status)
                        holder.myDeleteChk = convertView.findViewById(R.id.delete_text_checkbox)
                        convertView.tag = holder

                        changeMessageStatus(chatInfo, holder)

                        val fromServerUnicodeDecoded =
                            StringEscapeUtils.unescapeJava(chatInfo.message)
                        holder.myChat!!.text = fromServerUnicodeDecoded
                        if (!chatInfo.profile_image.isEmpty()) loadImage(
                            chatInfo.profile_image,
                            holder.myAvadharImage!!
                        ) else loadPlaceHolder(
                            myContext,
                            chatInfo.profile_image,
                            holder.myAvadharImage!!
                        )
                        if (!chatInfo.createdAt!!.isEmpty()) holder.myTime!!.text =
                            getTime(chatInfo.createdAt)
                    }
                }
            }
            // Outgoing Messages
            else {
                when {
                    // Image
                    chatInfo.imageUrl.isNotEmpty() -> {
                        holder = ViewHolder()
                        convertView =
                            inflater.inflate(R.layout.item_outcoming_image_message, parent, false)
                        holder.myTime = convertView.findViewById(R.id.messageTime)
                        holder.myImage =
                            convertView.findViewById(R.id.messageText)
                        holder.myStatus = convertView.findViewById(R.id.message_status)
                        holder.myDeleteChk = convertView.findViewById(R.id.delete_text_checkbox)

                        convertView.tag = holder
                        holder.myTime!!.text = getTime(chatInfo.createdAt)

                        loadImage(
                            chatInfo.imageUrl[0],
                            holder.myImage!!,
                            R.drawable.new_feed_image_place_holder
                        )

                        val myVideoListOnly =
                            ArrayList<String>()
                        val aMeadiaType =
                            ArrayList<String>()
                        holder.myImage!!.setOnClickListener(object : View.OnClickListener {
                            override fun onClick(v: View) {
                                if (checkInternet()) {
                                    aMeadiaType.clear()
                                    myImageListonly.clear()
                                    var aImgPosition = 0
                                    for (i in list.indices) {
                                        if (list[i].imageUrl.isNotEmpty()) {
                                            aMeadiaType.add("image")
                                            myImageListonly.addAll(list[i].imageUrl)
                                        } else if (!list[i].videoUrl.isEmpty()) {
                                            run {
                                                aMeadiaType.add("video")
                                                myImageListonly.addAll(list.get(i).videoThumbUrl)
                                                myVideoListOnly.addAll(list.get(i).videoUrl)
                                            }
                                        }
                                    }
                                    for (i in myImageListonly.indices) {
                                        if ((chatInfo.imageUrl[0] == myImageListonly[i])) {
                                            aImgPosition = i
                                        }
                                    }
                                    val aBundle = Bundle()
                                    val gson = Gson()
                                    val json = gson.toJson(myImageListonly)
                                    val jsonMediaType = gson.toJson(aMeadiaType)
                                    aBundle.putString("feed_images", json)
                                    aBundle.putString("feed_images_Type", jsonMediaType)
                                    aBundle.putInt("Position", aImgPosition)
                                    myFragmentMananager.addContent(
                                        TLScreenFeedPhotoView(),
                                        TLScreenFeedPhotoView.TAG,
                                        aBundle
                                    )
                                }
                            }
                        })
                    }

                    // Video
                    chatInfo.videoUrl.isNotEmpty() -> {
                        holder = ViewHolder()
                        convertView =
                            inflater.inflate(R.layout.item_outcoming_video_message, parent, false)
                        holder.myImage =
                            convertView.findViewById(R.id.messageText)
                        holder.myTime = convertView.findViewById(R.id.messageTime)
                        holder.myStatus = convertView.findViewById(R.id.message_status)
                        holder.myDeleteChk = convertView.findViewById(R.id.delete_text_checkbox)
                        holder.myPlayIMG = convertView.findViewById(R.id.img_play)
                        convertView.tag = holder

                        holder.myTime!!.text = getTime(chatInfo.createdAt)

                        if (chatInfo.videoThumbUrl.isNotEmpty())
                            loadImage(
                                chatInfo.videoThumbUrl[0],
                                holder.myImage!!,
                                R.drawable.new_feed_video_place_holder
                            )

                        holder.myImage!!.setOnClickListener {
                            if (checkInternet()) {
                                val aBundle = Bundle()
                                aBundle.putString("Video_url", chatInfo.videoUrl[0])
                                myFragmentMananager.addContent(
                                    TLFeedDetailsVideo(),
                                    TLFeedDetailsVideo.TAG,
                                    aBundle
                                )
                            }
                        }
                    }

                    // Documents
                    chatInfo.documentUrl.isNotEmpty() -> {
                        holder = ViewHolder()
                        convertView =
                            inflater.inflate(
                                R.layout.item_outcoming_document_message,
                                parent,
                                false
                            )
                        holder.myChat = convertView.findViewById(R.id.messageText)
                        holder.myTime = convertView.findViewById(R.id.messageTime)
                        //holder.myAvadharImage = convertView.findViewById(R.id.messageUserAvatar);
                        holder.myStatus = convertView.findViewById(R.id.message_status)
                        holder.myDocumentIMG =
                            convertView.findViewById(R.id.message_document)
                        holder.myDeleteChk = convertView.findViewById(R.id.delete_text_checkbox)
                        convertView.tag = holder
                        holder.myChat!!.text = chatInfo.message

                        holder.myTime!!.text = getTime(chatInfo.createdAt)
                        holder.myDocumentIMG!!.setOnClickListener(object : View.OnClickListener {
                            override fun onClick(view: View) {
                                try {
                                    if (checkInternet()) {
                                        val aBundle = Bundle()
                                        aBundle.putString("FILE_PATH", chatInfo.documentUrl[0])
                                        myFragmentMananager.addContent(
                                            TLDocFileViewer(),
                                            TLDocFileViewer.TAG,
                                            aBundle
                                        )
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        })
                    }

                    // Audio
                    chatInfo.audioUrl.isNotEmpty() -> {
                        holder = ViewHolder()
                        convertView =
                            inflater.inflate(R.layout.item_outcoming_audio_message, parent, false)
                        holder.myChat = convertView.findViewById(R.id.messageText)
                        holder.myTime = convertView.findViewById(R.id.messageTime)
                        holder.myStatus = convertView.findViewById(R.id.message_status)
                        holder.myOutGoingAudioPlayBtn =
                            convertView.findViewById(R.id.audio_play_img_btn)
                        holder.myOutGoingAudioPauseBtn =
                            convertView.findViewById(R.id.audio_play_pause_img_btn)
                        holder.myAudioDurationTXT = convertView.findViewById(R.id.audio_duration)
                        holder.myDeleteChk = convertView.findViewById(R.id.delete_text_checkbox)
                        holder.myOutGoingAudioView =
                            convertView.findViewById(R.id.outgoing_audio_view)
                        convertView.tag = holder

                        holder.myOutGoingAudioView!!.setOnClickListener {
                            if (checkInternet()) {
                                val bundle = Bundle()
                                bundle.putString("Chat_Audio_URL", chatInfo.audioUrl[0])
                                myFragmentMananager.addContent(
                                    TLChatAudio(),
                                    TLChatAudio.TAG,
                                    bundle
                                )
                            }
                        }
                        holder.myOutGoingAudioPauseBtn!!.setOnClickListener {
                            myOutGoingAudioPlayer!!.pause()
                            OutGoingAudiopause = true
                            holder.myOutGoingAudioPauseBtn!!.visibility = View.GONE
                            holder.myOutGoingAudioPlayBtn!!.visibility = View.VISIBLE
                            holder.myOutGoingAudioPlayBtn!!.isEnabled = true
                        }
                        holder.myTime!!.text = getTime(chatInfo.createdAt)
                    }

                    // Text
                    else -> {
                        holder = ViewHolder()
                        convertView =
                            inflater.inflate(R.layout.item_outcoming_text_message, parent, false)
                        holder.myChat = convertView.findViewById(R.id.messageText)
                        //   holder.mTxtEmojicon  = (EmojiconTextView) convertView.findViewById(R.id.editEmojicon);
                        holder.myTime = convertView.findViewById(R.id.messageTime)
                        holder.myAvadharImage = convertView.findViewById(R.id.messageUserAvatar)
                        holder.myStatus = convertView.findViewById(R.id.message_status)
                        holder.myDeleteChk = convertView.findViewById(R.id.delete_text_checkbox)
                        holder.myCardView = convertView.findViewById(R.id.item_outcoming_card_view)
                        convertView.tag = holder
                        holder.myCardView!!.cardElevation = getPixelsFromDPs(
                            myContext,
                            6
                        ).toFloat()

                        val fromServerUnicodeDecoded =
                            StringEscapeUtils.unescapeJava(chatInfo.message)
                        holder.myChat!!.text = fromServerUnicodeDecoded
                        // holder.mTxtEmojicon.setText(s  );
                        if (myUserInfo.mProfileImage!!.isNotEmpty()) loadImage(
                            (myUserInfo.mProfileImage),
                            holder.myAvadharImage!!
                        ) else loadPlaceHolder(
                            myContext,
                            (myUserInfo.mProfileImage),
                            holder.myAvadharImage!!
                        )
                        if (!chatInfo.createdAt!!.isEmpty()) holder.myTime!!.text =
                            getTime(chatInfo.createdAt)
                    }
                }

                changeMessageStatus(chatInfo, holder)
            }

            initCheckBox(
                position,
                holder.myDeleteChk!!,
                convertView.findViewById(R.id.main_layout)
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }

    private fun checkInternet(): Boolean {
        return Utility.isInternetAvailable(myContext, false)
    }

    private fun getTime(createdAt: String?): String {
        return convertDateFormat(
            (createdAt)!!,
            DATE_FORMAT_FROM_SERVER,
            HH_MM_AA
        )
    }

    fun remove(i: Int) {
        list.removeAt(i)
        notifyDataSetChanged()
    }

    fun toggleSelection(position: Int) {
        selectView(position, !selectedIds[position])
    }

    fun removeSelection() {
        selectedIds = SparseBooleanArray()
        notifyDataSetChanged()
    }

    fun selectView(position: Int, value: Boolean) {
        if (value) selectedIds.put(position, value) else selectedIds.delete(position)
        notifyDataSetChanged()
    }

    override fun returnData() {
        dialogFragment = null
    }

    @SuppressLint("NewApi")
    fun changeMessageStatus(
        chatInfo: Chats,
        holder: ViewHolder
    ) {

        holder.myStatus!!.text = ""

        if (chatInfo.sent == "1") {
            holder.myStatus!!.text = "S"
            holder.myStatus!!.backgroundTintList =
                myContext.resources.getColorStateList(R.color.color_alpha)
        }

        if (chatInfo.delivered == "1") {
            holder.myStatus!!.text = "D"
            holder.myStatus!!.backgroundTintList =
                myContext.resources.getColorStateList(R.color.orange)
        }

        if (chatInfo.read == "1") {
            holder.myStatus!!.text = "R"
            holder.myStatus!!.backgroundTintList =
                myContext.resources.getColorStateList(R.color.green)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun getHeaderView(
        position: Int,
        aConvertView: View?,
        parent: ViewGroup
    ): View {
        var aConvertView = aConvertView
        var aHolder: HeaderViewHolder? =
            null
        if (aConvertView == null) {
            aHolder = HeaderViewHolder()
            aConvertView = inflater.inflate(R.layout.item_chat_date_header_view, parent, false)
            aHolder.myDateHeaderLeft =
                aConvertView.findViewById(R.id.date_header_left_side)
            aHolder.myDateHeaderRight =
                aConvertView.findViewById(R.id.date_header_right_side)
            aHolder.myLeftLAY = aConvertView.findViewById(R.id.date_header_left_LAY)
            aHolder.myRightLAY = aConvertView.findViewById(R.id.date_header_right_LAY)
            aConvertView.tag = aHolder
        } else {
            aHolder =
                aConvertView.tag as HeaderViewHolder
        }
        val chatInfo = list[position]
        val aSpilt =
            list[position].createdAt!!.split(" ").toTypedArray()
        //String[] aSpilt1=myChatMessagesArray.get(position+1).getCreatedAt().split(" ");
        if (position != 0) {
            val aSpilt1 =
                list[position - 1].createdAt!!.split(" ").toTypedArray()
            if ((aSpilt[0] == aSpilt1[0])) {
                aHolder.myLeftLAY!!.visibility = View.GONE
                aHolder.myRightLAY!!.visibility = View.GONE
            } else {
                if (chatInfo.chatDateheader % 2 == 0) {
                    sectionPosition--
                    aHolder.myLeftLAY!!.visibility = View.GONE
                    aHolder.myRightLAY!!.visibility = View.VISIBLE
                    aHolder.myDateHeaderRight!!.text = "" + getDisplayChatGroupMonthName(
                        aSpilt.get(0).trim { it <= ' ' }
                    )
                } else {
                    sectionPosition = 1
                    aHolder.myLeftLAY!!.visibility = View.VISIBLE
                    aHolder.myRightLAY!!.visibility = View.GONE
                    aHolder.myDateHeaderLeft!!.text = "" + getDisplayChatGroupMonthName(
                        aSpilt.get(0).trim { it <= ' ' }
                    )
                }
            }
        } else {
            aHolder.myLeftLAY!!.visibility = View.VISIBLE
            aHolder.myRightLAY!!.visibility = View.GONE
            aHolder.myDateHeaderLeft!!.text = "" + getDisplayChatGroupMonthName(
                aSpilt.get(0).trim { it <= ' ' }
            )
        }
        return aConvertView!!
    }

    private val headerPosition = 0
    private var sectionPosition = 1
    private val aheaderName = ""
    override fun getHeaderId(position: Int): Long {
        return list[position].chatDateheader.toLong()
    }

    internal inner class HeaderViewHolder {
        var myDateHeaderLeft: TextView? = null
        var myDateHeaderRight: TextView? = null
        var myLeftLAY: LinearLayout? = null
        var myRightLAY: LinearLayout? = null
    }

    inner class ViewHolder {
        var myChat: TextView? = null
        var myTime: TextView? = null
        var myStatus: TextView? = null
        var myAudioDurationTXT: TextView? = null
        var myImage: ImageView? = null
        var myPlayIMG: ImageView? = null
        var myAvadharImage: CircleImageView? = null
        var myDocumentIMG: ImageView? = null
        var myIncomingAudioPlayBtn: ImageView? = null
        var myInComingAudioPauseBtn: ImageView? = null
        var myOutGoingAudioPlayBtn: ImageView? = null
        var myOutGoingAudioPauseBtn: ImageView? = null
        var myDeleteChk: CheckBox? = null
        var myIncomingPlayerSeekbar: SeekBar? = null
        var myOutGoingPlayerSeekbar: SeekBar? = null
        var myIncomingAudioView: LinearLayout? = null
        var myOutGoingAudioView: LinearLayout? = null
        var myCardView: CardView? = null
        var mMainLayout: LinearLayout? = null
    }

    private val InComingstartTime = 0.0
    private val OutGoingstartTime = 0.0
    private val InComingfinalTime = 0.0
    private val OutGoingfinalTime = 0.0
    private val myInComingAudioHandler = Handler()
    private val myOutGoingAudioHandler = Handler()
    private var IncomeAudiopause = false
    private var OutGoingAudiopause = false

    companion object {
        private val TAG = TLChatMessageAdapter::class.java.simpleName
        var myInComingoneTimeOnly = 0
        var myOutGoingoneTimeOnly = 0
    }

    init {
        myFragmentMananager = TLChatFragmentManager(context)
        inflater = LayoutInflater.from(context)
        list = vehiclesInfos
        myContext = context
        selectedIds = SparseBooleanArray()
        myDownloadPDF = TLDownloadPdf(myContext)
        myUserInfo = getLoginUser(myContext)
    }

    fun enableDelete(enable: Boolean) {
        myDeleteChatMsglistShow = enable
        notifyDataSetChanged()
    }

    interface Callback {
        fun delete(id: Int)
    }

    private fun initCheckBox(position: Int, checkBox: CheckBox, mainLayout: LinearLayout) {
        if (myDeleteChatMsglistShow)
            checkBox.visibility = View.VISIBLE
        else
            checkBox.visibility = View.GONE

        checkBox.isChecked = list[position].isSelectedStatus

        checkBox.setOnClickListener {
            list[position].isSelectedStatus = checkBox.isChecked
            notifyDataSetChanged()
            mCallback.delete(position)
        }

        mainLayout.setOnClickListener {
            if (myDeleteChatMsglistShow) {
                list[position].isSelectedStatus = !checkBox.isChecked
                notifyDataSetChanged()
                mCallback.delete(position)
            }
        }
    }
}