package com.example.walletconnet_test

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.net.URISyntaxException
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val REQUEST_LOCATION_PERMISSION = 100

        val myWebView: WebView = findViewById(R.id.webView)

        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.domStorageEnabled = true
        myWebView.settings.javaScriptCanOpenWindowsAutomatically = true
        myWebView.settings.setGeolocationEnabled(true)
        myWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val deepLink = request?.url?.toString()
                if (request?.url?.toString()?.startsWith("wc:") == true) {
                    val wcUri = Uri.parse(deepLink)
                    val wcIntent = Intent(Intent.ACTION_VIEW, wcUri)
                    wcIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(wcIntent)
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }

//        myWebView.webChromeClient = WebChromeClient()
        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val newWebView = WebView(this@MainActivity)
                newWebView.settings.javaScriptEnabled = true
                newWebView.settings.domStorageEnabled = true
                newWebView.settings.javaScriptCanOpenWindowsAutomatically = true
                newWebView.webViewClient = WebViewClient()
                newWebView.webChromeClient = this
                myWebView.addView(newWebView)
                val transport = resultMsg?.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()
                return true
            }

            // 위치 권한 요청
            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // 권한이 없는 경우 권한 요청
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_LOCATION_PERMISSION
                    )
                } else {
                    // 권한이 있는 경우 다이얼로그 보여줌
                    showLocationPermissionDialog(origin ?: "", callback ?: return)
                }

            }

            // 위치 권한 요청 다이얼로그
            private fun showLocationPermissionDialog(
                origin: String,
                callback: GeolocationPermissions.Callback
            ) {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setMessage("이 앱에 위치사용 권한을 허용하시겠습니까?")
                    .setCancelable(true)
                    .setPositiveButton("네") { _: DialogInterface, _: Int ->
                        callback.invoke(origin, true, true)
                    }
                    .setNegativeButton("아니요") { _: DialogInterface, _: Int ->
                        callback.invoke(origin, false, false)
                    }
                val dialog = builder.create()
                dialog.show()
            }

        }
        myWebView.loadUrl("https://web-quicker-reactjs-luj2cle2iiwho.sel3.cloudtype.app/")


    }
//
    private var isToastActive = false
    private var currentToast: Toast? = null

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onBackPressed() {
        if (isToastActive) {
            super.onBackPressed()
        } else {
            showFinishToast()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun showFinishToast() {
        currentToast?.cancel() // Cancel any existing toast message
        currentToast = Toast.makeText(this, "앱을 종료하시겠습니까?", Toast.LENGTH_SHORT)
        currentToast?.addCallback(object : Toast.Callback() {
            override fun onToastShown() {
                super.onToastShown()
                isToastActive = true
            }
            override fun onToastHidden() {
                super.onToastHidden()
                isToastActive = false
            }
        })
        currentToast?.show()
    }

}