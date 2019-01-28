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

package me.raatiniemi.worker.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import me.raatiniemi.worker.data.Database
import me.raatiniemi.worker.data.projects.TimeIntervalDao
import me.raatiniemi.worker.data.projects.TimeReportDao
import me.raatiniemi.worker.data.projects.projectEntity
import me.raatiniemi.worker.data.projects.timeIntervalEntity
import me.raatiniemi.worker.domain.comparator.TimeReportDateComparator
import me.raatiniemi.worker.domain.comparator.TimeReportItemComparator
import me.raatiniemi.worker.domain.model.Project
import me.raatiniemi.worker.domain.model.TimeInterval
import me.raatiniemi.worker.domain.model.TimeReportItem
import me.raatiniemi.worker.domain.repository.TimeReportRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class TimeReportRoomRepositoryTest {
    private val project = Project(1, "Name")

    private lateinit var database: Database
    private lateinit var timeReport: TimeReportDao
    private lateinit var timeIntervals: TimeIntervalDao
    private lateinit var repository: TimeReportRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, Database::class.java)
                .allowMainThreadQueries()
                .build()

        database.projects()
                .add(
                        projectEntity {
                            id = project.id ?: 0
                            name = project.name
                        }
                )
        timeReport = database.timeReport()
        timeIntervals = database.timeIntervals()
        repository = TimeReportRoomRepository(timeReport, timeIntervals)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun count_withoutTimeIntervals() {
        val expected = 0

        val actual = repository.count(1)

        assertEquals(expected, actual)
    }

    @Test
    fun count_withTimeInterval() {
        val expected = 1
        timeIntervals.add(timeIntervalEntity { })

        val actual = repository.count(1)

        assertEquals(expected, actual)
    }

    @Test
    fun count_withTimeIntervalsOnSameDay() {
        val expected = 1
        timeIntervals.add(timeIntervalEntity { })
        timeIntervals.add(timeIntervalEntity { })

        val actual = repository.count(1)

        assertEquals(expected, actual)
    }

    @Test
    fun count_withTimeIntervalsOnDifferentDays() {
        val expected = 2
        timeIntervals.add(timeIntervalEntity { })
        timeIntervals.add(timeIntervalEntity { startInMilliseconds = Date().time })

        val actual = repository.count(1)

        assertEquals(expected, actual)
    }

    @Test
    fun countNotRegistered_withoutTimeIntervals() {
        val expected = 0

        val actual = repository.countNotRegistered(1)

        assertEquals(expected, actual)
    }

    @Test
    fun countNotRegistered_withRegisteredTimeInterval() {
        val expected = 0
        timeIntervals.add(timeIntervalEntity { registered = true })

        val actual = repository.countNotRegistered(1)

        assertEquals(expected, actual)
    }

    @Test
    fun countNotRegistered_withTimeInterval() {
        val expected = 1
        timeIntervals.add(timeIntervalEntity { })

        val actual = repository.countNotRegistered(1)

        assertEquals(expected, actual)
    }

    @Test
    fun countNotRegistered_withTimeIntervalsOnSameDay() {
        val expected = 1
        timeIntervals.add(timeIntervalEntity { })
        timeIntervals.add(timeIntervalEntity { })

        val actual = repository.countNotRegistered(1)

        assertEquals(expected, actual)
    }

    @Test
    fun countNotRegistered_withRegisteredTimeIntervalOnDifferentDays() {
        val expected = 1
        timeIntervals.add(timeIntervalEntity { })
        timeIntervals.add(timeIntervalEntity {
            startInMilliseconds = Date().time
            registered = true
        })

        val actual = timeReport.countNotRegistered(1)

        assertEquals(expected, actual)
    }

    @Test
    fun countNotRegistered_withTimeIntervalsOnDifferentDays() {
        val expected = 2
        timeIntervals.add(timeIntervalEntity { })
        timeIntervals.add(timeIntervalEntity {
            startInMilliseconds = Date().time
        })

        val actual = repository.countNotRegistered(1)

        assertEquals(expected, actual)
    }

    private fun timeReportItemSet(timeIntervals: List<TimeInterval>): SortedSet<TimeReportItem> {
        return timeIntervals.map { TimeReportItem(it) }
                .toSortedSet(TimeReportItemComparator())
    }

    private fun timeReportItemSet(timeInterval: TimeInterval): SortedSet<TimeReportItem> {
        return timeReportItemSet(listOf(timeInterval))
    }

    @Test
    fun findAll_withoutTimeIntervals() {
        val actual = repository.findAll(1, 0, 10)

        assertEquals(emptyMap<Date, Set<TimeInterval>>(), actual)
    }

    @Test
    fun findAll_withoutTimeIntervalForProject() {
        database.projects().add(
                projectEntity {
                    id = 2
                    name = "Name #2"
                }
        )
        val entity = timeIntervalEntity {
            projectId = 2
            startInMilliseconds = 1
            stopInMilliseconds = 10
        }
        timeIntervals.add(entity)

        val actual = repository.findAll(1, 0, 10)

        assertEquals(emptyMap<Date, Set<TimeInterval>>(), actual)
    }

    @Test
    fun findAll_withTimeIntervalsForSameDay() {
        val ti1 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 1
            stopInMilliseconds = 10
        }
        val ti2 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 11
            stopInMilliseconds = 30
        }
        listOf(ti1, ti2).forEach { timeIntervals.add(it) }
        val timeIntervals = listOf(
                ti1.copy(id = 1).toTimeInterval(),
                ti2.copy(id = 2).toTimeInterval()
        )
        val expected = TreeMap<Date, Set<TimeReportItem>>(TimeReportDateComparator()).apply {
            put(Date(ti1.startInMilliseconds), timeReportItemSet(timeIntervals))
        }

        val actual = repository.findAll(1, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findAll_withTimeIntervalsForDifferentDays() {
        val ti1 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 1
            stopInMilliseconds = 10
        }
        val ti2 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 90000000
            stopInMilliseconds = 93000000
        }
        listOf(ti1, ti2).forEach { timeIntervals.add(it) }
        val expected = TreeMap<Date, Set<TimeReportItem>>(TimeReportDateComparator()).apply {
            put(Date(ti1.startInMilliseconds), timeReportItemSet(ti1.copy(id = 1).toTimeInterval()))
            put(Date(ti2.startInMilliseconds), timeReportItemSet(ti2.copy(id = 2).toTimeInterval()))
        }

        val actual = repository.findAll(1, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findAll_withTimeIntervalsWithPosition() {
        val ti1 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 1
            stopInMilliseconds = 10
        }
        val ti2 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 90000000
            stopInMilliseconds = 93000000
        }
        listOf(ti1, ti2).forEach { timeIntervals.add(it) }
        val expected = TreeMap<Date, Set<TimeReportItem>>(TimeReportDateComparator()).apply {
            put(Date(ti1.startInMilliseconds), timeReportItemSet(ti1.copy(id = 1).toTimeInterval()))
        }

        val actual = repository.findAll(1, 1, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findAll_withTimeIntervalsWithPageSize() {
        val ti1 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 1
            stopInMilliseconds = 10
        }
        val ti2 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 90000000
            stopInMilliseconds = 93000000
        }
        listOf(ti1, ti2).forEach { timeIntervals.add(it) }
        val expected = TreeMap<Date, Set<TimeReportItem>>(TimeReportDateComparator()).apply {
            put(Date(ti2.startInMilliseconds), timeReportItemSet(ti2.copy(id = 2).toTimeInterval()))
        }

        val actual = repository.findAll(1, 0, 1)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegistered_withoutTimeIntervals() {
        val actual = repository.findNotRegistered(1, 0, 10)

        assertEquals(emptyMap<Date, Set<TimeInterval>>(), actual)
    }

    @Test
    fun findNotRegistered_withoutTimeIntervalForProject() {
        database.projects().add(
                projectEntity {
                    id = 2
                    name = "Name #2"
                }
        )
        val entity = timeIntervalEntity {
            projectId = 2
            startInMilliseconds = 1
            stopInMilliseconds = 10
        }
        timeIntervals.add(entity)

        val actual = repository.findNotRegistered(1, 0, 10)

        assertEquals(emptyMap<Date, Set<TimeInterval>>(), actual)
    }

    @Test
    fun findNotRegistered_withTimeIntervalsForSameDay() {
        val ti1 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 1
            stopInMilliseconds = 10
        }
        val ti2 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 11
            stopInMilliseconds = 30
        }
        listOf(ti1, ti2).forEach { timeIntervals.add(it) }
        val timeIntervals = listOf(
                ti1.copy(id = 1).toTimeInterval(),
                ti2.copy(id = 2).toTimeInterval()
        )
        val expected = TreeMap<Date, Set<TimeReportItem>>(TimeReportDateComparator()).apply {
            put(Date(ti1.startInMilliseconds), timeReportItemSet(timeIntervals))
        }

        val actual = repository.findNotRegistered(1, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegistered_withTimeIntervalsForDifferentDays() {
        val ti1 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 1
            stopInMilliseconds = 10
        }
        val ti2 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 90000000
            stopInMilliseconds = 93000000
        }
        listOf(ti1, ti2).forEach { timeIntervals.add(it) }
        val expected = TreeMap<Date, Set<TimeReportItem>>(TimeReportDateComparator()).apply {
            put(Date(ti1.startInMilliseconds), timeReportItemSet(ti1.copy(id = 1).toTimeInterval()))
            put(Date(ti2.startInMilliseconds), timeReportItemSet(ti2.copy(id = 2).toTimeInterval()))
        }

        val actual = repository.findNotRegistered(1, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegistered_withTimeIntervalsWithPosition() {
        val ti1 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 1
            stopInMilliseconds = 10
        }
        val ti2 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 90000000
            stopInMilliseconds = 93000000
        }
        listOf(ti1, ti2).forEach { timeIntervals.add(it) }
        val expected = TreeMap<Date, Set<TimeReportItem>>(TimeReportDateComparator()).apply {
            put(Date(ti1.startInMilliseconds), timeReportItemSet(ti1.copy(id = 1).toTimeInterval()))
        }

        val actual = repository.findNotRegistered(1, 1, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegistered_withTimeIntervalsWithPageSize() {
        val ti1 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 1
            stopInMilliseconds = 10
        }
        val ti2 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 90000000
            stopInMilliseconds = 93000000
        }
        listOf(ti1, ti2).forEach { timeIntervals.add(it) }
        val expected = TreeMap<Date, Set<TimeReportItem>>(TimeReportDateComparator()).apply {
            put(Date(ti2.startInMilliseconds), timeReportItemSet(ti2.copy(id = 2).toTimeInterval()))
        }

        val actual = repository.findNotRegistered(1, 0, 1)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegistered_withRegisteredTimeInterval() {
        val ti1 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 1
            stopInMilliseconds = 10
            registered = true
        }
        val ti2 = timeIntervalEntity {
            projectId = 1
            startInMilliseconds = 90000000
            stopInMilliseconds = 93000000
        }
        listOf(ti1, ti2).forEach { timeIntervals.add(it) }
        val expected = TreeMap<Date, Set<TimeReportItem>>(TimeReportDateComparator()).apply {
            put(Date(ti2.startInMilliseconds), timeReportItemSet(ti2.copy(id = 2).toTimeInterval()))
        }

        val actual = repository.findNotRegistered(1, 0, 10)

        assertEquals(expected, actual)
    }
}
