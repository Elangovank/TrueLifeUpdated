/*
 * Created by Devlomi on 2020
 */

package com.truelife.chat.activities.calling.model;

import com.truelife.chat.activities.calling.model.AGEventHandler;

import io.agora.rtc.IRtcEngineEventHandler;

public interface BeforeCallEventHandler extends AGEventHandler {
    void onLastmileQuality(int quality);

    void onLastmileProbeResult(IRtcEngineEventHandler.LastmileProbeResult result);
}
