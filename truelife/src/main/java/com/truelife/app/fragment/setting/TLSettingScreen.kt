package com.truelife.app.fragment.setting

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.truelife.BuildConfig
import com.truelife.ClickListener
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.activity.*
import com.truelife.app.fragment.setting.adapter.TLSettingsAdapter
import com.truelife.app.model.User
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager
import com.truelife.chat.activities.SplashActivity
import com.truelife.chat.utils.SharedPreferencesManager
import com.truelife.chat.utils.network.FireManager
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import com.truelife.util.TLBottomOptionAdapter
import kotlinx.android.synthetic.main.fragment_setting_screen.*
import org.json.JSONObject

class TLSettingScreen : BaseFragment(), ClickListener, ResponseListener {

    private var mContext: Context? = null
    private var mFragmentManager: TLFragmentManager? = null

    private var mRecycleView: RecyclerView? = null

    private var mAdapter: TLSettingsAdapter? = null
    private var mView: View? = null

    private var mGifImageView: ImageView? = null

    private var mAppBarlayout: AppBarLayout? = null

    private var mCollapsingToolbarLayout: CollapsingToolbarLayout? = null

    private var mBackBtn: ImageView? = null

    companion object {
        var TAG: String = TLSettingScreen::class.java.simpleName
    }

