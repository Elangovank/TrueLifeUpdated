package com.truelife.chat.activities.authentication

interface AuthCallbacks {
    fun verifyPhoneNumber(phoneNumber: String,countryIso:String, screen:String, otpListener : OnOTPSent)
    fun verifyCode(code:String)
    fun cancelVerificationRequest()
}