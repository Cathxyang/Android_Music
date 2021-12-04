package com.example.music;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends AppCompatActivity {

    private DownloadService.DownloadBinder downloadBinder;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private List<String> music_name = new ArrayList<>();
    private MusicDatabaseHelper dbHelper;
    public int time;
    public String song;


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        dbHelper = new MusicDatabaseHelper(this, "Music.db", null, 1);
        Button btn_last = (Button) findViewById(R.id.btn_last);
        Button btn_play = (Button) findViewById(R.id.btn_play);
        Button btn_next = (Button) findViewById(R.id.btn_next);
        ListView list_music = (ListView) findViewById(R.id.list_music);
        SeekBar seek = (SeekBar) findViewById(R.id.seek);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MusicActivity.this,
                android.R.layout.simple_list_item_1, music_name);
        list_music.setAdapter(adapter);

        //在列表中加入音乐
        music_name.add("grave");
        music_name.add("lento");
        music_name.add("advent");
        music_name.add("ascent");
        music_name.add("ashore");

        //将歌名和id加入数据库
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", 0);
        values.put("name", "grave");
        db.insert("Music", null, values);
        values.clear();
        values.put("id", 1);
        values.put("name", "lento");
        db.insert("Music", null, values);
        values.clear();
        values.put("id", 2);
        values.put("name", "advent");
        db.insert("Music", null, values);
        values.clear();
        values.put("id", 3);
        values.put("name", "ascent");
        db.insert("Music", null, values);
        values.clear();
        values.put("id", 4);
        values.put("name", "ashore");
        db.insert("Music", null, values);


        //上一首歌
        btn_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //播放或暂停
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_play.getText() == "播放") {
                    btn_play.setText("暂停");
                    mediaPlayer.start();
                } else {
                    mediaPlayer.pause();
                    time = mediaPlayer.getDuration();
                    btn_play.setText("播放");
                }
            }
        });
        //下一首歌
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource("/data/user/0/com.example.music/" + song);
                    mediaPlayer.prepare();      //进入准备状态
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                btn_play.setText("暂停");
            }
        });
        //当前歌曲列表
        Context context = this;
        list_music.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                song = music_name.get(position);
                String url = "https://freemusicarchive.org/track/"
                        + song + "/download";
                downloadBinder.startDownload(context, url, song);
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource("/data/user/0/com.example.music/" + song);
                    mediaPlayer.prepare();      //进入准备状态
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                btn_play.setText("暂停");
            }
        });

        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);  //绑定服务
        if (ContextCompat.checkSelfPermission(MusicActivity.this, Manifest.permission
                .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MusicActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        mediaPlayer.stop();
        mediaPlayer.release();
    }

}