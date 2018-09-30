/*
 * Copyright (C) 2018 Worker Project
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

import androidx.room.*

@Dao
interface TimeIntervalDao {
    @Query("""SELECT * FROM time_intervals
        WHERE project_id = :projectId AND
            (start_in_milliseconds >= :startInMilliseconds OR stop_in_milliseconds = 0)
        ORDER BY stop_in_milliseconds ASC, start_in_milliseconds ASC""")
    fun findAll(projectId: Long, startInMilliseconds: Long): List<TimeIntervalEntity>

    @Query("SELECT * FROM time_intervals WHERE _id = :id LIMIT 1")
    fun find(id: Long): TimeIntervalEntity?

    @Query("""SELECT * FROM time_intervals
        WHERE project_id = :projectId AND stop_in_milliseconds = 0""")
    fun findActiveTime(projectId: Long): TimeIntervalEntity?

    @Insert
    fun add(entity: TimeIntervalEntity): Long

    @Update
    fun update(entities: List<TimeIntervalEntity>)

    @Delete
    fun remove(entities: List<TimeIntervalEntity>)
}
