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

package me.raatiniemi.worker.features.project.timereport.view

import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_time_report.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.raatiniemi.worker.R
import me.raatiniemi.worker.domain.util.DigitalHoursMinutesIntervalFormat
import me.raatiniemi.worker.domain.util.FractionIntervalFormat
import me.raatiniemi.worker.domain.util.HoursMinutesFormat
import me.raatiniemi.worker.features.project.model.ProjectHolder
import me.raatiniemi.worker.features.project.timereport.adapter.TimeReportAdapter
import me.raatiniemi.worker.features.project.timereport.model.TimeReportAction
import me.raatiniemi.worker.features.project.timereport.viewmodel.TimeReportViewModel
import me.raatiniemi.worker.features.shared.model.OngoingNotificationActionEvent
import me.raatiniemi.worker.features.shared.view.ConfirmAction
import me.raatiniemi.worker.features.shared.view.CoroutineScopedFragment
import me.raatiniemi.worker.util.KeyValueStore
import me.raatiniemi.worker.util.NullUtil.isNull
import me.raatiniemi.worker.util.SelectionListener
import me.raatiniemi.worker.util.TIME_REPORT_SUMMARY_FORMAT_FRACTION
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class TimeReportFragment : CoroutineScopedFragment(), SelectionListener {
    private val keyValueStore: KeyValueStore by inject()
    private val projectHolder: ProjectHolder by inject()

    private val vm: TimeReportViewModel by viewModel()

    private val eventBus = EventBus.getDefault()

    private lateinit var timeReportAdapter: TimeReportAdapter
    private var actionMode: ActionMode? = null

    private val hoursMinutesFormat: HoursMinutesFormat
        get() {
            val format = keyValueStore.timeReportSummaryFormat()
            return if (TIME_REPORT_SUMMARY_FORMAT_FRACTION == format) {
                FractionIntervalFormat()
            } else DigitalHoursMinutesIntervalFormat()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        eventBus.register(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_time_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeReportAdapter = TimeReportAdapter(hoursMinutesFormat, this)

        rvTimeReport.apply {
            adapter = timeReportAdapter
            layoutManager = LinearLayoutManager(requireActivity())
            setHasFixedSize(false)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        vm.timeReport.observe(this, Observer {
            timeReportAdapter.submitList(it)
        })

        vm.viewActions.observeAndConsume(this, Observer {
            it.action(requireActivity())
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        eventBus.unregister(this)
    }

    fun reloadTimeReport() {
        vm.reloadTimeReport()
    }

    override fun onSelect() {
        if (actionMode != null) {
            return
        }

        val callback = TimeReportActionModeCallback(object : TimeReportActionConsumer {
            override fun consume(action: TimeReportAction) {
                when (action) {
                    TimeReportAction.TOGGLE_REGISTERED -> toggleRegisterSelectedItems()
                    TimeReportAction.REMOVE -> confirmRemoveSelectedItems()
                    TimeReportAction.FINISH -> {
                        timeReportAdapter.deselectItems()
                        actionMode = null
                    }
                }
            }
        })
        actionMode = requireActivity().startActionMode(callback)
    }

    override fun onDeselect() {
        if (isNull(actionMode)) {
            return
        }

        if (timeReportAdapter.haveSelectedItems()) {
            return
        }

        actionMode?.finish()
    }

    private fun toggleRegisterSelectedItems() = launch {
        vm.register(timeReportAdapter.selectedItems)

        withContext(Dispatchers.Main) {
            actionMode?.finish()
        }
    }

    private fun confirmRemoveSelectedItems() = launch {
        val confirmAction = ConfirmDeleteTimeIntervalDialog.show(requireContext())
        if (ConfirmAction.YES == confirmAction) {
            vm.remove(timeReportAdapter.selectedItems)
        }

        withContext(Dispatchers.Main) {
            actionMode?.finish()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: OngoingNotificationActionEvent) {
        if (event.projectId == projectHolder.project) {
            vm.reloadTimeReport()
            return
        }

        Timber.d("No need to refresh, event is related to another project")
    }

    companion object {
        @JvmStatic
        fun newInstance() = TimeReportFragment()
    }
}
