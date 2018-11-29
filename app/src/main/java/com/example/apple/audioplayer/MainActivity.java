package com.example.apple.audioplayer;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{


    public static int collect_flag = 0;
    private MusicDBHelper musicDBHelper;
    private static final String TAG = "miao";
    private List<Music> musicList = new ArrayList<>();
    private List<Music> playingList = new ArrayList<>();
    private Music musicPlaying;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar seekBar;
    private Thread thread;
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what) {
                case 1:
                    if (seekBar.getProgress() != TotalMusicListActivity.mediaPlayer.getCurrentPosition() * 100 / TotalMusicListActivity.mediaPlayer.getDuration()){
                        Log.d(TAG, "handleMessage: " + TotalMusicListActivity.mediaPlayer.getCurrentPosition() * 100 / TotalMusicListActivity.mediaPlayer.getDuration());
                        seekBar.setProgress(TotalMusicListActivity.mediaPlayer.getCurrentPosition() * 100 / TotalMusicListActivity.mediaPlayer.getDuration());
                    }
                    break;
                case 2:
                    TextView startTime = findViewById(R.id.start_time);
                    if(startTime.getText().toString().trim()!=TotalMusicListActivity.mediaPlayer.getCurrentPosition()/1000/60+":"+TotalMusicListActivity.mediaPlayer.getCurrentPosition()/1000%60) {
                        Log.d(TAG, "handleMessage: " + TotalMusicListActivity.mediaPlayer.getCurrentPosition() / 1000 / 60 + ":" + TotalMusicListActivity.mediaPlayer.getCurrentPosition() / 1000 % 60);
                        startTime.setText(TotalMusicListActivity.mediaPlayer.getCurrentPosition() / 1000 / 60 + ":" + TotalMusicListActivity.mediaPlayer.getCurrentPosition() / 1000 % 60);
                    }
                    break;
                case 3:
                    Log.d(TAG, "handleMessage: 333");
                    if (seekBar.getProgress() != CollectActivity.mediaPlayer.getCurrentPosition() * 100 / CollectActivity.mediaPlayer.getDuration()){
                        Log.d(TAG, "handleMessage: " + CollectActivity.mediaPlayer.getCurrentPosition() * 100 / CollectActivity.mediaPlayer.getDuration());
                        seekBar.setProgress(CollectActivity.mediaPlayer.getCurrentPosition() * 100 / CollectActivity.mediaPlayer.getDuration());
                    }
                    break;
                case 4:
                    Log.d(TAG, "handleMessage: 444");
                    startTime = findViewById(R.id.start_time);
                    if(startTime.getText().toString().trim()!=CollectActivity.mediaPlayer.getCurrentPosition()/1000/60+":"+CollectActivity.mediaPlayer.getCurrentPosition()/1000%60) {
                        Log.d(TAG, "handleMessage: " + CollectActivity.mediaPlayer.getCurrentPosition() / 1000 / 60 + ":" + CollectActivity.mediaPlayer.getCurrentPosition() / 1000 % 60);
                        startTime.setText(CollectActivity.mediaPlayer.getCurrentPosition() / 1000 / 60 + ":" + CollectActivity.mediaPlayer.getCurrentPosition() / 1000 % 60);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicDBHelper = new MusicDBHelper(this,"Music.db",null,1);

        try {
            initMusic();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Button music_list = findViewById(R.id.music_list);
        music_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,TotalMusicListActivity.class);
                startActivityForResult(intent,0);
            }
        });


        Button music_stop = findViewById(R.id.music_stop);
        music_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TotalMusicListActivity.mediaPlayer.stop();
                CollectActivity.mediaPlayer.stop();
                TextView startTime = findViewById(R.id.start_time);
                startTime.setText("00:00");
                seekBar.setProgress(0);
                TextView music_playing = findViewById(R.id.now_playing);
                music_playing.setText("");
            }
        });

        final Button music_pause = findViewById(R.id.music_pause);
        music_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(collect_flag == 0) {
                    if (TotalMusicListActivity.mediaPlayer.isPlaying()) {
                        music_pause.setText("播放");
                        TotalMusicListActivity.mediaPlayer.pause();
                    } else {
                        music_pause.setText("暂停");
                        TotalMusicListActivity.mediaPlayer.start();
                        thread = new Thread(new SeekBarThread());
                        thread.start();
                    }
                }
                else if(CollectActivity.mediaPlayer.isPlaying()){
                    music_pause.setText("播放");
                    CollectActivity.mediaPlayer.pause();
                }
                else{
                    music_pause.setText("暂停");
                    CollectActivity.mediaPlayer.start();
                    thread = new Thread(new SeekBarThread());
                    thread.start();
                }
            }
        });

        Button music_loop1 = findViewById(R.id.music_loop1);
        music_loop1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (collect_flag == 0){
                    if (TotalMusicListActivity.mediaPlayer.isLooping()) {
                        Toast.makeText(MainActivity.this, "取消单曲循环", Toast.LENGTH_SHORT).show();
                        TotalMusicListActivity.mediaPlayer.setLooping(false);
                    } else {
                        Toast.makeText(MainActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
                        TotalMusicListActivity.mediaPlayer.setLooping(true);
                    }
                }
                else{
                    if (CollectActivity.mediaPlayer.isLooping()) {
                        Toast.makeText(MainActivity.this, "取消单曲循环", Toast.LENGTH_SHORT).show();
                        CollectActivity.mediaPlayer.setLooping(false);
                    } else {
                        Toast.makeText(MainActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
                        CollectActivity.mediaPlayer.setLooping(true);
                    }
                }
            }
        });

        seekBar = findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    if(collect_flag == 0) {
                        Log.d(TAG, "onStopTrackingTouch: " + (int) (seekBar.getProgress() / 100.0 * TotalMusicListActivity.mediaPlayer.getDuration()));
                        TotalMusicListActivity.mediaPlayer.seekTo((int) (seekBar.getProgress() / 100.0 * TotalMusicListActivity.mediaPlayer.getDuration()));
                        TotalMusicListActivity.mediaPlayer.start();
                    }
                    else{
                        Log.d(TAG, "onStopTrackingTouch: " + (int) (seekBar.getProgress() / 100.0 * CollectActivity.mediaPlayer.getDuration()));
                        CollectActivity.mediaPlayer.seekTo((int) (seekBar.getProgress() / 100.0 * CollectActivity.mediaPlayer.getDuration()));
                        CollectActivity.mediaPlayer.start();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });

        Button musicCollection = findViewById(R.id.music_collection);
        musicCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = musicDBHelper.getWritableDatabase();
                String sql = "update music set isCollect=1 where id=?";
                db.execSQL(sql,new Object[]{musicPlaying.getId()});
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 0 & resultCode == 0){
            musicPlaying = (Music) data.getSerializableExtra("music");
            TextView music_playing = findViewById(R.id.now_playing);
            if(TotalMusicListActivity.mediaPlayer.isPlaying() || CollectActivity.mediaPlayer.isPlaying()) {
                music_playing.setText(musicPlaying.getSinger() + " - " + musicPlaying.getSongName());
                Log.d(TAG, "onActivityResult: " + musicPlaying.toString());
            }
            refreshPlayingList();
        }
    }

    public void refreshPlayingList(){
        ListView listView = findViewById(R.id.playing);
        MusicAdapter adapter = new MusicAdapter(MainActivity.this,R.layout.music_item,playingList);
        listView.setAdapter(adapter);
//        Log.d(TAG, "refreshPlayingList: "+playingList);
    }

    public void initMusic() throws IOException {
        playingList.clear();
        SQLiteDatabase db = musicDBHelper.getWritableDatabase();
        if(isEmpty()) {
            String[] files;
            files = getAssets().list("songs");
            for (int i = 0; i < files.length; i++) {
                String[] data = files[i].split(" - ");
                Music music = new Music(i,0,0,data[1], data[0]);
                musicList.add(music);
                String sql = "insert into music(isCollect,isPlaying,songName,singer) values (?,?,?,?)";
                db.execSQL(sql, new Object[]{0,0, musicList.get(i).getSongName(), musicList.get(i).getSinger()});
            }
        }else{
            Cursor cursor = db.rawQuery("select * from music",null);
            if(cursor.moveToFirst()){
                do{
                    int id = cursor.getInt(0);
                    int isPlaying = cursor.getInt(1);
                    int isCollect = cursor.getInt(2);
                    String songName = cursor.getString(3);
                    String singer = cursor.getString(4);
                    Music music = new Music(id, isCollect, isPlaying,songName,singer);
                    if(isPlaying==1){
                        playingList.add(music);
                    }
                    musicList.add(music);
                }while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        }
    }

    public boolean isEmpty(){
        SQLiteDatabase db = musicDBHelper.getReadableDatabase();
        String sql = "select * from music";
        Cursor cursor = db.rawQuery(sql,new String[]{});
        if(cursor.moveToFirst())
            return false;
        else
            return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMusicList();
        refreshPlayingList();
        if(TotalMusicListActivity.mediaPlayer.isPlaying() && collect_flag ==0) {
            TextView end_time = findViewById(R.id.end_time);
            end_time.setText(TotalMusicListActivity.mediaPlayer.getDuration()/1000/60+":"+TotalMusicListActivity.mediaPlayer.getDuration()/1000%60);
            Log.d(TAG, "onResume: "+TotalMusicListActivity.mediaPlayer.getDuration());
            thread = new Thread(new SeekBarThread());
            thread.start();
        }
        else if(CollectActivity.mediaPlayer.isPlaying() && collect_flag ==1){
            TextView end_time = findViewById(R.id.end_time);
            end_time.setText(CollectActivity.mediaPlayer.getDuration()/1000/60+":"+CollectActivity.mediaPlayer.getDuration()/1000%60);
            Log.d(TAG, "onResume: "+CollectActivity.mediaPlayer.getDuration());
            thread = new Thread(new SeekBarThread());
            thread.start();
        }
    }

    public void refreshMusicList(){
        musicList.clear();
        playingList.clear();
        SQLiteDatabase db = musicDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from music",null);
        if(cursor.moveToFirst()){
            do{
                int id = cursor.getInt(0);
                int isPlaying = cursor.getInt(1);
                int isCollect = cursor.getInt(2);
                String songName = cursor.getString(3);
                String singer = cursor.getString(4);
                Music music = new Music(id, isCollect,isPlaying, songName,singer);
                if(isPlaying==1){
                    playingList.add(music);
                }
                musicList.add(music);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.collect_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsMenuClosed: ");
        Intent intent = new Intent(MainActivity.this,CollectActivity.class);
        startActivityForResult(intent,0);
        return true;
    }

    @Override
    protected void onDestroy() {
        SQLiteDatabase db = musicDBHelper.getWritableDatabase();
        String sql = "update music set isPlaying=0 ";
        db.execSQL(sql,new Object[]{});
        super.onDestroy();
    }

    class SeekBarThread implements Runnable {

        @Override
        public void run() {
            while (TotalMusicListActivity.mediaPlayer != null && TotalMusicListActivity.mediaPlayer.isPlaying() && collect_flag == 0) {
                Message message1 = new Message();
                message1.what = 1;
                handler.sendMessage(message1);
                Message message2 = new Message();
                message2.what = 2;
                handler.sendMessage(message2);
                try {
                    Thread.sleep(80);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            while (CollectActivity.mediaPlayer != null && CollectActivity.mediaPlayer.isPlaying() && collect_flag == 1) {
                Message message1 = new Message();
                message1.what = 3;
                handler.sendMessage(message1);
                Message message2 = new Message();
                message2.what = 4;
                handler.sendMessage(message2);
                try {
                    Thread.sleep(80);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
