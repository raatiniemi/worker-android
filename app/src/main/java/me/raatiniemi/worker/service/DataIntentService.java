/*
 * Copyright (C) 2015 Worker Project
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

package me.raatiniemi.worker.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.greenrobot.event.EventBus;
import me.raatiniemi.worker.R;
import me.raatiniemi.worker.model.backup.Backup;
import me.raatiniemi.worker.model.event.BackupSuccessfulEvent;
import me.raatiniemi.worker.presentation.view.activity.MainActivity;
import me.raatiniemi.worker.util.ExternalStorage;
import me.raatiniemi.worker.util.FileUtils;
import me.raatiniemi.worker.util.Worker;

/**
 * Service for running data operations.
 */
public class DataIntentService extends IntentService {
    /**
     * Intent action for running the backup operation.
     */
    public static final String INTENT_ACTION_BACKUP = "backup";

    /**
     * Intent action for running the restore operation.
     */
    public static final String INTENT_ACTION_RESTORE = "restore";

    /**
     * Tag for logging.
     */
    private static final String TAG = "DataIntentService";

    /**
     * Type of running data operation.
     */
    private static RUNNING sRunning = RUNNING.NONE;

    private final EventBus mEventBus;

    /**
     * Constructor.
     */
    public DataIntentService() {
        super(TAG);

        mEventBus = EventBus.getDefault();
    }

    /**
     * Get the type of data operation that is running. If the RUNNING.NONE is
     * returned no data operation is currently running.
     *
     * @return Type of running data operation.
     */
    public static RUNNING getRunning() {
        return sRunning;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // If an operation is already running, we should not allow another to
        // start. We wouldn't want backup and restore running simultaneously.
        if (RUNNING.NONE != getRunning()) {
            Log.w(TAG, "Data operation is already running, exiting");
            return;
        }

        String action = intent.getAction();
        switch (action) {
            case INTENT_ACTION_BACKUP:
                sRunning = RUNNING.BACKUP;
                backup();
                break;
            case INTENT_ACTION_RESTORE:
                sRunning = RUNNING.RESTORE;
                restore();
                break;
            default:
                Log.w(TAG, "Received unknown action: " + action);
                break;
        }
        sRunning = RUNNING.NONE;
    }

    /**
     * Run the backup operation. Copies the SQLite database to the backup
     * directory on the external storage.
     */
    private synchronized void backup() {
        NotificationManager manager = null;
        NotificationCompat.Builder notification = null;

        try {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Check that the external storage is writable.
            if (!ExternalStorage.isWritable()) {
                throw new IOException("External storage is not writable");
            }

            // Check that the backup directory is available.
            File directory = ExternalStorage.getBackupDirectory();
            if (null == directory) {
                throw new FileNotFoundException("Directory for backup is not available");
            }

            // Retrieve the source and destination file locations.
            File from = getDatabasePath(Worker.DATABASE_NAME);
            File to = new File(directory, Worker.DATABASE_NAME);

            // Perform the file copy.
            FileUtils.copy(from, to);

            // Send the "Backup complete" notification to the user.
            notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_archive_white_24dp)
                    .setContentTitle(getString(R.string.notification_backup_title))
                    .setContentText(getString(R.string.notification_backup_message));

            // Assemble and post the successful backup event.
            Backup backup = new Backup(directory);
            mEventBus.post(new BackupSuccessfulEvent(backup));
        } catch (IOException e) {
            Log.w(TAG, "Unable to backup: " + e.getMessage());

            // TODO: Display what was the cause of the backup failure.
            // Send the "Backup failed" notification to the user.
            notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_error_outline_white_24dp)
                    .setContentTitle(getString(R.string.error_notification_backup_title))
                    .setContentText(getString(R.string.error_notification_backup_message));
        } catch (ClassCastException e) {
            Log.w(TAG, "Unable to cast the NotificationManager: " + e.getMessage());
        } finally {
            // Both the notification and notification manager must be
            // available, otherwise we can't display the notification.
            //
            // The notification manager won't be available if a
            // ClassCastException have been thrown.
            if (null != manager && null != notification) {
                manager.notify(
                        Worker.NOTIFICATION_DATA_INTENT_SERVICE_ID,
                        notification.build()
                );
            }
        }
    }

    /**
     * Run the restore operation. Copies the SQLite database from the latest
     * backup directory on the external storage to the database location.
     */
    private synchronized void restore() {
        NotificationManager manager = null;
        NotificationCompat.Builder notification = null;

        try {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Check that the external storage is readable.
            if (!ExternalStorage.isReadable()) {
                throw new IOException("External storage is not readable");
            }

            // Check that we have backup to restore from.
            File directory = ExternalStorage.getLatestBackupDirectory();
            if (null == directory) {
                throw new FileNotFoundException("Unable to find backup from which to restore");
            }

            // Retrieve the source and destination file locations.
            File from = new File(directory, Worker.DATABASE_NAME);
            File to = getDatabasePath(Worker.DATABASE_NAME);

            // Perform the file copy.
            FileUtils.copy(from, to);

            Intent intent = new Intent(this, MainActivity.class);
            intent.setAction(Worker.INTENT_ACTION_RESTART);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT
            );

            // Send the "Restore complete" notification to the user.
            notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_restore_white_24dp)
                    .setContentTitle(getString(R.string.notification_restore_title))
                    .setContentText(getString(R.string.notification_restore_message))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        } catch (IOException e) {
            Log.w(TAG, "Unable to restore backup: " + e.getMessage());

            // TODO: Display what was the cause of the restore failure.
            // Send the "Restore failed" notification to the user.
            notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_error_outline_white_24dp)
                    .setContentTitle(getString(R.string.error_notification_restore_title))
                    .setContentText(getString(R.string.error_notification_restore_message));
        } catch (ClassCastException e) {
            Log.w(TAG, "Unable to cast the NotificationManager: " + e.getMessage());
        } finally {
            // Both the notification and notification manager must be
            // available, otherwise we can't display the notification.
            //
            // The notification manager won't be available if a
            // ClassCastException have been thrown.
            if (null != manager && null != notification) {
                manager.notify(
                        Worker.NOTIFICATION_DATA_INTENT_SERVICE_ID,
                        notification.build()
                );
            }
        }
    }

    /**
     * Available types of runnable data operations.
     */
    public enum RUNNING {
        NONE,
        BACKUP,
        RESTORE
    }
}
