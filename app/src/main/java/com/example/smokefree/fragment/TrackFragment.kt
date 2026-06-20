package com.example.smokefree.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smokefree.R
import java.text.SimpleDateFormat
import java.util.*

class TrackFragment : Fragment() {

    private lateinit var tvDays: TextView
    private lateinit var tvCigaretteCount: TextView
    private lateinit var btnMinus: View
    private lateinit var btnPlus: View
    private lateinit var btnSubmit: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvGoalName: TextView
    private lateinit var tvGoalTarget: TextView
    private lateinit var tvGoalSaved: TextView
    private lateinit var tvGoalRemain: TextView

    private var cigaretteCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_track, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupListeners()
        updateStats()
        updateGoalDisplay()
    }

    private fun initViews(view: View) {
        tvDays = view.findViewById(R.id.tv_track_days)
        tvCigaretteCount = view.findViewById(R.id.tv_cigarette_count)
        btnMinus = view.findViewById(R.id.btn_minus)
        btnPlus = view.findViewById(R.id.btn_plus)
        btnSubmit = view.findViewById(R.id.btn_submit_checkin)
        progressBar = view.findViewById(R.id.progress_goal)
        tvGoalName = view.findViewById(R.id.tv_goal_name)
        tvGoalTarget = view.findViewById(R.id.tv_goal_target)
        tvGoalSaved = view.findViewById(R.id.tv_goal_saved)
        tvGoalRemain = view.findViewById(R.id.tv_goal_remain)
    }

    private fun setupListeners() {
        btnMinus.setOnClickListener {
            if (cigaretteCount > 0) {
                cigaretteCount--
                tvCigaretteCount.text = cigaretteCount.toString()
            }
        }

        btnPlus.setOnClickListener {
            cigaretteCount++
            tvCigaretteCount.text = cigaretteCount.toString()
        }

        btnSubmit.setOnClickListener {
            submitCheckin()
        }
    }

    private fun updateStats() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        // 显示累计打卡天数
        val totalCheckins = prefs.getInt("total_checkin_days", 0)
        tvDays.text = totalCheckins.toString()
    }

    private fun updateGoalDisplay() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val goalName = prefs.getString("goal_name", "") ?: ""
        val goalAmount = prefs.getInt("goal_amount", 0)
        
        if (goalName.isNotEmpty() && goalAmount > 0) {
            val quitStartDate = prefs.getLong("quit_start_date", 0)
            val days = if (quitStartDate > 0) {
                ((System.currentTimeMillis() - quitStartDate) / (1000 * 60 * 60 * 24)).toInt()
            } else 0
            
            val dailyCigs = prefs.getInt("daily_cigs", 10)
            val packPrice = prefs.getInt("pack_price", 25)
            val pricePerCig = packPrice / 20.0
            val saved = (days * dailyCigs * pricePerCig).toInt()
            
            val progress = ((saved.toFloat() / goalAmount) * 100).toInt().coerceIn(0, 100)
            
            tvGoalName.text = "🎁 $goalName"
            tvGoalTarget.text = "目标金额：¥${goalAmount}"
            tvGoalSaved.text = "已攒：¥${saved}"
            tvGoalRemain.text = "还差：¥${goalAmount - saved}"
            progressBar.progress = progress
            
            if (saved >= goalAmount) {
                Toast.makeText(requireContext(), "🎉 恭喜！目标已达成！", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun submitCheckin() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val editor = prefs.edit()

        // 检查今天是否已经打卡
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastCheckinDate = prefs.getString("last_checkin_date", "")

        if (todayKey == lastCheckinDate) {
            Toast.makeText(requireContext(), "今天已经打过卡了，明天再来！", Toast.LENGTH_SHORT).show()
            return
        }

        editor.putString("last_checkin_date", todayKey)
        editor.putInt("today_smoked", cigaretteCount)

        // 按日期存储，供本周趋势图读取
        editor.putInt("smoke_$todayKey", cigaretteCount)

        // 累计总吸烟数（供进度页计算挽回生命时扣除）
        if (cigaretteCount > 0) {
            val totalSoFar = prefs.getInt("total_smoked_all_time", 0)
            editor.putInt("total_smoked_all_time", totalSoFar + cigaretteCount)
        }

        // 累计打卡天数 +1
        val totalCheckins = prefs.getInt("total_checkin_days", 0)
        editor.putInt("total_checkin_days", totalCheckins + 1)

        editor.apply()

        if (cigaretteCount == 0) {
            Toast.makeText(requireContext(), "🎉 太棒了！你一根都没抽！已坚持 ${totalCheckins + 1} 天！", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "✅ 打卡成功！今天抽了 ${cigaretteCount} 支，已坚持 ${totalCheckins + 1} 天！", Toast.LENGTH_LONG).show()
        }

        updateStats()
        updateGoalDisplay()
    }
}
