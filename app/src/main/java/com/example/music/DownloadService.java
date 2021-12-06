package com.example.music;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.net.URL;

public class DownloadService extends Service {

    interface completeHandler {
        void complete(Boolean result);
    }

    private DownloadTask downloadTask;

    private String downloadUrl;

    private DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class DownloadBinder extends Binder {

        public void startDownload(Context context, String url, String song, completeHandler handler) {
            downloadUrl = url;
            downloadTask = new DownloadTask(song, context, new DownloadTask.Music() {
                @Override
                public void complete(Boolean result) {
                    handler.complete(true);
                }

            });
            downloadTask.execute(downloadUrl);

        }

    }

}
