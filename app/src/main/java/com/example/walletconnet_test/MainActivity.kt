package com.example.walletconnet_test

import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.os.PatternMatcher
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val REQUEST_LOCATION_PERMISSION = 100
        val REQUEST_CODE_PERMISSIONS = 101
        val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

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
                    Toast.makeText(this@MainActivity, data.toString(), Toast.LENGTH_LONG)
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }

        var uploadMessage: ValueCallback<Array<Uri?>>? = null

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

            // 파일 선택 테스트 코드 s

            // 파일을 웹으로 전송할 수 있다. 반환값을 true설정하고 인텐트를 통해 데이터를 전송한다.
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri?>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                //일단은 image 를 업로드한다는 가정에서 작업.
                //val acceptType = fileChooserParams?.acceptTypes?.get(0)?.toString() //access 타입을 체크합니다.

                //filePathCallback: ValueCallback<Array<Uri?> 이놈의 경우 한번 오픈했으면 반드시 초기화 해줘야합니다.
                //확인또확인!
                //안 그러면 두번다시 안열리거나 에러를 뱉습니다.
                if (uploadMessage != null) {
                    uploadMessage!!.onReceiveValue(null)
                }
                uploadMessage = filePathCallback

//                if (ContextCompat.checkSelfPermission(
//                        this@MainActivity,
//                        android.Manifest.permission.READ_EXTERNAL_STORAGE
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) { //권한이 있다면
//
//                }else{
//
//                    //… 권한이 없을경우 작업 처리
//                    //…
//
//                    //얘는 무조건 해줘야 합니다!!!
//                    if (uploadMessage != null) {
//                        uploadMessage!!.onReceiveValue(null)
//                    }
//                    uploadMessage = null
//
//                }
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        REQUIRED_PERMISSIONS.toString()
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // 권한이 없는 경우 권한 요청
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(REQUIRED_PERMISSIONS.toString()),
                        REQUEST_CODE_PERMISSIONS
                    )
                    if (uploadMessage != null) {
                        uploadMessage!!.onReceiveValue(null)
                    }
                    uploadMessage = null
                } else {

                }
                return true
            }



            // 웹뷰에서 권한을 사용 시 호출되며 권한 사용을 수락할 수 있다.
            override fun onPermissionRequest(request: PermissionRequest?) {
                try {
                    request?.grant(request.resources)
                } catch (e: Exception) {
                    Log.i("myWebview" ,"permissionRequest: $e")
                }

            }

            // 파일 선택 테스트 코드 e

        }
        myWebView.loadUrl("https://web-quicker-reactjs-luj2cle2iiwho.sel3.cloudtype.app/")


    }
    //




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