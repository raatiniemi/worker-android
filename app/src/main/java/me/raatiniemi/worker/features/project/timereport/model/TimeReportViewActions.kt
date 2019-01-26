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

import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import me.raatiniemi.worker.R
import me.raatiniemi.worker.features.shared.model.ViewAction

sealed class TimeReportViewActions {
    object ShowUnableToLoadTimeReportErrorMessage : TimeReportViewActions(), ViewAction {
        override fun action(activity: FragmentActivity) {
            val snackBar = Snackbar.make(
                    activity.findViewById(android.R.id.content),
                    R.string.error_message_get_time_report,
                    Snackbar.LENGTH_SHORT
            )
            snackBar.show()
        }
    }

    data class UpdateRegistered(val results: List<TimeReportAdapterResult>) : TimeReportViewActions()

    object ShowUnableToRegisterErrorMessage : TimeReportViewActions(), ViewAction {
        override fun action(activity: FragmentActivity) {
            val snackBar = Snackbar.make(
                    activity.findViewById(android.R.id.content),
                    R.string.error_message_register_time_report,
                    Snackbar.LENGTH_SHORT
            )
            snackBar.show()
        }
    }

    data class RemoveRegistered(val results: List<TimeReportAdapterResult>) : TimeReportViewActions()

    object ShowUnableToDeleteErrorMessage : TimeReportViewActions(), ViewAction {
        override fun action(activity: FragmentActivity) {
            val snackBar = Snackbar.make(
                    activity.findViewById(android.R.id.content),
                    R.string.error_message_delete_time_report,
                    Snackbar.LENGTH_SHORT
            )
            snackBar.show()
        }
    }
}
