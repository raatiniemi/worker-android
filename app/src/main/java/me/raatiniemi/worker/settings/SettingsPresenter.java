package me.raatiniemi.worker.settings;

import android.content.Context;
import android.util.Log;

import java.io.File;

import me.raatiniemi.worker.base.presenter.RxPresenter;
import me.raatiniemi.worker.model.backup.Backup;
import me.raatiniemi.worker.util.ExternalStorage;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;

public class SettingsPresenter extends RxPresenter<SettingsView> {
    /**
     * Tag for logging.
     */
    private static final String TAG = "SettingsPresenter";

    /**
     * Constructor.
     *
     * @param context Context used with the presenter.
     */
    public SettingsPresenter(Context context) {
        super(context);
    }

    /**
     * Retrieve the latest backup and update the view.
     */
    public void getLatestBackup() {
        Observable.defer(new Func0<Observable<Backup>>() {
            @Override
            public Observable<Backup> call() {
                File directory = ExternalStorage.getLatestBackupDirectory();
                return Observable.just(new Backup(directory));
            }
        }).compose(this.<Backup>applySchedulers())
            .subscribe(new Subscriber<Backup>() {
                @Override
                public void onNext(Backup backup) {
                    // Check that we still have the view attached.
                    if (!isViewAttached()) {
                        Log.d(TAG, "View is not attached, skip pushing the latest backup");
                        return;
                    }

                    // Push the latest backup to the view for update.
                    getView().setLatestBackup(backup);
                }

                @Override
                public void onError(Throwable e) {
                    // Something has gone wrong when fetching the latest backup.
                    // We'd want to log the failure, even if the view is detached.
                    Log.w(TAG, "Failed to get latest backup: " + e.getMessage());

                    // Check that we still have the view attached.
                    if (!isViewAttached()) {
                        Log.d(TAG, "View is not attached, skip pushing the latest backup");
                        return;
                    }

                    getView().setLatestBackup(null);
                }

                @Override
                public void onCompleted() {
                    Log.d(TAG, "onCompleted for getLatestBackup was reached");
                }
            });
    }
}
