package com.example.music;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask extends AsyncTask<String, Integer, Integer> {

    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;

    public interface Music{
        void complete(Boolean result);
    }

    public String song;
    public Context context;
    public Music music;
    public DownloadTask(String song, Context context,Music music) {

        this.song = song;
        this.context = context;
        this.music = music;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected Integer doInBackground(String... params) {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        File file = null;
        try {
            //记录已下载文件长度
            long downloadedLength = 0;
            String downloadUrl = params[0];
            file = new File(context.getDataDir().getPath() + "/" + song);
            if (file.exists()) {
                downloadedLength = file.length();
            }
            long contentLength = getContentLength(downloadUrl);
            if (contentLength == 0) {
                return TYPE_FAILED;
            } else if (contentLength == downloadedLength) {
                music.complete(true);
                return TYPE_SUCCESS;
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            is = response.body().byteStream();
            savedFile = new RandomAccessFile(file, "rw");
            savedFile.seek(downloadedLength);   //跳过已下载的字节
            byte[] b = new byte[1024];
            int total = 0;
            int len;
            while ((len = is.read(b)) != -1) {
                total += len;
                savedFile.write(b, 0, len);
                //计算已下载的百分比
                int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                publishProgress(progress);
            }
            response.body().close();
            music.complete(true);
            return TYPE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.body().close();
            return contentLength;
        }
        return 0;
    }


}
