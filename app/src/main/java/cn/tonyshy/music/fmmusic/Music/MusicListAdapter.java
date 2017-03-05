package cn.tonyshy.music.fmmusic.Music;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import cn.tonyshy.music.fmmusic.R;

import java.util.List;

/**
 * Created by Liaowm5 on 2016-12-28.
 */

public class MusicListAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<MusicInfo> musicList;
    private MusicInfo musicInfo;
    private Bitmap bitmap;


    public MusicListAdapter(List<MusicInfo> musicList, Context context){
        this.musicList=musicList;
        inflater=LayoutInflater.from(context);
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

        //holder.img.setImageResource(musicInfo.getImgRes());
        holder.title.setText(musicInfo.getmusicTitle());
        holder.Artist_Album.setText(""+musicInfo.getMusicArtist()+" - "+musicInfo.getmusicAlbum());

        if (musicInfo.getBitmap() != null)
        {
            BitmapDrawable bmpDraw = new BitmapDrawable(musicInfo.getBitmap());
            holder.img.setImageDrawable(bmpDraw);
        }
        return convertView;
    }

    static class ViewHolder
    {
        ImageView img;
        ImageButton more;
        TextView title,Artist_Album;
    }
}
