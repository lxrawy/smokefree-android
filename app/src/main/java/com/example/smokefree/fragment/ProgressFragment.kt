package com.example.smokefree.fragment

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.smokefree.R

class ProgressFragment : Fragment() {

    // Circle progress views
    private lateinit var tvProgressDays: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvProgressTarget: TextView
    private lateinit var tvProgressStatus: TextView

    // Stats grid views (4 items)
    private lateinit var tvSmokeFreeTime: TextView      // 未吸菸時間
    private lateinit var tvMoneySaved: TextView          // 省下的錢
    private lateinit var tvLifeRegained: TextView        // 挽回的生命
    private lateinit var tvCigarettesAvoided: TextView   // 未吸的香菸

    // Smoking history stats
    private lateinit var tvHistCigarettes: TextView      // 已吸的香菸
    private lateinit var tvHistMoney: TextView           // 浪費的錢
    private lateinit var tvHistLifeLost: TextView        // 損失的生命

    private val handler = Handler(Looper.getMainLooper())

    // Timer runnable: updates every second
    private val timerRunnable = object : Runnable {
        override fun run() {
            updateAllStats()
            handler.postDelayed(this, 1000)
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
        updateAllStats()
        animateCircleProgress()

        // Start real-time timer
        handler.post(timerRunnable)
    }

    private fun initViews(view: View) {
        // Circle progress
        tvProgressDays = view.findViewById(R.id.tv_progress_days)
        tvProgressPercent = view.findViewById(R.id.tv_progress_percent)
        tvProgressTarget = view.findViewById(R.id.tv_progress_target)
        tvProgressStatus = view.findViewById(R.id.tv_progress_status)

        // 4 stat cards
        tvSmokeFreeTime = view.findViewById(R.id.tv_smoke_free_time)
        tvMoneySaved = view.findViewById(R.id.tv_money_saved)
        tvLifeRegained = view.findViewById(R.id.tv_life_regained)
        tvCigarettesAvoided = view.findViewById(R.id.tv_cigarettes_avoided)

        // History section
        tvHistCigarettes = view.findViewById(R.id.tv_hist_cigarettes)
        tvHistMoney = view.findViewById(R.id.tv_hist_money)
        tvHistLifeLost = view.findViewById(R.id.tv_hist_life_lost)
    }

    private fun updateAllStats() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val quitStartDate = prefs.getLong("quit_start_date", 0)
        val now = System.currentTimeMillis()

        if (quitStartDate <= 0) {
            // Not started yet
            tvProgressDays.text = "第0天"
            tvProgressPercent.text = "0%"
            tvProgressTarget.text = "24小時"
            tvProgressStatus.text = "點擊首頁開始戒菸"
            tvSmokeFreeTime.text = "0小時 00分 00秒"
            tvMoneySaved.text = "¥ 0.00"
            tvLifeRegained.text = "0小時 00分 00秒"
            tvCigarettesAvoided.text = "0"
            tvHistCigarettes.text = "0"
            tvHistMoney.text = "¥ 0"
            tvHistLifeLost.text = "0天 0小時"
            return
        }

        // --- User settings ---
        val dailyCigs = prefs.getInt("daily_cigs", 20)       // 每天吸烟量
        val packPrice = prefs.getInt("pack_price", 25)        // 每包价格
        val pricePerCig = packPrice / 20.0                     // 单价
        val yearsSmoking = prefs.getInt("years_smoking", 10)  // 吸烟年数

        // --- Time calculations ---
        val elapsedMs = now - quitStartDate
        val totalSeconds = (elapsedMs / 1000).toInt()
        val totalMinutes = totalSeconds / 60
        val totalHours = totalMinutes / 60
        val totalDays = totalHours / 24

        val hours = totalHours % 24
        val minutes = totalMinutes % 60
        val seconds = totalSeconds % 60

        // --- Smoke-free time ---
        tvSmokeFreeTime.text = "%d小時 %02d分 %02d秒".format(totalHours, minutes, seconds)

        // --- Money saved ---
        val cigsAvoided = totalDays * dailyCigs + (totalHours * dailyCigs / 24.0).toInt()
        val moneySaved = cigsAvoided * pricePerCig
        tvMoneySaved.text = "¥ %.2f".format(moneySaved)

        // --- Life regained ---
        // Assumption: each cigarette shortens life by ~11 minutes
        val minutesRegained = cigsAvoided * 11
        val lifeHours = minutesRegained / 60
        val lifeMins = minutesRegained % 60
        tvLifeRegained.text = "%d小時 %02d分 %02d秒".format(lifeHours, lifeMins, seconds)

        // --- Cigarettes avoided ---
        tvCigarettesAvoided.text = "%.1f".format(cigsAvoided.toFloat())

        // --- Circle progress ---
        tvProgressDays.text = "第${totalDays + 1}天"
        val dayProgress = if (totalDays == 0) {
            (totalHours.toFloat() / 24f * 100f)
        } else {
            (hours.toFloat() / 24f * 100f)
        }
        val displayPercent = dayProgress.coerceIn(0f, 99.9f)
        tvProgressPercent.text = "%.1f%%".format(displayPercent)
        tvProgressTarget.text = "24小時"

        // Status message
        when {
            totalDays >= 365 -> tvProgressStatus.text = "你已成功戒菸一年！你是傳奇！"
            totalDays >= 30 -> tvProgressStatus.text = "堅持了一個月！身體正在恢復"
            totalDays >= 7 -> tvProgressStatus.text = "一周了！你做得很棒！"
            totalDays >= 3 -> tvProgressStatus.text = "三天了，最難的階段已過去！"
            totalDays >= 1 -> tvProgressStatus.text = "第二天！繼續保持！"
            else -> tvProgressStatus.text = "第一天！現在的我掌控了！"
        }

        // --- Smoking history (before quitting) ---
        val histTotalCigs = yearsSmoking * 365 * dailyCigs
        tvHistCigarettes.text = "%,d".format(histTotalCigs)

        val histTotalCost = histTotalCigs * pricePerCig
        tvHistMoney.text = "¥ %.2f".format(histTotalCost)

        val histLifeLostDays = histTotalCigs * 11 / (24 * 60)
        val histLostYears = histLifeLostDays / 365
        val histLostMonths = (histLifeLostDays % 365) / 30
        val histLostRemainDays = histLifeLostDays % 30
        tvHistLifeLost.text = "%d年 %d月 %d天".format(histLostYears, histLostMonths, histLostRemainDays)
    }

    private fun animateCircleProgress() {
        // Animate the percentage text on first load
        val animator = ValueAnimator.ofFloat(0f, 80.4f)
        animator.duration = 1500
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            // Optional: can add custom circle drawable animation here later
        }
        animator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timerRunnable)
    }
}
