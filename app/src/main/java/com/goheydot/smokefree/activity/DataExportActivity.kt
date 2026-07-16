package com.goheydot.smokefree.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.goheydot.smokefree.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DataExportActivity : AppCompatActivity() {

    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var tvDateRange: TextView
    private lateinit var btnExport: Button

    private val calendarStart = Calendar.getInstance()
    private val calendarEnd = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_export)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_export)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.export_title)

        initViews()
        setupListeners()
        updateRangeInfo()
    }

    private fun initViews() {
        tvStartDate = findViewById(R.id.tv_start_date)
        tvEndDate = findViewById(R.id.tv_end_date)
        tvDateRange = findViewById(R.id.tv_date_range_info)
        btnExport = findViewById(R.id.btn_export_data)
    }

    private fun setupListeners() {
        tvStartDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendarStart.set(year, month, day)
                if (calendarStart.after(calendarEnd)) {
                    calendarEnd.time = calendarStart.time
                }
                tvStartDate.text = dateFormat.format(calendarStart.time)
                tvEndDate.text = dateFormat.format(calendarEnd.time)
                updateRangeInfo()
            },
            calendarStart.get(Calendar.YEAR),
            calendarStart.get(Calendar.MONTH),
            calendarStart.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        tvEndDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendarEnd.set(year, month, day)
                if (calendarEnd.before(calendarStart)) {
                    calendarStart.time = calendarEnd.time
                    tvStartDate.text = dateFormat.format(calendarStart.time)
                }
                tvEndDate.text = dateFormat.format(calendarEnd.time)
                updateRangeInfo()
            },
            calendarEnd.get(Calendar.YEAR),
            calendarEnd.get(Calendar.MONTH),
            calendarEnd.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnExport.setOnClickListener {
            exportData()
        }
    }

    private fun updateRangeInfo() {
        val diffMillis = calendarEnd.timeInMillis - calendarStart.timeInMillis
        val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt() + 1

        if (diffDays > 30) {
            tvDateRange.text = getString(R.string.format_days_over_limit, diffDays)
            tvDateRange.setTextColor(resources.getColor(R.color.red_500, null))
            btnExport.isEnabled = false
            btnExport.alpha = 0.5f
        } else if (diffDays < 1) {
            tvDateRange.text = getString(R.string.select_valid_range)
            tvDateRange.setTextColor(resources.getColor(R.color.red_500, null))
            btnExport.isEnabled = false
            btnExport.alpha = 0.5f
        } else {
            tvDateRange.text = getString(R.string.format_days_selected, diffDays)
            tvDateRange.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
            btnExport.isEnabled = true
            btnExport.alpha = 1f
        }
    }

    private fun exportData() {
        val prefs = getSharedPreferences("smokefree", 0)
        val dailyCigs = prefs.getInt("daily_cigs", 0)
        val packPrice = prefs.getInt("pack_price", 0)
        val yearsSmoking = prefs.getInt("years_smoking", 0)
        val quitStartDate = prefs.getLong("quit_start_date", 0)
        val currency = getString(R.string.currency_symbol)

        val sb = StringBuilder()
        sb.appendLine(getString(R.string.export_report_title))
        sb.appendLine(getString(R.string.export_report_time, dateFormat.format(System.currentTimeMillis())))
        sb.appendLine(getString(R.string.export_report_range, tvStartDate.text, tvEndDate.text))
        sb.appendLine("----------------------------------------")
        sb.appendLine()

        if (dailyCigs > 0) {
            sb.appendLine(getString(R.string.export_report_history))
            sb.appendLine(getString(R.string.export_report_daily, dailyCigs))
            sb.appendLine(getString(R.string.export_report_price, packPrice).replace("¥", currency))
            sb.appendLine(getString(R.string.export_report_years, yearsSmoking))

            val totalCigs = dailyCigs * 365 * yearsSmoking
            val totalMoney = (totalCigs / 20.0 * packPrice).toInt()
            sb.appendLine(getString(R.string.export_report_total_cigs, totalCigs))
            sb.appendLine(getString(R.string.export_report_total_money, totalMoney).replace("¥", currency))
            sb.appendLine()

            val savedMinutes = totalCigs * 20
            val savedHours = savedMinutes / 60
            val savedDays = savedHours / 24
            sb.appendLine(getString(R.string.export_report_life_saved, savedDays, savedHours % 24))
            sb.appendLine()
        }

        if (quitStartDate > 0) {
            val diffMs = System.currentTimeMillis() - quitStartDate
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            val diffHours = (diffMs / (1000 * 60 * 60)) % 24
            val unsmokedCigs = diffDays * dailyCigs + (diffHours.toFloat() / 24f * dailyCigs).toInt()
            val savedMoney = (unsmokedCigs / 20.0 * packPrice).toInt()
            val lifeSavedMinutes = unsmokedCigs * 20

            sb.appendLine(getString(R.string.export_report_progress))
            sb.appendLine(getString(R.string.export_report_quit_date, dateFormat.format(quitStartDate)))
            sb.appendLine(getString(R.string.export_report_quit_days, diffDays, diffHours))
            sb.appendLine(getString(R.string.export_report_unsmoked, unsmokedCigs))
            sb.appendLine(getString(R.string.export_report_saved_money, savedMoney).replace("¥", currency))
            sb.appendLine(getString(R.string.export_report_life_regained, lifeSavedMinutes / 1440, (lifeSavedMinutes / 60) % 24))
        } else {
            sb.appendLine(getString(R.string.export_report_not_started))
        }

        sb.appendLine()
        sb.appendLine("========================================")
        sb.appendLine(getString(R.string.export_report_footer))

        val reportText = sb.toString()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_share_title))
            putExtra(Intent.EXTRA_TEXT, reportText)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.export_share_chooser)))
        Toast.makeText(this, getString(R.string.toast_export_generated), Toast.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
