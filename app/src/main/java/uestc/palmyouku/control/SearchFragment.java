package uestc.palmyouku.control;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
//import android.support.v7.widget.SearchView;
//import android.support.v7.widget.Toolbar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.zip.Inflater;

import javax.security.auth.callback.Callback;

import uestc.palmyouku.R;
import uestc.palmyouku.model.Keyword;
import uestc.palmyouku.utils.Link;
import uestc.palmyouku.utils.MyJsonParser;


/**
 * Created by Administrator on 2016/8/25.
 */
public class SearchFragment extends Fragment implements SearchView.OnQueryTextListener{
    private static final String TAG = "SearchFragment";
    private static final String SEARCH_QUERY = "search_query";


    //private ScrollView mScrollView;
    private LinearLayout searchHistory;
    private GridView mGridView;
    private SearchView mSearchView;
    private MenuItem mSearchMenuItem;
    private Callback mCallback;
    private ArrayList<Keyword> mArrayList;


    public interface Callback{
        public void submit(String query);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (Callback)activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //getActivity().openOptionsMenu();
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup parent, Bundle savedInstanceState)
    {

        View v = inflater.inflate(R.layout.search_fragment,null,false);
        //((SearchActivity)getActivity()).setSupportActionBar(toolbar);
        searchHistory =(LinearLayout)v.findViewById(R.id.search_history);
        mGridView = (GridView)v.findViewById(R.id.search_word_gridView);
        loadKeywordsRank();
        Log.i(TAG, "createview");
        return v;
    }


    //  SearchView callback method
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
        Log.i(TAG, "prepare to inflate");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu_1, menu);
        Log.i(TAG,"inflate");
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

    public void loadKeywordsRank(){
        String url=Uri.parse(Link.SERACH_RANK_WORD_LINK).buildUpon()
                .appendQueryParameter("client_id","b5359b407b25e86e")
                .build().toString();
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // 设置关键字搜索榜
                mArrayList = MyJsonParser.parseKeywords(response);
                Log.i(TAG,mArrayList.get(0).getKeyord());
                setAdapter();

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
            mGridView.setAdapter(new KeywordAdapter(mArrayList));
            Log.i(TAG,"setAdapter");
        }else{
            mGridView.setAdapter(null);
        }
    }

    public class KeywordAdapter extends ArrayAdapter<Keyword>
    {
        public KeywordAdapter(ArrayList<Keyword> keywords){
            super(getActivity(),0,keywords);
        }
        @Override
        public View getView(int position,View convertView,ViewGroup parent){
            if(convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.keyword_item, parent, false);
            }
            TextView keyword = (TextView)convertView.findViewById(R.id.keyword);
            final String key = getItem(position).getKeyord();
            keyword.setText(key);
            keyword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.submit(key);
                }
            });
            Log.i(TAG, getItem(position).getKeyord());
            return convertView;
        }
    }

}
