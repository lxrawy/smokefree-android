package com.goheydot.smokefree.fragment

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.goheydot.smokefree.R
import com.goheydot.smokefree.widget.ArcProgressView

class ProgressFragment : Fragment() {

    companion object {
        const val LIFE_LOSS_MINUTES_PER_CIG = 20
        const val MINUTES_PER_HOUR = 60
        const val HOURS_PER_DAY = 24
        const val DAYS_PER_YEAR = 365
        const val DAYS_PER_MONTH_AVG = 30
    }

    private lateinit var tvProgressDays: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvProgressTarget: TextView
    private lateinit var tvProgressStatus: TextView
    private lateinit var btnDaySelector: TextView
    private lateinit var arcProgress: ArcProgressView

    private lateinit var tvSmokeFreeTime: TextView
    private lateinit var tvMoneySaved: TextView
    private lateinit var tvLifeRegained: TextView
    private lateinit var tvCigarettesAvoided: TextView

    private lateinit var tvHistCigarettes: TextView
    private lateinit var tvHistMoney: TextView
    private lateinit var tvHistLifeLost: TextView

    private lateinit var btnResetTimer: TextView

    private var selectedDayIndex: Int = 0

    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            updateAllStats()
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupDaySelector(view)
        updateAllStats()
        animateCircleProgress()

