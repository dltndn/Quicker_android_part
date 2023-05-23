package com.example.walletconnet_test

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import java.util.*

class BackgroundService: Service() {
    private var isServiceRunning = false

    // test s
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var handlerThread: HandlerThread
    private lateinit var backgroundHandler: Handler
    // test e

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isServiceRunning) {
            // test s
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            handlerThread = HandlerThread("BackgroundThread")
            handlerThread.start()
            backgroundHandler = Handler(handlerThread.looper)
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    Log.i("BackgroundService", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                }

                override fun onProviderDisabled(provider: String) {
                    // Provider (GPS 또는 네트워크)가 사용 불가능 상태로 변경될 때 호출됨
                }

                override fun onProviderEnabled(provider: String) {
                    // Provider (GPS 또는 네트워크)가 사용 가능 상태로 변경될 때 호출됨
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                    // Provider 상태가 변경될 때 호출됨
                    Log.i("Provider state", "Provider changed")
                }
            }
            // test e
            isServiceRunning = true
            startBackgroundTask()
        }
        return START_STICKY_COMPATIBILITY
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        locationManager.removeUpdates(locationListener)
        stopBackgroundTask()
    }

    private fun startBackgroundTask() {
        backgroundHandler.post{
            Thread {
                while (isServiceRunning) {
                    // 반복 실행할 함수
                    doTask()
                    try {
                        Thread.sleep(2000) // 2초마다 실행
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }.start()
        }
    }

    private fun stopBackgroundTask() {
        // 백그라운드 스레드 정리
        handlerThread.quit()
        handlerThread.join()
    }

    private fun doTask() {
        // 반복 실행할 작업을 수행
        requestLocationUpdates()
        Log.i("BackgroundService", "Task executed")
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        Log.i("BackgroundService", "requestLocationUpdates()")
        // 위치 업데이트 요청
        backgroundHandler.post{
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000, // 업데이트 주기 (1초)
                1f, // 최소 거리 변화 (1m)
                locationListener
            )
        }
        // GPS_PROVIDER가 작동하지 않을 때
        backgroundHandler.postDelayed({
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000, // 업데이트 주기 (1초)
                1f, // 최소 거리 변화 (1m)
                locationListener
            )
        }, 3000) // 5초 후에 NETWORK_PROVIDER로 변경
    }
}