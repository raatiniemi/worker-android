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

package me.raatiniemi.worker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TimesheetItemTest {
    @Test
    fun asTime() {
        val timeInterval = TimeInterval.builder(1L).build()
        val item = TimesheetItem.with(timeInterval)

        assertSame(timeInterval, item.asTimeInterval())
    }

    @Test
    fun getId() {
        val timeInterval = TimeInterval.builder(1L)
                .id(1L)
                .build()
        val item = TimesheetItem.with(timeInterval)

        assertEquals(timeInterval.id, item.id)
    }
}
