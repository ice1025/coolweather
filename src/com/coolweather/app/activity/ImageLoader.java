package com.coolweather.app.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by libing on 2016/4/19.
 */
public class ImageLoader {

    private ImageView mimageView;
    //创建cache
    private LruCache<String,Bitmap> mCaches;

    public ImageLoader() {//初始方法中
        //获取最大可用内存
        int max = (int) Runtime.getRuntime().maxMemory();
        int cachSize = max/4;
        mCaches=new LruCache<String,Bitmap>(cachSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存的时候调用
                return value.getByteCount();
            }
        };
    }

    /**
     * 增加到缓存
     */
    public void addBitmapToCache(String url,Bitmap bitmap){
        if(getBitmapFromCache(url)==null){
            mCaches.put(url,bitmap);
        }
    }
    //从缓存中获取数据
    public Bitmap getBitmapFromCache(String url){
        return mCaches.get(url);
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mimageView.setImageBitmap((Bitmap) msg.obj);
        }
    };
    public void showImageByThread(ImageView imageView, final String url){
        mimageView=imageView;
        new Thread(){
            @Override
            public void run() {
                super.run();
                Bitmap bitmap = getBitmapFromURl(url);
                Message message = Message.obtain();
                message.obj=bitmap;
                handler.sendMessage(message);
            }
        }.start();
    }
    public Bitmap getBitmapFromURl(String urlString){
        Bitmap bitmap;
        InputStream is = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(connection.getInputStream());
            bitmap= BitmapFactory.decodeStream(is);
            connection.disconnect();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public void showImageByAsyncTask(ImageView imageView,String url){
        //从缓存中取出对应的图片
        Bitmap bitmap = getBitmapFromCache(url);
        //如果缓存中没有，那么必须去下载
        if(bitmap==null){
            new NewsAsyncTask(imageView).execute(url);
        }else{
            //如果有，就直接设置给图片
            imageView.setImageBitmap(bitmap);
        }


    }
    private class NewsAsyncTask extends AsyncTask<String,Void,Bitmap>{
        private ImageView mimageView;
        public NewsAsyncTask(ImageView imageView) {
            mimageView=imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            //从网络获取图片
            Bitmap bitmap = getBitmapFromURl(params[0]);
            if(bitmap!=null){
                //将不在缓存的图片加入缓存（从而实现一个缓存效果）
                addBitmapToCache(params[0],bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            mimageView.setImageBitmap(bitmap);
        }
    }
}
