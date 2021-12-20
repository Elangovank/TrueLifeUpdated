package com.truelife.app.fragment.club

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.fragment.club.adapter.ClubAdapter
import com.truelife.app.model.Club
import com.truelife.app.model.User
import com.truelife.base.TLFragmentManager
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.EndlessRecyclerViewScrollListener
import com.truelife.util.Helper
import org.json.JSONObject

class TLClubByYouList() : DialogFragment(),
    ClubAdapter.ClickListener, ResponseListener {
    var LIST_LOADING_DURATION = 1000
    private var myContext: FragmentActivity? = null
    private var myClubByRecyclerList: RecyclerView? = null
    private var myFragmentManager: TLFragmentManager? = null
    private var aClubName: TextView? = null
    private var myCloseButton: ImageView? = null
    private var scrollListener: EndlessRecyclerViewScrollListener? = null
    var myClubByListAdapter: ClubAdapter? = null
    var aView: View? = null
    private var myRV_LayoutManager: GridLayoutManager? = null
    private var mClubList: ArrayList<Club.Clubs> =
        arrayListOf()


    private var myClubType: String? = null
    private var myCardView: CardView? = null
    var mClubType: String = ""
    var user: User? = null
    var mCurrentPage: Int = 1
    var mTotalPage: Int = 1

    fun initBundle() {
        try {
            val bundle = arguments
            if (bundle != null) {
                mClubType = bundle.getString("club_type")!!
                if (mClubType == "ClubByYou") {
                    aClubName!!.text = myContext!!.resources.getString(R.string.label_clubs_by_you)
                } else if (mClubType == "ClubIn") {
                    aClubName!!.text =
                        myContext!!.resources.getString(R.string.label_clubs_you_re_in)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        aView = inflater.inflate(R.layout.inflate_club, container, false)
        initializeClassAndWidgets(aView!!)
        initBundle()
        clubWebservice()
        return aView
    }

    private fun initializeClassAndWidgets(aView: View) {
        try {
            myContext = activity
            user = LocalStorageSP.getLoginUser(myContext)
            myFragmentManager = TLFragmentManager(myContext!!)
            myClubByRecyclerList =
                aView.findViewById<View>(R.id.inflate_club_by_recycler) as RecyclerView
            myCardView =
                aView.findViewById<View>(R.id.layout_club_Card_layout) as CardView
            aClubName = aView.findViewById<View>(R.id.Club_name_TET) as TextView
            myCloseButton =
                aView.findViewById<View>(R.id.inflate_alert_close_button) as ImageView

            if (dialog != null) {
                val displayMetrics = DisplayMetrics()
                myContext!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
                val height = displayMetrics.heightPixels
                val width = displayMetrics.widthPixels
                dialog!!.window!!.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
                dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog!!.window!!
                    .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }


            myCloseButton!!.setOnClickListener {

                myFragmentManager!!.onBackPress()

            }
            setRecylceview()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun loadNextDataClubByYou(offset: Int) {
        mCurrentPage++
        clubWebservice()
    }


    fun showMaterialAlertDialog(aContext: FragmentActivity, aMessage: String?) {
        try {
            val dialog =
                AlertDialog.Builder(aContext)
            dialog.setCancelable(true)
            dialog.setTitle(aContext.getString(R.string.app_name))
            dialog.setIcon(R.mipmap.ic_launcher)
            dialog.setMessage(aMessage)
            dialog.setPositiveButton("Ok") { dialog, id ->
                dialog.dismiss()
                if (getDialog() != null) {
                    getDialog()!!.dismiss()
                }
            }
            val alert = dialog.create()
            alert.show()
            //SET BUTTON_NEGATIVE and BUTTON_POSITIVE Color
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alert.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(aContext.getColor(R.color.green_color_alert))
            } else {
                alert.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(aContext.resources.getColor(R.color.green_color_alert))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(tt: Any, position: Int) {

        val obj = tt as Club.Clubs
        startActivity(
            Intent(myContext, TLClubDetails::class.java).putExtra(
                "Club_for_you", "Club_for_you"
            )
                .putExtra("Club_for_you_select_position", position.toString())
                .putExtra("ClubType", "ClubByYou")
                .putExtra("Club_id", obj.id)
        )
    }

    override fun onLongClick(view: View?, position: Int) {}

    companion object {
        var TAG = TLClubByYouList::class.java.simpleName
    }

    fun setRecylceview() {

        val layoutManager = GridLayoutManager(myContext, 2)
        myClubByRecyclerList!!.layoutManager = layoutManager
        myClubByRecyclerList!!.setHasFixedSize(true)
        myClubByListAdapter = ClubAdapter(myContext!!, mClubList, this)
        myClubByRecyclerList!!.adapter = myClubByListAdapter
        scrollListener = object : EndlessRecyclerViewScrollListener(layoutManager, 5) {
            override fun onLoadMore(
                page: Int,
                totalItemsCount: Int,
                view: RecyclerView?
            ) { // Triggered only when new data needs to be appended to the list
// Add whatever code is needed to append new items to the bottom of the list
                if (mTotalPage > page) {
                    val updateHandler = Handler()
                    val runnable = Runnable {
                        System.gc()
                        loadNextDataClubByYou(page)
                    }
                    updateHandler.postDelayed(runnable, LIST_LOADING_DURATION.toLong())
                }
            }
        }
        myClubByRecyclerList!!.addOnScrollListener(scrollListener!!)

    }


    fun clubWebservice() {


        AppDialogs.showProgressDialog(myContext!!)

        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                getClubByYou_and_ClubInCaseString(user!!.mUserId!!, mCurrentPage, mClubType!!)!!
            )
        AppServices.execute(
            myContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.clublist,
            Club::class.java
        )
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.clublist.hashCode()) {
                if (r.response!!.isSuccess) {
                    val data = r as Club
                    mTotalPage = data.totaPage.toInt()
                    mClubList.addAll(data.data!!)
                    Log.e("size", "" + mClubList.size)
                    myClubByListAdapter!!.notifyDataSetChanged()
                } else
                    AppDialogs.showNoData(
                        aView!!,
                        mClubList.isEmpty(),
                        r.response!!.responseMessage
                    )
            }
        }

    }

    private fun getClubByYou_and_ClubInCaseString(
        aUserId: String,
        aPage: Int,
        aClubName: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("login_user_id", aUserId)
            jsonParam1.put("page_no", aPage)
            val jsonParam = JSONObject()
            if (aClubName == "ClubByYou") {
                jsonParam.put("ClubsByYou", jsonParam1)
                Log.e("ClubsByYou", " $jsonParam")
            } else if (aClubName == "ClubIn") {
                jsonParam.put("ClubsIn", jsonParam1)
                Log.e("ClubsIn", " $jsonParam")
            } else {
                jsonParam.put("FriendsClubSuggestion", jsonParam1)
                Log.e("FriendsClubSuggestion", " $jsonParam")
            }
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


}