package com.truelife.storage

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.truelife.app.constants.TLConstant
import com.truelife.app.model.User

/**
 * Created by Elango on 28/01/19.
 **/

object LocalStorageSP {

    private fun getEditor(c: Context): SharedPreferences.Editor {
        return getPreference(c).edit()
    }

    private fun getPreference(c: Context): SharedPreferences {
        return c.getSharedPreferences(
            TLConstant.PREF_FILE_NAME, Context.MODE_PRIVATE
        )
    }

    /**
     * Store the login user data in stored preferences for further access. Consider like a session
     *
     * @param c Context of the app
     * @param u User Object
     */
    internal fun storeLoginUser(c: Context, u: User?) {
        val e = getEditor(c)
        if (u == null) {
            e.remove("user_info")
        } else {
            val gson = Gson()
            e.putString("user_info", gson.toJson(u))
        }
        e.commit()

    }

    /**
     * Get login user object from stored preferences
     *
     * @param c Context of the app
     * @return User instance if exists else nulls
     */
    fun getLoginUser(c: Context?): User {
        val s = getPreference(c!!).getString("user_info", "")
        if (s == null || s.isEmpty()) {
            return User()
        }
        return Gson().fromJson(s, User::class.java)
    }

    /**
     * @param c     Context
     * @param key   key
     * @param value value
     */
    fun put(c: Context, key: String, value: String) {
        val editor = getEditor(c)
        editor.putString(key, value)
        editor.commit()
    }

    fun put(c: Context, key: String, value: Int) {
        val editor = getEditor(c)
        editor.putInt(key, value)
        editor.commit()
    }

    operator fun get(c: Context, key: String, dv: Int): Int? {
        return getPreference(c).getInt(key, dv)
    }

    /**
     * @param c   Context
     * @param key key
     * @param dv  default value
     */
    operator fun get(c: Context, key: String, dv: String): String? {
        return getPreference(c).getString(key, dv)
    }

    /**
     * @param c     Context
     * @param key   key
     * @param value value
     */
    fun put(c: Context, key: String, value: Boolean) {
        val editor = getEditor(c)
        editor.putBoolean(key, value)
        editor.commit()
    }

    /**
     * @param c   Context
     * @param key key
     * @param dv  default value
     */
    operator fun get(c: Context, key: String, dv: Boolean): Boolean? {
        return getPreference(c).getBoolean(key, dv)
    }

    fun clearAll(c: Context) {
        val editor = getEditor(c)
        editor.clear()
        editor.commit()
    }

    fun clear(c: Context, key: String) {
        val editor = getEditor(c)
        editor.remove(key)
        editor.commit()
    }
}
