package com.example.smokefree.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.smokefree.R

class UserProfileActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserDesc: TextView
    private lateinit var layoutBindWechat: LinearLayout
    private lateinit var layoutBindPhone: LinearLayout
    private lateinit var layoutBindEmail: LinearLayout
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_profile)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "账户详情"

        initViews()
        updateUI()
        setupListeners()
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tv_profile_name)
        tvUserDesc = findViewById(R.id.tv_profile_desc)
        layoutBindWechat = findViewById(R.id.layout_bind_wechat)
        layoutBindPhone = findViewById(R.id.layout_bind_phone)
        layoutBindEmail = findViewById(R.id.layout_bind_email)
        btnLogout = findViewById(R.id.btn_profile_logout)
    }

    private fun updateUI() {
        val prefs = getSharedPreferences("smokefree", 0)
        val phone = prefs.getString("phone", "") ?: ""
        val wechatName = prefs.getString("wechat_name", "") ?: ""
        val email = prefs.getString("email", "") ?: ""

        if (phone.isNotEmpty()) {
            // 手机号登录 → 显示绑定微信和绑定邮箱
            tvUserName.text = phone.substring(0, 3) + "****" + phone.substring(7)
            tvUserDesc.text = "手机号登录"
            layoutBindPhone.visibility = View.GONE   // 已绑定手机号，隐藏
            layoutBindWechat.visibility = View.VISIBLE
            layoutBindEmail.visibility = View.VISIBLE
        } else if (wechatName.isNotEmpty()) {
            // 微信登录 → 显示绑定手机号和绑定邮箱
            tvUserName.text = wechatName
            tvUserDesc.text = "微信登录"
            layoutBindWechat.visibility = View.GONE    // 已绑定微信，隐藏
            layoutBindPhone.visibility = View.VISIBLE
            layoutBindEmail.visibility = View.VISIBLE
        } else {
            finish()
            return
        }

        // 邮箱已绑定则隐藏绑定邮箱选项
        if (email.isNotEmpty()) {
            layoutBindEmail.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        // 绑定微信
        layoutBindWechat.setOnClickListener {
            val prefs = getSharedPreferences("smokefree", 0)
            prefs.edit()
                .putString("wechat_name", "微信用户")
                .apply()
            Toast.makeText(this, "✅ 微信绑定成功", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        // 绑定手机号（模拟：直接标记已绑定）
        layoutBindPhone.setOnClickListener {
            val prefs = getSharedPreferences("smokefree", 0)
            prefs.edit()
                .putString("phone", "13800001111")
                .apply()
            Toast.makeText(this, "✅ 手机号绑定成功", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        // 绑定邮箱（模拟）
        layoutBindEmail.setOnClickListener {
            val prefs = getSharedPreferences("smokefree", 0)
            prefs.edit()
                .putString("email", "user@example.com")
                .apply()
            Toast.makeText(this, "✅ 邮箱绑定成功", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        // 退出登录
        btnLogout.setOnClickListener {
            getSharedPreferences("smokefree", 0).edit()
                .putBoolean("is_logged_in", false)
                .putString("phone", "")
                .putString("wechat_name", "")
                .apply()
            Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // 从绑定页面返回时刷新UI（模拟已绑定后隐藏对应项）
        updateUI()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
