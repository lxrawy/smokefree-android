package com.example.smokefree.fragment

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

    // 身体恢复进度条（5个里程碑）
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

    /** 首页数据自动刷新（与进度页保持同步） */
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

        // 身体恢复进度条
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
            // 计算生命损失并提示用户
            val lifeLostMinutes = count * 20
            Toast.makeText(
                requireContext(),
                "⚠️ 记录吸烟 ${count} 支\n💔 生命减少了 ${lifeLostMinutes} 分钟\n别灰心，下一次一定能忍住！",
                Toast.LENGTH_LONG
            ).show()
            saveTodayRecord(count)
        }
    }

    private fun updateStats() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val quitStartDate = prefs.getLong("quit_start_date", 0)

        if (quitStartDate > 0) {
            val now = System.currentTimeMillis()
            val elapsedMs = now - quitStartDate
            val days = (elapsedMs / (1000 * 60 * 60 * 24)).toInt()
            tvDays.text = days.toString()

            // ---- 三大基础参数（与进度页一致）----
            val dailyCigs = prefs.getInt("daily_cigs", 20)
            val packPrice = prefs.getInt("pack_price", 25)
            val pricePerCig = packPrice / 20.0

            // ---- 实时计算（与ProgressFragment.showTodayRealtimeData完全对齐）----
            val totalHoursDouble = elapsedMs / (1000.0 * 60.0 * 60.0)
            val cigsPerHour = dailyCigs.toDouble() / 24.0
            val cigsAvoided = totalHoursDouble * cigsPerHour

            // 扣减实际吸烟量
            val totalSmokedRecorded = prefs.getInt("total_smoked_all_time", 0).toDouble()
            val netCigsAvoided = (cigsAvoided - totalSmokedRecorded).coerceAtLeast(0.0)

            // 今日已吸 & 今日花费
            val todaySmoked = prefs.getInt("today_smoked", 0)
            tvTodaySmoked.text = todaySmoked.toString()
            tvTodayCost.text = "¥${(todaySmoked * pricePerCig).toInt()}"

            // 累计节省（与进度页"省下的錢"一致）
            val moneySaved = netCigsAvoided * pricePerCig
            tvTotalSaved.text = "¥${"%.2f".format(moneySaved)}"

            // 挽回生命 → 转换为健康指数（百分比）
            // 满分100分 = 挽回生命达到 30天（720小时）
            val totalLifeMinutes = netCigsAvoided * 20.0
            val healthScore = ((totalLifeMinutes / (30.0 * 24 * 60)) * 100).toInt().coerceIn(0, 100)
            tvHealthScore.text = "$healthScore%"
        }

        // 更新身体恢复进度条
        updateRecoveryProgress(quitStartDate)
    }

    private fun saveTodayRecord(count: Int) {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val editor = prefs.edit()
        editor.putInt("today_smoked", count)

        // 累计总吸烟数（供进度页计算挽回生命时扣除）
        // 每次基于增量更新：本次count - 上次记录的today_smoked
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

    /**
     * 身体恢复里程碑数据
     */
    data class RecoveryMilestone(
        val label: String,
        val targetMinutes: Long,
        val progressBar: ProgressBar,
        val labelView: TextView
    )

    /**
     * 更新身体恢复进度条 — 根据戒烟已过时间计算每个里程碑的完成百分比
     *
     * 里程碑（单位：分钟）：
     *   1. 20分钟    → 心率和血压恢复正常
     *   2. 12小时    → 血液中一氧化碳降至正常
     *   3. 21天(3周) → 循环系统改善，肺功能增强
     *   4. 270天(9月)→ 咳嗽和气短情况减少
     *   5. 365天(1年) → 冠心病风险降低50%
     *
     * 进度 = 已过分钟 ÷ 里程碑总分钟 × 100%
     * 达到100%后进度条填满绿色，标题变绿色+✅标记
     */
    private fun updateRecoveryProgress(quitStartDate: Long) {
        if (quitStartDate <= 0) return

        val elapsedMs = System.currentTimeMillis() - quitStartDate
        val elapsedMin = elapsedMs / (1000L * 60)

        val milestones = listOf(
            RecoveryMilestone("20分钟后", 20L, pbRecovery1, tvRecoveryTime1),
            RecoveryMilestone("12小时", 720L, pbRecovery2, tvRecoveryTime2),
            RecoveryMilestone("2-3周", 30240L, pbRecovery3, tvRecoveryTime3),   // 21天
            RecoveryMilestone("1-9个月", 388800L, pbRecovery4, tvRecoveryTime4), // 270天
            RecoveryMilestone("1年", 525600L, pbRecovery5, tvRecoveryTime5),      // 365天
        )

        for ((label, targetMin, progressBar, tvLabel) in milestones) {
            val progress = ((elapsedMin.toDouble() / targetMin.toDouble()) * 100).toInt().coerceIn(0, 100)
            progressBar.progress = progress

            if (progress >= 100) {
                // 已完成：标题变绿色 + ✅
                tvLabel.text = "✅ $label"
                tvLabel.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            } else {
                // 未完成：保持粉色
                tvLabel.text = label
                tvLabel.setTextColor(resources.getColor(R.color.pink_600))
            }
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(quoteRunnable)
        handler.removeCallbacks(statsRunnable)
    }
}
