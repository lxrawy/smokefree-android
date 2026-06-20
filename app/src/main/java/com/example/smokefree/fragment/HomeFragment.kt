package com.example.smokefree.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smokefree.R
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

    // Timer views
    private lateinit var tvTimerDisplay: TextView
    private lateinit var tvTimerSubtitle: TextView
    private lateinit var btnStartTimer: Button
    private lateinit var timerCard: View

    private val quotes = listOf(
        "「种一棵树最好的时间是十年前，其次是现在。」",
        "「每一次拒绝香烟，都是给未来自己的一份礼物。」",
        "「你比香烟更强大，相信自己！」",
        "「坚持一天，就是胜利；坚持一生，就是奇迹。」",
        "「今天的坚持，是明天健康的基石。」"
    )

    private var quoteIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private val quoteRunnable = object : Runnable {
        override fun run() {
            quoteIndex = (quoteIndex + 1) % quotes.size
            tvMotivation.text = quotes[quoteIndex]
            handler.postDelayed(this, 8000)
        }
    }

    // Real-time timer: ticks every second
    private val timerRunnable = object : Runnable {
        override fun run() {
            updateTimerDisplay()
            handler.postDelayed(this, 1000)
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
        setupTimer()
        startQuoteRotation()
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

        // Timer views
        tvTimerDisplay = view.findViewById(R.id.tv_timer_display)
        tvTimerSubtitle = view.findViewById(R.id.tv_timer_subtitle)
        btnStartTimer = view.findViewById(R.id.btn_start_timer)
        timerCard = view.findViewById(R.id.timer_card)
    }

    private fun setupListeners() {
        btnNoSmoke.setOnClickListener {
            showEncouragement()
            saveTodayRecord(0)
        }

        btnSmoked.setOnClickListener {
            Toast.makeText(requireContext(), "记录了吸烟数量，继续加油！", Toast.LENGTH_SHORT).show()
            // In real app, show dialog to input count
            saveTodayRecord(1)
        }
    }

    private fun updateStats() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val quitStartDate = prefs.getLong("quit_start_date", 0)
        
        if (quitStartDate > 0) {
            val days = ((System.currentTimeMillis() - quitStartDate) / (1000 * 60 * 60 * 24)).toInt()
            tvDays.text = days.toString()
            
            val dailyCigs = prefs.getInt("daily_cigs", 10)
            val packPrice = prefs.getInt("pack_price", 25)
            val pricePerCig = packPrice / 20.0
            
            val totalSaved = (days * dailyCigs * pricePerCig).toInt()
            tvTotalSaved.text = "¥${totalSaved}"
            
            val healthScore = ((days / 365.0) * 100).toInt().coerceAtMost(100)
            tvHealthScore.text = "$healthScore%"
            
            val todaySmoked = prefs.getInt("today_smoked", 0)
            tvTodaySmoked.text = todaySmoked.toString()
            tvTodayCost.text = "¥${(todaySmoked * pricePerCig).toInt()}"
        }
    }

    private fun saveTodayRecord(count: Int) {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        prefs.edit().putInt("today_smoked", count).apply()
        updateStats()
    }

    private fun showEncouragement() {
        val encouragements = listOf(
            "🎉 太棒了！你一根都没抽！" to "每一次拒绝，都是对健康的投资",
            "💪 坚持就是胜利！你做得很棒！" to "身体正在悄悄变好",
            "🌟 零吸烟，完美的记录！" to "为自己骄傲吧",
            "🏆 你是自律的冠军！" to "继续这张完美的成绩单",
            "💚 你的肺正在感谢你！" to "每一次呼吸都更轻松"
        )
        
        val (text, sub) = encouragements.random()
        Toast.makeText(requireContext(), "$text\n$sub", Toast.LENGTH_LONG).show()
    }

    private fun startQuoteRotation() {
        handler.postDelayed(quoteRunnable, 8000)
    }

    // ==================== Timer Logic ====================

    private fun setupTimer() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val quitStartDate = prefs.getLong("quit_start_date", 0)

        if (quitStartDate > 0) {
            // Already started - show running state
            btnStartTimer.visibility = View.GONE
            tvTimerSubtitle.text = getString(R.string.timer_running_subtitle)
            updateTimerDisplay()
            handler.post(timerRunnable)
        } else {
            // Not started yet - show start button
            btnStartTimer.visibility = View.VISIBLE
            tvTimerSubtitle.text = getString(R.string.timer_start_prompt)
            tvTimerDisplay.text = "0小時 0分 0秒"
        }

        btnStartTimer.setOnClickListener {
            startQuitTimer(prefs)
        }
    }

    private fun startQuitTimer(prefs: android.content.SharedPreferences) {
        val now = System.currentTimeMillis()
        prefs.edit()
            .putLong("quit_start_date", now)
            .apply()

        // Update UI to running state
        btnStartTimer.visibility = View.GONE
        tvTimerSubtitle.text = getString(R.string.timer_running_subtitle)

        // Start ticking
        handler.post(timerRunnable)

        // Also refresh day counter stats
        updateStats()

        Toast.makeText(requireContext(), "🎉 恭喜！戒烟计时已开始，加油！", Toast.LENGTH_SHORT).show()
    }

    private fun updateTimerDisplay() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val quitStartDate = prefs.getLong("quit_start_date", 0)

        if (quitStartDate <= 0) return

        val elapsedMs = System.currentTimeMillis() - quitStartDate
        val totalSeconds = (elapsedMs / 1000).toInt()

        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        tvTimerDisplay.text = getString(R.string.timer_format, hours, minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(quoteRunnable)
        handler.removeCallbacks(timerRunnable)
    }
}
