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

package me.raatiniemi.worker.features.projects.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_projects.*
import kotlinx.coroutines.launch
import me.raatiniemi.worker.R
import me.raatiniemi.worker.data.service.ongoing.ProjectNotificationService
import me.raatiniemi.worker.domain.model.Project
import me.raatiniemi.worker.features.project.view.ProjectActivity
import me.raatiniemi.worker.features.projects.createproject.model.CreateProjectEvent
import me.raatiniemi.worker.features.projects.model.ProjectsItem
import me.raatiniemi.worker.features.projects.model.ProjectsItemAdapterResult
import me.raatiniemi.worker.features.projects.model.ProjectsViewActions
import me.raatiniemi.worker.features.projects.viewmodel.ClockActivityViewModel
import me.raatiniemi.worker.features.projects.viewmodel.ProjectsViewModel
import me.raatiniemi.worker.features.projects.viewmodel.RefreshActiveProjectsViewModel
import me.raatiniemi.worker.features.settings.project.model.TimeSummaryStartingPointChangeEvent
import me.raatiniemi.worker.features.shared.model.OngoingNotificationActionEvent
import me.raatiniemi.worker.features.shared.model.ViewAction
import me.raatiniemi.worker.features.shared.view.CoroutineScopedFragment
import me.raatiniemi.worker.features.shared.view.adapter.SimpleListAdapter
import me.raatiniemi.worker.features.shared.view.dialog.RxAlertDialog
import me.raatiniemi.worker.util.HintedImageButtonListener
import me.raatiniemi.worker.util.KeyValueStore
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*
import kotlin.concurrent.schedule

class ProjectsFragment : CoroutineScopedFragment(), OnProjectActionListener, SimpleListAdapter.OnItemClickListener {
    private val eventBus = EventBus.getDefault()

    private val projectsViewModel: ProjectsViewModel by viewModel()
    private val clockActivityViewModel: ClockActivityViewModel by viewModel()
    private val refreshViewModel: RefreshActiveProjectsViewModel by viewModel()

    private val keyValueStore: KeyValueStore by inject()

    private var refreshActiveProjectsTimer: Timer? = null

    private lateinit var projectsAdapter: ProjectsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        eventBus.register(this)

