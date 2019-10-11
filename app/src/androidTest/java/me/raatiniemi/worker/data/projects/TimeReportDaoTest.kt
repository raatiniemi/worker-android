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

package me.raatiniemi.worker.data.projects

import androidx.test.ext.junit.runners.AndroidJUnit4
import me.raatiniemi.worker.domain.project.model.android
import me.raatiniemi.worker.domain.project.model.ios
import me.raatiniemi.worker.domain.time.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

private val timeZone = TimeZone.getTimeZone("UTC")

@RunWith(AndroidJUnit4::class)
class TimeReportDaoTest : BaseDaoTest() {
    @Before
    override fun setUp() {
        super.setUp()

        projects.add(projectEntity())
    }

    // Count weeks

    @Test
    fun countWeeks_withoutTimeIntervals() {
        val expected = 0

        val actual = timeReport.countWeeks(android.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun countWeeks_withoutTimeIntervalForProject() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value
                stopInMilliseconds = startOfDay.value + 10.minutes
            }
        )
        val expected = 0

        val actual = timeReport.countWeeks(ios.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun countWeeks_withTimeInterval() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value
                stopInMilliseconds = startOfDay.value + 10.minutes
            }
        )
        val expected = 1

        val actual = timeReport.countWeeks(android.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun countWeeks_withTimeIntervals() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value
                stopInMilliseconds = startOfDay.value + 10.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value + 20.minutes
                stopInMilliseconds = startOfDay.value + 30.minutes
            }
        )
        val expected = 1

        val actual = timeReport.countWeeks(android.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun countWeeks_withTimeIntervalsWithinSameWeek() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        val startOfWeek = setToStartOfWeek(startOfDay, timeZone)
        val endOfWeek = setToEndOfWeek(startOfDay, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfWeek.value
                stopInMilliseconds = startOfWeek.value + 10.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = endOfWeek.value
                stopInMilliseconds = endOfWeek.value + 10.minutes
            }
        )
        val expected = 1

        val actual = timeReport.countWeeks(android.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun countWeeks_withTimeIntervalsInDifferentWeeks() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        val startOfWeek = setToStartOfWeek(startOfDay, timeZone)
        val nextWeek = setToEndOfWeek(startOfDay, timeZone) + 1.weeks
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfWeek.value
                stopInMilliseconds = startOfWeek.value + 10.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = nextWeek.value
                startInMilliseconds = nextWeek.value + 10.minutes
            }
        )
        val expected = 2

        val actual = timeReport.countWeeks(android.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun countWeeks_withRegisteredTimeInterval() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value
                stopInMilliseconds = startOfDay.value + 10.minutes
                registered = true
            }
        )
        val expected = 1

        val actual = timeReport.countWeeks(android.id.value)

        assertEquals(expected, actual)
    }

    // Count not registered weeks

    @Test
    fun countNotRegisteredWeeks_withoutTimeIntervals() {
        val expected = 0

        val actual = timeReport.countNotRegisteredWeeks(android.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun countNotRegisteredWeeks_withoutTimeIntervalForProject() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value
                stopInMilliseconds = startOfDay.value + 10.minutes
            }
        )
        val expected = 0

        val actual = timeReport.countNotRegisteredWeeks(ios.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun countNotRegisteredWeeks_withTimeInterval() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value
                stopInMilliseconds = startOfDay.value + 10.minutes
            }
        )
        val expected = 1

        val actual = timeReport.countNotRegisteredWeeks(android.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun countNotRegisteredWeeks_withTimeIntervals() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value
                stopInMilliseconds = startOfDay.value + 10.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value + 20.minutes
                stopInMilliseconds = startOfDay.value + 30.minutes
            }
        )
        val expected = 1

        val actual = timeReport.countNotRegisteredWeeks(android.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun countNotRegisteredWeeks_withTimeIntervalsWithinSameWeek() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        val startOfWeek = setToStartOfWeek(startOfDay, timeZone)
        val endOfWeek = setToEndOfWeek(startOfDay, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfWeek.value
                stopInMilliseconds = startOfWeek.value + 10.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = endOfWeek.value
                stopInMilliseconds = endOfWeek.value + 10.minutes
            }
        )
        val expected = 1

        val actual = timeReport.countNotRegisteredWeeks(android.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun countNotRegisteredWeeks_withTimeIntervalsInDifferentWeeks() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        val startOfWeek = setToStartOfWeek(startOfDay, timeZone)
        val nextWeek = setToEndOfWeek(startOfDay, timeZone) + 1.weeks
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfWeek.value
                stopInMilliseconds = startOfWeek.value + 10.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = nextWeek.value
                startInMilliseconds = nextWeek.value + 10.minutes
            }
        )
        val expected = 2

        val actual = timeReport.countNotRegisteredWeeks(android.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun countNotRegisteredWeeks_withRegisteredTimeInterval() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value
                stopInMilliseconds = startOfDay.value + 10.minutes
                registered = true
            }
        )
        val expected = 0

        val actual = timeReport.countNotRegisteredWeeks(android.id.value)

        assertEquals(expected, actual)
    }

    // Count

    @Test
    fun count_withoutTimeIntervals() {
        val expected = 0

        val actual = timeReport.count(1)

        assertEquals(expected, actual)
    }

    @Test
    fun count_withTimeInterval() {
        val expected = 1
        timeIntervals.add(timeIntervalEntity { })

        val actual = timeReport.count(1)

        assertEquals(expected, actual)
    }

    @Test
    fun count_withTimeIntervalsOnSameDay() {
        val expected = 1
        timeIntervals.add(timeIntervalEntity { })
        timeIntervals.add(timeIntervalEntity { })

        val actual = timeReport.count(1)

        assertEquals(expected, actual)
    }

    @Test
    fun count_withTimeIntervalsOnDifferentDays() {
        val expected = 2
        timeIntervals.add(timeIntervalEntity { })
        timeIntervals.add(timeIntervalEntity {
            startInMilliseconds = Date().time
        })

        val actual = timeReport.count(1)

        assertEquals(expected, actual)
    }

    // Count not registered

    @Test
    fun countNotRegistered_withoutTimeIntervals() {
        val expected = 0

        val actual = timeReport.countNotRegistered(1)

        assertEquals(expected, actual)
    }

    @Test
    fun countNotRegistered_withTimeInterval() {
        val expected = 1
        timeIntervals.add(timeIntervalEntity { })

        val actual = timeReport.countNotRegistered(1)

        assertEquals(expected, actual)
    }

    @Test
    fun countNotRegistered_withTimeIntervalsOnSameDay() {
        val expected = 1
        timeIntervals.add(timeIntervalEntity { })
        timeIntervals.add(timeIntervalEntity { })

        val actual = timeReport.countNotRegistered(1)

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

        val actual = timeReport.countNotRegistered(1)

        assertEquals(expected, actual)
    }

    // Find weeks

    @Test
    fun findWeeks_withoutTimeIntervals() {
        val expected = emptyList<TimeReportQueryGroup>()

        val actual = timeReport.findWeeks(android.id.value, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findWeeks_withoutTimeIntervalForProject() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value
                stopInMilliseconds = startOfDay.value + 10.minutes
            }
        )
        val expected = emptyList<TimeReportQueryGroup>()

        val actual = timeReport.findWeeks(ios.id.value, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findWeeks_withTimeInterval() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value
                stopInMilliseconds = startOfDay.value + 10.minutes
            }
        )
        val expected = listOf(
            TimeReportQueryGroup(startOfDay.value, "1")
        )

        val actual = timeReport.findWeeks(android.id.value, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findWeeks_withTimeIntervals() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value
                stopInMilliseconds = startOfDay.value + 10.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value + 20.minutes
                stopInMilliseconds = startOfDay.value + 30.minutes
            }
        )
        val expected = listOf(
            TimeReportQueryGroup(startOfDay.value, "1,2")
        )

        val actual = timeReport.findWeeks(android.id.value, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findWeeks_withTimeIntervalWithinSameWeek() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        val startOfWeek = setToStartOfWeek(startOfDay, timeZone)
        val endOfWeek = setToEndOfWeek(startOfDay, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfWeek.value
                stopInMilliseconds = startOfWeek.value + 10.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = endOfWeek.value
                stopInMilliseconds = endOfWeek.value + 10.minutes
            }
        )
        val expected = listOf(
            TimeReportQueryGroup(startOfWeek.value, "1,2")
        )

        val actual = timeReport.findWeeks(android.id.value, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findWeeks_withTimeIntervalInDifferentWeeks() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        val startOfWeek = setToStartOfWeek(startOfDay, timeZone)
        val nextWeek = startOfWeek + 1.weeks
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfWeek.value
                stopInMilliseconds = startOfWeek.value + 10.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = nextWeek.value
                stopInMilliseconds = nextWeek.value + 10.minutes
            }
        )
        val expected = listOf(
            TimeReportQueryGroup(nextWeek.value, "2"),
            TimeReportQueryGroup(startOfWeek.value, "1")
        )

        val actual = timeReport.findWeeks(android.id.value, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findWeeks_withRegisteredTimeInterval() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value
                stopInMilliseconds = startOfDay.value + 10.minutes
                registered = true
            }
        )
        val expected = listOf(
            TimeReportQueryGroup(startOfDay.value, "1")
        )

        val actual = timeReport.findWeeks(android.id.value, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findWeeks_whenExcludingByLoadPosition() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        val startOfWeek = setToStartOfWeek(startOfDay, timeZone)
        val nextWeek = setToEndOfWeek(startOfDay, timeZone) + 1.weeks
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfWeek.value
                stopInMilliseconds = startOfWeek.value + 10.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfWeek.value + 20.minutes
                stopInMilliseconds = startOfWeek.value + 30.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = nextWeek.value
                stopInMilliseconds = nextWeek.value + 10.minutes
            }
        )
        val expected = listOf(
            TimeReportQueryGroup(startOfWeek.value, "1,2")
        )

        val actual = timeReport.findWeeks(android.id.value, 1, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findWeeks_whenExcludingByLoadSize() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        val startOfWeek = setToStartOfWeek(startOfDay, timeZone)
        val nextWeek = setToEndOfWeek(startOfDay, timeZone) + 1.weeks
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfWeek.value
                stopInMilliseconds = startOfWeek.value + 10.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfWeek.value + 20.minutes
                stopInMilliseconds = startOfWeek.value + 30.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = nextWeek.value
                stopInMilliseconds = nextWeek.value + 10.minutes
            }
        )
        val expected = listOf(
            TimeReportQueryGroup(nextWeek.value, "3")
        )

        val actual = timeReport.findWeeks(android.id.value, 0, 1)

        assertEquals(expected, actual)
    }

    // Find not registered weeks

    @Test
    fun findNotRegisteredWeeks_withoutTimeIntervals() {
        val expected = emptyList<TimeReportQueryGroup>()

        val actual = timeReport.findNotRegisteredWeeks(android.id.value, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegisteredWeeks_withoutTimeIntervalForProject() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value
                stopInMilliseconds = startOfDay.value + 10.minutes
            }
        )
        val expected = emptyList<TimeReportQueryGroup>()

        val actual = timeReport.findNotRegisteredWeeks(ios.id.value, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegisteredWeeks_withTimeInterval() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value
                stopInMilliseconds = startOfDay.value + 10.minutes
            }
        )
        val expected = listOf(
            TimeReportQueryGroup(startOfDay.value, "1")
        )

        val actual = timeReport.findNotRegisteredWeeks(android.id.value, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegisteredWeeks_withTimeIntervals() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value
                stopInMilliseconds = startOfDay.value + 10.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value + 20.minutes
                stopInMilliseconds = startOfDay.value + 30.minutes
            }
        )
        val expected = listOf(
            TimeReportQueryGroup(startOfDay.value, "1,2")
        )

        val actual = timeReport.findNotRegisteredWeeks(android.id.value, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegisteredWeeks_withTimeIntervalWithinSameWeek() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        val startOfWeek = setToStartOfWeek(startOfDay, timeZone)
        val endOfWeek = setToEndOfWeek(startOfDay, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfWeek.value
                stopInMilliseconds = startOfWeek.value + 10.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = endOfWeek.value
                stopInMilliseconds = endOfWeek.value + 10.minutes
            }
        )
        val expected = listOf(
            TimeReportQueryGroup(startOfWeek.value, "1,2")
        )

        val actual = timeReport.findNotRegisteredWeeks(android.id.value, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegisteredWeeks_withTimeIntervalInDifferentWeeks() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        val startOfWeek = setToStartOfWeek(startOfDay, timeZone)
        val nextWeek = setToEndOfWeek(startOfDay, timeZone) + 1.weeks
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfWeek.value
                stopInMilliseconds = startOfWeek.value + 10.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = nextWeek.value
                stopInMilliseconds = nextWeek.value + 10.minutes
            }
        )
        val expected = listOf(
            TimeReportQueryGroup(nextWeek.value, "2"),
            TimeReportQueryGroup(startOfWeek.value, "1")
        )

        val actual = timeReport.findNotRegisteredWeeks(android.id.value, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegisteredWeeks_withRegisteredTimeInterval() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfDay.value
                stopInMilliseconds = startOfDay.value + 10.minutes
                registered = true
            }
        )
        val expected = emptyList<TimeReportQueryGroup>()

        val actual = timeReport.findNotRegisteredWeeks(android.id.value, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegisteredWeeks_whenExcludingByLoadPosition() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        val startOfWeek = setToStartOfWeek(startOfDay, timeZone)
        val nextWeek = setToEndOfWeek(startOfDay, timeZone) + 1.weeks
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfWeek.value
                stopInMilliseconds = startOfWeek.value + 10.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfWeek.value + 20.minutes
                stopInMilliseconds = startOfWeek.value + 30.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = nextWeek.value
                stopInMilliseconds = nextWeek.value + 10.minutes
            }
        )
        val expected = listOf(
            TimeReportQueryGroup(startOfWeek.value, "1,2")
        )

        val actual = timeReport.findNotRegisteredWeeks(android.id.value, 1, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegisteredWeeks_whenExcludingByLoadSize() {
        val startOfDay = setToStartOfDay(Milliseconds.now, timeZone)
        val startOfWeek = setToStartOfWeek(startOfDay, timeZone)
        val nextWeek = setToEndOfWeek(startOfDay, timeZone) + 1.weeks
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfWeek.value
                stopInMilliseconds = startOfWeek.value + 10.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = startOfWeek.value + 20.minutes
                stopInMilliseconds = startOfWeek.value + 30.minutes
            }
        )
        timeIntervals.add(
            timeIntervalEntity {
                startInMilliseconds = nextWeek.value
                stopInMilliseconds = nextWeek.value + 10.minutes
            }
        )
        val expected = listOf(
            TimeReportQueryGroup(nextWeek.value, "3")
        )

        val actual = timeReport.findNotRegisteredWeeks(android.id.value, 0, 1)

        assertEquals(expected, actual)
    }

    // Find all

    @Test
    fun findAll_withoutTimeIntervals() {
        val actual = timeReport.findAll(1, 0, 10)

        assertEquals(emptyList<TimeReportQueryGroup>(), actual)
    }

    @Test
    fun findAll_withoutTimeIntervalForProject() {
        projects.add(projectEntity {
            id = 2
            name = "Name #2"
        })
        timeIntervals.add(timeIntervalEntity { projectId = 2 })

        val actual = timeReport.findAll(1, 0, 10)

        assertEquals(emptyList<TimeReportQueryGroup>(), actual)
    }

    @Test
    fun findAll_withTimeInterval() {
        timeIntervals.add(timeIntervalEntity())
        val expected = listOf(
            TimeReportQueryGroup(1, "1")
        )

        val actual = timeReport.findAll(1, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findAll_withTimeIntervalOnSameDay() {
        timeIntervals.add(timeIntervalEntity())
        timeIntervals.add(timeIntervalEntity {
            startInMilliseconds = 10
            stopInMilliseconds = 100
        })
        val expected = listOf(
            TimeReportQueryGroup(1, "1,2")
        )

        val actual = timeReport.findAll(1, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findAll_withTimeIntervalOnDifferentDays() {
        timeIntervals.add(timeIntervalEntity())
        timeIntervals.add(timeIntervalEntity {
            startInMilliseconds = 90000000
            stopInMilliseconds = 90000010
        })
        val expected = listOf(
            TimeReportQueryGroup(90000000, "2"),
            TimeReportQueryGroup(1, "1")
        )

        val actual = timeReport.findAll(1, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findAll_withTimeIntervalWithPosition() {
        timeIntervals.add(timeIntervalEntity())
        timeIntervals.add(timeIntervalEntity {
            startInMilliseconds = 90000000
            stopInMilliseconds = 90000010
        })
        val expected = listOf(
            TimeReportQueryGroup(1, "1")
        )

        val actual = timeReport.findAll(1, 1, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findAll_withTimeIntervalWithPageSize() {
        timeIntervals.add(timeIntervalEntity())
        timeIntervals.add(timeIntervalEntity {
            startInMilliseconds = 90000000
            stopInMilliseconds = 90000010
        })
        val expected = listOf(
            TimeReportQueryGroup(90000000, "2")
        )

        val actual = timeReport.findAll(1, 0, 1)

        assertEquals(expected, actual)
    }

    // Find not registered

    @Test
    fun findNotRegistered_withoutTimeIntervals() {
        val actual = timeReport.findNotRegistered(1, 0, 10)

        assertEquals(emptyList<TimeReportQueryGroup>(), actual)
    }

    @Test
    fun findNotRegistered_withoutTimeIntervalForProject() {
        projects.add(projectEntity {
            id = 2
            name = "Name #2"
        })
        timeIntervals.add(timeIntervalEntity { projectId = 2 })

        val actual = timeReport.findNotRegistered(1, 0, 10)

        assertEquals(emptyList<TimeReportQueryGroup>(), actual)
    }

    @Test
    fun findNotRegistered_withTimeInterval() {
        timeIntervals.add(timeIntervalEntity())
        val expected = listOf(
            TimeReportQueryGroup(1, "1")
        )

        val actual = timeReport.findNotRegistered(1, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegistered_withTimeIntervalOnSameDay() {
        timeIntervals.add(timeIntervalEntity())
        timeIntervals.add(timeIntervalEntity {
            startInMilliseconds = 10
            stopInMilliseconds = 100
        })
        val expected = listOf(
            TimeReportQueryGroup(1, "1,2")
        )

        val actual = timeReport.findNotRegistered(1, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegistered_withTimeIntervalOnDifferentDays() {
        timeIntervals.add(timeIntervalEntity())
        timeIntervals.add(timeIntervalEntity {
            startInMilliseconds = 90000000
            stopInMilliseconds = 90000010
        })
        val expected = listOf(
            TimeReportQueryGroup(90000000, "2"),
            TimeReportQueryGroup(1, "1")
        )

        val actual = timeReport.findNotRegistered(1, 0, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegistered_withTimeIntervalWithPosition() {
        timeIntervals.add(timeIntervalEntity())
        timeIntervals.add(timeIntervalEntity {
            startInMilliseconds = 90000000
            stopInMilliseconds = 90000010
        })
        val expected = listOf(
            TimeReportQueryGroup(1, "1")
        )

        val actual = timeReport.findNotRegistered(1, 1, 10)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegistered_withTimeIntervalWithPageSize() {
        timeIntervals.add(timeIntervalEntity())
        timeIntervals.add(timeIntervalEntity {
            startInMilliseconds = 90000000
            stopInMilliseconds = 90000010
        })
        val expected = listOf(
            TimeReportQueryGroup(90000000, "2")
        )

        val actual = timeReport.findNotRegistered(1, 0, 1)

        assertEquals(expected, actual)
    }

    @Test
    fun findNotRegistered_withRegisteredTimeInterval() {
        timeIntervals.add(timeIntervalEntity { registered = true })
        timeIntervals.add(timeIntervalEntity {
            startInMilliseconds = 90000000
            stopInMilliseconds = 90000010
        })
        val expected = listOf(
            TimeReportQueryGroup(90000000, "2")
        )

        val actual = timeReport.findNotRegistered(1, 0, 10)

        assertEquals(expected, actual)
    }
}
