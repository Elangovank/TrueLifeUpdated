package com.truelife.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.truelife.R
import com.truelife.app.model.ClubListShareModel
import org.apache.commons.lang3.StringEscapeUtils

class ClubShareMultiSelectionAdapter(val mModel: ClubListShareModel) :
    RecyclerView.Adapter<ClubShareMultiSelectionAdapter.ViewHolder>() {

    var mSelectedClubs: ArrayList<String> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_club_share_multi_selection_adapter, parent, false)
        return ViewHolder(v);
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var isSelected: Boolean = false
        val mImage: ImageView = itemView.findViewById(R.id.porofile_image)
        val mName: TextView = itemView.findViewById(R.id.club_name)
        val mMemberCount: TextView = itemView.findViewById(R.id.club_member_count)
        val mIndicatore: ImageView = itemView.findViewById(R.id.selection_indiactor)
        val mRoot: LinearLayout = itemView.findViewById(R.id.root_layout)

    }

    override fun getItemCount(): Int {
        return mModel.clubList!!.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mValue = mModel.clubList!![position]

        if (holder.isSelected)
            holder.mIndicatore.visibility = View.VISIBLE
        else
            holder.mIndicatore.visibility = View.GONE

        val aTitle: String = StringEscapeUtils.unescapeJava(mValue.clubName!!)
        holder.mName.text = aTitle

        holder.mMemberCount.text = String.format("%s %s", mValue.totalMembers, "members")

        if (!mValue.clubImage.isNullOrEmpty())
            Picasso.get().load(mValue.clubImage).into(holder.mImage)


        holder.mRoot.setOnClickListener {
            holder.isSelected = !holder.isSelected
            if (holder.isSelected)
                mSelectedClubs.add(mModel.clubList!![holder.adapterPosition].id!!)
            else {
                mSelectedClubs.remove(mModel.clubList!![holder.adapterPosition].id!!)
            }
            notifyDataSetChanged()
        }

    }

    fun getSelectedClubs(): ArrayList<String> {
        return mSelectedClubs
    }
}