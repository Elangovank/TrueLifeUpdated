package com.truelife.app.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.adapter.BlockedListAdapter
import com.truelife.app.model.User
import com.truelife.app.model.UserList
import com.truelife.base.BaseActivity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import kotlinx.android.synthetic.main.activity_block_list.*
import kotlinx.android.synthetic.main.app_main_header.*
import org.json.JSONObject

class BlockedListActivity : BaseActivity(), ResponseListener, BlockedListAdapter.ClickListener {
    lateinit var mContext: FragmentActivity

    var user: User? = null
    private var mUserList: UserList? = null
    var mBlockList = ArrayList<UserList.UList>()
    var mFilterBlockList = ArrayList<UserList.UList>()
    lateinit var mRecycleView: RecyclerView

    var mAdapter: BlockedListAdapter? = null

    //view
    var mBackBtn: ImageView? = null
    var mToolbarTitle: TextView? = null
    var mNoDataFound: TextView? = null
    var mSearchEdit: EditText? = null

    //variable
    var mMemberId: String = ""
    var mBlockStatus: String = "0"
    var mBlockMsg: String = ""
    var mSelectedPost: Int = -1
    var mSearchStr: String = ""

    override fun onResume() {
        super.onResume()
        mBlockList.clear()
        getBlockedList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_list)
        init()
        setRecylceview()

    }

    override fun clickListener() {
        mBackBtn!!.setOnClickListener {
            finish()
        }

        add_user!!.setOnClickListener {
            val aIntent = Intent(mContext, AddBlockedList::class.java)
            mContext.startActivity(aIntent)
        }

        mSearchEdit!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                mSearchStr = p0.toString()
                if (mSearchStr.length > 0)
                    filter()
                else {
                    updateAdpater(mBlockList)
                }

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    override fun init() {
        mContext = this
        mRecycleView = findViewById(R.id.fragment_menu_list_RV)
        mBackBtn = findViewById(R.id.app_header_back_arrow)
        mToolbarTitle = findViewById(R.id.app_header_title)
        mNoDataFound = findViewById(R.id.no_block_friends_found)
        mSearchEdit = findViewById(R.id.fragment_search_friend)
        user = LocalStorageSP.getLoginUser(mContext)

        mBackBtn!!.visibility = View.VISIBLE
        app_header_menu.visibility = View.GONE
        mToolbarTitle!!.text = "Block List"
        mToolbarTitle!!.typeface = ResourcesCompat.getFont(mContext, R.font.calling_heart)
        mToolbarTitle!!.textSize = 30f
        clickListener()
    }


    fun getBlockedList(aPage: String = "1") {
        AppDialogs.showProgressDialog(mContext)
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                getBlockListCaseString(user!!.mUserId!!, aPage)!!
            )
        AppServices.execute(
            mContext, this,
            result,
            Request.Method.POST,
            AppServices.API.blockedlist,
            UserList::class.java
        )
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.blockedlist.hashCode()) {
                if (r.response!!.isSuccess) {
                    mUserList = r as UserList
                    mUserList!!.blockedList?.let { mBlockList.addAll(it) }
                    mAdapter!!.notifyDataSetChanged()
                } else {
                    if (mBlockList.isNullOrEmpty() || mBlockList.size == 0) {
                        mNoDataFound!!.visibility = View.VISIBLE
                        mRecycleView.visibility = View.GONE
                    } else {
                        mRecycleView.visibility = View.VISIBLE
                        mNoDataFound!!.visibility = View.GONE
                    }
                }
            } else if (r.requestType!! == AppServices.API.blockOrUnblocklist.hashCode()) {
                if (r.response!!.isSuccess) {
                    mBlockList.removeAt(mSelectedPost)
                    mAdapter!!.notifyDataSetChanged()
                    AppDialogs.customSuccessAction(
                        this,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        null,
                        false
                    )
                } else {
                    AppDialogs.customSuccessAction(
                        this,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        null,
                        false
                    )
                }
            }
        }
    }

    private fun getBlockListCaseString(
        aUserId: String,
        aPage: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("login_user_id", aUserId)
            jsonParam1.put("page_no", aPage)
            val jsonParam = JSONObject()
            jsonParam.put("BlockedList", jsonParam1)
            Log.e("BlockedList", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    fun setRecylceview() {
        val layoutManager = LinearLayoutManager(mContext)
        mRecycleView.layoutManager = layoutManager
        mRecycleView.setHasFixedSize(true)
        mAdapter = BlockedListAdapter(mContext, mBlockList, this)
        mRecycleView.adapter = mAdapter


    }


    fun blockUnblockAPI() {
        AppDialogs.showProgressDialog(mContext)
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                getBlockCaseString(user!!.mUserId!!)!!
            )
        AppServices.execute(
            mContext, this,
            result,
            Request.Method.POST,
            AppServices.API.blockOrUnblocklist,
            Response::class.java
        )
    }

    private fun getBlockCaseString(
        aUserId: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("login_user_id", aUserId)
            jsonParam1.put("friend_id", mMemberId)
            jsonParam1.put("block_status", mBlockStatus)
            val jsonParam = JSONObject()
            jsonParam.put("BlockFriend", jsonParam1)
            Log.e("BlockFriend", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onClick(any: Any, position: Int) {
        mSelectedPost = position
        var selectedBlockedUser = any as UserList.UList

        AppDialogs.customDoubleAction(
            mContext,
            null,
            "Do you want to unblock your friend",
            "Ok", "Cancel",
            object : AppDialogs.OptionListener {
                override fun yes() {
                    mMemberId = selectedBlockedUser.id!!
                    blockUnblockAPI()
                }

                override fun no() {
                    AppDialogs.hidecustomView()
                }

            }, false, isOptionable = false
        )

    }

    override fun onLongClick(view: View?, position: Int) {

    }

    fun updateAdpater(aList: ArrayList<UserList.UList>) {
        if (aList.size == 0) {
            mNoDataFound!!.visibility = View.VISIBLE
            mRecycleView.visibility = View.GONE
        } else {
            mRecycleView.visibility = View.VISIBLE
            mNoDataFound!!.visibility = View.GONE
            mAdapter!!.updateAdapter(aList)
            mAdapter!!.notifyDataSetChanged()
        }

    }

    fun filter() {
        mFilterBlockList.clear()
        for (i in mBlockList.indices) {
            if (!mSearchStr.isNullOrEmpty())
                if (mBlockList.get(i).fullname!!.contains(mSearchStr, true)) {
                    mFilterBlockList.add(mBlockList.get(i))
                }
        }
        updateAdpater(mFilterBlockList)
    }


}
