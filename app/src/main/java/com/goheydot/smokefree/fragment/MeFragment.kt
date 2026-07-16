package com.goheydot.smokefree.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.goheydot.smokefree.R
import com.goheydot.smokefree.receiver.ReminderReceiver
import com.goheydot.smokefree.activity.LoginActivity
import com.goheydot.smokefree.activity.UserProfileActivity
import com.goheydot.smokefree.activity.DataExportActivity
import com.goheydot.smokefree.activity.FeedbackActivity
import com.goheydot.smokefree.activity.AboutActivity

class MeFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserDesc: TextView
    private lateinit var layoutLoggedIn: LinearLayout
    private lateinit var layoutHistoryForm: LinearLayout
    private lateinit var layoutHistoryResult: LinearLayout
    private lateinit var etDaily: EditText
    private lateinit var etPrice: EditText
    private lateinit var etYears: EditText
    private lateinit var btnCalculate: Button
    private lateinit var btnStartQuit: Button
    private lateinit var tvHistDaily: TextView
    private lateinit var tvHistYears: TextView
    private lateinit var tvHistTotal: TextView
    private lateinit var tvHistoryDesc: TextView
    private lateinit var btnLogout: Button

    // Timer / notification settings
    private lateinit var gridTimerOptions: GridLayout
    private lateinit var tvTimerDesc: TextView
    private var selectedTimerHours: Int = 2   // 默认2小时
    /** 当前选中的选项 View，用于切换样式 */
    private var selectedTimerView: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_me, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupListeners()
        updateUI()
    }

    private fun initViews(view: View) {
        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserDesc = view.findViewById(R.id.tv_user_desc)
        layoutLoggedIn = view.findViewById(R.id.layout_logged_in)
        layoutHistoryForm = view.findViewById(R.id.layout_history_form)
        layoutHistoryResult = view.findViewById(R.id.layout_history_result)
        etDaily = view.findViewById(R.id.et_daily_cigs)
        etPrice = view.findViewById(R.id.et_pack_price)
        etYears = view.findViewById(R.id.et_years_smoking)
        btnCalculate = view.findViewById(R.id.btn_calculate_history)
        btnStartQuit = view.findViewById(R.id.btn_start_quit)
        tvHistDaily = view.findViewById(R.id.tv_res_daily)
        tvHistYears = view.findViewById(R.id.tv_res_years)
        tvHistTotal = view.findViewById(R.id.tv_res_total)
        tvHistoryDesc = view.findViewById(R.id.tv_history_desc)
        btnLogout = view.findViewById(R.id.btn_logout)

        // Timer options
        gridTimerOptions = view.findViewById(R.id.grid_timer_options)
        tvTimerDesc = view.findViewById(R.id.tv_timer_desc)
    }

    private fun setupListeners() {
        // 点击登录 → 跳转登录页
        view?.findViewById<View>(R.id.layout_not_logged_in)?.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        // 已登录状态点击用户信息区域 → 跳转账户详情页
        view?.findViewById<View>(R.id.layout_logged_in)?.setOnClickListener {
            val intent = Intent(requireContext(), UserProfileActivity::class.java)
            startActivity(intent)
        }

        // Toggle history form
        view?.findViewById<View>(R.id.layout_smoking_history)?.setOnClickListener {
            layoutHistoryForm.visibility = if (layoutHistoryForm.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        // Toggle notification form
        view?.findViewById<View>(R.id.layout_notification)?.setOnClickListener {
            val notifForm = view?.findViewById<LinearLayout>(R.id.layout_notif_form)
            if (notifForm?.visibility == View.VISIBLE) {
                notifForm.visibility = View.GONE
            } else {
                notifForm?.visibility = View.VISIBLE
                // 首次展开时动态生成选项
                if (gridTimerOptions.childCount == 0) {
                    buildTimerOptions()
                }
            }
        }

        // Calculate history
        btnCalculate.setOnClickListener {
            calculateHistory()
        }

        // Start quit plan
        btnStartQuit.setOnClickListener {
            startQuitPlan()
        }

        // Save timer settings
        view?.findViewById<Button>(R.id.btn_save_timer)?.setOnClickListener {
            saveTimerSettings()
        }

        // Logout
        btnLogout.setOnClickListener {
            logout()
        }

        // 数据导出
        view?.findViewById<View>(R.id.layout_data_export)?.setOnClickListener {
            val intent = Intent(requireContext(), DataExportActivity::class.java)
            startActivity(intent)
        }

        // 帮助与反馈
        view?.findViewById<View>(R.id.layout_feedback)?.setOnClickListener {
            val intent = Intent(requireContext(), FeedbackActivity::class.java)
            startActivity(intent)
        }

        // 关于
        view?.findViewById<View>(R.id.layout_about_item)?.setOnClickListener {
            val intent = Intent(requireContext(), AboutActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateUI() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)

        if (isLoggedIn) {
            layoutLoggedIn.visibility = View.VISIBLE
            view?.findViewById<View>(R.id.layout_not_logged_in)?.visibility = View.GONE
            btnLogout.visibility = View.VISIBLE

            val phone = prefs.getString("phone", "") ?: ""
            val wechatName = prefs.getString("wechat_name", "") ?: ""
            if (phone.isNotEmpty()) {
                tvUserName.text = phone.substring(0, 3) + "****" + phone.substring(7)
                tvUserDesc.text = "手机号登录 · 点击查看详情"
            } else if (wechatName.isNotEmpty()) {
                tvUserName.text = wechatName
                tvUserDesc.text = "微信登录 · 点击查看详情"
            }
        } else {
            layoutLoggedIn.visibility = View.GONE
            view?.findViewById<View>(R.id.layout_not_logged_in)?.visibility = View.VISIBLE
            btnLogout.visibility = View.GONE
        }

        // Update history display（从SharedPreferences恢复已保存的数据）
        val dailyCigs = prefs.getInt("daily_cigs", 0)
        val yearsSmoking = prefs.getInt("years_smoking", 0)
        val packPrice = prefs.getInt("pack_price", 0)

        if (dailyCigs > 0 && packPrice > 0 && yearsSmoking > 0) {
            // 恢复输入框
            etDaily.setText(dailyCigs.toString())
            etPrice.setText(packPrice.toString())
            etYears.setText(yearsSmoking.toString())

            // 恢复摘要栏
            tvHistoryDesc.text = "${dailyCigs}支/天 · ${yearsSmoking}年"
            tvHistoryDesc.setTextColor(resources.getColor(R.color.pink_600, null))

            // 恢复结果区域
            val totalCigs = dailyCigs.toLong() * 365 * yearsSmoking
            val totalMoney = (totalCigs / 20.0 * packPrice).toLong()
            tvHistDaily.text = "${dailyCigs}支/天"
            tvHistYears.text = "${yearsSmoking}年"
            tvHistTotal.text = "¥${"%,d".format(totalMoney)}"
            layoutHistoryResult.visibility = View.VISIBLE
            layoutHistoryForm.visibility = View.GONE
        } else if (dailyCigs > 0) {
            // 只有部分数据的情况（兼容旧数据）
            tvHistDaily.text = "${dailyCigs}支/天"
            tvHistYears.text = "${yearsSmoking}年"
            layoutHistoryResult.visibility = View.VISIBLE
            tvHistoryDesc.text = "${dailyCigs}支/天 · ${yearsSmoking}年"
            tvHistoryDesc.setTextColor(resources.getColor(R.color.pink_600, null))
        }

        // 更新戒烟计划按钮状态
        val quitStartDate = prefs.getLong("quit_start_date", 0)
        if (quitStartDate > 0) {
            btnStartQuit.text = "🚀 戒烟计划进行中..."
            // 改变按钮背景为绿色表示已启动（需要新建一个绿色背景）
            btnStartQuit.setBackgroundColor(
                resources.getColor(android.R.color.holo_green_dark, null)
            )
            btnStartQuit.isEnabled = false
        }
    }

    private fun calculateHistory() {
        val daily = etDaily.text.toString().toIntOrNull() ?: 0
        val price = etPrice.text.toString().toIntOrNull() ?: 0
        val years = etYears.text.toString().toIntOrNull() ?: 0
        
        if (daily == 0 || price == 0 || years == 0) {
            Toast.makeText(requireContext(), "请填写完整信息哦～", Toast.LENGTH_SHORT).show()
            return
        }
        
        val totalCigs = daily * 365 * years
        val totalMoney = (totalCigs / 20.0 * price).toInt()
        
        // Save to preferences
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        prefs.edit()
            .putInt("daily_cigs", daily)
            .putInt("pack_price", price)
            .putInt("years_smoking", years)
            .apply()
        
        // Update display
        tvHistDaily.text = "$daily"
        tvHistYears.text = "$years"
        tvHistTotal.text = "¥${totalMoney}"

        // 更新"未填写"为已填写摘要
        tvHistoryDesc.text = "${daily}支/天 · ${years}年"
        tvHistoryDesc.setTextColor(resources.getColor(R.color.pink_600, null))

        layoutHistoryForm.visibility = View.GONE
        layoutHistoryResult.visibility = View.VISIBLE
        
        Toast.makeText(requireContext(), "已保存你的吸烟历史 📊", Toast.LENGTH_SHORT).show()
    }

    // ==================== 提醒设置：动态生成24个选项 ====================

    /**
     * 动态创建 1小时 ~ 24小时的选项按钮（4列 × 6行）
     */
    private fun buildTimerOptions() {
        gridTimerOptions.removeAllViews()

        val savedHours = requireContext().getSharedPreferences("smokefree", 0)
            .getInt("reminder_interval_hours", 2)

        for (hour in 1..24) {
            val tv = TextView(requireContext()).apply {
                id = View.generateViewId()
                text = "$hour\n小時"
                textSize = 12f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 10.dp, 0, 10.dp)
                setTextColor(resources.getColor(R.color.gray_600, null))
                background = resources.getDrawable(R.drawable.bg_input, null)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    marginStart = 3.dp
                    marginEnd = 3.dp
                    topMargin = 4.dp
                    bottomMargin = 4.dp
                }
                isClickable = true
                isFocusable = true

                setOnClickListener { v ->
                    selectTimerOption(v as TextView, hour)
                }
            }

            // 标记已保存的选中项
            if (hour == savedHours) {
                selectedTimerHours = hour
                applySelectedStyle(tv)
                selectedTimerView = tv
            }

            gridTimerOptions.addView(tv)
        }

        // 更新右侧描述文字
        tvTimerDesc.text = "每${savedHours}小時"
    }

    /** 选中某个时间间隔 */
    private fun selectTimerOption(tv: TextView, hours: Int) {
        // 恢复之前选中的为普通样式
        selectedTimerView?.let {
            it.setTextColor(resources.getColor(R.color.gray_600, null))
            it.background = resources.getDrawable(R.drawable.bg_input, null)
        }

        // 设置新的选中样式
        selectedTimerHours = hours
        selectedTimerView = tv
        applySelectedStyle(tv)
    }

    private fun applySelectedStyle(tv: TextView) {
        tv.setTextColor(resources.getColor(R.color.pink_700, null))
        tv.setTypeface(null, android.graphics.Typeface.BOLD)
        tv.background = resources.getDrawable(R.drawable.bg_tab_selected, null)
    }

    private fun saveTimerSettings() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        prefs.edit()
            .putInt("reminder_interval_hours", selectedTimerHours)
            .putBoolean("reminder_enabled", true)
            .apply()

        tvTimerDesc.text = "每${selectedTimerHours}小時"

        // 创建通知渠道 & 设置定时提醒
        ReminderReceiver.createNotificationChannel(requireContext())
        ReminderReceiver.scheduleReminder(requireContext(), selectedTimerHours)

        Toast.makeText(requireContext(),
            "✅ 已设置每 ${selectedTimerHours} 小时提醒",
            Toast.LENGTH_SHORT).show()

        // 收起表单
        view?.findViewById<LinearLayout>(R.id.layout_notif_form)?.visibility = View.GONE
    }

    // dp 转 px 扩展
    private val Int.dp: Int get() =
        (this * resources.displayMetrics.density).toInt()

    private fun startQuitPlan() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        prefs.edit()
            .putLong("quit_start_date", System.currentTimeMillis())
            .apply()

        // 立即更新按钮状态
        btnStartQuit.text = "🚀 戒烟计划进行中..."
        btnStartQuit.setBackgroundColor(
            resources.getColor(android.R.color.holo_green_dark, null)
        )
        btnStartQuit.isEnabled = false

        Toast.makeText(requireContext(), "🚭 戒烟计划已启动！加油！", Toast.LENGTH_LONG).show()
    }

    private fun logout() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .putString("phone", "")
            .apply()
        
        updateUI()
        Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}
