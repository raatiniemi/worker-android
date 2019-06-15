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

package me.raatiniemi.worker.domain.interactor

import me.raatiniemi.worker.domain.exception.ActiveProjectException
import me.raatiniemi.worker.domain.model.Milliseconds
import me.raatiniemi.worker.domain.model.android
import me.raatiniemi.worker.domain.model.newTimeInterval
import me.raatiniemi.worker.domain.model.timeInterval
import me.raatiniemi.worker.domain.repository.TimeIntervalInMemoryRepository
import me.raatiniemi.worker.domain.repository.TimeIntervalRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

@RunWith(JUnit4::class)
class ClockInTest {
    private lateinit var repository: TimeIntervalRepository
    private lateinit var clockIn: ClockIn

    @Before
    fun setUp() {
        repository = TimeIntervalInMemoryRepository()
        clockIn = ClockIn(repository)
    }

    @Test(expected = ActiveProjectException::class)
    fun execute_withActiveTime() {
        repository.add(
            newTimeInterval {
                projectId = android.id
                start = Milliseconds(1)
            }
        )

        clockIn(android.id, Date())
    }

    @Test
    fun execute() {
        val date = Date()
        val expected = listOf(
            timeInterval {
                id = 1
                projectId = android.id
                start = Milliseconds(date.time)
            }
        )

        clockIn(android.id, date)

        val actual = repository.findAll(android, Milliseconds.empty)
        assertEquals(expected, actual)
    }
}
