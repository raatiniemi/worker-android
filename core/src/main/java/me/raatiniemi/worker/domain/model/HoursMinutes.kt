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

@file:JvmName("HoursMinutesUtil")

package me.raatiniemi.worker.domain.model

import java.lang.Math.abs

data class HoursMinutes(val hours: Long, val minutes: Long) {
    @get:JvmName("isEmpty")
    val empty = hours == 0L && minutes == 0L

    @get:JvmName("isPositive")
    val positive = hours >= 0L && minutes >= 0L

    fun asMilliseconds(): Long {
        return calculateSeconds() * MILLISECONDS_IN_SECOND
    }

    private fun calculateSeconds(): Long {
        return calculateMinutes() * SECONDS_IN_MINUTE
    }

    private fun calculateMinutes(): Long {
        val hoursInMinutes = hours * MINUTES_IN_HOUR

        return hoursInMinutes + minutes
    }

    operator fun plus(value: HoursMinutes): HoursMinutes {
        var accumulatedHours = hours + value.hours
        var accumulatedMinutes = minutes + value.minutes

        if (accumulatedMinutes >= MINUTES_IN_HOUR) {
            accumulatedHours += accumulatedMinutes / MINUTES_IN_HOUR
            accumulatedMinutes %= MINUTES_IN_HOUR
        }

        return HoursMinutes(accumulatedHours, accumulatedMinutes)
    }

    operator fun minus(value: HoursMinutes): HoursMinutes {
        val milliseconds = asMilliseconds() - value.asMilliseconds()

        val seconds = abs(milliseconds) / MILLISECONDS_IN_SECOND
        var minutes = seconds / SECONDS_IN_MINUTE % MINUTES_IN_HOUR
        var hours = seconds / SECONDS_IN_MINUTE / MINUTES_IN_HOUR

        val isNewValueNegative = milliseconds < 0
        if (isNewValueNegative) {
            minutes *= -1
            hours *= -1
        }

        return HoursMinutes(hours = hours, minutes = minutes)
    }

    companion object {
        private const val MINUTES_IN_HOUR = 60
        private const val SECONDS_IN_MINUTE = 60
        private const val MILLISECONDS_IN_SECOND = 1000

        val empty = HoursMinutes(hours = 0, minutes = 0)
    }
}

fun Collection<HoursMinutes>.accumulated(): HoursMinutes {
    return fold(HoursMinutes.empty) { acc, value -> acc + value }
}
