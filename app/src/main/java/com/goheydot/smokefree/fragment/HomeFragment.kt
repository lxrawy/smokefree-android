package com.goheydot.smokefree.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.goheydot.smokefree.R
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var tvDays: TextView
    private lateinit var tvMotivation: TextView
    private lateinit var tvTodaySmoked: TextView
    private lateinit var tvTodayCost: TextView
    private lateinit var tvTotalSaved: TextView
    private lateinit var tvHealthScore: TextView
    private lateinit var btnNoSmoke: TextView
    private lateinit var btnSmoked: TextView

    private lateinit var pbRecovery1: ProgressBar
    private lateinit var pbRecovery2: ProgressBar
    private lateinit var pbRecovery3: ProgressBar
    private lateinit var pbRecovery4: ProgressBar
    private lateinit var pbRecovery5: ProgressBar
    private lateinit var tvRecoveryTime1: TextView
    private lateinit var tvRecoveryTime2: TextView
    private lateinit var tvRecoveryTime3: TextView
    private lateinit var tvRecoveryTime4: TextView
    private lateinit var tvRecoveryTime5: TextView

    private val quoteResIds = intArrayOf(
        R.string.quote_1, R.string.quote_2, R.string.quote_3, R.string.quote_4, R.string.quote_5
    )

    private var quoteIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private val quoteRunnable = object : Runnable {
        override fun run() {
            quoteIndex = (quoteIndex + 1) % quoteResIds.size
            tvMotivation.text = getString(quoteResIds[quoteIndex])
            handler.postDelayed(this, 8000)
        }
    }

    private val statsRunnable = object : Runnable {
        override fun run() {
            updateStats()
            handler.postDelayed(this, 5000L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners()
        updateStats()
        startQuoteRotation()
        handler.post(statsRunnable)
    }

    private fun initViews(view: View) {
        tvDays = view.findViewById(R.id.tv_days)
        tvMotivation = view.findViewById(R.id.tv_motivation)
        tvTodaySmoked = view.findViewById(R.id.tv_today_smoked)
        tvTodayCost = view.findViewById(R.id.tv_today_cost)
        tvTotalSaved = view.findViewById(R.id.tv_total_saved)
        tvHealthScore = view.findViewById(R.id.tv_health_score)
        btnNoSmoke = view.findViewById(R.id.btn_no_smoke)
        btnSmoked = view.findViewById(R.id.btn_smoked)

        pbRecovery1 = view.findViewById(R.id.pb_recovery_1)
        pbRecovery2 = view.findViewById(R.id.pb_recovery_2)
        pbRecovery3 = view.findViewById(R.id.pb_recovery_3)
        pbRecovery4 = view.findViewById(R.id.pb_recovery_4)
        pbRecovery5 = view.findViewById(R.id.pb_recovery_5)
        tvRecoveryTime1 = view.findViewById(R.id.tv_recovery_time_1)
        tvRecoveryTime2 = view.findViewById(R.id.tv_recovery_time_2)
        tvRecoveryTime3 = view.findViewById(R.id.tv_recovery_time_3)
        tvRecoveryTime4 = view.findViewById(R.id.tv_recovery_time_4)
        tvRecoveryTime5 = view.findViewById(R.id.tv_recovery_time_5)
    }

    private fun setupListeners() {
        btnNoSmoke.setOnClickListener {
            showEncouragement()
            saveTodayRecord(0)
        }

        btnSmoked.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("smokefree", 0)
            val count = prefs.getInt("today_smoked", 0) + 1
            val lifeLostMinutes = count * 20
            Toast.makeText(
                requireContext(),
                getString(R.string.toast_smoked_warning, count, lifeLostMinutes),
                Toast.LENGTH_LONG
            ).show()
            saveTodayRecord(count)
        }
    }

    private fun updateStats() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val quitStartDate = prefs.getLong("quit_start_date", 0)
        val currency = getString(R.string.currency_symbol)

        if (quitStartDate > 0) {
            val now = System.currentTimeMillis()
            val elapsedMs = now - quitStartDate
            val days = (elapsedMs / (1000 * 60 * 60 * 24)).toInt()
            tvDays.text = days.toString()

            val dailyCigs = prefs.getInt("daily_cigs", 20)
            val packPrice = prefs.getInt("pack_price", 25)
            val pricePerCig = packPrice / 20.0

            val totalHoursDouble = elapsedMs / (1000.0 * 60.0 * 60.0)
            val cigsPerHour = dailyCigs.toDouble() / 24.0
            val cigsAvoided = totalHoursDouble * cigsPerHour

            val totalSmokedRecorded = prefs.getInt("total_smoked_all_time", 0).toDouble()
            val netCigsAvoided = (cigsAvoided - totalSmokedRecorded).coerceAtLeast(0.0)

            val todaySmoked = prefs.getInt("today_smoked", 0)
            tvTodaySmoked.text = todaySmoked.toString()
            val todayCost = todaySmoked * pricePerCig
            tvTodayCost.text = if (todayCost == todayCost.toLong().toDouble())
                "$currency${todayCost.toLong()}" else "$currency${"%.1f".format(todayCost)}"

            val moneySaved = netCigsAvoided * pricePerCig
            tvTotalSaved.text = "$currency${"%.2f".format(moneySaved)}"

            val totalLifeMinutes = netCigsAvoided * 20.0
            val healthScore = ((totalLifeMinutes / (30.0 * 24 * 60)) * 100).toInt().coerceIn(0, 100)
            tvHealthScore.text = "$healthScore%"
        }

        updateRecoveryProgress(quitStartDate)
    }

    private fun saveTodayRecord(count: Int) {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val editor = prefs.edit()
        editor.putInt("today_smoked", count)

        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        editor.putInt("smoke_$todayKey", count)

        val prevSmoked = prefs.getInt("today_smoked_prev", 0)
        val increment = count - prevSmoked
        if (increment > 0) {
            val totalSoFar = prefs.getInt("total_smoked_all_time", 0)
            editor.putInt("total_smoked_all_time", totalSoFar + increment)
        }
        editor.putInt("today_smoked_prev", count)

        editor.apply()
        updateStats()
    }

    data class RecoveryMilestone(
        val label: String,
        val targetMinutes: Long,
        val progressBar: ProgressBar,
        val labelView: TextView
    )

    private fun updateRecoveryProgress(quitStartDate: Long) {
        if (quitStartDate <= 0) return

        val elapsedMs = System.currentTimeMillis() - quitStartDate
        val elapsedMin = elapsedMs / (1000L * 60)

        val milestones = listOf(
            RecoveryMilestone(getString(R.string.milestone_20min), 20L, pbRecovery1, tvRecoveryTime1),
            RecoveryMilestone(getString(R.string.milestone_12h), 720L, pbRecovery2, tvRecoveryTime2),
            RecoveryMilestone(getString(R.string.milestone_3weeks), 30240L, pbRecovery3, tvRecoveryTime3),
            RecoveryMilestone(getString(R.string.milestone_9months), 388800L, pbRecovery4, tvRecoveryTime4),
            RecoveryMilestone(getString(R.string.milestone_1year), 525600L, pbRecovery5, tvRecoveryTime5),
        )

        for ((label, targetMin, progressBar, tvLabel) in milestones) {
            val progress = ((elapsedMin.toDouble() / targetMin.toDouble()) * 100).toInt().coerceIn(0, 100)
            progressBar.progress = progress

            if (progress >= 100) {
                tvLabel.text = "✅ $label"
                tvLabel.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            } else {
                tvLabel.text = label
                tvLabel.setTextColor(resources.getColor(R.color.pink_600))
            }
        }
    }

    private fun showEncouragement() {
        val titleResIds = intArrayOf(
            R.string.encourage_title_1, R.string.encourage_title_2,
            R.string.encourage_title_3, R.string.encourage_title_4, R.string.encourage_title_5
        )
        val subResIds = intArrayOf(
            R.string.encourage_sub_1, R.string.encourage_sub_2,
            R.string.encourage_sub_3, R.string.encourage_sub_4, R.string.encourage_sub_5
        )
        val idx = (Math.random() * titleResIds.size).toInt()
        val text = getString(titleResIds[idx])
        val sub = getString(subResIds[idx])
        Toast.makeText(requireContext(), "$text\n$sub", Toast.LENGTH_LONG).show()
    }

    private fun startQuoteRotation() {
        handler.postDelayed(quoteRunnable, 8000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(quoteRunnable)
        handler.removeCallbacks(statsRunnable)
    }
}
