package com.uts.socialuts.utils

import com.uts.socialuts.providers.UsersProvider
import android.app.ActivityManager
import android.content.Context

object ViewedMessageHelper {
    fun updateOnline(status: Boolean, context: Context) {
        val usersProvider = UsersProvider()
        val authProvider: com.uts.socialuts.providers.AuthProvider = com.uts.socialuts.providers.AuthProvider()
        if (authProvider.getUid() != null) {
            if (isApplicationSentToBackground(context)) {
                usersProvider.updateOnline(authProvider.getUid(), status)
            } else if (status) {
                usersProvider.updateOnline(authProvider.getUid(), status)
            }
        }
    }

    private fun isApplicationSentToBackground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = activityManager.getRunningTasks(1)
        if (tasks.isNotEmpty()) {
            val topActivity = tasks[0].topActivity
            if (topActivity!!.packageName != context.packageName) {
                return true
            }
        }
        return false
    }
}