package com.truelife.app.activity

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.adapter.LocationSearchFilterAdapter
import com.truelife.app.constants.TLConstant
import com.truelife.app.listeners.SelectedListener
import com.truelife.app.model.LocationModel
import com.truelife.app.model.RegisterUser
import com.truelife.base.BaseActivity
import com.truelife.base.progressInterface
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.util.AppDialogs
import com.truelife.util.DateUtil
import com.truelife.util.Helper
import com.truelife.util.SingleChoiceAdapter
import kotlinx.android.synthetic.main.activity_signup.*
import org.json.JSONObject
import java.util.*


/*Developer Name : Elangovan*/

class TLSignupActivity : BaseActivity(), ResponseListener, SelectedListener, progressInterface {

    lateinit var mContext: FragmentActivity;
    var mCode: String? = ""
    private var myalertDialog: android.app.AlertDialog? = null
    private var myAdapter: LocationSearchFilterAdapter? = null
    private var myDialog: AlertDialog.Builder? = null
    private var mCategory: String? = null
    private var mCountryId: String? = null
    private var mCategoryId: String? = null
    private var mStateId: String? = null
    private var mCityId: String? = null;

    /* Variable Initialization*/
    var mCountryList = ArrayList<LocationModel>()
    var mGenderVal: String? = null;

    override fun clickListener() {

        country_code_EDT.setOnClickListener {

            if (mCountryList.size == 0)
                showCountry()
            else {
                val list = ArrayList<String>()
                for (i in mCountryList.indices)
                    list.add(mCountryList[i].name!!)
                initCountry(list)
            }
        }

        selection_email_lay.setOnClickListener {
            activity_signup_EDT_email.setText("")
            activity_signup_EDT_mobile.setText("")
            country_code_EDT.setText("")
            mCode = ""
            email_or_mob_selection_lay.visibility = View.GONE
            email_lay.visibility = View.VISIBLE
            email_or_mobile_line.visibility = View.VISIBLE
            activity_signup_EDT_email.requestFocus()
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(activity_signup_EDT_email, InputMethodManager.SHOW_IMPLICIT)
        }

        selection_mobile_lay.setOnClickListener {
            activity_signup_EDT_email.setText("")
            activity_signup_EDT_mobile.setText("")
            country_code_EDT.setText("")
            mCode = ""
            email_or_mob_selection_lay.visibility = View.GONE
            mobile_lay.visibility = View.VISIBLE
            email_lay.visibility = View.GONE
            email_or_mobile_line.visibility = View.VISIBLE
            activity_signup_EDT_mobile.requestFocus()
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(activity_signup_EDT_mobile, InputMethodManager.SHOW_IMPLICIT)
        }

        email_img.setOnClickListener {
            activity_signup_EDT_email.setText("")
            activity_signup_EDT_mobile.setText("")
            country_code_EDT.setText("")
            mCode = ""
            email_or_mob_selection_lay.visibility = View.GONE
            email_lay.visibility = View.GONE
            mobile_lay.visibility = View.VISIBLE
            email_or_mobile_line.visibility = View.VISIBLE
            activity_signup_EDT_mobile.requestFocus()
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(activity_signup_EDT_mobile, InputMethodManager.SHOW_IMPLICIT)
        }


        mobile_img.setOnClickListener {
            email_or_mob_selection_lay.visibility = View.GONE
            mobile_lay.visibility = View.GONE
            email_lay.visibility = View.VISIBLE
            activity_signup_EDT_email.setText("")
            activity_signup_EDT_mobile.setText("")
            country_code_EDT.setText("")
            mCode = ""
            email_or_mobile_line.visibility = View.VISIBLE
            activity_signup_EDT_email.requestFocus()
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(activity_signup_EDT_mobile, InputMethodManager.SHOW_IMPLICIT)
        }

        activity_log_in_TXT.setOnClickListener {
            startActivity(Intent(mContext, TLSigninActivity::class.java))
            finish()
        }


        policy_text.setOnClickListener {
            AppDialogs.alertDialogwebViw(mContext, getString(R.string.url_privacy))

        }
        terms_text.setOnClickListener {
            AppDialogs.alertDialogwebViw(mContext, getString(R.string.url_terms))
        }

        activity_vip_button.setOnClickListener {
            startActivity(Intent(mContext, TLCelebrityLogin::class.java))
        }
        activity_country_signup.setOnClickListener {
            mCategory = "0"
            mStateId = "0"
            mCategoryId = "0"

            getLocationDetails()
            activity_signup_EDT_password.requestFocus()
        }

        activity_state_lay.setOnClickListener {
            if (!getTXTValue(activity_signup_EDT_country).isNullOrEmpty()) {
                mCategory = "1"
                mCategoryId = mCountryId
                mStateId = "0"
                getLocationDetails()
            } else {
                AppDialogs.customOkAction(this, null, "Please select Country", null, null, false)
            }
        }

        activity_city_lay.setOnClickListener {
            if (!getTXTValue(activity_signup_EDT_state).isNullOrEmpty()) {
                mCategory = "2"
                mCategoryId = mCountryId
                getLocationDetails()
            } else {
                AppDialogs.customOkAction(this, null, "Please select State", null, null, false)
            }
        }

        activity_signin_RV_signup.setOnClickListener {
            if (validation()) {

                AppDialogs.customDoubleAction(mContext,"","Do you consent to the use/ processing of your personally identifiable data by True Life?",
                "Consent","Do not consent",object : AppDialogs.OptionListener{
                        override fun no() {
                            setRegisterData("0")
                        }

                        override fun yes() {
                            setRegisterData("1")
                        }

                    },false,false)

            }
        }
    }

