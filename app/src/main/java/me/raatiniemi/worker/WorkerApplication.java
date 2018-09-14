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

package me.raatiniemi.worker;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.squareup.leakcanary.LeakCanary;

import me.raatiniemi.worker.data.DaggerDataComponent;
import me.raatiniemi.worker.data.DataComponent;
import me.raatiniemi.worker.data.DataModule;
import me.raatiniemi.worker.data.service.ongoing.ReloadNotificationService;
import me.raatiniemi.worker.exception.NoApplicationInstanceException;
import me.raatiniemi.worker.presentation.util.Notifications;
import timber.log.Timber;
import timber.log.Timber.DebugTree;

/**
 * Stores application constants.
 */
public class WorkerApplication extends Application {
    /**
     * Package for the application.
     */
    public static final String PACKAGE = "me.raatiniemi.worker";

    /**
     * Name of the application database.
     */
    public static final String DATABASE_NAME = "worker";

    public static final int NOTIFICATION_BACKUP_SERVICE_ID = 1;
    public static final int NOTIFICATION_RESTORE_SERVICE_ID = 2;

    /**
     * Id for on-going notification.
     */
    public static final int NOTIFICATION_ON_GOING_ID = 3;

    /**
     * Prefix for backup directories.
     */
    public static final String STORAGE_BACKUP_DIRECTORY_PREFIX = "backup-";

    /**
     * Pattern for the backup directories.
     */
    public static final String STORAGE_BACKUP_DIRECTORY_PATTERN
            = WorkerApplication.STORAGE_BACKUP_DIRECTORY_PREFIX + "(\\d+)";

    /**
     * Intent action for restarting the application.
     */
    public static final String INTENT_ACTION_RESTART = "action_restart";

    private static WorkerApplication instance;

    private DataComponent dataComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        synchronized (WorkerApplication.class) {
            instance = this;
        }

        dataComponent = DaggerDataComponent.builder()
                .dataModule(createDataModule())
                .build();

        if (!isUnitTesting()) {
            initializeKoin();

            if (Notifications.Companion.isChannelsAvailable()) {
                registerNotificationChannel();
            }

            LeakCanary.install(this);
            ReloadNotificationService.startServiceWithContext(this);
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        }
    }

    protected void initializeKoin() {
        JavaAppKoinKt.start(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void registerNotificationChannel() {
        try {
            NotificationManager notificationManager = getNotificationManager();
            Notifications.Companion.createChannel(
                    notificationManager,
                    Notifications.Companion.ongoingChannel(getResources())
            );
            Notifications.Companion.createChannel(
                    notificationManager,
                    Notifications.Companion.backupChannel(getResources())
            );
        } catch (ClassCastException | NullPointerException e) {
            Timber.e(e);
        }
    }

    @NonNull
    private NotificationManager getNotificationManager() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (null == nm) {
            throw new NullPointerException("Unable to get NotificationManager");
        }

        return nm;
    }

    public static synchronized WorkerApplication getInstance() {
        if (null == instance) {
            throw new NoApplicationInstanceException();
        }

        return instance;
    }

    @NonNull
    DataModule createDataModule() {
        return new DataModule(this);
    }

    public DataComponent getDataComponent() {
        return dataComponent;
    }

    boolean isUnitTesting() {
        return false;
    }
}
