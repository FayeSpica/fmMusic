package cn.tonyshy.music.fmmusic;

/**
 * Created by Liaowm5 on 2016-12-27.
 */
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import cn.tonyshy.music.fmmusic.Music.MusicInfo;

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service {

    private ArrayList<MusicInfo> musicDirList = new ArrayList<MusicInfo>();
    public int musicIndex = 0;
    public int playmode=1;//1 列表循环 2 随机 3 单曲循环
    public final IBinder binder = new MyBinder();
    public class MyBinder extends Binder{
        MusicService getService() {
            return MusicService.this;
        }
    }
    public static MediaPlayer mp = new MediaPlayer();

    public static final String PLAY_ACTION = "cn.tonyshy.music.fmmusic.MusicService.PLAY_ACTION";
    public static final String PLAY_OR_PAUSE_ACTION = "cn.tonyshy.music.fmmusic.MusicService.PLAY_OR_PAUSE_ACTION";
    public static final String PAUSE_ACTION = "cn.tonyshy.music.fmmusic.MusicService.PAUSE_ACTION";
    public static final String NEXT_ACTION = "cn.tonyshy.music.fmmusic.MusicService.NEXT_ACTION";
    public static final String PREVIOUS_ACTION = "cn.tonyshy.music.fmmusic.MusicService.PREVIOUS_ACTION";

    public MusicService() {
        /*
        try {
            mp.setDataSource(musicDirList.get(musicIndex).getMusicPath());
            //mp.setDataSource(Environment.getDataDirectory().getAbsolutePath()+"/Music/1.mp3");
            mp.prepare();
            musicIndex = 1;
        } catch (Exception e) {
            Log.d("hint","can't get to the song");
            e.printStackTrace();
        }*/
        //监听音频播放完的代码，实现音频的自动循环播放
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer arg0) {
                switch (playmode){
                    case 1:
                        nextMusic();
                        Log.d("liaowm10","next");
                        //mp.setLooping(false);
                        break;
                    case 2:
                        Random rand = new Random();
                        playAt(rand.nextInt(musicDirList.size()- 1));
                        Log.d("liaowm10","rand");
                        //mp.setLooping(false);
                        break;
                    case 3:
                        mp.seekTo(0);
                        mp.start();
                        mp.setLooping(true);
                        Log.d("liaowm10","one");
                        break;
                }

            }
        });
    }
    public void timing(int time) {
        Intent intent = new Intent(MusicService.PLAY_OR_PAUSE_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(MusicService.this, 0,intent ,
                0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + time, pendingIntent);
    }
    public void playAt(int index){
        try {
            mp.reset();
            musicIndex=index;
            mp.setDataSource(musicDirList.get(musicIndex).getMusicPath());
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            Log.d("hint","can't get to the song"+musicDirList.get(musicIndex).getMusicPath());
            Log.d("hint",Environment.getDataDirectory().getAbsolutePath());
            e.printStackTrace();
        }
    }
    public void playOrPause() {
        if(mp.isPlaying()){
            mp.pause();
        } else {
            mp.start();
        }
    }
    public void stop() {
        if(mp != null) {
            mp.stop();
            try {
                mp.prepare();
                mp.seekTo(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void nextMusic() {
        if(mp != null && musicIndex < musicDirList.size()) {
            switch (playmode){
                case 2:
                    Random rand = new Random();
                    playAt(rand.nextInt(musicDirList.size()- 1));
                    break;
                default:
                    if(musicIndex!=musicDirList.size()-1)
                        ++musicIndex;
                    else
                        musicIndex=0;
                    playAt(musicIndex);
                    break;
            }
        }
    }
    public void preMusic() {
        if(mp != null && musicIndex > 0) {
            switch (playmode){
                case 2:
                    Random rand = new Random();
                    playAt(rand.nextInt(musicDirList.size()- 1));
                    break;
                default:
                    if(musicIndex!=0)
                        --musicIndex;
                    else
                        musicIndex=musicDirList.size()-1;
                    playAt(musicIndex);
                    break;
            }
        }
    }
    @Override
    public void onStart(Intent intent, int startId) {
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer arg0) {
                switch (playmode){
                    case 3:
                        mp.seekTo(0);
                        mp.start();
                        mp.setLooping(true);
                        Log.d("liaowm10","one");
                        break;
                    default:
                        nextMusic();
                        Log.d("liaowm10","next");
                        break;
                }

            }
        });
        super.onStart(intent, startId);

        String action = intent.getAction();
        if(action!=null) {
            Log.d("liaowm11",action);
            if (action.equals(PLAY_ACTION)) {
                mp.start();
            } else if (action.equals(PLAY_OR_PAUSE_ACTION)) {
                Log.d("liaowm9", "PLAY_OR_PAUSE_ACTION");
                playOrPause();
            } else if (action.equals(PAUSE_ACTION)) {
                Log.d("liaowm9", "PAUSE_ACTION");
                mp.pause();
            } else if (action.equals(NEXT_ACTION)) {
                nextMusic();
            } else if (action.equals(PREVIOUS_ACTION)) {
                preMusic();
            }
        }
    }
    @Override
    public void onDestroy() {
        mp.stop();
        mp.release();
        super.onDestroy();
        System.exit(0);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setmusicDirList(ArrayList<MusicInfo> musicDirList){
        /*this.musicDirList=new ArrayList<MusicInfo>();
        for (MusicInfo m:musicDirList) {
            this.musicDirList.add(m);
        }*/
        Log.d("liaowm5","musicDirList.size()="+musicDirList.size());
        this.musicDirList=musicDirList;
        Log.d("liaowm5","this.musicDirList.size()="+this.musicDirList.size());
        musicIndex=0;
    }
    public ArrayList<MusicInfo> getmusicDirList(){
        return this.musicDirList;
    }

    public MusicInfo getCurrentPlayingInfo(){
        Log.d("liaowm5","return musicDirList.get(musicIndex) this.musicDirList.size()="+this.musicDirList.size());
        if(musicDirList.size()!=0)
            return musicDirList.get(musicIndex);
        else
            return null;
    }
}