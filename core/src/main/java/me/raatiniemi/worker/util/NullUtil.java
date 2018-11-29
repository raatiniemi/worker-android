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

package me.raatiniemi.worker.util;

public final class NullUtil {
    private NullUtil() {
    }

    /**
     * Check whether an object is not null.
     *
     * @param o Object to check for not null.
     * @return True if object is not null, otherwise false.
     */
    public static boolean nonNull(Object o) {
        return null != o;
    }

    /**
     * Check whether an object is null.
     *
     * @param o Object to check for null.
     * @return True if object is null, otherwise false.
     */
    public static boolean isNull(Object o) {
        return null == o;
    }
}
