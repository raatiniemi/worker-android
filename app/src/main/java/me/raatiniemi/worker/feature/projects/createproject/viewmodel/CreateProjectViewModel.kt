/*
 * Copyright (C) 2020 Tobias Raatiniemi
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

package me.raatiniemi.worker.feature.projects.createproject.viewmodel

import androidx.lifecycle.*
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.raatiniemi.worker.domain.project.model.isValid
import me.raatiniemi.worker.domain.project.model.projectName
import me.raatiniemi.worker.domain.project.usecase.CreateProject
import me.raatiniemi.worker.domain.project.usecase.FindProject
import me.raatiniemi.worker.domain.project.usecase.InvalidProjectNameException
import me.raatiniemi.worker.domain.project.usecase.ProjectAlreadyExistsException
import me.raatiniemi.worker.feature.projects.createproject.model.CreateProjectViewActions
import me.raatiniemi.worker.feature.shared.model.*
import me.raatiniemi.worker.monitor.analytics.Event
import me.raatiniemi.worker.monitor.analytics.TracePerformanceEvents
import me.raatiniemi.worker.monitor.analytics.UsageAnalytics
import me.raatiniemi.worker.util.CoroutineDispatchProvider
import timber.log.Timber

internal class CreateProjectViewModel(
    private val usageAnalytics: UsageAnalytics,
    private val createProject: CreateProject,
    private val findProject: FindProject,
    private val dispatchProvider: CoroutineDispatchProvider
) : ViewModel() {
    private val _name = MutableLiveData<String>()
    var name: String by MutableLiveDataProperty(_name, "")

    private val isNameValid = _name.map(::isValid)
    private val isNameAvailable = debounceSuspend(viewModelScope, _name) { name ->
        checkForAvailability(name)
    }

    val isCreateEnabled: LiveData<Boolean> = combineLatest(isNameValid, isNameAvailable)
        .map { it.first && it.second }

    val viewActions = ConsumableLiveData<CreateProjectViewActions>()

    private suspend fun checkForAvailability(value: String): Boolean {
        return withContext(dispatchProvider.io()) {
            try {
                val project = findProject(projectName(value))
                if (project != null) {
                    viewActions += CreateProjectViewActions.DuplicateNameErrorMessage
                    false
                } else {
                    true
                }
            } catch (e: InvalidProjectNameException) {
                false
            }
        }
    }

    @AddTrace(name = TracePerformanceEvents.CREATE_PROJECT)
    fun createProject() = viewModelScope.launch(dispatchProvider.io()) {
        consumeSuspending(_name) { name ->
            try {
                val project = createProject(projectName(name))

                usageAnalytics.log(Event.ProjectCreate)
                viewActions += CreateProjectViewActions.Created(project)
            } catch (e: ProjectAlreadyExistsException) {
                Timber.d("Project with name \"$name\" already exists")
                viewActions += CreateProjectViewActions.DuplicateNameErrorMessage
            } catch (e: InvalidProjectNameException) {
                Timber.w("Project name \"$name\" is not valid")
                viewActions += CreateProjectViewActions.InvalidProjectNameErrorMessage
            } catch (e: Exception) {
                Timber.w(e, "Unable to create project")
                viewActions += CreateProjectViewActions.UnknownErrorMessage
            }
        }
    }

    fun dismiss() {
        viewActions += CreateProjectViewActions.Dismiss
    }
}
