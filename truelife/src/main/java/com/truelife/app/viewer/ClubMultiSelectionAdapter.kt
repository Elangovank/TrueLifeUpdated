package com.truelife.app.viewer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.activity.DashBoardSearchDetailActivity
import com.truelife.app.model.ClubListShareModel
import org.apache.commons.lang3.StringEscapeUtils

class ClubMultiSelectionAdapter(
    val mModel: ArrayList<ClubListShareModel.ClubList>,
    val mCallback: Callback
) :
    RecyclerView.Adapter<ClubMultiSelectionAdapter.ViewHolder>() {

    var mSelectedClubs: ArrayList<String> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.inflate_club_list, parent, false)
        return ViewHolder(v);
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mName: AppCompatCheckBox = itemView.findViewById(R.id.search_filter_area_chk)
    }

    override fun getItemCount(): Int {
        return mModel.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mClub = mModel[position]
        holder.mName.isChecked = mClub.isChecked
        val aTitle: String = StringEscapeUtils.unescapeJava(mClub.clubName!!)
        holder.mName.text = aTitle

        holder.mName.setOnClickListener {
            mClub.isChecked = !mClub.isChecked
            mCallback.isCheck()
        }

    }

    fun getSelectedClubs(): ArrayList<String> {
        mSelectedClubs.clear()
        for (i in mModel.indices) {
            if (mModel[i].isChecked)
                mSelectedClubs.add(mModel[i].id!!)
        }

        return mSelectedClubs
    }

    interface Callback {
        fun isCheck()
    }
}