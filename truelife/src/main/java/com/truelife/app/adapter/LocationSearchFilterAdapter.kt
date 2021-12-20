package com.truelife.app.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.truelife.R
import com.truelife.app.listeners.SelectedListener
import com.truelife.app.model.LocationModel
import java.util.*

/**
 * Created by elangovan.
 */
class LocationSearchFilterAdapter(
    val mContext: FragmentActivity,
    var myRawJobList: ArrayList<LocationModel>,
    val mySelectedListener: SelectedListener?
) : BaseAdapter() {

    private var mySearchArrayList: ArrayList<LocationModel>? = myRawJobList
    private var myFilter: ItemFilter? = null
    private var myInflater: LayoutInflater? = null
    var myScreenName: String = "SignUp"


    override fun getCount(): Int {
        return if (mySearchArrayList == null) 0 else mySearchArrayList!!.size
    }

    override fun getItem(i: Int): LocationModel {
        return mySearchArrayList!![i]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /*
        override fun getView(position: Int, aConvertView: View, arg2: ViewGroup): View {
           var aConvertView = aConvertView
            var aHolder: ViewHolder? = null
            if (aConvertView == null) {
                aHolder = ViewHolder()
                myInflater = mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                aConvertView = myInflater!!.inflate(R.layout.search_alert_single_select, null)
                aHolder.aItemTXT = aConvertView.findViewById<View>(R.id.custom_alert_item_text) as TextView
                aHolder.aDoneIMG = aConvertView.findViewById<View>(R.id.custom_alert_item_IMG) as ImageView
                aConvertView.tag = aHolder
            } else {
                aHolder =
                    aConvertView.tag as ViewHolder
            }
            val aSearchListItem: LocationModel = mySearchArrayList!![position]
            aHolder.aItemTXT!!.setText(aSearchListItem.name)
            aHolder!!.aItemTXT!!.setOnClickListener {
                if (myScreenName == "SingUp") {
                    mySelectedListener!!.onClick(
                        aSearchListItem.name,
                        aSearchListItem.id,
                        aSearchListItem.code.toString() + "",
                        aSearchListItem
                    )
                }
            }
            if (aSearchListItem.name.equals(mySeletedSTR)) {
                aHolder.aDoneIMG!!.visibility = View.VISIBLE
            } else {
                aHolder.aDoneIMG!!.visibility = View.GONE
            }
            return aConvertView
        }*/
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        var aConvertView = convertView
        var aHolder: ViewHolder? = null
        if (aConvertView == null) {
            aHolder = ViewHolder()
            myInflater =
                mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            aConvertView = myInflater!!.inflate(R.layout.search_alert_single_select, null)
            aHolder.aItemTXT =
                aConvertView.findViewById<View>(R.id.custom_alert_item_text) as TextView
            aHolder.aDoneIMG =
                aConvertView.findViewById<View>(R.id.custom_alert_item_IMG) as ImageView
            aConvertView.tag = aHolder
        } else {
            aHolder =
                aConvertView.tag as ViewHolder
        }
        val aSearchListItem: LocationModel = mySearchArrayList!![position]
        aHolder.aItemTXT!!.setText(aSearchListItem.name)
        aHolder!!.aItemTXT!!.setOnClickListener {
            if (myScreenName == "SignUp") {
                mySelectedListener!!.onClick(
                    aSearchListItem.name,
                    aSearchListItem.id,
                    aSearchListItem.code.toString() + "",
                    aSearchListItem
                )
            }
        }
        return aConvertView
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun getFilter(): Filter? {
        if (myFilter == null) myFilter = ItemFilter()
        return myFilter
    }

    private fun updateDataInfo(aSearchList: ArrayList<LocationModel>) {
        mySearchArrayList = aSearchList
        notifyDataSetChanged()
    }

    /* fun getHeaderView(position: Int, aConvertView: View?, parent: ViewGroup?): View? {
         var aConvertView = aConvertView
         var aHolder: HeaderViewHolder? = null
         if (aConvertView == null) {
             aHolder = HeaderViewHolder()
             myInflater = mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
             aConvertView = myInflater!!.inflate(R.layout.search_alert_header_layout, null)
             aHolder.aHeaderTXT = aConvertView!!.findViewById<View>(R.id.custom_alert_item_text) as TextView
             aConvertView.tag = aHolder
         } else {
             aHolder = aConvertView.tag as HeaderViewHolder
         }
         val aSearchListItem: LocationModel = mySearchArrayList!![position]
         aHolder!!.aHeaderTXT!!.setText(aSearchListItem.name)
         return aConvertView
     }*/


    /*  internal inner class HeaderViewHolder {
          var aHeaderTXT: TextView? = null
      }*/

    internal inner class ViewHolder {
        var aItemTXT: TextView? = null
        var aDoneIMG: ImageView? = null
    }

    private inner class ItemFilter : Filter() {
        override fun performFiltering(aConstraint: CharSequence): FilterResults {
            var aConstraint: CharSequence? = aConstraint
            aConstraint = aConstraint.toString().toLowerCase()
            // Initialize filter results object
            val result = FilterResults()
            val aFilteredJobList: ArrayList<LocationModel> =
                ArrayList<LocationModel>()
            if (aConstraint != null && aConstraint.toString().length > 0) {
                for (aJobList in myRawJobList) {
                    if (aJobList.name!!.toLowerCase().contains(aConstraint.toString()) || aJobList.name!!.toLowerCase().startsWith(
                            aConstraint.toString()
                        )
                    ) {
                        aFilteredJobList.add(aJobList)
                    }
                    // setting the values as filtered map list
                    result.values = aFilteredJobList
                    // Setting the size of filtered maps list
                    result.count = aFilteredJobList.size
                }
            } else { // setting the values as filtered map list
                result.values = myRawJobList
                // Setting the size of filtered maps list
                result.count = myRawJobList.size
            }
            return result
        }

        override fun publishResults(
            constraint: CharSequence,
            results: FilterResults
        ) { // copy the results to your original names and refresh list
            if (results.values != null)
                updateDataInfo(results.values!! as ArrayList<LocationModel>)
        }
    }


}