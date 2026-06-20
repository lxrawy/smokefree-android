package com.example.smokefree.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            // 从 SharedPreferences 读取当天的吸烟记录（默认 0）
            val count = prefs.getInt("smoke_$dateKey", 0).toFloat()
            entries.add(BarEntry(i.toFloat(), count))
        }

        val dataSet = BarDataSet(entries, "吸烟数量")
        dataSet.color = resources.getColor(R.color.pink_400)
        dataSet.setDrawValues(true)
        dataSet.valueTextColor = resources.getColor(R.color.gray_600)
        dataSet.valueTextSize = 10f

        val data = BarData(dataSet)
        data.barWidth = 0.6f

        barChart.data = data
        barChart.invalidate() // Refresh chart
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
    }
}
