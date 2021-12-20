/*
 * Created by Devlomi on 2020
 */

package com.truelife.chat.activities.calling.model

enum class CallingState {
    NONE,
    INITIATING,
    CONNECTING,
    CONNECTED,
    FAILED,
    RECONNECTING,
    ANSWERED,
    REJECTED_BY_USER,
    NO_ANSWER,
    ERROR

}