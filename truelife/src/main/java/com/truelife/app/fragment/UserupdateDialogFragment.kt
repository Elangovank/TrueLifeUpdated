package com.truelife.app.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
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
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.adapter.LocationSearchFilterAdapter
import com.truelife.app.constants.TLConstant
import com.truelife.app.listeners.DailogFragmentToFragmentListener
import com.truelife.app.listeners.SelectedListener
import com.truelife.app.model.LocationModel
import com.truelife.app.model.RegisterUser
import com.truelife.app.model.User
import com.truelife.base.progressInterface
import com.truelife.databinding.FragmentUserupdateDialogBinding
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.*
import kotlinx.android.synthetic.main.activity_signup.*
import org.json.JSONObject
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class UserupdateDialogFragment : DialogFragment(), ResponseListener, SelectedListener,
    progressInterface {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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

    lateinit var mContext: Context

    private lateinit var binding: FragmentUserupdateDialogBinding

    lateinit var userData: User

    lateinit var dailogFragmentToFragmentListener: DailogFragmentToFragmentListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireContext()

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }


    }

    private fun initCountry(list: ArrayList<String>) {
        AppDialogs.showSingleChoice(
            mContext,
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

    fun clickListener() {

      /*  binding.countryCodeEDT.setOnClickListener {

            if (mCountryList.size == 0)
                showCountry()
            else {
                val list = ArrayList<String>()
                for (i in mCountryList.indices)
                    list.add(mCountryList[i].name!!)
                initCountry(list)
            }
        }*/

        /*binding.selectionEmailLay.setOnClickListener {
            binding.activitySignupEDTEmail.setText("")
            binding.activitySignupEDTMobile.setText("")
            binding.countryCodeEDT.setText("")
            mCode = ""
            binding.emailOrMobSelectionLay.visibility = View.GONE
            binding.emailLay.visibility = View.VISIBLE
            binding.emailOrMobileLine.visibility = View.VISIBLE
            binding.activitySignupEDTEmail.requestFocus()
            val imm: InputMethodManager =
                mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.activitySignupEDTEmail, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.selectionMobileLay.setOnClickListener {
            binding.activitySignupEDTEmail.setText("")
            binding.activitySignupEDTMobile.setText("")
            binding.countryCodeEDT.setText("")
            mCode = ""
            binding.emailOrMobSelectionLay.visibility = View.GONE
            binding.mobileLay.visibility = View.VISIBLE
            binding.emailLay.visibility = View.GONE
            binding.emailOrMobileLine.visibility = View.VISIBLE
            binding.activitySignupEDTEmail.requestFocus()
            val imm: InputMethodManager =
                mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.activitySignupEDTMobile, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.emailImg.setOnClickListener {
            binding.activitySignupEDTEmail.setText("")
            binding.activitySignupEDTMobile.setText("")
            binding.countryCodeEDT.setText("")
            mCode = ""
            binding.emailOrMobSelectionLay.visibility = View.GONE
            binding.emailLay.visibility = View.GONE
            binding.mobileLay.visibility = View.VISIBLE
            binding.emailOrMobileLine.visibility = View.VISIBLE
            binding.activitySignupEDTMobile.requestFocus()
            val imm: InputMethodManager =
                mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.activitySignupEDTMobile, InputMethodManager.SHOW_IMPLICIT)
        }*/


        /* binding.mobileImg.setOnClickListener {
             email_or_mob_selection_lay.visibility = View.GONE
             mobile_lay.visibility = View.GONE
             email_lay.visibility = View.VISIBLE
             binding.activitySignupEDTEmail.setText("")
               binding.activitySignupEDTMobile.setText("")
             country_code_EDT.setText("")
             mCode = ""
             email_or_mobile_line.visibility = View.VISIBLE
             binding.activitySignupEDTEmail.requestFocus()
             val imm: InputMethodManager =
                 mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
             imm.showSoftInput(  binding.activitySignupEDTMobile, InputMethodManager.SHOW_IMPLICIT)
         }*/


        /*policy_text.setOnClickListener {
            AppDialogs.alertDialogwebViw(mContext, getString(R.string.url_privacy))

        }
        terms_text.setOnClickListener {
            AppDialogs.alertDialogwebViw(mContext, getString(R.string.url_terms))
        }

        activity_vip_button.setOnClickListener {
            startActivity(Intent(mContext, TLCelebrityLogin::class.java))
        }*/
        binding.activityCountrySignup.setOnClickListener {
            mCategory = "0"
            mStateId = "0"
            mCategoryId = "0"

            getLocationDetails()
            binding.activitySignupEDTPassword.requestFocus()
        }

        binding.activityStateLay.setOnClickListener {
            if (!getTXTValue(binding.activitySignupEDTCountry).isNullOrEmpty()) {
                mCategory = "1"
                mCategoryId = mCountryId
                mStateId = "0"
                getLocationDetails()
            } else {
                AppDialogs.customOkAction(
                    mContext,
                    null,
                    "Please select Country",
                    null,
                    null,
                    false
                )
            }
        }

        binding.activityCityLay.setOnClickListener {
            if (!getTXTValue(binding.activitySignupEDTState).isNullOrEmpty()) {
                mCategory = "2"
                mCategoryId = mCountryId
                getLocationDetails()
            } else {
                AppDialogs.customOkAction(mContext, null, "Please select State", null, null, false)
            }
        }

        binding.activitySigninRVSignup.setOnClickListener {
            if (validation()) {

                AppDialogs.customDoubleAction(
                    mContext,
                    "",
                    "Do you consent to the use/ processing of your personally identifiable data by True Life?",
                    "Consent",
                    "Do not consent",
                    object : AppDialogs.OptionListener {
                        override fun no() {
                            setRegisterData("0")
                        }

                        override fun yes() {
                            setRegisterData("1")
                        }

                    },
                    false,
                    false
                )

            }
        }
    }

    private fun validation(): Boolean {
        if (getStringFromEditText(binding.activitySignupEDTFirstname)!!.length == 0) {
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
        } else if (getStringFromEditText(binding.activitySignupEDTLastname)!!.length == 0) {

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
        } /*else if (getStringFromEditText(binding.activitySignupEDTEmail)!!.length == 0 && getStringFromEditText(
                binding.activitySignupEDTMobile
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

        } else if (getStringFromEditText(binding.activitySignupEDTEmail)!!.length > 0 && !Helper.isValidEmail(
                getStringFromEditText(binding.activitySignupEDTEmail)!!
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
        }*/ /*else if (getStringFromTextView(binding.activitySignupEDTMobile)!!.length > 0 && !Helper.isValidMobileNo(
                getStringFromTextView(binding.activitySignupEDTMobile)!!
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
        }*//* else if (getStringFromTextView(binding.activitySignupEDTMobile)!!.length > 0 && Helper.isValidMobileNo(
                getStringFromTextView(binding.activitySignupEDTMobile)!!
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
        }*/ /*else if (getStringFromEditText(binding.activitySignupEDTPassword)!!.length == 0) {
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
        }else if (getStringFromEditText(  binding.activitySignupEDTPassword)!!.length < 7) {
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
                        activity_signup_EDT_confirm_password.getParent().requestChildFocus(
                            activity_signup_EDT_confirm_password,
                            activity_signup_EDT_confirm_password
                        )
                        activity_signup_EDT_confirm_password.setError("Enter Confirm Password")
                    }

                },
                false
            )

        } else if (getStringFromEditText(  binding.activitySignupEDTPassword) != getStringFromEditText(
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
        } */ else if (binding.activitySignupEDTCountry.getText().toString().isEmpty()) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_country),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                        binding.activitySignupEDTCountry.getParent().requestChildFocus(
                            binding.activitySignupEDTCountry,
                            binding.activitySignupEDTCountry
                        )
                        // scrollview.smoothScrollTo(0,   binding.activitySignupEDTCountry.bottom + 460)
                    }

                },
                false
            )
        } else if (binding.activitySignupEDTState.getText().toString().isEmpty()) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_state),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                        binding.activitySignupEDTState.getParent()
                            .requestChildFocus(
                                binding.activitySignupEDTState,
                                binding.activitySignupEDTState
                            )
                        //  scrollview.smoothScrollTo(0,(binding.activitySignupEDTState.bottom+40))
                    }
                },
                false
            )
        } else if (binding.activitySignupEDTCity.getText().toString().isEmpty()) {

            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_city),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                        binding.activitySignupEDTCity.getParent()
                            .requestChildFocus(
                                binding.activitySignupEDTCity,
                                binding.activitySignupEDTCity
                            )
                        //      scrollview.scrollTo(0,  binding.activitySignupEDTCity.bottom+120)
                    }

                },
                false
            )

        } else if (getStringFromEditText(binding.activitySignupEDTPincode)!!.length == 0) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_Pincode),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                        binding.activitySignupEDTPincode.getParent().requestChildFocus(
                            binding.activitySignupEDTPincode,
                            binding.activitySignupEDTPincode
                        )
                        // scrollview.scrollTo(0,  binding.activitySignupEDTPincode.bottom+160)
                    }

                },
                false
            )
        } else if (getStringFromEditText(binding.activitySignupEDTPincode)!!.length < 6) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_Pincode_size),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                        binding.activitySignupEDTPincode.getParent().requestChildFocus(
                            binding.activitySignupEDTPincode,
                            binding.activitySignupEDTPincode
                        )
                        // scrollview.scrollTo(0,  binding.activitySignupEDTPincode.bottom+160)
                    }

                },
                false
            )
        } else if (!binding.maleRd.isChecked() && !binding.femaleRd.isChecked()) {

            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_empty_gender),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                        lay_gender.getParent().requestChildFocus(lay_gender, lay_gender)
                        //   scrollview.scrollTo(0,male_Rd.bottom+200)
                    }

                },
                false
            )

        } /*else if (!agree_btn.isChecked()) {


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

        }*/ else {
            if (binding.maleRd.isChecked()) {
                mGenderVal = "1";
            } else {
                mGenderVal = "2";
            }
            return true
        }
        return false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserupdateDialogBinding.inflate(layoutInflater)

        userData = LocalStorageSP.getLoginUser(mContext)
        // Inflate the layout for this fragment
        clickListener()
        //   val mView = inflater.inflate(R.layout.fragment_userupdate_dialog, container, false)

        return binding.root
    }

    companion object {
        var TAG: String = UserupdateDialogFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(param1: String, listener: DailogFragmentToFragmentListener) =
            UserupdateDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    dailogFragmentToFragmentListener = listener
                }
            }
    }

    fun getLocationDetails() {

        AppDialogs.showProgressDialog(mContext)
        // if (Helper.isValidEmail("")) {
        val mCase = getCountryStateCityApiCaseString(mCategory!!, mCategoryId!!, mStateId!!)
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
        Log.e("URL", result)
        AppServices.getCountryStateCityApi(requireContext(), this, result)
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

    fun getRegisterDetails(aUser: RegisterUser, value: String) {
        AppDialogs.showProgressDialog(mContext)
        // if (Helper.isValidEmail("")) {
        val mCase = getUserDataApiCaseString(aUser, value)
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)

        AppServices.execute(
            requireActivity(), this,
            result,
            Request.Method.POST,
            AppServices.API.register,
            User::class.java
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
        aUser: RegisterUser, value: String
    ): String? {
        var aCaseStr = " "
        try {

            val aUserJson = JSONObject()
            if (aUser.mEmail.isNullOrEmpty())
                Log.e("email empty", "")
            // aUserJson.put("email", "")
            else
                aUserJson.put("email", aUser.mEmail)
            aUserJson.put("id", userData.mUserId)
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
            jsonParam1.put("UpdateUserDetail", jsonParam)

            Log.e("UpdateUserDetail", " $jsonParam1")
            aCaseStr = Base64.encodeToString(jsonParam1.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    private fun getStringFromEditText(aEdittext: EditText): String? {
        return aEdittext.text.toString().trim { it <= ' ' }
    }

    private fun getStringFromTextView(aEdittext: TextView): String? {
        return aEdittext.text.toString().trim { it <= ' ' }
    }

    fun setRegisterData(value: String) {
        val aUser = RegisterUser()

        aUser.mFirstName = binding.activitySignupEDTFirstname.text.toString()
        aUser.mLastMame = binding.activitySignupEDTLastname.text.toString()
        aUser.mPassword = binding.activitySignupEDTPassword.text.toString()
       /* if (binding.activitySignupEDTEmail.text.toString().isNotEmpty()) {
            aUser.mEmail = binding.activitySignupEDTEmail.text.toString()
        } else if (binding.activitySignupEDTMobile.text.toString()
                .isNotEmpty() && country_code_EDT.text.toString().isNotEmpty()
        ) {
            aUser.mCountryCode = mCode
            aUser.mMobileNo = binding.activitySignupEDTMobile.text.toString()
        }*/
        aUser.mPinCode = binding.activitySignupEDTPincode.text.toString()
        aUser.mGender = mGenderVal
        aUser.mStreetName = ""
        aUser.mCityId = mCityId
        aUser.mStateId = mStateId
        aUser.mCountryId = mCountryId
        var uName = binding.activitySignupEDTFirstname.text.toString().trim()
        if (uName.contains("")) {
            val na = uName.split(" ")
            uName = na[0]
        }
        aUser.mUserName = uName.trim() + DateUtil.currentDateAsTimeStamp
        aUser.mDob = ""
        getRegisterDetails(aUser, value)
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

    fun checkInternet(): Boolean {
        return Utility.isInternetAvailable(mContext)
    }

    fun getTXTValue(aTextText: TextView?): String {
        return aTextText?.text?.toString()?.trim { it <= ' ' } ?: ""
    }

    override fun onClick(aName: String?, aId: String?, aCode: String?, alocation: LocationModel?) {
        when (mCategory) {
            "0" -> {
                binding.activitySignupEDTCountry.setText(aName)
                mCountryId = aId
                mCityId = ""
                mStateId = ""
                binding.activitySignupEDTState.setText("")
                binding.activitySignupEDTCity.setText("")
                binding.activitySignupEDTState.setHint(
                    mContext.getResources().getString(R.string.label_state)
                )
                binding.activitySignupEDTCity.setHint(
                    mContext.getResources().getString(R.string.label_city)
                )
            }
            "1" -> {
                binding.activitySignupEDTState.setText(aName)
                mStateId = aId
                mCityId = ""
                binding.activitySignupEDTCity.setText("")
                binding.activitySignupEDTCity.setHint(
                    mContext.getResources().getString(R.string.label_city)
                )
            }
            "2" -> {
                binding.activitySignupEDTCity.setText(aName)
                mCityId = aId
            }
        }
        myalertDialog!!.dismiss()
    }

    override fun showProgress() {

    }

    override fun showProgressCancelable() {

    }

    override fun hideProgress() {

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
            myAdapter =
                LocationSearchFilterAdapter(mContext as FragmentActivity, aCountryInfoList, this)
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
                myAdapter = LocationSearchFilterAdapter(
                    mContext as FragmentActivity,
                    aCountryInfoList,
                    this
                )
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

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.country.hashCode()) {
                if (r.response!!.isSuccess) {
                    var mLocationList = (r as LocationModel).mLocation
                    LocationDialog(mLocationList!!, mCategory!!)
                } else AppDialogs.showToastDialog(mContext, r.response!!.responseMessage!!)
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

                    val user = (r as User).mUserdetails
                    LocalStorageSP.storeLoginUser(mContext, user)
                    AppDialogs.customSuccessAction(
                        mContext,
                        null,
                        getString(R.string.lable_update_profile_success),
                        null,
                        object : AppDialogs.ConfirmListener {
                            override fun yes() {
                                dailogFragmentToFragmentListener.onSuccess()
                                dialog!!.dismiss()
                            }

                        },
                        false
                    )
                } else {
                    AppDialogs.customSuccessAction(
                        mContext,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        null,
                        false
                    )
                }
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

    fun getETValue(aEditText: EditText?): String {
        return aEditText?.text?.toString()?.trim { it <= ' ' } ?: ""
    }
}