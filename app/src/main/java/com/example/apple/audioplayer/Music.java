package com.example.apple.audioplayer;

public class Music {

    public int id;
    public int isCollect;
    public int isPlaying;
    public String singer;
    public String songName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIsCollect() {
        return isCollect;
    }

    public void setIsCollect(int isCollect) {
        this.isCollect = isCollect;
    }

    public int getIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(int isPlaying) {
        this.isPlaying = isPlaying;
    }

    public Music(int id, int isCollect, int isPlaying, String songName, String singer) {

        this.id = id;
        this.isCollect = isCollect;
        this.isPlaying = isPlaying;
        this.singer = singer;
        this.songName = songName;
    }

    public Music(String singer, String songName) {
        this.singer = singer;
        this.songName = songName;
    }

    public String getSinger() {

        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    @Override
    public String toString() {
        return "Music{" +
                "id=" + id +
                ", isCollect=" + isCollect +
                ", isPlaying=" + isPlaying +
                ", singer='" + singer + '\'' +
                ", songName='" + songName + '\'' +
                '}';
    }
}
