package com.example.testarmflow.CompClassesCalendar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.testarmflow.R
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate


class AlarmReceiver : BroadcastReceiver() {
    private lateinit var uid: String
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            uid = intent?.getStringExtra("UID").toString()
            showNotify(context)
        }
    }

    private fun showNotify(context: Context) {
        val channelId = "all_notifications"
        getToDoTask { taskString ->
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Nie zapomnij! Dzisiaj:")
                .setContentText(taskString)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(notificationManager)
            }
            notificationManager.notify(123, builder.build())
        }
    }

    private fun getToDoTask(callback: (String) -> Unit) {
        val refCalendar = FirebaseFirestore.getInstance().collection("calendar")
        val date = LocalDate.now()
        val stringBuilder = StringBuilder()
        val query = refCalendar
            .whereEqualTo("uid", uid)
            .whereEqualTo("date", "${date.year}.${date.monthValue}.${date.dayOfMonth}")

        query.get().addOnSuccessListener { taskSnapshot ->
            if (!taskSnapshot.isEmpty) {
                for (document in taskSnapshot.documents){
                    stringBuilder.append("${document.getString("title")}\n${document.getString("description")}\n")
                }
            }
            callback.invoke(stringBuilder.toString()) // ze wzgledu na asynchornicznosc pobieranie wynikow
        }.addOnFailureListener { exception ->
            Log.e("FirebaseQueryError", "Error getting documents: $exception")
            callback.invoke("Error getting documents: $exception")
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelId = "all_notifications"
        val mChannel = NotificationChannel(
            channelId,
            "General Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        mChannel.description = "This is default channel used for all other notifications"
        notificationManager.createNotificationChannel(mChannel)
    }
}