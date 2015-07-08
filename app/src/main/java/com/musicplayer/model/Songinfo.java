package com.musicplayer.model;

/**
 * Created by WangZ on 2015/6/29.
 *  mp3歌曲模型类，最主要的是存储路径
 */
public class Songinfo{
    //歌曲编号
    private long id;
    //歌曲标题
    private String title;
    //歌曲的专辑名：MediaStore.Audio.Media.ALBUM
    private String album;
    private long album_id;
    //歌曲的歌手名： MediaStore.Audio.Media.ARTIST
    private String artist;
    //歌曲文件的路径 ：MediaStore.Audio.Media.DATA
    private String url;
    //歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
    private int duration;
    //歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
    private long size;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(long album_id) {
        this.album_id = album_id;
    }
}
