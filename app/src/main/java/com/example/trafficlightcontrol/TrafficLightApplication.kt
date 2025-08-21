package com.example.trafficlightcontrol

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class TrafficLightApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Khởi tạo Firebase
        FirebaseApp.initializeApp(this)

        // Bật tính năng lưu trữ offline cho Firebase Realtime Database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}