    fun setRegisterData(value : String){
        val aUser = RegisterUser()

        aUser.mFirstName = activity_signup_EDT_firstname.text.toString()
        aUser.mLastMame = activity_signup_EDT_lastname.text.toString()
        aUser.mPassword = activity_signup_EDT_password.text.toString()
        if (activity_signup_EDT_email.text.toString().isNotEmpty()) {
            aUser.mEmail = activity_signup_EDT_email.text.toString()
        } else if (activity_signup_EDT_mobile.text.toString()
                .isNotEmpty() && country_code_EDT.text.toString().isNotEmpty()
        ) {
            aUser.mCountryCode = mCode
            aUser.mMobileNo = activity_signup_EDT_mobile.text.toString()
        }
        aUser.mPinCode = activity_signup_EDT_pincode.text.toString()
        aUser.mGender = mGenderVal
        aUser.mStreetName = ""
        aUser.mCityId = mCityId
        aUser.mStateId = mStateId
        aUser.mCountryId = mCountryId
        var uName = activity_signup_EDT_firstname.text.toString().trim()
        if (uName.contains("")) {
            val na = uName.split(" ")
            uName = na[0]
        }
        aUser.mUserName = uName.trim() + DateUtil.currentDateAsTimeStamp
        //  aUser.mUserName = "Elango".trim() + DateUtil.currentDateAsTimeStamp.trim()
        aUser.mDob = ""
        getRegisterDetails(aUser, value)
    }

    override fun init() {
        mContext = this;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        init()
        clickListener()
    }


