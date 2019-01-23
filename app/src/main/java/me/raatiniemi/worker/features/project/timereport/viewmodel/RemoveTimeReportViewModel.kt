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

package me.raatiniemi.worker.features.project.timereport.viewmodel

import me.raatiniemi.worker.domain.interactor.RemoveTime
import me.raatiniemi.worker.features.project.timereport.model.TimeReportAdapterResult
import me.raatiniemi.worker.util.RxUtil.hideErrors
import me.raatiniemi.worker.util.RxUtil.redirectErrors
import rx.Observable
import rx.subjects.PublishSubject

class RemoveTimeReportViewModel internal constructor(private val useCase: RemoveTime) {
    private val remove = PublishSubject.create<List<TimeReportAdapterResult>>()

    private val success = PublishSubject.create<TimeReportAdapterResult>()
    private val errors = PublishSubject.create<Throwable>()

    init {
        remove.switchMap {
            executeUseCase(it)
                    .compose(redirectErrors(errors))
                    .compose(hideErrors())
        }.subscribe(success)
    }

    private fun executeUseCase(results: List<TimeReportAdapterResult>): Observable<TimeReportAdapterResult> {
        return Observable.defer<TimeReportAdapterResult> {
            try {
                val times = results.map { it.timeInterval }.toList()
                useCase.execute(times)

                Observable.from(results.sorted().reversed())
            } catch (e: Exception) {
                Observable.error(e)
            }
        }
    }

    fun remove(results: List<TimeReportAdapterResult>) {
        remove.onNext(results)
    }

    fun success(): Observable<TimeReportAdapterResult> {
        return success
    }

    fun errors(): Observable<Throwable> {
        return errors
    }
}
