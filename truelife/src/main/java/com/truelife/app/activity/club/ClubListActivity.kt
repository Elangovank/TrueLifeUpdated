package com.truelife.app.activity.club

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.activity.club.adapter.ClubListAdapter
import com.truelife.app.constants.TLConstant.SelectedClubList
import com.truelife.app.model.ClubInfo
import com.truelife.app.model.User
import com.truelife.base.BaseActivity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import kotlinx.android.synthetic.main.activity_club_list.*
import org.json.JSONObject

class ClubListActivity : BaseActivity(), ResponseListener, ClubListAdapter.ClickListener {

    private var mClubInfo: ArrayList<ClubInfo> = arrayListOf()
    private var mSelectedClubInfo: ArrayList<ClubInfo> = arrayListOf()
    var user: User? = null
    var mSelectedClubAdapter: ClubListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_club_list)
        init()
        setClubRecylceview()
        getClubsList()
    }


    fun getClubsList() {

        AppDialogs.showProgressDialog(myContext)

        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                GetUserClubDetailsCase()
            )
        AppServices.execute(
            myContext, this,
            result,
            Request.Method.POST,
            AppServices.API.clublist,
            ClubInfo::class.java
        )
    }

    fun setClubRecylceview() {
        mSelectedClubAdapter = ClubListAdapter(myContext!!, mClubInfo, this)
        val horizontalLayoutManagaer = LinearLayoutManager(myContext)
        club_list.setLayoutManager(horizontalLayoutManagaer)
        club_list.setAdapter(mSelectedClubAdapter)
    }

    override fun clickListener() {
        submit_club_button.setOnClickListener {


            val mArray: ArrayList<ClubInfo> = arrayListOf()
            mArray.addAll(mClubInfo.filter { it.isSelected })


            val intent = Intent(SelectedClubList)

            val bundle = Bundle()
            bundle.putSerializable(SelectedClubList, mArray)
            intent.putExtras(bundle)
            LocalBroadcastManager.getInstance(myContext).sendBroadcast(intent)
            finish()
        }

        close_club_button.setOnClickListener {


            val mArray: ArrayList<ClubInfo> = arrayListOf()
            mArray.addAll(mClubInfo.filter { it.isSelected })

            val intent = Intent(SelectedClubList)

            val bundle = Bundle()
            bundle.putSerializable(SelectedClubList, mArray)
            intent.putExtras(bundle)
            LocalBroadcastManager.getInstance(myContext).sendBroadcast(intent)
            finish()
        }
    }

    fun getBundle() {

        val bundle = intent.extras
        if (bundle != null) {
            if (bundle!!.containsKey(SelectedClubList)) {
                mSelectedClubInfo = bundle.getSerializable(SelectedClubList) as ArrayList<ClubInfo>
            }
        }
    }

    override fun init() {
        user = LocalStorageSP.getLoginUser(myContext)

        getBundle()
        clickListener()
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.clublist.hashCode()) {
                if (r.response!!.isSuccess) {
                    val data = r as ClubInfo

                    mClubInfo.addAll(data.data!!)

                    mSelectedClubInfo.forEach {
                        var i = 0
                        for (club in mClubInfo) {
                            if (club.id.equals(it.id, false)) {
                                mClubInfo.get(i).isSelected = true
                            }
                            i++
                        }
                    }
                    mSelectedClubAdapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    private fun GetUserClubDetailsCase(
        aLastUpdateTime: String = ""
    ): String {
        var aCaseStr = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("login_user_id", user!!.mUserId!!)
            jsonParam1.put("last_updated_time", aLastUpdateTime)
            val jsonParam = JSONObject()
            jsonParam.put("UserClubsList", jsonParam1)
            Log.e("UserClubsList", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onClick(tt: Any, position: Int, type: Boolean) {
        mClubInfo.get(position).isSelected = type
    }

    override fun onLongClick(view: View?, position: Int) {

    }
}