package com.truelife.app.fragment.setting.adapter

import android.content.Context
import android.content.res.TypedArray
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.truelife.ClickListener
import com.truelife.R

/**
 * Created by Elango - 23-12-2019.
 */
class TLSettingsAdapter(
    aContext: Context, var clickListener: ClickListener
) : RecyclerView.Adapter<TLSettingsAdapter.ViewHolder>() {
    private val mySettingsTitleArray: Array<String>
    private val mySettingsTitleDescription: Array<String>
    private val myContext: Context
    private val mySettingIconArray: TypedArray
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_settings_adapter, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        try {
            if(position==0){
             holder.mLine.visibility= View.GONE
            }else
                holder.mLine.visibility= View.VISIBLE
            holder.mySettingsTitle.text = mySettingsTitleDescription[position]
            holder.mySettingsImage.setImageResource(mySettingIconArray.getResourceId(position, -1))
            holder.mySettingsName.text = mySettingsTitleArray[position]
            holder.mRootLayout.setOnClickListener() {
                clickListener.click(position)
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


    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        // Log.e("View", String.valueOf(holder.mySettingsImage.getVisibility()));
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        Log.e("View", holder.mySettingsImage.visibility.toString())
    }

    inner class ViewHolder(aView: View) : RecyclerView.ViewHolder(aView) {
        var mySettingsCard: CardView
        var mySettingsImage: ImageView
        var mySettingsTitle: TextView
        var mySettingsName: TextView
        var mRootLayout: LinearLayout
val  mLine:View
        init {
            mySettingsCard =
                aView.findViewById<View>(R.id.inflate_settings_card) as CardView
            mySettingsImage =
                aView.findViewById<View>(R.id.settings_icon) as ImageView
            mySettingsTitle =
                aView.findViewById<View>(R.id.setting_description1) as TextView
            mySettingsName = aView.findViewById<View>(R.id.setting_name) as TextView
            mRootLayout = aView.findViewById<View>(R.id.root_layout) as LinearLayout
            mLine = aView.findViewById<View>(R.id.LAY_view) as View
        }
    }

    init {
        myContext = aContext
        mySettingsTitleArray = myContext.resources.getStringArray(R.array.settings_title)
        mySettingsTitleDescription =
            myContext.resources.getStringArray(R.array.settings_title_description)
        mySettingIconArray = myContext.resources.obtainTypedArray(R.array.settings_icon)
    }
}