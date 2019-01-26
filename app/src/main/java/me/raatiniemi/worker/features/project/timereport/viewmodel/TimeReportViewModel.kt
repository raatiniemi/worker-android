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

package me.raatiniemi.worker.features.project.timereport.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.raatiniemi.worker.domain.interactor.GetTimeReport
import me.raatiniemi.worker.features.project.timereport.model.TimeReportGroup
import me.raatiniemi.worker.features.project.timereport.model.TimeReportViewActions
import me.raatiniemi.worker.features.shared.model.ConsumableLiveData
import me.raatiniemi.worker.util.AppKeys
import me.raatiniemi.worker.util.KeyValueStore

class TimeReportViewModel internal constructor(
        private val keyValueStore: KeyValueStore,
        private val getTimeReport: GetTimeReport
) : ViewModel() {
    private val shouldHideRegisteredTime: Boolean
        get() = keyValueStore.bool(AppKeys.HIDE_REGISTERED_TIME.rawValue, false)

    private val _timeReport = MutableLiveData<List<TimeReportGroup>>()
    val timeReport: LiveData<List<TimeReportGroup>> = _timeReport

    val viewActions = ConsumableLiveData<TimeReportViewActions>()

    suspend fun fetch(id: Long, offset: Int) = withContext(Dispatchers.IO) {
        try {
            val items = getTimeReport(id, offset, shouldHideRegisteredTime)
                    .entries
                    .map { TimeReportGroup.build(it.key, it.value) }

            _timeReport.postValue(items)
        } catch (e: Exception) {
            viewActions.postValue(TimeReportViewActions.ShowUnableToLoadTimeReportErrorMessage)
        }
    }
}
