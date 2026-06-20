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

/**
 * 进度页面 — 实时戒烟数据展示
 *
 * 计算标准（全球戒烟软件统一基准）:
 *   每支香烟折损寿命 = 20分钟 （英国伦敦大学《Addiction》期刊研究）
 *
 * 三大基础参数（用户在"我"页面填写）：
 *   1. 日均吸烟支数（默认20支/天）
 *   2. 单包价格 / 20 = 单支单价
 *   3. 吸烟总年限
 *
 * 五大模块：
 *   A. 未吸菸時間    → 戒烟开始时刻 → 当前时间，实时走秒
 *   B. 未吸的香菸    → 已过小时数 × (日均支数÷24)
 *   C. 省下的錢      → 未吸支数 × 单支价
 *   D. 挽回的生命    → 未吸支数 × 20分钟，转时分秒
 *   E. 过往总损耗    → 年限×365×日均支数，算总钱+总寿命损失
 */
class ProgressFragment : Fragment() {

    // ==================== 常量 ====================

    /** 科学标准：每支香烟折损预期寿命 20 分钟 */
    companion object {
        const val LIFE_LOSS_MINUTES_PER_CIG = 20
        const val MINUTES_PER_HOUR = 60
        const val HOURS_PER_DAY = 24
        const val DAYS_PER_YEAR = 365
        const val DAYS_PER_MONTH_AVG = 30
    }

    // ==================== 视图引用 ====================

    // 圆环进度
    private lateinit var tvProgressDays: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvProgressTarget: TextView
    private lateinit var tvProgressStatus: TextView
    private lateinit var btnDaySelector: TextView

    // 四宫格实时数据
    private lateinit var tvSmokeFreeTime: TextView      // 未吸菸時間
    private lateinit var tvMoneySaved: TextView          // 省下的錢
    private lateinit var tvLifeRegained: TextView        // 挽回的生命
    private lateinit var tvCigarettesAvoided: TextView   // 未吸的香菸

    // 底部历史损耗
    private lateinit var tvHistCigarettes: TextView      // 已吸的香菸
    private lateinit var tvHistMoney: TextView           // 浪費的錢
    private lateinit var tvHistLifeLost: TextView        // 損失的生命

    /** 当前查看的天数：0=今天(实时), 1=第1天, 2=第2天 ... */
    private var selectedDayIndex: Int = 0

