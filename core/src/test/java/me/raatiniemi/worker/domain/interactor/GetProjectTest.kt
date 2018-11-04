/*
 * Copyright (C) 2017 Worker Project
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

import me.raatiniemi.worker.domain.exception.NoProjectException
import me.raatiniemi.worker.domain.model.Project
import me.raatiniemi.worker.domain.repository.ProjectInMemoryRepository
import me.raatiniemi.worker.domain.repository.ProjectRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GetProjectTest {
    private val repository: ProjectRepository = ProjectInMemoryRepository()
    private lateinit var useCase: GetProject

    @Before
    fun setUp() {
        useCase = GetProject(repository)
    }

    @Test
    fun execute() {
        repository.add(Project.from("Project name"))
        val expected = Project.from(1L, "Project name")

        val actual = useCase.execute(1L)

        assertEquals(expected, actual)
    }

    @Test(expected = NoProjectException::class)
    fun `execute withoutProject`() {
        useCase.execute(1L)
    }
}
