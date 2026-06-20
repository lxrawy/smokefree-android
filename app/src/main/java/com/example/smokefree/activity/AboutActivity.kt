package com.example.smokefree.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.smokefree.R

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_about)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "关于"

        // 评分按钮（跳转应用商店）
        findViewById<android.widget.LinearLayout>(R.id.layout_rate_app)?.setOnClickListener {
            Toast.makeText(this, "感谢您的支持！", Toast.LENGTH_SHORT).show()
        }

        // 隐私政策
        findViewById<android.widget.LinearLayout>(R.id.layout_privacy)?.setOnClickListener {
            Toast.makeText(this, "隐私政策页面开发中", Toast.LENGTH_SHORT).show()
        }

        // 用户协议
        findViewById<android.widget.LinearLayout>(R.id.layout_agreement)?.setOnClickListener {
            Toast.makeText(this, "用户协议页面开发中", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
