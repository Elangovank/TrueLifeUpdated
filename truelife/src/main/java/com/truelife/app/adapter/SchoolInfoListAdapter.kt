package com.truelife.app.adapter

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.listeners.ProfileSettingListener
import com.truelife.app.model.ProfileSettingModel

/**
 * Created by Elango on 18-12-2019.
 */
class SchoolInfoListAdapter(
    private val mContext: FragmentActivity,
    var mUList: List<ProfileSettingModel.SchoolInfo>, val listener: ProfileSettingListener
) : RecyclerView.Adapter<SchoolInfoListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(mContext)
            .inflate(R.layout.inflate_add_school, parent, false)
        return ViewHolder(v)
    }


    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {
        var data = getInfo(position)
        aHolder.mSchoolNameEdit.setText(data.school)
        aHolder.fromDateTxt.setText(data.school_from)
        aHolder.toDateTxt.setText(data.school_to)



        aHolder.mSchoolNameEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Log.e("Text Change", s.toString())
                listener.update(position, s.toString(), "school")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        aHolder.mRemoveBtn.setOnClickListener {

            listener.onDelete(position, "school")
        }

        aHolder.fromDateLay.setOnClickListener {

            listener.fromDate(data, position, "school")
        }

        aHolder.toDateLay.setOnClickListener {

            listener.toDate(data, position, "school")
        }


    }

    fun getInfo(position: Int): ProfileSettingModel.SchoolInfo {
        return mUList[position]
    }

    override fun getItemCount(): Int {
        return mUList.size
    }


    inner class ViewHolder(aView: View) :
        RecyclerView.ViewHolder(aView) {
        val mSchoolNameEdit: EditText

        val fromDateLay: LinearLayout
        val toDateLay: LinearLayout

        val fromDateTxt: TextView
        val toDateTxt: TextView
        val mAddLayout: LinearLayout
        val mRemoveBtn: ImageButton

        init {
            mSchoolNameEdit = aView.findViewById(R.id.mSchoolNameEdit)
            fromDateLay = aView.findViewById(R.id.fromDateLay)
            toDateLay = aView.findViewById(R.id.toDateLay)
            mAddLayout = aView.findViewById(R.id.mAddSchoolLay)
            fromDateTxt = aView.findViewById(R.id.mSchoolFromDateTxt)
            toDateTxt = aView.findViewById(R.id.mSchoolToDateTxt)
            mRemoveBtn = aView.findViewById(R.id.remove_img)

        }
    }

    fun updateAdapter(
        aUList: List<ProfileSettingModel.SchoolInfo>
    ) {
        mUList = aUList
    }

}