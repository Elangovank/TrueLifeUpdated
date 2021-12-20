package com.truelife.app.listeners

import com.truelife.app.model.LocationModel

interface SelectedListener {
    fun onClick(
        aName: String?,
        aId: String?,
        aCode: String?,
        alocation: LocationModel?
    )
}