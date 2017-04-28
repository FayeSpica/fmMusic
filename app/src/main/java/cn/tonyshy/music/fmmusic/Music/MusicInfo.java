package cn.tonyshy.music.fmmusic.Music;

import android.graphics.Bitmap;
import android.net.Uri;

/**
* Created by Liaowm5 on 2016-12-28.
*/

public class MusicInfo {

    private String musicId;

    /** 歌曲名 */
    private String musicTitle;

    /** 歌曲专辑 */
    private String musicAlbum;

    /** 歌曲演唱者 */
    private String musicArtist;

    /** 歌曲地址 */
    private String musicPath;

    /** 歌曲时间长度 */
    private String musicDuration;

    /** 专辑封面id*/
    private int album_id;

    private Uri album_uri;

    public Uri getAlbum_uri() {
        return album_uri;
    }

    public void setAlbum_uri(Uri album_uri) {
        this.album_uri = album_uri;
    }

    public int getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(int album_id) {
        this.album_id = album_id;
    }

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public MusicInfo() {
        super();
        // TODO Auto-generated constructor stub
    }



    public MusicInfo(String musicTitle, String musicAlbum, String musicArtist,
                     String musicPath, String musicDuration) {
        super();
        this.musicTitle = musicTitle;
        this.musicAlbum = musicAlbum;
        this.musicArtist = musicArtist;
        this.musicPath = musicPath;
        this.musicDuration = musicDuration;
    }



    public String getmusicTitle() {
        return musicTitle;
    }

    public void setmusicTitle(String musicTitle) {
        this.musicTitle = musicTitle;
    }

    public String getmusicAlbum() {
        return musicAlbum;
    }

    public void setmusicAlbum(String musicAlbum) {
        this.musicAlbum = musicAlbum;
    }

    public String getMusicArtist() {
        return musicArtist;
    }

    public void setMusicArtist(String musicArtist) {
        this.musicArtist = musicArtist;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public String getmusicDuration() {
        return musicDuration;
    }

    public void setmusicDuration(String musicDuration) {
        this.musicDuration = musicDuration;
    }


}