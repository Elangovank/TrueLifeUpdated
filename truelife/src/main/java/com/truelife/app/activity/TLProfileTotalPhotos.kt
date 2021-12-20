package com.truelife.app.activity

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.adapter.TLProfileTotalPhotosAdapter
import com.truelife.app.constants.TLConstant
import com.truelife.app.model.Profile
import com.truelife.base.BaseActivity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.util.AppDialogs
import com.truelife.util.EndlessRecyclerViewScrollListener
import com.truelife.util.Helper
import kotlinx.android.synthetic.main.app_chat_header.*
import org.json.JSONObject


/**
 * Created by Elango on 24-02-2020.
 */

class TLProfileTotalPhotos : BaseActivity(), ResponseListener {

    private var mContext: Context? = null

    private lateinit var mPhotosRecycler: RecyclerView
    private lateinit var mAdapter: TLProfileTotalPhotosAdapter
    var mTotal = 0
    var mPage = 1
    var mUserId = ""
    val mData = ArrayList<Profile.Photos>()
    private lateinit var mProgress: ProgressBar

    companion object {
        var TAG: String = TLProfileTotalPhotos::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_total_photos)
        mContext = this
        init()
    }

    override fun init() {

        app_header_title.text = "PHOTOS"
        app_header_back_arrow.visibility = View.VISIBLE
        app_header_back_arrow.setOnClickListener { finish() }

        initBunble()

        mPhotosRecycler = findViewById(R.id.fragment_horizontal_recycler_member)
        mProgress = findViewById(R.id.progress_bar)
        initRecycler()
        getData(true)
    }

    private fun initBunble() {
        val intent = intent
        if (intent != null) {
            mUserId = intent.extras?.getString("user_id")!!
        }
    }

    private fun initRecycler() {
        val layoutManager = GridLayoutManager(mContext, 3)
        /*layoutManager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0 || position % 1 == 0 || position % 2 == 0) 1 else 3
            }
        }*/
        mPhotosRecycler.layoutManager = layoutManager
        mPhotosRecycler.setHasFixedSize(true)
        mAdapter = TLProfileTotalPhotosAdapter(mContext!!, mData)
        mPhotosRecycler.adapter = mAdapter

        val mListener = object : EndlessRecyclerViewScrollListener(layoutManager, 5) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                try {
                    if (mPage < mTotal) {
                        mPage++
                        getData(false)
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

        mPhotosRecycler.addOnScrollListener(mListener)
    }

    override fun clickListener() {

    }

    private fun getData(show: Boolean) {
        if (checkInternet()) {
            if (show)
                AppDialogs.showProgressDialog(mContext!!)
            else mProgress.visibility = View.VISIBLE

            val mCase = getParam()
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
            AppServices.execute(
                mContext!!, this,
                result,
                Request.Method.POST,
                AppServices.API.PhotosList,
                Profile.Photos::class.java
            )
        }
    }

    private fun getParam(): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("farmid", mUserId)
            jsonObject.put("page_no", mPage)

            val jsonParam = JSONObject()
            jsonParam.put("PhotosList", jsonObject)

            Log.i("PhotosList --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        mProgress.visibility = View.GONE
        if (r != null) {
            if (r.requestType!! == AppServices.API.PhotosList.hashCode()) {
                if (r.response!!.isSuccess) {
                    mTotal = (r as Profile.Photos).total_page.toInt()
                    if (mPage == 1)
                        mData.clear()
                    mData.addAll(r.mData)
                    mAdapter.notifyDataSetChanged()
                } else AppDialogs.showToastDialog(mContext!!, r.response!!.responseMessage!!)
            }
        } else AppDialogs.customOkAction(
            mContext!!, null, TLConstant.SERVER_NOT_REACH, null, null, false
        )
    }

}