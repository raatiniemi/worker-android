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

package me.raatiniemi.worker.domain.interactor

import me.raatiniemi.worker.domain.date.hours
import me.raatiniemi.worker.domain.date.minutes
import me.raatiniemi.worker.domain.model.*
import me.raatiniemi.worker.domain.repository.*
import me.raatiniemi.worker.util.AppKeys
import me.raatiniemi.worker.util.InMemoryKeyValueStore
import me.raatiniemi.worker.util.KeyValueStore
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TimeReportsKtTest {
    private val project = Project(1, projectName("Project name"))

    private lateinit var keyValueStore: KeyValueStore
    private lateinit var timeIntervalRepository: TimeIntervalRepository
    private lateinit var repository: TimeReportRepository

    private lateinit var countTimeReports: CountTimeReports
    private lateinit var findTimeReports: FindTimeReports

    @Before
    fun setUp() {
        keyValueStore = InMemoryKeyValueStore()
        timeIntervalRepository = TimeIntervalInMemoryRepository()
        repository = TimeReportInMemoryRepository(timeIntervalRepository)

        countTimeReports = countTimeReports(keyValueStore, repository)
        findTimeReports = findTimeReports(keyValueStore, repository)
    }

    @Test
    fun `count time reports without time intervals`() {
        val expected = 0

        val actual = countTimeReports(project)

        assertEquals(expected, actual)
    }

    @Test
    fun `count time reports with unregistered time interval`() {
        val expected = 1
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds(1)
            }
        )

        val actual = countTimeReports(project)

        assertEquals(expected, actual)
    }

    @Test
    fun `count time reports with unregistered time intervals on same day`() {
        val expected = 1
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds(1)
            }
        )
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds(1)
            }
        )

        val actual = countTimeReports(project)

        assertEquals(expected, actual)
    }

    @Test
    fun `count time reports with unregistered time intervals on different days`() {
        val expected = 2
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now - 25.hours
            }
        )
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
            }
        )

        val actual = countTimeReports(project)

        assertEquals(expected, actual)
    }

    @Test
    fun `count time reports with registered time interval`() {
        val expected = 1
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
                isRegistered = true
            }
        )

        val actual = countTimeReports(project)

        assertEquals(expected, actual)
    }

    @Test
    fun `count time reports with registered time interval when hiding registered time`() {
        val expected = 0
        keyValueStore.set(AppKeys.HIDE_REGISTERED_TIME, true)
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
                isRegistered = true
            }
        )

        val actual = countTimeReports(project)

        assertEquals(expected, actual)
    }

    @Test
    fun `count time reports with registered time intervals on same day when hiding registered time`() {
        val expected = 0
        keyValueStore.set(AppKeys.HIDE_REGISTERED_TIME, true)
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
                isRegistered = true
            }
        )
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
                isRegistered = true
            }
        )

        val actual = countTimeReports(project)

        assertEquals(expected, actual)
    }

    @Test
    fun `count time reports with registered time intervals on different days when hiding registered time`() {
        val expected = 0
        keyValueStore.set(AppKeys.HIDE_REGISTERED_TIME, true)
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now - 25.hours
                isRegistered = true
            }
        )
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
                isRegistered = true
            }
        )

        val actual = countTimeReports(project)

        assertEquals(expected, actual)
    }

    @Test
    fun `count time reports with time intervals on same day`() {
        val expected = 1
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds(1)
            }
        )
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds(1)
                isRegistered = true
            }
        )

        val actual = countTimeReports(project)

        assertEquals(expected, actual)
    }

    @Test
    fun `count time reports with time intervals on different days`() {
        val expected = 2
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now - 25.hours
            }
        )
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
                isRegistered = true
            }
        )

        val actual = countTimeReports(project)

        assertEquals(expected, actual)
    }

    @Test
    fun `count time reports with time intervals on same day when hiding registered time`() {
        val expected = 1
        keyValueStore.set(AppKeys.HIDE_REGISTERED_TIME, true)
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds(1)
            }
        )
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds(1)
                isRegistered = true
            }
        )

        val actual = countTimeReports(project)

        assertEquals(expected, actual)
    }

    @Test
    fun `count time reports with time intervals on different days when hiding registered time`() {
        val expected = 1
        keyValueStore.set(AppKeys.HIDE_REGISTERED_TIME, true)
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds(1)
            }
        )
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
                isRegistered = true
            }
        )

        val actual = countTimeReports(project)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time reports without time intervals`() {
        val expected = emptyList<TimeReportDay>()
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))

        val actual = findTimeReports(project, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time reports with unregistered time interval`() {
        val timeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds(1)
            }
        )
        val expected = listOf(
            TimeReportDay(
                resetToStartOfDay(timeInterval.start),
                listOf(
                    TimeReportItem(timeInterval)
                )
            )
        )
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))

        val actual = findTimeReports(project, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time reports with unregistered time intervals on same day`() {
        val firstTimeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds(1)
            }
        )
        val secondTimeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds(11)
            }
        )
        val expected = listOf(
            TimeReportDay(
                resetToStartOfDay(firstTimeInterval.start),
                listOf(
                    TimeReportItem(secondTimeInterval),
                    TimeReportItem(firstTimeInterval)
                )
            )
        )
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))

        val actual = findTimeReports(project, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time reports with unregistered time intervals on different days`() {
        val firstTimeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now - 25.hours
            }
        )
        val secondTimeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
            }
        )
        val expected = listOf(
            TimeReportDay(
                resetToStartOfDay(secondTimeInterval.start),
                listOf(
                    TimeReportItem(secondTimeInterval)
                )
            ),
            TimeReportDay(
                resetToStartOfDay(firstTimeInterval.start),
                listOf(
                    TimeReportItem(firstTimeInterval)
                )
            )
        )
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))

        val actual = findTimeReports(project, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time reports with registered time interval`() {
        val timeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
                isRegistered = true
            }
        )
        val expected = listOf(
            TimeReportDay(
                resetToStartOfDay(timeInterval.start),
                listOf(
                    TimeReportItem(timeInterval)
                )
            )
        )
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))

        val actual = findTimeReports(project, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time reports with registered time intervals on same day`() {
        val firstTimeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
                isRegistered = true
            }
        )
        val secondTimeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now + 1.minutes
                isRegistered = true
            }
        )
        val expected = listOf(
            TimeReportDay(
                resetToStartOfDay(firstTimeInterval.start),
                listOf(
                    TimeReportItem(secondTimeInterval),
                    TimeReportItem(firstTimeInterval)
                )
            )
        )
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))

        val actual = findTimeReports(project, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time reports with registered time intervals on different days`() {
        val firstTimeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now - 25.hours
                isRegistered = true
            }
        )
        val secondTimeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
                isRegistered = true
            }
        )
        val expected = listOf(
            TimeReportDay(
                resetToStartOfDay(secondTimeInterval.start),
                listOf(
                    TimeReportItem(secondTimeInterval)
                )
            ),
            TimeReportDay(
                resetToStartOfDay(firstTimeInterval.start),
                listOf(
                    TimeReportItem(firstTimeInterval)
                )
            )
        )
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))

        val actual = findTimeReports(project, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time reports with time intervals on same day`() {
        val firstTimeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds(1)
            }
        )
        val secondTimeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds(11)
                isRegistered = true
            }
        )
        val expected = listOf(
            TimeReportDay(
                resetToStartOfDay(firstTimeInterval.start),
                listOf(
                    TimeReportItem(secondTimeInterval),
                    TimeReportItem(firstTimeInterval)
                )
            )
        )
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))

        val actual = findTimeReports(project, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time reports with time intervals on different days`() {
        val firstTimeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now - 25.hours
            }
        )
        val secondTimeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
                isRegistered = true
            }
        )
        val expected = listOf(
            TimeReportDay(
                resetToStartOfDay(secondTimeInterval.start),
                listOf(
                    TimeReportItem(secondTimeInterval)
                )
            ),
            TimeReportDay(
                resetToStartOfDay(firstTimeInterval.start),
                listOf(
                    TimeReportItem(firstTimeInterval)
                )
            )
        )
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))

        val actual = findTimeReports(project, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time reports with time intervals on same day when hiding registered time`() {
        keyValueStore.set(AppKeys.HIDE_REGISTERED_TIME, true)
        val firstTimeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
            }
        )
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
                isRegistered = true
            }
        )
        val expected = listOf(
            TimeReportDay(
                resetToStartOfDay(firstTimeInterval.start),
                listOf(
                    TimeReportItem(firstTimeInterval)
                )
            )
        )
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))

        val actual = findTimeReports(project, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time reports with time intervals on different days when hiding registered time`() {
        keyValueStore.set(AppKeys.HIDE_REGISTERED_TIME, true)
        val firstTimeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now - 25.hours
            }
        )
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
                isRegistered = true
            }
        )
        val expected = listOf(
            TimeReportDay(
                resetToStartOfDay(firstTimeInterval.start),
                listOf(
                    TimeReportItem(firstTimeInterval)
                )
            )
        )
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))

        val actual = findTimeReports(project, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time reports with time intervals filter using position`() {
        val firstTimeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now - 25.hours
            }
        )
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
            }
        )
        val expected = listOf(
            TimeReportDay(
                resetToStartOfDay(firstTimeInterval.start),
                listOf(
                    TimeReportItem(firstTimeInterval)
                )
            )
        )
        val loadRange = LoadRange(LoadPosition(1), LoadSize(10))

        val actual = findTimeReports(project, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time reports with time intervals filter using page size`() {
        timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now - 25.hours
            }
        )
        val secondTimeInterval = timeIntervalRepository.add(
            newTimeInterval {
                start = Milliseconds.now
            }
        )
        val expected = listOf(
            TimeReportDay(
                resetToStartOfDay(secondTimeInterval.start),
                listOf(
                    TimeReportItem(secondTimeInterval)
                )
            )
        )
        val loadRange = LoadRange(LoadPosition(0), LoadSize(1))

        val actual = findTimeReports(project, loadRange)

        assertEquals(expected, actual)
    }
}
