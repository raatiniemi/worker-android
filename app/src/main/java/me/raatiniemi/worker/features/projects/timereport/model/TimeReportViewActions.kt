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

package me.raatiniemi.worker.features.projects.timereport.model

import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import me.raatiniemi.worker.R
import me.raatiniemi.worker.features.projects.timereport.adapter.TimeReportAdapter
import me.raatiniemi.worker.features.shared.model.ActivityViewAction

internal sealed class TimeReportViewActions {
    data class RefreshTimeReportDays(private val positions: List<Int>) : TimeReportViewActions() {
        fun action(adapter: TimeReportAdapter) {
            positions.forEach { adapter.notifyItemChanged(it) }
        }
    }

    object ShowUnableToRegisterErrorMessage : TimeReportViewActions(), ActivityViewAction {
        override fun action(activity: FragmentActivity) {
            val snackBar = Snackbar.make(
                activity.findViewById(android.R.id.content),
                R.string.projects_time_report_unable_to_register_message,
                Snackbar.LENGTH_SHORT
            )
            snackBar.show()
        }
    }

    object ShowUnableToDeleteErrorMessage : TimeReportViewActions(), ActivityViewAction {
        override fun action(activity: FragmentActivity) {
            val snackBar = Snackbar.make(
                activity.findViewById(android.R.id.content),
                R.string.projects_time_report_unable_to_delete_message,
                Snackbar.LENGTH_SHORT
            )
            snackBar.show()
        }
    }
}