    var user: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_setting_screen, container, false)
        init(mView!!)
        return mView
    }

    override fun onBackPressed() {

    }

    override fun onPause() {
        super.onPause()
        //   (mContext as TLDashboardActivity?)!!.showBottomBar()
    }

    override fun onResumeFragment() {
        (mContext as TLDashboardActivity?)!!.hideBottomBar()
    }

    override fun init(view: View) {
        mContext = activity
        (mContext as TLDashboardActivity?)!!.hideBottomBar()

        user = LocalStorageSP.getLoginUser(mContext)
        mFragmentManager = TLFragmentManager(requireActivity())
        mRecycleView = view.findViewById(R.id.setting_Recycler) as RecyclerView
        mGifImageView = view.findViewById(R.id.gif_image) as ImageView
        mAppBarlayout = view.findViewById(R.id.app_bar_layout) as AppBarLayout
        mBackBtn = view.findViewById(R.id.common_back_arrow) as ImageView
        mCollapsingToolbarLayout =
            view.findViewById(R.id.collapsing_toolbar) as CollapsingToolbarLayout
        loadGif()
        loadRecycleView()
        clickListener()
    }

    override fun initBundle() {

    }

    override fun clickListener() {
        mBackBtn!!.setOnClickListener {

            mFragmentManager!!.onBackPress()
        }
        mAppBarlayout!!.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, i ->

            if (Math.abs(i) - appBarLayout.totalScrollRange === 0) { //  Collapsed
                //  mCollapsingToolbarLayout!!.setTitle("Settings")
                //  mCollapsingToolbarLayout!!.collapsedTitleGravity = Gravity.CENTER
                heading.setText("Settings")
            } else { //Expanded
                //   mCollapsingToolbarLayout!!.setTitle("")
                heading.setText("")
            }
        })
    }


    fun loadRecycleView() {
        mRecycleView!!.setHasFixedSize(true)
        mRecycleView!!.setLayoutManager(LinearLayoutManager(mContext))
        mRecycleView!!.setNestedScrollingEnabled(true)
        mAdapter = TLSettingsAdapter(mContext!!, this)
        mRecycleView!!.adapter = mAdapter
    }

    @SuppressLint("NewApi")
    fun loadGif() {
        Glide.with(mContext!!).asGif()
            .load(
                resources.getIdentifier(
                    "setting_animate",
                    "raw", mContext!!.opPackageName
                )
            )
            .into(mGifImageView!!)
    }

    override fun click(position: Int) {

        when (position) {
            0 -> {
                val aIntent = Intent(mContext, AccountSetting::class.java)
                mContext!!.startActivity(aIntent)
            }
            1 -> {
                val aIntent = Intent(mContext, ProfileSettingActivity::class.java)
                mContext!!.startActivity(aIntent)
            }
            2 -> {
                val aIntent = Intent(mContext, PersonalSetting::class.java)
                mContext!!.startActivity(aIntent)
            }
            3 -> {
                val aIntent = Intent(mContext, PrivacySetting::class.java)
                mContext!!.startActivity(aIntent)
            }
            4 -> {
                val aIntent = Intent(mContext, YourFollowersActivity::class.java)
                mContext!!.startActivity(aIntent)
            }
            5 -> {
                val aIntent = Intent(mContext, AboutTrueLife::class.java)
                mContext!!.startActivity(aIntent)
            }
            6 -> {
                val aIntent = Intent(mContext, BlockedListActivity::class.java)
                mContext!!.startActivity(aIntent)
            }
            7 -> {
                val aIntent = Intent(mContext, ReportProblemActivity::class.java)
                mContext!!.startActivity(aIntent)
            }
            8 -> {
                val array = arrayOf("Yes")
                AppDialogs.showBottomMenu(
                    mContext!!,
                    array,
                    object : TLBottomOptionAdapter.Callback {
                        override fun info(option: Int, text: String) {
                            when (option) {
                                0 -> {
                                    logOut()
                                }
                            }
                        }
                    },
                    "Cancel", null, getString(R.string.label_logout_msg)
                )
            }
            9 -> {
                val array = arrayOf("Yes")
                AppDialogs.showBottomMenu(
                    mContext!!,
                    array,
                    object : TLBottomOptionAdapter.Callback {
                        override fun info(option: Int, text: String) {
                            when (option) {
                                0 -> {
                                    deleteAccount()
                                }
                            }
                        }
                    },
                    "Cancel", null, getString(R.string.label_delete_msg)
                )

            }


        }
    }

    fun logOut() {
        AppDialogs.showProgressDialog(mContext!!)

        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, getLogOutAsString())
        AppServices.execute(
            mContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.logout,
            Response::class.java
        )
    }

    fun deleteAccount() {
        AppDialogs.showProgressDialog(mContext!!)

        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, getDeleteAccount())
        AppServices.execute(
            mContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.deleteAccount,
            Response::class.java
        )

    }

    fun getLogOutAsString(): String {
        var aCaseStr = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("login_user_id", user!!.mUserId)
            val jsonParam = JSONObject()
            jsonParam.put("Logout", jsonParam1)
            Log.e("Logout", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            Log.e("Logout", " $aCaseStr")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return aCaseStr
    }

    fun getDeleteAccount(): String {

        var aCaseStr = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", user!!.mUserId)
            jsonParam1.put("active", user!!.isUserActive)
            val jsonParam = JSONObject()
            if (user!!.isUserActive == "1") jsonParam.put(
                "ActiveUser",
                jsonParam1
            ) else jsonParam.put("InactiveUser", jsonParam1)
            Log.e("activeUser", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            Log.e("activeUser", " $aCaseStr")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        return aCaseStr
    }


    override fun onResponse(r: Response?) {

        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.logout.hashCode()) {
                if (r.response!!.isSuccess) {
                    if (FireManager.isLoggedIn()) {
                        SharedPreferencesManager.deleteUser()
                        FireManager.signout()
                    }
                    LocalStorageSP.clearAll(mContext!!)
                    val aIntent = Intent(mContext, SplashActivity::class.java)
                    aIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    mContext!!.startActivity(aIntent)
                    requireActivity().finish()
                } else AppDialogs.showToastDialog(mContext!!, r.response!!.responseMessage!!)
            } else if (r.requestType!! == AppServices.API.deleteAccount.hashCode()) {
                if (r.response!!.isSuccess) {

                    // aDbHelper.truncateDataBase();
                    val aIntent = Intent(mContext, TLSigninActivity::class.java)
                    aIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    mContext!!.startActivity(aIntent)
                    requireActivity().finish()
                } else AppDialogs.showToastDialog(mContext!!, r.response!!.responseMessage!!)
            }
        }
    }

}


