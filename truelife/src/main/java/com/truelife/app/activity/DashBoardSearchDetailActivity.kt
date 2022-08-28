package com.truelife.app.activity

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.truelife.BuildConfig
import com.truelife.FeedMenuClickListener
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.VideoPlayerRecyclerAdapter
import com.truelife.app.VideoPlayerRecyclerView
import com.truelife.app.VideoPreviewActivity
import com.truelife.app.activity.feedpost.FeedDetailActivity
import com.truelife.app.adapter.SearchClubListAdapter
import com.truelife.app.adapter.SearchPeopleListAdapter
import com.truelife.app.constants.TLConstant
import com.truelife.app.fragment.ReportDailogFragment
import com.truelife.app.listeners.Feedistener
import com.truelife.app.model.*
import com.truelife.app.viewer.ClubMultiSelectionAdapter
import com.truelife.base.BaseActivity
import com.truelife.base.TLFragmentManager
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.*
import kotlinx.android.synthetic.main.activity_dash_board_search_detail.*
import org.json.JSONObject

class DashBoardSearchDetailActivity : BaseActivity(), FeedClickListener, Feedistener,
    FeedMenuClickListener, ClubMultiSelectionAdapter.Callback, ResponseListener {
    var aType = ""
    var aSearchText = ""
    lateinit var myUserInfo: User
    var myIsFilter = "1"
    var myCurrent_city_id = "0"
    var myCountyId = "0"
    var myStateId = "0"
    var myPincode = "0"
    var isFriend = "1"
    var isFriendofFriend = "1"
    var mSelectedClubs = ""
    var myPage = 1
    var myFilterText = ""
    var mTotalPages = 0
    var mPeopleListModel: PeopleListModel = PeopleListModel()
    var mClubListModel: SearchClubListModel = SearchClubListModel()
    var mPeopleAdapter: SearchPeopleListAdapter? = null
    var mClubAdapter = SearchClubListAdapter(this, mClubListModel)
    lateinit var mRecyclerView: VideoPlayerRecyclerView
    lateinit var mList: ArrayList<PublicFeedModel.FeedList>
    var mClubList = ArrayList<ClubListShareModel.ClubList>()
    lateinit var mClubListAdapter: ClubMultiSelectionAdapter
    lateinit var mAdapter: VideoPlayerRecyclerAdapter
    lateinit var mContext: Context
    var mAllCheckBok: CheckBox? = null
    var isFirst = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board_search_detail)
        mContext = this
        myUserInfo = LocalStorageSP.getLoginUser(this)
        mRecyclerView = findViewById(R.id.publicRv)
        common_back_arrow.setOnClickListener {
            finish()
        }

        myCountyId = myUserInfo.mCountryId ?: "0"
        myStateId = myUserInfo.mStateId ?: "0"
        myPincode = myUserInfo.mPincode ?: "0"
        myCurrent_city_id = myUserInfo.mCurrentCityId ?: "0"

        LocalBroadcastManager.getInstance(myContext)
            .registerReceiver(
                myBroadcastReceiver,
                IntentFilter(TLConstant.VIDEO_SEEK_POSITION_SEARCH)
            )


        getInentValues()
        val layoutManager = LinearLayoutManager(this)
        mList = ArrayList()
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.setMediaObjects(mList)
        mRecyclerView.setFragmentActivity(this, TLFragmentManager(this))
        mRecyclerView.setInterface(this)
        mAdapter = VideoPlayerRecyclerAdapter(mList, initGlide()!!, this, this, aSearchText, "0")
        mRecyclerView.adapter = mAdapter


        val listener = object : EndlessRecyclerViewScrollListener(layoutManager, 2) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                try {
                    if (myPage < mTotalPages) {
                        mAdapter.showBottomLoading()
                        myPage++
                        loadData()
                    } else {
                        mAdapter.stopBottomLoading()
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

        mRecyclerView.addOnScrollListener(listener)


        filter.setOnClickListener {
            if (aType == "clubs_post") {
                if (checkInternet())
                    initClubList()
            } else
                alertDialogSearchFilter("Filter Type")
        }

        loadData()
    }

    override fun clickListener() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun init() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun initClubList() {
        AppDialogs.showProgressDialog(this)
        val mUser = LocalStorageSP.getLoginUser(this)
        val mCase = AppDialogs.GetUserClubDetailsCase(mUser.mUserId!!, "")
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
        AppServices.execute(
            this, object : ResponseListener {
                override fun onResponse(r: Response?) {
                    AppDialogs.hideProgressDialog()
                    if (r != null) {
                        if (r.response!!.isSuccess) {
                            mClubList.clear()
                            mClubList.addAll((r as ClubListShareModel).clubList!!)
                            if (mClubList.isNotEmpty())
                                alertDialogSearchFilter("Filter Type")
                            else AppDialogs.showToastDialog(
                                mContext,
                                r.response!!.responseMessage!!
                            )
                        }
                    }
                }
            },
            result,
            Request.Method.POST,
            AppServices.API.ClubListShare,
            ClubListShareModel::class.java
        )

    }


    private fun initGlide(): RequestManager? {
        val options: RequestOptions = RequestOptions()
            .placeholder(R.drawable.bg_border_view_white)
            .error(R.drawable.bg_border_view_white)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
        return Glide.with(this)
            .setDefaultRequestOptions(options)
    }

    private fun getInentValues() {
        val intent = intent
        aType = intent.getStringExtra("type")!!
        aSearchText = intent.getStringExtra("search_text")!!

        frament_feed_search_Edt.text = aSearchText.toEditable()

        when (aType) {
            "people" -> {
                header_name.text = getString(R.string.label_people)
                fragment_feed_search_post_type_IMG.setImageResource(R.drawable.people)
                fragment_feed_search_post_type_TXT.text = getString(R.string.label_people)
                filter.visibility = View.VISIBLE
                recycler.visibility = View.VISIBLE
                publicRv.visibility = View.GONE
            }
            "friends" -> {
                header_name.text = getString(R.string.label_friends)
                fragment_feed_search_post_type_IMG.setImageResource(R.drawable.clubs_main_icon)
                fragment_feed_search_post_type_TXT.text = getString(R.string.label_friends)
                filter.visibility = View.VISIBLE
                recycler.visibility = View.VISIBLE
                publicRv.visibility = View.GONE
            }
            "clubs" -> {
                header_name.text = getString(R.string.label_club)
                fragment_feed_search_post_type_IMG.setImageResource(R.drawable.clubs_friend_icon)
                fragment_feed_search_post_type_TXT.text = getString(R.string.label_club)
                filter.visibility = View.INVISIBLE
                recycler.visibility = View.VISIBLE
                publicRv.visibility = View.GONE
            }
            "public_post" -> {
                header_name.text = getString(R.string.labe_post)
                fragment_feed_search_post_type_IMG.setImageResource(R.drawable.ic_public_new)
                fragment_feed_search_post_type_TXT.text = getString(R.string.label_by_public)
                filter.visibility = View.VISIBLE
                recycler.visibility = View.GONE
                publicRv.visibility = View.VISIBLE
            }
            "friends_post" -> {
                header_name.text = getString(R.string.labe_post)
                fragment_feed_search_post_type_IMG.setImageResource(R.drawable.clubs_main_icon)
                fragment_feed_search_post_type_TXT.text = getString(R.string.label_by_friends)
                filter.visibility = View.VISIBLE
                recycler.visibility = View.GONE
                publicRv.visibility = View.VISIBLE
            }
            "clubs_post" -> {
                header_name.text = getString(R.string.labe_post)
                fragment_feed_search_post_type_IMG.setImageResource(R.drawable.clubs_friend_icon)
                fragment_feed_search_post_type_TXT.text = getString(R.string.label_by_clubs)
                filter.visibility = View.VISIBLE
                recycler.visibility = View.GONE
                publicRv.visibility = View.VISIBLE
            }
        }
    }

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)


    private fun alertDialogSearchFilter(aName: String) {
        val aDlg = Dialog(this)
        val window = aDlg.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.CENTER
        wlp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window.attributes = wlp
        aDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        aDlg.setTitle(aName)
        aDlg.setContentView(R.layout.search_filter_new)
        aDlg.setCanceledOnTouchOutside(true)
        aDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val myShowImage =
            aDlg.findViewById<View>(R.id.first_page) as LinearLayout
        myShowImage.visibility = View.VISIBLE

        val aPublicFilter =
            aDlg.findViewById<View>(R.id.search_filter_public_filter) as LinearLayout
        val aFriendsFilter =
            aDlg.findViewById<View>(R.id.search_filter_friends_filter) as LinearLayout

        val aPeopleFilter =
            aDlg.findViewById<View>(R.id.search_filter_people_filter_option) as LinearLayout
        val aPeoplePlaceName =
            aDlg.findViewById<View>(R.id.search_filter_people_search_place) as EditText
        if (!myFilterText?.isEmpty())
            aPeoplePlaceName.setText(myFilterText)
        val aShareNowBtn =
            aDlg.findViewById<View>(R.id.share_now_btn) as Button
        mAllCheckBok =
            aDlg.findViewById<View>(R.id.search_filter_all_chk) as CheckBox
        val aStreetChk =
            aDlg.findViewById<View>(R.id.search_filter_street_chk) as CheckBox
        val aAreaChk =
            aDlg.findViewById<View>(R.id.search_filter_area_chk) as CheckBox
        val aCityChK =
            aDlg.findViewById<View>(R.id.search_filter_city_chk) as CheckBox
        val aStateChk =
            aDlg.findViewById<View>(R.id.search_filter_state_chk) as CheckBox
        val aCountryChk =
            aDlg.findViewById<View>(R.id.search_filter_country_chk) as CheckBox
        val aFriends =
            aDlg.findViewById<View>(R.id.search_filter_friends_chk) as CheckBox
        val aFriendsofFriendsChk =
            aDlg.findViewById<View>(R.id.search_filter_friends_of_friends_chk) as CheckBox
        val aClubList = aDlg.findViewById<RecyclerView>(R.id.club_list)

        aDlg.show()

        when (aType) {
            "people" -> {
                aPublicFilter.visibility = View.VISIBLE
                aFriendsFilter.visibility = View.GONE
                aPeopleFilter.visibility = View.VISIBLE
                aClubList.visibility = View.GONE
                mPeopleListModel.peoplesList!!.clear()
                mPeopleListModel.totalPage = 0
            }
            "clubs" -> {
                mClubListModel.totalPage = 0
                mClubListModel.clubList!!.clear()
            }
            "friends" -> {
                aPublicFilter.visibility = View.VISIBLE
                aFriendsFilter.visibility = View.GONE
                aPeopleFilter.visibility = View.VISIBLE
                aClubList.visibility = View.GONE
                mPeopleListModel.peoplesList!!.clear()
                mPeopleListModel.totalPage = 0
            }

            "public_post" -> {
                aPeopleFilter.visibility = View.GONE
                aPublicFilter.visibility = View.VISIBLE
                aFriendsFilter.visibility = View.GONE
                aClubList.visibility = View.GONE
                mTotalPages = 0
                mList.clear()
            }
            "friends_post" -> {
                aPeopleFilter.visibility = View.GONE
                aPublicFilter.visibility = View.GONE
                aFriendsFilter.visibility = View.VISIBLE
                aClubList.visibility = View.GONE
                mTotalPages = 0
                mList.clear()
            }
            "clubs_post" -> {
                aPeopleFilter.visibility = View.GONE
                aPublicFilter.visibility = View.GONE
                aFriendsFilter.visibility = View.GONE
                aClubList.visibility = View.VISIBLE
                initAdapter(aClubList)
                mTotalPages = 0
                mList.clear()
            }
        }

        if (aType != "clubs_post") {

            when (aType) {
                "public_post" -> {
                    aCountryChk.isChecked = myCountyId != "0"
                    aStateChk.isChecked = myStateId != "0"
                    aAreaChk.isChecked = myPincode != "0"
                    aCityChK.isChecked = myCurrent_city_id != "0"

                    mAllCheckBok!!.isChecked =
                        aCountryChk.isChecked && aStateChk.isChecked && aAreaChk.isChecked && aCityChK.isChecked

                    aCountryChk.setOnClickListener {
                        mAllCheckBok!!.isChecked =
                            aCountryChk.isChecked && aStateChk.isChecked && aAreaChk.isChecked && aCityChK.isChecked
                    }

                    aStateChk.setOnClickListener {
                        mAllCheckBok!!.isChecked =
                            aCountryChk.isChecked && aStateChk.isChecked && aAreaChk.isChecked && aCityChK.isChecked
                    }

                    aAreaChk.setOnClickListener {
                        mAllCheckBok!!.isChecked =
                            aCountryChk.isChecked && aStateChk.isChecked && aAreaChk.isChecked && aCityChK.isChecked
                    }

                    aCityChK.setOnClickListener {
                        mAllCheckBok!!.isChecked =
                            aCountryChk.isChecked && aStateChk.isChecked && aAreaChk.isChecked && aCityChK.isChecked
                    }

                    mAllCheckBok!!.setOnClickListener {
                        aCountryChk.isChecked = mAllCheckBok!!.isChecked
                        aStateChk.isChecked = mAllCheckBok!!.isChecked
                        aAreaChk.isChecked = mAllCheckBok!!.isChecked
                        aCityChK.isChecked = mAllCheckBok!!.isChecked
                    }
                }
                "friends_post" -> {
                    aFriends.isChecked = isFriend != "0"
                    aFriendsofFriendsChk.isChecked = isFriendofFriend != "0"

                    mAllCheckBok!!.isChecked = aFriends.isChecked && aFriendsofFriendsChk.isChecked

                    aFriends.setOnClickListener {
                        mAllCheckBok!!.isChecked =
                            aFriends.isChecked && aFriendsofFriendsChk.isChecked
                    }

                    aFriendsofFriendsChk.setOnClickListener {
                        mAllCheckBok!!.isChecked =
                            aFriends.isChecked && aFriendsofFriendsChk.isChecked
                    }

                    mAllCheckBok!!.setOnClickListener {
                        aFriends.isChecked = mAllCheckBok!!.isChecked
                        aFriendsofFriendsChk.isChecked = mAllCheckBok!!.isChecked
                    }
                }
                "people" -> {
                    aCountryChk.isChecked = myCountyId != "0"
                    aStateChk.isChecked = myStateId != "0"
                    aAreaChk.isChecked = myPincode != "0"
                    aCityChK.isChecked = myCurrent_city_id != "0"

                    mAllCheckBok!!.isChecked =
                        aCountryChk.isChecked && aStateChk.isChecked && aAreaChk.isChecked && aCityChK.isChecked

                    aCountryChk.setOnClickListener {
                        mAllCheckBok!!.isChecked =
                            aCountryChk.isChecked && aStateChk.isChecked && aAreaChk.isChecked && aCityChK.isChecked
                    }

                    aStateChk.setOnClickListener {
                        mAllCheckBok!!.isChecked =
                            aCountryChk.isChecked && aStateChk.isChecked && aAreaChk.isChecked && aCityChK.isChecked
                    }

                    aAreaChk.setOnClickListener {
                        mAllCheckBok!!.isChecked =
                            aCountryChk.isChecked && aStateChk.isChecked && aAreaChk.isChecked && aCityChK.isChecked
                    }

                    aCityChK.setOnClickListener {
                        mAllCheckBok!!.isChecked =
                            aCountryChk.isChecked && aStateChk.isChecked && aAreaChk.isChecked && aCityChK.isChecked
                    }

                    mAllCheckBok!!.setOnClickListener {
                        aCountryChk.isChecked = mAllCheckBok!!.isChecked
                        aStateChk.isChecked = mAllCheckBok!!.isChecked
                        aAreaChk.isChecked = mAllCheckBok!!.isChecked
                        aCityChK.isChecked = mAllCheckBok!!.isChecked
                    }
                }
            }

        } else {
            if (mSelectedClubs.isNotEmpty())
                initPreviousSelection(mSelectedClubs)
            else initClubCheck(true)
            mAllCheckBok!!.setOnClickListener {
                initClubCheck(mAllCheckBok!!.isChecked)
            }
        }

        aShareNowBtn.setOnClickListener {
            if ((aType == "clubs_post" && mClubListAdapter.getSelectedClubs().isNotEmpty()) ||
                (aType == "public_post" && (mAllCheckBok!!.isChecked || aStateChk.isChecked || aAreaChk.isChecked || aCityChK.isChecked || aStateChk.isChecked ||
                        aCountryChk.isChecked)) ||
                (aType == "people" && (mAllCheckBok!!.isChecked || aStateChk.isChecked || aAreaChk.isChecked || aCityChK.isChecked || aStateChk.isChecked ||
                        aCountryChk.isChecked) || aPeoplePlaceName.text.isNotEmpty()) ||
                (aType == "friends_post" && (aFriends.isChecked || aFriendsofFriendsChk.isChecked ||
                        aPeoplePlaceName.text.toString().isNotEmpty()))
            ) {

                myCountyId = if (aCountryChk.isChecked) myUserInfo.mCountryId!! else "0"
                myStateId = if (aStateChk.isChecked) myUserInfo.mStateId!! else "0"
                myPincode = if (aAreaChk.isChecked) myUserInfo.mPincode!! else "0"
                myCurrent_city_id = if (aCityChK.isChecked) myUserInfo.mCurrentCityId!! else "0"

                isFriend = if (aFriends.isChecked) "1" else "0"
                isFriendofFriend = if (aFriendsofFriendsChk.isChecked) "1" else "0"

                if (aType == "clubs_post")
                    mSelectedClubs = TextUtils.join(",", mClubListAdapter.getSelectedClubs())

                myFilterText = aPeoplePlaceName.text.toString()
                myPage = 1
                aStreetChk.isChecked = false
                aAreaChk.isChecked = false
                aCityChK.isChecked = false
                aStateChk.isChecked = false
                aCountryChk.isChecked = false
                aFriends.isChecked = false
                aFriendsofFriendsChk.isChecked = false
                myIsFilter = "1"
                isFirst = false
                loadData()
                aDlg.dismiss()
            } else {
                AppDialogs.okAction(
                    this,
                    getString(R.string.label_search_filter_selection_validation)
                )
            }

            Log.d("asdasda", myCurrent_city_id + myCountyId + myStateId + myPincode)
        }
    }

    private fun initPreviousSelection(mSelectedClubs: String) {
        val list = mSelectedClubs.trim().split(",")

        for (x in mClubList.indices) {
            for (i in list) {
                if (i == mClubList[x].id) {
                    mClubList[x].isChecked = true
                    break
                }
            }
        }

        isCheck()
    }

    private fun initClubCheck(checked: Boolean) {
        if (mClubList.isNotEmpty() && mClubListAdapter != null) {
            for (i in mClubList.indices) {
                mClubList[i].isChecked = checked
            }

            mClubListAdapter.notifyDataSetChanged()
        }
    }

    private fun initAdapter(aClubList: RecyclerView) {
        aClubList.layoutManager = LinearLayoutManager(this)
        aClubList.setHasFixedSize(true)
        mClubListAdapter = ClubMultiSelectionAdapter(mClubList, this)
        aClubList.adapter = mClubListAdapter

    }

    fun loadData() {
        when (aType) {
            "people" -> {
                val mCase = getSearchPeopleCaseString(
                    myPage,
                    myUserInfo.mUserId!!,
                    aSearchText,
                    myFilterText,
                    myIsFilter,
                    if (isFirst) "0" else myCurrent_city_id,
                    if (isFirst) "0" else myCountyId,
                    if (isFirst) "0" else myPincode,
                    if (isFirst) "0" else myStateId
                )
                val result =
                    Helper.GenerateEncrptedUrl(
                        BuildConfig.API_URL,
                        mCase!!
                    )
                Log.e("URL", result)
                AppServices.execute(
                    this, object : ResponseListener {
                        override fun onResponse(r: Response?) {

                            AppDialogs.hideProgressDialog()
                            if (r != null) {
                                if (r.response!!.isSuccess) {
                                    val aValue = r as PeopleListModel
                                    mPeopleListModel.totalPage = aValue.totalPage
                                    mPeopleListModel.peoplesList!!.addAll(aValue.peoplesList!!)
                                    mTotalPages = mPeopleListModel.totalPage
                                    mPeopleAdapter = SearchPeopleListAdapter(
                                        this@DashBoardSearchDetailActivity,
                                        mPeopleListModel
                                    )

                                    val FriendsLinearLayoutManagaer =
                                        LinearLayoutManager(
                                            this@DashBoardSearchDetailActivity,
                                            LinearLayoutManager.VERTICAL,
                                            false
                                        )
                                    recycler.layoutManager = FriendsLinearLayoutManagaer
                                    recycler.isNestedScrollingEnabled = true
                                    recycler.adapter = mPeopleAdapter
                                    recycler.setHasFixedSize(true)

                                } else {
                                    mPeopleListModel.peoplesList!!.clear()
                                    mPeopleAdapter = SearchPeopleListAdapter(
                                        this@DashBoardSearchDetailActivity,
                                        mPeopleListModel
                                    )
                                    mPeopleAdapter!!.notifyDataSetChanged()
                                    val FriendsLinearLayoutManagaer =
                                        LinearLayoutManager(
                                            this@DashBoardSearchDetailActivity,
                                            LinearLayoutManager.VERTICAL,
                                            false
                                        )
                                    recycler.layoutManager = FriendsLinearLayoutManagaer
                                    recycler.isNestedScrollingEnabled = true
                                    recycler.adapter = mPeopleAdapter
                                    recycler.setHasFixedSize(true)
                                    AppDialogs.showToastDialog(
                                        this@DashBoardSearchDetailActivity,
                                        r.response!!.responseMessage!!
                                    )
                                }
                            } else AppDialogs.customOkAction(
                                this@DashBoardSearchDetailActivity,
                                null,
                                TLConstant.SERVER_NOT_REACH,
                                null,
                                null,
                                false
                            )

                        }

                    },
                    result,
                    Request.Method.POST,
                    AppServices.API.publicFeed,
                    PeopleListModel::class.java
                )
            }
            "friends" -> {

                val mCase = getSearchPeopleCaseString(
                    myPage,
                    myUserInfo.mUserId!!,
                    aSearchText,
                    myFilterText,
                    myIsFilter,
                    myCurrent_city_id,
                    myCountyId,
                    myPincode,
                    myStateId
                )
                val result =
                    Helper.GenerateEncrptedUrl(
                        BuildConfig.API_URL,
                        mCase!!
                    )
                Log.e("URL", result)
                AppServices.execute(
                    this, object : ResponseListener {
                        override fun onResponse(r: Response?) {
                            AppDialogs.hideProgressDialog()
                            if (r != null) {
                                if (r.response!!.isSuccess) {
                                    val aValue = r as PeopleListModel
                                    mPeopleListModel.totalPage = aValue.totalPage
                                    mPeopleListModel.peoplesList!!.addAll(aValue.peoplesList!!)
                                    mTotalPages = mPeopleListModel.totalPage
                                    mPeopleAdapter = SearchPeopleListAdapter(
                                        this@DashBoardSearchDetailActivity,
                                        mPeopleListModel
                                    )

                                    val FriendsLinearLayoutManagaer =
                                        LinearLayoutManager(
                                            this@DashBoardSearchDetailActivity,
                                            LinearLayoutManager.VERTICAL,
                                            false
                                        )
                                    recycler.layoutManager = FriendsLinearLayoutManagaer
                                    recycler.isNestedScrollingEnabled = true
                                    recycler.adapter = mPeopleAdapter
                                    recycler.setHasFixedSize(true)

                                } else {
                                    mPeopleListModel.peoplesList!!.clear()
                                    mPeopleAdapter = SearchPeopleListAdapter(
                                        this@DashBoardSearchDetailActivity,
                                        mPeopleListModel
                                    )
                                    mPeopleAdapter!!.notifyDataSetChanged()
                                    val FriendsLinearLayoutManagaer =
                                        LinearLayoutManager(
                                            this@DashBoardSearchDetailActivity,
                                            LinearLayoutManager.VERTICAL,
                                            false
                                        )
                                    recycler.layoutManager = FriendsLinearLayoutManagaer
                                    recycler.isNestedScrollingEnabled = true
                                    recycler.adapter = mPeopleAdapter
                                    recycler.setHasFixedSize(true)
                                    AppDialogs.showToastDialog(
                                        this@DashBoardSearchDetailActivity,
                                        r.response!!.responseMessage!!
                                    )
                                }
                            } else AppDialogs.customOkAction(
                                this@DashBoardSearchDetailActivity,
                                null,
                                TLConstant.SERVER_NOT_REACH,
                                null,
                                null,
                                false
                            )
                        }

                    },
                    result,
                    Request.Method.POST,
                    AppServices.API.publicFeed,
                    PeopleListModel::class.java
                )

            }

            "public_post" -> {


                val mCase = getSearchFeedCaseInfo(
                    "1",
                    myUserInfo.mUserId!!,
                    myCountyId,
                    myStateId,
                    myCurrent_city_id,
                    myPage,
                    aSearchText,
                    myPincode, "", "", ""
                )
                val result =
                    Helper.GenerateEncrptedUrl(
                        BuildConfig.API_URL,
                        mCase!!
                    )
                Log.e("URL", result)
                AppServices.execute(
                    this, object : ResponseListener {
                        override fun onResponse(r: Response?) {
                            AppDialogs.hideProgressDialog()
                            if (r != null) {
                                if (r.requestType!! == AppServices.API.publicFeed.hashCode()) {
                                    if (r.response!!.isSuccess) {
                                        mTotalPages = r.response!!.totalPages
                                        mList.addAll((r as PublicFeedModel).feedList!!)
                                        mRecyclerView.setMediaObjects(mList)
                                        mAdapter.notifyDataSetChanged()
                                    } else {
                                        mList.clear()
                                        mAdapter.notifyDataSetChanged()
                                        AppDialogs.showToastDialog(
                                            this@DashBoardSearchDetailActivity,
                                            r.response!!.responseMessage!!
                                        )
                                    }
                                }
                            } else AppDialogs.customOkAction(
                                this@DashBoardSearchDetailActivity,
                                null,
                                TLConstant.SERVER_NOT_REACH,
                                null,
                                null,
                                false
                            )
                        }
                    },
                    result,
                    Request.Method.POST,
                    AppServices.API.publicFeed,
                    PublicFeedModel::class.java
                )

            }
            "friends_post" -> {

                val mCase = getSearchFeedCaseInfo(
                    "2",
                    myUserInfo.mUserId!!,
                    myCountyId,
                    myStateId,
                    myCurrent_city_id,
                    myPage,
                    aSearchText,
                    myPincode, isFriend, isFriendofFriend, ""
                )
                val result =
                    Helper.GenerateEncrptedUrl(
                        BuildConfig.API_URL,
                        mCase!!
                    )
                Log.e("URL", result)
                AppServices.execute(
                    this, object : ResponseListener {
                        override fun onResponse(r: Response?) {
                            AppDialogs.hideProgressDialog()
                            if (r != null) {
                                if (r.requestType!! == AppServices.API.publicFeed.hashCode()) {
                                    if (r.response!!.isSuccess) {
                                        mTotalPages = r.response!!.totalPages
                                        mList.addAll((r as PublicFeedModel).feedList!!)
                                        mRecyclerView.setMediaObjects(mList)
                                        mAdapter.notifyDataSetChanged()
                                    } else {
                                        mList.clear()
                                        mAdapter.notifyDataSetChanged()
                                        AppDialogs.showToastDialog(
                                            this@DashBoardSearchDetailActivity,
                                            r.response!!.responseMessage!!
                                        )
                                    }
                                }
                            } else AppDialogs.customOkAction(
                                this@DashBoardSearchDetailActivity,
                                null,
                                TLConstant.SERVER_NOT_REACH,
                                null,
                                null,
                                false
                            )
                        }
                    },
                    result,
                    Request.Method.POST,
                    AppServices.API.publicFeed,
                    PublicFeedModel::class.java
                )

            }
            "clubs_post" -> {

                val mCase = getSearchFeedCaseInfo(
                    "3",
                    myUserInfo.mUserId!!,
                    myCountyId,
                    myStateId,
                    myCurrent_city_id,
                    myPage,
                    aSearchText,
                    myPincode, "", "", mSelectedClubs
                )
                val result =
                    Helper.GenerateEncrptedUrl(
                        BuildConfig.API_URL,
                        mCase!!
                    )
                Log.e("URL", result)
                AppServices.execute(
                    this, object : ResponseListener {
                        override fun onResponse(r: Response?) {
                            AppDialogs.hideProgressDialog()
                            if (r != null) {
                                if (r.requestType!! == AppServices.API.publicFeed.hashCode()) {
                                    if (r.response!!.isSuccess) {
                                        mTotalPages = r.response!!.totalPages
                                        mList.addAll((r as PublicFeedModel).feedList!!)
                                        mRecyclerView.setMediaObjects(mList)
                                        mAdapter.notifyDataSetChanged()
                                    } else {
                                        mList.clear()
                                        mAdapter.notifyDataSetChanged()
                                        AppDialogs.showToastDialog(
                                            this@DashBoardSearchDetailActivity,
                                            r.response!!.responseMessage!!
                                        )
                                    }
                                }
                            } else AppDialogs.customOkAction(
                                this@DashBoardSearchDetailActivity,
                                null,
                                TLConstant.SERVER_NOT_REACH,
                                null,
                                null,
                                false
                            )
                        }
                    },
                    result,
                    Request.Method.POST,
                    AppServices.API.publicFeed,
                    PublicFeedModel::class.java
                )

            }
            "clubs" -> {

                val mCase = getSearchClubCaseString(
                    myPage,
                    myUserInfo.mUserId!!,
                    aSearchText
                )
                val result =
                    Helper.GenerateEncrptedUrl(
                        BuildConfig.API_URL,
                        mCase!!
                    )
                Log.e("URL", result)
                AppServices.execute(
                    this, object : ResponseListener {
                        override fun onResponse(r: Response?) {
                            AppDialogs.hideProgressDialog()
                            if (r != null) {
                                if (r.response!!.isSuccess) {
                                    val aValue = r as SearchClubListModel
                                    mClubListModel.totalPage = aValue.totalPage
                                    mClubListModel.clubList!!.addAll(aValue.clubList!!)
                                    mTotalPages = mClubListModel.totalPage!!
                                    mClubAdapter = SearchClubListAdapter(mContext, mClubListModel)

                                    val FriendsLinearLayoutManagaer =
                                        LinearLayoutManager(
                                            this@DashBoardSearchDetailActivity,
                                            LinearLayoutManager.VERTICAL,
                                            false
                                        )
                                    recycler.layoutManager = FriendsLinearLayoutManagaer
                                    recycler.isNestedScrollingEnabled = true
                                    recycler.adapter = mClubAdapter
                                    recycler.setHasFixedSize(true)

                                } else AppDialogs.showToastDialog(
                                    this@DashBoardSearchDetailActivity,
                                    r.response!!.responseMessage!!
                                )
                            } else AppDialogs.customOkAction(
                                this@DashBoardSearchDetailActivity,
                                null,
                                TLConstant.SERVER_NOT_REACH,
                                null,
                                null,
                                false
                            )
                        }

                    },
                    result,
                    Request.Method.POST,
                    AppServices.API.publicFeed,
                    SearchClubListModel::class.java
                )
            }
        }
    }


    private fun getSearchPeopleCaseString(
        aPage: Int,
        aUserId: String,
        aSearchName: String,
        aFilterText: String,
        aIsFileter: String,
        aCurrentCityId: String,
        aCountryid: String,
        aPincode: String,
        aStateId: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("page_no", aPage)
            jsonParam1.put("search_text", aSearchName)
            val jsonParam = JSONObject()
            jsonParam1.put("filter_text", aFilterText)
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("is_filter", aIsFileter)
            jsonParam1.put("current_city_id", aCurrentCityId)
            jsonParam1.put("country_id", aCountryid)
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("pincode", aPincode)
            jsonParam1.put("state_id", aStateId)
            jsonParam.put("SearchPeoplesList", jsonParam1)
            Log.e("SearchPeoplesList", " $jsonParam")

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    private fun getSearchClubCaseString(
        aPage: Int,
        aUserId: String,
        aSearchName: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("page_no", aPage)
            jsonParam1.put("search_text", aSearchName)
            val jsonParam = JSONObject()
            jsonParam1.put("login_user_id", aUserId)
            jsonParam.put("SearchClubList", jsonParam1)
            Log.e("SearchClubList", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    //  serach Feed case
    private fun getSearchFeedCaseInfo(
        Type: String,
        aUserId: String,
        aCountryId: String,
        aStateId: String,
        aCurrentCityId: String,
        aPage: Int,
        aSearchTXT: String,
        aPincode: String,
        aFriendSearch: String,
        aFriends_of_Friends: String,
        aSearchClubid: String
    ): String? {
        var aCaseStr = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("source", Type)
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("country_id", aCountryId)
            jsonParam1.put("state_id", aStateId)
            jsonParam1.put("current_city_id", aCurrentCityId)
            jsonParam1.put("page", aPage.toString())
            jsonParam1.put("search_text", aSearchTXT)
            jsonParam1.put("pincode", aPincode)
            jsonParam1.put("search_club_ids", aSearchClubid)
            jsonParam1.put("is_friend_search", aFriendSearch)
            jsonParam1.put("is_friends_of_friends_search", aFriends_of_Friends)
            val jsonParam = JSONObject()
            jsonParam.put("SearchFeeds", jsonParam1)
            Log.e("SearchFeeds", "" + jsonParam.toString())
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            Log.e("SearchFeeds", " $aCaseStr")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun volumeStateChange(isMute: Boolean) {
        mAdapter.changeVolumeState(isMute)
    }

    override fun onResume() {
        super.onResume()
        if (mRecyclerView.isVideoViewAdded)
            mRecyclerView.startPlayer()
    }

    override fun onPause() {
        super.onPause()
        mRecyclerView.pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRecyclerView.releasePlayer()
        LocalBroadcastManager.getInstance(myContext).unregisterReceiver(myBroadcastReceiver)
    }


    private val myBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            try {
                if (intent.action.equals(TLConstant.VIDEO_SEEK_POSITION_SEARCH)) {
                    val bundle = intent.extras
                    if (bundle!!.containsKey(TLConstant.VIDEO_SEEK_POSITION_SEARCH)) {
                        var pos: Long = 0
                        pos = bundle.getLong(TLConstant.VIDEO_SEEK_POSITION_SEARCH, 0)
                        try {
                            mRecyclerView.setPlayPostion(pos)
                        } catch (e: Exception) {
                            print(e.toString())
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun postLikeString(
        aUserId: String,
        aPostId: String,
        aLikeStatus: String,
        aSourceType: String,
        aLikeType: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("post_id", aPostId)
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("like", aLikeStatus)
            jsonParam1.put("like_type", aLikeType)
            jsonParam1.put("share", "0")
            //   if(aLikeType.equals( "1" )){
            jsonParam1.put("level", "")
            jsonParam1.put("source", aSourceType)
            //  }
            val jsonParam = JSONObject()
            jsonParam.put("LikeShare", jsonParam1)
            Log.e("PostLike", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun like_details(position: Int) {
        AppDialogs.showProgressDialog(mContext)
        AppServices.postLikeDetailsString(mList[position].id!!, mContext, this, myUserInfo.mUserId!!)
    }

    override fun like_click(position: Int, isLike: Boolean) {
        var like: String = ""
        if (isLike)
            like = "1"
        else
            like = "0"


        val mCase = postLikeString(
            myUserInfo.mUserId!!,
            mList[position].id!!,
            like,
            "", "1"
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            this, object : ResponseListener {
                override fun onResponse(r: Response?) {

                }

            },
            result,
            Request.Method.POST,
            AppServices.API.publicFeed,
            PublicFeedModel::class.java
        )
    }

    override fun share_click(position: Int, imgView: ImageView) {

    }

    override fun share_click(position: Int) {
        AppDialogs.ShowShareDialog(mList[position], this)
    }

    override fun comment_click(position: Int) {
        openFeedDetail(mList[position])
    }


    override fun menu_click(position: Int) {
        AppDialogs.show_feed_menu(this, mList[position], this)
    }

    override fun EditThisPost(feedList: PublicFeedModel.FeedList) {

        val intent = Intent(myContext, FeedEditActivity::class.java)
        intent.putExtra("feed", feedList)
        startActivity(intent)

    }

    override fun HideThisPost(feedList: PublicFeedModel.FeedList) {

        val mCase = getFeedHidePostCaseString(
            myUserInfo.mUserId!!,
            feedList.id!!
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            this, object : ResponseListener {
                override fun onResponse(r: Response?) {
                    if (r != null) {
                        if (r.response!!.isSuccess) {
                            Toast.makeText(
                                myContext,
                                "Post successfully hidden",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            },
            result,
            Request.Method.POST,
            AppServices.API.hide_post,
            Response::class.java
        )
        mList.remove(feedList)
        mAdapter.update(mList)
    }

    override fun DeleteThisPost(feedList: PublicFeedModel.FeedList) {

        AppDialogs.customDoubleAction(
            myContext,
            "",
            "Are you sure want to delete this post?",
            "Yes",
            "No",
            object : AppDialogs.OptionListener {
                override fun no() {

                }

                override fun yes() {
                    val mCase = getFeedDeletePostCaseString(
                        feedList.id!!
                    )
                    val result =
                        Helper.GenerateEncrptedUrl(
                            BuildConfig.API_URL,
                            mCase!!
                        )
                    Log.e("URL", result)
                    AppServices.execute(
                        myContext, object : ResponseListener {
                            override fun onResponse(r: Response?) {
                                if (r != null) {
                                    if (r.response!!.isSuccess) {
                                        Toast.makeText(
                                            myContext,
                                            "Post successfully deleted",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        },
                        result,
                        Request.Method.POST,
                        AppServices.API.delete_post,
                        Response::class.java
                    )


                    mList.remove(feedList)
                    mAdapter.update(mList)
                }

            },
            false,
            false
        )

    }

    override fun ReportThisPost(feedList: PublicFeedModel.FeedList) {

        /*val intent = Intent(mContext, ReportProblemActivity::class.java)
        intent.putExtra("post_id", feedList.id)
        intent.putExtra("FromScreen", "feed_list")
        startActivity(intent)*/

        val fm = myContext.supportFragmentManager
        val dialogFragment = ReportDailogFragment()
        val args: Bundle? = Bundle()
        args?.putString("post_id", feedList.id);
        args?.putString("FromScreen", "feed_list");
        dialogFragment.setArguments(args)
        dialogFragment.show(fm, "ReportDailogFragment")

    }

    override fun BlockThisPost(feedList: PublicFeedModel.FeedList) {

        val mCase = getSettingBlockFriendMemberString(
            myUserInfo.mUserId!!,
            feedList.userId!!,
            "1"
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            myContext, object : ResponseListener {
                override fun onResponse(r: Response?) {
                    if (r != null) {
                        if (r.response!!.isSuccess) {
                            Toast.makeText(
                                myContext,
                                r.response!!.responseMessage,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            },
            result,
            Request.Method.POST,
            AppServices.API.hide_post,
            Response::class.java
        )

    }

    override fun FollowThisPost(feedList: PublicFeedModel.FeedList) {
        val status = if (feedList.isFollow.equals("0"))
            "1"
        else "0"

        val mCase = follow_unfollow_case(
            myUserInfo.mUserId!!,
            feedList.userId!!,
            status
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            myContext, object : ResponseListener {
                override fun onResponse(r: Response?) {
                    if (r != null) {
                        if (r.response!!.isSuccess) {
                            Toast.makeText(
                                myContext,
                                r.response!!.responseMessage,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            },
            result,
            Request.Method.POST,
            AppServices.API.hide_post,
            Response::class.java
        )
    }


    private fun follow_unfollow_case(
        aUserId: String,
        aProfileUserId: String,
        aStatus: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("follows_id", aProfileUserId)
            jsonParam1.put("follow_status", aStatus)

            val jsonParam = JSONObject()
            jsonParam.put("FollowUnfollow", jsonParam1)
            Log.e("FollowUnfollow", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    private fun getSettingBlockFriendMemberString(
        aLoginUserId: String,
        aFriendId: String,
        aBlockStatus: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("login_user_id", aLoginUserId)
            jsonParam1.put("friend_id", aFriendId)
            jsonParam1.put("block_status", aBlockStatus)
            val jsonParam = JSONObject()
            jsonParam.put("BlockFriend", jsonParam1)
            Log.e("BlockFriend", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    private fun getFeedHidePostCaseString(
        aUserId: String,
        aPostId: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("login_user_id", aUserId)
            jsonParam1.put("post_id", aPostId)
            val jsonParam = JSONObject()
            jsonParam.put("HidePost", jsonParam1)
            Log.e("HidePost", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    private fun getFeedDeletePostCaseString(aPostId: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("post_id", aPostId)
            val jsonParam = JSONObject()
            jsonParam.put("DeletePost", jsonParam1)
            Log.e("DeletePost", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun video_preview(
        position: Int,
        isRecycler: Boolean,
        isVideo: Boolean,
        focus_pos: Int
    ) {

        if (isVideo)
            openVideoPreview(mList[position].media!![0].original)
        else
            openImagePreview(mList[position], focus_pos)

    }


    fun openVideoPreview(original: String?) {
        var pos: Long = 0
        try {
            pos = mRecyclerView.getPlayPosition()
        } catch (e: Exception) {
            print(e.toString())
        }

        val intent =
            Intent(this, VideoPreviewActivity::class.java)
        intent.putExtra("url", original)
        intent.putExtra("play_postion", pos)
        intent.putExtra("action", TLConstant.VIDEO_SEEK_POSITION_SEARCH)
        startActivity(intent)
    }

    fun openImagePreview(
        media: PublicFeedModel.FeedList,
        focusPos: Int
    ) {
        val intent =
            Intent(this, ImagePreview::class.java)
        intent.putExtra("media", media)
        intent.putExtra("focus", focusPos)
        startActivity(intent)
    }

    fun openFeedDetail(feedList: PublicFeedModel.FeedList) {
        val intent =
            Intent(this, FeedDetailActivity::class.java)
        intent.putExtra("media", feedList)
        startActivity(intent)
    }

    override fun isCheck() {
        if (mAllCheckBok != null)
            mAllCheckBok!!.isChecked = getCheckedCount()
    }

    private fun getCheckedCount(): Boolean {
        var count = 0

        for (i in mClubList.indices) {
            if (mClubList[i].isChecked)
                count++
        }

        return count == mClubList.size
    }

    override fun onResponse(r: Response?) {
        try {

            if (r != null) {
                 if (r.requestType!! == AppServices.API.likedetails.hashCode()) {
                    if (r.response!!.isSuccess) {
                        val value = r as LikeList

                        AppDialogs.showLikeList(
                            mContext,
                            "",
                            value.mDataList,
                            object : LikeListAdapter.Callback {
                                override fun info(position: Int, text: String) {

                                    val aIntent =
                                        Intent(mContext, ProfileActivity::class.java).putExtra(
                                            "userid",
                                            text
                                        )
                                    mContext.startActivity(aIntent)
                                }

                            },
                            true,
                            true
                        )


                    }
                }

            }
            AppDialogs.hideProgressDialog()
        } catch (exception: Exception) {
            AppDialogs.hideProgressDialog()
        }
    }

}
