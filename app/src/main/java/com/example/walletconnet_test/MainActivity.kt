package com.example.walletconnet_test

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.net.URISyntaxException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myWebView: WebView = findViewById(R.id.webView)

        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.domStorageEnabled = true
        myWebView.settings.javaScriptCanOpenWindowsAutomatically = true
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