package com.truelife.app.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.adapter.LocationSearchFilterAdapter
import com.truelife.app.listeners.SelectedListener
import com.truelife.app.model.LocationModel
import com.truelife.app.model.PersonalSettingModel
import com.truelife.app.model.User
import com.truelife.base.BaseActivity
import com.truelife.base.progressInterface
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import org.json.JSONObject
import java.util.*

class PersonalSetting : BaseActivity(), ResponseListener, SelectedListener, progressInterface {

    lateinit var mContext: FragmentActivity
    var mBackBtn: ImageView? = null
    var mToolbarTitle: TextView? = null

    var mCountryTV: EditText? = null
    var mStateTV: EditText? = null
    var mCityTV: EditText? = null

    var mDoorNoEdit: EditText? = null
    var mStreetNameEdit: EditText? = null
    var mAddressEdit: EditText? = null
    var mHomeTownEdit: EditText? = null
    var mPinCodeEdit: EditText? = null
    var mSaveBtn: Button? = null

    var user: User? = null
    private var mCategoryId: String? = null
    private var mCategory: String? = null
    var mCountryId: String? = null
    var mStateId: String? = null
    var mCityId: String? = null

    var mPinCodeValue: String? = null

    private var myalertDialog: android.app.AlertDialog? = null
    private var myAdapter: LocationSearchFilterAdapter? = null
    private var myDialog: AlertDialog.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_setting)
        init()
        loadValues()
    }

    fun validate(): Boolean {

        if (mCountryId.isNullOrEmpty() || mCountryId!!.equals("0")) {
            AppDialogs.showToastDialog(mContext, getString(R.string.alert_country))
            return false
        } else if (mStateId.isNullOrEmpty() || mStateId!!.equals("0")) {
            AppDialogs.showToastDialog(mContext, getString(R.string.alert_state))
            return false
        } else if (mCityId.isNullOrEmpty() || mCityId!!.equals("0")) {
            AppDialogs.showToastDialog(mContext, getString(R.string.alert_city))
            return false
        } else if (mPinCodeValue.isNullOrEmpty() || mPinCodeValue!!.length == 0) {
            AppDialogs.showToastDialog(mContext, getString(R.string.alert_pincode))
            return false
        }
        return true
    }

    fun getValues() {

        mPinCodeValue = getETValue(mPinCodeEdit)
    }

    fun saveAPI() {

        getValues()

        if (validate()) {

            saveCaseAPI()
        }

    }

    override fun clickListener() {

        mSaveBtn!!.setOnClickListener {
            saveAPI()
        }

        mBackBtn!!.setOnClickListener {
            finish()
        }

        mCountryTV!!.setOnClickListener {

            mCategory = "0"
            mStateId = "0"
            mCategoryId = "0"
            getLocationDetails()
        }

        mStateTV!!.setOnClickListener {

            mCategory = "1"
            mCategoryId = mCountryId
            mStateId = "0"
            getLocationDetails()
        }


        mCityTV!!.setOnClickListener {

            mCategory = "2"
            mCategoryId = mCountryId
            getLocationDetails()
        }
    }

    override fun init() {
        mContext = this
        user = LocalStorageSP.getLoginUser(mContext)
        mBackBtn = findViewById(R.id.common_back_arrow) as ImageView
        mToolbarTitle = findViewById(R.id.set_personal_header) as TextView

        mCountryTV = findViewById(R.id.fragment_setting_personal_country) as EditText
        mStateTV = findViewById(R.id.fragment_setting_personal_state) as EditText
        mCityTV = findViewById(R.id.fragment_setting_personal_current_city) as EditText

        mDoorNoEdit = findViewById(R.id.fragment_setting_personal_door_no) as EditText
        mStreetNameEdit = findViewById(R.id.fragment_setting_personal_street_name) as EditText

        mAddressEdit = findViewById(R.id.fragment_setting_personal_address) as EditText
        mHomeTownEdit = findViewById(R.id.fragment_setting_personal_home_town) as EditText
        mPinCodeEdit = findViewById(R.id.fragment_setting_personal_pin_code) as EditText
        mSaveBtn = findViewById(R.id.privacy_setting_post_save_btn) as Button

        val aTitle =
            "<b><font color='#000000'>PERSONAL</font></b>" + "<br>" + " <font color='#000000'>SETTINGS</font>"
        mToolbarTitle!!.setText(Html.fromHtml(aTitle))
        clickListener()
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.perosnalsetting.hashCode()) {
                if (r.response!!.isSuccess) {
                    var privacySetting = r as PersonalSettingModel
                    setValues(privacySetting.setting!!)
                }
            } else if (r.requestType!! == AppServices.API.country.hashCode()) {
                if (r.response!!.isSuccess) {
                    var mLocationList = (r as LocationModel).mLocation
                    LocationDialog(mLocationList!!, mCategory!!)
                } else   AppDialogs.customSuccessAction(
                    this,
                    null,
                    r.response!!.responseMessage!!,
                    null,
                    null,
                    false
                )
            } else if (r.requestType!! == AppServices.API.savepersonalsetting.hashCode()) {
                AppDialogs.customSuccessAction(
                    this,
                    null,
                    r.response!!.responseMessage!!,
                    null,
                    null,
                    false
                )
                /*  if (r.response!!.isSuccess) {
                      var mLocationList = (r as LocationModel).mLocation
                      LocationDialog(mLocationList!!, mCategory!!)
                  } else AppDialogs.showToastDialog(this, r.response!!.responseMessage!!)*/
            }
        }
    }

    fun setValues(aValues: PersonalSettingModel) {

        mDoorNoEdit!!.setText(aValues.doorNo)
        mStreetNameEdit!!.setText(aValues.streetName)
        mAddressEdit!!.setText(aValues.address)
        mHomeTownEdit!!.setText(aValues.homeTown)
        mPinCodeEdit!!.setText(aValues.pinCode)

        mCountryTV!!.setText(aValues.countryName)
        mStateTV!!.setText(aValues.stateName)
        mCityTV!!.setText(aValues.currentCityName)

        mCountryId = aValues.countryId
        mCityId = aValues.currentCityId
        mStateId = aValues.stateId
    }

    fun loadValues() {

        AppDialogs.showProgressDialog(mContext!!)
        val result = Helper.GenerateEncrptedUrl(
            BuildConfig.API_URL,
            getAccountSettingCaseString(user!!.mUserId!!, "PersonalSetting")!!
        )
        AppServices.execute(
            mContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.perosnalsetting,
            PersonalSettingModel::class.java
        )
    }

    private fun getAccountSettingCaseString(
        aUserId: String,
        aFromScreen: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", aUserId)
            val jsonParam = JSONObject()
            when (aFromScreen) {
                "AccountSetting" -> {
                    jsonParam.put("AccountSettingList", jsonParam1)
                    Log.e("AccountSettingList", " $jsonParam")
                }
                "PrivacyScreen" -> {
                    jsonParam.put("PrivacySettingsList", jsonParam1)
                    Log.e("PrivacySettingsList", " $jsonParam")
                }
                "PersonalSetting" -> {
                    jsonParam.put("PersonalSettingList", jsonParam1)
                    Log.e("PersonalSettingList", " $jsonParam")
                }
                "ProfileSetting" -> {
                    jsonParam.put("ProfileSettingList", jsonParam1)
                    Log.e("ProfileSettingList", " $jsonParam")
                }
            }
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    fun getLocationDetails() {
        AppDialogs.showProgressDialog(mContext)
        // if (Helper.isValidEmail("")) {
        val mCase = getCountryStateCityApiCaseString(mCategory!!, mCategoryId!!, mStateId!!)
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
        Log.e("URL", result)
        AppServices.getCountryStateCityApi(this, result)
        /* } else {
             AppDialogs.customOkAction(
                 this,
                 getString(R.string.app_name),
                 "Enter aValid email",
                 null,
                 null,
                 true
             )

         }*/
    }

    private fun getCountryStateCityApiCaseString(
        acategory: String,
        acategory_id: String,
        aStateid: String
    ): String? {
        var aCaseStr = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("category", acategory)
            jsonParam1.put("category_id", acategory_id)
            jsonParam1.put("state_id", aStateid)
            val jsonParam = JSONObject()
            jsonParam.put("GetAddressList", jsonParam1)
            Log.e("getCountryStateCityCase", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            Log.e("getCountryStateCityCase", " $aCaseStr")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    private fun LocationDialog(
        aCountryInfoList: ArrayList<LocationModel>,
        aCategory: String
    ) {
        try {
            var aTitle = ""
            when (aCategory) {
                "0" -> aTitle = "Choose Country"
                "1" -> aTitle = "Choose State"
                "2" -> aTitle = "Choose City"
            }
            myDialog = AlertDialog.Builder(mContext)
            myDialog!!.setCancelable(false)
            val view =
                LayoutInflater.from(mContext).inflate(R.layout.search_alert_custom_dialog, null)

            val searchCloseIMG = view.findViewById<View>(R.id.searchCloseIMG) as ImageView
            val myAlertSearchEDT = view.findViewById<View>(R.id.searchview) as EditText
            val myAlertListview =
                view.findViewById<View>(R.id.layout_inflate_service_selection_LV) as ListView
            val myAlertNodata =
                view.findViewById<View>(R.id.layout_text_nodata) as TextView
            val myCloseBTN =
                view.findViewById<View>(R.id.screen_alert_custom_dialog_close_BTN) as ImageButton
            (view.findViewById<View>(R.id.screen_alert_custom_dialog_title_TXT) as TextView).text =
                aTitle
            myAlertSearchEDT.setText("")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val colorStateList =
                    ColorStateList.valueOf(mContext.getResources().getColor(R.color.transparent))
                myAlertSearchEDT.setBackgroundTintList(colorStateList)
            }
            myAdapter = LocationSearchFilterAdapter(mContext, aCountryInfoList, this)
            myAlertListview.setAdapter(myAdapter)
            try {
                if (myAdapter!!.count == 0) {
                    myAlertSearchEDT.setVisibility(View.GONE)
                    // myAlertNodata.setText(getString(R.string.no_search_filter_alret));
                    myAlertNodata.setVisibility(View.VISIBLE)
                } else {
                    myAlertNodata.setVisibility(View.GONE)
                    myAlertSearchEDT.setVisibility(View.VISIBLE)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            searchCloseIMG.setOnClickListener {
                myAlertSearchEDT.setText("")
                myAdapter = LocationSearchFilterAdapter(mContext, aCountryInfoList, this)
                myAlertListview.setAdapter(myAdapter)
            }
            myAlertSearchEDT.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(aEditable: Editable) {
                    if (myAdapter != null) {
                        //    myAdapter!!.getFilter().fil

                        myAdapter!!.getFilter()!!.filter(aEditable, {
                            if (myAdapter!!.count == 0) {
                                myAlertNodata.text = "No data found for \" $aEditable\""
                                myAlertNodata.visibility = View.VISIBLE
                            } else {
                                myAlertNodata.visibility = View.GONE
                            }
                        })

                    }

                    /*   if (myAdapter != null) myAdapter!!.getFilter()!!.filter(
                           aEditable
                       ) {
                           if (myAdapter!!.count == 0) {
                               myAlertNodata.text = "No data found for \" $aEditable\""
                               myAlertNodata.visibility = View.VISIBLE
                           } else {
                               myAlertNodata.visibility = View.GONE
                           }
                       }*/
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    try {
                        if (s.length > 0) {
                            myAlertSearchEDT.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_search_white,
                                0,
                                0,
                                0
                            )
                            searchCloseIMG.visibility = View.VISIBLE
                        } else {
                            myAlertSearchEDT.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_search_white,
                                0,
                                0,
                                0
                            )
                            searchCloseIMG.visibility = View.INVISIBLE
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            })
            myCloseBTN.setOnClickListener(View.OnClickListener { myalertDialog!!.dismiss() })


            /*  myAlertSearchEDT.setOnTouchListener(OnTouchListener { v, event ->
                  val DRAWABLE_LEFT = 0
                  val DRAWABLE_TOP = 1
                  val DRAWABLE_RIGHT = 2
                  val DRAWABLE_BOTTOM = 3
                  if (!myAlertSearchEDT.getText().toString().isEmpty()) {
                      if (event.action == MotionEvent.ACTION_UP) {
                          if (event.rawX >= myAlertSearchEDT.getRight() -
                              myAlertSearchEDT.getCompoundDrawables().get(DRAWABLE_RIGHT).getBounds().width()
                          ) { // your action here
                              myAlertSearchEDT.setText("")
                              myAlertSearchEDT.setCursorVisible(true)
                              myAlertSearchEDT.setCompoundDrawablesWithIntrinsicBounds(
                                  R.drawable.ic_search_white,
                                  0,
                                  0,
                                  0
                              )
                              return@OnTouchListener true
                          }
                      }
                  }
                  return@OnTouchListener false
              })*/
            //  myDialog!!.setView(view)
            //     myDialog!!.setCancelable(true)
            myDialog!!.setView(view)
            myDialog!!.setCancelable(true)
            myalertDialog = myDialog!!.show()
            myalertDialog!!.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(resources.getColor(R.color.colorAccent))
            myalertDialog!!.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(aName: String?, aId: String?, aCode: String?, alocation: LocationModel?) {
        when (mCategory) {
            "0" -> {
                mCountryTV!!.setText(aName)
                mCountryId = aId
                mStateTV!!.setText("")
                mCityTV!!.setText("")
                mStateId = "0"
                mCityId = "0"
                mStateTV!!.setHint(mContext.resources.getString(R.string.label_state))
                mCityTV!!.setHint(mContext.resources.getString(R.string.label_city))
            }
            "1" -> {
                mStateTV!!.setText(aName)
                mStateId = aId
                mCityTV!!.setText("")
                mCityId = "0"
                mCityTV!!.setHint(mContext.resources.getString(R.string.label_city))
            }
            "2" -> {
                mCityTV!!.setText(aName)
                mCityId = aId
            }
        }
        myalertDialog!!.dismiss()
    }

    fun saveCaseAPI() {

        AppDialogs.showProgressDialog(mContext!!)
        val result = Helper.GenerateEncrptedUrl(
            BuildConfig.API_URL,
            PostPersonalSettingCaseString(
                user!!.mUserId!!,
                getETValue(mDoorNoEdit),
                getETValue(mHomeTownEdit),
                getETValue(mStreetNameEdit),
                getETValue(mPinCodeEdit),
                mCountryId!!,
                mStateId!!,
                mCityId!!,
                getETValue(mAddressEdit)
            )!!
        )
        AppServices.execute(
            mContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.savepersonalsetting,
            Response::class.java
        )
    }

    private fun PostPersonalSettingCaseString(
        auserId: String,
        aDoorNo: String,
        aHometown: String,
        aStreetName: String,
        aPincode: String,
        aCountry: String,
        aStateId: String,
        aCurrentCity: String,
        aAddress: String
    ): String? {
        var aCaseStr: String? = ""
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("door_no", aDoorNo)
            jsonParam1.put("home_town", aHometown)
            jsonParam1.put("street_name", aStreetName)
            jsonParam1.put("pincode", aPincode)
            jsonParam1.put("user_id", auserId)
            jsonParam1.put("country_id", aCountry)
            jsonParam1.put("state_id", aStateId)
            jsonParam1.put("current_city_id", aCurrentCity)
            jsonParam1.put("address", aAddress)
            val jsonParam = JSONObject()
            jsonParam.put("UpdateAddress", jsonParam1)
            Log.e("UpdateAddress", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }
}
