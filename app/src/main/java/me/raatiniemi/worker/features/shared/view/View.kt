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

package me.raatiniemi.worker.features.shared.view

import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun View.onClick(action: suspend () -> Unit) {
    setOnClickListener {
        GlobalScope.launch(Dispatchers.Default) {
            action()
        }
    }
}

fun View.visibleIf(defaultVisibility: Int = View.INVISIBLE, predicate: () -> Boolean) {
    if (defaultVisibility != View.INVISIBLE && defaultVisibility != View.GONE) {
        throw IllegalArgumentException("defaultVisibility needs to be either `View.GONE` or `View.INVISIBLE`")
    }

    visibility = if (predicate()) {
        View.VISIBLE
    } else {
        defaultVisibility
    }
}
