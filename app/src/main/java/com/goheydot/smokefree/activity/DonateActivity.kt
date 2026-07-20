package com.goheydot.smokefree.activity

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.goheydot.smokefree.R

class DonateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_donate)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.donate_title)

        // Show real QR if a PNG (BitmapDrawable) is provided as R.drawable.qr_donate,
        // otherwise show the placeholder text. Default placeholder is a transparent
        // shape drawable, so the type check is reliable.
        val ivQr = findViewById<ImageView>(R.id.iv_donate_qr)
        val tvPlaceholder = findViewById<TextView>(R.id.tv_qr_placeholder)
        val drawable = resources.getDrawable(R.drawable.qr_donate, null)
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            ivQr.setImageDrawable(drawable)
            ivQr.visibility = View.VISIBLE
            tvPlaceholder.visibility = View.GONE
        } else {
            ivQr.visibility = View.GONE
            tvPlaceholder.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.btn_donate_close).setOnClickListener {
            finish()
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
