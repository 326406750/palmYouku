package uestc.palmyouku.control;

/**
 * Created by Administrator on 2016/8/23.
 */
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import uestc.palmyouku.R;
import uestc.palmyouku.control.User.PlayerActivity;


public class SearchActivity  extends AppCompatActivity implements SearchFragment.Callback,SearchResultFragment.Callback2,SearchResultFragment.VideoCallBack{

    private FragmentManager fm;
    public static final String PREF_SEARCH_QUERY = "search_history";

    public SearchActivity() {
    }

    public void onCall(String id)
    {
        Intent intent = new Intent(SearchActivity.this, PlayerActivity.class);

        //点击缓存视频播放时需要传递给播放界面的两个参数
        intent.putExtra("isFromLocal", false);
        intent.putExtra("vid", id);

        startActivity(intent);
    }

    @Override
    public void submit(String query) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(PREF_SEARCH_QUERY,query)
                .commit();
        fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.search_fragmentContainer);
        if(fragment != null)
            fm.beginTransaction().remove(fragment)
            .add(R.id.search_fragmentContainer,SearchResultFragment.newInstance(query))
            .commit();
    }

    @Override
    public void returnToSearch() {
        fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.search_fragmentContainer);
        if(fragment != null)
            fm.beginTransaction().remove(fragment)
                    .add(R.id.search_fragmentContainer,new SearchFragment())
                    .commit();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.search_fragmentContainer, new SearchFragment()).commit();
    }


}
