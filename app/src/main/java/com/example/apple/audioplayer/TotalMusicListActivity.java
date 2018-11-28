package com.example.apple.audioplayer;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TotalMusicListActivity extends AppCompatActivity {

    private MusicDBHelper musicDBHelper;
    private static final String TAG = "miao";
    private List<Music> musicList = new ArrayList<>();
    private MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_music_list);
        musicDBHelper = new MusicDBHelper(this,"Music.db",null,1);
        ListView listView = findViewById(R.id.total_music);
        try {
            initMusic();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MusicAdapter adapter = new MusicAdapter(TotalMusicListActivity.this,R.layout.music_item,musicList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music music = musicList.get(position);
                music.setIsPlaying(1);
                mediaPlayer.reset();
                AssetManager assetManager = getAssets();
                try {
                    AssetFileDescriptor assetFileDescriptor = assetManager.openFd("songs/"+music.getSinger()+" - "+music.getSongName());
                    mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(),assetFileDescriptor.getStartOffset(),assetFileDescriptor.getLength());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    music.setIsPlaying(1);
                    updatePlayingStat(music);
                    Log.d(TAG, "onItemClick: "+"start");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void initMusic() throws IOException {
        SQLiteDatabase db = musicDBHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from music",null);
        if(cursor.moveToFirst()){
            do{
                int id = cursor.getInt(0);
                int isPlaying = cursor.getInt(1);
                int isCollect = cursor.getInt(2);
                String songName = cursor.getString(3);
                String singer = cursor.getString(4);
                Music music = new Music(id, isCollect,isPlaying, songName,singer);
                musicList.add(music);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

    public void updatePlayingStat(Music music){
        SQLiteDatabase db = musicDBHelper.getWritableDatabase();
        String sql = "update music set isPlaying=1 where id=?";
        db.execSQL(sql,new Object[]{music.getId()});
    }

}