package com.truelife.app.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.squareup.picasso.Picasso
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.adapter.CreateClubFriendsListAdapter
import com.truelife.app.listeners.TLClubEndlessRecyclerViewScrollListener
import com.truelife.app.model.CreateClubFriendsList
import com.truelife.app.model.TLCreateClubModel
import com.truelife.app.model.User
import com.truelife.base.BaseActivity
import com.truelife.base.TLFragmentManager
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.*
import de.hdodenhof.circleimageview.CircleImageView
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import kotlinx.android.synthetic.main.fragment_create_club.*
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * Created by Elango on 04-02-2020.
 */
class CreateClub : BaseActivity(), ResponseListener,
    CreateClubFriendsListAdapter.ClickListener {
    companion object {
        var TAG = CreateClub::class.java.simpleName
    }

    var mContext: Context? = null
    private var myCountryDialogue: TLProgressDialog? = null
    private var myFragmentManager: TLFragmentManager? = null
    private var myHeaderText: TextView? = null
    private var myCloseButton: ImageView? = null
    private var myClubForText: TextView? = null
    private var myRootLayout: LinearLayout? = null
    private var mySelectTypeRG: RadioGroup? = null
    private var myForeverRb: RadioButton? = null
    private var myAdminApprovalRB: RadioButton? = null
    private var myOnlyAdminRB: RadioButton? = null
    private var myInviteRB: RadioButton? = null
    private var myTotalMemberEDT: EditText? = null
    private var mySearchFriendEDT: EditText? = null
    private var myClubNameEDT: EditText? = null
    private var myClubTypeEDT: EditText? = null
    private var myPostChkBox: CheckBox? = null
    private var myShareChkBox: CheckBox? = null
    private var myLikeChkBox: CheckBox? = null
    private var myCommentsChkBox: CheckBox? = null
    private var myAdmittanceHeaderTxt: TextView? = null
    private var myInviteHeaderTxt: TextView? = null
    private var myFriendsHeaderTxt: TextView? = null
    private var myProfileButton: Button? = null
    private var myCreateClubButton: Button? = null
    private var myCircularImage: CircleImageView? = null
    private var FriendStr: StringBuilder? = null
    private var myPostStr = "0"
    private var myShareStr = "0"
    private var myLikeStr = "0"
    private var myCommentsStr = "0"
    private var myRule = ""


    private var myFriendsRecyclerView: RecyclerView? = null
    private var myFriendsMenuRecyclerAdapter: CreateClubFriendsListAdapter? = null
    private var myUserInfo: User? = null

    private var scrollListener: TLClubEndlessRecyclerViewScrollListener? = null
    private var myAddInviteMemberLAY: LinearLayout? = null
    private var myClearTextIMG: ImageView? = null
    var LIST_LOADING_DURATION = 1000
    var mCurrentPage: Int = 1
    private var myPath = java.util.ArrayList<String>()
    private var myProfileFile: File? = null

    var mTotalPage: Int = 1
    var mCreateClubFriendsList: CreateClubFriendsList? = null
    var mFriendsList: ArrayList<CreateClubFriendsList.FriendsList> = arrayListOf()

    private val myFilterValue = ArrayList<CreateClubFriendsList.FriendsList>()
    var list = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_create_club)
        addlist()
        initializeClassAndWidgets()
        getFriends(mCurrentPage)
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.clubfriendsList.hashCode()) {
                if (r.response!!.isSuccess) {
                    mCreateClubFriendsList = r as CreateClubFriendsList
                    mTotalPage = mCreateClubFriendsList!!.total_page!!.toInt()
                    mFriendsList.addAll(mCreateClubFriendsList!!.mData)
                    myFriendsMenuRecyclerAdapter!!.notifyDataSetChanged()
                    Log.e("List", "" + mCreateClubFriendsList!!.mData.size)
                }
            }
        }
    }

    fun addlist() {
        list.add("Public")
        list.add("Friends")
    }

    private fun initializeClassAndWidgets() {
        try {
            myContext = this
            myUserInfo = LocalStorageSP.getLoginUser(myContext)
            myCountryDialogue = TLProgressDialog(myContext)
            myFragmentManager = TLFragmentManager(myContext)
            myRootLayout =
                findViewById<View>(R.id.fragment_crate_club_root_layout) as LinearLayout
            myAddInviteMemberLAY =
                findViewById<View>(R.id.add_invite_LAY) as LinearLayout
            myProfileButton =
                findViewById<View>(R.id.mClubProfilePicBTN) as Button
            myCreateClubButton =
                findViewById<View>(R.id.fragment_create_club_but) as Button
            myClubNameEDT =
                findViewById<View>(R.id.fragment_club_name_edt) as EditText
            // myClubNameEDT.setHint("C L U B"+"   "+"N A M E");
            myClubTypeEDT =
                findViewById<View>(R.id.fragment_club_type_edt) as EditText
            mySearchFriendEDT =
                findViewById<View>(R.id.fragment_search_friend) as EditText
            myCircularImage =
                findViewById<View>(R.id.fragment_circular_image) as CircleImageView
            myHeaderText = findViewById<View>(R.id.fragment_header_text) as TextView
            myCloseButton = findViewById<View>(R.id.close_club_button) as ImageView
            myClubForText = findViewById<View>(R.id.fragment_club_type) as TextView
            mySelectTypeRG =
                findViewById<View>(R.id.fragment_members_type_rg) as RadioGroup
            myForeverRb =
                findViewById<View>(R.id.fragment_open_forever_rb) as RadioButton
            myAdminApprovalRB =
                findViewById<View>(R.id.fragment_approve_by_admin_rb) as RadioButton
            myOnlyAdminRB =
                findViewById<View>(R.id.fragment_only_approve_by_admin_rb) as RadioButton
            myInviteRB =
                findViewById<View>(R.id.fragment_invite_people_rb) as RadioButton
            myPostChkBox = findViewById<View>(R.id.fragment_post_chkbox) as CheckBox
            myShareChkBox = findViewById<View>(R.id.fragment_share_chkbox) as CheckBox
            myLikeChkBox = findViewById<View>(R.id.fragment_like_chkbox) as CheckBox
            myCommentsChkBox =
                findViewById<View>(R.id.fragment_comments_chkbox) as CheckBox
            myFriendsRecyclerView =
                findViewById<View>(R.id.friends_recycler) as RecyclerView
            myAdmittanceHeaderTxt =
                findViewById<View>(R.id.fragment_admittance_header) as TextView
            myInviteHeaderTxt =
                findViewById<View>(R.id.fragment_invite_header) as TextView
            myFriendsHeaderTxt =
                findViewById<View>(R.id.fragment_friends_header) as TextView
            myTotalMemberEDT =
                findViewById<View>(R.id.fragment_approve_by_edt) as EditText
            myClearTextIMG =
                findViewById<View>(R.id.clear_search_text) as ImageView
            myTotalMemberEDT!!.isEnabled = false
            setRecylceview()
            setListener()
            setDefault()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setDefault() {
        try {
            myForeverRb!!.isChecked = true
            myForeverRb!!.setTextColor(ContextCompat.getColor(myContext, R.color.black))
            myPostChkBox!!.isChecked = true
            myShareChkBox!!.isChecked = true
            myLikeChkBox!!.isChecked = true
            myCommentsChkBox!!.isChecked = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setListener() {
        myCloseButton!!.setOnClickListener { finish() }
        myTotalMemberEDT!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {

                if (myTotalMemberEDT!!.text.toString().matches("^0".toRegex())) {
                    myTotalMemberEDT!!.setText("")
                }
            }

            override fun afterTextChanged(arg0: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }
        })
        close_club_button!!.setOnClickListener { finish() }

        mClubProfilePicBTN!!.setOnClickListener {
            selectClubProfilePicture()

        }
        myClubForText!!.setOnClickListener {
            openClubType()
        }

        fragment_club_type_img.setOnClickListener {
            openClubType()
        }

        myClearTextIMG!!.setOnClickListener { mySearchFriendEDT!!.text.clear() }
        mySearchFriendEDT!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {

            }

            override fun afterTextChanged(s: Editable) {
                try {
                    if (s.toString().length > 0) {
                        friendsFilterValue
                        myClearTextIMG!!.visibility = View.VISIBLE
                    } else {
                        updateFriendFilterAdapter(mFriendsList)
                        myClearTextIMG!!.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        })
        myCreateClubButton!!.setOnClickListener {
            try {
                FriendStr = StringBuilder()
                getSelectedFriendsList()
                checkCheckBoxStatus()

                val aEditCheck = validation()
                if (aEditCheck) {
                    loadDetails()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mySelectTypeRG!!.setOnCheckedChangeListener { group, checkedId ->
            try {
                when (checkedId) {
                    R.id.fragment_open_forever_rb -> {
                        myForeverRb!!.setTextColor(
                            ContextCompat.getColor(
                                myContext,
                                R.color.black
                            )
                        )
                        setRadiocolor(myAdminApprovalRB, myOnlyAdminRB, myInviteRB)
                        myTotalMemberEDT!!.isEnabled = false
                        myTotalMemberEDT!!.setText("")
                        myRule = "1"
                    }
                    R.id.fragment_approve_by_admin_rb -> {
                        myAdminApprovalRB!!.setTextColor(
                            ContextCompat.getColor(
                                myContext,
                                R.color.black
                            )
                        )
                        setRadiocolor(myForeverRb, myOnlyAdminRB, myInviteRB)
                        myTotalMemberEDT!!.isEnabled = true
                        myRule = "2"
                    }
                    R.id.fragment_only_approve_by_admin_rb -> {
                        myOnlyAdminRB!!.setTextColor(
                            ContextCompat.getColor(
                                myContext,
                                R.color.black
                            )
                        )
                        setRadiocolor(myForeverRb, myAdminApprovalRB, myInviteRB)
                        myTotalMemberEDT!!.isEnabled = false
                        myTotalMemberEDT!!.setText("")
                        myRule = "3"
                    }
                    R.id.fragment_invite_people_rb -> {
                        myInviteRB!!.setTextColor(
                            ContextCompat.getColor(
                                myContext,
                                R.color.black
                            )
                        )
                        setRadiocolor(myForeverRb, myAdminApprovalRB, myOnlyAdminRB)
                        myTotalMemberEDT!!.isEnabled = false
                        myTotalMemberEDT!!.setText("")
                        myRule = "4"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun openClubType() {
        AppDialogs.showSingleChoice(
            myContext,
            "C L U B  F O R ",
            list,
            object : SingleChoiceAdapter.Callback {
                override fun info(position: Int, text: String) {
                    AppDialogs.hideSingleChoice()
                    myClubForText!!.setText(text)
                }
            }, true
        )
    }

    private fun validation(): Boolean {
        val aRuleCheck = checkRuleStatus()
        val aMemberCheck = checkMemberCount()
        try {
            if (myClubNameEDT!!.text.toString().trim().length == 0) {

                AppDialogs.customOkAction(
                    myContext, null, "Please Enter Club name",
                    null,
                    null,
                    false
                )
                return false
            } else if (myClubTypeEDT!!.text.toString().trim().length == 0) {
                AppDialogs.customOkAction(
                    myContext, null, "Please Enter Club type",
                    null,
                    null,
                    false
                )
                return false
            } else if (myClubForText!!.text.toString().trim().length == 0) {
                AppDialogs.customOkAction(
                    myContext, null, "Please select Club for",
                    null,
                    null,
                    false
                )
                return false
            } else if (!aRuleCheck) {
                AppDialogs.customOkAction(
                    myContext, null, "Please Enter total members",
                    null,
                    null,
                    false
                )
                return false
            } else if (!aMemberCheck) {
                AppDialogs.customOkAction(
                    myContext, null, "Please Enter maximum approval limit ",
                    null,
                    null,
                    false
                )
                return false
            } else {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun checkRuleStatus(): Boolean {
        try {
            return if (myRule == "2") {
                myTotalMemberEDT!!.text.toString().length != 0
            } else if (myRule == "2" && myTotalMemberEDT!!.text.toString() == "0") {
                myTotalMemberEDT!!.text.toString() != "0"
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun checkMemberCount(): Boolean {
        try {
            return if (myTotalMemberEDT!!.text.toString().length != 0) {
                myTotalMemberEDT!!.text.toString() != "0"
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun checkCheckBoxStatus() {
        try {
            myPostStr = if (myPostChkBox!!.isChecked) "1" else "0"
            myShareStr = if (myShareChkBox!!.isChecked) "1" else "0"
            myLikeStr = if (myLikeChkBox!!.isChecked) "1" else "0"
            myCommentsStr = if (myCommentsChkBox!!.isChecked) "1" else "0"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setRadiocolor(
        myAdminApprovalRB: RadioButton?,
        myOnlyAdminRB: RadioButton?,
        myInviteRB: RadioButton?
    ) {
        try {
            myAdminApprovalRB!!.setTextColor(ContextCompat.getColor(myContext, R.color.dark_grey))
            myOnlyAdminRB!!.setTextColor(ContextCompat.getColor(myContext, R.color.dark_grey))
            myInviteRB!!.setTextColor(ContextCompat.getColor(myContext, R.color.dark_grey))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*
     * Search Function Process
     */
    private val friendsFilterValue: Unit
        private get() {
            myFilterValue.clear()
            try {
                for (i in mFriendsList.indices) {
                    if (mySearchFriendEDT!!.text.toString()
                            .trim { it <= ' ' }.length <= mFriendsList.get(i).fullname!!.length
                    ) {
                        if (mFriendsList[i].fullname!!.toLowerCase().trim { it <= ' ' }.startsWith(
                                mySearchFriendEDT!!.text.toString().toLowerCase().trim { it <= ' ' }
                            )
                        ) {
                            val slistPojoCIL = mFriendsList[i]
                            myFilterValue.add(slistPojoCIL)
                        }
                    }
                }
                updateFriendFilterAdapter(myFilterValue)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private fun updateFriendFilterAdapter(myFilterValue: ArrayList<CreateClubFriendsList.FriendsList>) {
        myFriendsMenuRecyclerAdapter!!.updateAdapter(myFilterValue)
        myFriendsMenuRecyclerAdapter!!.notifyDataSetChanged()
    }

    public override fun onResume() {
        super.onResume()
    }

    private fun addClub() { /*  try {


            User aUserInfo = myUserInfo;
            String aPage = "1";

            TLCreateClubModel aClub = new TLCreateClubModel();
            aClub.setClubname(myClubNameEDT.getText().toString().trim());
            aClub.setClub_type(myClubTypeEDT.getText().toString().trim());
            aClub.setClub_for(myClubForStr);
            aClub.setLogin_user_id(aUserInfo.getUser_id());
            aClub.setClub_members(FriendStr.toString());


            aClub.setRule(myRule);
            aClub.setPosting(myPostStr);
            aClub.setShare(myShareStr);
            aClub.setComment(myCommentsStr);
            aClub.setLike(myLikeStr);
            aClub.setMaximum_members(myTotalMemberEDT.getText().toString().length() > 0 ? myTotalMemberEDT.getText().toString().trim() : "0");
            aUserInfo.getUser_id();


            if (checkInternet()) {
                try {

                    */
/*myWebservice.postProfileData(myClubProfileFile, aClub, new TLWebServiceHttpCallBack() {
                        @Override
                        public void onSuccess(JSONObject aJsonObject) {
                            try {
                                if (aJsonObject.has(RESPONSE)) {
                                    String aResponseStr = aJsonObject.getString(RESPONSE);
                                    JSONObject aJsonInObject = new JSONObject(aResponseStr);

                                    if (aJsonInObject.getString(RESPONSE_CODE).equals(RESPONSE_CODE_FAILURE)) {
                                        TLHelper.showMaterialAlertDialog(myContext, aJsonInObject.getString(RESPONSE_MSG));
                                    } else if (aJsonInObject.getString(RESPONSE_CODE).equals(RESPONSE_CODE_SUCCESS)) {
                                        showMaterialAlertDialog(myContext, aJsonInObject.getString(RESPONSE_MSG));
                                    }
                                } else {
                                    TLHelper.showMaterialAlertDialog(myContext, ALERT_SOMETHING_WENT_WRONG);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailer(String object) {
                            TLHelper.showMaterialAlertDialog(myContext, Toast_time_out);
                        }
                    });*/
/*
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private fun loadDetails() {
        if (checkInternet()) {
            var club = TLCreateClubModel()


            club.setClubname(getETValue(myClubNameEDT))
            club.setClub_type(getETValue(myClubTypeEDT))
            club.setClub_for(
                if (getTXTValue(myClubForText).equals("public", true))
                    "1"
                else if (getTXTValue(myClubForText).equals("Friends", true))
                    "2"
                else ""
            )
            club.setLogin_user_id(myUserInfo!!.mUserId)
            club.setClub_members(FriendStr.toString())
            club.setRule(myRule)
            club.setPosting(if (myPostChkBox!!.isChecked) "1" else "0")
            club.setShare(if (myShareChkBox!!.isChecked) "1" else "0")
            club.setComment(if (myCommentsChkBox!!.isChecked) "1" else "0")
            club.setLike(if (myLikeChkBox!!.isChecked) "1" else "0")
            club.setMaximum_members(
                if (myTotalMemberEDT!!.text.toString().length > 0) myTotalMemberEDT!!.text.toString()
                    .trim { it <= ' ' } else "0")
            createClub(club)
        } else {

        }
    }

    override fun clickListener() {}
    override fun init() {}

    override fun onClick(tt: Any, position: Int) {}
    override fun onLongClick(
        view: View?,
        position: Int
    ) {
    } //asyn task to sign in

    fun getSelectedFriendsList() {
        if (!mFriendsList.isNullOrEmpty() && mFriendsList.size > 0) {
            for (i in mFriendsList.indices) {
                if (mFriendsList[i].isSelected)
                    FriendStr!!.append(mFriendsList[i].id).append(",")
            }
            if (FriendStr!!.length > 0) {
                FriendStr!!.setLength(FriendStr!!.length - 1)
            }
        } else {
            FriendStr.toString()
        }
    }


    override fun onSelected(type: Boolean, data: CreateClubFriendsList.FriendsList) {
        var i = 0
        for (friendsList in mFriendsList) {
            if (friendsList.id!!.equals(data.id, true)) {
                mFriendsList.get(i).isSelected = type
                return
            }
            i++
        }

    }


    fun selectClubProfilePicture() {

        FilePickerBuilder.instance.setMaxCount(1)
            .setActivityTheme(R.style.FilePickerTheme)
            .enableVideoPicker(false)
            .enableCameraSupport(true)
            .showGifs(false)
            .showFolderView(true)
            .enableImagePicker(true)
            .setCameraPlaceholder(R.drawable.ic_action_camera)
            .pickPhoto(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
            try {
                if (!data!!.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA).isEmpty()) {
                    myPath = java.util.ArrayList(
                        data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)
                    )
                    myProfileFile =  FileCompression.compressImage(myContext, File(myPath.get(0)))
                    //myProfileFile = File(myPath.get(0))
                    var uri: Uri? = null
                  // myProfileFile = Compressor(myContext).compressToFile(File(myPath.get(0)))
                    uri = Uri.fromFile(myProfileFile)
                    Picasso.get().load(uri!!).resize(750, 750).centerCrop()
                        .into(myCircularImage)


                } else {
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getFriends(aPage: Int) {

        AppDialogs.showProgressDialog(myContext)
        val result = Helper.GenerateEncrptedUrl(
            BuildConfig.API_URL,
            getClubFriendsListCaseString(aPage)!!
        )
        AppServices.execute(
            myContext, this,
            result,
            Request.Method.POST,
            AppServices.API.clubfriendsList,
            CreateClubFriendsList::class.java
        )
    }

    private fun getClubFriendsListCaseString(
        aPage: Int
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("club_id", "")
            jsonParam1.put("page_no", aPage)
            jsonParam1.put("login_user_id", myUserInfo!!.mUserId!!)
            val jsonParam = JSONObject()
            jsonParam.put("ClubFriendsList", jsonParam1)
            Log.e("ClubFriendsList", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    fun setRecylceview() {
        val layoutManager = LinearLayoutManager(mContext)
        myFriendsRecyclerView!!.layoutManager = layoutManager
        myFriendsRecyclerView!!.setHasFixedSize(true)
        myFriendsMenuRecyclerAdapter = CreateClubFriendsListAdapter(myContext, mFriendsList, this)
        myFriendsRecyclerView!!.adapter = myFriendsMenuRecyclerAdapter

        myFriendsRecyclerView!!.isNestedScrollingEnabled = true
        scrollListener =
            object : TLClubEndlessRecyclerViewScrollListener(layoutManager) {
                override fun onLoadMore(
                    page: Int,
                    totalItemsCount: Int,
                    view: RecyclerView
                ) { // Triggered only when new data needs to be appended to the list
// Add whatever code is needed to append new items to the bottom of the list
                    if (mTotalPage > page) {
                        val updateHandler = Handler()
                        val runnable = Runnable {
                            mCurrentPage++
                            getFriends(mCurrentPage)
                        }
                        updateHandler.postDelayed(runnable, LIST_LOADING_DURATION.toLong())
                    }
                }
            }
        myFriendsRecyclerView!!.addOnScrollListener(scrollListener!!)
    }

    fun createClub(aClub: TLCreateClubModel) {
        myContext.runOnUiThread(java.lang.Runnable {
            AppDialogs.showProgressDialog(myContext)
        })
        val result =
            Helper.GenerateEncrptedUrl(BuildConfig.API_URL, getCeateClubCaseString(aClub)!!)


        var httpclient: HttpClient? = DefaultHttpClient()
        val httppost = HttpPost(result)


        val multipartEntity: MultipartEntityBuilder = MultipartEntityBuilder.create()
        if (myProfileFile != null)
            multipartEntity.addBinaryBody("image", myProfileFile)

        Thread({
            httppost.setEntity(multipartEntity.build());
            val httpResponse = httpclient!!.execute(httppost)
            val httpEntity = httpResponse.entity

            var aJsonResponse = EntityUtils.toString(httpEntity)
            Log.e("Report Response --> ", aJsonResponse)
            runOnUiThread({


                if (aJsonResponse != "") { //  TLHelper.hideDialog(myProgressDialog);
                    try {
                        var aJsonObject = JSONObject(aJsonResponse)
                        Log.e("Response", aJsonObject.toString())
                        AppDialogs.hideProgressDialog()
                        if (aJsonObject.has("response")) {
                            var response_msg: JSONObject = aJsonObject.getJSONObject("response")
                            var ss = response_msg.getString("response_msg")
                            var responsecode = response_msg.getString("response_code")
                            AppDialogs.showToastDialog(myContext, ss)
                            if (responsecode.equals("1")) {
                                finish()
                            }


                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {

                }
            })
        }).start()


    }

    private fun getCeateClubCaseString(aClub: TLCreateClubModel): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("club_name", aClub.getClubname())
            jsonParam1.put("club_type", aClub.getClub_type())
            jsonParam1.put("club_for", aClub.getClub_for())
            jsonParam1.put("login_user_id", aClub.getLogin_user_id())
            jsonParam1.put("club_members", aClub.getClub_members())
            jsonParam1.put("rule", aClub.getRule())
            jsonParam1.put("posting", aClub.getPosting())
            jsonParam1.put("like", aClub.getLike())
            jsonParam1.put("share", aClub.getShare())
            jsonParam1.put("comment", aClub.getComment())
            jsonParam1.put("maximum_members", aClub.getMaximum_members())
            //  jsonParam1.put("maximum_members", aClub.getMaximum_members());
            val jsonParam = JSONObject()
            jsonParam.put("CreateClub", jsonParam1)
            Log.e("CreateClub", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }
}