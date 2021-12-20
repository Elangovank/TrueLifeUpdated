package com.truelife.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.fragment.club.TLClubDetails
import com.truelife.app.model.SearchClubListModel
import com.truelife.util.AppDialogs
import com.truelife.util.Utility
import org.apache.commons.lang3.StringEscapeUtils


class SearchClubListAdapter(val mContext: Context, val mValues: SearchClubListModel) :
    RecyclerView.Adapter<SearchClubListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_search_friends_list_adapter, parent, false)
        return ViewHolder(v)
    }


    @SuppressLint("NewApi")
    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {
        if (!mValues.clubList!![position].clubImage.isNullOrEmpty())
            Utility.loadImage(mValues.clubList!![position].clubImage!!, aHolder.mImage)
        else aHolder.mImage.setImageDrawable(mContext.getDrawable(R.drawable.club_placeholder))

        val aTitle: String =
            StringEscapeUtils.unescapeJava(mValues.clubList!![position].clubName)
        aHolder.mText.text = aTitle

        aHolder.mLayout.setOnClickListener {


                if (Utility.isInternetAvailable(mContext)) {
                    mContext.startActivity(
                        Intent(mContext, TLClubDetails::class.java)
                            .putExtra("Club_id", mValues.clubList!![position].id)
                    )
                }

        }
        if (mValues.clubList!!.size < 1) {
            aHolder.mView.visibility = View.GONE
        } else {
            aHolder.mView.visibility = View.VISIBLE
        }


    }


    override fun getItemCount(): Int {
        return mValues.clubList!!.size
    }


    inner class ViewHolder(aView: View) :
        RecyclerView.ViewHolder(aView) {

        var mImage: ImageView = aView.findViewById(R.id.inflate_menu_list_icon)
        var mText: TextView = aView.findViewById(R.id.inflate_menu_list_name)
        var mLayout: LinearLayout = aView.findViewById(R.id.inflate_menu_container_LAY)
        var mView: View = aView.findViewById(R.id.clubView)


    }

}


