package com.truelife.app.adapter

import android.text.Editable
import android.text.TextWatcher
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
class CollageInfoListAdapter(
    private val mContext: FragmentActivity,
    var mUList: List<ProfileSettingModel.CollageInfo>, val listener: ProfileSettingListener
) : RecyclerView.Adapter<CollageInfoListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(mContext)
            .inflate(R.layout.inflate_add_college, parent, false)
        return ViewHolder(v)
    }


    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {

        var data = getInfo(position)
        aHolder.mCollageNameEdit.setText(data.college)
        aHolder.fromDateTxt.setText(data.collegeFrom)
        aHolder.toDateTxt.setText(data.collegeTo)

        aHolder.mRemoveBtn.setOnClickListener {
            listener.onDelete(position, "college")
        }

        aHolder.fromDateLay.setOnClickListener {

            listener.fromDate(data, position, "college")
        }

        aHolder.toDateLay.setOnClickListener {

            listener.toDate(data, position, "college")
        }

        aHolder.mCollageNameEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                listener.update(position, s.toString(), "college")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

    }

    fun getInfo(position: Int): ProfileSettingModel.CollageInfo {
        return mUList[position]
    }

    override fun getItemCount(): Int {
        return mUList.size
    }

    inner class ViewHolder(aView: View) :
        RecyclerView.ViewHolder(aView) {
        val mCollageNameEdit: EditText

        val fromDateLay: LinearLayout
        val toDateLay: LinearLayout

        val fromDateTxt: TextView
        val toDateTxt: TextView
        val mAddLayout: LinearLayout
        val mRemoveBtn: ImageButton

        init {
            mCollageNameEdit = aView.findViewById(R.id.mSchoolNameEdit)
            fromDateLay = aView.findViewById(R.id.fromDateLay)
            toDateLay = aView.findViewById(R.id.toDateLay)
            mAddLayout = aView.findViewById(R.id.mAddSchoolLay)
            fromDateTxt = aView.findViewById(R.id.mSchoolFromDateTxt)
            toDateTxt = aView.findViewById(R.id.mSchoolToDateTxt)
            mRemoveBtn = aView.findViewById(R.id.remove_img)

        }
    }

    fun updateAdapter(
        aUList: List<ProfileSettingModel.CollageInfo>
    ) {
        mUList = aUList
    }

}