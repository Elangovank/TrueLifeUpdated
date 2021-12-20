package com.truelife.app.fragment.club.more.invite

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.model.Club.PublicCategory

/**
 * Created by Elango on 19-02-2020.
 */

class TLClubByYouGeneralPublicAdapter(
    val myContext: Context,
    val myCategortMembers: PublicCategory,
    val mCallback: ClickListener
) : RecyclerView.Adapter<TLClubByYouGeneralPublicAdapter.ViewHolder>() {


    private val mySettingsTitleArray =
        myContext.resources.getStringArray(R.array.club_by_you_general_public)

    // Keep all Images in array
    var mThumbIds = arrayOf(
        R.drawable.area_icon, R.drawable.icon_city,
        R.drawable.icon_state, R.drawable.india_icon
    )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_general_public_adapter, parent, false)
        return ViewHolder(
            itemView
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        try {
            holder.myTitle.text = mySettingsTitleArray[position]
            holder.mySettingsImage.setImageResource(mThumbIds[position])
            when (position) {
                0 -> holder.myMembersCount.text =
                    String.format("%s Members", myCategortMembers.mAreaCount)
                1 -> holder.myMembersCount.text =
                    String.format("%s Members", myCategortMembers.mCityCount)
                2 -> holder.myMembersCount.text =
                    String.format("%s Members", myCategortMembers.mStateCount)
                3 -> holder.myMembersCount.text =
                    String.format("%s Members", myCategortMembers.mCountryCount)
            }

            holder.mySettingsCard.setOnClickListener {
                mCallback.onClick(position)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return mySettingsTitleArray.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    /**
     * interface onclick
     */
    interface ClickListener {
        fun onClick(position: Int)
    }

    inner class ViewHolder(aView: View) : RecyclerView.ViewHolder(aView) {
        var mySettingsCard: CardView
        var mySettingsImage: ImageView
        var myTitle: TextView
        var myMembersCount: TextView

        init {
            mySettingsCard = aView.findViewById(R.id.inflate_settings_card)
            mySettingsImage = aView.findViewById(R.id.settings_icon)
            myTitle = aView.findViewById(R.id.general_public_name)
            myMembersCount = aView.findViewById(R.id.general_public_member_name)
        }
    }
}