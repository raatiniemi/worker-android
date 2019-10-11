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

package me.raatiniemi.worker.domain.timereport.usecase

import me.raatiniemi.worker.domain.configuration.AppKeys
import me.raatiniemi.worker.domain.configuration.InMemoryKeyValueStore
import me.raatiniemi.worker.domain.configuration.KeyValueStore
import me.raatiniemi.worker.domain.date.plus
import me.raatiniemi.worker.domain.model.LoadPosition
import me.raatiniemi.worker.domain.model.LoadRange
import me.raatiniemi.worker.domain.model.LoadSize
import me.raatiniemi.worker.domain.project.model.android
import me.raatiniemi.worker.domain.project.model.ios
import me.raatiniemi.worker.domain.time.*
import me.raatiniemi.worker.domain.timeinterval.model.TimeIntervalId
import me.raatiniemi.worker.domain.timeinterval.model.timeInterval
import me.raatiniemi.worker.domain.timeinterval.repository.TimeIntervalInMemoryRepository
import me.raatiniemi.worker.domain.timeinterval.usecase.ClockIn
import me.raatiniemi.worker.domain.timeinterval.usecase.ClockOut
import me.raatiniemi.worker.domain.timeinterval.usecase.MarkRegisteredTime
import me.raatiniemi.worker.domain.timereport.model.TimeReportWeek
import me.raatiniemi.worker.domain.timereport.model.timeReportDay
import me.raatiniemi.worker.domain.timereport.model.timeReportWeek
import me.raatiniemi.worker.domain.timereport.repository.TimeReportInMemoryRepository
import me.raatiniemi.worker.domain.timereport.repository.TimeReportRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

@RunWith(JUnit4::class)
class FindTimeReportWeeksTest {
    private lateinit var clockIn: ClockIn
    private lateinit var clockOut: ClockOut
    private lateinit var markRegisteredTime: MarkRegisteredTime

    private lateinit var keyValueStore: KeyValueStore
    private lateinit var timeReportRepository: TimeReportRepository

    private lateinit var findTimeReportWeeks: FindTimeReportWeeks

    @Before
    fun setUp() {
        val timeIntervalRepository = TimeIntervalInMemoryRepository()
        clockIn = ClockIn(timeIntervalRepository)
        clockOut = ClockOut(timeIntervalRepository)
        markRegisteredTime = MarkRegisteredTime(timeIntervalRepository)

        keyValueStore = InMemoryKeyValueStore()
        timeReportRepository = TimeReportInMemoryRepository(timeIntervalRepository)

        findTimeReportWeeks = FindTimeReportWeeks(keyValueStore, timeReportRepository)
    }

    // When not hiding registered time

