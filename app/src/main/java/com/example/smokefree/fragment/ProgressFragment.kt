package com.example.smokefree.fragment

import android.animation.ValueAnimator
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
import com.example.smokefree.R

class ProgressFragment : Fragment() {

    // Circle progress views
    private lateinit var tvProgressDays: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvProgressTarget: TextView
    private lateinit var tvProgressStatus: TextView
    private lateinit var btnDaySelector: TextView

    // Stats grid views (4 items)
    private lateinit var tvSmokeFreeTime: TextView      // 未吸菸時間
    private lateinit var tvMoneySaved: TextView          // 省下的錢
    private lateinit var tvLifeRegained: TextView        // 挽回的生命
    private lateinit var tvCigarettesAvoided: TextView   // 未吸的香菸

    // Smoking history stats
    private lateinit var tvHistCigarettes: TextView      // 已吸的香菸
    private lateinit var tvHistMoney: TextView           // 浪費的錢
    private lateinit var tvHistLifeLost: TextView        // 損失的生命

    /** 当前查看的天数: 0 = 今天(实时), 1 = 第1天(完整), 2 = 第2天 ... */
    private var selectedDayIndex: Int = 0   // 0 means "current / latest"

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
        setupDaySelector(view)
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

        // Day selector button
        btnDaySelector = view.findViewById(R.id.btn_day_selector)

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

    /**
     * 点击 ⋮ 弹出天数选择菜单
     */
    private fun setupDaySelector(anchorView: View) {
        btnDaySelector.setOnClickListener { v ->
            val prefs = requireContext().getSharedPreferences("smokefree", 0)
            val quitStartDate = prefs.getLong("quit_start_date", 0)

            if (quitStartDate <= 0) return@setOnClickListener

            val now = System.currentTimeMillis()
            val totalDaysPassed = ((now - quitStartDate) / (1000 * 60 * 60 * 24)).toInt() + 1

            val popup = PopupMenu(requireContext(), v, Gravity.END)
            val menu = popup.menu

            // "今天" option (default, index 0)
            menu.add(0, 0, 0, "📅 今天（實時）")

            // Past days: 第1天, 第2天, ..., up to totalDaysPassed
            for (d in 1..totalDaysPassed.coerceAtMost(365)) {
                menu.add(0, d, d, "第${d}天")
            }

            popup.setOnMenuItemClickListener { item: MenuItem ->
                selectedDayIndex = item.itemId
                updateAllStats()          // 立即刷新显示
                true
            }
            popup.show()
        }
    }

    private fun updateAllStats() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val quitStartDate = prefs.getLong("quit_start_date", 0)
        val now = System.currentTimeMillis()

        if (quitStartDate <= 0) {
            showZeroState()
            return
        }

        // --- User settings ---
        val dailyCigs = prefs.getInt("daily_cigs", 20)
        val packPrice = prefs.getInt("pack_price", 25)
        val pricePerCig = packPrice / 20.0
        val yearsSmoking = prefs.getInt("years_smoking", 10)

        val LIFE_LOSS_PER_CIG_HOURS = 1.2f

        // --- 总时间跨度 ---
        val elapsedMs = now - quitStartDate
        val totalSeconds = (elapsedMs / 1000).toInt()
        val totalMinutes = totalSeconds / 60
        val totalHours = totalMinutes.toDouble()
        val totalDaysInt = totalMinutes / 60 / 24   // 完整天数(0-based)

        // =============================================
        // 根据选中的天数计算数据
        // selectedDayIndex == 0 → 显示今天（实时的，可能不满24h）
        // selectedDayIndex >= 1 → 显示第N天的完整24h数据
        // =============================================

