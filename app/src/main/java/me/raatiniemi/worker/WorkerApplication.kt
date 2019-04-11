/*
 * Copyright (C) 2018 Tobias Raatiniemi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.raatiniemi.worker

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import io.fabric.sdk.android.Fabric
import me.raatiniemi.worker.data.dataModule
import me.raatiniemi.worker.features.projects.projectsModule
import me.raatiniemi.worker.features.settings.settingsModule
import me.raatiniemi.worker.monitor.logging.CrashlyticsTree
import me.raatiniemi.worker.monitor.monitorModule
import me.raatiniemi.worker.util.Notifications
import org.koin.android.ext.android.startKoin
import timber.log.Timber
import timber.log.Timber.DebugTree

open class WorkerApplication : Application() {
    private val notificationManager: NotificationManager
        get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    internal open val isUnitTesting: Boolean
        get() = false

    override fun onCreate() {
        super.onCreate()

        if (!isUnitTesting) {
            startKoin(this, listOf(
                    monitorModule,
                    preferenceModule,
                    dataModule,
                    projectsModule,
                    settingsModule,
                    useCaseModule
            ))

            configureLogging()
            registerNotificationChannel()
        }
    }

    private fun configureLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        if (Fabric.isInitialized()) {
            Timber.plant(CrashlyticsTree())
        }
    }

    private fun registerNotificationChannel() {
        try {
            val notificationManager = notificationManager
            Notifications.createChannel(
                    notificationManager,
                    Notifications.ongoingChannel(resources)
            )
        } catch (e: ClassCastException) {
            Timber.e(e, "Unable to register notification channel")
        } catch (e: NullPointerException) {
            Timber.e(e, "Unable to register notification channel")
        }
    }

    companion object {
        const val NOTIFICATION_ON_GOING_ID = 3
    }
}
