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

package me.raatiniemi.worker.features.project.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import me.raatiniemi.worker.R
import me.raatiniemi.worker.features.project.model.ProjectHolder
import me.raatiniemi.worker.features.project.timereport.view.TimeReportFragment
import me.raatiniemi.worker.features.shared.view.activity.BaseActivity
import me.raatiniemi.worker.util.KeyValueStore
import me.raatiniemi.worker.util.NullUtil.isNull
import me.raatiniemi.worker.util.NullUtil.nonNull
import org.koin.android.ext.android.inject

class ProjectActivity : BaseActivity() {
    private val keyValueStore: KeyValueStore by inject()
    private val projectHolder: ProjectHolder by inject()

    private lateinit var timeReportFragment: TimeReportFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        projectHolder.project = intent.getLongExtra(ProjectActivity.MESSAGE_PROJECT_ID, -1)
        timeReportFragment = TimeReportFragment.newInstance()

        if (isNull(savedInstanceState)) {
            supportFragmentManager.beginTransaction()
                    .replace(
                            R.id.fragment_container,
                            timeReportFragment,
                            ProjectActivity.FRAGMENT_TIME_REPORT_TAG
                    )
                    .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.actions_project, menu)

        // Set the selected value for the option, otherwise the value will be set to default each
        // time the activity is created.
        val hideRegistered = menu.findItem(R.id.actions_project_hide_registered)
        if (nonNull(hideRegistered)) {
            hideRegistered.isChecked = keyValueStore.hideRegisteredTime()
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.actions_project_hide_registered == item.itemId) {
            handleHideRegisteredTimeChange(item)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun handleHideRegisteredTimeChange(item: MenuItem) {
        item.isChecked = !item.isChecked

        keyValueStore.setHideRegisteredTime(item.isChecked)
        timeReportFragment.refresh()
    }

    companion object {
        const val MESSAGE_PROJECT_ID = "project id"

        /**
         * Tag for the time report fragment.
         */
        private const val FRAGMENT_TIME_REPORT_TAG = "time report"

        @JvmStatic
        fun newIntent(context: Context, projectId: Long?): Intent {
            val intent = Intent(context, ProjectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra(ProjectActivity.MESSAGE_PROJECT_ID, projectId)

            return intent
        }
    }
}
