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

package me.raatiniemi.worker.features.projects.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.raatiniemi.worker.data.projects.datasource.ProjectDataSourceFactory
import me.raatiniemi.worker.domain.exception.DomainException
import me.raatiniemi.worker.domain.exception.InvalidStartingPointException
import me.raatiniemi.worker.domain.interactor.ClockIn
import me.raatiniemi.worker.domain.interactor.ClockOut
import me.raatiniemi.worker.domain.interactor.GetProjectTimeSince
import me.raatiniemi.worker.domain.interactor.RemoveProject
import me.raatiniemi.worker.domain.model.Project
import me.raatiniemi.worker.domain.model.TimeInterval
import me.raatiniemi.worker.domain.model.TimeIntervalStartingPoint
import me.raatiniemi.worker.domain.repository.ProjectRepository
import me.raatiniemi.worker.features.projects.model.ProjectsItem
import me.raatiniemi.worker.features.projects.model.ProjectsViewActions
import me.raatiniemi.worker.features.shared.model.ConsumableLiveData
import me.raatiniemi.worker.util.AppKeys
import me.raatiniemi.worker.util.KeyValueStore
import timber.log.Timber
import java.util.*

internal class ProjectsViewModel(
        private val keyValueStore: KeyValueStore,
        projectRepository: ProjectRepository,
        private val getProjectTimeSince: GetProjectTimeSince,
        private val clockIn: ClockIn,
        private val clockOut: ClockOut,
        private val removeProject: RemoveProject
) : ViewModel() {
    private val startingPoint: TimeIntervalStartingPoint
        get() {
            val defaultValue = TimeIntervalStartingPoint.MONTH
            val startingPoint = keyValueStore.int(
                    AppKeys.TIME_SUMMARY.rawValue,
                    defaultValue.rawValue
            )

            return try {
                TimeIntervalStartingPoint.from(startingPoint)
            } catch (e: InvalidStartingPointException) {
                Timber.w(e, "Invalid starting point supplied: %i", startingPoint)
                defaultValue
            }
        }

    val projects: LiveData<PagedList<ProjectsItem>>

    private val factory = ProjectDataSourceFactory(projectRepository)
            .map { buildProjectsItem(it) }

    val viewActions = ConsumableLiveData<ProjectsViewActions>()

    init {
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setEnablePlaceholders(true)
                .build()

        projects = LivePagedListBuilder(factory, config).build()
    }

    private fun buildProjectsItem(project: Project): ProjectsItem {
        val registeredTime = loadRegisteredTimeForProject(project)

        return ProjectsItem(project, registeredTime)
    }

    private fun loadRegisteredTimeForProject(project: Project): List<TimeInterval> {
        return try {
            getProjectTimeSince(project, startingPoint)
        } catch (e: DomainException) {
            Timber.w(e, "Unable to get registered time for project")
            emptyList()
        }
    }

    fun reloadProjects() {
        projects.value?.run {
            dataSource.invalidate()
        }
    }

    suspend fun refreshActiveProjects(projects: List<ProjectsItem?>) = withContext(Dispatchers.Default) {
        val positions = projects.filterNotNull()
                .filter { it.isActive }
                .map { projects.indexOf(it) }

        if (positions.isEmpty()) {
            return@withContext
        }

        val viewAction = ProjectsViewActions.RefreshProjects(positions)
        viewActions.postValue(viewAction)
    }

    suspend fun clockIn(item: ProjectsItem, date: Date) = withContext(Dispatchers.IO) {
        try {
            val project = item.asProject()

            clockIn(project.id, date)

            viewActions.postValue(ProjectsViewActions.UpdateNotification(project))
            reloadProjects()
        } catch (e: Exception) {
            viewActions.postValue(ProjectsViewActions.ShowUnableToClockInErrorMessage)
        }
    }

    suspend fun clockOut(item: ProjectsItem, date: Date) = withContext(Dispatchers.IO) {
        try {
            val project = item.asProject()

            clockOut(project.id, date)

            viewActions.postValue(ProjectsViewActions.UpdateNotification(project))
            reloadProjects()
        } catch (e: Exception) {
            viewActions.postValue(ProjectsViewActions.ShowUnableToClockOutErrorMessage)
        }
    }

    suspend fun remove(item: ProjectsItem) = withContext(Dispatchers.IO) {
        try {
            val project = item.asProject()
            removeProject(project)

            viewActions.postValue(ProjectsViewActions.DismissNotification(project))
            reloadProjects()
        } catch (e: Exception) {
            viewActions.postValue(ProjectsViewActions.ShowUnableToDeleteProjectErrorMessage)
        }
    }
}
