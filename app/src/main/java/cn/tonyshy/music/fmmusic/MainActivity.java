package cn.tonyshy.music.fmmusic;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.tonyshy.music.fmmusic.Music.ImageLoader;
import cn.tonyshy.music.fmmusic.Music.MusicInfo;
import cn.tonyshy.music.fmmusic.Music.MusicListAdapter;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean isBind = false ;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private boolean isPermmited=false;
    //列表显示部分
    private ListView listView;
    static ArrayList<MusicInfo> musicList;
    private List<String> paths;
    private List<File> fileList;
    private MusicListAdapter musicAdapter;
    private long times = 0;
    //
    static MusicService musicService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //权限申请
        permissionCheck();
        Log.d("LifeCycle", "MainActivity onCreate: ");
    }
    public void UIStart(){
        //音乐服务初始化
        serviceIni();
        //音乐初始化
        allMusic();

        //
        iniMusicFloatBar();
        Intent intent = new Intent(MainActivity.this,MusicActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0,intent ,
                0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        //am.set(AlarmManager.RTC, System.currentTimeMillis() + 1, pendingIntent);

        intent = new Intent(MusicService.PLAY_OR_PAUSE_ACTION);
        pendingIntent = PendingIntent.getService(MainActivity.this, 0,intent ,
                0);
        AlarmManager bm = (AlarmManager) getSystemService(ALARM_SERVICE);
        bm.set(AlarmManager.RTC, System.currentTimeMillis() + 1, pendingIntent);
    }
    public void permissionCheck(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            isPermmited=false;
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
        else{
            isPermmited=true;
        }
        if(isPermmited)
            UIStart();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode,grantResults);
    }
    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                isPermmited=true;
                UIStart();
            } else {
                // Permission Denied
                MainActivity.this.finish();
            }
        }
    }
    public void timing(int time) {
        Intent intent = new Intent(MusicService.PLAY_OR_PAUSE_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0,intent ,
                0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + time, pendingIntent);
    }
    public void serviceIni(){
        musicService = new MusicService();
        bindServiceConnection();
    }
    private void bindServiceConnection() {
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        getApplicationContext().startService(intent);
        getApplicationContext().bindService(intent, sc, this.BIND_AUTO_CREATE);
    }
    private ServiceConnection sc = new ServiceConnection() {
        boolean isBound=false;
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MyBinder)iBinder).getService();
            isBind=true;
            //Toast.makeText(MainActivity.this, "Service connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService = null;
            isBind=false;
            Toast.makeText(MainActivity.this, "Service Failed", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public void onDestroy() {
        if(handler!=null)
            handler.removeCallbacks(runnable);
        try{
            if (isBind) {
                getApplicationContext().unbindService(sc);
            }
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            getApplicationContext().stopService(intent);
            super.onDestroy();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Log.d("LifeCycle", "MainActivity onDestroy: ");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    /**
     * 双击返回桌面
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - times > 1000)) {
                Toast.makeText(this, "再按一次返回桌面", Toast.LENGTH_SHORT).show();
                times = System.currentTimeMillis();
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_list) {
            // Handle the camera action
        } else if (id == R.id.nav_playing) {
            startActivity(new Intent(MainActivity.this,MusicActivity.class));
            item.setChecked(false);
        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_time) {
            TimingFragment fragment3 = new TimingFragment();
            fragment3.show(getSupportFragmentManager(), "timing");
        } else if (id == R.id.nav_about) {
        }else if (id == R.id.nav_exit) {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            if(isBind)
                getApplicationContext().unbindService(sc);
            getApplicationContext().stopService(intent);
            System.exit(0);
            MainActivity.this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onMusicActivityClicked(View view){
        startActivity(new Intent(MainActivity.this,MusicActivity.class));
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        //mContentContainer.addView(mFloatView, layoutParams);
    }
    /**
     * 设置启动activity时没有动画
     * @param intent
     */
    @Override
    public void startActivity(Intent intent) {
        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        super.startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
        //overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    /**
     * 防止退出activity时闪烁
     */
    @Override
    public void finish() {
        super.finish();
        //overridePendingTransition(0, 0);
        overridePendingTransition(R.anim.in_right,R.anim.out_left);
        //overridePendingTransition(R.anim.in_left,android.R.anim.slide_out_right);
    }
    /*
    浮动音乐条初始化
     */
    private SimpleDateFormat time = new SimpleDateFormat("m:ss");
    private ProgressBar progressBar;
    private ImageView control;
    protected void iniMusicFloatBar(){
        control=(ImageView)findViewById(R.id.control);
        progressBar=(ProgressBar)findViewById(R.id.song_progress_normal);
        handler.post(runnable);
    }

    public void onControlClick(View view){
        musicService.playOrPause();
    }
    public void onListClick(View view){

    }
    public void onNextClick(View view){
        musicService.nextMusic();
    }
    public android.os.Handler handler = new android.os.Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(musicService.getmusicDirList()!=null&&musicService.getmusicDirList().size()!=0) {
                MusicInfo musicInfoTmp = (MusicInfo) musicService.getmusicDirList().get(musicService.musicIndex);
                setPlayBar(musicInfoTmp);
            }
            //定时更新
            if(musicService.mp.isPlaying()) {
                control.setImageResource(R.drawable.pause_btn);
                progressBar.setMax(musicService.mp.getDuration());
                progressBar.setProgress(musicService.mp.getCurrentPosition());
            }
            else
                control.setImageResource(R.drawable.play_btn);

            handler.postDelayed(runnable, 100);
        }
    };

    //列表显示部分
    protected void allMusic(){
        listView = (ListView) findViewById(R.id.list);
        musicList = new ArrayList<MusicInfo>();

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null, "duration > 60000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        getMusicInfo(cursor);
        cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, "duration > 60000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        getMusicInfo(cursor);
        musicAdapter =new MusicListAdapter(listView,musicList,this,MusicListAdapter.MAIN_ACTIVITY);
        listView.setAdapter(musicAdapter);

        //初始化播放列表
        musicService.setmusicDirList(musicList);
        Log.d("liaowm6","size()="+musicService.getmusicDirList().size());

        //音乐列表监听
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = listView.getItemAtPosition(position)+"";
                MusicInfo musicInfoTmp=(MusicInfo)musicList.get(position);
                Toast.makeText(getApplicationContext(),musicInfoTmp.getmusicTitle(), Toast.LENGTH_SHORT).show();
                musicService.setmusicDirList(musicList);
                Log.d("liaowm6","size()="+musicService.getmusicDirList().size());
                musicService.playAt(position);
                //musicService.setmusicDirList(arrayList);
                //Log.d("liaowm5","arrayList.size()="+arrayList.size())

                musicService.mp.start();
                timing(1);
                setPlayBar(musicInfoTmp);
            }
        });

    }
    public void setPlayBar(MusicInfo musicInfo){
        ImageView playbarImage=(ImageView) findViewById(R.id.playbar_img);
        playbarImage.setImageBitmap(ImageLoader.getBitmap(this,musicInfo.getAlbum_uri()));
        TextView playbar_info=(TextView)findViewById(R.id.playbar_info);
        TextView playbar_singer=(TextView)findViewById(R.id.playbar_singer);

        playbar_info.setText(musicInfo.getmusicTitle());
        playbar_singer.setText(musicInfo.getMusicArtist());
    }
    protected void getMusicInfo(Cursor cursor){
        int c=0;
        while (cursor.moveToNext()) {
            c++;
            MusicInfo info = new MusicInfo();
            info.setMusicId(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
            info.setmusicTitle(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
            info.setmusicDuration(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
            info.setMusicArtist(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
            info.setmusicAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
            info.setMusicPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
            //get imageUri
            info.setAlbum_uri(Uri.parse("content://media/external/audio/albumart/"+cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))));
            musicList.add(info);

            Log.d("hint", (cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)))+""+cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)));
        }
    }
}
