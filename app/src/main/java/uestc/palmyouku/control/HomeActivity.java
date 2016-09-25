package uestc.palmyouku.control;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.ArrayList;

import uestc.palmyouku.R;
import uestc.palmyouku.control.User.PlayerActivity;
import uestc.palmyouku.model.Category;
import uestc.palmyouku.utils.Link;
import uestc.palmyouku.utils.MyJsonParser;

/**
 * Created by Administrator on 2016/8/23.
 */
public class HomeActivity extends FragmentActivity implements VideoItemFragment.VideoCallBack {

    private static final String TAG = "HomeActivity";
    private ProgressDialog mProgressDialog;
    private ArrayList<Category> mArrayList;
    private RadioGroup mRadioGroup;
    private FragmentManager fm;

    public HomeActivity() {
    }

    public void onCall(String id)
    {
        Intent intent = new Intent(HomeActivity.this, PlayerActivity.class);

        //点击缓存视频播放时需要传递给播放界面的两个参数
        intent.putExtra("isFromLocal", false);
        intent.putExtra("vid", id);

        startActivity(intent);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_home);
        fm = getSupportFragmentManager();
        loadCategories();


        Log.i(TAG,"create");
    }

    //将Category列表载入布局
    public void initView() {
        mRadioGroup = (RadioGroup)this.findViewById(R.id.bar_rg);
        for(int i=0;i<13;i++){
            RadioButton tempButton = (RadioButton)mRadioGroup.getChildAt(i);
            tempButton.setText(mArrayList.get(i).getName());
            //  tempButton.setBackgroundResource(R.drawable.sc_bar_selector);
            //tempButton.setText(mArrayList.get(i).getName());
            //tempButton.setGravity(Gravity.CENTER);
           // tempButton.;
           // mRadioGroup.addView(tempButton, HorizontalScrollView.LayoutParams.FILL_PARENT, HorizontalScrollView.LayoutParams.WRAP_CONTENT);
        }

    }


    public void loadCategories(){
        //load the categories from internet;
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("loading");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        JsonObjectRequest request = new JsonObjectRequest(Link.CATEGORIES_LINK, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                mArrayList = MyJsonParser.parseCategories(response);
                initView();
                mProgressDialog.dismiss();

                //载入Category后设置监听
                mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton tempButton = (RadioButton) mRadioGroup.findViewById(checkedId);
                        String category = tempButton.getText().toString();
                        Log.i(TAG, category);
                        Fragment fragment = VideoItemFragment.newInstance(category);

                        //清理原有fragment
                        if(fm.getFragments()!=null)
                            for(Fragment f:fm.getFragments()){
                                fm.beginTransaction().remove(f).commit();
                            }
                        //载入新Fragment
                        fm.beginTransaction()
                                .add(R.id.fragmentContainer, fragment).commit();

                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        CoreInternetControl mControl = CoreInternetControl.getInstance();
        mControl.addToRequestQueue(request);
    }
}

