package com.truelife.chat.activities.main.contacts

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import  com.truelife.R
import com.truelife.chat.activities.main.MainViewModel
import com.truelife.chat.adapters.CallsAdapter
import com.truelife.chat.fragments.BaseFragment
import com.truelife.chat.interfaces.FragmentCallback
import com.truelife.chat.model.realms.FireCall
import com.truelife.chat.utils.PerformCall
import com.truelife.chat.utils.RealmHelper
import com.truelife.chat.utils.network.FireManager
import com.devlomi.hidely.hidelyviews.HidelyImageView
import com.google.android.gms.ads.AdView
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_calls.*
import java.util.*

class ContactFragment : BaseFragment() {

    override var adView: AdView? = null

    var actionMode: ActionMode? = null
   // val fireManager = FireManager()

    val viewModel: MainViewModel by activityViewModels()
    override fun showAds(): Boolean {
        return resources.getBoolean(R.bool.is_calls_ad_enabled)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onPause() {
        super.onPause()
        actionMode?.finish()
    }

    fun exitActionMode() {
        actionMode?.finish()
    }


}