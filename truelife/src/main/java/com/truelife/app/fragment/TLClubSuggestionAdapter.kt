package com.truelife.app.fragment

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.fragment.club.TLClubDetails
import com.truelife.app.model.ClubSuggestionModel
import com.truelife.base.TLFragmentManager
import com.truelife.util.Utility

/**
 * Created by mahalingam on 01-09-2017.
 * TLClubByAdapter
 */
class TLClubSuggestionAdapter(
    context: FragmentActivity?,
    aclubInfoList: ClubSuggestionModel?
) : RecyclerView.Adapter<TLClubSuggestionAdapter.ItemViewHolder>() {
    private var myContext: FragmentActivity? = null
    private var myClubInfoList: ClubSuggestionModel? = null
    private var myFragmentManager: TLFragmentManager? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder { // Context context = holder.itemView.getContext();
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_inflate_club_suggestion_member_item, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int
    ) {
        try {

            val aClubObject =
                myClubInfoList!!.clubList!![position]
            holder.myclubName.text = aClubObject.clubName
            if (aClubObject.totalMembers == "0" || aClubObject.totalMembers == "1") {
                holder.myClubMembers.text = aClubObject.totalMembers + " Member"
            } else {
                holder.myClubMembers.text = aClubObject.totalMembers + " Members"
            }
            when (aClubObject.clubFor) {
                "0" -> holder.myClubType.text =
                    myContext!!.resources.getString(R.string.label_public_club)
                "1" -> holder.myClubType.text =
                    myContext!!.resources.getString(R.string.label_public_club)
                "2" -> holder.myClubType.text =
                    myContext!!.resources.getString(R.string.label_friends_club)
            }

            if (!aClubObject.clubImage.isNullOrEmpty())
                Utility.loadImage(
                    aClubObject.clubImage!!,
                    holder.myCropImage,
                    R.drawable.club_placeholder
                )
            else
                holder.myCropImage.setImageResource(R.drawable.club_placeholder)

            holder.myClubLayout.setOnClickListener {
                myContext!!.startActivity(
                    Intent(myContext, TLClubDetails::class.java)
                        .putExtra("Club_id", aClubObject.id)
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return myClubInfoList!!.clubList!!.size
    }

    /*
     * ItemViewHolder
     * */
    inner class ItemViewHolder internal constructor(view: View) :
        RecyclerView.ViewHolder(view) {
        var myclubName: TextView
        var myClubType: TextView
        var myCropImage: ImageView
        var myClubLayout: CardView
        var myClubMembers: TextView

        init {
            myclubName = view.findViewById<View>(R.id.layout_club_name) as TextView
            myClubType = view.findViewById<View>(R.id.layout_club_type) as TextView
            myClubMembers =
                view.findViewById<View>(R.id.layout_club_members) as TextView
            myCropImage =
                view.findViewById<View>(R.id.layout_club_image) as ImageView
            myClubLayout =
                view.findViewById<View>(R.id.layout_club_linear_layout) as CardView
        }
    }

    /**
     * interface onclick
     */
    interface ClickListener {
        fun onClick(view: View?, position: Int)
        fun onLongClick(view: View?, position: Int)
    }

    companion object {
        var TAG = TLClubSuggestionAdapter::class.java.simpleName
    }

    init {
        try {
            myContext = context
            myClubInfoList = aclubInfoList
            myFragmentManager = TLFragmentManager(myContext!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}