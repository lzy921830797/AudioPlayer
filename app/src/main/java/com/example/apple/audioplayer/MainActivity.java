package com.example.apple.audioplayer;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MusicDBHelper musicDBHelper;
    private static final String TAG = "miao";
    private List<Music> musicList = new ArrayList<>();
    private List<Music> playingList = new ArrayList<>();
    private Music musicPlaying;

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

        //跳到歌单活动
        Button music_list = findViewById(R.id.music_list);
        music_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,TotalMusicListActivity.class);
                startActivityForResult(intent,1);
            }
        });


        Button music_stop = findViewById(R.id.music_stop);
        music_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 & resultCode == 0){
            musicPlaying = (Music) data.getSerializableExtra("music");
            TextView music_playing = findViewById(R.id.music_playing);
            music_playing.setText(musicPlaying.getSinger()+" - "+musicPlaying.getSongName());
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
        Log.d(TAG, "onResume: "+"onResume");
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
//                Log.d(TAG, "refreshMusicList: "+musicList);
//                Log.d(TAG, "refreshMusicList: "+playingList);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }


    @Override
    protected void onDestroy() {
        SQLiteDatabase db = musicDBHelper.getWritableDatabase();
        String sql = "update music set isPlaying=0 ";
        db.execSQL(sql,new Object[]{});
        super.onDestroy();
    }
}
