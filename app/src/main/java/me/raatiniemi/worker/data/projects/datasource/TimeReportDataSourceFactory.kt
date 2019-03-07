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

package me.raatiniemi.worker.data.projects.datasource

import androidx.paging.DataSource
import me.raatiniemi.worker.domain.model.TimeReportDay
import me.raatiniemi.worker.domain.repository.TimeReportRepository
import me.raatiniemi.worker.features.project.model.ProjectHolder
import me.raatiniemi.worker.util.KeyValueStore

internal class TimeReportDataSourceFactory(
        private val projectHolder: ProjectHolder,
        val keyValueStore: KeyValueStore,
        val repository: TimeReportRepository
) : DataSource.Factory<Int, TimeReportDay>() {
    override fun create() = TimeReportDataSource(projectHolder, keyValueStore, repository)
}
