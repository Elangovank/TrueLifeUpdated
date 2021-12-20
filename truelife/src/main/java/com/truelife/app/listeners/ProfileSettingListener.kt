package com.truelife.app.listeners

interface ProfileSettingListener {
    fun onAdd(tt: Any, position: Int, type: String)
    fun onDelete(position: Int, type: String)
    fun fromDate(obj: Any, position: Int, type: String)
    fun toDate(obj: Any, position: Int, type: String)

    fun update(position: Int, text: String, type: String)
}