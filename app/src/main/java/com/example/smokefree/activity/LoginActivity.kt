package com.example.smokefree.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smokefree.R

class LoginActivity : AppCompatActivity() {

    private lateinit var etPhone: EditText
    private lateinit var etCode: EditText
    private lateinit var btnSendCode: TextView
    private lateinit var btnLogin: Button
    private lateinit var btnWechatLogin: Button
    private lateinit var tvSkipLogin: TextView
    private lateinit var tabPhone: TextView
    private lateinit var tabWechat: TextView

    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etPhone = findViewById(R.id.et_phone)
        etCode = findViewById(R.id.et_code)
        btnSendCode = findViewById(R.id.btn_send_code)
        btnLogin = findViewById(R.id.btn_login)
        btnWechatLogin = findViewById(R.id.btn_wechat_login)
        tvSkipLogin = findViewById(R.id.tv_skip_login)
        tabPhone = findViewById(R.id.tab_phone)
        tabWechat = findViewById(R.id.tab_wechat)
    }

    private fun setupListeners() {
        // Tab switching
        tabPhone.setOnClickListener {
            switchTab(true)
        }

        tabWechat.setOnClickListener {
            switchTab(false)
        }

        // Send verification code
        btnSendCode.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            if (validatePhone(phone)) {
                sendVerificationCode(phone)
            }
        }

        // Phone login
        btnLogin.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val code = etCode.text.toString().trim()
            
            if (validatePhone(phone) && validateCode(code)) {
                // Simulate login success
                Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
        }

        // Wechat login
        btnWechatLogin.setOnClickListener {
            Toast.makeText(this, "微信登录功能开发中", Toast.LENGTH_SHORT).show()
            // In real app, integrate WeChat SDK here
        }

        // Skip login
        tvSkipLogin.setOnClickListener {
            navigateToMain()
        }
    }

    private fun switchTab(isPhone: Boolean) {
        if (isPhone) {
            tabPhone.setBackgroundResource(R.drawable.bg_card)
            tabPhone.setTextColor(getColor(R.color.pink_600))
            tabPhone.setTypeface(null, android.graphics.Typeface.BOLD)
            
            tabWechat.setBackgroundResource(0)
            tabWechat.setTextColor(getColor(R.color.gray_500))
            tabWechat.setTypeface(null, android.graphics.Typeface.NORMAL)
            
            findViewById<android.widget.ScrollView>(R.id.form_phone).visibility = android.view.View.VISIBLE
            findViewById<android.widget.ScrollView>(R.id.form_wechat).visibility = android.view.View.GONE
        } else {
            tabWechat.setBackgroundResource(R.drawable.bg_card)
            tabWechat.setTextColor(getColor(R.color.pink_600))
            tabWechat.setTypeface(null, android.graphics.Typeface.BOLD)
            
            tabPhone.setBackgroundResource(0)
            tabPhone.setTextColor(getColor(R.color.gray_500))
            tabPhone.setTypeface(null, android.graphics.Typeface.NORMAL)
            
            findViewById<android.widget.ScrollView>(R.id.form_phone).visibility = android.view.View.GONE
            findViewById<android.widget.ScrollView>(R.id.form_wechat).visibility = android.view.View.VISIBLE
        }
    }

    private fun validatePhone(phone: String): Boolean {
        if (phone.isEmpty()) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!phone.matches(Regex("^1[3-9]\\d{9}$"))) {
            Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun validateCode(code: String): Boolean {
        if (code.isEmpty()) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show()
            return false
        }
        if (code.length < 4) {
            Toast.makeText(this, "请输入正确的验证码", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun sendVerificationCode(phone: String) {
        // Simulate sending code
        Toast.makeText(this, "验证码已发送（模拟：123456）", Toast.LENGTH_SHORT).show()
        
        // Start countdown
        btnSendCode.isEnabled = false
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                btnSendCode.text = "${millisUntilFinished / 1000}s后重发"
            }

            override fun onFinish() {
                btnSendCode.isEnabled = true
                btnSendCode.text = getString(R.string.btn_send_code)
            }
        }.start()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
