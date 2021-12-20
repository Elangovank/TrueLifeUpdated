package com.truelife.app.fragment.chat.message

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AbsListView.MultiChoiceModeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.github.curioustechizen.ago.RelativeTimeTextView
import com.google.gson.Gson
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.activity.TLChatActivity
import com.truelife.app.constants.TLConstant
import com.truelife.app.constants.TLConstant.DATE_FORMAT_FROM_SERVER
import com.truelife.app.constants.TLConstant.DATE_FORMAT_FROM_SERVER_1
import com.truelife.app.constants.TLConstant.DATE_FORMAT_FROM_SERVER_CURRENT_DAY
import com.truelife.app.fragment.chat.chatlist.TLChatListFragment
import com.truelife.app.model.Chat
import com.truelife.app.model.Chat.Chats
import com.truelife.app.model.User
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager
import com.truelife.http.MyHttpEntity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.AppDataBase
import com.truelife.storage.DatabaseClient
import com.truelife.storage.LocalStorageSP.getLoginUser
import com.truelife.util.*
import com.vincent.filepicker.Constant
import com.vincent.filepicker.activity.AudioPickActivity
import com.vincent.filepicker.activity.AudioPickActivity.IS_NEED_RECORDER
import de.hdodenhof.circleimageview.CircleImageView
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst.KEY_SELECTED_DOCS
import droidninja.filepicker.FilePickerConst.KEY_SELECTED_MEDIA
import droidninja.filepicker.FilePickerConst.REQUEST_CODE_DOC
import droidninja.filepicker.FilePickerConst.REQUEST_CODE_PHOTO
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONException
import org.json.JSONObject
import se.emilsjolander.stickylistheaders.StickyListHeadersListView
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URI
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Kathirvel on 25-12-2017.
 */
