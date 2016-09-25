package uestc.palmyouku.control;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TabHost;

import uestc.palmyouku.R;
import uestc.palmyouku.control.User.*;

public class MainActivity extends TabActivity implements RadioGroup.OnCheckedChangeListener{

        private static final String HOME = "home";
        private static final String LOCAL = "local";
        private static final String SEARCH = "search";
        private static final String USER = "user";
        private RadioGroup rGroup;   //这里用RadioGroup来取代TabWidget，更加方便我们设计控件的样式，用于底部4个按钮
        private TabHost tabHost;      //像一个容器，把我们需要展示的activity放进去，根据我们需要进行显示
        private TabHost.TabSpec tabSpec;     //各个选项卡
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                openOptionsMenu();
                initView();
                setListner();
        }
        private void setListner() {        //绑定事件
                rGroup.setOnCheckedChangeListener(this);
        }
        private void initView()    //初始化view
        {
                rGroup = (RadioGroup) findViewById(R.id.RadioGroup);
                tabHost = this.getTabHost();   //从TabActivity中获取一个tabHost对象，因为继承了TabActivity，它已经获取了tabHost，不用再用findViewById
                /**
                 * 获取各个选项卡，并且把相应的activity装进去，可以加一个或者多个，根据需要添加
                 */

                tabSpec = tabHost.newTabSpec(HOME).setIndicator(HOME)
                        .setContent(new Intent(this, HomeActivity.class)); //显示主页界面
                tabHost.addTab(tabSpec);
                tabSpec = tabHost.newTabSpec(SEARCH).setIndicator(SEARCH)
                        .setContent(new Intent(this, SearchActivity.class));
                tabHost.addTab(tabSpec);
                tabSpec = tabHost.newTabSpec(LOCAL).setIndicator(LOCAL)
                        .setContent(new Intent(this, CachedActivity.class)); //本地视频
                tabHost.addTab(tabSpec);

                tabSpec = tabHost.newTabSpec(USER).setIndicator(USER)
                        .setContent(new Intent(this, UserActivity.class));   //我的界面
                tabHost.addTab(tabSpec);
                tabHost.setCurrentTab(0);

        }
        //点击下面5个Radiobutton，点击时候触发这里的事件,分别切换到相应的界面
        public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                        case R.id.home_id:
                                tabHost.setCurrentTab(0);      //跳转到首页界面
                                break;
                        case R.id.search_id:
                                tabHost.setCurrentTab(1);//跳转搜索界面
                                break;
                        case R.id.local_id:
                                tabHost.setCurrentTab(2);  //跳转本地界面
                                break;
                        case R.id.user_id:
                                tabHost.setCurrentTab(3);    //跳转我的界面
                                break;

                }
        }

}

