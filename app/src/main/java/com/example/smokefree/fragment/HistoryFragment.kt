package com.example.smokefree.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.smokefree.R

class HistoryFragment : Fragment() {

    private lateinit var tvHistDaily: TextView
    private lateinit var tvHistYears: TextView
    private lateinit var tvHistTotal: TextView
    private lateinit var tvHistNote: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        updateHistory()
    }

    private fun initViews(view: View) {
        tvHistDaily = view.findViewById(R.id.tv_hist_daily)
        tvHistYears = view.findViewById(R.id.tv_hist_years)
        tvHistTotal = view.findViewById(R.id.tv_hist_total)
        tvHistNote = view.findViewById(R.id.tv_hist_note)
    }

    private fun updateHistory() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val dailyCigs = prefs.getInt("daily_cigs", 0)
        val yearsSmoking = prefs.getInt("years_smoking", 0)
        val packPrice = prefs.getInt("pack_price", 0)
        
        if (dailyCigs > 0 && yearsSmoking > 0) {
            val totalCigs = dailyCigs * 365 * yearsSmoking
            val totalMoney = (totalCigs / 20.0 * packPrice).toInt()
            val packEquiv = totalCigs / 20
            val coffeeEquiv = totalMoney / 30
            
            tvHistDaily.text = dailyCigs.toString()
            tvHistYears.text = yearsSmoking.toString()
            tvHistTotal.text = "¥${totalMoney}"
            
            tvHistNote.text = "💡 你已累计吸烟 ${totalCigs} 支，相当于 ${packEquiv} 包烟。\n这些钱可以买 ${coffeeEquiv} 杯咖啡 ☕"
        }
    }
}