class TLChatFragment : BaseFragment(), ResponseListener, AbsListView.OnScrollListener,
    TLChatMessageAdapter.Callback, View.OnClickListener {
    private var mChatList = ArrayList<Chats>()
    private var mTempData = Chats()
    private val mySwipeRefreshLay: SwipeRefreshLayout? = null
    private var myChatListView: StickyListHeadersListView? = null
    private var mAdapter: TLChatMessageAdapter? = null
    private var myContext: FragmentActivity? = null
    private var myFragmentManager: TLFragmentManager? = null
    private var aToolbar: Toolbar? = null
    private var isLoading = false
    private var myAttachmentIMG: ImageView? = null
    private var mysentIMG: ImageView? = null
    private var mySmilyIMG: ImageView? = null
    private var myChatMenuIMG: ImageView? = null
    private var myMsgEDT: EditText? = null
    var myFirst = 0
    var myVisible = 0
    var myTotal = 0
    var mTotalPage = 0
    var mPage = 1
    var mSelection = 0
    private var isClear = true
    var mWebSocketClient: WebSocketClient? = null
    var mFiles = ArrayList<String>()
    private var aFilter: IntentFilter? = null
    private var myBackIcon: ImageView? = null
    private var myUserIcon: CircleImageView? = null
    var myChatTitle: TextView? = null
    var myChatStatus: RelativeTimeTextView? = null
    var myFilePaths: ArrayList<File>? =
        ArrayList()
    var mLastMsg = ""
    private val myFriendId =
        ArrayList<String?>()
    private val myChatId = ArrayList<String>()
    private var myMsgSent = false
    private val REQUEST_CODE_IMAGE_PICKER = 101
    private var myUserInfo: User? = null
    private var myChatBottomLAY: LinearLayout? = null
    private var myBottomCancel: TextView? = null
    private var myBottomDelete: TextView? = null
    private var myListener: ClickListener? =
        null
    private var myRootLayout: RelativeLayout? = null
    private var mSendLayout: RelativeLayout? = null
    private var myProgressBar: ProgressBar? = null
    private var mySelectPosition: String? = ""
    private val mySelectedCount: TextView? = null
    private var myTopLoadingBarLay: RelativeLayout? = null
    private lateinit var mChat: Chat.ChatList
    private var mPermission: Array<String> = arrayOf()
    var mMenuList: Array<String> = arrayOf()
    var mBlockStatus = ""
    var isUserBlocked = false
    var mDeletedIds = ""
    var mChatDAO: AppDataBase? = null


    lateinit var httpPost: HttpPost
    lateinit var httpClient: HttpClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        val rootView: View =
            inflater.inflate(R.layout.fragment_chat_message, container, false)

        init(rootView)

        return rootView
    }

    override fun init(aView: View) {
        myContext = activity
        myFragmentManager = TLFragmentManager(myContext!!)
        aFilter = IntentFilter()
        aFilter!!.addAction("MESSAGE")
        myChatListView = aView.findViewById(R.id.fragment_chat_LIST)
        myUserInfo = getLoginUser(myContext)
        aToolbar = aView.findViewById(R.id.common_toolbar)
        myAttachmentIMG = aView.findViewById(R.id.attachment)
        mysentIMG = aView.findViewById(R.id.send)
        mySmilyIMG = aView.findViewById(R.id.smiley)
        myMsgEDT = aView.findViewById(R.id.message)
        myBackIcon = aView.findViewById(R.id.back_icon)
        myUserIcon = aView.findViewById(R.id.chat_userIcon)
        myChatTitle = aView.findViewById(R.id.chat_title)
        myTopLoadingBarLay = aView.findViewById(R.id.screen_LAY_loading)
        myTopLoadingBarLay!!.visibility = View.GONE
        myProgressBar = aView.findViewById(R.id.chat_progress)
        myChatStatus = aView.findViewById(R.id.chat_status)
        myRootLayout = aView.findViewById(R.id.fragment_chat_root_LAY)
        myContext!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        myChatMenuIMG = aView.findViewById(R.id.fragment_chat_IMG_menu)
        myChatBottomLAY = aView.findViewById(R.id.fragment_messenger_bottom_lay)
        myBottomCancel = aView.findViewById(R.id.bottom_layout_cancel)
        myBottomDelete = aView.findViewById(R.id.bottom_layout_delete)
        mSendLayout = aView.findViewById(R.id.chat_text_LAY)

        mMenuList = arrayOf("Info", "Block", "Delete", "Clear Chat")

        mChatDAO = DatabaseClient(myContext!!).getAppDatabase()

        initBundle()

        mPermission = arrayOf(
            TLConstant.CAMERA,
            TLConstant.READ_EXTERNAL_STORAGE,
            TLConstant.WRITE_EXTERNAL_STORAGE
        )
        PermissionChecker().askAllPermissions(myContext!!, mPermission)

        (myContext as TLChatActivity).showBottomBar(false)

        mysentIMG!!.setOnClickListener(this)

        aView.findViewById<LinearLayout>(R.id.common_header_layout).setOnClickListener {
            Helper.navigateChatProfile(activity!!, mChat.user_id)
        }

        myUserIcon!!.setOnClickListener {
            Helper.navigateChatProfile(activity!!, mChat.user_id)
        }
    }

    private fun initListView() {
        mAdapter = TLChatMessageAdapter(myContext!!, loadType(mChatList), this)
        myChatListView!!.adapter = mAdapter
        myChatListView!!.setOnScrollListener(this)
    }

    private fun updateStatus(aBlock: String?) {
        if (aBlock != null) {
            if (aBlock == "1") {
                myChatStatus!!.visibility = View.INVISIBLE
            } else {
                myChatStatus!!.visibility = View.VISIBLE
            }
        }
    }

    private fun offlineOnlinestatus(online: String, lastSeen: String) {
        try {
            if (online == "1") {
                myChatStatus!!.text = getString(R.string.label_online)
                myChatStatus!!.setTextColor(myContext!!.resources.getColor(R.color.dark_grey))
            } else {
                try {
                    loadData(lastSeen)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity?)!!.setSupportActionBar(aToolbar)
    }

    @Throws(ParseException::class)
    fun loadData(aDate: String?) {
        var aFormat =
            SimpleDateFormat(DATE_FORMAT_FROM_SERVER)
        val timestamp = aFormat.parse(aDate)
        val now = Calendar.getInstance()
        val servierTime = Calendar.getInstance()
        servierTime.time = timestamp
        aFormat = if (now[Calendar.DATE] == servierTime[Calendar.DATE]) {
            SimpleDateFormat(DATE_FORMAT_FROM_SERVER_CURRENT_DAY)
        } else {
            SimpleDateFormat(DATE_FORMAT_FROM_SERVER_1)
        }
        val output = aFormat.format(timestamp)
        try { // Date aReffereceDate = format.parse(aDateSTR);
// myChatStatus.setReferenceTime(Long.parseLong("Last Seen "+timestamp.getTime()));
            if (now[Calendar.DATE] == servierTime[Calendar.DATE]) {
                myChatStatus!!.text = "Last seen today$output"
                myChatStatus!!.setTextColor(myContext!!.resources.getColor(R.color.dark_grey))
            } else {
                myChatStatus!!.text = "Last seen $output"
                myChatStatus!!.setTextColor(myContext!!.resources.getColor(R.color.dark_grey))
            }
            //holder.myTimeTXT.setReferenceTime(timestamp.getTime());
//holder.myTimeTXT.setReferenceTime(timestamp.getTime());
        } catch (e: Exception) { // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    private val mActionMode: ActionMode? = null
    private fun setTopLoadingBarStatus(aStatus: Boolean) {
        try {
            if (aStatus) myTopLoadingBarLay!!.visibility =
                View.VISIBLE else myTopLoadingBarLay!!.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var updateview: View? = null
    private fun addListeners() {
        myChatListView!!.setMultiChoiceModeListener(object : MultiChoiceModeListener {
            override fun onItemCheckedStateChanged(
                mode: ActionMode,
                position: Int,
                id: Long,
                checked: Boolean
            ) { // Capture total checked items
                val checkedCount = myChatListView!!.checkedItemCount
                if (1 < checkedCount) {
                    mode.menu.findItem(R.id.copy).isVisible = false
                } else {
                    mode.menu.findItem(R.id.copy).isVisible =
                        mChatList[position].message!!.isNotEmpty()
                }
                // Set the CAB title according to total checked items
                mode.title = "$checkedCount Selected"
                // mySelectedCount.setText(checkedCount+" mySelectedCount");
// Calls toggleSelection method from ListViewAdapter Class
                mAdapter!!.toggleSelection(position)
            }

            override fun onActionItemClicked(
                mode: ActionMode,
                item: MenuItem
            ): Boolean {
                return when (item.itemId) {
                    R.id.delete -> {
                        return true
                    }
                    R.id.copy -> {
                        val sb = StringBuilder()
                        val selectedCopy = mAdapter!!.selectedIds
                        // Captures all selected ids with a loop
                        var i = selectedCopy.size() - 1
                        while (i >= 0) {
                            if (selectedCopy.valueAt(i)) {
                                val mAdapterItem =
                                    mAdapter!!.getItem(selectedCopy.keyAt(i)) as Chats
                                // WorldPopulation selecteditem = listviewadapter.getItem(selected.keyAt(i));
// Remove selected items following the ids
                                sb.append("" + mAdapterItem.message)
                            }
                            i--
                        }
                        val clipboard =
                            myContext!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip =
                            ClipData.newPlainText("Copied Text", sb)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(myContext, "Message Copied", Toast.LENGTH_SHORT).show()
                        mode.finish()
                        true
                    }
                    else -> false
                }
            }

            override fun onCreateActionMode(
                mode: ActionMode,
                menu: Menu
            ): Boolean { /* LayoutInflater mInflater = LayoutInflater.from(myContext);
                View mCustomView = mInflater.inflate(R.layout.chat_select_item_delete, null);
                mySelectedCount=mCustomView.findViewById(R.id.selected_count);
                mode.setCustomView(mCustomView);*/
                mode.menuInflater.inflate(R.menu.chat_delete_menu, menu)
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode) { // TODO Auto-generated method stub
                mAdapter!!.removeSelection()
            }

            override fun onPrepareActionMode(
                mode: ActionMode,
                menu: Menu
            ): Boolean { // TODO Auto-generated method stub
                return false
            }
        })
        myChatListView!!.setOnItemLongClickListener { parent, view, position, id -> false }
        myRootLayout!!.setOnTouchListener { v, event ->
            val imm =
                myContext!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(myContext!!.currentFocus!!.windowToken, 0)
            true
        }
        myChatListView!!.setOnItemClickListener { parent, view, position, id ->
        }
        myBottomCancel!!.setOnClickListener {
            DeleteBottomView(false)
            mAdapter!!.updateChecked(false)
        }
        myBottomDelete!!.setOnClickListener {
            val list = mAdapter!!.getAll()
            val id = ArrayList<String>()
            for (i in list.indices) {
                if (list[i].isSelectedStatus)
                    id.add(list[i].messageId!!)
            }

            initDelete(TextUtils.join(",", id))
        }
        myMsgEDT!!.addTextChangedListener(object : TextWatcher {
            var isTyping = false
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                myMsgEDT!!.isCursorVisible = true
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (charSequence.toString().startsWith(" ")) {
                    Toast.makeText(myContext, "Not allow start with space", Toast.LENGTH_SHORT)
                        .show()
                }
                if (charSequence.toString().trim { it <= ' ' } == "") {
                    mysentIMG!!.visibility = View.GONE
                } else {
                    mysentIMG!!.visibility = View.VISIBLE
                }
                if (charSequence.isNotEmpty()) {
                } else {
                    myMsgEDT!!.isCursorVisible = true
                }
            }

            private var timer = Timer()
            private val DELAY: Long = 2000 // milliseconds
            override fun afterTextChanged(editable: Editable) {
                val handler = Handler()
                if (mWebSocketClient!!.connection.isOpen) {
                    if (!isTyping) {
                        Log.d(TAG, "started typing")
                        // Send notification for start typing event
                        isTyping = true
                        mWebSocketClient!!.send(updateTypingStatus("1", "typing"))
                    }
                    timer.cancel()
                    timer = Timer()
                    timer.schedule(
                        object : TimerTask() {
                            override fun run() {
                                isTyping = false
                                Log.d(TAG, "stopped typing")
                                mWebSocketClient!!.send(updateTypingStatus("0", "typing"))
                                //send notification for stopped typing event
                            }
                        },
                        DELAY
                    )
                }
            }
        })
        myBackIcon!!.setOnClickListener {
            myDeleteChatMsglistShow = false
            mAdapter!!.notifyDataSetChanged()
            myFragmentManager!!.onBackPress()
            sentLastMessage()
        }

        myAttachmentIMG!!.setOnClickListener {
            if (checkInternet())
                if (mChat.blocked != null && mChat.blocked == "1") {
                    blockAction("Unblock ${mChat.user_name} to send a message")
                } else {
                    if (PermissionChecker().checkAllPermission(myContext!!, mPermission)) {
                        val array = arrayOf("Camera", "Gallery", "Files")

                        AppDialogs.showBottomMenu(
                            myContext!!,
                            array,
                            object : TLBottomOptionAdapter.Callback {
                                override fun info(option: Int, text: String) {
                                    when (option) {
                                        0 -> {
                                            initImagePicker()
                                        }

                                        1 -> {
                                            initVideoPicker()
                                        }

                                        2 -> {
                                            initDocPicker()
                                        }
                                    }
                                }
                            },
                            "Cancel"
                        )
                    }
                }
        }
        myChatMenuIMG!!.setOnClickListener { view ->


            AppDialogs.showBottomMenu(
                myContext!!,
                mMenuList,
                object : TLBottomOptionAdapter.Callback {
                    override fun info(option: Int, text: String) {
                        when (text) {
                            "Info" -> Helper.navigateChatProfile(activity!!, mChat.user_id)

                            "Block" ->
                                if (checkInternet())
                                    blockAction(getBlockMsg())


                            "Unblock" ->
                                if (checkInternet())
                                    blockAction(getBlockMsg())


                            "Delete" -> {
                                if (checkInternet()) {
                                    if (mAdapter!!.count != 0)
                                        DeleteBottomView(true)
                                    DeleteBottomView(true)
                                }
                            }

                            "Clear Chat" -> {
                                if (checkInternet())
                                    AppDialogs.customDoubleAction(
                                        myContext!!,
                                        null,
                                        "Are you sure you want to clear this chat?",
                                        "Clear", "Cancel",
                                        object : AppDialogs.OptionListener {
                                            override fun yes() {
                                                if (checkInternet()) {
                                                    initClear()
                                                }
                                            }

                                            override fun no() {
                                                AppDialogs.hidecustomView()
                                            }

                                        }, false, isOptionable = false
                                    )
                            }
                        }
                    }
                },
                "Cancel"
            )
        }
    }

    private fun initDelete(ids: String?) {
        if (checkInternet()) {
            mDeletedIds = ids!!
            AppDialogs.showProgressDialog(myContext!!)
            val mCase = getClearParam("2", ids)
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
            AppServices.execute(
                myContext!!, this,
                result,
                Request.Method.POST,
                AppServices.API.deleteChat,
                User::class.java
            )
        }
    }

    private fun initClear() {
        AppDialogs.showProgressDialog(myContext!!)
        val mCase = getClearParam("1", "")
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
        AppServices.execute(
            myContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.clearChat,
            User::class.java
        )
    }

    private fun getClearParam(type: Any, chatId: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("friend_id", mChat.user_id)
            jsonObject.put("chat_ids", chatId)
            jsonObject.put("login_user_id", getLoginUser(myContext).mUserId!!)
            jsonObject.put("remove_type", type)
            val jsonParam = JSONObject()
            jsonParam.put("RemoveChat", jsonObject)
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)

            Log.i("Block -->> ", jsonParam.toString())

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun getBlockMsg(): String {
        return String.format(
            "Do you want to %s your friend",
            if (mChat.blocked == "1") "unblock" else "block"
        )
    }

    private fun blockAction(msg: String) {
        AppDialogs.customDoubleAction(
            myContext!!,
            null,
            msg,
            "Ok", "Cancel",
            object : AppDialogs.OptionListener {
                override fun yes() {
                    if (checkInternet()) {
                        mBlockStatus = if (mChat.blocked == "1") "0" else "1"
                        initBlock(mBlockStatus)
                    }
                }

                override fun no() {
                    AppDialogs.hidecustomView()
                }

            }, false, isOptionable = false
        )
    }

    private fun initBlock(status: String) {
        AppDialogs.showProgressDialog(myContext!!)
        val mCase = getBlockParam(status, mChat.user_id!!)
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
        AppServices.execute(
            myContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.blockFriend,
            User::class.java
        )
    }

    private fun sentLastMessage() {
        if (myMsgSent) {
            val filter = "ChatLastMSGFrgament"
            val intent = Intent(filter)
            intent.putExtra("selected_pos", mySelectPosition)
            intent.putExtra("getMessage", mLastMsg)
            intent.putExtra("getAttachment", myFilePaths)
            intent.putExtra("user_id", mChat.user_id)
            LocalBroadcastManager.getInstance(myContext!!).sendBroadcast(intent)
        }
    }


    override fun initBundle() {
        try {
            val bundle = arguments
            if (bundle != null) {
                mChat = Gson().fromJson(bundle.getString("data"), Chat.ChatList::class.java)
                initData(mChat)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initData(mChat: Chat.ChatList?) {
        try {
            addListeners()
            ProfileImage()
            setupToolbar()
            initListView()
            getChatMessages(mPage == 1, mPage.toString())
            isUserBlocked = mChat!!.user_blocked == "1"
            update(mChat)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun update(mChat: Chat.ChatList) {
        myChatTitle!!.text = mChat.user_name
        offlineOnlinestatus(mChat.online!!, mChat.last_seen!!)
        updateStatus(mChat.blocked)
    }

    private fun ProfileImage() {
        if (mChat.profile_image!!.isNotEmpty())
            Utility.loadImage(mChat.profile_image!!, myUserIcon!!)
        else Utility.loadPlaceHolder(myContext!!, mChat.gender!!, myUserIcon!!)
    }

    private fun getChatMessages(showProgress: Boolean, page: String) {
        if (Utility.isInternetAvailable(myContext, false)) {
            isLoading = true

            AppDialogs.hideSoftKeyboard((myContext as Activity?)!!, myChatListView!!)
            if (showProgress)
                AppDialogs.showProgressDialog(myContext!!)
            else setTopLoadingBarStatus(mPage != 1)

            val mCase = getParam(page, mChat.user_id!!)
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
            AppServices.execute(
                myContext!!, this,
                result,
                Request.Method.POST,
                AppServices.API.chat,
                Chats::class.java
            )
        } else {
            // Load Data from Local Database
            activity!!.runOnUiThread {
                if (mChatDAO != null) {
                    val mDAO = mChatDAO!!.chat()
                    val mList = ArrayList<Chats>()
                    val query = "SELECT * FROM chat ORDER BY id ASC"
                    val aList = mDAO.getAllList(SimpleSQLiteQuery(query))
                    for (i in aList.indices) {
                        if (aList[i].message!!.isEmpty())
                            when (aList[i].type) {
                                "image" -> aList[i].imageUrl.add("image")
                                "video" -> aList[i].videoUrl.add("video")
                                "document" -> aList[i].documentUrl.add("document")
                                "audio" -> aList[i].audioUrl.add("audio")
                            }

                        mList.add(aList[i])
                    }
                    mAdapter!!.addAll(mList)
                }
            }
        }
    }

    private fun getParam(page: String, id: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("page_no", page)
            jsonObject.put("friend_id", id)
            jsonObject.put("login_user_id", getLoginUser(myContext).mUserId!!)
            val jsonParam = JSONObject()
            jsonParam.put("ChatMessage", jsonObject)
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)

            Log.i("Param -->> ", jsonParam.toString())

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun getBlockParam(status: String, id: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("block_status", status)
            jsonObject.put("friend_id", id)
            jsonObject.put("login_user_id", getLoginUser(myContext).mUserId!!)
            val jsonParam = JSONObject()
            jsonParam.put("BlockFriend", jsonObject)
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)

            Log.i("Block -->> ", jsonParam.toString())

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.chat.hashCode()) {
                isLoading = false
                setTopLoadingBarStatus(false)

                if (r.response!!.isSuccess) {
                    val data = r as Chats

                    if (isClear)
                        mChatList.clear()

                    mTotalPage = data.total_page!!.toInt()
                    mChat.blocked = data.is_blocked
                    mChat.online = data.is_online
                    mChat.last_seen = data.last_seen
                    update(mChat)

                    val chat = data.mData
                    chat.reverse()

                    if (isClear) mAdapter!!.addAll(loadType(chat))
                    else {
                        mSelection = loadType(chat).size

                        myChatListView!!.requestFocusFromTouch()
                        myChatListView!!.setSelection(mSelection)
                        myChatListView!!.requestFocus()

                        mAdapter!!.updateFirst(loadType(chat))
                    }

                } else AppDialogs.showToastDialog(myContext!!, r.response!!.responseMessage!!)

                initMenu()
            } else if (r.requestType!! == AppServices.API.blockFriend.hashCode()) {
                if (r.response!!.isSuccess) {
                    mChat.blocked = mBlockStatus
                    AppDialogs.showToastDialog(myContext!!, r.response!!.responseMessage!!)
                } else AppDialogs.showToastDialog(myContext!!, r.response!!.responseMessage!!)
                initMenu()
                updateStatus(mChat.blocked)
            } else if (r.requestType!! == AppServices.API.clearChat.hashCode()) {
                if (r.response!!.isSuccess) {
                    mChatList.clear()
                    deleteTable()
                    mAdapter!!.addAll(mChatList)
                } else AppDialogs.showToastDialog(myContext!!, r.response!!.responseMessage!!)
                initMenu()
            } else if (r.requestType!! == AppServices.API.deleteChat.hashCode()) {
                if (r.response!!.isSuccess) {
                    val ids = mDeletedIds.split(",")
                    val mChatData = mAdapter!!.getAll()
                    for (i in ids.indices) {
                        for (x in mChatData.indices) {
                            if (ids[i].toInt() == mChatData[x].messageId!!.toInt()) {
                                deleteData(mChatData[x])
                                mAdapter!!.remove(x)
                                break
                            }
                        }
                    }
                    DeleteBottomView(false)
                } else AppDialogs.showToastDialog(myContext!!, r.response!!.responseMessage!!)
                initMenu()
            } else if (r.requestType!! == AppServices.API.sendTextMsg.hashCode()) {
                if (!r.response!!.isSuccess)
                    AppDialogs.showToastDialog(myContext!!, r.response!!.responseMessage!!)
                initMenu()
            }
        } else AppDialogs.customOkAction(
            myContext!!,
            null,
            TLConstant.SERVER_NOT_REACH,
            null,
            null,
            false
        )
    }

    private fun disconnectSocket() {
        if (mWebSocketClient != null && mWebSocketClient!!.connection.isOpen) {
            mWebSocketClient!!.connection.close()
            mWebSocketClient!!.close()
        }
    }


    private fun initSocket() {
        val uri = URI(BuildConfig.SOCKET_URL)

        mWebSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake) {
                Log.i("Socket onOpen --> ", handshakedata.toString())
            }

            override fun onMessage(message: String) {

                if (mWebSocketClient == null || !mWebSocketClient!!.connection.isOpen)
                    return

                Log.i("Socket onMessage --> ", message)

                activity!!.runOnUiThread {
                    if (message.isNotEmpty()) {
                        val obj = JSONObject(message)

                        // Update User Status
                        if (obj.has("user_status")) {
                            val array = obj.getJSONArray("user_status")
                            for (x in 0 until array.length()) {
                                val data = array.getJSONObject(x)
                                if (mChat.user_id == data.getString("user_id")) {
                                    offlineOnlinestatus(
                                        data.getInt("is_online").toString()
                                        , data.getString("last_seen")
                                    )
                                }
                            }
                        }

                        // Update User Messages
                        if (obj.has("chat_history") && obj.getJSONArray("chat_history").length() > 0) {
                            val chat = Gson().fromJson(message, Chats::class.java)
                            var isFound = false
                            val list = mAdapter!!.getAll()
                            if (chat.mSocketData.size > 0) {
//                                mChat.blocked = chat.mSocketData[0].is_blocked
                                if (chat.mSocketData[0].is_blocked == "0") {
                                    var position = -1
                                    for (i in list.indices) {
                                        if (list[i].messageId == chat.mSocketData[0].messageId) {
                                            isFound = true
                                            position = i
                                            break
                                        }
                                    }
                                    isClear = false
                                    if (isFound)
                                        mAdapter!!.updateContent(
                                            position,
                                            loadType(chat.mSocketData)
                                        )
                                    else mAdapter!!.update(loadType(chat.mSocketData))

                                    offlineOnlinestatus(
                                        chat.mSocketData[0].is_online!!,
                                        chat.mSocketData[0].last_seen!!
                                    )
                                }
                            }
                            initMenu()
                        }

                        // Typing
                        if (obj.has("isTyping")) {
                            try {
                                val type = obj.getJSONObject("isTyping")
                                if (isOtherUser(type.getString("FromUserid"))
                                    && mChat.blocked == "0" && !isUserBlocked
                                ) {
                                    Handler().postDelayed({
                                        offlineOnlinestatus(
                                            type.getString("status"),
                                            mChat.last_seen!!
                                        )
                                    }, 1000)
                                    myChatStatus!!.text = "Typing..."
                                    myChatStatus!!.setTextColor(
                                        Utility.getColor(
                                            myContext!!,
                                            R.color.typing
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                Log.i("Socket onClose --> ", reason)
            }

            override fun onError(ex: Exception) {
                Log.i("Socket onError --> ", ex.printStackTrace().toString())
            }
        }

        mWebSocketClient!!.connect()

    }

    private fun isOtherUser(id: String): Boolean {
        return getLoginUser(myContext).mUserId != id
    }

    override fun clickListener() {}
    interface ClickListener {
        fun selectOrder(): Boolean
    }

    fun selectOrder(aListener: ClickListener?) {
        myListener = aListener
    }

    private fun initImagePicker() {
        FilePickerBuilder.instance.setMaxCount(5)
            .enableImagePicker(true)
            .enableVideoPicker(false)
            .pickPhoto(this@TLChatFragment)
    }

    private fun initVideoPicker() {
        FilePickerBuilder.instance.setMaxCount(5)
            .enableImagePicker(false)
            .enableVideoPicker(true)
            .pickPhoto(this@TLChatFragment)
    }

    private fun initAudioPicker() {
        val intent3 = Intent(myContext, AudioPickActivity::class.java)
        intent3.putExtra(IS_NEED_RECORDER, false)
        intent3.putExtra(Constant.MAX_NUMBER, 5)
        startActivityForResult(intent3, Constant.REQUEST_CODE_PICK_AUDIO)
    }

    private fun initDocPicker() {
        FilePickerBuilder.instance.setMaxCount(5)
            .enableImagePicker(false)
            .enableVideoPicker(false)
            .pickFile(this@TLChatFragment)
    }

    fun loadType(aData: List<Chats?>): ArrayList<Chats> {
        val aChatArrayList = ArrayList<Chats>()
        var aHeadcount = 0
        for (i in aData.indices) {
            val aSalonStylist = aData[i]
            val aSpilt: List<String> = aSalonStylist!!.createdAt!!.split(" ")
            if (i != 0) {
                val aSpilt1: List<String> = aData[i - 1]!!.createdAt!!.split(" ")
                if (aSpilt[0] == aSpilt1[0]) {
                } else {
                    aHeadcount += 1
                    aSalonStylist.chatDateheader = (aHeadcount)
                    Log.d(TAG, "aHeadcount: $aHeadcount")
                }
            } else {
                aHeadcount += 1
                aSalonStylist.chatDateheader = (aHeadcount)
                Log.d(TAG, "aHeadcount: $aHeadcount")
            }
            aChatArrayList.add(aSalonStylist)
        }

        insertData(aChatArrayList, isClear)

        return aChatArrayList
    }

    /**
     * Delete Table into Local Database
     */
    private fun deleteTable() {
        if (mChatDAO != null) {
            val mDAO = mChatDAO!!.chat()
            mDAO.truncateTable()
        }
    }

    /**
     * Delete Data into Local Database
     */
    private fun deleteData(chat: Chats) {
        if (mChatDAO != null) {
            val mDAO = mChatDAO!!.chat()
            mDAO.delete(chat)
        }
    }

    /**
     * Update Data into Local Database
     */
    private fun updateData(chat: Chats) {
        if (mChatDAO != null) {
            val mDAO = mChatDAO!!.chat()
            mDAO.update(chat)
        }
    }

    /**
     * Insert Data into Local Database
     */
    private fun insertData(mChatData: ArrayList<Chats>, isClear: Boolean) {
        if (mChatData.isEmpty())
            return

        if (mChatDAO != null) {
            val mDAO = mChatDAO!!.chat()

            if (isClear)
                mDAO.truncateTable()

            var isFound = false
            for (i in mChatData.indices) {
                if (mChatData[i].message!!.isEmpty()) {
                    when {
                        mChatData[i].imageUrl.isNotEmpty() -> mChatData[i].type = "image"
                        mChatData[i].videoUrl.isNotEmpty() -> mChatData[i].type = "video"
                        mChatData[i].documentUrl.isNotEmpty() -> mChatData[i].type = "document"
                        mChatData[i].audioUrl.isNotEmpty() -> mChatData[i].type = "audio"
                    }
                }

                if (!isFound) {
                    isFound = mDAO.getCount(mChatData[i].messageId!!) != 0

                    if (isFound)
                        mDAO.update(mChatData[i])
                }
            }

            if (!isFound)
                mDAO.insert(mChatData)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PHOTO && resultCode == Activity.RESULT_OK) {
            val mPaths: List<String>? =
                data!!.getStringArrayListExtra(KEY_SELECTED_MEDIA)
            mFiles.clear()
            mFiles.addAll(mPaths!!)

            if (mFiles.isNotEmpty())
                report(mFiles)
        }

        if (resultCode == Activity.RESULT_OK && data != null && requestCode == REQUEST_CODE_DOC) {
            val mPaths: List<String>? =
                data.getStringArrayListExtra(KEY_SELECTED_DOCS)
            mFiles.clear()
            mFiles.addAll(mPaths!!)

            if (mFiles.isNotEmpty())
                report(mFiles)
        }

        /*if (requestCode == REQUEST_CODE_PICK_AUDIO && resultCode == Activity.RESULT_OK && data != null) {
            val list: ArrayList<AudioFile>? =
                data.getParcelableArrayListExtra<AudioFile>(Constant.RESULT_PICK_AUDIO)
            mFiles.clear()
            if (list != null && list.size > 0) {
                for (aUri in list) {
                    mFiles.add(aUri.path)
                }
            }
        }*/

    }

    private fun clear(aEditText: EditText?) {
        aEditText!!.setText("")
    }

    /**
     * To receive otp from broadcast receiver
     */
    private val myBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) { //String aOTP = intent.getStringExtra("MESSAGE");
            Log.d(TAG, "onReceive: received")
//            chatsArrayList = myDBHelper.getChatMessages(myVisibleUserID)
            //chatsArrayList.addAll(myDBHelper.getChatMessages(myVisibleUserID));
            mAdapter!!.update(loadType(mChatList))
            if (intent.hasExtra("MSG_ID")) {
//                updateRead(intent.getStringExtra("MSG_ID"))
            }
        }
    }

    override fun onPause() {
        super.onPause()
        //mWebSocketClient.close();
        Log.e("leaving_fragment", "chat_scree")
        //myWebservice.PostIsOnlineIsOffLine("0");
    }

    override fun onDestroy() {
        super.onDestroy()
        myContext!!.unregisterReceiver(myBroadcastReceiver)
        val view = myContext!!.currentFocus
        if (view != null) {
            val imm =
                myContext!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        disconnectSocket()
    }

    override fun onResume() {
        super.onResume()
        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
        view!!.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                if (myDeleteChatMsglistShow == true) {
                    myDeleteChatMsglistShow = false
                    mAdapter!!.notifyDataSetChanged()
                    myChatBottomLAY!!.visibility = View.GONE
                } else {
                    sentLastMessage()
                    myFragmentManager!!.onBackPress()
                    // myFragmentManager.removeFragment(1);
                }
                // finish your previous fragments here.
                return@OnKeyListener true
            }
            false
        })
        activity!!.registerReceiver(myBroadcastReceiver, aFilter)
        if (Utility.isInternetAvailable(activity, false))
            initOnlineOffline("1")
        initSocket()
    }

    private fun updateUnreadJson(MsgId: String): JSONObject? {
        try {
            val parentObject = JSONObject()
            val childObject = JSONObject()
            childObject.put("chat_id", MsgId)
            parentObject.put("ReadChat", childObject)
            Log.d(
                TAG,
                "updateUnreadJson: $childObject"
            )
            return parentObject
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    // typing status
    private fun updateTypingStatus(aStstus: String, aTyping: String): String {
        val parentObject = JSONObject()
        val childObject = JSONObject()
        try {
            childObject.put("status", aStstus)
            childObject.put("FromUserid", getLoginUser(myContext).mUserId)
            childObject.put("ToUserid", mChat.user_id)
            parentObject.put("isTyping", childObject)
            parentObject.put("type", aTyping)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        Log.d(
            TAG,
            "isTypingStatus: $childObject"
        )
        return parentObject.toString()
    }

    // Online Offline status
    private fun updateOnlineOfflinStatus(
        aStstus: String,
        aTyping: String
    ): String {
        val parentObject = JSONObject()
        val childObject = JSONObject()
        try {
            childObject.put("is_online", aStstus)
            childObject.put("login_user_id", myUserInfo!!.mUserId)
            parentObject.put("is_online_data", childObject)
            parentObject.put("type", aTyping)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        Log.d(
            TAG,
            "isTypingStatus: $childObject"
        )
        return parentObject.toString()
    }

    override fun onBackPressed() {
        val mFragmentManager = TLFragmentManager(activity!!)
        mFragmentManager.replaceContent(
            TLChatListFragment(),
            TLChatListFragment.TAG,
            null
        )
    }

    override fun onResumeFragment() {
        sentLastMessage()
        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
        view!!.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                myFragmentManager!!.onBackPress()
                return@OnKeyListener true
            }
            false
        })
    }

    var previousMsgId = ""

    companion object {
        var TAG = TLChatFragment::class.java.simpleName
        var myDeleteChatMsglistShow = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (myContext as TLChatActivity).showBottomBar(true)
        if (Utility.isInternetAvailable(activity, false))
            initOnlineOffline("0")
        disconnectSocket()
    }

    override fun onScroll(absListView: AbsListView?, first: Int, visible: Int, total: Int) {
        myFirst = first
        myVisible = visible
        myTotal = total

        if (!isLoading && mTotalPage > mPage) {
            if (!isLoading) if (myFirst == 0 && myTotal != 0 && mPage <= mTotalPage) {
                isLoading = true
                mPage++
                isClear = false
                getChatMessages(mPage == 1, mPage.toString())
            }
        }
    }

    override fun onScrollStateChanged(absListView: AbsListView?, scrollState: Int) {

    }

    fun initMenu() {
        if (mAdapter!!.count == 0)
            mMenuList = arrayOf("Info", getBlockText())
        else mMenuList = arrayOf("Info", getBlockText(), "Delete", "Clear Chat")
    }

    private fun getBlockText(): String {
        return if (mChat.blocked == "1") "Unblock" else "Block"
    }

    private fun DeleteBottomView(aStatus: Boolean) {
        mAdapter!!.enableDelete(aStatus)
        myChatMenuIMG!!.isEnabled = !aStatus

        if (aStatus) {
            mSendLayout!!.visibility = View.GONE
            myChatBottomLAY!!.visibility = View.VISIBLE
        } else {
            mSendLayout!!.visibility = View.VISIBLE
            myChatBottomLAY!!.visibility = View.GONE
        }
    }

    override fun delete(id: Int) {
        var isChecked = false

        val list = mAdapter!!.getAll()

        for (i in list.indices) {
            if (list[i].isSelectedStatus) {
                isChecked = true
                break
            }
        }

        myBottomDelete!!.isEnabled = isChecked
        if (isChecked) {
            myBottomDelete!!.setTextColor(Utility.getColor(myContext!!, R.color.white))
            myBottomDelete!!.setBackgroundResource(R.drawable.bg_border_red_filled)
        } else {
            myBottomDelete!!.setTextColor(Utility.getColor(myContext!!, R.color.black))
            myBottomDelete!!.setBackgroundResource(R.drawable.bg_border_gray_filled)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            mysentIMG -> {
                if (getETValue(myMsgEDT).isNotEmpty()) {
                    if (mChat.blocked != null && mChat.blocked == "1") {
                        blockAction("Unblock ${mChat.user_name} to send a message")
                    } else {
                        if (checkInternet()) {
                            if (mWebSocketClient!!.connection.isOpen) {
                                mWebSocketClient!!.send(updateTypingStatus("1", "typing"))
                            }

                            val text = StringEscapeUtils.escapeJava(getETValue(myMsgEDT))
                            text!!.replace("\\n", "\n")

                            mTempData = Chats()
                            mTempData.messagefrom = getLoginUser(myContext).mUserId
                            mTempData.createdAt =
                                DateUtil.currentDateAndTime(DATE_FORMAT_FROM_SERVER)
                            mTempData.message_to = mChat.user_id!!
                            mTempData.message = text

                            initTextMessage(text)
                            clear(myMsgEDT)
                        }
                    }

                } else AppDialogs.showSnackbar(myMsgEDT!!, getString(R.string.labe_type_message))


            }
        }
    }

    private fun initTextMessage(msg: String) {
        AppDialogs.hideSoftKeyboard(activity!!, myChatListView!!)

        val mCase = getTextMsgParam(msg)
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)

        AppServices.execute(
            myContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.sendTextMsg,
            User::class.java
        )
    }

    private fun getTextMsgParam(msg: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("FromUserid", getLoginUser(myContext!!).mUserId!!)
            jsonObject.put("ToUserid", mChat.user_id)
            jsonObject.put("messagecontent", msg)
            jsonObject.put("club_id", "0")
            jsonObject.put("message_id", UUID.randomUUID().toString())
            val jsonParam = JSONObject()
            jsonParam.put("Chat", jsonObject)

            Log.i("Chat --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun initOnlineOffline(status: String) {
        val mCase = getOnlineOfflineParam(status)
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
        AppServices.execute(
            myContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.OnlineOfflineStatus,
            User::class.java
        )
    }

    private fun getOnlineOfflineParam(status: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("login_user_id", getLoginUser(myContext!!).mUserId!!)
            jsonObject.put("is_online", status)
            val jsonParam = JSONObject()
            jsonParam.put("IsOnline", jsonObject)

            Log.i("IsOnline --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    @SuppressLint("NewApi")
    fun report(mFiles: ArrayList<String>) {

        try {
            val mThumb = ArrayList<Bitmap>()
            activity!!.runOnUiThread {
                myProgressBar!!.visibility = View.VISIBLE
            }

            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, getTextMsgParam("")!!)

            httpClient = DefaultHttpClient()
            httpPost = HttpPost(result)
            val multipartEntity: MultipartEntityBuilder = MultipartEntityBuilder.create()

            for (i in mFiles.indices) {
                var thumb: Bitmap?
                val ImagepathURL: String = mFiles[i]
                multipartEntity.addBinaryBody("myfile$i", File(mFiles[i]))
                if (ImagepathURL != null || ImagepathURL != "null") {
                    if (Utility.getMediaType(ImagepathURL) == "video") {
                        val uri =
                            Uri.fromFile(File(mFiles[i]))

                        mThumb.add(
                            ThumbnailUtils.createVideoThumbnail(
                                uri.path,
                                MediaStore.Video.Thumbnails.MINI_KIND
                            )!!
                        )
                        thumb = mThumb[i]
                        val filesDir = myContext!!.filesDir
                        val imageFile = File(filesDir, i.toString() + "Test" + ".jpeg")
                        var os: OutputStream
                        try {
                            if (thumb != null) {
                                os = FileOutputStream(imageFile)
                                thumb.compress(Bitmap.CompressFormat.JPEG, 100, os)
                                os.flush()
                                os.close()
                            }
                        } catch (e: java.lang.Exception) {
                            Log.e(javaClass.simpleName, "Error writing bitmap", e)
                        }
                        if (thumb != null) {
                            multipartEntity.addBinaryBody("mythumb$i", imageFile)
                            Log.e(
                                "mythumb",
                                multipartEntity.addBinaryBody(
                                    "mythumb$i",
                                    imageFile
                                ).toString()
                            )
                        }
                    }
                }
            }

            httpPost.entity = MyHttpEntity(multipartEntity.build(), MyHttpEntity.ProgressListener {
                try {
                    activity!!.runOnUiThread {
                        myProgressBar!!.setProgress(it.toInt(), true)
                    }
                    Log.e("Progress --> ", it.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })

            try {
                Thread {
                    //Do some Network Request
                    if (httpClient == null || httpPost == null)
                        return@Thread

                    val httpResponse = httpClient.execute(httpPost)
                    val httpEntity = httpResponse.entity

                    val aJsonResponse = EntityUtils.toString(httpEntity)

                    if (activity != null) {
                        activity!!.runOnUiThread {
                            try {
                                val array = JSONObject(aJsonResponse).getJSONObject("response")
                                    .getJSONArray("messagecontent")

                                var count = 0

                                for (i in 0 until array.length()) {
                                    count++
                                }

                                if (count == array.length()) {
                                    myProgressBar!!.visibility = View.GONE
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}