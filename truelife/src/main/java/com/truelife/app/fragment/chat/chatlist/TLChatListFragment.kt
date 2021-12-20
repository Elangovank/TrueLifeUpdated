package com.truelife.app.fragment.chat.chatlist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.activity.CreateClub
import com.truelife.app.activity.TLChatActivity
import com.truelife.app.constants.TLConstant
import com.truelife.app.fragment.chat.message.TLChatFragment
import com.truelife.app.fragment.chat.newchat.TLStartNewChatTab
import com.truelife.app.fragment.chat.settings.TLChatSettingsFragment
import com.truelife.app.model.Chat
import com.truelife.app.model.User
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.AppDataBase
import com.truelife.storage.DatabaseClient
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import com.truelife.util.TLBottomOptionAdapter
import com.truelife.util.Utility
import com.truelife.util.Utility.isInternetAvailable
import kotlinx.android.synthetic.main.app_main_header.*
import kotlinx.android.synthetic.main.no_data_text.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI


/**
 * Created by Elango on 12-12-2019.
 */

class TLChatListFragment : BaseFragment(), View.OnClickListener,
    TLChatListAdapter.Callback,
    ResponseListener {

    private var mContext: Context? = null
    private var mFragmentManager: TLFragmentManager? = null
    private lateinit var mBack: ImageView
    private lateinit var mMenu: ImageView
    private lateinit var mSearch: EditText
    private lateinit var mChatRecycler: RecyclerView
    private lateinit var mNoDataText: TextView
    private lateinit var mSearchCancel: TextView
    private lateinit var mStartNewChat: FloatingActionButton
    private lateinit var mCancel: TextView
    private lateinit var mDelete: TextView
    private var mChatAdapter: TLChatListAdapter? = null
    private var mSwipeRefresh: SwipeRefreshLayout? = null
    var mChatData = ArrayList<Chat.ChatList>()
    private var myChatBottomLAY: LinearLayout? = null
    var mWebSocketClient: WebSocketClient? = null
    var mDeletedIds = ""
    var mMenuList: Array<String> = arrayOf()
    var mChatListDAO: AppDataBase? = null
    var mMsg = "No chats found"

    private var mView: View? = null

    companion object {
        var TAG: String = TLChatListFragment::class.java.simpleName
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_chat_list, container, false)
        init(mView!!)
        clickListener()

        return mView
    }

    override fun init(view: View) {
        mContext = activity
        mFragmentManager = TLFragmentManager(activity!!)

        mBack = mView!!.findViewById(R.id.app_header_back_arrow)
        mMenu = mView!!.findViewById(R.id.app_header_dot_menu)

        mSwipeRefresh = mView!!.findViewById(R.id.fragment_chat_swipe_ref)
        mChatRecycler = mView!!.findViewById(R.id.fragment_chat_recycler)
        mSearch = mView!!.findViewById(R.id.fragment_search)
        mNoDataText = mView!!.findViewById(R.id.no_data_found_txt)
        mSearchCancel = mView!!.findViewById(R.id.fragment_cancel)

        myChatBottomLAY = mView!!.findViewById(R.id.fragment_messenger_bottom_lay)
        mCancel = mView!!.findViewById(R.id.bottom_layout_cancel)
        mDelete = mView!!.findViewById(R.id.bottom_layout_delete)
        mStartNewChat = mView!!.findViewById(R.id.startNewChat)

        initRecycler()

        mChatListDAO = DatabaseClient(mContext!!).getAppDatabase()

        AppDialogs.swipeRefColor(mContext!!, mSwipeRefresh!!)
        mSwipeRefresh!!.setOnRefreshListener {
            getData(false)
        }

        mBack.visibility = View.VISIBLE
        mMenu.visibility = View.VISIBLE

        mSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 0)
                    mSearchCancel.visibility = View.VISIBLE

                mChatAdapter!!.filter.filter(s!!.toString()) {
                    if (it == 0) {
                        mNoDataText.text = String.format("We couldn't find anything for\n'%s'", s)
                        mNoDataText.visibility = View.VISIBLE
                    } else mNoDataText.visibility = View.GONE
                }
            }
        })

    }

    private fun initSocket() {
        val uri = URI(BuildConfig.SOCKET_URL)

        mWebSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake) {
                Log.i("Socket onOpen --> ", handshakedata.toString())
            }

            override fun onMessage(message: String) {

                if (mWebSocketClient == null || !mWebSocketClient!!.connection.isOpen || activity == null)
                    return

                Log.i("Socket onMessage --> ", message)

                activity!!.runOnUiThread {
                    if (message.isNotEmpty() && mChatData.isNotEmpty()) {
                        val obj = JSONObject(message)

                        // Update User Status
                        if (obj.has("user_status")) {
                            val array = obj.getJSONArray("user_status")
                            for (x in 0 until array.length()) {
                                val data = array.getJSONObject(x)
                                for (i in mChatData.indices) {
                                    if (mChatData[i].user_id == data.getString("user_id")) {
                                        mChatData[i].online = data.getString("is_online")
                                        mChatData[i].last_seen = data.getString("last_seen")
                                        mChatAdapter!!.notifyDataSetChanged()
                                    }
                                }
                            }
                        }

                        // Update User Messages
                        if (obj.has("chat_history")) {
                            val array = obj.getJSONArray("chat_history")
                            for (x in 0 until array.length()) {
                                var isFound = false
                                val data = array.getJSONObject(x)

                                if (isReceiver(data.getString("ToUserid"))) {
                                    // Old Chat
                                    for (i in mChatData.indices) {

                                        for (mChatDatum in mChatData) {
                                            if(data.getString("id") .equals(mChatDatum.id)){
                                                return@runOnUiThread
                                            }
                                        }
                                        if (mChatData[i].user_id == data.getString("FromUserid")) {
                                            isFound = true
                                            if (data.getInt("is_blocked") == 0) {
                                                mChatData[i].message =
                                                    data.getString("messagecontent")
                                                mChatData[i].attachments =
                                                    data.getString("attachments")
                                                mChatData[i].profile_image = data.getString("image")
                                                mChatData[i].id = data.getString("id")
                                                mChatData[i].online = data.getString("is_online")
                                                mChatData[i].last_seen = data.getString("last_seen")

                                                if (mChatData[i].unread_count != null
                                                    && mChatData[i].unread_count!!.isNotEmpty()
                                                ) {
                                                    mChatData[i].unread_count =
                                                        (mChatData[i].unread_count!!.toInt() + 1).toString()
                                                }

                                                val chat = mChatData[i]
                                                mChatData.removeAt(i)
                                                mChatData.add(0, chat)
                                                updateData(chat)
                                                mChatAdapter!!.notifyDataSetChanged()
                                                showNoText(mMsg)
                                            }
                                        }
                                    }

                                    // New Chat
                                    if (data.getInt("is_blocked") == 0 && !isFound) {

                                        val chat = Chat.ChatList()

                                        chat.id = data.getString("id")
                                        chat.user_id = data.getString("FromUserid")
                                        chat.message = data.getString("messagecontent")
                                        chat.attachments = data.getString("attachments")
                                        chat.profile_image = data.getString("image")
                                        chat.online = data.getString("is_online")
                                        chat.last_seen = data.getString("last_seen")
                                        chat.user_name = data.getString("user_name")
                                        chat.unread_count = "1"
                                        chat.read = data.getInt("is_read")

                                        mChatData.add(0, chat)
                                        insertData(mChatData)
                                        mChatAdapter!!.notifyDataSetChanged()
                                        showNoText(mMsg)
                                    }
                                }
                            }
                        }

                        // Typing
                        if (obj.has("isTyping")) {
                            val data = obj.getJSONObject("isTyping")
                            if (isReceiver(data.getString("ToUserid")))
                                for (i in mChatData.indices) {
                                    if (mChatData[i].user_id == data.getString("FromUserid")
                                        && !mChatData[i].typing && !isBlocked(mChatData[i])
                                    ) {
                                        mChatData[i].typing = true
                                        mChatAdapter!!.notifyDataSetChanged()
                                    }
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

    private fun isBlocked(chatList: Chat.ChatList): Boolean {
        return chatList.blocked == "1" || chatList.user_blocked == "1"
    }

    private fun isReceiver(id: String): Boolean {
        return LocalStorageSP.getLoginUser(mContext!!).mUserId == id
    }

    override fun clickListener() {
        mBack.setOnClickListener(this)
        mMenu.setOnClickListener(this)
        mSearchCancel.setOnClickListener(this)
        mCancel.setOnClickListener(this)
        mDelete.setOnClickListener(this)
        mStartNewChat.setOnClickListener(this)
    }

    private fun disconnectSocket() {
        if (mWebSocketClient != null && mWebSocketClient!!.connection.isOpen) {
            mWebSocketClient!!.connection.close()
            mWebSocketClient!!.close()
        }
    }

    private fun DeleteBottomView(aStatus: Boolean) {

        mChatAdapter!!.enableDelete(aStatus)
        (mContext as TLChatActivity).showBottomBar(!aStatus)

        if (aStatus) {
            myChatBottomLAY!!.visibility = View.VISIBLE
            mStartNewChat.hide()
        } else {
            myChatBottomLAY!!.visibility = View.GONE
            mStartNewChat.show()
        }
    }


    private fun getData(showProgress: Boolean) {
        if (isInternetAvailable(mContext, false)) {
            AppDialogs.hideSoftKeyboard((mContext as Activity?)!!, mChatRecycler)
            if (showProgress)
                AppDialogs.showProgressDialog(mContext!!)

            val mCase = getListParam()
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
            AppServices.execute(
                mContext!!, this,
                result,
                Request.Method.POST,
                AppServices.API.chatList,
                Chat.ChatList::class.java
            )
        } else {
            // Load Data from Local Database
            val mList = ArrayList<Chat.ChatList>()
            val list = mChatListDAO!!.chatlist().getAllList()
            for (i in list.indices) {
                mList.add(list[i])
            }
            loadData(mList)
            mSwipeRefresh!!.isRefreshing = false
        }
    }

    private fun getListParam(): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("page_no", "1")
            jsonObject.put("login_user_id", LocalStorageSP.getLoginUser(mContext!!).mUserId!!)
            val jsonParam = JSONObject()
            jsonParam.put("ChatUsersList", jsonObject)

            Log.i("ChatUsersList --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun getStatusParam(text: String): String? {
        var aCaseStr: String? = " "
        try {
            val user = LocalStorageSP.getLoginUser(mContext!!)
            val jsonObject = JSONObject()
            jsonObject.put("user_id", user.mUserId)
            jsonObject.put("status", text)
            jsonObject.put("home_town", user.mHomeTown)
            jsonObject.put("profession", user.mProfession)
            jsonObject.put("education", user.mEducation)
            jsonObject.put("about_us", "Test")
            val jsonParam = JSONObject()
            jsonParam.put("UpdateProfile", jsonObject)

            Log.i("Status --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun getDeleteParam(ids: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("login_user_id", LocalStorageSP.getLoginUser(mContext!!).mUserId!!)
            jsonObject.put("chat_ids", "")
            jsonObject.put("friend_id", ids)
            jsonObject.put("remove_type", "3")
            val jsonParam = JSONObject()
            jsonParam.put("RemoveChat", jsonObject)

            Log.i("RemoveChat --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(mContext)
        mChatRecycler.layoutManager = layoutManager
        mChatRecycler.setHasFixedSize(true)
        mChatAdapter = TLChatListAdapter(
            mContext!!,
            mChatData,
            this
        )
        mChatRecycler.adapter = mChatAdapter
    }


    override fun initBundle() {

    }

    override fun onClick(v: View?) {
        when (v!!) {
            mBack -> (mContext as TLChatActivity).finishActivity()
            mStartNewChat -> {
                if (checkInternet())
                    mFragmentManager!!.addContent(
                        TLStartNewChatTab(),
                        TLStartNewChatTab.TAG,
                        R.anim.slide_in_up,
                        R.anim.slide_out_up,
                        null
                    )
            }
            mCancel -> {
                DeleteBottomView(false)

                for (i in mChatData.indices) {
                    mChatData[i].checked = false
                }

                mChatAdapter!!.notifyDataSetChanged()
            }
            mSearchCancel -> {
                AppDialogs.hideSoftKeyboard(activity!!, mChatRecycler)
                mSearch.text = null
                mSearchCancel.visibility = View.GONE
                mSearch.clearFocus()
            }
            mMenu -> {

                mMenuList = if (mChatData.isNotEmpty())
                    arrayOf("Start a Club", "Status", "Delete", "Profile", "Settings")
                else arrayOf("Start a Club", "Status", "Profile", "Settings")

                AppDialogs.showBottomMenu(
                    mContext!!,
                    mMenuList,
                    object : TLBottomOptionAdapter.Callback {
                        override fun info(option: Int, text: String) {
                            when (text) {
                                "Start a Club" -> {
                                    if (checkInternet())
                                        startActivity(Intent(mContext!!, CreateClub::class.java))
                                }
                                "Status" -> {
                                    if (checkInternet()) {
                                        val view = AppDialogs.showcustomView(
                                            mContext!!,
                                            R.layout.layout_inflate_profile_details_dialog,
                                            false, false
                                        )
                                        val title = view!!.findViewById<TextView>(R.id.header_name)
                                        title.text = "STATUS"

                                        val status =
                                            view.findViewById<EditText>(R.id.chat_status_edt)

                                        status.setText(LocalStorageSP.getLoginUser(mContext).mStatus!!.trim())

                                        view.findViewById<Button>(R.id.layout_inflate_profile_details_dialog_cancel_Btn)
                                            .setOnClickListener { AppDialogs.hidecustomView() }

                                        view.findViewById<Button>(R.id.layout_inflate_profile_details_dialog_update_Btn)
                                            .setOnClickListener { initUpdateStatus(getETValue(status)) }
                                    }
                                }
                                "Delete" -> {
                                    if (checkInternet())
                                        if (mChatData.isNotEmpty())
                                            DeleteBottomView(true)
                                }
                                "Profile" -> {
                                    Helper.navigateChatProfile(
                                        activity!!,
                                        LocalStorageSP.getLoginUser(mContext!!).mUserId
                                    )

                                }
                                "Settings" -> {
                                    mFragmentManager!!.addContent(
                                        TLChatSettingsFragment(),
                                        TLChatSettingsFragment.TAG,
                                        null
                                    )
                                }
                            }
                        }
                    },
                    "Cancel"
                )
            }

            mDelete -> {
                if (checkInternet()) {
                    val ids = ArrayList<String>()
                    for (i in mChatData.indices) {
                        if (mChatData[i].checked)
                            ids.add(mChatData[i].user_id!!)
                    }

                    initDelete(TextUtils.join(",", ids))

                }
            }
        }
    }

    private fun initDelete(ids: String) {
        mDeletedIds = ids
        AppDialogs.showProgressDialog(mContext!!)

        val mCase = getDeleteParam(mDeletedIds)
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
        AppServices.execute(
            mContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.removeChatList,
            User::class.java
        )
    }

    private fun initUpdateStatus(text: String) {
        if (checkInternet()) {
            AppDialogs.showProgressDialog(mContext!!)

            val mCase = getStatusParam(text)
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
            AppServices.execute(
                mContext!!, this,
                result,
                Request.Method.POST,
                AppServices.API.updateProfile,
                User::class.java
            )
        }
    }

    override fun onBackPressed() {
        (mContext as TLChatActivity).finishActivity()
    }

    override fun onResumeFragment() {
        app_header_title.text = "Messenger"
        app_header_title.typeface = ResourcesCompat.getFont(mContext!!, R.font.calling_heart)
        app_header_title.textSize = 30f
        app_header_menu.visibility = View.GONE
        getData(true)
        initSocket()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disconnectSocket()
    }

    override fun info(position: Int) {
        isInternetAvailable(mContext, false)
        val bundle = Bundle()
        bundle.putString("data", Gson().toJson(mChatData[position]))
        mFragmentManager!!.replaceContent(TLChatFragment(), TLChatFragment.TAG, bundle)
    }

    override fun onResume() {
        onResumeFragment()
        super.onResume()
    }

    override fun delete() {
        var isChecked = false
        for (i in mChatData.indices) {
            if (mChatData[i].checked) {
                isChecked = true
                break
            }
        }

        mDelete.isEnabled = isChecked
        if (isChecked) {
            mDelete.setTextColor(Utility.getColor(mContext!!, R.color.white))
            mDelete.setBackgroundResource(R.drawable.bg_border_red_filled)
        } else {
            mDelete.setTextColor(Utility.getColor(mContext!!, R.color.black))
            mDelete.setBackgroundResource(R.drawable.bg_border_gray_filled)
        }
    }

    override fun onResponse(r: Response?) {
        mSwipeRefresh!!.isRefreshing = false
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.chatList.hashCode()) {
                if (r.response!!.isSuccess) {
                    loadData((r as Chat.ChatList).mData)
                    Thread {
                        // Local Database
                        insertData(mChatData)
                    }.start()
                }
                showNoText(r.response!!.responseMessage!!)
            } else if (r.requestType!! == AppServices.API.updateProfile.hashCode()) {
                if (r.response!!.isSuccess) {
                    val user = LocalStorageSP.getLoginUser(mContext)
                    user.mStatus = (r as User).mStatus
                    LocalStorageSP.storeLoginUser(mContext!!, user)
                    AppDialogs.hidecustomView()
                } else AppDialogs.showToastDialog(mContext!!, r.response!!.responseMessage!!)
            } else if (r.requestType!! == AppServices.API.removeChatList.hashCode()) {
                if (r.response!!.isSuccess) {
                    val ids = mDeletedIds.split(",")
                    for (i in ids.indices) {
                        for (x in mChatData.indices) {
                            if (ids[i].toInt() == mChatData[x].user_id!!.toInt()) {
                                removeFromDB(mChatData[x])
                                mChatData.removeAt(x)
                                mChatAdapter!!.notifyItemRemoved(x)
                                mChatAdapter!!.notifyItemRangeChanged(x, mChatData.size)
                                break
                            }
                        }
                    }
                    DeleteBottomView(false)
                    showNoText(mMsg)
                } else AppDialogs.showToastDialog(mContext!!, r.response!!.responseMessage!!)
            }
        } else AppDialogs.customOkAction(
            mContext!!,
            null,
            TLConstant.SERVER_NOT_REACH,
            null,
            null,
            false
        )
    }

    fun showNoText(msg: String) {
        if (mChatData.isEmpty()) {
            no_data_found_txt!!.text = msg
            no_data_found_txt!!.visibility = View.VISIBLE
        } else no_data_found_txt!!.visibility = View.GONE
    }

    private fun loadData(mData: ArrayList<Chat.ChatList>) {
        mChatData.clear()
        mChatData.addAll(mData)
        mChatAdapter!!.notifyDataSetChanged()
    }

    /**
     * Delete Data into Local Database
     */
    private fun removeFromDB(chatList: Chat.ChatList) {
        try {
            if (mChatListDAO != null) {
                val mDAO = mChatListDAO!!.chatlist()
                mDAO.delete(chatList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Update Data into Local Database
     */
    private fun updateData(chatList: Chat.ChatList) {
        try {
            if (mChatListDAO != null) {
                val mDAO = mChatListDAO!!.chatlist()
                mDAO.update(chatList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Insert Data into Local Database
     */
    private fun insertData(mChatData: ArrayList<Chat.ChatList>) {
        try {
            if (mChatListDAO != null) {
                val mDAO = mChatListDAO!!.chatlist()
                mDAO.truncateTable()
                mDAO.insert(mChatData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}