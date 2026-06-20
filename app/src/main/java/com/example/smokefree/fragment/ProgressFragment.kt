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
            // Not started yet — show zeros
            tvProgressDays.text = "第0天"
            tvProgressPercent.text = "0.0%"
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
        val pricePerCig = packPrice / 20.0                     // 单价(元)
        val yearsSmoking = prefs.getInt("years_smoking", 10)  // 吸烟年数

        // --- 核心常量 ---
        val LIFE_LOSS_PER_CIG_HOURS = 1.2f                    // 每支烟损失1.2小时生命

        // --- 时间计算 ---
        val elapsedMs = now - quitStartDate
        val totalSeconds = (elapsedMs / 1000).toInt()
        val totalMinutes = totalSeconds / 60
        val totalHours = totalMinutes.toDouble()               // 总戒烟小时数(含小数)
        val totalDaysInt = totalMinutes / 60 / 24              // 整天数

        val displayHours = (totalHours).toInt()
        val displayMins = totalMinutes % 60
        val displaySecs = totalSeconds % 60

        // =============================================
        // ① 未吸烟時間 — 直接显示总时长
        // =============================================
        tvSmokeFreeTime.text = "%d小時 %02d分 %02d秒".format(displayHours, displayMins, displaySecs)

        // =============================================
        // ② 圆环进度 = 未吸烟总小时 ÷ 24小时目标 × 100%
        //    超过24小时进入下一天，显示当日进度
        // =============================================
        val dayNumber = totalDaysInt + 1                        // 第几天(从1开始)
        val todayProgressPct = ((totalHours % 24.0) / 24.0 * 100.0).coerceIn(0.0, 99.9)

        tvProgressDays.text = "第${dayNumber}天"
        tvProgressPercent.text = "%.1f%%".format(todayProgressPct)
        tvProgressTarget.text = "24小時"

        when {
            totalDaysInt >= 365 -> tvProgressStatus.text = "你已成功戒菸一年！你是傳奇！"
            totalDaysInt >= 30 -> tvProgressStatus.text = "堅持了一個月！身體正在恢復"
            totalDaysInt >= 7 -> tvProgressStatus.text = "一周了！你做得很棒！"
            totalDaysInt >= 3 -> tvProgressStatus.text = "三天了，最難的階段已過去！"
            totalDaysInt >= 1 -> tvProgressStatus.text = "第二天！繼續保持！"
            else -> tvProgressStatus.text = "第一天！現在我掌控了！"
        }

        // =============================================
        // ③ 避免的香烟数量 & 省下的錢
        //    逻辑：每过完完整一天 = 避免 dailyCigs 支
        //          当天按时间比例计算 + 扣除实际吸烟数
        // =============================================
        // 获取用户记录的实际吸烟总数（从每日打卡累计）
        val totalSmokedRecorded = prefs.getInt("total_smoked_all_time", 0)

        // 完整天数应避免的支数
        val cigsFromFullDays = totalDaysInt * dailyCigs
        // 今天按时间比例应避免的支数
        val todayFraction = (totalHours % 24.0) / 24.0
        val cigsTodayFraction = (todayFraction * dailyCigs).toInt()
        // 总避免 = 应避免 - 实际吸了
        val cigsAvoidedTotal = (cigsFromFullDays + cigsTodayFraction - totalSmokedRecorded).coerceAtLeast(0)

        // 省下的钱
        val moneySaved = cigsAvoidedTotal * pricePerCig
        tvMoneySaved.text = "¥ %.2f".format(moneySaved)
        tvCigarettesAvoided.text = "%.1f".format(cigsAvoidedTotal.toFloat())

        // =============================================
        // ④ 挽回的生命
        //    逻辑：每避免1支烟 = 挽回 1.2 小时生命
        //          没打卡/没记录吸烟 = 视为完全避免
        // =============================================
        val lifeRegainedTotalHours = cigsAvoidedTotal * LIFE_LOSS_PER_CIG_HOURS
        val lifeRegainedH = lifeRegainedTotalHours.toInt()
        val lifeRegainedM = ((lifeRegainedTotalHours - lifeRegainedH) * 60).toInt()
        val lifeRegainedS = (((lifeRegainedTotalHours * 60) % 60)).toInt()

        if (lifeRegainedH > 0) {
            tvLifeRegained.text = "%d小時 %02d分 %02d秒".format(lifeRegainedH, lifeRegainedM, lifeRegainedS)
        } else {
            tvLifeRegained.text = "%d分 %02d秒".format(lifeRegainedM.coerceAtLeast(0), lifeRegainedS.coerceAtLeast(0))
        }

        // =============================================
        // ⑤ 吸烟历史统计（戒烟之前的数据）
        // =============================================
        val histTotalCigs = yearsSmoking * 365 * dailyCigs
        tvHistCigarettes.text = "%,d".format(histTotalCigs)

        val histTotalCost = histTotalCigs * pricePerCig
        tvHistMoney.text = "¥ %.2f".format(histTotalCost)

        // 历史损失的生命（也用1.2小时/支来算）
        val histLifeLostHours = (histTotalCigs * LIFE_LOSS_PER_CIG_HOURS).toLong()
        val lostYears = (histLifeLostHours / (365 * 24)).toInt()
        val lostMonths = ((histLifeLostHours % (365 * 24)) / (30 * 24)).toInt()
        val lostDays = ((histLifeLostHours % (30 * 24)) / 24).toInt()
        tvHistLifeLost.text = "%d年 %d月 %d天".format(lostYears, lostMonths, lostDays)
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
