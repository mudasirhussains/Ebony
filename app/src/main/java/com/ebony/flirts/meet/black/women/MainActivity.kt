package com.ebony.flirts.meet.black.women

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.webkit.*
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.geeklabs.webviewkotlin.Progress
import com.google.android.material.snackbar.Snackbar
import com.adjust.sdk.webbridge.AdjustBridge
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    lateinit var webView: WebView
    lateinit var infoTV: TextView
    private var isLoaded: Boolean = false
    private var doubleBackToExitPressedOnce = false
    private var webURL = ""

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adjustToolBar()
        setBindingIds()
        webURL = intent.getStringExtra("completeURL").toString()
        Log.d("CUrl==", "" + webURL)

        if (savedInstanceState == null) {
            webView.loadUrl(webURL)
        }

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.allowFileAccess = true
        settings.domStorageEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.supportMultipleWindows()
        //progress = Progress(this, R.string.please_wait, cancelable = true)
        if (!isOnline()) {
            showToast(getString(R.string.no_internet))
            infoTV.text = getString(R.string.no_internet)
            showNoNetSnackBar()
            return
        }

        AdjustBridge.registerAndGetInstance(application, webView)
        try {
            webView.loadUrl(webURL)
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    private fun adjustToolBar() {
        val decorView: View = window.decorView
        val uiOptions: Int = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
        var actionBar = supportActionBar
        actionBar!!.hide()
    }

    private fun setBindingIds() {
        webView = findViewById(R.id.webView)
        infoTV = findViewById(R.id.infoTV)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        if (isOnline() && !isLoaded) loadWebView()
        super.onResume()
    }

    private fun loadWebView() {
        showProgress(true)
        infoTV.text = ""
        webView.loadUrl(webURL)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                if (url.startsWith("tel:") || url.startsWith("whatsapp:")) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    webView.goBack()
                    return true
                } else {
                    view?.loadUrl(url)
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                showProgress(true)
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                isLoaded = true
                showProgress(false)
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                isLoaded = false
                val errorMessage = "Got Error! $error"
                showToast(errorMessage)
                infoTV.text = errorMessage
                showProgress(false)
                super.onReceivedError(view, request, error)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {

                when {
                    doubleBackToExitPressedOnce -> {
                        showCloseDialog()
                    }
                    else -> {
                        doubleBackToExitPressedOnce = true
                        if (webView.canGoBack()) {
                            webView.goBack()
                        } else {
                            showCloseDialog()
                        }
                        Handler(Looper.myLooper()!!).postDelayed(
                            { doubleBackToExitPressedOnce = false },
                            2000
                        )
                    }
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }


    private fun showProgress(visible: Boolean) {
        //progress?.apply { if (visible) show() else dismiss() }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isOnline(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showNoNetSnackBar() {
        Toast.makeText(applicationContext, "No internet", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        when {
            doubleBackToExitPressedOnce -> {
                showCloseDialog()
            }
            else -> {
                doubleBackToExitPressedOnce = true
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    showCloseDialog()
                }
                Handler(Looper.myLooper()!!).postDelayed(
                    { doubleBackToExitPressedOnce = false },
                    2000
                )
            }
        }
    }

    private fun showCloseDialog() {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setMessage("Are you sure you want to exit?")
        builder.setPositiveButton("YES") { dialog, which ->
            finish()
        }
        builder.setNegativeButton("NO", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }

    override fun onDestroy() {
        AdjustBridge.unregister()
        super.onDestroy()
    }

}