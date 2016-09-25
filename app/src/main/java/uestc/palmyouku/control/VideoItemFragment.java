package uestc.palmyouku.control;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.youku.player.base.YoukuPlayer;

import org.json.JSONObject;

import java.util.ArrayList;

import uestc.palmyouku.R;
import uestc.palmyouku.control.User.CachedActivity;
import uestc.palmyouku.control.User.PlayerActivity;
import uestc.palmyouku.model.Video;
import uestc.palmyouku.utils.Link;
import uestc.palmyouku.utils.MyJsonParser;


/**
 * Created by Administrator on 2016/8/23.
 */
public class VideoItemFragment extends Fragment {

    GridView mGridView;
    private ArrayList<Video> mArrayList = new ArrayList<Video>();
    private static final String ARGS = "videoItemFragment_args";
    private static final String TAG = "VideoItemFragment";
    VideoCallBack mCallBack;

    public interface VideoCallBack
    {
        public abstract  void onCall(String s);

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallBack = (VideoCallBack)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallBack = null;
    }

    public static VideoItemFragment newInstance(String query){
        VideoItemFragment fragment = new VideoItemFragment();
        Bundle argument = new Bundle();
        argument.putString(ARGS,query);
        Log.i(TAG,query);
        fragment.setArguments(argument);

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        Log.i(TAG,"create view");
        View v = inflater.inflate(R.layout.video_fragment,parent,false);
        mGridView = (GridView)v.findViewById(R.id.gridView);
        loadVideos();
        return v;
    }

    //载入所选分类Video
    public void loadVideos(){
        String url = Uri.parse(Link.VIDEO_BY_CATEGORIES_LINK).buildUpon()
                .appendQueryParameter("client_id","b5359b407b25e86e")
                .appendQueryParameter("category",getArguments().getString(ARGS))
                .build().toString();
        Log.i(TAG,getArguments().getString(ARGS));
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                mArrayList = MyJsonParser.parseVideos(response);

                //设置界面
                setAdapter();

                //设置按键响应

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        CoreInternetControl.getInstance().addToRequestQueue(request);
    }

    public void setAdapter(){
        if(mGridView==null||getActivity() == null)return;
        if(mArrayList != null){
            mGridView.setAdapter(new VideoItemAdapeter(mArrayList));

        }else{
            mGridView.setAdapter(null);
        }
    }


    //自定义Adapter
    public class VideoItemAdapeter extends ArrayAdapter<Video>{
        public VideoItemAdapeter(ArrayList<Video> videos){
            super(getActivity(),0,videos);
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            if(convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.video_item, parent, false);
            }
            ImageView imageView = (ImageView) convertView.findViewById(R.id.video_image);
            ImageView playIcon = (ImageView)convertView.findViewById(R.id.play_icon);
            TextView duratiom = (TextView) convertView.findViewById(R.id.video_duration);
            TextView title = (TextView) convertView.findViewById(R.id.video_title);


            Log.i(TAG,getItem(position).getThumbnail());
            CoreInternetControl.getInstance().loadImage(imageView,getItem(position).getThumbnail());
            playIcon.setImageResource(R.drawable.test_local1);
            String durationInString = String.valueOf(getItem(position).getDuration()); //将时间（s）转化为字符串
            duratiom.setText(durationInString);
            title.setText(getItem(position).getTitle());

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                   mCallBack.onCall(getItem(position).getId());
                }
            });
            return convertView;

        }
    }
}
