package com.example.walletconnet_test

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
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
    val ACTION_STOP_LOCATION_SERVICE = "stopLocationService"
    var isLocationUpdatesActive = false

    private var mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            Log.i("isLocationUpdatesActive", isLocationUpdatesActive.toString())
            if (isLocationUpdatesActive) {
                if (p0 !== null) {
                    super.onLocationResult(p0)
                }
                if (p0 !== null && p0.lastLocation !== null) {
                    val latitude = p0.lastLocation.latitude
                    val longitude = p0.lastLocation.longitude
                    Log.v("LOCATION_UPDATE", "$latitude, $longitude")
                }
            }
        }
    }

    inner class LocalBinder : Binder() {
        fun getServiceInstance(): LocationService {
            return this@LocationService
        }
    }

    private val binder = LocalBinder()

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
//        throw UnsupportedOperationException("Not yet implemented")
        return binder
    }

    private fun startLocationService() {
        isLocationUpdatesActive = true
        val channelId = "location_notification_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.casual_life_3d_cardboard_boxes)
            .setContentTitle("Quicker")
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentText("의뢰인에게 위치를 전송중입니다...")
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
            interval = 4000
            fastestInterval = 2000
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


    private fun stopLocationService() {
        isLocationUpdatesActive = false
        Log.i("LocationService", "stopLocationService")
        LocationServices.getFusedLocationProviderClient(this)
            .removeLocationUpdates(mLocationCallback)
//        mLocationCallback = null
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("LocationService", "onStartCommand")
        if (intent.action != null) {
            when (intent.action) {
                "1" -> startLocationService()
                "2" -> stopLocationService()
            }
        }
        return START_STICKY_COMPATIBILITY
    }

}