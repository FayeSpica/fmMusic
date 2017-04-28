package cn.tonyshy.music.fmmusic.Music;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import cn.tonyshy.music.fmmusic.MainActivity;
import cn.tonyshy.music.fmmusic.R;

import java.util.List;

/**
 * Created by Liaowm5 on 2016-12-28.
 */

public class MusicListAdapter extends BaseAdapter implements AbsListView.OnScrollListener {
    private LayoutInflater inflater;
    private List<MusicInfo> musicList;
    private MusicInfo musicInfo;
    private Bitmap bitmap;
    private Context mContext;

    //ImageLoder
    private ImageLoader mImageLoader;
    private int mStart,mEnd;
    public static Uri[] URIS;
    private boolean mIsFirstStart=false;

    public static int MAIN_ACTIVITY=0;
    public static int MUSIC_ACTIVITY=1;
    private int mCurrentActivity=MAIN_ACTIVITY;

    public MusicListAdapter(ListView listView,List<MusicInfo> musicList, Context context,int currentActivity){
        this.musicList=musicList;
        inflater=LayoutInflater.from(context);
        URIS=new Uri[musicList.size()];
        for(int i=0;i<URIS.length;i++){
            URIS[i]=musicList.get(i).getAlbum_uri();
        }
        mContext=context;
        mImageLoader=new ImageLoader(listView);
        listView.setOnScrollListener(this);//register
        mIsFirstStart=true;
        mCurrentActivity=currentActivity;
    }

    @Override
    public int getCount(){
        return musicList.size();
    }
    @Override
    public Object getItem(int position) {
        return musicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 1;//musicList.get(position).getMusicId();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView==null)
        {
            holder=new ViewHolder();
            convertView=inflater.inflate(R.layout.list_music_item, null);
            holder.img=(ImageView) convertView.findViewById(R.id.musicImageView);
            holder.more=(ImageButton) convertView.findViewById(R.id.musicBtnMore);
            holder.title=(TextView)convertView.findViewById(R.id.title);
            holder.Artist_Album=(TextView)convertView.findViewById(R.id.Artist);
            holder.img=(ImageView) convertView.findViewById(R.id.musicImageView);
            convertView.setTag(holder);
        }else
        {
            holder=(ViewHolder) convertView.getTag();
        }
        musicInfo=musicList.get(position);
        holder.img.setImageResource(R.drawable.placeholder_disk_210);
        mImageLoader.showImageByAsyncTask(mContext,holder.img,musicInfo.getAlbum_uri());//Cache
        holder.img.setTag(position);
        holder.title.setText(musicInfo.getmusicTitle());
        holder.Artist_Album.setText(""+musicInfo.getMusicArtist()+" - "+musicInfo.getmusicAlbum());
        if(mCurrentActivity==MUSIC_ACTIVITY){
            holder.title.setTextColor(Color.WHITE);
            holder.Artist_Album.setTextColor(Color.WHITE);
        }
        return convertView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(scrollState==SCROLL_STATE_IDLE){
            mImageLoader.loadImages(mContext,mStart,mEnd);
        }else{
            mImageLoader.cancelAllTasks();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mStart=firstVisibleItem;
        mEnd=firstVisibleItem+visibleItemCount;
        if(mIsFirstStart==true&&visibleItemCount>0){//First time loadImage
            mImageLoader.loadImages(mContext,mStart,mEnd);
            mIsFirstStart=false;
        }
    }

    static class ViewHolder
    {
        ImageView img;
        ImageButton more;
        TextView title,Artist_Album;
    }
}
