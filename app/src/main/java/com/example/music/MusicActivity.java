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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.Transliterator;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
    public DownloadTask.Music music;
    public String song;
    public Handler seekbarHandler = new Handler();
    Button btn_last;
    Button btn_play;
    Button btn_next;
    ListView list_music;
    SeekBar seek;
    TextView text_music;                              
    Context context = this;

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
    @SuppressLint({"Range", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        btn_last = (Button) findViewById(R.id.btn_last);
        btn_play = (Button) findViewById(R.id.btn_play);
        btn_next = (Button) findViewById(R.id.btn_next);
        list_music = (ListView) findViewById(R.id.list_music);
        seek = (SeekBar) findViewById(R.id.seek);
        text_music = (TextView) findViewById(R.id.song);

        dbHelper = new MusicDatabaseHelper(this, "Music.db", null, 1);

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
                Cursor cursor = db.rawQuery("select id from Music where name = ?",
                        new String[]{song});
                cursor.moveToFirst();
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                if (id == 0) {
                    Toast.makeText(MusicActivity.this,
                            "已经是第一首歌了哦", Toast.LENGTH_SHORT).show();
                } else {
                    id--;
                    cursor = db.rawQuery("select name from Music where id = ?",
                            new String[]{String.valueOf(id)});
                    cursor.moveToFirst();
                    song = cursor.getString(cursor.getColumnIndex("name"));
                    try {
                        play(song);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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
                Cursor cursor = db.rawQuery("select id from Music where name = ?",
                        new String[]{song});
                cursor.moveToFirst();
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                if (id == 4) {
                    Toast.makeText(MusicActivity.this,
                            "最后一首歌啦", Toast.LENGTH_SHORT).show();
                } else {
                    id++;
                    cursor = db.rawQuery("select name from Music where id = ?",
                            new String[]{String.valueOf(id)});
                    cursor.moveToFirst();
                    song = cursor.getString(cursor.getColumnIndex("name"));
                    try {
                        play(song);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //当前歌曲列表
        list_music.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                song = music_name.get(position);
                try {
                    play(song);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //进度条
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress() * 1000);

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

    //播放音乐
    public void play(String song) throws IOException {
        String url = "https://freemusicarchive.org/track/"
                + song + "/download";
        downloadBinder.startDownload(context, url, song, new DownloadService.completeHandler() {
            @Override
            public void complete(Boolean result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource("/data/user/0/com.example.music/" + song);
                            mediaPlayer.prepare();      //进入准备状态
                            text_music.setText(song);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    seek.setProgress(mediaPlayer.getCurrentPosition() / 1000);
                                    seekbarHandler.postDelayed(this, 1);
                                }
                            });
                            seek.setMax(mediaPlayer.getDuration() / 1000);
                            mediaPlayer.start();
                            btn_play.setText("暂停");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }


}