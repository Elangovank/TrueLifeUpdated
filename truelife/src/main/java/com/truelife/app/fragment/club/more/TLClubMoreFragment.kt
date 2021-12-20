package com.truelife.app.fragment.club.more

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.google.gson.Gson
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.constants.TLConstant
import com.truelife.app.fragment.ReportDailogFragment
import com.truelife.app.fragment.club.TLClubDetails
import com.truelife.app.fragment.club.more.invite.TLClubFriendsContacts
import com.truelife.app.fragment.club.more.invite.TLClubGeneralPublic
import com.truelife.app.fragment.club.more.invite.TLClubRemoveFriends
import com.truelife.app.model.Club
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import org.json.JSONObject


/**
 * Created by Elango on 20-02-2020.
 */


class TLClubMoreFragment(var mClubs: Club.Clubs) : BaseFragment(), ClubMoreAdapter.ClickListener,
    ResponseListener {

    private var mContext: FragmentActivity? = null
    private var mFragmentManager: TLFragmentManager? = null
    var mAdapter: ClubMoreAdapter? = null
    private var mMenuRecycler: RecyclerView? = null
    private var mView: View? = null
    var isAdmin = false


    companion object {
        var TAG: String = TLClubMoreFragment::class.java.simpleName
    }


    private fun setRecycleViewAdapter() {
        mMenuRecycler!!.setHasFixedSize(true)
        mMenuRecycler!!.layoutManager = GridLayoutManager(mContext, if (isAdmin) 3 else 2)
        mMenuRecycler!!.isNestedScrollingEnabled = true
        mAdapter = ClubMoreAdapter(
            mContext!!,
            isAdmin,
            this
        )
        mMenuRecycler!!.adapter = mAdapter
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_club_more_screen, container, false)
        init(mView!!)
        return mView
    }

    override fun init(view: View) {
        mContext = activity
        mFragmentManager = TLFragmentManager(mContext!!)
        mMenuRecycler = view.findViewById(R.id.fragment_menu_list_RV) as RecyclerView

        isAdmin = mClubs.clubs!!.adminId == LocalStorageSP.getLoginUser(mContext).mUserId

        setRecycleViewAdapter()
    }

    override fun clickListener() {

    }


    override fun initBundle() {

    }


    override fun onBackPressed() {
        mFragmentManager!!.onBackPress()
    }

    override fun onResumeFragment() {

    }

    override fun onClick(tt: String, position: Int) {
        when (tt) {

            // Admin Menu

            getString(R.string.label_add_invite_member) -> navigateInvite()
            getString(R.string.label_remove_member) -> {
                if (checkInternet()) {
                    val intent = Intent(mContext, TLClubRemoveFriends::class.java)
                    intent.putExtra("club_details", Gson().toJson(mClubs))
                    startActivity(intent)
                }
            }
            getString(R.string.label_change_admin) -> changeAdmin()

            getString(R.string.label_members_rights) -> {
                val fm = mContext!!.supportFragmentManager
                val dialogFragment = TLClubMemberRights(mClubs.clubs!!)
                dialogFragment.show(fm, TLClubMemberRights.TAG)
            }
            getString(R.string.label_privacy) -> {
                val aIntent =
                    Intent(mContext, ClubPrivacyAcitivty()::class.java).putExtra(
                        "obj",
                        Gson().toJson(mClubs).toString()
                    )
                startActivityForResult(aIntent, 1)
            }

            // Member Menu

            getString(R.string.label_club_in_invite) -> navigateInvite()

            getString(R.string.label_more_club_report) -> {
                val fm = mContext!!.supportFragmentManager
                val dialogFragment = ReportDailogFragment()
                val args = Bundle()
                args.putString("club_id", mClubs.clubs!!.id)
                args.putString("FromScreen", "ClubInMore")
                dialogFragment.arguments = args
                dialogFragment.show(fm, "ReportDailogFragment")
            }

            getString(R.string.label_club_in_msg_administrator) -> {
                try {
                    if (mClubs.clubs!!.admin_email.isNotEmpty()) {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "plain/text"
                        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mClubs.clubs!!.admin_email))
                        intent.putExtra(
                            Intent.EXTRA_SUBJECT,
                            "REG : Truelife | Message to Be your own admin"
                        )
                        startActivity(Intent.createChooser(intent, ""))
                    } else AppDialogs.showToastDialog(mContext!!, "Admin email is missing!")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Exit
            getString(R.string.label_club_exit) -> {
                if (isAdmin) {

                    AppDialogs.customDoubleAction(
                        mContext!!,
                        null,
                        "You want to change administrator to exit from club",
                        "Ok", "Cancel",
                        object : AppDialogs.OptionListener {
                            override fun yes() {
                                changeAdmin()
                            }

                            override fun no() {
                                AppDialogs.hidecustomView()
                            }

                        }, false, isOptionable = false
                    )

                } else
                    AppDialogs.customDoubleAction(
                        mContext!!,
                        null,
                        "Do you want to exit club?",
                        "Ok", "Cancel",
                        object : AppDialogs.OptionListener {
                            override fun yes() {
                                initExit()
                            }

                            override fun no() {
                                AppDialogs.hidecustomView()
                            }

                        }, false, isOptionable = false
                    )
            }
        }

    }

    private fun changeAdmin() {
        try {
            if (checkInternet()) {
                val intent = Intent(mContext, TLClubChangeAdmin::class.java)
                intent.putExtra("club_details", Gson().toJson(mClubs))
                intent.putExtra("admin", isAdmin)
                startActivityForResult(intent, 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initExit() {
        AppDialogs.showProgressDialog(mContext!!)
        val mCase = getParam()
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
        AppServices.execute(
            mContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.clubfriendsList,
            Club.FriendList::class.java
        )
    }

    private fun navigateInvite() {
        val view = AppDialogs.showcustomView(
            mContext!!,
            R.layout.fragment_club_by_you_add_invite_member,
            true, fullScreen = true
        )

        val mGeneral = view!!.findViewById<Button>(R.id.add_invite_general_public_btn)
        val mFriends = view.findViewById<Button>(R.id.add_invite_friends_contact_btn)

        val mClose = view.findViewById<ImageButton>(R.id.close_club_button)
        mClose.setOnClickListener {
            AppDialogs.hidecustomView()
        }

        mGeneral.setOnClickListener {
            val intent = Intent(mContext, TLClubGeneralPublic::class.java)
            intent.putExtra("club_details", Gson().toJson(mClubs))
            startActivity(intent)
        }

        mFriends.setOnClickListener {
            val intent = Intent(mContext, TLClubFriendsContacts::class.java)
            intent.putExtra("club_details", Gson().toJson(mClubs))
            startActivity(intent)
        }
    }


    override fun onLongClick(view: View?, position: Int) {
    }

    private fun getParam(): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            val user = LocalStorageSP.getLoginUser(mContext)
            jsonObject.put("club_id", mClubs.clubs!!.id)
            jsonObject.put("login_user_id", user.mUserId)

            val jsonParam = JSONObject()
            jsonParam.put("ExitClub", jsonObject)

            Log.i("ExitClub --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.clubfriendsList.hashCode()) {
                AppDialogs.showToastDialog(mContext!!, r.response!!.responseMessage!!)
                if (r.response!!.isSuccess)
                    (mContext as TLClubDetails).finishActivity()
            }
        } else AppDialogs.customOkAction(
            mContext!!, null, TLConstant.SERVER_NOT_REACH, null, null, false
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == 1)
            (mContext as TLClubDetails).finishActivity()
    }

}