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

package me.raatiniemi.worker.data.service.ongoing

import android.content.Context
import android.content.Intent
import me.raatiniemi.worker.domain.interactor.CalculateTimeToday

import me.raatiniemi.worker.domain.interactor.GetProject
import me.raatiniemi.worker.domain.interactor.IsProjectActive
import me.raatiniemi.worker.domain.model.Project
import me.raatiniemi.worker.features.shared.view.notification.PauseNotification
import me.raatiniemi.worker.util.OngoingUriCommunicator
import org.koin.android.ext.android.inject
import timber.log.Timber

class ProjectNotificationService : OngoingService("ProjectNotificationService") {
    private val getProject: GetProject by inject()
    private val isProjectActive: IsProjectActive by inject()
    private val calculateTimeToday: CalculateTimeToday by inject()

    override fun onHandleIntent(intent: Intent?) {
        try {
            val projectId = getProjectId(intent)
            val project = getProject(projectId)

            if (isProjectActive(project.id)) {
                sendOrDismissPauseNotification(project)
                return
            }
            dismissNotification(project.id)
        } catch (e: Exception) {
            Timber.e(e, "Unable to update notification for project")
        }
    }

    private fun sendOrDismissPauseNotification(project: Project) {
        sendOrDismissOngoingNotification(project) {
            PauseNotification.build(
                    this,
                    project,
                    calculateTimeToday(project),
                    isOngoingNotificationChronometerEnabled
            )
        }
    }

    companion object {
        fun startServiceWithContext(context: Context, project: Project) {
            val intent = Intent(context, ProjectNotificationService::class.java)
            intent.data = OngoingUriCommunicator.createWith(project.id)
            context.startService(intent)
        }
    }
}
