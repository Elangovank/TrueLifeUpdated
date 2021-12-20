package com.truelife.app.fragment.more.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.model.User
import com.truelife.storage.LocalStorageSP.getLoginUser

/**
 * Created by Elango on 19-12-2019.
 */
class TLMoreScreenRecyclerAdapter(
    private val myContext: Context,
    private var mClickList: ClickListener
) : RecyclerView.Adapter<TLMoreScreenRecyclerAdapter.ViewHolder>() {

    private val myMenuTitleArray = myContext.resources.getStringArray(R.array.menu_title)
    private val myMenuImageArray = myContext.resources.obtainTypedArray(R.array.menu_image)

    var user: User? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v = LayoutInflater.from(myContext)
            .inflate(R.layout.inflate_more_screen_adapter_item, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("NewApi")
    override fun onBindViewHolder(
        aHolder: ViewHolder,
        position: Int
    ) {
        aHolder.myMenuTitle.text = myMenuTitleArray[position]

        if (position == 4) {
            val user = getLoginUser(myContext)
            if (user.mGender.equals("1"))
                aHolder.myMenuImageIcon.setImageDrawable(myContext.getDrawable(R.drawable.ic_male_profile))
            else aHolder.myMenuImageIcon.setImageDrawable(myContext.getDrawable(R.drawable.ic_female_profile))
        } else aHolder.myMenuImageIcon.setImageResource(myMenuImageArray.getResourceId(position, 0))
        aHolder.mRootlayout!!.setOnClickListener() {
            mClickList.onClick(aHolder.myMenuTitle, position)
        }

    }

    override fun getItemCount(): Int {
        return myMenuTitleArray.size
    }

    /**
     * interface onclick
     */
    interface ClickListener {
        fun onClick(view: View?, position: Int)
        fun onLongClick(view: View?, position: Int)
    }

    inner class ViewHolder constructor(aView: View) :
        RecyclerView.ViewHolder(aView) {
        val myMenuImageIcon: ImageView
        val myMenuTitle: TextView
        private val myMenuRightIcon: ImageView

        var mRootlayout: LinearLayout? = null

        init {
            myMenuImageIcon =
                aView.findViewById<View>(R.id.inflate_menu_list_icon) as ImageView
            mRootlayout = aView.findViewById<View>(R.id.inflate_menu_card_view) as LinearLayout
            myMenuTitle =
                aView.findViewById<View>(R.id.inflate_menu_list_name) as TextView
            myMenuRightIcon =
                aView.findViewById<View>(R.id.inflate_menu_right_arrow) as ImageView
        }
    }
}