        if (selectedDayIndex == 0) {
            // ---- 今天：实时数据 ----
            showTodayStats(totalHours, totalMinutes, totalSeconds, totalDaysInt,
                dailyCigs, pricePerCig, LIFE_LOSS_PER_CIG_HOURS, prefs,
                yearsSmoking)
        } else {
            // ---- 历史某一天：按完整24小时计算 ----
            val targetDay = selectedDayIndex           // 用户选的"第N天"(1-based)
            val targetDayZeroBased = targetDay - 1     // 转为0-based

            if (targetDayZeroBased < totalDaysInt) {
                // 已过去的完整天：满24小时
                showPastDayStats(targetDay, dailyCigs, pricePerCig,
                    LIFE_LOSS_PER_CIG_HOURS, prefs, yearsSmoking)
            } else if (targetDayZeroBased == totalDaysInt) {
                // 就是今天（但用户手动选了今天的标签）
                showTodayStats(totalHours, totalMinutes, totalSeconds, totalDaysInt,
                    dailyCigs, pricePerCig, LIFE_LOSS_PER_CIG_HOURS, prefs,
                    yearsSmoking)
            } else {
                // 还没到这一天，不可能，回退到今天
                selectedDayIndex = 0
                showTodayStats(totalHours, totalMinutes, totalSeconds, totalDaysInt,
                    dailyCigs, pricePerCig, LIFE_LOSS_PER_CIG_HOURS, prefs,
                    yearsSmoking)
            }
        }
    }

    // ==================== 今天（实时） ====================
    private fun showTodayStats(
        totalHours: Double,
        totalMinutes: Int,
        totalSeconds: Int,
        totalDaysInt: Int,
        dailyCigs: Int,
        pricePerCig: Double,
        lifeLossPerCig: Float,
        prefs: android.content.SharedPreferences,
        yearsSmoking: Int
    ) {
        val displayHours = totalHours.toInt()
        val displayMins = totalMinutes % 60
        val displaySecs = totalSeconds % 60

        // ① 未吸烟時間
        tvSmokeFreeTime.text = "%d小時 %02d分 %02d秒".format(displayHours, displayMins, displaySecs)

        // ② 圆环进度
        val dayNumber = totalDaysInt + 1
        val todayPct = ((totalHours % 24.0) / 24.0 * 100.0).coerceIn(0.0, 99.9)

        tvProgressDays.text = "第${dayNumber}天"
        tvProgressPercent.text = "%.1f%%".format(todayPct)
        tvProgressTarget.text = "24小時"

        when {
            totalDaysInt >= 365 -> tvProgressStatus.text = "你已成功戒菸一年！你是傳奇！"
            totalDaysInt >= 30 -> tvProgressStatus.text = "堅持了一個月！身體正在恢復"
            totalDaysInt >= 7 -> tvProgressStatus.text = "一周了！你做得很棒！"
            totalDaysInt >= 3 -> tvProgressStatus.text = "三天了，最難的階段已過去！"
            totalDaysInt >= 1 -> tvProgressStatus.text = "第二天！繼續保持！"
            else -> tvProgressStatus.text = "第一天！現在我掌控了！"
        }

        // ③ 避免香烟 & 省钱
        val totalSmokedRecorded = prefs.getInt("total_smoked_all_time", 0)
        val cigsFromFullDays = totalDaysInt * dailyCigs
        val todayFraction = (totalHours % 24.0) / 24.0
        val cigsTodayFrac = (todayFraction * dailyCigs).toInt()
        val cigsAvoided = (cigsFromFullDays + cigsTodayFrac - totalSmokedRecorded).coerceAtLeast(0)

        tvMoneySaved.text = "¥ %.2f".format(cigsAvoided * pricePerCig)
        tvCigarettesAvoided.text = "%.1f".format(cigsAvoided.toFloat())

        // ④ 挽回生命
        val lifeHrs = cigsAvoided * lifeLossPerCig
        formatLifeTime(lifeHrs)

        // ⑤ 吸烟历史（不变）
        showHistorySection(yearsSmoking, dailyCigs, pricePerCig, lifeLossPerCig)
    }

    // ==================== 历史某一天（完整24h） ====================
    private fun showPastDayStats(
        dayNumber: Int,       // 1-based: 第几天
        dailyCigs: Int,
        pricePerCig: Double,
        lifeLossPerCig: Float,
        prefs: android.content.SharedPreferences,
        yearsSmoking: Int
    ) {
        // 历史天：默认视为完全避免（用户没记录=没抽）
        val cigsAvoidedThisDay = dailyCigs   // 整天避免

        // ① 未吸烟时间 = 24小时整
        tvSmokeFreeTime.text = "24小時 00分 00秒"

        // ② 圆环 = 100%
        tvProgressDays.text = "第${dayNumber}天"
        tvProgressPercent.text = "100.0%"
        tvProgressTarget.text = "24小時"

        // 历史天的鼓励语
        when (dayNumber) {
            1 -> tvProgressStatus.text = "第一天成功！偉大的開始！"
            in 2..6 -> tvProgressStatus.text = "第${dayNumber}天完成！堅持住！"
            7 -> tvProgressStatus.text = "一周全勤！太強了！"
            14, 21, 30 -> tvProgressStatus.text = "第${dayNumber}天里程碑！🎉"
            100, 200, 300 -> tvProgressStatus.text = "第${dayNumber}天！你是戰神！"
            365 -> tvProgressStatus.text = "一年了！！！傳奇人物！！！"
            else -> tvProgressStatus.text = "第${dayNumber}天完美通關 ✅"
        }

        // 到这一天为止的累计数据（含之前所有天）
        val totalSmokedRecorded = prefs.getInt("total_smoked_all_time", 0)
        val cigsAvoidedTotal = (dayNumber * dailyCigs - totalSmokedRecorded).coerceAtLeast(0)

        // ③ 省下的錢
        tvMoneySaved.text = "¥ %.2f".format(cigsAvoidedTotal * pricePerCig)

        // ④ 未吸的香菸（累计）
        tvCigarettesAvoided.text = "%.1f".format(cigsAvoidedTotal.toFloat())

        // ⑤ 挽回的生命（累计）
        val lifeHrs = cigsAvoidedTotal * lifeLossPerCig
        formatLifeTime(lifeHrs)

        // ⑥ 吸烟历史
        showHistorySection(yearsSmoking, dailyCigs, pricePerCig, lifeLossPerCig)
    }

    private fun formatLifeTime(totalHours: Float) {
        val h = totalHours.toInt()
        val m = ((totalHours - h) * 60).toInt()
        val s = (((totalHours * 60) % 60)).toInt()

        if (h > 0) {
            tvLifeRegained.text = "%d小時 %02d分 %02d秒".format(h, m, s.coerceAtLeast(0))
        } else if (m > 0) {
            tvLifeRegained.text = "%d分 %02d秒".format(m.coerceAtLeast(0), s.coerceAtLeast(0))
        } else {
            tvLifeRegained.text = "%d秒".format(s.coerceAtLeast(0))
        }
    }

    private fun showHistorySection(
        yearsSmoking: Int,
        dailyCigs: Int,
        pricePerCig: Double,
        lifeLossPerCig: Float
    ) {
        val histTotalCigs = yearsSmoking * 365 * dailyCigs
        tvHistCigarettes.text = "%,d".format(histTotalCigs)

        val histCost = histTotalCigs * pricePerCig
        tvHistMoney.text = "¥ %.2f".format(histCost)

        val lostHrs = (histTotalCigs * lifeLossPerCig).toLong()
        val y = (lostHrs / (365 * 24)).toInt()
        val mo = ((lostHrs % (365 * 24)) / (30 * 24)).toInt()
        val d = ((lostHrs % (30 * 24)) / 24).toInt()
        tvHistLifeLost.text = "%d年 %d月 %d天".format(y, mo, d)
    }

    private fun showZeroState() {
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
    }

    private fun animateCircleProgress() {
        val animator = ValueAnimator.ofFloat(0f, 80.4f)
        animator.duration = 1500
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { /* placeholder */ }
        animator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timerRunnable)
    }
}
