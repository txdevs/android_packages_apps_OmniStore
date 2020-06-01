package org.omnirom.omnistore

import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.omnirom.omnistore.Constants.APPS_BASE_URI

class AppItem(val appData: JSONObject) {
    var mInstalled = false
    var mDownloadId: Long? = -1
    private val TAG = "OmniStore:AppItem"

    fun isValied(): Boolean {
        try {
            appData.get("file").toString()
            appData.get("title").toString()
            appData.get("package").toString()
            appData.get("icon").toString()

            if (appData.has("devices")) {
                val devices:JSONArray = appData.getJSONArray("devices")
                for (i in 0 until devices.length()) {
                    if (devices.get(i).toString().equals(Build.DEVICE)) {
                        return true;
                    }
                }
            } else {
                return true
            }
        } catch (e: JSONException) {
            Log.e(TAG, "isValied", e)
        }
        return false
    }

    fun file(): String {
        try {
            return appData.get("file").toString()
        } catch (e: JSONException) {
            return "unknown"
        }
    }

    fun title(): String {
        try {
            return appData.get("title").toString()
        } catch (e: JSONException) {
            return "unknown"
        }
    }

    fun iconUrl(): String? {
        try {
            return APPS_BASE_URI + appData.get("icon").toString()
        } catch (e: JSONException) {
            return null
        }
    }

    fun pkg(): String? {
        try {
            return appData.get("package").toString()
        } catch (e: JSONException) {
            return "unknown"
        }
    }
}