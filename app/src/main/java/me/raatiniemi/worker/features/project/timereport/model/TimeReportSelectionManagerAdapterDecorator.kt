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

package me.raatiniemi.worker.features.project.timereport.model

import androidx.annotation.MainThread
import androidx.recyclerview.widget.RecyclerView
import me.raatiniemi.worker.domain.model.TimeReportDay
import me.raatiniemi.worker.domain.model.TimeReportItem
import me.raatiniemi.worker.features.project.timereport.viewmodel.TimeReportSelectionManager

class TimeReportSelectionManagerAdapterDecorator(
        private val adapter: RecyclerView.Adapter<*>,
        private val selectionManager: TimeReportSelectionManager
) : TimeReportSelectionManager {
    @MainThread
    override fun state(day: TimeReportDay) = selectionManager.state(day)

    @MainThread
    override fun state(item: TimeReportItem) = selectionManager.state(item)

    @MainThread
    override fun consume(longPress: TimeReportLongPressAction): Boolean {
        return selectionManager.consume(longPress)
                .apply { adapter.notifyDataSetChanged() }
    }

    @MainThread
    override fun consume(tap: TimeReportTapAction) {
        return selectionManager.consume(tap)
                .apply { adapter.notifyDataSetChanged() }
    }
}
