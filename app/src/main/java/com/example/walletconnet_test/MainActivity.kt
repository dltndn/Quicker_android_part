package com.example.walletconnet_test

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.MediaStore
import android.provider.SyncStateContract.Constants
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.net.URISyntaxException
import java.util.jar.Manifest
import java.util.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_SELECT_FILE = 100
    private val REQUEST_LOCATION_PERMISSION = 100
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var locationCallback: GeolocationPermissions.Callback? = null
    val ACTION_START_LOCATION_SERVICE = "startLocationService"
    val ACTION_STOP_LOCATION_SERVICE = "stopLocationService"
    private var isAlreadyStartedLocationService = false

//    private var locationService: LocationService? = null
    private lateinit var serviceIntent: Intent

    private lateinit var notificationManager: NotificationManager
    private lateinit var builderT: NotificationCompat.Builder

    private fun startBackgroundService() {
        serviceIntent = Intent(this@MainActivity, LocationService::class.java)
        serviceIntent.action = ACTION_START_LOCATION_SERVICE
        startService(serviceIntent)
        Log.i("background", "startLocationService")
    }

    private fun stopBackgroundService() {
        serviceIntent.action = ACTION_STOP_LOCATION_SERVICE
        stopService(serviceIntent)
        Log.i("background", "stopLocationService")
    }

    private fun alertNotification(contentString: String) {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "impo_notification_channel"
        val resultIntent = Intent()
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            resultIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        builderT = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.casual_life_3d_cardboard_boxes)
            .setContentTitle("Quicker")
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentText(contentString)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val notificationChannel = NotificationChannel(
                    channelId,
                    "Location Service",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationChannel.description = "This channel is used by test"
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        val notificationId = 176
        notificationManager.notify(notificationId, builderT.build())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("QuickerPref", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val chooserIntent = Intent(Intent.ACTION_GET_CONTENT)
        chooserIntent.type = "image/*"

        val fileIntent = Intent(Intent.ACTION_PICK)
        fileIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")

        val intent = Intent.createChooser(chooserIntent, "Choose Image")
        intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(fileIntent))

        // 위치 권한 요청
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            startBackgroundService()
            isAlreadyStartedLocationService = true
        }
        if (!isAlreadyStartedLocationService) {
            startBackgroundService()
        }

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
                    val walletAddress = quickerIntent.data?.getQueryParameter("walletAddress")
                    val isDelivering = quickerIntent.data?.getQueryParameter("isDelivering")
                    val isMatchedOrder = quickerIntent.data?.getQueryParameter("isMatchedOrder")
                    val isDeliveredOrder = quickerIntent.data?.getQueryParameter("isDeliveredOrder")
                    val isCompletedOrder = quickerIntent.data?.getQueryParameter("isCompletedOrder")
                    Log.i("wallet", walletAddress.toString())
                    Log.i("isDelivering", isDelivering.toString())
                    if (walletAddress != null) {
                        editor.putString("q_walletAddress", walletAddress.toString())
                        editor.apply()
                    }
                    if (isDelivering.toString() == "true") {
                        editor.putBoolean("q_isDelivering", true)
                        editor.apply()
                    } else if (isDelivering.toString() == "false") {
                        editor.putBoolean("q_isDelivering", false)
                        editor.apply()
                    }
                    if (isMatchedOrder.toString() == "true") {
                        alertNotification("배송원이 오더를 수락하였습니다. 오더 내역을 확인해보세요!")
                    }
                    if (isDeliveredOrder.toString() == "true") {
                        alertNotification("배송원이 배송을 완료하였습니다. 오더 내역을 확인해보세요!")
                    }
                    if (isCompletedOrder.toString() == "true") {
                        alertNotification("의뢰인이 계약을 확정했습니다. 수행 내역을 확인해보세요!")
                    }
                    return true
                }
                if (request?.url?.toString()?.startsWith("https:") == true) {
                    val wcUri = Uri.parse(deepLink)
                    val httpsIntent = Intent(Intent.ACTION_VIEW, wcUri)
                    httpsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(httpsIntent)
                    return true
                }
                if (request?.url?.toString()?.startsWith("nmap:") == true) {
                    val nmapUri = Uri.parse(deepLink)
                    val nmapIntent = Intent(Intent.ACTION_VIEW, nmapUri)
                    nmapIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(nmapIntent)
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
                locationCallback = callback
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
                    locationCallback?.invoke(origin, true, true)
                }
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                    // 권한이 없는 경우 권한 요청
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        REQUEST_LOCATION_PERMISSION
                    )
                    locationCallback?.invoke(origin, true, true)
                } else {
                    Log.i("test", "what?")
                }
                // 백그라운드에서 주기적으로 위치 정보를 얻어오는 함수
                startBackgroundService()
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

    override fun onDestroy() {
        super.onDestroy()
        stopBackgroundService()
        Log.i("background", "onDestroy")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBackgroundService()
            } else {
                Toast.makeText(this@MainActivity, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
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
            finish()
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