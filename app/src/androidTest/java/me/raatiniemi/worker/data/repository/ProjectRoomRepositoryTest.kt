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
import me.raatiniemi.worker.domain.model.*
import me.raatiniemi.worker.domain.repository.ProjectRepository
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectRoomRepositoryTest {
    private lateinit var database: Database
    private lateinit var repository: ProjectRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, Database::class.java)
            .allowMainThreadQueries()
            .build()

        repository = ProjectRoomRepository(database.projects())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun count_withoutProjects() {
        val expected = 0

        val actual = repository.count()

        assertEquals(expected, actual)
    }

    @Test
    fun count_withProject() {
        repository.add(NewProject(android.name))
        val expected = 1

        val actual = repository.count()

        assertEquals(expected, actual)
    }

    @Test
    fun count_withProjects() {
        repository.add(NewProject(android.name))
        repository.add(NewProject(cli.name))
        val expected = 2

        val actual = repository.count()

        assertEquals(expected, actual)
    }

    @Test
    fun findAll_pagingWithoutProjects() {
        val expected = emptyList<Project>()
        val loadRange = LoadRange(
            LoadPosition(0),
            LoadSize(10)
        )

        val actual = repository.findAll(loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun findAll_pagingWithProject() {
        repository.add(NewProject(android.name))
        val expected = listOf(android)
        val loadRange = LoadRange(
            LoadPosition(0),
            LoadSize(10)
        )

        val actual = repository.findAll(loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun findAll_pagingWithProjects() {
        repository.add(NewProject(android.name))
        repository.add(NewProject(cli.name))
        val expected = listOf(android, cli)
        val loadRange = LoadRange(
            LoadPosition(0),
            LoadSize(10)
        )

        val actual = repository.findAll(loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun findAll_withProjectBeforePage() {
        repository.add(NewProject(android.name))
        repository.add(NewProject(cli.name))
        val expected = listOf(cli)
        val loadRange = LoadRange(
            LoadPosition(1),
            LoadSize(10)
        )

        val actual = repository.findAll(loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun findAll_withProjectAfterPage() {
        repository.add(NewProject(android.name))
        repository.add(NewProject(cli.name))
        val expected = listOf(android)
        val loadRange = LoadRange(
            LoadPosition(0),
            LoadSize(1)
        )

        val actual = repository.findAll(loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun findAll_sortedPage() {
        repository.add(NewProject(cli.name))
        repository.add(NewProject(android.name))
        val expected = listOf(
            Project(2, android.name),
            Project(1, cli.name)
        )
        val loadRange = LoadRange(
            LoadPosition(0),
            LoadSize(10)
        )

        val actual = repository.findAll(loadRange)

        assertEquals(expected, actual)
    }

    @Test
    fun findAll_withoutProjects() {
        val actual = repository.findAll()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun findAll_withProject() {
        repository.add(NewProject(android.name))
        val expected = listOf(android)

        val actual = repository.findAll()

        assertEquals(expected, actual)
    }

    @Test
    fun findAll_withProjects() {
        repository.add(NewProject(cli.name))
        repository.add(NewProject(android.name))
        val expected = listOf(
            Project(2, android.name),
            Project(1, cli.name)
        )

        val actual = repository.findAll()

        assertEquals(expected, actual)
    }

    @Test
    fun findByName_withoutProject() {
        val actual = repository.findByName(android.name)

        assertNull(actual)
    }

    @Test
    fun findByName_withProject() {
        repository.add(NewProject(android.name))

        val actual = repository.findByName(android.name)

        assertEquals(android, actual)
    }

    @Test
    fun findById_withoutProjects() {
        val actual = repository.findById(ProjectId(android.id))

        assertNull(actual)
    }

    @Test
    fun findById_withProject() {
        repository.add(NewProject(android.name))

        val actual = repository.findById(ProjectId(android.id))

        assertEquals(android, actual)
    }

    @Test
    fun findById_withProjects() {
        repository.add(NewProject(android.name))
        repository.add(NewProject(cli.name))

        val actual = repository.findById(ProjectId(cli.id))

        assertEquals(cli, actual)
    }

    @Test
    fun remove_withoutProjects() {
        repository.remove(android.id)
    }

    @Test
    fun remove_withProject() {
        repository.add(NewProject(android.name))
        val expected = emptyList<Project>()

        repository.remove(android.id)

        val actual = repository.findAll()
        assertEquals(expected, actual)
    }

    @Test
    fun remove_withProjects() {
        repository.add(NewProject(android.name))
        repository.add(NewProject(cli.name))
        val expected = listOf(cli)

        repository.remove(android.id)

        val actual = repository.findAll()
        assertEquals(expected, actual)
    }
}