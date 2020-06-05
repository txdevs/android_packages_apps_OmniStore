package org.omnirom.omnistore

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.util.Log
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

class NetworkUtils {
    private val TAG = "OmniStore:NetworkUtils"
    private val HTTP_READ_TIMEOUT = 30000
    private val HTTP_CONNECTION_TIMEOUT = 30000

    inner class FetchAppsTask(
        context: Context,
        preAction: Runnable,
        postAction: Runnable,
        newAppsList: ArrayList<AppItem>
    ) : AsyncTask<String, Int, Int>() {
        val mNewAppsList: ArrayList<AppItem> = newAppsList
        val mPreaction: Runnable = preAction
        val mPostAction: Runnable = postAction
        val mContext: Context = context

        override fun onPreExecute() {
            super.onPreExecute()
            mPreaction.run()
        }

        override fun doInBackground(vararg params: String?): Int {
            val appListData: String? = downloadUrlMemoryAsString(Constants.APPS_LIST_URI)
            if (appListData != null) {
                val apps = JSONArray(appListData)
                for (i in 0 until apps.length()) {
                    val app = apps.getJSONObject(i);
                    val appData = AppItem(app)
                    if (appData.isValied(DeviceUtils().getProperty(mContext, "ro.omni.device"))) {
                        mNewAppsList.add(appData)
                    } else {
                        Log.i(TAG, "ignore app " + app.toString())
                    }
                }
            }
            mNewAppsList.sortBy { it -> it.title() }
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            mPostAction.run()
        }
    }

    private fun setupHttpsRequest(urlStr: String): HttpsURLConnection? {
        val url: URL
        try {
            url = URL(urlStr)
            val urlConnection = url.openConnection() as HttpsURLConnection
            urlConnection.setConnectTimeout(HTTP_CONNECTION_TIMEOUT)
            urlConnection.setReadTimeout(HTTP_READ_TIMEOUT)
            urlConnection.setRequestMethod("GET")
            urlConnection.setDoInput(true)
            urlConnection.connect()
            val code: Int = urlConnection.getResponseCode()
            if (code != HttpsURLConnection.HTTP_OK) {
                Log.d(TAG, "response: " + code)
                return null
            }
            return urlConnection
        } catch (e: Exception) {
            Log.e(TAG, "setupHttpsRequest " + e, e)
            return null
        }
    }

    private fun downloadUrlMemoryAsString(url: String): String? {
        Log.d(TAG, "download: " + url)
        var urlConnection: HttpsURLConnection? = null
        return try {
            urlConnection = setupHttpsRequest(url)
            if (urlConnection == null) {
                return null
            }
            val input: InputStream = urlConnection.inputStream
            val byteArray = ByteArrayOutputStream()
            var byteInt: Int = 0
            while (input.read().also({ byteInt = it }) >= 0) {
                byteArray.write(byteInt)
            }
            val bytes: ByteArray = byteArray.toByteArray() ?: return null
            String(bytes, StandardCharsets.UTF_8)
        } catch (e: java.lang.Exception) {
            // Download failed for any number of reasons, timeouts, connection
            // drops, etc. Just log it in debugging mode.
            Log.e(TAG, "downloadUrlMemoryAsString " + e, e)
            null
        } finally {
            urlConnection?.disconnect()
        }
    }
}