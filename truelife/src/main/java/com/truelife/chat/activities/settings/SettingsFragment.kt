package com.truelife.chat.activities.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import  com.truelife.R
import com.truelife.chat.activities.AgreePrivacyPolicyActivity
import com.truelife.chat.utils.SharedPreferencesManager
import com.truelife.chat.utils.network.FireManager
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : Fragment(), View.OnClickListener {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_profile.setOnClickListener(this)
        tv_notifications.setOnClickListener(this)
        tv_security.setOnClickListener(this)
        tv_chat.setOnClickListener(this)
        tv_privacy_policy.setOnClickListener(this)
        tv_about.setOnClickListener(this)
        tv_delete.setOnClickListener(this)
    }

    override fun onClick(view: View) {

        when (view.id) {

            R.id.tv_profile -> Navigation.findNavController(view)
                .navigate(R.id.action_settingsFragment_to_profilePreferenceFragment)
            R.id.tv_notifications -> Navigation.findNavController(view)
                .navigate(R.id.action_settingsFragment_to_notificationPreferenceFragment)
            R.id.tv_security -> Navigation.findNavController(view)
                .navigate(R.id.action_settingsFragment_to_securityPreferencesFragment)
            R.id.tv_chat -> Navigation.findNavController(view)
                .navigate(R.id.action_settingsFragment_to_chatSettingsPreferenceFragment2)
            R.id.tv_privacy_policy -> Navigation.findNavController(view)
                .navigate(R.id.action_settingsFragment_to_privacyPolicyFragment)
            R.id.tv_about -> Navigation.findNavController(view)
                .navigate(R.id.action_settingsFragment_to_aboutFragment2)
            R.id.tv_delete -> showDeleteAccountDialog()

        }
    }

    private fun showDeleteAccountDialog() {
        val dialog = AlertDialog.Builder(requireActivity())
        dialog.setTitle("")
        dialog.setCancelable(false)

        val view = LayoutInflater.from(requireActivity())
            .inflate(R.layout.delete_account_dialog, null, false)
        dialog.setView(view)
        val tv = view.findViewById<TextView>(R.id.tv_privacy_policy_dialog)
        tv.setText(getString(R.string.label_are_sure_delete_account))

        dialog.setPositiveButton("Yes") { dialogq, which ->
            SharedPreferencesManager.deleteUser()
            dialogq.dismiss()
            FireManager.signout()
            requireActivity().startActivity(
                Intent(
                    requireActivity(),
                    AgreePrivacyPolicyActivity::class.java
                )
            )
            requireActivity().finish()
            /*if (!FireManager.isLoggedIn()) {

            } else {

            }*/
        }
        dialog.setNegativeButton("No", null)

        dialog.show()

    }
}