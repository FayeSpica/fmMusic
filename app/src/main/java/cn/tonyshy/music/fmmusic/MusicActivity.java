package cn.tonyshy.music.fmmusic;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import cn.tonyshy.music.fmmusic.Music.ImageLoader;
import cn.tonyshy.music.fmmusic.Music.MusicInfo;
import cn.tonyshy.music.fmmusic.Music.MusicListAdapter;

import java.text.SimpleDateFormat;

/**
 * Created by Liaowm5 on 2016-12-27.
 */

public class MusicActivity extends AppCompatActivity implements View.OnClickListener {
            /*
    音乐部分
    */

    private MusicService musicService;
    private SeekBar seekBar;
    private TextView DTime,LeftTime;
    private ImageButton btnPlayOrPause, btnPre, btnNext;
    private ComponentName component;
    private SimpleDateFormat time = new SimpleDateFormat("m:ss");
    private TabHost tabhost;
    private boolean isBind = false ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_music_main);

        onMusicCreate();//音乐服务初始化

        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        toolbar.setBackgroundColor(getResources().getColor(R.color.theme_color_primary));
        //Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        //返回按钮

        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        //得到TabHost对象实例
        tabhost =(TabHost) findViewById(R.id.musicTab);
        //调用 TabHost.setup()
        tabhost.setup();
        //创建Tab标签
        tabhost.addTab(tabhost.newTabSpec("one").setContent(R.id.tab1).setIndicator("正在播放"));
        tabhost.addTab(tabhost.newTabSpec("two").setContent(R.id.tab2).setIndicator("列表"));
        tabhost.setOnTabChangedListener(new musicTabChangedLiListener());
        //（重要：设定Taps的标题的字体大小、对齐方式等）
        for (int i = 0; i < tabhost.getTabWidget().getChildCount(); i++)
        {
            //获取标题
            TextView tabs_title = (TextView)tabhost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            //定义字体大小
            tabs_title.setTextSize(16);
            tabs_title.setTextColor(getResources().getColor(R.color.white));
        }
        //tab音乐列表
        ListView listView = (ListView) findViewById(R.id.musicPlayList);
        MusicListAdapter musicAdapter =new MusicListAdapter(listView,MainActivity.musicList,this,MusicListAdapter.MUSIC_ACTIVITY);
        Log.d("liaowm7",""+MainActivity.musicList.size());
        listView.setAdapter(musicAdapter);

        //音乐列表监听
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView1 = (ListView) findViewById(R.id.musicPlayList);
                String text = listView1.getItemAtPosition(position)+"";
                MusicInfo musicInfoTmp=(MusicInfo)MainActivity.musicList.get(position);
                Toast.makeText(getApplicationContext(),musicInfoTmp.getmusicTitle(), Toast.LENGTH_SHORT).show();
                musicService.setmusicDirList(MainActivity.musicList);
                Log.d("liaowm6","size()="+musicService.getmusicDirList().size());
                musicService.playAt(position);
                //musicService.setmusicDirList(arrayList);
                //Log.d("liaowm5","arrayList.size()="+arrayList.size())

                musicService.mp.start();
            }
        });
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return false;
            }
        });
        Log.d("LifeCycle", "MusicActivity onCreate: ");
    }
    @Override
    public void startActivity(Intent intent) {
        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        super.startActivity(intent);
        overridePendingTransition(R.anim.in_left,R.anim.out_right);
    }
    @Override
    public void finish() {
        super.finish();
        //overridePendingTransition(0, 0);
        overridePendingTransition(R.anim.in_left,R.anim.out_right);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                try{
                    if (isBind) {
                        unbindService(sc);
                        isBind = false;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("liaowm8","unbindService(sc);");
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MyBinder)iBinder).getService();
            isBind=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService = null;
            isBind=false;
            Toast.makeText(MusicActivity.this, "Service Failed", Toast.LENGTH_SHORT).show();
        }
    };
    private void bindServiceConnection() {
        Intent intent = new Intent(MusicActivity.this, MusicService.class);
        //startService(intent);
        isBind=bindService(intent, sc, this.BIND_AUTO_CREATE);
    }
    public android.os.Handler handler = new android.os.Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {

            //动态更新播放模式UI  //0 顺序 1 列表循环 2 随机 3 单曲循环
            ImageView playMode=(ImageView)findViewById(R.id.playMode);
            switch (musicService.playmode){
                case 1:playMode.setImageResource(R.drawable.play_icn_loop);break;
                case 2:playMode.setImageResource(R.drawable.play_icn_shuffle);break;
                case 3:playMode.setImageResource(R.drawable.play_icn_one);break;
            }
            //动态更新专辑封面
            ImageView imageView=(ImageView) findViewById(R.id.albumImageView);
            Log.d("liaowm5","musicService.getmusicDirList().size()"+musicService.getmusicDirList().size());
            if(musicService.getCurrentPlayingInfo()!=null) {
                Log.d("liaowm5","musicService.getmusicDirList().size()"+musicService.getCurrentPlayingInfo().getmusicTitle());
                MusicInfo musicInfo = musicService.getCurrentPlayingInfo();
                imageView.setImageBitmap(ImageLoader.customGetBitmapFromUri(MusicActivity.this,musicInfo.getAlbum_uri(),imageView.getWidth(),imageView.getHeight()));
                //动态更新toolbar
                Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
                toolbar.setTitle(musicService.getCurrentPlayingInfo().getmusicTitle());
                toolbar.setSubtitle(musicService.getCurrentPlayingInfo().getMusicArtist());
            }
            //定时更新播放UI
            if(musicService.mp.isPlaying())
                btnPlayOrPause.setImageResource(R.drawable.play_btn_pause_w);
            else
                btnPlayOrPause.setImageResource(R.drawable.play_btn_play_w);

            DTime.setText(time.format(musicService.mp.getCurrentPosition()));
            LeftTime.setText("-"+time.format(musicService.mp.getDuration()-musicService.mp.getCurrentPosition()));
            seekBar.setMax(musicService.mp.getDuration());
            seekBar.setProgress(musicService.mp.getCurrentPosition());
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        musicService.mp.seekTo(seekBar.getProgress());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            handler.postDelayed(runnable, 100);
        }
    };

    protected void onMusicCreate() {

        Log.d("hint", "ready to new MusicService");
        musicService = new MusicService();
        Log.d("hint", "finish to new MusicService");
        bindServiceConnection();

        seekBar = (SeekBar)this.findViewById(R.id.MusicSeekBar);
        seekBar.setProgress(musicService.mp.getCurrentPosition());
        seekBar.setMax(musicService.mp.getDuration());

        DTime = (TextView)findViewById(R.id.Dtime);
        LeftTime=(TextView)findViewById(R.id.LeftTime);

        btnPlayOrPause = (ImageButton)findViewById(R.id.BtnPlayorPause);
        btnPre = (ImageButton)findViewById(R.id.btnPre);
        btnNext=(ImageButton)findViewById(R.id.btnNext);

        btnPlayOrPause.setOnClickListener(this);
        btnPre.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        component = new ComponentName(this, MusicService.class);
    }

    @Override
    protected void onResume() {
        seekBar.setProgress(musicService.mp.getCurrentPosition());
        seekBar.setMax(musicService.mp.getDuration());
        handler.post(runnable);
        super.onResume();
        Intent mIntent = new Intent(MusicService.NOTIFY);
        mIntent.setComponent(component);
        startService(mIntent);
        Log.d("hint", "handler post runnable");
    }
    public void onPlayModeOclick(View view){
        if(musicService.playmode!=3)
            musicService.playmode++;
        else
            musicService.playmode=1;

    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.BtnPlayorPause:
                Intent mIntent = new Intent(MusicService.PLAY_OR_PAUSE_ACTION);
                mIntent.setComponent(component);
                startService(mIntent);
                break;
            /*
            case R.id.BtnStop:
                musicService.stop();
                seekBar.setProgress(0);
                break;
            case R.id.BtnQuit:
                handler.removeCallbacks(runnable);
                unbindService(sc);
                try {
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;*/
            case R.id.btnPre:
                mIntent = new Intent(MusicService.PREVIOUS_ACTION);
                mIntent.setComponent(component);
                startService(mIntent);
                Toast.makeText(getApplicationContext(),"上一首", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnNext:
                mIntent = new Intent(MusicService.NEXT_ACTION);
                mIntent.setComponent(component);
                startService(mIntent);
                Toast.makeText(getApplicationContext(),"下一首", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        Log.d("LifeCycle", "MusicActivity onNewIntent: ");
    }
    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        //Intent intent = new Intent(MusicActivity.this, MusicService.class);
        //stopService(intent);
        super.onDestroy();
        Log.d("LifeCycle", "MusicActivity onDestroy: ");
    }
    /**
     *  tabHost 部分
     */
    private final class musicTabChangedLiListener implements TabHost.OnTabChangeListener {
        @Override
        public void onTabChanged(String tabId) {
            tabIndex = tabhost.getCurrentTab();

            //System.out.println(mTabHost.getTabContentView().getChildCount());
            if(tabIndex==1){
                tabhost.getTabContentView().getChildAt(0).startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.out_left));
                tabhost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.in_right));
            }
            else{
                tabhost.getTabContentView().getChildAt(1).startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.out_right));
                tabhost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.in_left));
            }
        }
    }
    /**
     * 手势监听
     */
    @SuppressWarnings("deprecation")
    private GestureDetector gestureDetector = new GestureDetector(
            new GestureDetector.SimpleOnGestureListener() {
                public boolean onFling(MotionEvent e1,
                                       MotionEvent e2, float velocityX,
                                       float velocityY) {
                    if ((e2.getRawX() - e1.getRawX()) > 400) {
                        showNext();
                        return true;
                    }

                    if ((e1.getRawX() - e2.getRawX()) > 400) {
                        showPre();
                        return true;
                    }
                    return super.onFling(e1, e2, velocityX, velocityY);
                }

            });

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(gestureDetector.onTouchEvent(event))
            return true;
        return false;
    }


    /**
     * 显示下一个页面
     */
    int tabIndex=0;
    protected void showNext() {
        tabhost.setCurrentTab(tabIndex = tabIndex == 1 ? tabIndex = 0 : ++tabIndex);
    }

    /**
     * 显示前一个页面
     */
    protected void showPre() {
        // 三元表达式控制2个页面的循环.
        tabhost.setCurrentTab(tabIndex = tabIndex == 0 ? tabIndex = 1 : --tabIndex);
    }

}
