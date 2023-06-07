package com.example.walletconnet_test

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationCallback


class LocationService : Service() {
    val LOCATION_SERVICE_ID = 175
    val ACTION_START_LOCATION_SERVICE = "startLocationService"
//    val ACTION_STOP_LOCATION_SERVICE = "stopLocationService"
    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder
    private lateinit var sharedPref: SharedPreferences
    val notiContextDelivering = "물품을 배달 중이세요. 마감기한을 지켜주세요!"
    val notiContextClient = "배송원이 물품을 배달 중입니다... "
    var walletAddress= "0x2cC285279f6970d00F84f3034439ab8D29D04d97"

    override fun onCreate() {
        super.onCreate()
        sharedPref = getSharedPreferences("QuickerPref", Context.MODE_PRIVATE)
    }

    private var mLocationCallback = object : LocationCallback() {
        var preIsLocationUpdatesActive = false
        override fun onLocationResult(p0: LocationResult) {

            // 배송 여부 값 불러오는 코드(2번째 인자는 데이터가 null일 때 반환 값)
            val isLocationUpdatesActive = sharedPref.getBoolean("q_isDelivering", false)
            val walletAddress = sharedPref.getString("q_walletAddress", "")
            Log.i("isLocationUpdatesActive", isLocationUpdatesActive.toString())
            if (isLocationUpdatesActive) {
                if (!preIsLocationUpdatesActive) {
                    updateNotificationContentText(notiContextDelivering)
                    preIsLocationUpdatesActive = true
                }
                if (p0 !== null) {
                    super.onLocationResult(p0)
                }
                if (p0 !== null && p0.lastLocation !== null) {
                    val latitude = p0.lastLocation.latitude
                    val longitude = p0.lastLocation.longitude
                    Log.v("LOCATION_UPDATE", "$latitude, $longitude")

                    val sendLocationToServer = SendLocationToServer()

                    // 사용자 정보 요청
                    if(walletAddress != "") {
                        if (walletAddress != null) {
                            sendLocationToServer.fetchUser(walletAddress, latitude, longitude)
                        }
                    }
                }
            } else {
                if (preIsLocationUpdatesActive) {
                    updateNotificationContentText(notiContextClient)
                    preIsLocationUpdatesActive = false
                }
            }
        }
    }

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun startLocationService() {
        val channelId = "location_notification_channel"
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
        builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.casual_life_3d_cardboard_boxes)
            .setContentTitle("Quicker")
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentText("Quicker를 실행 중입니다...")
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val notificationChannel = NotificationChannel(
                    channelId,
                    "Location Service",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationChannel.description = "This channel is used by location service"
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 7000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper()!!)
        startForeground(LOCATION_SERVICE_ID, builder.build())
    }

    private fun updateNotificationContentText(contentText: String) {
        builder.setContentText(contentText)

        notificationManager.notify(LOCATION_SERVICE_ID, builder.build())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("LocationService", "onStartCommand")
        if (intent.action != null) {
            when (intent.action) {
                ACTION_START_LOCATION_SERVICE -> startLocationService()
//                ACTION_STOP_LOCATION_SERVICE -> stopLocationService()
            }
        }
        return START_STICKY_COMPATIBILITY //서비스가 강제로 종료되더라도 시스템이 재시작하도록 설정
    }

}