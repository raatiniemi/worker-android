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

package me.raatiniemi.worker.presentation.util;

import android.content.SharedPreferences;

/**
 * Communicate with the shared preferences.
 */
public class Settings implements TimeSheetSummaryFormatPreferences {
    // TODO: Should time sheet summary format constants be moved to a better location?
    public static final int TIME_SHEET_SUMMARY_FORMAT_DIGITAL_CLOCK = 1;
    public static final int TIME_SHEET_SUMMARY_FORMAT_FRACTION = 2;

    private final SharedPreferences preferences;

    public Settings(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public int getTimeSheetSummaryFormat() {
        return preferences.getInt(AppKeys.TIME_SHEET_SUMMARY_FORMAT.getRawValue(), TIME_SHEET_SUMMARY_FORMAT_DIGITAL_CLOCK);
    }

    @Override
    public void useFractionAsTimeSheetSummaryFormat() {
        preferences.edit()
                .putInt(AppKeys.TIME_SHEET_SUMMARY_FORMAT.getRawValue(), TIME_SHEET_SUMMARY_FORMAT_FRACTION)
                .apply();

    }

    @Override
    public void useDigitalClockAsTimeSheetSummaryFormat() {
        preferences.edit()
                .putInt(AppKeys.TIME_SHEET_SUMMARY_FORMAT.getRawValue(), TIME_SHEET_SUMMARY_FORMAT_DIGITAL_CLOCK)
                .apply();
    }
}
