package com.truelife.chat.adapters.messaging

import androidx.lifecycle.LiveData
import com.truelife.chat.model.AudibleState

interface AudibleBase {
    var audibleState: LiveData<Map<String, AudibleState>>?
    var audibleInteraction:AudibleInteraction?
}