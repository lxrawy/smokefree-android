package com.example.smokefree.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.smokefree.R
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
        supportActionBar?.title = "数据导出"

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
        // 选择开始日期
        tvStartDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendarStart.set(year, month, day)
                // 开始日期不能晚于结束日期
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

        // 选择结束日期
        tvEndDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendarEnd.set(year, month, day)
                // 结束日期不能早于开始日期
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

        // 导出按钮
        btnExport.setOnClickListener {
            exportData()
        }
    }

    private fun updateRangeInfo() {
        val diffMillis = calendarEnd.timeInMillis - calendarStart.timeInMillis
        val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt() + 1

        if (diffDays > 30) {
            tvDateRange.text = "已选 $diffDays 天（最多30天）"
            tvDateRange.setTextColor(resources.getColor(R.color.red_500, null))
            btnExport.isEnabled = false
            btnExport.alpha = 0.5f
        } else if (diffDays < 1) {
            tvDateRange.text = "请选择有效的日期范围"
            tvDateRange.setTextColor(resources.getColor(R.color.red_500, null))
            btnExport.isEnabled = false
            btnExport.alpha = 0.5f
        } else {
            tvDateRange.text = "已选 $diffDays 天数据"
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

        // 构建导出文本
        val sb = StringBuilder()
        sb.appendLine("=== 戒烟助手 - 数据导出报告 ===")
        sb.appendLine("导出时间：${dateFormat.format(System.currentTimeMillis())}")
        sb.appendLine("导出范围：${tvStartDate.text} ~ ${tvEndDate.text}")
        sb.appendLine("----------------------------------------")
        sb.appendLine()

        // 吸烟历史
        if (dailyCigs > 0) {
            sb.appendLine("【吸烟历史信息】")
            sb.appendLine("日均吸烟量：$dailyCigs 支/天")
            sb.appendLine("每包价格：¥$packPrice")
            sb.appendLine("吸烟年限：$yearsSmoking 年")

            val totalCigs = dailyCigs * 365 * yearsSmoking
            val totalMoney = (totalCigs / 20.0 * packPrice).toInt()
            sb.appendLine("历史总吸烟量：${totalCigs} 支")
            sb.appendLine("历史累计花费：¥$totalMoney 元")
            sb.appendLine()

            // 挽回生命（20分钟/支）
            val savedMinutes = totalCigs * 20
            val savedHours = savedMinutes / 60
            val savedDays = savedHours / 24
            sb.appendLine("挽回生命时长：约 ${savedDays} 天 ${savedHours % 24} 小时")
            sb.appendLine()
        }

        // 戒烟进度
        if (quitStartDate > 0) {
            val diffMs = System.currentTimeMillis() - quitStartDate
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            val diffHours = (diffMs / (1000 * 60 * 60)) % 24
            val unsmokedCigs = diffDays * dailyCigs + (diffHours.toFloat() / 24f * dailyCigs).toInt()
            val savedMoney = (unsmokedCigs / 20.0 * packPrice).toInt()
            val lifeSavedMinutes = unsmokedCigs * 20

            sb.appendLine("【戒烟进度统计】")
            sb.appendLine("戒烟开始日期：${dateFormat.format(quitStartDate)}")
            sb.appendLine("已坚持天数：${diffDays}天 ${diffHours}小时")
            sb.appendLine("未吸香烟数：约 $unsmokedCigs 支")
            sb.appendLine("已节省金额：¥$savedMoney 元")
            sb.appendLine("挽回生命：约 ${lifeSavedMinutes / 1440} 天 ${(lifeSavedMinutes / 60) % 24} 小时")
        } else {
            sb.appendLine("【戒烟进度】尚未启动戒烟计划")
        }

        sb.appendLine()
        sb.appendLine("========================================")
        sb.appendLine("感谢使用戒烟助手，祝您早日戒烟成功！🚭")

        val reportText = sb.toString()

        // 通过分享方式发送
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "戒烟助手-数据导出报告")
            putExtra(Intent.EXTRA_TEXT, reportText)
        }
        startActivity(Intent.createChooser(shareIntent, "分享/保存戒烟报告"))
        Toast.makeText(this, "✅ 数据已生成，请选择保存或分享", Toast.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
