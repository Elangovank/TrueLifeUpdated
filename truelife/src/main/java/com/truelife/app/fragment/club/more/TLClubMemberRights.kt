package com.truelife.app.fragment.club.more

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.constants.TLConstant
import com.truelife.app.fragment.club.TLClubDetails
import com.truelife.app.model.Club
import com.truelife.app.model.User
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import com.truelife.util.Utility
import org.json.JSONObject

/**
 * Created by Elango on 20-02-2020.
 */

class TLClubMemberRights(var mClubs: Club.Clubs) : DialogFragment(), View.OnClickListener,
    ResponseListener {

    lateinit var mContext: Context
    lateinit var mView: View

    lateinit var mPostLay: LinearLayout
    lateinit var mLikeLay: LinearLayout
    lateinit var mShareLay: LinearLayout
    lateinit var mCommentLay: LinearLayout

    lateinit var mPost: CheckBox
    lateinit var mLike: CheckBox
    lateinit var mShare: CheckBox
    lateinit var mComment: CheckBox

    lateinit var mSave: Button
    lateinit var mClose: ImageButton

    companion object {
        var TAG: String = TLClubMemberRights::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_club_by_you_member_rights, container, false)
        mContext = activity!!
        initView()
        return mView
    }

    private fun initView() {
        mPostLay = mView.findViewById(R.id.post_layout)
        mLikeLay = mView.findViewById(R.id.like_layout)
        mShareLay = mView.findViewById(R.id.share_layout)
        mCommentLay = mView.findViewById(R.id.comments_layout)

        mPost = mView.findViewById(R.id.checkbox_post)
        mLike = mView.findViewById(R.id.checkbox_like)
        mShare = mView.findViewById(R.id.checkbox_share)
        mComment = mView.findViewById(R.id.checkbox_comments)

        mSave = mView.findViewById(R.id.privacy_post_btn)
        mClose = mView.findViewById(R.id.close_club_button)

        initData()

        clickListener()
    }

    private fun initData() {
        mPost.isChecked = mClubs.clubRights!!.posting == "1"
        mLike.isChecked = mClubs.clubRights!!.like == "1"
        mShare.isChecked = mClubs.clubRights!!.share == "1"
        mComment.isChecked = mClubs.clubRights!!.comment == "1"
    }

    private fun clickListener() {
        mPostLay.setOnClickListener(this)
        mLikeLay.setOnClickListener(this)
        mShareLay.setOnClickListener(this)
        mCommentLay.setOnClickListener(this)
        mSave.setOnClickListener(this)
        mClose.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            mClose -> dismiss()
            mSave -> {
                if (Utility.isInternetAvailable(mContext, false)) {
                    AppDialogs.showProgressDialog(mContext)

                    val mCase = getRightsParam()
                    val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
                    AppServices.execute(
                        mContext, this,
                        result,
                        Request.Method.POST,
                        AppServices.API.updateProfile,
                        User::class.java
                    )
                }
            }
        }
    }

    private fun getRightsParam(): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("club_id", mClubs.id)
            jsonParam1.put("posting", if (mPost.isChecked) 1 else 0)
            jsonParam1.put("like", if (mLike.isChecked) 1 else 0)
            jsonParam1.put("share", if (mShare.isChecked) 1 else 0)
            jsonParam1.put("comment", if (mComment.isChecked) 1 else 0)
            val jsonParam = JSONObject()
            jsonParam.put("ChangeClubRights", jsonParam1)
            Log.e("ChangeClubRights", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.updateProfile.hashCode()) {
                if (r.response!!.isSuccess) {
                    AppDialogs.customOkAction(
                        mContext,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        object : AppDialogs.ConfirmListener {
                            override fun yes() {
                                dismiss()
                                (mContext as TLClubDetails).finishActivity()
                            }

                        }, false
                    )

                } else
                    AppDialogs.showToastDialog(mContext, r.response!!.responseMessage!!)
            }
        } else AppDialogs.customOkAction(
            mContext,
            null,
            TLConstant.SERVER_NOT_REACH,
            null,
            null,
            false
        )
    }

}