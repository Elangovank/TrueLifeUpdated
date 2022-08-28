package com.truelife.app.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.adapter.SearchFriendsListAdapter
import com.truelife.app.constants.TLConstant
import com.truelife.app.model.DashboardSearchModel
import com.truelife.app.model.User
import com.truelife.base.BaseActivity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import kotlinx.android.synthetic.main.activity_dashboard_search.*
import org.json.JSONObject

class DashboardSearchActivity : BaseActivity(), ResponseListener {
    var mSearchResult: DashboardSearchModel = DashboardSearchModel()

    val mText = "No Posts Found. Try with another word"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_search)


        frament_feed_search_Edt.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (frament_feed_search_Edt.text.toString().trim().isEmpty()) {
                    frament_feed_search_Edt.text = "".toEditable()
                    return@OnEditorActionListener true
                }

                frament_feed_search_Edt.setText(frament_feed_search_Edt.text.toString().trim())

                AppDialogs.showProgressDialog(myContext)
                loadData()
                return@OnEditorActionListener true
            }
            false
        })

        frament_feed_search_Edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0!!.length > 0) {
                    clear_image_view.visibility = View.VISIBLE
                } else {
                    clear_image_view.visibility = View.GONE
                    search_result_LAY.visibility = View.GONE
                }
            }

        })


        clear_image_view.setOnClickListener {
            frament_feed_search_Edt.text = "".toEditable()
        }

        cancel_action_btn.setOnClickListener {
            finish()
        }


        fragment_feed_new_search_list_public_LAY.setOnClickListener {
            if (mSearchResult.searchInfo!!.publicFeedsCount!! != 0.toLong()) {
                val intent = Intent(this, DashBoardSearchDetailActivity::class.java)
                intent.putExtra("type", "public_post")
                intent.putExtra("search_text", frament_feed_search_Edt.text.toString().trim())
                startActivity(intent)
            } else AppDialogs.showToastDialog(this, mText)
        }

        fragment_feed_new_search_list_friends_LAY.setOnClickListener {
            if (mSearchResult.searchInfo!!.friendsFeedsCount!! != 0.toLong()) {
                val intent = Intent(this, DashBoardSearchDetailActivity::class.java)
                intent.putExtra("type", "friends_post")
                intent.putExtra("search_text", frament_feed_search_Edt.text.toString().trim())
                startActivity(intent)
            } else AppDialogs.showToastDialog(this, mText)
        }

        fragment_feed_new_search_list_club_LAY.setOnClickListener {
            if (mSearchResult.searchInfo!!.clubsFeedsCount!! != 0.toLong()) {
                val intent = Intent(this, DashBoardSearchDetailActivity::class.java)
                intent.putExtra("type", "clubs_post")
                intent.putExtra("search_text", frament_feed_search_Edt.text.toString().trim())
                startActivity(intent)
            } else AppDialogs.showToastDialog(this, mText)
        }

        fragment_feed_new_search_list_people_LAY.setOnClickListener {
            if (mSearchResult.searchInfo!!.peoplesCount!! != 0.toLong()) {
                val intent = Intent(this, DashBoardSearchDetailActivity::class.java)
                intent.putExtra("type", "people")
                intent.putExtra("search_text", frament_feed_search_Edt.text.toString().trim())
                startActivity(intent)
            } else AppDialogs.showToastDialog(this, mText)
        }

        fragment_feed_new_search_list_friends_see_all_TXT.setOnClickListener {
            val intent = Intent(this, DashBoardSearchDetailActivity::class.java)
            intent.putExtra("type", "friends")
            intent.putExtra("search_text", frament_feed_search_Edt.text.toString().trim())
            startActivity(intent)
        }

        fragment_feed_new_search_list_clubs_see_all_TXT.setOnClickListener {
            val intent = Intent(this, DashBoardSearchDetailActivity::class.java)
            intent.putExtra("type", "clubs")
            intent.putExtra("search_text", frament_feed_search_Edt.text.toString().trim())
            startActivity(intent)
        }

    }

    override fun clickListener() {

    }

    override fun init() {
    }

    fun loadData() {
        val mUser: User = LocalStorageSP.getLoginUser(myContext)
        val mCase = PostSearchDetails(
            mUser.mCurrentCityId?: "0",
            mUser.mCountryId ?: "0",
            mUser.mUserId ?: "0",
            frament_feed_search_Edt.text.toString().trim(),
            mUser.mPincode ?: "0",
            mUser.mStateId ?: "0"
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            this, this,
            result,
            Request.Method.POST,
            AppServices.API.publicFeed,
            DashboardSearchModel::class.java
        )
    }

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    private fun PostSearchDetails(
        aCurrentCityId: String,
        aCountryName: String,
        aUserId: String,
        aSearchName: String,
        aPincode: String,
        aStateId: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("current_city_id", aCurrentCityId)
            jsonParam1.put("country_id", aCountryName)
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("search_text", aSearchName)
            jsonParam1.put("pincode", aPincode)
            jsonParam1.put("state_id", aStateId)
            val jsonParam = JSONObject()
            jsonParam.put("SearchFeedCount", jsonParam1)
            Log.e("SearchFeedCount", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()

        if (r != null) {
            if (r.response!!.isSuccess) {
                mSearchResult = r as DashboardSearchModel
                search_result_LAY.visibility = View.VISIBLE
                loadValues()
            } else AppDialogs.showToastDialog(myContext, r.response!!.responseMessage!!)

        } else AppDialogs.customOkAction(
            myContext,
            null,
            TLConstant.SERVER_NOT_REACH,
            null,
            null,
            false
        )
    }

    @SuppressLint("SetTextI18n")
    private fun loadValues() {
        if (mSearchResult.searchInfo!!.publicFeedsCount!!.toString() == "0")
            fragment_feed_new_search_list_public_post_count.text = "No Posts"
        else
            fragment_feed_new_search_list_public_post_count.text =
                mSearchResult.searchInfo!!.publicFeedsCount!!.toString() + " Result(s)"

        if (mSearchResult.searchInfo!!.friendsFeedsCount!!.toString() == "0")
            fragment_feed_new_search_list_friends_post_count.text = "No Posts"
        else
            fragment_feed_new_search_list_friends_post_count.text =
                mSearchResult.searchInfo!!.friendsFeedsCount!!.toString() + " Result(s)"

        if (mSearchResult.searchInfo!!.clubsFeedsCount!!.toString() == "0")
            fragment_feed_new_search_list_clubs_post_count.text = "No Posts"
        else
            fragment_feed_new_search_list_clubs_post_count.text =
                mSearchResult.searchInfo!!.clubsFeedsCount!!.toString() + " Result(s)"

        if (mSearchResult.searchInfo!!.peoplesCount!!.toString() == "0")
            fragment_feed_new_search_list_people_count_TXT.text = "No people found"
        else
            fragment_feed_new_search_list_people_count_TXT.text =
                mSearchResult.searchInfo!!.peoplesCount!!.toString() + " Result(s)"


        if (mSearchResult.searchInfo!!.friendsDetails!!.size == 0) {
            fragment_feed_new_search_list_friends_see_all_TXT.visibility = View.GONE
            fragment_feed_new_search_list_friends_recycler.visibility = View.GONE
            no_friends_found.visibility = View.VISIBLE
        } else {
            no_friends_found.visibility = View.GONE
            fragment_feed_new_search_list_friends_recycler.visibility = View.VISIBLE

            val mAdapter = SearchFriendsListAdapter(this, mSearchResult.searchInfo!!, 1)

            val FriendsLinearLayoutManagaer =
                LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
            fragment_feed_new_search_list_friends_recycler.setLayoutManager(
                FriendsLinearLayoutManagaer
            )
            fragment_feed_new_search_list_friends_recycler.isNestedScrollingEnabled = true
            fragment_feed_new_search_list_friends_recycler.setAdapter(mAdapter)
            fragment_feed_new_search_list_friends_recycler.setHasFixedSize(true)


            if (mSearchResult.searchInfo!!.moreFriends!!.toString().equals("0")
            )
                fragment_feed_new_search_list_friends_see_all_TXT.visibility = View.GONE
            else
                fragment_feed_new_search_list_friends_see_all_TXT.visibility = View.VISIBLE
        }

        if (mSearchResult.searchInfo!!.clubDetails!!.size == 0) {
            fragment_feed_new_search_list_clubs_recycler.visibility = View.GONE
            fragment_feed_new_search_list_clubs_see_all_TXT.visibility = View.GONE
            no_clubs_found.visibility = View.VISIBLE
        } else {
            fragment_feed_new_search_list_clubs_recycler.visibility = View.VISIBLE

            val mAdapter = SearchFriendsListAdapter(this, mSearchResult.searchInfo!!, 2)

            val FriendsLinearLayoutManagaer =
                LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
            fragment_feed_new_search_list_clubs_recycler.isNestedScrollingEnabled = true
            fragment_feed_new_search_list_clubs_recycler.setLayoutManager(
                FriendsLinearLayoutManagaer
            )

            fragment_feed_new_search_list_clubs_recycler.setAdapter(mAdapter)
            fragment_feed_new_search_list_clubs_recycler.setHasFixedSize(true)

            no_clubs_found.visibility = View.GONE
            if (mSearchResult.searchInfo!!.moreClubs!!.toString().equals("0"))
                fragment_feed_new_search_list_clubs_see_all_TXT.visibility = View.GONE
            else
                fragment_feed_new_search_list_clubs_see_all_TXT.visibility = View.VISIBLE
        }
    }


}