    fun getLocationDetails() {

        AppDialogs.showProgressDialog(mContext)
        // if (Helper.isValidEmail("")) {
        val mCase = getCountryStateCityApiCaseString(mCategory!!, mCategoryId!!, mStateId!!)
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
        Log.e("URL", result)
        AppServices.getCountryStateCityApi(this,this, result)
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

    fun getRegisterDetails(aUser: RegisterUser, value : String) {
        AppDialogs.showProgressDialog(mContext)
        // if (Helper.isValidEmail("")) {
        val mCase = getUserDataApiCaseString(aUser,value)
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)

        AppServices.execute(
            this,
            result,
            Request.Method.POST,
            AppServices.API.register,
            Response::class.java
        )
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


    private fun getUserDataApiCaseString(
        aUser: RegisterUser, value:String
    ): String? {
        var aCaseStr = " "
        try {

            val aUserJson = JSONObject()
            if (aUser.mEmail.isNullOrEmpty())
                Log.e("email empty", "")
            // aUserJson.put("email", "")
            else
                aUserJson.put("email", aUser.mEmail)
            aUserJson.put("mobile_no", aUser.mMobileNo)
            aUserJson.put("username", aUser.mUserName!!.trim())
            aUserJson.put("password", aUser.mPassword)
            aUserJson.put("last_name", aUser.mLastMame!!.trim())
            aUserJson.put("first_name", aUser.mFirstName!!.trim())
            aUserJson.put("country_code", aUser.mCountryCode)
            aUserJson.put("allow_personal_data", value)

            var aPersonal = JSONObject()
            aPersonal.put("gender", aUser.mGender)
            aPersonal.put("date_of_birth", aUser.mDob)

            val aAddressJson = JSONObject()
            aAddressJson.put("state_id", aUser.mStateId)
            aAddressJson.put("street_name", aUser.mStreetName)
            aAddressJson.put("country_id", aUser.mCountryId)
            aAddressJson.put("pincode", aUser.mPinCode)
            aAddressJson.put("current_city_id", aUser.mCityId)


            val jsonParam = JSONObject()
            jsonParam.put("User", aUserJson)
            jsonParam.put("Personal", aPersonal)
            jsonParam.put("Address", aAddressJson)

            val jsonParam1 = JSONObject()
            jsonParam1.put("RegisterUser", jsonParam)

            Log.e("RegisterUserCase", " $jsonParam1")
            aCaseStr = Base64.encodeToString(jsonParam1.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    private fun getStringFromEditText(aEdittext: EditText): String? {
        return aEdittext.text.toString().trim { it <= ' ' }
    }

    private fun validation(): Boolean {
        if (getStringFromEditText(activity_signup_EDT_firstname)!!.length == 0) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_first_name),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                    }

                },
                false
            )
        } else if (getStringFromEditText(activity_signup_EDT_lastname)!!.length == 0) {

            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_last_name),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                    }

                },
                false
            )
        } else if (getStringFromEditText(activity_signup_EDT_email)!!.length == 0 && getStringFromEditText(
                activity_signup_EDT_mobile
            )!!.length == 0
        ) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_email_or_mobile),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                    }

                },
                false
            )

        } else if (getStringFromEditText(activity_signup_EDT_email)!!.length > 0 && !Helper.isValidEmail(
                getStringFromEditText(activity_signup_EDT_email)!!
            )
        ) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_valid_email),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                    }
                },
                false
            )
        } else if (getStringFromEditText(activity_signup_EDT_mobile)!!.length > 0 && !Helper.isValidMobileNo(
                getStringFromEditText(activity_signup_EDT_mobile)!!
            )
        ) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_valid_mobile),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                    }
                },
                false
            )
        } else if (getStringFromEditText(activity_signup_EDT_mobile)!!.length > 0 && Helper.isValidMobileNo(
                getStringFromEditText(activity_signup_EDT_mobile)!!
            ) && getETValue(country_code_EDT).length == 0
        ) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_valid_country_code),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                    }
                },
                false
            )
        } else if (getStringFromEditText(activity_signup_EDT_password)!!.length == 0) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_password),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                    }

                },
                false
            )
        } else if (getStringFromEditText(activity_signup_EDT_password)!!.length < 7) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_valid_password),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                    }

                },
                false
            )

        } else if (getStringFromEditText(activity_signup_EDT_confirm_password)!!.length == 0) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_confirm_password),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                        activity_signup_EDT_confirm_password.getParent().requestChildFocus(activity_signup_EDT_confirm_password,activity_signup_EDT_confirm_password)
                        activity_signup_EDT_confirm_password.setError("Enter Confirm Password")
                    }

                },
                false
            )

        } else if (getStringFromEditText(activity_signup_EDT_password) != getStringFromEditText(
                activity_signup_EDT_confirm_password
            )
        ) {

            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_confirm_password),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {

                    }
                },
                false
            )
        } else if (activity_signup_EDT_country.getText().toString().isEmpty()) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_country),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                        activity_signup_EDT_country.getParent().requestChildFocus(activity_signup_EDT_country,activity_signup_EDT_country)
                       // scrollview.smoothScrollTo(0, activity_signup_EDT_country.bottom + 460)
                    }

                },
                false
            )
        } else if (activity_signup_EDT_state.getText().toString().isEmpty()) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_state),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                        activity_signup_EDT_state.getParent().requestChildFocus(activity_signup_EDT_state,activity_signup_EDT_state)
                        //  scrollview.smoothScrollTo(0,(activity_signup_EDT_state.bottom+40))
                    }
                },
                false
            )
        } else if (activity_signup_EDT_city.getText().toString().isEmpty()) {

            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_city),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                        activity_signup_EDT_city.getParent().requestChildFocus(activity_signup_EDT_city,activity_signup_EDT_city)
                   //      scrollview.scrollTo(0,activity_signup_EDT_city.bottom+120)
                    }

                },
                false
            )

        } else if (getStringFromEditText(activity_signup_EDT_pincode)!!.length == 0) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_Pincode),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                        activity_signup_EDT_pincode.getParent().requestChildFocus(activity_signup_EDT_pincode,activity_signup_EDT_pincode)
                       // scrollview.scrollTo(0,activity_signup_EDT_pincode.bottom+160)
                    }

                },
                false
            )
        } else if (getStringFromEditText(activity_signup_EDT_pincode)!!.length < 6) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_Pincode_size),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                        activity_signup_EDT_pincode.getParent().requestChildFocus(activity_signup_EDT_pincode,activity_signup_EDT_pincode)
                        // scrollview.scrollTo(0,activity_signup_EDT_pincode.bottom+160)
                    }

                },
                false
            )
        } else if (!male_Rd.isChecked() && !female_Rd.isChecked()) {

            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_gender),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                        lay_gender.getParent().requestChildFocus(lay_gender,lay_gender)
                        //   scrollview.scrollTo(0,male_Rd.bottom+200)
                    }

                },
                false
            )

        } else if (!agree_btn.isChecked()) {


            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_agree_terms),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                    }

                },
                false
            )

        } else {
            if (male_Rd.isChecked()) {
                mGenderVal = "1";
            } else {
                mGenderVal = "2";
            }
            return true
        }
        return false
    }

    override fun onResponse(r: Response?) {
        // hideProgress()
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.country.hashCode()) {
                if (r.response!!.isSuccess) {
                    var mLocationList = (r as LocationModel).mLocation
                    LocationDialog(mLocationList!!, mCategory!!)
                } else AppDialogs.showToastDialog(this, r.response!!.responseMessage!!)
            } else if (r.requestType!! == AppServices.API.countryCode.hashCode()) {
                if (r.response!!.isSuccess) {
                    mCountryList = (r as LocationModel).mLocation!!
                    if (mCountryList.isNotEmpty()) {
                        val list = ArrayList<String>()
                        for (i in mCountryList.indices)
                            list.add(mCountryList[i].name!!)
                        initCountry(list)
                    }
                } else AppDialogs.customOkAction(
                    mContext,
                    null,
                    r.response!!.responseMessage!!,
                    null,
                    object : AppDialogs.ConfirmListener {
                        override fun yes() {
                            AppDialogs.hidecustomView()
                        }
                    },
                    false
                )
            } else if (r.requestType!! == AppServices.API.register.hashCode()) {
                if (r.response!!.isSuccess) {
                    AppDialogs.customSuccessAction(
                        mContext,
                        null,
                        getString(R.string.lable_register_success),
                        null,
                        object : AppDialogs.ConfirmListener {
                            override fun yes() {
                                onBackPressed()
                            }

                        },
                        false
                    )
                } else {
                    AppDialogs.customSuccessAction(
                        this,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        null,
                        false
                    )
                }
            }
        } else AppDialogs.customOkAction(this, null, TLConstant.SERVER_NOT_REACH, null, null, false)
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


            val myAlertSearchEDT = view.findViewById<View>(R.id.searchview) as EditText
            val searchCloseIMG = view.findViewById<View>(R.id.searchCloseIMG) as ImageView
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
                activity_signup_EDT_country.setText(aName)
                mCountryId = aId
                mCityId = ""
                mStateId = ""
                activity_signup_EDT_state.setText("")
                activity_signup_EDT_city.setText("")
                activity_signup_EDT_state.setHint(
                    mContext.getResources().getString(R.string.label_state)
                )
                activity_signup_EDT_city.setHint(
                    mContext.getResources().getString(R.string.label_city)
                )
            }
            "1" -> {
                activity_signup_EDT_state.setText(aName)
                mStateId = aId
                mCityId = ""
                activity_signup_EDT_city.setText("")
                activity_signup_EDT_city.setHint(
                    mContext.getResources().getString(R.string.label_city)
                )
            }
            "2" -> {
                activity_signup_EDT_city.setText(aName)
                mCityId = aId
            }
        }
        myalertDialog!!.dismiss()
    }

    private fun showCountry() {
        if (checkInternet()) {
            AppDialogs.showProgressDialog(mContext)
            val mCase = getListParam()
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
            AppServices.execute(
                mContext, this,
                result,
                Request.Method.POST,
                AppServices.API.countryCode,
                LocationModel::class.java
            )
        }
    }

    private fun getListParam(): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("category", "0")
            jsonObject.put("category_id", "0")
            jsonObject.put("state_id", "0")
            val jsonParam = JSONObject()
            jsonParam.put("GetAddressList", jsonObject)

            Log.i("GetAddressList --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun initCountry(list: ArrayList<String>) {
        AppDialogs.showSingleChoice(
            mContext!!,
            "Country",
            list,
            object : SingleChoiceAdapter.Callback {
                override fun info(position: Int, text: String) {
                    AppDialogs.hideSingleChoice()
                    for (i in mCountryList.indices) {
                        if (text == mCountryList[i].name!!) {
                            country_code_EDT.setText(
                                String.format("+%s", mCountryList[i].code)
                            )

                            mCode = String.format("+%s", mCountryList[i].code)
                        }
                    }

                }
            }, true, isSearch = true
        )
    }
}