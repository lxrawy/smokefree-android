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
import com.goheydot.smokefree.activity.AboutActivity
import com.goheydot.smokefree.activity.DataExportActivity
import com.goheydot.smokefree.activity.FeedbackActivity
import com.goheydot.smokefree.receiver.ReminderReceiver

class MeFragment : Fragment() {

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

    private lateinit var gridTimerOptions: GridLayout
    private lateinit var tvTimerDesc: TextView
    private var selectedTimerHours: Int = 2
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

        gridTimerOptions = view.findViewById(R.id.grid_timer_options)
        tvTimerDesc = view.findViewById(R.id.tv_timer_desc)
    }

    private fun setupListeners() {
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
                if (gridTimerOptions.childCount == 0) {
                    buildTimerOptions()
                }
            }
        }

        btnCalculate.setOnClickListener {
            calculateHistory()
        }

        btnStartQuit.setOnClickListener {
            startQuitPlan()
        }

        view?.findViewById<Button>(R.id.btn_save_timer)?.setOnClickListener {
            saveTimerSettings()
        }

        view?.findViewById<View>(R.id.layout_data_export)?.setOnClickListener {
            val intent = Intent(requireContext(), DataExportActivity::class.java)
            startActivity(intent)
        }

        view?.findViewById<View>(R.id.layout_feedback)?.setOnClickListener {
            val intent = Intent(requireContext(), FeedbackActivity::class.java)
            startActivity(intent)
        }

        view?.findViewById<View>(R.id.layout_about_item)?.setOnClickListener {
            val intent = Intent(requireContext(), AboutActivity::class.java)
            startActivity(intent)
        }

    }

    private fun updateUI() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        val currency = getString(R.string.currency_symbol)

        val dailyCigs = prefs.getInt("daily_cigs", 0)
        val yearsSmoking = prefs.getInt("years_smoking", 0)
        val packPrice = prefs.getInt("pack_price", 0)

        if (dailyCigs > 0 && packPrice > 0 && yearsSmoking > 0) {
            etDaily.setText(dailyCigs.toString())
            etPrice.setText(packPrice.toString())
            etYears.setText(yearsSmoking.toString())

            tvHistoryDesc.text = getString(R.string.format_cigs_per_day, dailyCigs) + " · " + getString(R.string.format_years, yearsSmoking)
            tvHistoryDesc.setTextColor(resources.getColor(R.color.pink_600, null))

            val totalCigs = dailyCigs.toLong() * 365 * yearsSmoking
            val totalMoney = (totalCigs / 20.0 * packPrice).toLong()
            tvHistDaily.text = getString(R.string.format_cigs_per_day, dailyCigs)
            tvHistYears.text = getString(R.string.format_years, yearsSmoking)
            tvHistTotal.text = "$currency${"%,d".format(totalMoney)}"
            layoutHistoryResult.visibility = View.VISIBLE
            layoutHistoryForm.visibility = View.GONE
        } else if (dailyCigs > 0) {
            tvHistDaily.text = getString(R.string.format_cigs_per_day, dailyCigs)
            tvHistYears.text = getString(R.string.format_years, yearsSmoking)
            layoutHistoryResult.visibility = View.VISIBLE
            tvHistoryDesc.text = getString(R.string.format_cigs_per_day, dailyCigs) + " · " + getString(R.string.format_years, yearsSmoking)
            tvHistoryDesc.setTextColor(resources.getColor(R.color.pink_600, null))
        }

        val quitStartDate = prefs.getLong("quit_start_date", 0)
        if (quitStartDate > 0) {
            btnStartQuit.text = getString(R.string.me_quit_in_progress)
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
            Toast.makeText(requireContext(), getString(R.string.me_toast_fill_complete), Toast.LENGTH_SHORT).show()
            return
        }

        val totalCigs = daily * 365 * years
        val totalMoney = (totalCigs / 20.0 * price).toInt()

        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        prefs.edit()
            .putInt("daily_cigs", daily)
            .putInt("pack_price", price)
            .putInt("years_smoking", years)
            .apply()

        val currency = getString(R.string.currency_symbol)
        tvHistDaily.text = getString(R.string.format_cigs_per_day, daily)
        tvHistYears.text = getString(R.string.format_years, years)
        tvHistTotal.text = "$currency$totalMoney"

        tvHistoryDesc.text = getString(R.string.format_cigs_per_day, daily) + " · " + getString(R.string.format_years, years)
        tvHistoryDesc.setTextColor(resources.getColor(R.color.pink_600, null))

        layoutHistoryForm.visibility = View.GONE
        layoutHistoryResult.visibility = View.VISIBLE

        Toast.makeText(requireContext(), getString(R.string.me_toast_history_saved), Toast.LENGTH_SHORT).show()
    }

    private fun buildTimerOptions() {
        gridTimerOptions.removeAllViews()

        val savedHours = requireContext().getSharedPreferences("smokefree", 0)
            .getInt("reminder_interval_hours", 2)

        val hourLabel = getString(R.string.me_unit_hour)

        for (hour in 1..24) {
            val tv = TextView(requireContext()).apply {
                id = View.generateViewId()
                text = "$hour\n$hourLabel"
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

            if (hour == savedHours) {
                selectedTimerHours = hour
                applySelectedStyle(tv)
                selectedTimerView = tv
            }

            gridTimerOptions.addView(tv)
        }

        tvTimerDesc.text = getString(R.string.me_reminder_desc, savedHours)
    }

    private fun selectTimerOption(tv: TextView, hours: Int) {
        selectedTimerView?.let {
            it.setTextColor(resources.getColor(R.color.gray_600, null))
            it.background = resources.getDrawable(R.drawable.bg_input, null)
        }

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

        tvTimerDesc.text = getString(R.string.me_reminder_desc, selectedTimerHours)

        ReminderReceiver.createNotificationChannel(requireContext())
        ReminderReceiver.scheduleReminder(requireContext(), selectedTimerHours)

        Toast.makeText(requireContext(),
            getString(R.string.me_toast_reminder_set, selectedTimerHours),
            Toast.LENGTH_SHORT).show()

        view?.findViewById<LinearLayout>(R.id.layout_notif_form)?.visibility = View.GONE
    }

    private val Int.dp: Int get() =
        (this * resources.displayMetrics.density).toInt()

    private fun startQuitPlan() {
        val prefs = requireContext().getSharedPreferences("smokefree", 0)
        prefs.edit()
            .putLong("quit_start_date", System.currentTimeMillis())
            .apply()

        btnStartQuit.text = getString(R.string.me_quit_in_progress)
        btnStartQuit.setBackgroundColor(
            resources.getColor(android.R.color.holo_green_dark, null)
        )
        btnStartQuit.isEnabled = false

        Toast.makeText(requireContext(), getString(R.string.me_toast_quit_started), Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}