    // 定时器
    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            updateAllStats()
            handler.postDelayed(this, 1000L)
        }
    }

    // ==================== 生命周期 ====================

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

        // 启动每秒定时刷新
        handler.post(timerRunnable)
    }

    // ==================== 初始化 ====================

    private fun initViews(view: View) {
        tvProgressDays = view.findViewById(R.id.tv_progress_days)
        tvProgressPercent = view.findViewById(R.id.tv_progress_percent)
        tvProgressTarget = view.findViewById(R.id.tv_progress_target)
        tvProgressStatus = view.findViewById(R.id.tv_progress_status)

        btnDaySelector = view.findViewById(R.id.btn_day_selector)

        tvSmokeFreeTime = view.findViewById(R.id.tv_smoke_free_time)
        tvMoneySaved = view.findViewById(R.id.tv_money_saved)
        tvLifeRegained = view.findViewById(R.id.tv_life_regained)
        tvCigarettesAvoided = view.findViewById(R.id.tv_cigarettes_avoided)

        tvHistCigarettes = view.findViewById(R.id.tv_hist_cigarettes)
        tvHistMoney = view.findViewById(R.id.tv_hist_money)
        tvHistLifeLost = view.findViewById(R.id.tv_hist_life_lost)
    }

    /**
     * 右上角 ⋮ 天数选择菜单
     */
    private fun setupDaySelector(anchorView: View) {
        btnDaySelector.setOnClickListener { v ->
            val prefs = requireContext().getSharedPreferences("smokefree", 0)
            val quitStartDate = prefs.getLong("quit_start_date", 0)
            if (quitStartDate <= 0) return@setOnClickListener

            val now = System.currentTimeMillis()
            val totalDaysPassed = ((now - quitStartDate) / (1000L * 60 * 60 * 24)).toInt() + 1

            val popup = PopupMenu(requireContext(), v, Gravity.END)
            val menu = popup.menu

            menu.add(0, 0, 0, "📅 今天（實時）")
            for (d in 1..totalDaysPassed.coerceAtMost(365)) {
                menu.add(0, d, d, "第${d}天")
            }

            popup.setOnMenuItemClickListener { item: MenuItem ->
                selectedDayIndex = item.itemId
                updateAllStats()
                true
            }
            popup.show()
        }
    }

    // ==================== 核心计算引擎 ====================

    /**
     * 统一入口：根据 selectedDayIndex 决定显示今天还是历史某一天的数据
     */
    private fun updateAllStats() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val quitStartDate = prefs.getLong("quit_start_date", 0)
        val now = System.currentTimeMillis()

        if (quitStartDate <= 0) {
            showZeroState()
            return
        }

        // ---- 读取三大基础参数 ----
        val dailyCigs = prefs.getInt("daily_cigs", 20)       // 日均支数
        val packPrice = prefs.getInt("pack_price", 25)        // 包价格(元)
        val pricePerCig = packPrice / 20.0                    // 单支价 = 包价 ÷ 20
        val yearsSmoking = prefs.getInt("years_smoking", 10) // 吸烟年限

        // ---- 时间差 ----
        val elapsedMs = now - quitStartDate
        val totalSeconds = (elapsedMs / 1000).toInt()
        val totalMinutes = totalSeconds / 60
        val totalHoursDouble = elapsedMs / (1000.0 * 60.0 * 60.0)
        val fullDaysElapsed = totalMinutes / 60 / 24          // 完整天数(0-based)

        if (selectedDayIndex == 0) {
            // ===== 今天：实时的、可能不满24小时 =====
            showTodayRealtimeData(
                elapsedMs, totalSeconds, totalHoursDouble,
                fullDaysElapsed, dailyCigs, pricePerCig,
                yearsSmoking, prefs
            )
        } else {
            // ===== 历史某一天：完整24小时数据 =====
            showPastDayFullData(
                selectedDayIndex, dailyCigs, pricePerCig,
                yearsSmoking, fullDaysElapsed, prefs
            )
        }

        // 底部历史损耗区域（始终不变）
        showHistoryLossSection(dailyCigs, pricePerCig, yearsSmoking)
    }

    // ==================== A. 今天（实时） ====================

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
        // ---- A1. 未吸菸時間（实时走秒）----
        val displayHrs = totalHours.toInt()
        val displayMins = (totalSeconds / 60) % 60
        val displaySecs = totalSeconds % 60
        tvSmokeFreeTime.text = "%d小時 %02d分 %02d秒".format(displayHrs, displayMins, displaySecs)

        // ---- A2. 圆环进度 = 当天已过时长 ÷ 24h × 100% ----
        val todayFractionalHour = totalHours % 24.0           // 今天已过的小时数
        val dayNumber = fullDaysElapsed + 1                   // 第N天(1-based)
        val circlePercent = (todayFractionalHour / 24.0 * 100.0).coerceIn(0.0, 99.9)

        tvProgressDays.text = "第${dayNumber}天"
        tvProgressPercent.text = "%.1f%%".format(circlePercent)
        tvProgressTarget.text = "24小時"
        tvProgressStatus.text = getTodayMotivation(fullDaysElapsed)

        // ---- A3. 未吸的香菸 = 已过总小时 × (日均支数 ÷ 24h) ----
        // 例: 19.8236h × (20÷24) ≈ 16.52支
        val cigsPerHour = dailyCigs.toDouble() / HOURS_PER_DAY
        val cigsAvoided = totalHours * cigsPerHour

        // 减去用户实际记录的吸烟量
        val totalSmokedRecorded = prefs.getInt("total_smoked_all_time", 0).toDouble()
        val netCigsAvoided = (cigsAvoided - totalSmokedRecorded).coerceAtLeast(0.0)
        tvCigarettesAvoided.text = "%.1f".format(netCigsAvoided)

        // ---- A4. 省下的錢 = 未吸支数 × 单支价 ----
        // 例: 20.6支 × ¥0.70 = ¥14.42
        val moneySaved = netCigsAvoided * pricePerCig
        tvMoneySaved.text = "¥ %.2f".format(moneySaved)

        // ---- A5. 挽回的生命 = 未吸支数 × 20分钟/支 → 转时分秒 ----
        // 例: 20.6支 × 20min = 412min = 6h 52m
        formatLifeRegained(netCigsAvoided)
    }

    // ==================== B. 历史某一天（完整24h） ====================

    private fun showPastDayFullData(
        targetDay: Int,              // 用户选的第N天(1-based)
        dailyCigs: Int,
        pricePerCig: Double,
        yearsSmoking: Int,
        fullDaysElapsed: Int,
        prefs: android.content.SharedPreferences
    ) {
        val dayZeroBased = targetDay - 1

        // 安全检查：如果选了还没到的天，回退到今天
        if (dayZeroBased > fullDaysElapsed) {
            selectedDayIndex = 0
            updateAllStats()
            return
        }

        // ---- B1. 未吸菸時間 = 完整 24小時 00分 00秒 ----
        tvSmokeFreeTime.text = "24小時 00分 00秒"

        // ---- B2. 圆环 = 100% ----
        tvProgressDays.text = "第${targetDay}天"
        tvProgressPercent.text = "100.0%"
        tvProgressTarget.text = "24小時"
        tvProgressStatus.text = getPastDayMotivation(targetDay)

        // ---- 到这一天为止的累计数据 ----
        // 历史天默认视为完全避免(没打卡记录=没抽烟)
        val totalSmokedRecorded = prefs.getInt("total_smoked_all_time", 0).toDouble()
        val cumulativeAvoided = (targetDay * dailyCigs.toDouble() - totalSmokedRecorded)
            .coerceAtLeast(0.0)

        // ---- B3. 未吸的香菸（累计到该天）----
        tvCigarettesAvoided.text = "%.1f".format(cumulativeAvoided)

        // ---- B4. 省下的錢（累计到该天）----
        tvMoneySaved.text = "¥ %.2f".format(cumulativeAvoided * pricePerCig)

        // ---- B5. 挽回的生命（累计到该天）----
        formatLifeRegained(cumulativeAvoided)
    }

    // ==================== E. 底部：过往吸烟总损耗 ====================

    /**
     * 已吸的香菸总数 = 日均支数 × 一年365天 × 吸烟年限
     * 浪費的錢     = 总吸烟支数 × 单支价
     * 損失的生命   = 总吸烟支数 × 20分钟/支 → 转年月天
     */
    private fun showHistoryLossSection(
        dailyCigs: Int,
        pricePerCig: Double,
        yearsSmoking: Int
    ) {
        // E1. 已吸香烟总数
        val histTotalCigs = dailyCigs.toLong() * DAYS_PER_YEAR * yearsSmoking
        tvHistCigarettes.text = "%,d".format(histTotalCigs)

        // E2. 浪费的钱
        val histCost = histTotalCigs * pricePerCig
        tvHistMoney.text = "¥ %.2f".format(histCost)

        // E3. 损失的生命（年/月/天）
        // 总折寿分钟 = 总支数 × 20分钟
        val totalLostMinutes = histTotalCigs * LIFE_LOSS_MINUTES_PER_CIG
        val lostYears = totalLostMinutes / (DAYS_PER_YEAR * MINUTES_PER_HOUR * HOURS_PER_DAY)
        val lostMonths = (totalLostMinutes % (DAYS_PER_YEAR * MINUTES_PER_HOUR * HOURS_PER_DAY)) /
                (DAYS_PER_MONTH_AVG * MINUTES_PER_HOUR * HOURS_PER_DAY)
        val lostDays = (totalLostMinutes % (DAYS_PER_MONTH_AVG * MINUTES_PER_HOUR * HOURS_PER_DAY)) /
                (MINUTES_PER_HOUR * HOURS_PER_DAY)

        tvHistLifeLost.text = "${lostYears}年 ${lostMonths}月 ${lostDays}天"
    }

    // ==================== 工具方法 ====================

    /**
     * 格式化挽回生命：未吸支数 × 20分钟 → X小時 XX分 XX秒
     */
    private fun formatLifeRegained(cigsAvoided: Double) {
        // 总挽回分钟数
        val totalMinutesRegained = cigsAvoided * LIFE_LOSS_MINUTES_PER_CIG

        // 转为 小時 : 分 : 秒
        val hours = totalMinutesRegained.toInt() / MINUTES_PER_HOUR
        val remainingMins = totalMinutesRegained.toInt() % MINUTES_PER_HOUR
        val seconds = ((totalMinutesRegained % 1.0) * MINUTES_PER_HOUR).toInt()

        tvLifeRegained.text = "%d小時 %02d分 %02d秒".format(hours, remainingMins, seconds)
    }

    /** 鼓励语 — 今天 */
    private fun getTodayMotivation(fullDaysElapsed: Int): String = when {
        fullDaysElapsed >= 365 -> "你已成功戒菸一年！你是傳奇！"
        fullDaysElapsed >= 30 -> "堅持了一個月！身體正在恢復"
        fullDaysElapsed >= 7 -> "一周了！你做得很棒！"
        fullDaysElapsed >= 3 -> "三天了，最難的階段已過去！"
        fullDaysElapsed >= 1 -> "第二天！繼續保持！"
        else -> "每天我都在贏！"
    }

    /** 鼓励语 — 历史某一天 */
    private fun getPastDayMotivation(dayNumber: Int): String = when (dayNumber) {
        1 -> "第一天成功！偉大的開始！"
        in 2..6 -> "第${dayNumber}天完成！堅持住！"
        7 -> "一周全勤！太強了！"
        14, 21, 30 -> "第${dayNumber}天里程碑！🎉"
        50, 100, 200, 300 -> "第${dayNumber}天！你是戰神！"
        365 -> "一年了！！！傳奇人物！！！"
        else -> "第${dayNumber}天完美通關 ✅"
    }

    /** 零状态（尚未开始戒烟） */
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
        tvHistLifeLost.text = "0年 0月 0天"
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
