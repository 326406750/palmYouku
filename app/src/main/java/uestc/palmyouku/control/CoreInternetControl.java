package uestc.palmyouku.control;

import android.app.Activity;
import android.app.Application;
import android.app.DownloadManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.LruCache;
import android.widget.ImageView;
import uestc.palmyouku.R;
import uestc.palmyouku.control.User.CachedActivity;
import uestc.palmyouku.control.User.CachingActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.youku.player.YoukuPlayerBaseConfiguration;

/**
 * Created by Administrator on 2016/8/23.
 */
public class CoreInternetControl extends Application{

    private static final String TAG = CoreInternetControl.class.getSimpleName();
    private RequestQueue mQueue;
    private ImageLoader mImageLoader;
    private static CoreInternetControl mCoreInternetControl;
    public static YoukuPlayerBaseConfiguration sConfiguration;
   // public static YouKuPlayerBaseConfiguration sConfiguration;

    @Override
    public void onCreate(){
        super.onCreate();
        mCoreInternetControl = this;
        sConfiguration = new YoukuPlayerBaseConfiguration(this) {
            @Override
            public Class<? extends Activity> getCachingActivityClass() {

                return CachingActivity.class;
            }

            @Override
            public Class<? extends Activity> getCachedActivityClass() {
                return CachedActivity.class;
            }


            /**
             * 配置视频的缓存路径，格式举例： /appname/videocache/
             * 如果返回空，则视频默认缓存路径为： /应用程序包名/videocache/
             *
             */
            @Override
            public String configDownloadPath() {
                return null;
            }
        };
    }

    public synchronized static CoreInternetControl getInstance(){
        return mCoreInternetControl;
    }

    public RequestQueue getRequestQueue(){
        if(mQueue==null){
            return Volley.newRequestQueue(getApplicationContext());
        }else
            return mQueue;
    }

    public ImageLoader getImageLoader(){
        mQueue =getRequestQueue();
        if(mImageLoader == null)
            return new ImageLoader(mQueue, new ImageLoader.ImageCache() {
                @Override
                public Bitmap getBitmap(String url) {
                    return null;
                }

                @Override
                public void putBitmap(String url, Bitmap bitmap) {

                }
            });
        else
            return mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> request)
    {
        // set default TAG
        request.setTag(TAG);
        getRequestQueue().add(request);
    }

    public void loadImage(ImageView imageView,String url){
        ImageLoader imageLoader = getImageLoader();
        ImageLoader.ImageListener listener = ImageLoader.getImageListener(imageView,R.drawable.test_home1,R.drawable.test_home2);
        imageLoader.get(url,listener);
    }

}
