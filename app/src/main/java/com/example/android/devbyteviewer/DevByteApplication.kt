/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.devbyteviewer

import android.app.Application
import android.os.Build
import androidx.work.*
import com.example.android.devbyteviewer.work.RefreshDataWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Override application to setup background work via WorkManager
 */
class DevByteApplication : Application() {

    /**
     * Scope that the application uses to run background tasks by way of coroutines
     */
    val applicationScope = CoroutineScope(Dispatchers.Default)

    /**
     * onCreate is called before the first screen is shown to the user.
     *
     * Use it to setup any background tasks, running expensive setup operations in a background
     * thread to avoid delaying app start.
     */
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        delayedInit()
    }

    /**
     * Background task that sets up periodic work
     */
    private fun delayedInit() = applicationScope.launch {
        setUpRecurringWork()
    }

    /**
     * Sets up periodic work using workManager
     */
    private fun setUpRecurringWork() {
        // constraints for the workManager
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED) // User doesn't pay for the network
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(true)
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // if OS is greater than Android Marshmallow
                        setRequiresDeviceIdle(true)
                    }
                }.build()
        // builder that creates a periodic work request that repeats once a day
        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWork>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()
        // work manager that enqueues periodic request with policy to keep the old request in case a second one is issued
        WorkManager.getInstance()
                .enqueueUniquePeriodicWork(RefreshDataWork.WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        repeatingRequest)
    }
}
