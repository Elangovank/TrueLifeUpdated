package com.truelife.app.adapter

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
import com.truelife.app.activity.ProfileActivity
import com.truelife.app.model.PeopleListModel
import com.truelife.util.Utility
import org.apache.commons.lang3.StringEscapeUtils


class SearchPeopleListAdapter(val mContext: Context, val mValues: PeopleListModel) :
    RecyclerView.Adapter<SearchPeopleListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_search_friends_list_adapter, parent, false)
        return ViewHolder(v)
    }


    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {
        if (!mValues.peoplesList!![position].profileImage.isNullOrEmpty())
            Utility.loadImageWithGender(mValues.peoplesList!![position].profileImage!!, aHolder.mImage,mValues.peoplesList!![position].gender!!)
           // Utility.loadImage(mValues.peoplesList!![position].profileImage!!, aHolder.mImage)
        else Utility.loadPlaceHolder(
            mContext,
            mValues.peoplesList!![position].gender!!,
            aHolder.mImage
        )

        aHolder.mRoot.setOnClickListener {

            val aIntent = Intent(mContext, ProfileActivity::class.java).putExtra(
                "userid",
                mValues.peoplesList!![position].id
            )
            mContext.startActivity(aIntent)
        }

        val aTitle: String = StringEscapeUtils.unescapeJava(mValues.peoplesList!![position].fullname)
        aHolder.mText.text = aTitle

    }


    override fun getItemCount(): Int {
        return mValues.peoplesList!!.size
    }


    inner class ViewHolder(aView: View) :
        RecyclerView.ViewHolder(aView) {

        var mRoot: LinearLayout = aView.findViewById(R.id.inflate_menu_container_LAY)
        var mImage: ImageView = aView.findViewById(R.id.inflate_menu_list_icon)
        var mText: TextView = aView.findViewById(R.id.inflate_menu_list_name)

    }

    fun clearAll(clear: Boolean) {
        if (clear)
            mValues.peoplesList!!.clear()
        notifyDataSetChanged()
    }

}


