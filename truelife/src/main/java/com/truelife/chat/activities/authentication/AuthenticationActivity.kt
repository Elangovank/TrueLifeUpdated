package com.truelife.chat.activities.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import  com.truelife.R
import com.truelife.chat.activities.SplashActivity
import com.truelife.chat.activities.setup.SetupUserActivity
import com.truelife.chat.utils.IntentUtils
import com.truelife.chat.utils.Util
import com.truelife.chat.utils.network.AuthManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import durdinapps.rxfirebase2.RxFirebaseAuth
import kotlinx.android.synthetic.main.activity_authentication.*


class AuthenticationActivity : AppCompatActivity(), AuthCallbacks {


    private val viewModel: AuthenticationViewModel by viewModels()

    private var isCancelled = false
    private var storedVerificationId = ""
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var navigation: NavController
    private lateinit var authCallback: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var navHostFragment: NavHostFragment
    private var currentPhoneNumber = ""
    private var countryCode = ""
    private var rawPhone = ""
    private var fromScreen = ""
    private lateinit var onotpsent: OnOTPSent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        navigation = Navigation.findNavController(this, R.id.nav_host_fragment)
        navigation.setGraph(R.navigation.nav_signup)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        auth = FirebaseAuth.getInstance()

        authCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(authCredential: PhoneAuthCredential) {
                Log.d("AuthenticationActivity", "onVerificationCompleted: ")
                if (!isCancelled)
                    signInWithCredential(authCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("AuthenticationActivity", "onVerificationFailed: ")
                setLoading(false)

                if (e is FirebaseAuthException) {
                    val message = FirebaseAuthError.fromException(e).description

                    AlertDialog.Builder(this@AuthenticationActivity).apply {
                        setMessage(message)
                        setPositiveButton(R.string.ok, null)
                        show()
                    }
                }
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d("AuthenticationActivity", "onCodeSent: ")
                super.onCodeSent(verificationId, token)

                storedVerificationId = verificationId
                resendToken = token

                val bundle = bundleOf(Pair(IntentUtils.PHONE, currentPhoneNumber),
                        Pair(IntentUtils.COUNTRY_CODE, countryCode),
                        Pair(IntentUtils.RAW_PHONE, rawPhone))
                onotpsent.otpSent()
                if (fromScreen != "otp")
                    navigation.navigate(R.id.action_enterPhoneNumberFragment_to_verifyPhoneFragment, bundle)
                setLoading(false)
            }
        }
    }

    override fun verifyPhoneNumber(phoneNumber: String, countryCode: String, screen: String, otplistener : OnOTPSent) {
        Log.d("verifyPhoneNumber", "phoneNumber: " + phoneNumber + " countryCode " + countryCode)
        this.countryCode = countryCode
        this.rawPhone = phoneNumber
        this.fromScreen = screen
        this.onotpsent = otplistener
        setLoading(true)
        val authManager = AuthManager()

        authManager.formatNumber(phoneNumber, countryCode)?.let { formattedNumber ->
            currentPhoneNumber = formattedNumber
            authManager.verify(formattedNumber, this, authCallback)
        }
    }

    override fun verifyCode(code: String) {
        try {
            val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
            if (!isCancelled)
                signInWithCredential(credential)
        } catch (e: Exception) {

        }

    }

    override fun cancelVerificationRequest() {
        isCancelled = true
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        setLoading(true)

        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            setLoading(false)

            if (task.isSuccessful) {
                startActivity(Intent(this, AuthenticationActivity::class.java))

            } else {
                task.exception?.let { exception ->
                    if (exception is FirebaseAuthInvalidCredentialsException) {
                        AlertDialog.Builder(this).apply {
                            setMessage(R.string.invalid_verification_code)
                            setPositiveButton(R.string.ok, null)
                            show()
                        }
                    } else {
                        Util.showSnackbar(this, exception.localizedMessage, Snackbar.LENGTH_LONG)

                    }
                }
            }

        }
    }

    private fun setLoading(setLoading: Boolean) {
        progressbar.isVisible = setLoading

        navHostFragment.childFragmentManager.fragments.getOrNull(0)?.let { fragment ->
            if (fragment is BaseAuthFragment) {
                if (setLoading)
                    fragment.disableViews()
                else
                    fragment.enableViews()

            }
        }


    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            startSplashActivity()
        }


    }

    private fun startSplashActivity() {
        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
        finish()
    }


}