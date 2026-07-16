package com.goheydot.smokefree.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.goheydot.smokefree.R
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
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setBackgroundColor(resources.getColor(android.R.color.transparent))

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(listOf(
            getString(R.string.weekday_mon), getString(R.string.weekday_tue),
            getString(R.string.weekday_wed), getString(R.string.weekday_thu),
            getString(R.string.weekday_fri), getString(R.string.weekday_sat),
            getString(R.string.weekday_sun)
        ))
        xAxis.textColor = resources.getColor(R.color.gray_400)
        xAxis.textSize = 10f

        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = resources.getColor(R.color.pink_100)
        leftAxis.textColor = resources.getColor(R.color.gray_400)
        leftAxis.textSize = 10f
        leftAxis.axisMinimum = 0f

        barChart.axisRight.isEnabled = false

        barChart.animateY(1000)
    }

    private fun updateChartData() {
        val entries = ArrayList<BarEntry>()
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(Date())
        val cal = Calendar.getInstance()

        cal.firstDayOfWeek = Calendar.MONDAY
        cal[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
        val mondayMs = cal.timeInMillis

        for (i in 0..6) {
            cal.timeInMillis = mondayMs
            cal.add(Calendar.DATE, i)
            val dateKey = sdf.format(cal.time)
            var count = prefs.getInt("smoke_$dateKey", -1)
            if (count < 0 && dateKey == todayStr) {
                count = prefs.getInt("today_smoked", 0)
            } else if (count < 0) {
                count = 0
            }
            entries.add(BarEntry(i.toFloat(), count.toFloat()))
        }

        val dataSet = BarDataSet(entries, getString(R.string.label_smoking_count))
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
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val dailyCigs = prefs.getInt("daily_cigs", 0)
        val yearsSmoking = prefs.getInt("years_smoking", 0)
        val packPrice = prefs.getInt("pack_price", 0)
        val currency = getString(R.string.currency_symbol)

        if (dailyCigs > 0 && yearsSmoking > 0 && packPrice > 0) {
            val totalCigs = dailyCigs * 365L * yearsSmoking
            val totalMoney = totalCigs * packPrice / 20.0
            val packEquiv = totalCigs / 20
            val coffeeEquiv = (totalMoney / 30).toInt()

            view?.findViewById<TextView>(R.id.tv_hist_daily)?.text = dailyCigs.toString()
            view?.findViewById<TextView>(R.id.tv_hist_years)?.text = yearsSmoking.toString()
            view?.findViewById<TextView>(R.id.tv_hist_total)?.text = "$currency${"%,.0f".format(totalMoney)}"

            view?.findViewById<TextView>(R.id.tv_hist_note)?.text =
                getString(R.string.format_history_note,
                    String.format("%,d", totalCigs),
                    String.format("%,d", packEquiv),
                    String.format("%,d", coffeeEquiv)
                )
        }

        updateChartData()
        updateDailyRecords()
    }

    private fun updateDailyRecords() {
        val container = view?.findViewById<LinearLayout>(R.id.daily_record_container) ?: return
        container.removeAllViews()

        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sdfDisplay = SimpleDateFormat("M/d", Locale.US)
        val todayStr = sdfKey.format(Date())
        val packPrice = prefs.getInt("pack_price", 25)
        val pricePerCig = if (packPrice > 0) packPrice / 20.0 else 1.25
        val currency = getString(R.string.currency_symbol)
        val unitCigs = getString(R.string.unit_cigs)

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, -i)
            val dateKey = sdfKey.format(cal.time)
            val dateDisplay = sdfDisplay.format(cal.time)

            var cigs = prefs.getInt("smoke_$dateKey", -1)
            if (cigs < 0 && dateKey == todayStr) {
                cigs = prefs.getInt("today_smoked", 0)
            } else if (cigs < 0) {
                cigs = 0
            }

            val costRaw = cigs * pricePerCig
            val costStr = if (costRaw == costRaw.toLong().toDouble()) "$currency${costRaw.toInt()}" else "$currency${"%.1f".format(costRaw)}"
            val status: String
            val statusColor: Int
            when {
                cigs == 0 -> {
                    status = getString(R.string.status_perfect)
                    statusColor = Color.parseColor("#16A34A")
                }
                cigs <= 3 -> {
                    status = getString(R.string.status_warning)
                    statusColor = Color.parseColor("#D97706")
                }
                else -> {
                    status = getString(R.string.status_over)
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

            row.addView(TextView(requireContext()).apply {
                text = dateDisplay
                textSize = 13f
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })

            row.addView(TextView(requireContext()).apply {
                text = "$cigs $unitCigs"
                textSize = 13f
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })

            row.addView(TextView(requireContext()).apply {
                text = costStr
                textSize = 13f
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })

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

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
