/*
 * Copyright (C) 2019 Tobias Raatiniemi
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

package me.raatiniemi.worker.data.datasource

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.raatiniemi.worker.domain.model.LoadPosition
import me.raatiniemi.worker.domain.model.LoadRange
import me.raatiniemi.worker.domain.model.LoadSize
import me.raatiniemi.worker.domain.project.model.Project
import me.raatiniemi.worker.domain.timereport.model.TimeReportWeek
import me.raatiniemi.worker.domain.timereport.usecase.CountTimeReportWeeks
import me.raatiniemi.worker.domain.timereport.usecase.FindTimeReportWeeks
import me.raatiniemi.worker.feature.projects.model.ProjectProvider
import me.raatiniemi.worker.util.CoroutineDispatchProvider
import timber.log.Timber

internal class TimeReportWeekDataSource(
    private val scope: CoroutineScope,
    private val dispatcherProvider: CoroutineDispatchProvider,
    private val projectProvider: ProjectProvider,
    private val countTimeReportWeeks: CountTimeReportWeeks,
    private val findTimeReportWeeks: FindTimeReportWeeks
) : PositionalDataSource<TimeReportWeek>() {
    private val project: Project?
        get() {
            return try {
                requireNotNull(projectProvider.value) {
                    "No project is available from `ProjectHolder`"
                }
            } catch (e: IllegalArgumentException) {
                Timber.w(e, "Unable to load time report without project")
                null
            }
        }

    private suspend fun countTotal(): Int {
        return project?.let { countTimeReportWeeks(it) } ?: 0
    }

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<TimeReportWeek>
    ) {
        scope.launch(dispatcherProvider.io()) {
            val totalCount = countTotal()
            val position = computeInitialLoadPosition(params, totalCount)
            val loadSize = computeInitialLoadSize(params, position, totalCount)

            val loadRange = LoadRange(
                LoadPosition(position),
                LoadSize(loadSize)
            )
            callback.onResult(loadData(loadRange), position, totalCount)
        }
    }

    private fun loadData(loadRange: LoadRange): List<TimeReportWeek> {
        return when (val project = this.project) {
            is Project -> findTimeReportWeeks(project, loadRange)
            else -> emptyList()
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<TimeReportWeek>) {
        scope.launch(dispatcherProvider.io()) {
            val loadRange = LoadRange(
                LoadPosition(params.startPosition),
                LoadSize(params.loadSize)
            )
            callback.onResult(loadData(loadRange))
        }
    }

    class Factory(
        private val scope: CoroutineScope,
        private val dispatcherProvider: CoroutineDispatchProvider,
        private val projectProvider: ProjectProvider,
        private val countTimeReportWeeks: CountTimeReportWeeks,
        private val findTimeReportWeeks: FindTimeReportWeeks
    ) : DataSource.Factory<Int, TimeReportWeek>() {
        override fun create(): TimeReportWeekDataSource {
            return TimeReportWeekDataSource(
                scope,
                dispatcherProvider,
                projectProvider,
                countTimeReportWeeks,
                findTimeReportWeeks
            )
        }
    }
}
