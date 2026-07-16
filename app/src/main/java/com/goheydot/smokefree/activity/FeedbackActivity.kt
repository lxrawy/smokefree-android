package com.goheydot.smokefree.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.goheydot.smokefree.R

class FeedbackActivity : AppCompatActivity() {

    private lateinit var etFeedbackTitle: EditText
    private lateinit var etFeedbackContent: EditText
    private lateinit var etFeedbackContact: EditText
    private lateinit var btnSendFeedback: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_feedback)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "帮助与反馈"

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etFeedbackTitle = findViewById(R.id.et_feedback_title)
        etFeedbackContent = findViewById(R.id.et_feedback_content)
        etFeedbackContact = findViewById(R.id.et_feedback_contact)
        btnSendFeedback = findViewById(R.id.btn_send_feedback)
    }

    private fun setupListeners() {
        btnSendFeedback.setOnClickListener {
            val title = etFeedbackTitle.text.toString().trim()
            val content = etFeedbackContent.text.toString().trim()
            val contact = etFeedbackContact.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "请输入反馈标题", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (content.isEmpty()) {
                Toast.makeText(this, "请输入反馈内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 构建邮件内容
            val emailBody = buildString {
                appendLine("=== 戒烟助手 - 用户反馈 ===")
                appendLine()
                appendLine("【反馈标题】")
                appendLine(title)
                appendLine()
                appendLine("【反馈内容】")
                appendLine(content)
                appendLine()
                if (contact.isNotEmpty()) {
                    appendLine("【联系方式】$contact")
                    appendLine()
                }
                appendLine("---")
                appendLine("设备信息：Android ${android.os.Build.VERSION.RELEASE}")
                appendLine("应用版本：v1.0.0")
                appendLine("反馈时间：${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
            }

            // 调起系统邮件客户端发送
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("smokefree@example.com"))
                putExtra(Intent.EXTRA_SUBJECT, "[戒烟助手] $title")
                putExtra(Intent.EXTRA_TEXT, emailBody)
            }

            try {
                startActivity(emailIntent)
                Toast.makeText(this, "正在打开邮件客户端...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // 如果没有邮件客户端，用分享方式代替
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "[戒烟助手] $title")
                    putExtra(Intent.EXTRA_TEXT, emailBody)
                }
                startActivity(Intent.createChooser(shareIntent, "分享反馈内容"))
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
