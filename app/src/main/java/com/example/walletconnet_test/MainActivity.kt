package com.example.walletconnet_test

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.os.PatternMatcher
import android.provider.MediaStore
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.net.URISyntaxException
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    private val REQUEST_SELECT_FILE = 100
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val REQUEST_LOCATION_PERMISSION = 100
        val chooserIntent = Intent(Intent.ACTION_GET_CONTENT)
        chooserIntent.type = "image/*"

        val fileIntent = Intent(Intent.ACTION_PICK)
        fileIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")

        val intent = Intent.createChooser(chooserIntent, "Choose Image")
        intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(fileIntent))
        // e

        val myWebView: WebView = findViewById(R.id.webView)

        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.domStorageEnabled = true
        myWebView.settings.javaScriptCanOpenWindowsAutomatically = true
        myWebView.settings.setGeolocationEnabled(true)
        myWebView.settings.loadWithOverviewMode = true
        myWebView.settings.useWideViewPort = true
        myWebView.settings.allowContentAccess = true
        myWebView.settings.allowFileAccess = true
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
                if (request?.url?.toString()?.startsWith("quicker:") == true) {
                    // 인텐트 필터 등록
                    val quickerUri = Uri.parse(deepLink)
                    val quickerIntent = Intent(Intent.ACTION_VIEW, quickerUri)
                    val data = quickerIntent.data?.getQueryParameter("walletAddress")
                    Log.i("wallet", data.toString())
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }

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

            val activity: Activity = this@MainActivity
            // 파일 선택 다이얼로그를 호출한다.
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                if (this@MainActivity.filePathCallback != null) {
                    this@MainActivity.filePathCallback?.onReceiveValue(null)
                }
                this@MainActivity.filePathCallback = filePathCallback

                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE)
                } catch (e: ActivityNotFoundException) {
                    this@MainActivity.filePathCallback = null
                    Toast.makeText(activity, "Cannot open file chooser", Toast.LENGTH_LONG).show()
                    return false
                }

                return true
            }

        }
        myWebView.loadUrl("https://web-quicker-reactjs-luj2cle2iiwho.sel3.cloudtype.app/")

    }
    //
    // 파일 선택 다이얼로그에서 선택한 결과를 처리한다.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SELECT_FILE) {
            if (filePathCallback == null) return
            val result = if (data == null || resultCode != RESULT_OK) null else data.data
            if (result == null) {
                filePathCallback?.onReceiveValue(null)
            } else {
                filePathCallback?.onReceiveValue(arrayOf(result))
            }
            filePathCallback = null
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
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