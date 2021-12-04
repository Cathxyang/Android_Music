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

    private DownloadTask downloadTask;

    private String downloadUrl;

    private DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent){
        return mBinder;
    }

    class DownloadBinder extends Binder{

        public void startDownload(Context context,String url, String song){
            if(downloadTask == null){
                downloadUrl = url;
                downloadTask = new DownloadTask(song,context);
                downloadTask.execute(downloadUrl);
                Toast.makeText(DownloadService.this, "Downloading...",
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

}
