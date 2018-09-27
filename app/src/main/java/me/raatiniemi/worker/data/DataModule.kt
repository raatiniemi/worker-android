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

package me.raatiniemi.worker.data

import androidx.room.Room
import me.raatiniemi.worker.data.repository.ProjectRoomRepository
import me.raatiniemi.worker.data.repository.TimeIntervalResolverRepository
import me.raatiniemi.worker.data.repository.TimesheetResolverRepository
import me.raatiniemi.worker.domain.repository.ProjectRepository
import me.raatiniemi.worker.domain.repository.TimeIntervalRepository
import me.raatiniemi.worker.domain.repository.TimesheetRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val dataModule = module {
    single {
        // Use an in memory database for now. When using the real database we need to
        // migrate the existing data to the new scheme.
        Room.inMemoryDatabaseBuilder(androidContext(), Database::class.java)
                // TODO: Remove `allowMainThreadQueries` when more code is migrated to coroutines.
                // Using Room with RxJava 1 seem to not work properly in regards to main thread,
                // etc. Therefor should we allow for main thread queries until more of the app have
                // been migrated to use coroutines.
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
    }

    single<ProjectRepository> {
        val database: Database = get()

        ProjectRoomRepository(database.projects())
    }

    single<TimeIntervalRepository> { TimeIntervalResolverRepository(androidContext().contentResolver) }

    single<TimesheetRepository> { TimesheetResolverRepository(androidContext().contentResolver) }
}