        handler.post(timerRunnable)
    }

    private fun initViews(view: View) {
        tvProgressDays = view.findViewById(R.id.tv_progress_days)
        tvProgressPercent = view.findViewById(R.id.tv_progress_percent)
        tvProgressTarget = view.findViewById(R.id.tv_progress_target)
        tvProgressStatus = view.findViewById(R.id.tv_progress_status)

        btnDaySelector = view.findViewById(R.id.btn_day_selector)
        arcProgress = view.findViewById(R.id.arc_progress)

        tvSmokeFreeTime = view.findViewById(R.id.tv_smoke_free_time)
        tvMoneySaved = view.findViewById(R.id.tv_money_saved)
        tvLifeRegained = view.findViewById(R.id.tv_life_regained)
        tvCigarettesAvoided = view.findViewById(R.id.tv_cigarettes_avoided)

        tvHistCigarettes = view.findViewById(R.id.tv_hist_cigarettes)
        tvHistMoney = view.findViewById(R.id.tv_hist_money)
        tvHistLifeLost = view.findViewById(R.id.tv_hist_life_lost)

        btnResetTimer = view.findViewById(R.id.btn_reset_timer)
        setupResetButton()
    }

    private fun setupDaySelector(anchorView: View) {
        btnDaySelector.setOnClickListener { v ->
            val prefs = requireContext().getSharedPreferences("smokefree", 0)
            val quitStartDate = prefs.getLong("quit_start_date", 0)
            if (quitStartDate <= 0) return@setOnClickListener

            val now = System.currentTimeMillis()
            val totalDaysPassed = ((now - quitStartDate) / (1000L * 60 * 60 * 24)).toInt() + 1

            val popup = PopupMenu(requireContext(), v, Gravity.END)
            val menu = popup.menu

            menu.add(0, 0, 0, getString(R.string.menu_today_realtime))
            for (d in 1..totalDaysPassed.coerceAtMost(365)) {
                menu.add(0, d, d, getString(R.string.format_day_n, d))
            }

            popup.setOnMenuItemClickListener { item: MenuItem ->
                selectedDayIndex = item.itemId
                updateAllStats()
                true
            }
            popup.show()
        }
    }

    private fun setupResetButton() {
        btnResetTimer.setOnClickListener {
            showResetConfirmationDialog()
        }
    }

    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.reset_confirm_title))
            .setMessage(getString(R.string.reset_confirm_message))
            .setPositiveButton(getString(R.string.reset_confirm_button)) { _, _ ->
                doResetTimer()
            }
            .setNegativeButton(getString(R.string.reset_cancel_button), null)
            .show()
    }

    private fun doResetTimer() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val now = System.currentTimeMillis()

        prefs.edit()
            .putLong("quit_start_date", now)
            .putInt("today_smoked", 0)
            .putInt("today_smoked_prev", 0)
            .putInt("total_smoked_all_time", 0)
            .remove("last_checkin_date")
            .apply()

        selectedDayIndex = 0
        updateAllStats()

        android.widget.Toast.makeText(
            requireContext(),
            getString(R.string.toast_timer_reset),
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateAllStats() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val quitStartDate = prefs.getLong("quit_start_date", 0)
        val now = System.currentTimeMillis()

        if (quitStartDate <= 0) {
            showZeroState()
            return
        }

        val dailyCigs = prefs.getInt("daily_cigs", 20)
        val packPrice = prefs.getInt("pack_price", 25)
        val pricePerCig = packPrice / 20.0
        val yearsSmoking = prefs.getInt("years_smoking", 10)

        val elapsedMs = now - quitStartDate
        val totalSeconds = (elapsedMs / 1000).toInt()
        val totalMinutes = totalSeconds / 60
        val totalHoursDouble = elapsedMs / (1000.0 * 60.0 * 60.0)
        val fullDaysElapsed = totalMinutes / 60 / 24

        if (selectedDayIndex == 0) {
            showTodayRealtimeData(
                elapsedMs, totalSeconds, totalHoursDouble,
                fullDaysElapsed, dailyCigs, pricePerCig,
                yearsSmoking, prefs
            )
        } else {
            showPastDayFullData(
                selectedDayIndex, dailyCigs, pricePerCig,
                yearsSmoking, fullDaysElapsed, prefs
            )
        }

        showHistoryLossSection(dailyCigs, pricePerCig, yearsSmoking)
    }

    private fun showTodayRealtimeData(
        elapsedMs: Long,
        totalSeconds: Int,
        totalHours: Double,
        fullDaysElapsed: Int,
        dailyCigs: Int,
        pricePerCig: Double,
        yearsSmoking: Int,
        prefs: android.content.SharedPreferences
    ) {
        val displayHrs = totalHours.toInt()
        val displayMins = (totalSeconds / 60) % 60
        val displaySecs = totalSeconds % 60
        tvSmokeFreeTime.text = getString(R.string.format_time_hms, displayHrs, displayMins, displaySecs)

        val todayFractionalHour = totalHours % 24.0
        val dayNumber = fullDaysElapsed + 1
        val circlePercent = (todayFractionalHour / 24.0 * 100.0).coerceIn(0.0, 99.9)

        tvProgressDays.text = getString(R.string.format_day_n, dayNumber)
        tvProgressPercent.text = "%.1f%%".format(circlePercent)
        tvProgressTarget.text = getString(R.string.format_24h)
        tvProgressStatus.text = getTodayMotivation(fullDaysElapsed)

        arcProgress.progress = (circlePercent * 100).toInt()

        val cigsPerHour = dailyCigs.toDouble() / HOURS_PER_DAY
        val cigsAvoided = totalHours * cigsPerHour

        val totalSmokedRecorded = prefs.getInt("total_smoked_all_time", 0).toDouble()
        val netCigsAvoided = (cigsAvoided - totalSmokedRecorded).coerceAtLeast(0.0)
        tvCigarettesAvoided.text = "%.2f".format(netCigsAvoided)

        val moneySaved = netCigsAvoided * pricePerCig
        val currency = getString(R.string.currency_symbol)
        tvMoneySaved.text = "$currency %.2f".format(moneySaved)

        formatLifeRegainedRealtime(netCigsAvoided)
    }

    private fun showPastDayFullData(
        targetDay: Int,
        dailyCigs: Int,
        pricePerCig: Double,
        yearsSmoking: Int,
        fullDaysElapsed: Int,
        prefs: android.content.SharedPreferences
    ) {
        val dayZeroBased = targetDay - 1

        if (dayZeroBased > fullDaysElapsed) {
            selectedDayIndex = 0
            updateAllStats()
            return
        }

        tvSmokeFreeTime.text = getString(R.string.format_time_hms, 24, 0, 0)

        tvProgressDays.text = getString(R.string.format_day_n, targetDay)
        tvProgressPercent.text = "100.0%"
        tvProgressTarget.text = getString(R.string.format_24h)
        tvProgressStatus.text = getPastDayMotivation(targetDay)
        arcProgress.progress = 10000

        val totalSmokedRecorded = prefs.getInt("total_smoked_all_time", 0).toDouble()
        val cumulativeAvoided = (targetDay * dailyCigs.toDouble() - totalSmokedRecorded)
            .coerceAtLeast(0.0)

        tvCigarettesAvoided.text = "%.1f".format(cumulativeAvoided)

        val currency = getString(R.string.currency_symbol)
        tvMoneySaved.text = "$currency %.2f".format(cumulativeAvoided * pricePerCig)

        formatLifeRegained(cumulativeAvoided)
    }

    private fun showHistoryLossSection(
        dailyCigs: Int,
        pricePerCig: Double,
        yearsSmoking: Int
    ) {
        val histTotalCigs = dailyCigs.toLong() * DAYS_PER_YEAR * yearsSmoking
        tvHistCigarettes.text = "%,d".format(histTotalCigs)

        val histCost = histTotalCigs * pricePerCig
        val currency = getString(R.string.currency_symbol)
        tvHistMoney.text = "$currency %.2f".format(histCost)

        val totalLostMinutes = histTotalCigs * LIFE_LOSS_MINUTES_PER_CIG
        val lostYears = totalLostMinutes / (DAYS_PER_YEAR * MINUTES_PER_HOUR * HOURS_PER_DAY)
        val lostMonths = (totalLostMinutes % (DAYS_PER_YEAR * MINUTES_PER_HOUR * HOURS_PER_DAY)) /
                (DAYS_PER_MONTH_AVG * MINUTES_PER_HOUR * HOURS_PER_DAY)
        val lostDays = (totalLostMinutes % (DAYS_PER_MONTH_AVG * MINUTES_PER_HOUR * HOURS_PER_DAY)) /
                (MINUTES_PER_HOUR * HOURS_PER_DAY)

        tvHistLifeLost.text = getString(R.string.format_life_ymd, lostYears, lostMonths, lostDays)
    }

    private fun formatLifeRegainedRealtime(cigsAvoided: Double) {
        val totalSecondsRegained = cigsAvoided * LIFE_LOSS_MINUTES_PER_CIG * MINUTES_PER_HOUR

        val hours = (totalSecondsRegained / 3600.0).toInt()
        val mins = ((totalSecondsRegained % 3600.0) / 60.0).toInt()
        val secs = (totalSecondsRegained % 60.0).toInt()

        tvLifeRegained.text = getString(R.string.format_time_hms, hours, mins, secs)
    }

    private fun formatLifeRegained(cigsAvoided: Double) {
        formatLifeRegainedRealtime(cigsAvoided)
    }

    private fun getTodayMotivation(fullDaysElapsed: Int): String = when {
        fullDaysElapsed >= 365 -> getString(R.string.motiv_today_year)
        fullDaysElapsed >= 30 -> getString(R.string.motiv_today_month)
        fullDaysElapsed >= 7 -> getString(R.string.motiv_today_week)
        fullDaysElapsed >= 3 -> getString(R.string.motiv_today_day3)
        fullDaysElapsed >= 1 -> getString(R.string.motiv_today_day2)
        else -> getString(R.string.motiv_today_default)
    }

    private fun getPastDayMotivation(dayNumber: Int): String = when (dayNumber) {
        1 -> getString(R.string.motiv_past_day1)
        7 -> getString(R.string.motiv_past_week)
        14, 21, 30 -> getString(R.string.motiv_past_milestone, dayNumber)
        50, 100, 200, 300 -> getString(R.string.motiv_past_warrior, dayNumber)
        365 -> getString(R.string.motiv_past_year)
        in 2..6 -> getString(R.string.motiv_past_normal, dayNumber)
        else -> getString(R.string.motiv_past_default, dayNumber)
    }

    private fun showZeroState() {
        val currency = getString(R.string.currency_symbol)
        tvProgressDays.text = getString(R.string.zero_day)
        tvProgressPercent.text = "0.0%"
        tvProgressTarget.text = getString(R.string.format_24h)
        tvProgressStatus.text = getString(R.string.zero_status)
        arcProgress.progress = 0
        tvSmokeFreeTime.text = getString(R.string.zero_time)
        tvMoneySaved.text = "$currency 0.00"
        tvLifeRegained.text = getString(R.string.zero_time)
        tvCigarettesAvoided.text = "0"
        tvHistCigarettes.text = "0"
        tvHistMoney.text = "$currency 0"
        tvHistLifeLost.text = getString(R.string.zero_life)
    }

    private fun animateCircleProgress() {
        val animator = ValueAnimator.ofFloat(0f, 80.4f)
        animator.duration = 1500
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { }
        animator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timerRunnable)
    }
}
