package com.goheydot.smokefree.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
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
        supportActionBar?.title = getString(R.string.feedback_title)

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
                Toast.makeText(this, getString(R.string.toast_enter_title), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (content.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_enter_content), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val emailBody = buildString {
                appendLine(getString(R.string.feedback_email_body_title))
                appendLine()
                appendLine(getString(R.string.feedback_email_body_title_label))
                appendLine(title)
                appendLine()
                appendLine(getString(R.string.feedback_email_body_content_label))
                appendLine(content)
                appendLine()
                if (contact.isNotEmpty()) {
                    appendLine(getString(R.string.feedback_email_body_contact_label, contact))
                    appendLine()
                }
                appendLine("---")
                appendLine(getString(R.string.feedback_email_device, android.os.Build.VERSION.RELEASE))
                appendLine(getString(R.string.feedback_email_version))
                appendLine(getString(R.string.feedback_email_time,
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())))
            }

            val supportEmail = getString(R.string.support_email)
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_email_subject, title))
                putExtra(Intent.EXTRA_TEXT, emailBody)
            }

            try {
                startActivity(emailIntent)
                Toast.makeText(this, getString(R.string.toast_opening_email), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_email_subject, title))
                    putExtra(Intent.EXTRA_TEXT, emailBody)
                }
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_feedback)))
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
