package com.goheydot.smokefree.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goheydot.smokefree.R

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
        tabPhone.setOnClickListener {
            switchTab(true)
        }

        tabWechat.setOnClickListener {
            switchTab(false)
        }

        btnSendCode.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            if (validatePhone(phone)) {
                sendVerificationCode(phone)
            }
        }

        btnLogin.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val code = etCode.text.toString().trim()

            if (validatePhone(phone) && validateCode(code)) {
                getSharedPreferences("smokefree", MODE_PRIVATE).edit()
                    .putBoolean("is_logged_in", true)
                    .putString("phone", phone)
                    .apply()
                Toast.makeText(this, getString(R.string.btn_login), Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
        }

        btnWechatLogin.setOnClickListener {
            getSharedPreferences("smokefree", MODE_PRIVATE).edit()
                .putBoolean("is_logged_in", true)
                .putString("phone", "")
                .putString("wechat_name", getString(R.string.wechat_login))
                .apply()
            Toast.makeText(this, getString(R.string.btn_wechat_login), Toast.LENGTH_SHORT).show()
            navigateToMain()
        }

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

            findViewById<android.widget.LinearLayout>(R.id.form_phone).visibility = android.view.View.VISIBLE
            findViewById<android.widget.LinearLayout>(R.id.form_wechat).visibility = android.view.View.GONE
        } else {
            tabWechat.setBackgroundResource(R.drawable.bg_card)
            tabWechat.setTextColor(getColor(R.color.pink_600))
            tabWechat.setTypeface(null, android.graphics.Typeface.BOLD)

            tabPhone.setBackgroundResource(0)
            tabPhone.setTextColor(getColor(R.color.gray_500))
            tabPhone.setTypeface(null, android.graphics.Typeface.NORMAL)

            findViewById<android.widget.LinearLayout>(R.id.form_phone).visibility = android.view.View.GONE
            findViewById<android.widget.LinearLayout>(R.id.form_wechat).visibility = android.view.View.VISIBLE
        }
    }

    private fun validatePhone(phone: String): Boolean {
        if (phone.isEmpty()) {
            Toast.makeText(this, getString(R.string.hint_phone), Toast.LENGTH_SHORT).show()
            return false
        }
        if (!phone.matches(Regex("^1[3-9]\\d{9}$"))) {
            Toast.makeText(this, getString(R.string.hint_phone), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun validateCode(code: String): Boolean {
        if (code.isEmpty()) {
            Toast.makeText(this, getString(R.string.hint_code), Toast.LENGTH_SHORT).show()
            return false
        }
        if (code.length < 4) {
            Toast.makeText(this, getString(R.string.hint_code), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun sendVerificationCode(phone: String) {
        Toast.makeText(this, getString(R.string.btn_send_code), Toast.LENGTH_SHORT).show()

        btnSendCode.isEnabled = false
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                btnSendCode.text = "${millisUntilFinished / 1000}s"
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
