package com.truelife.app.fragment.chat.profile

import android.app.Activity
import android.content.res.Resources.NotFoundException
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.constants.TLConstant
import com.truelife.app.listeners.TLClubEndlessRecyclerViewScrollListener
import com.truelife.app.model.ChatProfile.ClubMedia
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP.getLoginUser
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import com.truelife.util.Utility
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Elango on 10-02-2020.
 */


class TLChatMedia : BaseFragment(), ResponseListener {
    var SpiltDateArray = ArrayList<String>()
    private var myContext: FragmentActivity? = null
    private var myUserIdStr: String? = ""
    private var myChatUserIdStr: String? = ""
    private var myRecyclerView: RecyclerView? = null
    private var myMenuRecyclerAdapter: TLChatProfileMediaAdapter? = null
    private var myPage = 1
    private var MyTotalPage = 1
    private var myFragmentManager: TLFragmentManager? = null
    private var myBackIcon: ImageView? = null
    private val scrollListener: TLClubEndlessRecyclerViewScrollListener? = null
    var myImageCreatedList = ArrayList<String>()
    var myMap = TreeMap<String, ArrayList<ClubMedia>>(Comparator { o1, o2 -> o1.compareTo(o2) })
    var myFinalList =
        TreeMap<String, ArrayList<ClubMedia>>(Comparator { o1, o2 -> o1.compareTo(o2) })
    private var loading = true
    var pastVisiblesItems = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    private var myListTypeStr: String? = ""
    private var mType = ""
    private var myHeaderNameTET: TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        val aView =
            inflater.inflate(R.layout.fragment_profile_total_photos, container, false)
        initializeClassAndWidgets(aView)
        return aView
    }

    private fun initializeClassAndWidgets(aView: View) {
        myContext = activity
        myFragmentManager = TLFragmentManager(myContext!!)
        myRecyclerView = aView.findViewById(R.id.fragment_horizontal_recycler_member)
        myBackIcon = aView.findViewById(R.id.app_header_back_arrow)
        myBackIcon!!.visibility = View.VISIBLE

        aView.findViewById<ImageView>(R.id.app_header_menu).visibility = View.GONE

        myHeaderNameTET = aView.findViewById(R.id.app_header_title)
        bundle
        SpiltDateArray.clear()
        loadTaskList()
        val linearLayoutManager =
            LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
        myRecyclerView!!.layoutManager = linearLayoutManager
        val splitcount = if (myListTypeStr == "image_list") 4 else 2
        myMenuRecyclerAdapter =
            TLChatProfileMediaAdapter(
                myContext!!,
                myMap,
                splitcount
            )
        myRecyclerView!!.adapter = myMenuRecyclerAdapter
        myRecyclerView!!.isNestedScrollingEnabled = true
        myRecyclerView!!.setHasFixedSize(true)
        loadValues(myFinalList)
        myRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                if (dy > 0) //check for scroll down
                {
                    visibleItemCount = linearLayoutManager.childCount
                    totalItemCount = linearLayoutManager.itemCount
                    pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition()
                    if (loading) {
                        if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                            loading = false
                            if (MyTotalPage != myPage) {
                                val updateHandler = Handler()
                                val runnable = Runnable {
                                    // TLHelper.showDialog(myProgressDialog);
                                    loadNextDataFromApi(myPage)
                                }
                                updateHandler.postDelayed(runnable, 1000)
                            }
                            Log.v("...", "Last Item Wow !")
                            //Do pagination.. i.e. fetch new data
                        }
                    }
                }
            }
        })
        clickListener()
    }

    override fun clickListener() {
        myBackIcon!!.setOnClickListener { myFragmentManager!!.onBackPress() }
    }

    val bundle: Unit
        get() {
            val aBundle = arguments
            if (aBundle != null) {
                myUserIdStr = getLoginUser(myContext).mUserId
                myChatUserIdStr = aBundle.getString("Chat_User_Id")
                myListTypeStr = aBundle.getString("list_type")
                if (myListTypeStr == "image_list") {
                    mType = "1"
                    myHeaderNameTET!!.text = getString(R.string.label_image_video)
                } else {
                    myHeaderNameTET!!.text = getString(R.string.labe_document)
                    mType = "2"
                }
            }
        }

    private fun loadValues(aNewsFeeds: TreeMap<String, ArrayList<ClubMedia>>) {
        try {
            myMenuRecyclerAdapter!!.updateAdapter(myContext!!, aNewsFeeds)
        } catch (e: NotFoundException) {
            e.printStackTrace()
        }
    }

    fun loadNextDataFromApi(offset: Int) {
        myPage++
        loadTaskList()
    }

    private fun loadTaskList() {
        try {
            getPhotos()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getPhotos() {
        if (checkInternet()) {
            AppDialogs.hideSoftKeyboard((myContext as Activity?)!!, myBackIcon!!)
            AppDialogs.showProgressDialog(myContext!!)

            val mCase = getAttachments(myPage)
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase)
            AppServices.execute(
                myContext!!, this,
                result,
                Request.Method.POST,
                AppServices.API.ClubMedia,
                ClubMedia::class.java
            )
        }
    }

    var DocuArrayList = ArrayList<ClubMedia>()

    private fun getAttachments(aPage: Int): String {
        var aCaseStr = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", myChatUserIdStr)
            jsonParam1.put("login_user_id", getLoginUser(myContext).mUserId)
            jsonParam1.put("page_no", aPage)
            jsonParam1.put("type", mType)
            val jsonParam = JSONObject()
            jsonParam.put("MessengerFileList", jsonParam1)
            Log.e("MessengerFileList", jsonParam.toString())
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onResumeFragment() {
        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
        view!!.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                myFragmentManager!!.removeFragment(1)
                // finish your previous fragments here.
                return@OnKeyListener true
            }
            false
        })
    }

    override fun onBackPressed() {}
    override fun init(view: View) {}
    override fun initBundle() {}

    companion object {
        val TAG = TLChatMedia::class.java.simpleName
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.ClubMedia.hashCode()) {
                if (r.response!!.isSuccess) {
                    val media = (r as ClubMedia)
                    MyTotalPage = media.total_page.toInt()
                    initMedia(media.mData)
                } else AppDialogs.showToastDialog(myContext!!, r.response!!.responseMessage!!)
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

    private fun initMedia(mData: ArrayList<ClubMedia>) {
        DocuArrayList.addAll(mData)
        val aDate = ArrayList<String>()
        for (i in DocuArrayList.indices) {
            if (!aDate.contains(getDisplayChatMonthName(DocuArrayList[i].created))) {
                aDate.add(getDisplayChatMonthName(DocuArrayList[i].created)!!)
            }
        }


        try {
            for (aDisplayDate in aDate) {
                val myMedia: ArrayList<ClubMedia> = ArrayList()
                for (i in DocuArrayList.indices) {
                    if (aDisplayDate == getDisplayChatMonthName(DocuArrayList[i].created)) {
                        myMedia.add(DocuArrayList[i])
                    }
                }
                myMap[aDisplayDate] = myMedia
            }
            myFinalList.putAll(myMap)
            myMenuRecyclerAdapter!!.notifyDataSetChanged()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun getDisplayChatMonthName(aDisplayDate: String?): String? {
        val formatrecived = SimpleDateFormat("dd-MM-yyyy")
        val formarwanted = SimpleDateFormat("MMM, yyyy")
        var recived: Date? = null
        try {
            recived = formatrecived.parse(aDisplayDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return formarwanted.format(recived)
    }
}