    @Test
    fun `find time report weeks without time intervals`() {
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))
        val expected = emptyList<TimeReportWeek>()

        val actual = findTimeReportWeeks(android, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time report weeks without time interval for project`() {
        val startOfDay = setToStartOfDay(Milliseconds.now)
        clockIn(android, date = Date(startOfDay.value))
        clockOut(android, date = Date(startOfDay.value) + 10.minutes)
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))
        val expected = emptyList<TimeReportWeek>()

        val actual = findTimeReportWeeks(ios, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time report weeks with time interval`() {
        val startOfDay = setToStartOfDay(Milliseconds.now)
        clockIn(android, date = Date(startOfDay.value))
        clockOut(android, date = Date(startOfDay.value) + 10.minutes)
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))
        val expected = listOf(
            timeReportWeek(
                startOfDay,
                listOf(
                    timeReportDay(
                        Date(startOfDay.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(1)
                                builder.start = startOfDay
                                builder.stop = startOfDay + 10.minutes
                            }
                        )
                    )
                )
            )
        )

        val actual = findTimeReportWeeks(android, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time report weeks with time intervals`() {
        val startOfDay = setToStartOfDay(Milliseconds.now)
        clockIn(android, date = Date(startOfDay.value))
        clockOut(android, date = Date(startOfDay.value) + 10.minutes)
        clockIn(android, date = Date(startOfDay.value) + 20.minutes)
        clockOut(android, date = Date(startOfDay.value) + 30.minutes)
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))
        val expected = listOf(
            timeReportWeek(
                startOfDay,
                listOf(
                    timeReportDay(
                        Date(startOfDay.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(2)
                                builder.start = startOfDay + 20.minutes
                                builder.stop = startOfDay + 30.minutes
                            },
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(1)
                                builder.start = startOfDay
                                builder.stop = startOfDay + 10.minutes
                            }
                        )
                    )
                )
            )
        )

        val actual = findTimeReportWeeks(android, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time report weeks with time intervals within same week`() {
        val startOfWeek = setToStartOfWeek(Milliseconds.now)
        val endOfWeek = setToEndOfWeek(startOfWeek)
        clockIn(android, date = Date(startOfWeek.value))
        clockOut(android, date = Date(startOfWeek.value) + 10.minutes)
        clockIn(android, date = Date(endOfWeek.value))
        clockOut(android, date = Date(endOfWeek.value) + 10.minutes)
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))
        val expected = listOf(
            timeReportWeek(
                startOfWeek,
                listOf(
                    timeReportDay(
                        Date(endOfWeek.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(2)
                                builder.start = endOfWeek
                                builder.stop = endOfWeek + 10.minutes
                            }
                        )
                    ),
                    timeReportDay(
                        Date(startOfWeek.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(1)
                                builder.start = startOfWeek
                                builder.stop = startOfWeek + 10.minutes
                            }
                        )
                    )
                )
            )
        )

        val actual = findTimeReportWeeks(android, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time report weeks with time intervals in different weeks`() {
        val startOfWeek = setToStartOfWeek(Milliseconds.now)
        val nextWeek = startOfWeek + 1.weeks
        clockIn(android, date = Date(startOfWeek.value))
        clockOut(android, date = Date(startOfWeek.value) + 10.minutes)
        clockIn(android, date = Date(nextWeek.value))
        clockOut(android, date = Date(nextWeek.value) + 10.minutes)
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))
        val expected = listOf(
            timeReportWeek(
                nextWeek,
                listOf(
                    timeReportDay(
                        Date(nextWeek.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(2)
                                builder.start = nextWeek
                                builder.stop = nextWeek + 10.minutes
                            }
                        )
                    )
                )
            ),
            timeReportWeek(
                startOfWeek,
                listOf(
                    timeReportDay(
                        Date(startOfWeek.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(1)
                                builder.start = startOfWeek
                                builder.stop = startOfWeek + 10.minutes
                            }
                        )
                    )
                )
            )
        )

        val actual = findTimeReportWeeks(android, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time report weeks with registered time interval`() {
        val startOfWeek = setToStartOfWeek(Milliseconds.now)
        clockIn(android, date = Date(startOfWeek.value))
        clockOut(android, date = Date(startOfWeek.value) + 10.minutes)
            .also { timeInterval ->
                markRegisteredTime(listOf(timeInterval))
            }
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))
        val expected = listOf(
            timeReportWeek(
                startOfWeek,
                listOf(
                    timeReportDay(
                        Date(startOfWeek.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(1)
                                builder.start = startOfWeek
                                builder.stop = startOfWeek + 10.minutes
                                builder.isRegistered = true
                            }
                        )
                    )
                )
            )
        )

        val actual = findTimeReportWeeks(android, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time report weeks when excluding by load position`() {
        val startOfWeek = setToStartOfWeek(Milliseconds.now)
        val nextWeek = startOfWeek + 1.weeks
        clockIn(android, date = Date(startOfWeek.value))
        clockOut(android, date = Date(startOfWeek.value) + 10.minutes)
        clockIn(android, date = Date(startOfWeek.value) + 20.minutes)
        clockOut(android, date = Date(startOfWeek.value) + 30.minutes)
        clockIn(android, date = Date(nextWeek.value))
        clockOut(android, date = Date(nextWeek.value) + 10.minutes)
        val loadRange = LoadRange(LoadPosition(1), LoadSize(10))
        val expected = listOf(
            timeReportWeek(
                startOfWeek,
                listOf(
                    timeReportDay(
                        Date(startOfWeek.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(2)
                                builder.start = startOfWeek + 20.minutes
                                builder.stop = startOfWeek + 30.minutes
                            },
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(1)
                                builder.start = startOfWeek
                                builder.stop = startOfWeek + 10.minutes
                            }
                        )
                    )
                )
            )
        )

        val actual = findTimeReportWeeks(android, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time report weeks when excluding by load size`() {
        val startOfWeek = setToStartOfWeek(Milliseconds.now)
        val nextWeek = startOfWeek + 1.weeks
        clockIn(android, date = Date(startOfWeek.value))
        clockOut(android, date = Date(startOfWeek.value) + 10.minutes)
        clockIn(android, date = Date(startOfWeek.value) + 20.minutes)
        clockOut(android, date = Date(startOfWeek.value) + 30.minutes)
        clockIn(android, date = Date(nextWeek.value))
        clockOut(android, date = Date(nextWeek.value) + 10.minutes)
        val loadRange = LoadRange(LoadPosition(0), LoadSize(1))
        val expected = listOf(
            timeReportWeek(
                nextWeek,
                listOf(
                    timeReportDay(
                        Date(nextWeek.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(3)
                                builder.start = nextWeek
                                builder.stop = nextWeek + 10.minutes
                            }
                        )
                    )
                )
            )
        )

        val actual = findTimeReportWeeks(android, loadRange)

        assertEquals(expected, actual)
    }

    // When hiding registered time

    @Test
    fun `find time report weeks when hiding registered time without time intervals`() {
        keyValueStore.set(AppKeys.HIDE_REGISTERED_TIME, true)
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))
        val expected = emptyList<TimeReportWeek>()

        val actual = findTimeReportWeeks(android, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time report weeks when hiding registered time without time interval for project`() {
        keyValueStore.set(AppKeys.HIDE_REGISTERED_TIME, true)
        val startOfDay = setToStartOfDay(Milliseconds.now)
        clockIn(android, date = Date(startOfDay.value))
        clockOut(android, date = Date(startOfDay.value) + 10.minutes)
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))
        val expected = emptyList<TimeReportWeek>()

        val actual = findTimeReportWeeks(ios, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time report weeks when hiding registered time with time interval`() {
        keyValueStore.set(AppKeys.HIDE_REGISTERED_TIME, true)
        val startOfDay = setToStartOfDay(Milliseconds.now)
        clockIn(android, date = Date(startOfDay.value))
        clockOut(android, date = Date(startOfDay.value) + 10.minutes)
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))
        val expected = listOf(
            timeReportWeek(
                startOfDay,
                listOf(
                    timeReportDay(
                        Date(startOfDay.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(1)
                                builder.start = startOfDay
                                builder.stop = startOfDay + 10.minutes
                            }
                        )
                    )
                )
            )
        )

        val actual = findTimeReportWeeks(android, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time report weeks when hiding registered time with time intervals`() {
        keyValueStore.set(AppKeys.HIDE_REGISTERED_TIME, true)
        val startOfDay = setToStartOfDay(Milliseconds.now)
        clockIn(android, date = Date(startOfDay.value))
        clockOut(android, date = Date(startOfDay.value) + 10.minutes)
        clockIn(android, date = Date(startOfDay.value) + 20.minutes)
        clockOut(android, date = Date(startOfDay.value) + 30.minutes)
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))
        val expected = listOf(
            timeReportWeek(
                startOfDay,
                listOf(
                    timeReportDay(
                        Date(startOfDay.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(2)
                                builder.start = startOfDay + 20.minutes
                                builder.stop = startOfDay + 30.minutes
                            },
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(1)
                                builder.start = startOfDay
                                builder.stop = startOfDay + 10.minutes
                            }
                        )
                    )
                )
            )
        )

        val actual = findTimeReportWeeks(android, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time report weeks when hiding registered time with time intervals within same week`() {
        keyValueStore.set(AppKeys.HIDE_REGISTERED_TIME, true)
        val startOfWeek = setToStartOfWeek(Milliseconds.now)
        val endOfWeek = setToEndOfWeek(startOfWeek)
        clockIn(android, date = Date(startOfWeek.value))
        clockOut(android, date = Date(startOfWeek.value) + 10.minutes)
        clockIn(android, date = Date(endOfWeek.value))
        clockOut(android, date = Date(endOfWeek.value) + 10.minutes)
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))
        val expected = listOf(
            timeReportWeek(
                startOfWeek,
                listOf(
                    timeReportDay(
                        Date(endOfWeek.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(2)
                                builder.start = endOfWeek
                                builder.stop = endOfWeek + 10.minutes
                            }
                        )
                    ),
                    timeReportDay(
                        Date(startOfWeek.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(1)
                                builder.start = startOfWeek
                                builder.stop = startOfWeek + 10.minutes
                            }
                        )
                    )
                )
            )
        )

        val actual = findTimeReportWeeks(android, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time report weeks when hiding registered time with time intervals in different weeks`() {
        keyValueStore.set(AppKeys.HIDE_REGISTERED_TIME, true)
        val startOfWeek = setToStartOfWeek(Milliseconds.now)
        val nextWeek = startOfWeek + 1.weeks
        clockIn(android, date = Date(startOfWeek.value))
        clockOut(android, date = Date(startOfWeek.value) + 10.minutes)
        clockIn(android, date = Date(nextWeek.value))
        clockOut(android, date = Date(nextWeek.value) + 10.minutes)
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))
        val expected = listOf(
            timeReportWeek(
                nextWeek,
                listOf(
                    timeReportDay(
                        Date(nextWeek.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(2)
                                builder.start = nextWeek
                                builder.stop = nextWeek + 10.minutes
                            }
                        )
                    )
                )
            ),
            timeReportWeek(
                startOfWeek,
                listOf(
                    timeReportDay(
                        Date(startOfWeek.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(1)
                                builder.start = startOfWeek
                                builder.stop = startOfWeek + 10.minutes
                            }
                        )
                    )
                )
            )
        )

        val actual = findTimeReportWeeks(android, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time report weeks when hiding registered time with registered time interval`() {
        keyValueStore.set(AppKeys.HIDE_REGISTERED_TIME, true)
        val startOfWeek = setToStartOfWeek(Milliseconds.now)
        clockIn(android, date = Date(startOfWeek.value))
        clockOut(android, date = Date(startOfWeek.value) + 10.minutes)
            .also { timeInterval ->
                markRegisteredTime(listOf(timeInterval))
            }
        val loadRange = LoadRange(LoadPosition(0), LoadSize(10))
        val expected = emptyList<TimeReportWeek>()

        val actual = findTimeReportWeeks(android, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time report weeks when hiding registered time when excluding by load position`() {
        keyValueStore.set(AppKeys.HIDE_REGISTERED_TIME, true)
        val startOfWeek = setToStartOfWeek(Milliseconds.now)
        val nextWeek = startOfWeek + 1.weeks
        clockIn(android, date = Date(startOfWeek.value))
        clockOut(android, date = Date(startOfWeek.value) + 10.minutes)
        clockIn(android, date = Date(startOfWeek.value) + 20.minutes)
        clockOut(android, date = Date(startOfWeek.value) + 30.minutes)
        clockIn(android, date = Date(nextWeek.value))
        clockOut(android, date = Date(nextWeek.value) + 10.minutes)
        val loadRange = LoadRange(LoadPosition(1), LoadSize(10))
        val expected = listOf(
            timeReportWeek(
                startOfWeek,
                listOf(
                    timeReportDay(
                        Date(startOfWeek.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(2)
                                builder.start = startOfWeek + 20.minutes
                                builder.stop = startOfWeek + 30.minutes
                            },
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(1)
                                builder.start = startOfWeek
                                builder.stop = startOfWeek + 10.minutes
                            }
                        )
                    )
                )
            )
        )

        val actual = findTimeReportWeeks(android, loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun `find time report weeks when hiding registered time when excluding by load size`() {
        keyValueStore.set(AppKeys.HIDE_REGISTERED_TIME, true)
        val startOfWeek = setToStartOfWeek(Milliseconds.now)
        val nextWeek = startOfWeek + 1.weeks
        clockIn(android, date = Date(startOfWeek.value))
        clockOut(android, date = Date(startOfWeek.value) + 10.minutes)
        clockIn(android, date = Date(startOfWeek.value) + 20.minutes)
        clockOut(android, date = Date(startOfWeek.value) + 30.minutes)
        clockIn(android, date = Date(nextWeek.value))
        clockOut(android, date = Date(nextWeek.value) + 10.minutes)
        val loadRange = LoadRange(LoadPosition(0), LoadSize(1))
        val expected = listOf(
            timeReportWeek(
                nextWeek,
                listOf(
                    timeReportDay(
                        Date(nextWeek.value),
                        listOf(
                            timeInterval(android.id) { builder ->
                                builder.id = TimeIntervalId(3)
                                builder.start = nextWeek
                                builder.stop = nextWeek + 10.minutes
                            }
                        )
                    )
                )
            )
        )

        val actual = findTimeReportWeeks(android, loadRange)

        assertEquals(expected, actual)
    }
}
