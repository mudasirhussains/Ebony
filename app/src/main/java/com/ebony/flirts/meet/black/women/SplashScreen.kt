package com.ebony.flirts.meet.black.women

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.RemoteException
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.kochava.base.Tracker
import java.io.IOException

class SplashScreen : AppCompatActivity() {
    private var completeUrl: String = ""
    private var deviceId: String = ""
    private var pushToken: String = ""
    private var kdId: String = ""
    private var gaid: String = ""
    private lateinit var referrerClient: InstallReferrerClient
    var referrerUrl: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val decorView: View = window.decorView
        val uiOptions: Int = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
        var actionBar = supportActionBar
        actionBar!!.hide()

        if (isNetworkAvailable(this)) {
            goToMain()
        } else {
            setupConnectivity()
        }

    }

    private fun goToMain() {
        //one signal ID
        pushToken = "6937ac88-bba1-4436-beba-6e6709a3fc31"

        //1st for referrer client
        referrerClient = InstallReferrerClient.newBuilder(this).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        // Connection established.
                        Log.d(
                            "InstallReferrerSetup",
                            "onInstallReferrerSetupFinished: Connection established"
                        )
                        var response: ReferrerDetails? = null
                        try {
                            response = referrerClient.installReferrer
                        } catch (e: RemoteException) {
                            e.printStackTrace()
                        }
                        referrerUrl = response!!.installReferrer
                        val referrerClickTime = response!!.referrerClickTimestampSeconds
                        val appInstallTime = response!!.installBeginTimestampSeconds
                        val instantExperienceLaunched = response!!.googlePlayInstantParam

                        Log.d("referrerUrl==", "" + referrerUrl)

                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        // API not available on the current Play Store app.
                        Log.d(
                            "InstallReferrerSetup",
                            "onInstallReferrerSetupFinished: API not available"
                        )
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        // Connection couldn't be established.
                        Log.d(
                            "InstallReferrerSetup",
                            "onInstallReferrerSetupFinished: Connection couldn't be established"
                        )
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
            }
        })


        //2nd for gaid
        val thread: Thread = object : Thread() {
            override fun run() {
                try {
                    val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(
                        applicationContext
                    )
                    val advertisingId: String? = if (adInfo != null) adInfo.id else null
                    gaid = advertisingId.toString()
                    Log.d("gaidUrl==", "" + gaid)
                } catch (exception: IOException) {
                    exception.printStackTrace()
                } catch (exception: GooglePlayServicesRepairableException) {
                    exception.printStackTrace()
                } catch (exception: GooglePlayServicesNotAvailableException) {
                    exception.printStackTrace()
                }
            }
        }
        thread.start()


        //3rd for Kochava Tracker
        Tracker.configure(
            Tracker.Configuration(applicationContext)
                .setAppGuid("koebony-flirt-8yg")
        )
        kdId = Tracker.getDeviceId()
        Log.d("myKochavaDeviceId", "startKochavaSDK: " + kdId)

        //4th for device ID
        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)



        //start intent for Web View Activity
        Handler().postDelayed({
            var completeURL =
                "http://ebflt.cyou?utm_source=ebflt_aosapp&device_id=" + deviceId + "&push-token=" + pushToken + "&kd_id=" + kdId + "&ref=" + referrerUrl + "&gaid=" + gaid
            Log.d("URRL==", ""+completeURL)
            var intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("completeURL", completeURL)
            startActivity(intent)
            finish()

        }, 3000)

    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
    }

    private fun setupConnectivity() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Permission to access the internet is required for this app.")
            .setTitle("Internet required")
        builder.setPositiveButton(
            "Go to Settings"
        ) { dialog, id ->
            dialog.dismiss()
            this!!.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        }
        val dialog = builder.create()
        dialog.show()
    }

}