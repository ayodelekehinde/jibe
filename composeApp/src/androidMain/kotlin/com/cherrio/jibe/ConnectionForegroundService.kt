package com.cherrio.jibe

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.cherrio.jibe.di.Di
import com.cherrio.jibe.network.ConnectionEvent
import com.cherrio.jibe.network.ConnectionManager
import com.cherrio.jibe.network.Device
import kotlinx.coroutines.Job

class ConnectionForegroundService : Service() {

    private val manager: ConnectionManager = Di.get()
    private lateinit var job: Job
    private val devices = mutableListOf<Device>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startForegroundWithNotification()
        job = Di.launch {
            manager.events.collect {
                when (it.event) {
                    ConnectionEvent.Connected -> {
                        devices.add(it.device)
                        updateNotification("Connected to ${it.device.name}")
                    }
                    ConnectionEvent.Disconnected -> {
                        devices.removeIf { device -> it.device.ip == device.ip }
                        updateNotification("Disconnected")
                    }
                    ConnectionEvent.RetryFailed -> {
                        if (devices.isEmpty()) {
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            stopSelf()
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun onDestroy() {
        isRunning = false
        job.cancel()
        Di.close()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_CONNECT" -> {
                val host = intent.getStringExtra("host") ?: return START_NOT_STICKY
                val port = intent.getIntExtra("port", 8008)
                connectToDevice(host, port)
            }
        }
        return START_STICKY
    }

    private fun connectToDevice(host: String, port: Int) {
        manager.connectTo(host, port)
    }

    private fun startForegroundWithNotification() {
        val channelId = getString(R.string.notification_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "jibe app", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notif = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Jibe is running")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentIntent(pendingIntent())
            .setOngoing(true)
            .build()
        ServiceCompat.startForeground(
             this,
            1,
            notif,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            } else {
                0
            }
        )
    }
    private fun updateNotification(text: String) {
        val channelId = getString(R.string.notification_name)
        val notif = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Jibe is running")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentIntent(pendingIntent())
            .setOngoing(true)
            .build()
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .notify(1, notif)
    }

    private fun pendingIntent(): PendingIntent {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    companion object {
        var isRunning = false
    }

}
