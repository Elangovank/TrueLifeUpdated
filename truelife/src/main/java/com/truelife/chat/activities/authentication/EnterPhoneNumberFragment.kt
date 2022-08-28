package com.truelife.chat.activities.authentication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.widget.CheckBox
import android.widget.Toast
import com.truelife.R
import com.truelife.TLApplication
import com.truelife.app.activity.TLSigninActivity
import com.truelife.app.constants.TLConstant.SELECTED_COUNTRY_CODE
import com.truelife.app.constants.TLConstant.SELECTED_MOBILE_NUMBER
import com.truelife.chat.utils.NetworkHelper
import com.truelife.chat.utils.Util
import com.truelife.storage.LocalStorageSP
import kotlinx.android.synthetic.main.fragment_enter_phone_number.*

class EnterPhoneNumberFragment : BaseAuthFragment() {

    private lateinit var onotpsent: OnOTPSent


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_enter_phone_number, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cp.setDefaultCountryUsingNameCode("US")
        cp.detectSIMCountry(true)

        txtAlreadyHave.setOnClickListener {
            val intent = Intent(activity, TLSigninActivity::class.java)
            intent.putExtra("alreadyUser", true)
            startActivity(intent)

        }

        tc_tv.setOnClickListener({ v: View? ->
            showTcDialog()
        })

        pp_tv.setOnClickListener({ v: View? ->
            showppDialog()
        })

        btn_verify.setOnClickListener {

            if (!chb_agree.isChecked) {
                Toast.makeText(
                    requireContext(),
                    "Please choose Terms and Policy",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val number = et_number.text.toString().trim()
                val fullNumber = cp.selectedCountryCodeWithPlus + number

                //dismiss keyboard
                et_number.onEditorAction(EditorInfo.IME_ACTION_DONE)

                onotpsent = object : OnOTPSent {
                    override fun otpSent() {

                    }
                }


                AlertDialog.Builder(requireActivity()).apply {
                    val message = requireActivity().getString(
                        R.string.enter_phone_confirmation_message,
                        fullNumber
                    )
                    setMessage(message)
                    setNegativeButton(R.string.edit, null)
                    setPositiveButton(R.string.ok) { _, _ ->
                        //check for internet connection
                        if (NetworkHelper.isConnected(TLApplication.context())) {

                            LocalStorageSP.put(
                                requireContext(),
                                SELECTED_COUNTRY_CODE,
                                cp.selectedCountryCodeWithPlus
                            )
                            LocalStorageSP.put(
                                requireContext(),
                                SELECTED_MOBILE_NUMBER,
                                number
                            )

                            if (TextUtils.isEmpty(et_number.text) || TextUtils.isDigitsOnly(
                                    et_number.text
                                ).not()
                            )
                                Util.showSnackbar(
                                    requireActivity(),
                                    requireActivity().getString(R.string.enter_correct_number)
                                )
                            else {
                                callbacks?.verifyPhoneNumber(
                                    number,
                                    cp.selectedCountryNameCode,
                                    "number",
                                    onotpsent
                                )
                            }

                        } else {
                            Util.showSnackbar(
                                requireActivity(),
                                requireActivity().getString(R.string.no_internet_connection)
                            )
                        }
                    }
                    show()
                }
            }
        }

    }

    private fun showTcDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
        dialog.setTitle("Terms and Conditions")
        dialog.setCancelable(false)

        val view = LayoutInflater.from(requireActivity())
            .inflate(R.layout.privacy_policy_dialog, null, false)
        dialog.setView(view)
        // val tv = view.findViewById<TextView>(R.id.tv_privacy_policy_dialog)
        val checkBox = view.findViewById<CheckBox>(R.id.chb_agree)
        checkBox.text =
            "By Checking this, You agree to the collection and use of information in accordance with this Privacy Policy"
        val wv: WebView
        wv = view.findViewById<WebView>(R.id.webview_privacy_policy_dialog)
        wv.loadUrl("file:///android_asset/terms_and_condition.html")
        //getHtml4(tv)
        dialog.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }
        val mDialog = dialog.show()
    }

    private fun showppDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
        dialog.setTitle("Privacy policy")
        dialog.setCancelable(false)

        val view = LayoutInflater.from(requireActivity())
            .inflate(R.layout.privacy_policy_dialog, null, false)
        dialog.setView(view)
        // val tv = view.findViewById<TextView>(R.id.tv_privacy_policy_dialog)
        val checkBox = view.findViewById<CheckBox>(R.id.chb_agree)
        checkBox.text =
            "By Checking this, You agree to the collection and use of information in accordance with this Privacy Policy"
        val wv: WebView
        wv = view.findViewById<WebView>(R.id.webview_privacy_policy_dialog)
        wv.loadUrl("file:///android_asset/privacy_policy.html")
        //getHtml4(tv)
        dialog.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }
        val mDialog = dialog.show()
    }

    override fun enableViews() {
        super.enableViews()
        et_number.isEnabled = true
        btn_verify.isEnabled = true
    }

    override fun disableViews() {
        super.disableViews()
        et_number.isEnabled = false
        btn_verify.isEnabled = false
    }


}