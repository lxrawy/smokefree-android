package com.goheydot.smokefree.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.goheydot.smokefree.R
import com.goheydot.smokefree.activity.MainActivity

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "smokefree_reminder"
        const val CHANNEL_NAME = "戒烟提醒"
        const val NOTIFICATION_ID = 1001

        private val encouragements = listOf(
            "每一次拒绝香烟，都是给未来自己的一份礼物",
            "你比香烟更强大，相信自己！",
            "坚持一天，就是胜利；坚持一生，就是奇迹",
            "今天的坚持，是明天健康的基石",
            "呼吸越来越顺畅，身体正在感谢你",
            "别让一根烟毁掉你的努力",
            "你已经很棒了，继续保持！",
            "种一棵树最好的时间是十年前，其次是现在"
        )

        /**
         * 创建通知渠道（应用启动时调用一次即可）
         */
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "戒烟定时提醒"
                    enableVibration(true)
                }
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
            }
        }

        /**
         * 设置或更新定时提醒
         * @param intervalHours 间隔小时数（1~24）
         */
        fun scheduleReminder(context: Context, intervalHours: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 从当前时间开始，每隔 intervalHours 触发一次
            val intervalMs = intervalHours.toLong() * 60 * 60 * 1000
            val triggerTime = System.currentTimeMillis() + intervalMs

            alarmManager.setRepeating(
                android.app.AlarmManager.RTC_WAKEUP,
                triggerTime,
                intervalMs,
                pendingIntent
            )
        }

        /**
         * 取消所有提醒
         */
        fun cancelReminder(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val randomIndex = (Math.random() * encouragements.size).toInt()
        val message = encouragements[randomIndex]

        val openIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🚭 戒烟提醒")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }
}
