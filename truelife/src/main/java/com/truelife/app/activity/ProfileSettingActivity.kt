package com.truelife.app.activity

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Paint
import android.os.Bundle
import android.text.Html
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.adapter.CollageInfoListAdapter
import com.truelife.app.adapter.SchoolInfoListAdapter
import com.truelife.app.adapter.WorkPlaceInfoListAdapter
import com.truelife.app.listeners.ProfileSettingListener
import com.truelife.app.model.ProfileSettingModel
import com.truelife.app.model.User
import com.truelife.base.BaseActivity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.DateUtil
import com.truelife.util.Helper
import kotlinx.android.synthetic.main.activity_profile_setting.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Field
import java.util.*


class ProfileSettingActivity : BaseActivity(), ResponseListener,
    ProfileSettingListener, NumberPicker.OnValueChangeListener, DatePickerDialog.OnDateSetListener {

    lateinit var mContext: FragmentActivity
    var mBackBtn: ImageView? = null
    var mToolbarTitle: TextView? = null
    var mAddSchoolLay: LinearLayout? = null
    var mAddCollegeLay: LinearLayout? = null
    var mAddworkPlaceLay: LinearLayout? = null
    var user: User? = null

    var mSchoolInfoList: ArrayList<ProfileSettingModel.SchoolInfo> = arrayListOf()
    var mCollegeInfoList: ArrayList<ProfileSettingModel.CollageInfo> = arrayListOf()
    var mWorkPlaceInfoList: ArrayList<ProfileSettingModel.WorkPlaceInfo> = arrayListOf()

    var mSelectedDateValue: String? = null
    //adapter

    var mSchoolAdapter: SchoolInfoListAdapter? = null
    var mCollageAdapter: CollageInfoListAdapter? = null
    var mWorkPlaceAdapter: WorkPlaceInfoListAdapter? = null

    var mCurrentYear: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setting)
        init()
        setAdapter()
        loadValues()
        mCurrentYear = Calendar.getInstance().get(Calendar.YEAR);
    }

    fun setNumberPickerTextColor(numberPicker: NumberPicker, color: Int) {
        try {
            val selectorWheelPaintField: Field = numberPicker.javaClass
                .getDeclaredField("mSelectorWheelPaint")
            selectorWheelPaintField.setAccessible(true)
            (selectorWheelPaintField.get(numberPicker) as Paint).setColor(color)
        } catch (e: NoSuchFieldException) {
            Log.w("setNumberPickerColor", e)
        } catch (e: IllegalAccessException) {
            Log.w("setNumberPickerColor", e)
        } catch (e: IllegalArgumentException) {
            Log.w("setNumberPickerColor", e)
        }
        val count = numberPicker.childCount
        for (i in 0 until count) {
            val child: View = numberPicker.getChildAt(i)
            if (child is EditText) (child as EditText).setTextColor(color)
        }
        numberPicker.invalidate()
    }
    override fun clickListener() {
        mDateTxt!!.setOnClickListener {
            DateUtil.showFromDateDialog(mContext, mSelectedDateValue!!, this, false)
        }
        mBackBtn!!.setOnClickListener {
            finish()

        }
        submit_save_text.setOnClickListener {

            submit()
        }

        mAddSchoolLay!!.setOnClickListener {
            val mSchool: ProfileSettingModel.SchoolInfo = ProfileSettingModel.SchoolInfo()
            mSchoolInfoList.add(mSchool)
            mSchoolAdapter!!.notifyDataSetChanged()
        }
        mAddCollegeLay!!.setOnClickListener {
            val mCoolege: ProfileSettingModel.CollageInfo = ProfileSettingModel.CollageInfo()
            mCollegeInfoList.add(mCoolege)
            mCollageAdapter!!.notifyDataSetChanged()
        }
        mAddworkPlaceLay!!.setOnClickListener {
            val mWorkPlace: ProfileSettingModel.WorkPlaceInfo = ProfileSettingModel.WorkPlaceInfo()
            mWorkPlaceInfoList.add(mWorkPlace)
            mWorkPlaceAdapter!!.notifyDataSetChanged()
        }
    }

    fun setSchoolAdapter() {
        val layoutManager = LinearLayoutManager(mContext)
        mSchoolRecycle.layoutManager = layoutManager
        mSchoolRecycle.setHasFixedSize(true)
        mSchoolAdapter = SchoolInfoListAdapter(mContext, mSchoolInfoList, this)
        mSchoolRecycle.adapter = mSchoolAdapter
    }

    fun setCollageAdapter() {
        val layoutManager = LinearLayoutManager(mContext)
        mCollageRecycle.layoutManager = layoutManager
        mCollageRecycle.setHasFixedSize(true)
        mCollageAdapter = CollageInfoListAdapter(mContext, mCollegeInfoList, this)
        mCollageRecycle.adapter = mCollageAdapter
    }

    fun setWorkPlaceAdapter() {
        val layoutManager = LinearLayoutManager(mContext)
        mEmployeeRecycle.layoutManager = layoutManager
        mEmployeeRecycle.setHasFixedSize(true)
        mWorkPlaceAdapter = WorkPlaceInfoListAdapter(mContext, mWorkPlaceInfoList, this)
        mEmployeeRecycle.adapter = mWorkPlaceAdapter
    }

    fun setAdapter() {
        setSchoolAdapter()
        setCollageAdapter()
        setWorkPlaceAdapter()
    }

    override fun init() {
        mContext = this
        user = LocalStorageSP.getLoginUser(mContext)
        mCollageRecycle.isNestedScrollingEnabled = false
        mSchoolRecycle.isNestedScrollingEnabled = false
        mEmployeeRecycle.isNestedScrollingEnabled = false
        mBackBtn = findViewById(R.id.common_back_arrow) as ImageView
        mToolbarTitle = findViewById(R.id.set_profile_header) as TextView
        mAddSchoolLay = findViewById(R.id.add_school_layout) as LinearLayout
        mAddCollegeLay = findViewById(R.id.add_college_lay) as LinearLayout
        mAddworkPlaceLay = findViewById(R.id.add_workplace_lay) as LinearLayout

        val aTitle =
            "<b><font color='#000000'>PROFILE</font></b>" + "<br>" + "<font color='#000000'>SETTINGS</font>"
        mToolbarTitle!!.setText(Html.fromHtml(aTitle))
        clickListener()
    }

    fun setValues(values: ProfileSettingModel) {
        mSchoolInfoList.clear()
        mCollegeInfoList.clear()
        mWorkPlaceInfoList.clear()

        mSchoolInfoList.addAll(values.schoolInfo!!)
        mCollegeInfoList.addAll(values.collageInfo!!)
        mWorkPlaceInfoList.addAll(values.workPlaceInfo!!)
        mSchoolAdapter!!.notifyDataSetChanged()
        mCollageAdapter!!.notifyDataSetChanged()
        mWorkPlaceAdapter!!.notifyDataSetChanged()
        mOccupationEdit.setText(values.celebrity)
        mDateTxt.setText(values.dateOfBirth)
        mSelectedDateValue =
            DateUtil.convertDateFormat(values.dateOfBirth!!, "yyyy-MM-dd", "dd-MM-yyyy")
        religionEdit.setText(values.religionBeliefs)
        nationalityEdit.setText(values.nationality)
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.profilesetting.hashCode()) {
                if (r.response!!.isSuccess) {
                    val aa = r as ProfileSettingModel
                    setValues(aa.setting!!)
                }
            } else if (r.requestType!! == AppServices.API.profilesettingsubmit.hashCode()) {
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
    }


    fun loadValues() {

        AppDialogs.showProgressDialog(mContext)
        val result = Helper.GenerateEncrptedUrl(
            BuildConfig.API_URL,
            getAccountSettingCaseString(user!!.mUserId!!)!!
        )
        AppServices.execute(
            mContext, this,
            result,
            Request.Method.POST,
            AppServices.API.profilesetting,
            ProfileSettingModel::class.java
        )
    }

    private fun getAccountSettingCaseString(
        aUserId: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", aUserId)
            val jsonParam = JSONObject()
            when ("ProfileSetting") {
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

    override fun onAdd(tt: Any, position: Int, type: String) {

        /*if (type.toLowerCase(Locale.ENGLISH).equals("school", true)) {
            val mSchool: ProfileSettingModel.SchoolInfo = ProfileSettingModel.SchoolInfo()
            mSchoolInfoList.add(mSchool)
            mSchoolAdapter!!.notifyDataSetChanged()
        } else if (type.toLowerCase(Locale.ENGLISH).equals("college", true)) {
            val mCoolege: ProfileSettingModel.CollageInfo = ProfileSettingModel.CollageInfo()
            mCollegeInfoList.add(mCoolege)
            mCollageAdapter!!.notifyDataSetChanged()
        } else if (type.toLowerCase(Locale.ENGLISH).equals("workplace", true)) {
            val mWorkPlace: ProfileSettingModel.WorkPlaceInfo = ProfileSettingModel.WorkPlaceInfo()
            mWorkPlaceInfoList.add(mWorkPlace)
            mWorkPlaceAdapter!!.notifyDataSetChanged()
        }*/
    }

    override fun onDelete(position: Int, type: String) {
        if (type.toLowerCase(Locale.ENGLISH).equals("school", true)) {
            mSchoolInfoList.removeAt(position)
            mSchoolAdapter!!.notifyDataSetChanged()
        } else if (type.toLowerCase(Locale.ENGLISH).equals("college", true)) {
            mCollegeInfoList.removeAt(position)
            mCollageAdapter!!.notifyDataSetChanged()
        } else if (type.toLowerCase(Locale.ENGLISH).equals("workplace", true)) {
            mWorkPlaceInfoList.removeAt(position)
            mWorkPlaceAdapter!!.notifyDataSetChanged()
        }
    }

    override fun fromDate(obj: Any, position: Int, type: String) {
        if (type.toLowerCase(Locale.ENGLISH).equals("school", true)) {
            obj as ProfileSettingModel.SchoolInfo

            if (obj.school_from != null) {
                mCurrentYear = obj.school_from!!.toInt()
            }
            showYearPicker("school", mCurrentYear!!, position, "from")
        } else if (type.toLowerCase(Locale.ENGLISH).equals("college", true)) {
            obj as ProfileSettingModel.CollageInfo
            if (obj.collegeFrom != null) {
                mCurrentYear = obj.collegeFrom!!.toInt()
            }
            showYearPicker("college", mCurrentYear!!, position, "from")
        } else if (type.toLowerCase(Locale.ENGLISH).equals("workplace", true)) {
            obj as ProfileSettingModel.WorkPlaceInfo

            if (obj.workplaceFrom != null) {
                mCurrentYear = obj.workplaceFrom!!.toInt()
            }
            showYearPicker("workplace", mCurrentYear!!, position, "from")
        }

    }

    override fun toDate(obj: Any, position: Int, type: String) {
        if (type.toLowerCase(Locale.ENGLISH).equals("school", true)) {
            obj as ProfileSettingModel.SchoolInfo

            if (obj.school_from != null) {
                mCurrentYear = obj.school_from!!.toInt()
            }
            showYearPicker("school", mCurrentYear!!, position, "to")
        } else if (type.toLowerCase(Locale.ENGLISH).equals("college", true)) {
            obj as ProfileSettingModel.CollageInfo
            if (obj.collegeTo != null) {
                mCurrentYear = obj.collegeTo!!.toInt()
            }
            showYearPicker("college", mCurrentYear!!, position, "to")
        } else if (type.toLowerCase(Locale.ENGLISH).equals("workplace", true)) {
            obj as ProfileSettingModel.WorkPlaceInfo
            if (obj.workplaceTo != null) {
                mCurrentYear = obj.workplaceTo!!.toInt()
            }
            showYearPicker("workplace", mCurrentYear!!, position, "to")
        }
    }

    override fun update(position: Int, text: String, type: String) {
        if (type.toLowerCase(Locale.ENGLISH).equals("school", true)) {
            mSchoolInfoList.get(position).school = text
        } else if (type.toLowerCase(Locale.ENGLISH).equals("college", true)) {
            mCollegeInfoList.get(position).college = text
        } else if (type.toLowerCase(Locale.ENGLISH).equals("workplace", true)) {
            mWorkPlaceInfoList.get(position).workPlace = text
        }
    }

    fun showYearPicker(type: String, selectedYear: Int, postion: Int, aFromOrTo: String) {
        val d = Dialog(mContext)
        d.setTitle("NumberPicker")
        d.setContentView(R.layout.layout_dialog)
        val ok: Button = d.findViewById(R.id.ok) as Button
        val cancel: Button = d.findViewById(R.id.cancel) as Button
        val np = d.findViewById(R.id.numberPicker1) as NumberPicker

        setNumberPickerTextColor(np,R.color.green)
        np.maxValue = 3000
        np.minValue = 1900
        np.wrapSelectorWheel = false
        np.value = selectedYear
        np.setOnValueChangedListener(this)
        ok.setOnClickListener {
            if (type.toLowerCase(Locale.ENGLISH).equals("school", true)) {
                if (aFromOrTo.equals("from", true)) {
                    mSchoolInfoList.get(postion).school_from = np.value.toString()
                } else {
                    mSchoolInfoList.get(postion).school_to = np.value.toString()
                }
                mSchoolAdapter!!.notifyItemChanged(postion)
            } else if (type.toLowerCase(Locale.ENGLISH).equals("college", true)) {
                if (aFromOrTo.equals("from", true)) {
                    mCollegeInfoList.get(postion).collegeFrom = np.value.toString()
                } else {
                    mCollegeInfoList.get(postion).collegeTo = np.value.toString()
                }
                mCollageAdapter!!.notifyItemChanged(postion)
            } else if (type.toLowerCase(Locale.ENGLISH).equals("workplace", true)) {
                if (aFromOrTo.equals("from", true)) {
                    mWorkPlaceInfoList.get(postion).workplaceFrom = np.value.toString()
                } else {
                    mWorkPlaceInfoList.get(postion).workplaceTo = np.value.toString()
                }
                mWorkPlaceAdapter!!.notifyItemChanged(postion)
            }

            // Log.e("Picked Year", np.value.toString())
            d.dismiss()
        }
        cancel.setOnClickListener {

            d.dismiss()
        }
        d.show()
    }

    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {

    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        mSelectedDateValue =
            (dayOfMonth).toString() + "-" + (month + 1).toString() + "-" + year.toString()

        mDateTxt.setText(
            (DateUtil.convertDateFormat(
                year.toString() + "-" + (month + 1).toString() + "-" + (dayOfMonth).toString(),
                "yyyy-MM-dd",
                "yyyy-MM-dd"
            )
                    )
        )


    }

    fun submit() {
        if (checkInternet()) {
            AppDialogs.showProgressDialog(mContext)
            val result = Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                PostProfileSettingCaseString(
                    getETValue(mOccupationEdit),
                    getTXTValue(mDateTxt),
                    getETValue(religionEdit),
                    getETValue(nationalityEdit),
                    mSchoolInfoList,
                    mCollegeInfoList,
                    mWorkPlaceInfoList
                )!!
            )
            AppServices.execute(
                mContext, this,
                result,
                Request.Method.POST,
                AppServices.API.profilesettingsubmit,
                Response::class.java
            )
        }
    }

    private fun PostProfileSettingCaseString(
        aOccupation: String,
        aDate: String,
        aReligious: String,
        Nationality: String,
        aSchoolsDetailsList: ArrayList<ProfileSettingModel.SchoolInfo>,
        aCollegeDetailsList: ArrayList<ProfileSettingModel.CollageInfo>,
        aWorkPlaceDetailsList: ArrayList<ProfileSettingModel.WorkPlaceInfo>
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", user!!.mUserId!!)
            jsonParam1.put("date_of_birth", aDate)
            jsonParam1.put("category", "General")
            jsonParam1.put("celebrity", aOccupation)
            jsonParam1.put("nationality", Nationality)
            jsonParam1.put("religion_beliefs", aReligious)
            val jsonSchoolInfoArr = JSONArray()
            for (i in aSchoolsDetailsList.indices) {
                val pnObj = JSONObject()
                pnObj.put("id", aSchoolsDetailsList[i].id)
                pnObj.put("school", aSchoolsDetailsList[i].school)
                pnObj.put("school_from", aSchoolsDetailsList[i].school_from)
                pnObj.put("school_to", aSchoolsDetailsList[i].school_to)
                jsonSchoolInfoArr.put(pnObj)
            }
            jsonParam1.put("school_info", jsonSchoolInfoArr)
            val jsonCollegeInfoArr = JSONArray()
            if (aCollegeDetailsList.size > 0) {
                for (i in aCollegeDetailsList.indices) {
                    val pnObj = JSONObject()
                    pnObj.put("id", aCollegeDetailsList[i].id)
                    pnObj.put("college", aCollegeDetailsList[i].college)
                    pnObj.put("college_from", aCollegeDetailsList[i].collegeFrom)
                    pnObj.put("college_to", aCollegeDetailsList[i].collegeTo)
                    jsonCollegeInfoArr.put(pnObj)
                }
            }
            jsonParam1.put("college_info", jsonCollegeInfoArr)
            val jsonWorkPlaceArr = JSONArray()
            if (aWorkPlaceDetailsList.size > 0) {
                for (i in aWorkPlaceDetailsList.indices) {
                    val pnObj = JSONObject()
                    pnObj.put("id", aWorkPlaceDetailsList[i].id)
                    pnObj.put("workplace", aWorkPlaceDetailsList[i].workPlace)
                    pnObj.put("workplace_from", aWorkPlaceDetailsList[i].workplaceFrom)
                    pnObj.put("workplace_to", aWorkPlaceDetailsList[i].workplaceTo)
                    jsonWorkPlaceArr.put(pnObj)
                }
            }
            jsonParam1.put("workplace_info", jsonWorkPlaceArr)
            val jsonParam = JSONObject()
            jsonParam.put("ProfileSettings", jsonParam1)
            Log.e("ProfileSettings", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }
}
