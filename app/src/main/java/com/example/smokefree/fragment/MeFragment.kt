package com.example.smokefree.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smokefree.R
import com.example.smokefree.activity.LoginActivity

class MeFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserDesc: TextView
    private lateinit var btnLogin: Button
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
    private lateinit var btnLogout: Button

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
        btnLogin = view.findViewById(R.id.btn_login)
        layoutLoggedIn = view.findViewById(R.id.layout_logged_in)
        layoutHistoryForm = view.findViewById(R.id.layout_history_form)
        layoutHistoryResult = view.findViewById(R.id.layout_history_result)
        etDaily = view.findViewById(R.id.et_daily)
        etPrice = view.findViewById(R.id.et_price)
        etYears = view.findViewById(R.id.et_years)
        btnCalculate = view.findViewById(R.id.btn_calculate)
        btnStartQuit = view.findViewById(R.id.btn_start_quit)
        tvHistDaily = view.findViewById(R.id.tv_hist_daily)
        tvHistYears = view.findViewById(R.id.tv_hist_years)
        tvHistTotal = view.findViewById(R.id.tv_hist_total)
        btnLogout = view.findViewById(R.id.btn_logout)
    }

    private fun setupListeners() {
        // Login button
        btnLogin.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        // Calculate history
        btnCalculate.setOnClickListener {
            calculateHistory()
        }

        // Start quit plan
        btnStartQuit.setOnClickListener {
            startQuitPlan()
        }

        // Logout
        btnLogout.setOnClickListener {
            logout()
        }

        // Toggle history form
        view?.findViewById<TextView>(R.id.tv_history_title)?.setOnClickListener {
            layoutHistoryForm.visibility = if (layoutHistoryForm.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    private fun updateUI() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        
        if (isLoggedIn) {
            btnLogin.visibility = View.GONE
            layoutLoggedIn.visibility = View.VISIBLE
            btnLogout.visibility = View.VISIBLE
            
            val phone = prefs.getString("phone", "") ?: ""
            if (phone.isNotEmpty()) {
                tvUserName.text = phone.substring(0, 3) + "****" + phone.substring(7)
                tvUserDesc.text = "手机号登录 · 点击查看详情"
            }
        } else {
            btnLogin.visibility = View.VISIBLE
            layoutLoggedIn.visibility = View.GONE
            btnLogout.visibility = View.GONE
        }
        
        // Update history display
        val dailyCigs = prefs.getInt("daily_cigs", 0)
        val yearsSmoking = prefs.getInt("years_smoking", 0)
        
        if (dailyCigs > 0) {
            tvHistDaily.text = "${dailyCigs}支/天"
            tvHistYears.text = "${yearsSmoking}年"
            layoutHistoryResult.visibility = View.VISIBLE
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
        
        layoutHistoryForm.visibility = View.GONE
        layoutHistoryResult.visibility = View.VISIBLE
        
        Toast.makeText(requireContext(), "已保存你的吸烟历史 📊", Toast.LENGTH_SHORT).show()
    }

    private fun startQuitPlan() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        prefs.edit()
            .putLong("quit_start_date", System.currentTimeMillis())
            .apply()
        
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
