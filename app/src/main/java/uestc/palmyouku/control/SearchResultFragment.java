package uestc.palmyouku.control;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.ArrayList;

import uestc.palmyouku.R;
import uestc.palmyouku.model.Video;
import uestc.palmyouku.utils.Link;
import uestc.palmyouku.utils.MyJsonParser;

/**
 * Created by Administrator on 2016/8/24.
 */
public class SearchResultFragment extends Fragment implements SearchView.OnQueryTextListener{
    private static final String TAG = "SearchResultFragment";
    private static final String QUERY = "search_query";
    private GridView mGridView;
    private SearchView mSearchView;
    private MenuItem mSearchMenuItem;
    private Callback2 mCallback;
    private ArrayList<Video> mArrayList = new ArrayList<Video>();

    VideoCallBack mCallBack3;

    public interface VideoCallBack
    {
        public abstract  void onCall(String s);

    }




    public interface Callback2{
        public void submit(String query);
        public void returnToSearch();
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (Callback2)activity;
        mCallBack3 = (VideoCallBack)activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
        mCallBack3 = null;
    }

    //传入query参数，现已用shared Preference替代其作用
    public static SearchResultFragment newInstance(String query){
        Bundle args = new Bundle();
        args.putString(QUERY, query);
        SearchResultFragment fragment = new SearchResultFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //设置菜单栏
        setHasOptionsMenu(true);
        setRetainInstance(true);
        loadVideos();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup parent, Bundle savedInstanceState)
    {

        //View v = inflater.inflate(R.layout.search_result_fragment,null,false);
        View v = inflater.inflate(R.layout.video_fragment,null,false);
        mGridView = (GridView)v.findViewById(R.id.gridView);
        return v;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        mCallback.submit(query);
        return false;
    }

    //create Options Menu and set response
    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu_2, menu);
        SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchMenuItem = menu.findItem(R.id.search_button);
        mSearchView = (SearchView)mSearchMenuItem.getActionView();
        ComponentName name = new ComponentName(getActivity().getApplicationContext(), SearchActivity.class);

        //ComponentName name = getActivity().getComponentName();
        SearchableInfo info = searchManager.getSearchableInfo(name);

        mSearchView.setSearchableInfo(info);
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.cancel_button:
                mCallback.returnToSearch();
                break;

        }
        return true;
    }

    public void loadVideos(){
        String keyWord = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(SearchActivity.PREF_SEARCH_QUERY,null);
        String url = Uri.parse(Link.SEARCH_LINK).buildUpon()
                .appendQueryParameter("client_id", "b5359b407b25e86e")
                //.appendQueryParameter("keyword",getArguments().getString(ARGS))
                .appendQueryParameter("keyword", keyWord)
                .build().toString();
       // Log.i(TAG,getArguments().getString(ARGS));
        Log.i(TAG,keyWord);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                mArrayList = MyJsonParser.parseVideos(response);
                Log.i(TAG,mArrayList.get(0).getId());
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

    public class VideoItemAdapeter extends ArrayAdapter<Video> {
        public VideoItemAdapeter(ArrayList<Video> videos){
            super(getActivity(),0,videos);
        }
        @Override
        public View getView(final int position,View convertView,ViewGroup parent){
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

            //设置 点击触发视频播放事件
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallBack3.onCall(getItem(position).getId());
                }
            });

            return convertView;

        }
    }
}
