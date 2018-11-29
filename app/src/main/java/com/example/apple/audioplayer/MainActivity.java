package com.example.apple.audioplayer;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
    public static int isMain = 0;
    private MusicDBHelper musicDBHelper;
    private static final String TAG = "miao";
    private List<Music> musicList = new ArrayList<>();
    private List<Music> playingList = new ArrayList<>();
    private Music musicPlaying;
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar seekBar;
    private Thread thread;
    private boolean isLooping=false;
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what) {
                case 1:
                    if (seekBar.getProgress() != TotalMusicListActivity.mediaPlayer.getCurrentPosition() * 100 / TotalMusicListActivity.mediaPlayer.getDuration()){
 //                       Log.d(TAG, "handleMessage: " + TotalMusicListActivity.mediaPlayer.getCurrentPosition() * 100 / TotalMusicListActivity.mediaPlayer.getDuration());
                        seekBar.setProgress(TotalMusicListActivity.mediaPlayer.getCurrentPosition() * 100 / TotalMusicListActivity.mediaPlayer.getDuration());
                    }
                    break;
                case 2:
                    TextView startTime = findViewById(R.id.start_time);
                    if(startTime.getText().toString().trim()!=TotalMusicListActivity.mediaPlayer.getCurrentPosition()/1000/60+":"+TotalMusicListActivity.mediaPlayer.getCurrentPosition()/1000%60) {
 //                       Log.d(TAG, "handleMessage: " + TotalMusicListActivity.mediaPlayer.getCurrentPosition() / 1000 / 60 + ":" + TotalMusicListActivity.mediaPlayer.getCurrentPosition() / 1000 % 60);
                        startTime.setText(TotalMusicListActivity.mediaPlayer.getCurrentPosition() / 1000 / 60 + ":" + TotalMusicListActivity.mediaPlayer.getCurrentPosition() / 1000 % 60);
                    }
                    break;
                case 3:
                    if (seekBar.getProgress() != CollectActivity.mediaPlayer.getCurrentPosition() * 100 / CollectActivity.mediaPlayer.getDuration()){
 //                       Log.d(TAG, "handleMessage: " + CollectActivity.mediaPlayer.getCurrentPosition() * 100 / CollectActivity.mediaPlayer.getDuration());
                        seekBar.setProgress(CollectActivity.mediaPlayer.getCurrentPosition() * 100 / CollectActivity.mediaPlayer.getDuration());
                    }
                    break;
                case 4:
                    startTime = findViewById(R.id.start_time);
                    if(startTime.getText().toString().trim()!=CollectActivity.mediaPlayer.getCurrentPosition()/1000/60+":"+CollectActivity.mediaPlayer.getCurrentPosition()/1000%60) {
 //                       Log.d(TAG, "handleMessage: " + CollectActivity.mediaPlayer.getCurrentPosition() / 1000 / 60 + ":" + CollectActivity.mediaPlayer.getCurrentPosition() / 1000 % 60);
                        startTime.setText(CollectActivity.mediaPlayer.getCurrentPosition() / 1000 / 60 + ":" + CollectActivity.mediaPlayer.getCurrentPosition() / 1000 % 60);
                    }
                    break;
                case 5:
                    if (seekBar.getProgress() != mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration()){
                        //                       Log.d(TAG, "handleMessage: " + mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration());
                        seekBar.setProgress(mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration());
                    }
                    break;
                case 6:
                    startTime = findViewById(R.id.start_time);
                    if(startTime.getText().toString().trim()!=mediaPlayer.getCurrentPosition()/1000/60+":"+mediaPlayer.getCurrentPosition()/1000%60) {
                        //                       Log.d(TAG, "handleMessage: " + mediaPlayer.getCurrentPosition() / 1000 / 60 + ":" + mediaPlayer.getCurrentPosition() / 1000 % 60);
                        startTime.setText(mediaPlayer.getCurrentPosition() / 1000 / 60 + ":" + mediaPlayer.getCurrentPosition() / 1000 % 60);
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
                mediaPlayer.stop();
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
                if(collect_flag == 0 && isMain == 0) {
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
                else if(collect_flag == 1 && isMain == 0) {
                    if (CollectActivity.mediaPlayer.isPlaying()) {
                        music_pause.setText("播放");
                        CollectActivity.mediaPlayer.pause();
                    } else {
                        music_pause.setText("暂停");
                        CollectActivity.mediaPlayer.start();
                        thread = new Thread(new SeekBarThread());
                        thread.start();
                    }
                }
                else{
                    if (mediaPlayer.isPlaying()) {
                        music_pause.setText("播放");
                        mediaPlayer.pause();
                    } else {
                        music_pause.setText("暂停");
                        mediaPlayer.start();
                        thread = new Thread(new SeekBarThread());
                        thread.start();
                    }
                }
            }
        });

        Button music_loop1 = findViewById(R.id.music_loop1);
        music_loop1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLooping = false;
                if (collect_flag == 0 && isMain == 0){
                    if (TotalMusicListActivity.mediaPlayer.isLooping()) {
                        Toast.makeText(MainActivity.this, "取消单曲循环", Toast.LENGTH_SHORT).show();
                        TotalMusicListActivity.mediaPlayer.setLooping(false);
                    } else {
                        Toast.makeText(MainActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
                        TotalMusicListActivity.mediaPlayer.setLooping(true);
                    }
                }
                else if(collect_flag == 1 && isMain == 0){
                    if (CollectActivity.mediaPlayer.isLooping()) {
                        Toast.makeText(MainActivity.this, "取消单曲循环", Toast.LENGTH_SHORT).show();
                        CollectActivity.mediaPlayer.setLooping(false);
                    } else {
                        Toast.makeText(MainActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
                        CollectActivity.mediaPlayer.setLooping(true);
                    }
                }
                else{
                    if (mediaPlayer.isLooping()) {
                        Toast.makeText(MainActivity.this, "取消单曲循环", Toast.LENGTH_SHORT).show();
                        mediaPlayer.setLooping(false);
                    } else {
                        Toast.makeText(MainActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
                        mediaPlayer.setLooping(true);
                    }
                }
            }
        });

        seekBar = findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    if(collect_flag == 0 && isMain == 0) {
//                        Log.d(TAG, "onStopTrackingTouch: " + (int) (seekBar.getProgress() / 100.0 * TotalMusicListActivity.mediaPlayer.getDuration()));
                        TotalMusicListActivity.mediaPlayer.seekTo((int) (seekBar.getProgress() / 100.0 * TotalMusicListActivity.mediaPlayer.getDuration()));
                        TotalMusicListActivity.mediaPlayer.start();
                    }
                    else if(collect_flag == 1 && isMain == 0){
//                        Log.d(TAG, "onStopTrackingTouch: " + (int) (seekBar.getProgress() / 100.0 * CollectActivity.mediaPlayer.getDuration()));
                        CollectActivity.mediaPlayer.seekTo((int) (seekBar.getProgress() / 100.0 * CollectActivity.mediaPlayer.getDuration()));
                        CollectActivity.mediaPlayer.start();
                    }
                    else{
                        mediaPlayer.seekTo((int) (seekBar.getProgress() / 100.0 * mediaPlayer.getDuration()));
                        mediaPlayer.start();
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


        Button music_last = findViewById(R.id.music_last);
        music_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0;i<playingList.size();i++){
                    if(playingList.get(i).getId() == musicPlaying.getId()){
                        TotalMusicListActivity.mediaPlayer.stop();
                        CollectActivity.mediaPlayer.stop();
                        Log.d(TAG, "onClick: "+i);
                        if((i-1)!=-1) {
                            musicPlaying = playingList.get(i - 1);
                        }
                        else {
                            musicPlaying = playingList.get(playingList.size() - 1);
                        }
                        musicPlaying.setIsPlaying(1);
                        mediaPlayer.reset();
                        AssetManager assetManager = getAssets();
                        try {
                            TotalMusicListActivity.mediaPlayer.stop();
                            CollectActivity.mediaPlayer.stop();
                            AssetFileDescriptor assetFileDescriptor = assetManager.openFd("songs/" + musicPlaying.getSinger() + " - " + musicPlaying.getSongName());
                            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            isMain = 1;
                            TextView music_playing = findViewById(R.id.now_playing);
                            music_playing.setText(musicPlaying.getSinger() + " - " + musicPlaying.getSongName());
                            TextView end_time = findViewById(R.id.end_time);
                            end_time.setText(mediaPlayer.getDuration()/1000/60+":"+mediaPlayer.getDuration()/1000%60);
                            thread = new Thread(new SeekBarThread());
                            thread.start();
                            Log.d(TAG, "onItemClick: " + "start");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        });

        Button musicNext = findViewById(R.id.music_next);
        musicNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0;i<playingList.size();i++){
                    if(playingList.get(i).getId() == musicPlaying.getId()){
                        TotalMusicListActivity.mediaPlayer.stop();
                        CollectActivity.mediaPlayer.stop();
                        Log.d(TAG, "onClick: "+i);
                        if((i+1)!=playingList.size()) {
                            musicPlaying = playingList.get(i + 1);
                        }
                        else {
                            musicPlaying = playingList.get(0);
                        }
                        musicPlaying.setIsPlaying(1);
                        mediaPlayer.reset();
                        AssetManager assetManager = getAssets();
                        try {
                            TotalMusicListActivity.mediaPlayer.stop();
                            CollectActivity.mediaPlayer.stop();
                            AssetFileDescriptor assetFileDescriptor = assetManager.openFd("songs/" + musicPlaying.getSinger() + " - " + musicPlaying.getSongName());
                            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            isMain = 1;
                            TextView music_playing = findViewById(R.id.now_playing);
                            music_playing.setText(musicPlaying.getSinger() + " - " + musicPlaying.getSongName());
                            TextView end_time = findViewById(R.id.end_time);
                            end_time.setText(mediaPlayer.getDuration()/1000/60+":"+mediaPlayer.getDuration()/1000%60);
                            thread = new Thread(new SeekBarThread());
                            thread.start();
                            Log.d(TAG, "onItemClick: " + "start");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(isLooping) {
                    for (int i = 0; i < playingList.size(); i++) {
                        Log.d(TAG, "onCompletion: " + musicPlaying.getId());
                        if (playingList.get(i).getId() == musicPlaying.getId()) {
                            if ((i + 1) != playingList.size()) {
                                musicPlaying = playingList.get(i + 1);
                            } else {
                                musicPlaying = playingList.get(0);
                            }
                            musicPlaying.setIsPlaying(1);
                            mediaPlayer.reset();
                            AssetManager assetManager = getAssets();
                            try {
                                TotalMusicListActivity.mediaPlayer.stop();
                                CollectActivity.mediaPlayer.stop();
                                AssetFileDescriptor assetFileDescriptor = assetManager.openFd("songs/" + musicPlaying.getSinger() + " - " + musicPlaying.getSongName());
                                mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                                isMain = 1;
                                TextView music_playing = findViewById(R.id.now_playing);
                                music_playing.setText(musicPlaying.getSinger() + " - " + musicPlaying.getSongName());
                                TextView end_time = findViewById(R.id.end_time);
                                end_time.setText(mediaPlayer.getDuration() / 1000 / 60 + ":" + mediaPlayer.getDuration() / 1000 % 60);
                                thread = new Thread(new SeekBarThread());
                                thread.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        });

        Button musicLoop2 = findViewById(R.id.music_loop2);
        musicLoop2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 if(isLooping) {
                     Toast.makeText(MainActivity.this,"结束循环播放",Toast.LENGTH_SHORT).show();
                     isLooping = false;
                     mediaPlayer.setLooping(false);
                 }
                 else {
                     Toast.makeText(MainActivity.this,"开始循环播放",Toast.LENGTH_SHORT).show();
                     isLooping = true;
                     mediaPlayer.setLooping(false);
                 }
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
                //Log.d(TAG, "onActivityResult: " + musicPlaying.toString());
            }
            refreshPlayingList();
        }
    }

    public void refreshPlayingList(){
        ListView listView = findViewById(R.id.playing);
        MusicAdapter adapter = new MusicAdapter(MainActivity.this,R.layout.music_item,playingList);
        listView.setAdapter(adapter);
//        Log.d(TAG, "refreshPlayingList: "+playingList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                musicPlaying = playingList.get(position);
                mediaPlayer.reset();
                AssetManager assetManager = getAssets();
                try {
                    TotalMusicListActivity.mediaPlayer.stop();
                    CollectActivity.mediaPlayer.stop();
                    AssetFileDescriptor assetFileDescriptor = assetManager.openFd("songs/" + musicPlaying.getSinger() + " - " + musicPlaying.getSongName());
                    mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    isMain = 1;
                    TextView music_playing = findViewById(R.id.now_playing);
                    music_playing.setText(musicPlaying.getSinger() + " - " + musicPlaying.getSongName());
                    TextView end_time = findViewById(R.id.end_time);
                    end_time.setText(mediaPlayer.getDuration()/1000/60+":"+mediaPlayer.getDuration()/1000%60);
                    thread = new Thread(new SeekBarThread());
                    thread.start();
                    Log.d(TAG, "onItemClick: " + "start");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.context_menu,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo musicInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        Music music = playingList.get(musicInfo.position);
        Log.e(TAG, "onContextItemSelected: "+music.toString());
        String sql = "update music set isPlaying=0 where id=?";
        SQLiteDatabase db = musicDBHelper.getWritableDatabase();
        db.execSQL(sql,new Object[]{music.getId()});
        db.close();
        db = musicDBHelper.getWritableDatabase();
        db.execSQL("update sqlite_sequence set seq=0 where name='note'");
        refreshMusicList();
        refreshPlayingList();
        return true;
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
//        Log.d(TAG, "onOptionsMenuClosed: ");
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
            while (TotalMusicListActivity.mediaPlayer != null && TotalMusicListActivity.mediaPlayer.isPlaying() && collect_flag == 0 && isMain == 0) {
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

            while (mediaPlayer != null && mediaPlayer.isPlaying() && isMain == 1) {
                Message message1 = new Message();
                message1.what = 5;
                handler.sendMessage(message1);
                Message message2 = new Message();
                message2.what = 6;
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
