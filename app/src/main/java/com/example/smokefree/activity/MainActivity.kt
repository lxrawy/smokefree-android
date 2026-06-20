package com.example.smokefree.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.smokefree.R
import com.example.smokefree.fragment.*
import com.example.smokefree.receiver.ReminderReceiver
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 创建通知渠道
        ReminderReceiver.createNotificationChannel(this)

        initViews()
        setupBottomNavigation()
        
        // Load home fragment by default
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_history -> HistoryFragment()
                R.id.nav_progress -> ProgressFragment()
                R.id.nav_track -> TrackFragment()
                R.id.nav_me -> MeFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
