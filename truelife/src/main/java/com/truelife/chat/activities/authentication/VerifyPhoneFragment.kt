package com.truelife.chat.activities.authentication

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.navigation.Navigation
import  com.truelife.R
import com.truelife.TLApplication
import com.truelife.chat.utils.IntentUtils
import com.truelife.chat.utils.NetworkHelper
import com.truelife.chat.utils.Util
import kotlinx.android.synthetic.main.fragment_enter_phone_number.*
import kotlinx.android.synthetic.main.fragment_verify_phone.*


class VerifyPhoneFragment : BaseAuthFragment() {

    private var countryCode = ""
    private var rawPhone = ""
    private lateinit var timer : CountDownTimer

    private lateinit var onotpsent: OnOTPSent

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verify_phone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString(IntentUtils.PHONE)?.let { phone ->
            tv_otp_info.text = requireActivity().getString(R.string.enter_the_otp_sent_to, phone)
        }

        arguments?.getString(IntentUtils.COUNTRY_CODE)?.let { country ->
            countryCode = country
        }
        arguments?.getString(IntentUtils.RAW_PHONE)?.let { rawPhone ->
            this.rawPhone = rawPhone
        }

        startTimer()

        et_otp.doOnTextChanged { text, _, _, _ ->
            if (text?.length == 6) {
                et_otp.onEditorAction(EditorInfo.IME_ACTION_DONE)
                completeRegistration()
            }
        }

        onotpsent = object : OnOTPSent {
            override fun otpSent() {
                startTimer()
            }
        }

        resend_otp.setOnClickListener {
            if (NetworkHelper.isConnected(TLApplication.context())) {
                callbacks?.verifyPhoneNumber(rawPhone, countryCode, "otp", onotpsent)
            } else {
                Util.showSnackbar(requireActivity(), requireActivity().getString(R.string.no_internet_connection))
            }
        }


        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {

                AlertDialog.Builder(requireActivity()).apply {
                    setMessage(R.string.cancel_verification_confirmation_message)
                    setNegativeButton(R.string.no, null)
                    setPositiveButton(R.string.yes) { _, _ ->
                        callbacks?.cancelVerificationRequest()
                        Navigation.findNavController(et_otp).navigateUp()
                    }
                    show()
                }

            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun startTimer() {
        Log.d("VerifyPhoneFragment", "startTimer: ")
        resend_otp.isEnabled = false
        timer = object: CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                resend_timer_label.setText(" in ")
                resend_timer.setText((millisUntilFinished / 1000).toString()+" Sec")
            }

            override fun onFinish() {
                Log.d("VerifyPhoneFragment", "onFinish: ")
                resend_timer_label.setText("")
                resend_timer.setText("")
                resend_otp.isEnabled = true
            }
        }
        timer.start()
    }

    private fun completeRegistration() {
        callbacks?.verifyCode(et_otp.text.toString())
    }

    override fun enableViews() {
        super.enableViews()
        et_otp.isEnabled = true
    }

    override fun disableViews() {
        super.disableViews()
        et_otp.isEnabled = false

    }

    override fun onDestroy() {
        super.onDestroy()

        timer?.cancel()
    }
}