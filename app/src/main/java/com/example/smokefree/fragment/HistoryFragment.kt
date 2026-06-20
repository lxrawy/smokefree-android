package com.example.smokefree.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.smokefree.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        barChart = view.findViewById(R.id.bar_chart)
        setupChart()
        updateHistory()
    }

    private fun setupChart() {
        // Configure chart appearance
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setBackgroundColor(resources.getColor(android.R.color.transparent))
        
        // Configure X axis
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(listOf("一", "二", "三", "四", "五", "六", "日"))
        xAxis.textColor = resources.getColor(R.color.gray_400)
        xAxis.textSize = 10f
        
        // Configure Y axis
        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = resources.getColor(R.color.pink_100)
        leftAxis.textColor = resources.getColor(R.color.gray_400)
        leftAxis.textSize = 10f
        leftAxis.axisMinimum = 0f
        
        barChart.axisRight.isEnabled = false
        
        // Animation
        barChart.animateY(1000)
    }

    private fun updateChartData() {
        val entries = ArrayList<BarEntry>()
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(Date())
        val cal = Calendar.getInstance()

        // 计算本周一到本周日的日期
        cal.firstDayOfWeek = Calendar.MONDAY
        // 回退到本周一
        cal[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
        val mondayMs = cal.timeInMillis

        for (i in 0..6) {
            cal.timeInMillis = mondayMs
            cal.add(Calendar.DATE, i)
            val dateKey = sdf.format(cal.time)
            // 优先读取日期key；如果是今天且没有日期key，回退读取 today_smoked（兼容旧数据）
            var count = prefs.getInt("smoke_$dateKey", -1)
            if (count < 0 && dateKey == todayStr) {
                count = prefs.getInt("today_smoked", 0)
            } else if (count < 0) {
                count = 0
            }
            entries.add(BarEntry(i.toFloat(), count.toFloat()))
        }

        val dataSet = BarDataSet(entries, "吸烟数量")
        dataSet.color = resources.getColor(R.color.pink_400)
        dataSet.setDrawValues(true)
        dataSet.valueTextColor = resources.getColor(R.color.gray_600)
        dataSet.valueTextSize = 10f

        val data = BarData(dataSet)
        data.barWidth = 0.6f

        barChart.data = data
        barChart.invalidate()
    }

    private fun updateHistory() {
        // Keep existing history summary logic
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val dailyCigs = prefs.getInt("daily_cigs", 0)
        val yearsSmoking = prefs.getInt("years_smoking", 0)
        val packPrice = prefs.getInt("pack_price", 0)
        
        if (dailyCigs > 0 && yearsSmoking > 0) {
            val totalCigs = dailyCigs * 365 * yearsSmoking
            val totalMoney = (totalCigs / 20.0 * packPrice).toInt()
            val packEquiv = totalCigs / 20
            val coffeeEquiv = totalMoney / 30
            
            view?.findViewById<TextView>(R.id.tv_hist_daily)?.text = dailyCigs.toString()
            view?.findViewById<TextView>(R.id.tv_hist_years)?.text = yearsSmoking.toString()
            view?.findViewById<TextView>(R.id.tv_hist_total)?.text = "¥${totalMoney}"
            
            view?.findViewById<TextView>(R.id.tv_hist_note)?.text = 
                "💡 你已累计吸烟 ${totalCigs} 支，相当于 ${packEquiv} 包烟。\n这些钱可以买 ${coffeeEquiv} 杯咖啡 ☕"
        }
        
        // Update chart
        updateChartData()

        // Update daily record table
        updateDailyRecords()
    }

    /**
     * 动态生成每日记录表格行 — 从 SharedPreferences 读取最近7天的真实数据
     */
    private fun updateDailyRecords() {
        val container = view?.findViewById<LinearLayout>(R.id.daily_record_container) ?: return
        container.removeAllViews()

        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sdfDisplay = SimpleDateFormat("M/d", Locale.US)
        val todayStr = sdfKey.format(Date())
        val packPrice = prefs.getInt("pack_price", 25)
        val pricePerCig = if (packPrice > 0) packPrice / 20.0 else 1.25

        // 最近7天（今天往前倒推，包括今天）
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, -i)
            val dateKey = sdfKey.format(cal.time)
            val dateDisplay = sdfDisplay.format(cal.time)

            // 读取当天数据（兼容：优先日期key，今天回退today_smoked）
            var cigs = prefs.getInt("smoke_$dateKey", -1)
            if (cigs < 0 && dateKey == todayStr) {
                cigs = prefs.getInt("today_smoked", 0)
            } else if (cigs < 0) {
                cigs = 0
            }

            val cost = (cigs * pricePerCig).toInt()
            val status: String
            val statusColor: Int
            when {
                cigs == 0 -> {
                    status = "✅ 完美"
                    statusColor = Color.parseColor("#16A34A")
                }
                cigs <= 3 -> {
                    status = "⚠️ 注意"
                    statusColor = Color.parseColor("#D97706")
                }
                else -> {
                    status = "❌ 超标"
                    statusColor = Color.parseColor("#DC2626")
                }
            }

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 10.dp, 0, 10.dp)
                background = resources.getDrawable(R.color.pink_50, null)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 2.dp }
            }

            // 日期
            row.addView(TextView(requireContext()).apply {
                text = dateDisplay
                textSize = 13f
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })

            // 吸烟数
            row.addView(TextView(requireContext()).apply {
                text = "${cigs}支"
                textSize = 13f
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })

            // 花费
            row.addView(TextView(requireContext()).apply {
                text = "¥$cost"
                textSize = 13f
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })

            // 状态
            row.addView(TextView(requireContext()).apply {
                text = status
                textSize = 12f
                setTextColor(statusColor)
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })

            container.addView(row)
        }
    }

    /** dp 转 px 扩展 */
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
