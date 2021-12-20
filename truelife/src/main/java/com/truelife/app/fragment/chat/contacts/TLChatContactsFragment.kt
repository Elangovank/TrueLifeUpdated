package com.truelife.app.fragment.chat.contacts

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.activity.TLChatActivity
import com.truelife.app.constants.TLConstant
import com.truelife.app.model.Chat
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.*
import kotlinx.android.synthetic.main.app_main_header.*
import org.json.JSONObject


/**
 * Created by Elango on 26-12-2019.
 */

class TLChatContactsFragment : BaseFragment(), View.OnClickListener,
    TLContactsAdapter.Callback,
    ResponseListener {

    private var mContext: Context? = null
    private var mFragmentManager: TLFragmentManager? = null
    private lateinit var mBack: ImageView
    private lateinit var mSearch: EditText
    private lateinit var mChatRecycler: RecyclerView
    private lateinit var mNoDataText: TextView
    private var mAdapter: TLContactsAdapter? = null
    private var mSwipeRefresh: SwipeRefreshLayout? = null
    var mData = ArrayList<Chat.ContactsList>()
    var mSelectedData: Chat.ContactsList? = null
    var mSelectedNumber = ""
    private lateinit var mSearchCancel: TextView

    private var mView: View? = null

    private var mPermission: Array<String> = arrayOf()

    companion object {
        var TAG: String = TLChatContactsFragment::class.java.simpleName
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_contacts_list, container, false)
        init(mView!!)
        clickListener()

        return mView
    }

    override fun init(view: View) {
        mContext = activity
        mFragmentManager = TLFragmentManager(activity!!)

//        mPermission = arrayOf(TLConstant.CONTACTS)
//        PermissionChecker().askAllPermissions(mContext!!, mPermission)

        mBack = mView!!.findViewById(R.id.app_header_back_arrow)

        mSwipeRefresh = mView!!.findViewById(R.id.fragment_chat_swipe_ref)
        mChatRecycler = mView!!.findViewById(R.id.fragment_chat_recycler)
        mSearch = mView!!.findViewById(R.id.fragment_search)
        mNoDataText = mView!!.findViewById(R.id.no_data_found_txt)
        mSearchCancel = mView!!.findViewById(R.id.fragment_cancel)

        initRecycler()

        AppDialogs.swipeRefColor(mContext!!, mSwipeRefresh!!)
        mSwipeRefresh!!.setOnRefreshListener {
            getData(false)
        }

        getData(true)

        mBack.visibility = View.VISIBLE

        mSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 0)
                    mSearchCancel.visibility = View.VISIBLE
                else noText("No Contacts Found!", mData.isEmpty())

                mAdapter!!.filter.filter(s!!.toString()) {
                    if (it == 0)
                        noText(String.format("We couldn't find anything for\n'%s'", s), true)
                    if (count == 0)
                        noText("No Contacts Found!", mData.isEmpty())
                }
            }
        })
    }

    private fun noText(text: String, visible: Boolean) {
        mNoDataText.text = text
        if (visible) {
            mNoDataText.visibility = View.VISIBLE
            mChatRecycler.visibility = View.GONE
        } else {
            mNoDataText.visibility = View.GONE
            mChatRecycler.visibility = View.VISIBLE
        }

    }

    override fun clickListener() {
        mBack.setOnClickListener(this)
        mSearchCancel.setOnClickListener(this)
    }

    private fun getData(showProgress: Boolean) {
        /*  if (PermissionChecker().checkAllPermission(mContext!!, mPermission)) {
              if (checkInternet()) {
                  if (showProgress)
                      AppDialogs.showProgressDialog(mContext!!)

                  val contentResolver: ContentResolver = mContext!!.contentResolver
                  val cursor = contentResolver.query(
                      ContactsContract.Contacts.CONTENT_URI,
                      null,
                      null,
                      null,
                      ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                  )
                  if (cursor!!.count > 0) {
                      mData.clear()
                      while (cursor.moveToNext()) {
                          val hasPhoneNumber =
                              cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                                  .toInt()
                          if (hasPhoneNumber > 0) {
                              val contact = Chat.ContactsList()
                              contact.id =
                                  cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                              contact.name =
                                  cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                              contact.profile_image =
                                  cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))

                              val pCur: Cursor? = contentResolver.query(
                                  Phone.CONTENT_URI,
                                  null,
                                  Phone.CONTACT_ID + " = ?",
                                  arrayOf(contact.id!!),
                                  null
                              )
                              while (pCur!!.moveToNext()) {
                                  val contactNumber =
                                      pCur.getString(pCur.getColumnIndex(Phone.NUMBER))
                                  if (!contact.numbers.contains(contactNumber))
                                      contact.numbers.add(contactNumber)
                              }
                              pCur.close()

                              mData.add(contact)
                          }
                      }
                      cursor.close()
                  }

                  if (mData.isNotEmpty())
                      mAdapter!!.notifyDataSetChanged()

                  noText("No Contacts Found!", mData.isEmpty())

                  AppDialogs.hideProgressDialog()
                  mSwipeRefresh!!.isRefreshing = false
              } else {
                  mSwipeRefresh!!.isRefreshing = false
              }
          }*/
    }

    private fun getParam(number: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("login_user_id", LocalStorageSP.getLoginUser(mContext).mUserId)
            jsonObject.put("ToUserNumber", number)
            val jsonParam = JSONObject()
            jsonParam.put("ChatByNumber", jsonObject)

            Log.i("Param -->> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(mContext)
        mChatRecycler.layoutManager = layoutManager
        mChatRecycler.setHasFixedSize(true)
        mAdapter = TLContactsAdapter(
            mContext!!,
            mData,
            this
        )
        mChatRecycler.adapter = mAdapter
    }


    override fun initBundle() {

    }

    override fun onClick(v: View?) {
        when (v!!) {
            mBack -> (mContext as TLChatActivity).finishActivity()
            mSearchCancel -> {
                AppDialogs.hideSoftKeyboard(activity!!, mChatRecycler)
                mSearch.text = null
                mSearchCancel.visibility = View.GONE
                mSearch.clearFocus()
            }
        }
    }

    override fun onBackPressed() {
        (mContext as TLChatActivity).finishActivity()
    }

    override fun onResumeFragment() {
        app_header_title.text = "Contacts"
        app_header_title.typeface = ResourcesCompat.getFont(mContext!!, R.font.calling_heart)
        app_header_title.textSize = 30f
        app_header_menu.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun info(data: Chat.ContactsList) {
        AppDialogs.showSingleChoice(
            mContext!!,
            "Select Number",
            data.numbers,
            object : SingleChoiceAdapter.Callback {
                override fun info(position: Int, text: String) {
                    if (checkInternet()) {
                        mSelectedData = data
                        AppDialogs.hideSingleChoice()
                        AppDialogs.showProgressDialog(mContext!!)
                        searchNumber(text.trim())
                    }
                }
            }, true
        )
    }

    private fun searchNumber(text: String) {
        mSelectedNumber = text
        val mCase = getParam(text)
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
        AppServices.execute(
            mContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.searchNumber,
            Chat.ChatList::class.java
        )
    }

    override fun onResponse(r: Response?) {
        mSwipeRefresh!!.isRefreshing = false
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.searchNumber.hashCode()) {
                if (r.response!!.isSuccess) {
                    val data = (r as Chat.ChatList).mData
                    Log.i("Data Size -->>> ", data.size.toString())
                } else {
                    AppDialogs.customDoubleAction(
                        mContext!!,
                        null,
                        "This user is not in True Life currently, but you can send and invitation to join",
                        "Ok", "Cancel",
                        object : AppDialogs.OptionListener {
                            override fun yes() {

                                val uri = Uri.parse(String.format("smsto:%s", mSelectedNumber))
                                val sendIntent = Intent(Intent.ACTION_SENDTO, uri)
                                sendIntent.putExtra(
                                    "sms_body",
                                    String.format(
                                        getString(R.string.chat_contact_invite),
                                        mSelectedData?.name
                                    )
                                )
                                startActivity(sendIntent)
                            }

                            override fun no() {
                                AppDialogs.hidecustomView()
                            }

                        }, false, isOptionable = false
                    )
                }
            } else AppDialogs.showToastDialog(mContext!!, r.response!!.responseMessage!!)

        } else AppDialogs.customOkAction(
            mContext!!,
            null,
            TLConstant.SERVER_NOT_REACH,
            null,
            null,
            false
        )
    }

}