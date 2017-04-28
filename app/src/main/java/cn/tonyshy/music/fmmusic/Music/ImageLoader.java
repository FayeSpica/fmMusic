package cn.tonyshy.music.fmmusic.Music;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import cn.tonyshy.music.fmmusic.R;

/**
 * Created by liaowm5 on 17/4/28.
 */

public class ImageLoader {
    private ImageView mImageView;
    private Uri mUri;

    private LruCache<Uri,Bitmap> mCaches;
    private ListView mListView;
    private Set<NewAsyncTask> mTasks;

    public ImageLoader(ListView listView){
        mListView=listView;
        mTasks=new HashSet<>();
        //getMax
        int MaxMemory=(int)Runtime.getRuntime().maxMemory();
        int cacheSize=MaxMemory/8;
        mCaches=new LruCache<Uri,Bitmap>(cacheSize){
            @Override
            protected  int sizeOf(Uri key,Bitmap value){
                return value.getByteCount();
            }
        };
    }

    public void addBitmapToCache(Uri uri,Bitmap bitmap){
        if(getBitmapFromCache(uri)==null) {
            mCaches.put(uri, bitmap);
        }
    }
    public Bitmap getBitmapFromCache(Uri uri){
        return mCaches.get(uri);
    }

    public Bitmap getBitmapFromUri(Context context,Uri uri){
        Bitmap bitmap=null;
        InputStream is=null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        try {
            is = context.getContentResolver().openInputStream(uri);
            if (null != is) {
                bitmap = BitmapFactory.decodeStream(is);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                is.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return bitmap;
    }


    public void loadImages(Context context,int start,int end){
        for(int i=start;i<end;i++){
            Uri uri=MusicListAdapter.URIS[i];
            Bitmap bitmap=getBitmapFromCache(uri);
            ImageView imageView=(ImageView) mListView.findViewWithTag(i);
            if(bitmap==null){
                NewAsyncTask task=new NewAsyncTask(uri,context,i,imageView.getWidth(),imageView.getHeight());
                task.execute();
                mTasks.add(task);
            }else{
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public void cancelAllTasks(){
        if(mTasks!=null){
            for(NewAsyncTask task:mTasks){
                task.cancel(false);
            }
        }
    }

    public void showImageByAsyncTask(final Context context, ImageView imageView, final Uri uri){
        Bitmap bitmap=getBitmapFromCache(uri);
        if(bitmap==null){
            imageView.setImageResource(R.drawable.placeholder_disk_210);
        }else{
            imageView.setImageBitmap(bitmap);
        }
    }

    private class NewAsyncTask extends AsyncTask<Uri,Void,Bitmap>{
        private Uri mUri;
        private Context mContext;
        private int mPosition;//Unique
        private int mReqWidth;
        private int mReqHeight;
        public NewAsyncTask(Uri uri,Context context,int position,int reqWidth,int reqHeight){//load effectively by height&width of spec imageView
            mUri=uri;
            mContext=context;
            mPosition=position;
            mReqWidth=reqWidth;
            mReqHeight=reqHeight;
        }
        @Override
        protected Bitmap doInBackground(Uri... params) {
            //Bitmap bitmap=getBitmapFromUri(mContext,mUri);
            Bitmap bitmap=customGetBitmapFromUri(mContext,mUri,mReqWidth,mReqHeight);
            if(bitmap!=null) {
                addBitmapToCache(mUri, bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap){
            super.onPostExecute(bitmap);
            ImageView imageView=(ImageView) mListView.findViewWithTag(mPosition);
            imageView.getLayoutParams();
            if(imageView!=null&&bitmap!=null){
                imageView.setImageBitmap(bitmap);
            }
            mTasks.remove(this);
        }
    }

    public static Bitmap getBitmap(Context context,Uri uri){
        Bitmap bitmap=null;
        InputStream is=null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            if (null != is) {
                bitmap = BitmapFactory.decodeStream(is);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                is.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return bitmap;
    }
    /*load effectively*/

    public static Bitmap customGetBitmapFromUri(Context context,Uri uri,int reqWidth,int reqHeight ){
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        InputStream is=null;
        Bitmap bitmap=null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            if (null != is) {
                BitmapFactory.decodeStream(is, null,opts);
                opts.inSampleSize=calculateInSampleSize(opts,reqWidth,reqHeight);
                opts.inJustDecodeBounds = false;
                is = context.getContentResolver().openInputStream(uri);
                bitmap =BitmapFactory.decodeStream(is, null,opts);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                is.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        return bitmap;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options,int reqWidth,int reqHeight){
        final int height=options.outHeight;
        final int width=options.outWidth;
        int inSampleSize=1;

        if(height>reqHeight||width>reqWidth){
            final int halfHeight=height/2;
            final int halfWidth=width/2;

            while ((halfHeight/inSampleSize)>reqHeight&&(halfWidth/inSampleSize)>reqWidth){
                inSampleSize=inSampleSize<<1;
            }
        }
        return inSampleSize;
    }
}