        projectsAdapter = ProjectsAdapter(resources, this, HintedImageButtonListener(requireActivity()))
        projectsAdapter.setOnItemClickListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_projects, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvProjects.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = projectsAdapter
        }

        observeViewModel()
        loadProjectsViaViewModel()
    }

    private fun observeViewModel() {
        projectsViewModel.projects.observe(this, Observer {
            projectsAdapter.add(it)
        })

        projectsViewModel.viewActions.observeAndConsume(this, Observer {
            if (it is ViewAction) {
                it.action(requireActivity())
            }
        })

        clockActivityViewModel.viewActions.observeAndConsume(this, Observer {
            when (it) {
                is ProjectsViewActions.UpdateProject -> {
                    updateNotificationForProject(it.result.projectsItem)
                    updateProject(it.result)
                }
                is ViewAction -> it.action(requireActivity())
                else -> Timber.w("No response")
            }
        })

        projectsViewModel.restoreProject.observeAndConsume(this, Observer {
            restoreProjectAtPreviousPosition(it)

            showDeleteProjectErrorMessage()
        })

        refreshViewModel.activePositions.observe(this, Observer {
            refreshPositions(it)
        })
    }

    private fun loadProjectsViaViewModel() {
        launch {
            projectsViewModel.loadProjects()
        }
    }

    override fun onResume() {
        super.onResume()

        startRefreshTimer()
    }

    private fun startRefreshTimer() {
        cancelRefreshTimer()

        refreshActiveProjectsTimer = Timer()
        refreshActiveProjectsTimer?.schedule(Date(), 60_000) {
            refreshViewModel.projects(projectsAdapter.items)
        }
    }

    override fun onPause() {
        super.onPause()

        cancelRefreshTimer()
    }

    private fun cancelRefreshTimer() {
        refreshActiveProjectsTimer?.cancel()
        refreshActiveProjectsTimer = null
    }

    override fun onDestroy() {
        super.onDestroy()

        eventBus.unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: CreateProjectEvent) {
        addCreatedProject(event.project)
    }

    private fun addCreatedProject(project: Project) {
        val item = ProjectsItem.from(project, emptyList())
        val position = projectsAdapter.add(item)

        rvProjects.scrollToPosition(position)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: OngoingNotificationActionEvent) {
        reloadProjects()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: TimeSummaryStartingPointChangeEvent) {
        reloadProjects()
    }

    private fun reloadProjects() {
        projectsAdapter.clear()

        loadProjectsViaViewModel()
    }

    private fun refreshPositions(positions: List<Int>) {
        // Check that we have positions to refresh.
        if (positions.isEmpty()) {
            // We should never reach this code since there are supposed to be
            // checks for positions before the refreshPositions-method is called.
            Timber.w("No positions, skip refreshing projects")
            return
        }

        // Iterate and refresh every position.
        Timber.d("Refreshing %d projects", positions.size)
        for (position in positions) {
            projectsAdapter.notifyItemChanged(position)
        }
    }

    private fun updateNotificationForProject(project: ProjectsItem) {
        ProjectNotificationService.startServiceWithContext(
                requireActivity(),
                project.asProject()
        )
    }

    private fun updateProject(result: ProjectsItemAdapterResult) {
        projectsAdapter.set(result.position, result.projectsItem)
    }

    private fun deleteProjectAtPosition(position: Int) {
        projectsAdapter.remove(position)
    }

    private fun restoreProjectAtPreviousPosition(result: ProjectsItemAdapterResult) {
        projectsAdapter.add(result.position, result.projectsItem)

        rvProjects.scrollToPosition(result.position)
    }

    private fun showDeleteProjectErrorMessage() {
        Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                R.string.error_message_project_deleted,
                Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onItemClick(view: View) {
        // Retrieve the position for the project from the RecyclerView.
        val position = rvProjects.getChildAdapterPosition(view)
        if (RecyclerView.NO_POSITION == position) {
            Timber.w("Unable to retrieve project position for onItemClick")
            return
        }

        try {
            // Retrieve the project from the retrieved position.
            val item = projectsAdapter.get(position)
            val (id) = item.asProject()

            val intent = ProjectActivity.newIntent(
                    requireActivity(),
                    id
            )
            startActivity(intent)
        } catch (e: IndexOutOfBoundsException) {
            Timber.w(e, "Unable to get project position")
            Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    R.string.error_message_unable_to_find_project,
                    Snackbar.LENGTH_SHORT
            ).show()
        }

    }

    override fun onClockActivityToggle(result: ProjectsItemAdapterResult) {
        val projectsItem = result.projectsItem
        if (projectsItem.isActive) {
            // Check if clock out require confirmation.
            if (!keyValueStore.confirmClockOut()) {
                launch {
                    clockActivityViewModel.clockOut(result, Date())
                }
                return
            }

            ConfirmClockOutDialog.show(requireActivity())
                    .filter { RxAlertDialog.isPositive(it) }
                    .subscribe(
                            {
                                launch {
                                    clockActivityViewModel.clockOut(result, Date())
                                }
                            },
                            { Timber.w(it) }
                    )
            return
        }

        launch {
            clockActivityViewModel.clockIn(result, Date())
        }
    }

    override fun onClockActivityAt(result: ProjectsItemAdapterResult) {
        val projectsItem = result.projectsItem
        val fragment = ClockActivityAtFragment.newInstance(
                projectsItem
        ) { calendar ->
            if (projectsItem.isActive) {
                launch {
                    clockActivityViewModel.clockOut(result, calendar.time)
                }
                return@newInstance
            }

            launch {
                clockActivityViewModel.clockIn(result, calendar.time)
            }
        }

        childFragmentManager.beginTransaction()
                .add(fragment, FRAGMENT_CLOCK_ACTIVITY_AT_TAG)
                .commit()
    }

    override fun onDelete(result: ProjectsItemAdapterResult) {
        RemoveProjectDialog.show(requireActivity())
                .filter { RxAlertDialog.isPositive(it) }
                .subscribe(
                        {
                            deleteProjectAtPosition(result.position)

                            launch {
                                projectsViewModel.remove(result)
                            }
                        },
                        { Timber.w(it) }
                )
    }

    companion object {
        private const val FRAGMENT_CLOCK_ACTIVITY_AT_TAG = "clock activity at"
    }
}
