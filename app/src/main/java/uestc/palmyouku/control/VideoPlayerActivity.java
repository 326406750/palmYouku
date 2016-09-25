package uestc.palmyouku.control;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.youku.player.ApiManager;
import com.youku.player.VideoQuality;
import com.youku.player.base.YoukuBasePlayerManager;
import com.youku.player.base.YoukuPlayer;
import com.youku.player.base.YoukuPlayerView;
import com.youku.service.download.DownloadManager;
import com.youku.service.download.OnCreateDownloadListener;

import uestc.palmyouku.R;

/**
 * Created by Administrator on 2016/8/24.
 */
public class VideoPlayerActivity extends Activity {
    public YoukuBasePlayerManager mBasePlayerManager;
    private YoukuPlayerView mYoukuPlayerView;
    private YoukuPlayer mYoukuPlayer;
    private String vid;
    // 清晰度相关按钮
    private Button btn_standard, btn_hight, btn_super, btn_1080;

    // 下载视频按钮
    private Button btn_download;

    // 需要播放的本地视频的id
    private String local_vid;

    // 标示是否播放的本地视频
    private boolean isFromLocal = false;

    private String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_activity);
        iniView();
        mBasePlayerManager = new YoukuBasePlayerManager(this) {
            @Override
            public void setPadHorizontalLayout() {

            }

            @Override
            public void onInitializationSuccess(YoukuPlayer player) {
// 初始化成功后需要添加该行代码
                addPlugins();

                // 实例化YoukuPlayer实例
                mYoukuPlayer = player;

                // 进行播放
                goPlay();
            }

            @Override
            public void onFullscreenListener() {

            }

            @Override
            public void onSmallscreenListener() {

            }
        };

        mYoukuPlayerView = (YoukuPlayerView)this.findViewById(R.id.full_holder);
        mYoukuPlayerView.setSmallScreenLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mYoukuPlayerView.setFullScreenLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        mYoukuPlayerView.initialize(mBasePlayerManager);
        mBasePlayerManager.onCreate();

        // 通过上个页面传递过来的Intent获取播放参数
        getIntentData(getIntent());

        if (TextUtils.isEmpty(id)) {
            vid = "XODQwMTY4NDg0"; // 默认视频
        } else {
            vid = id;
        }

    }


    private void getIntentData(Intent intent) {

        if (intent != null) {
            // 判断是不是本地视频
            isFromLocal = intent.getBooleanExtra("isFromLocal", false);

            if (isFromLocal) { // 播放本地视频
                local_vid = intent.getStringExtra("video_id");
            } else { // 在线播放
                id = intent.getStringExtra("vid");
            }
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);

        // 通过Intent获取播放需要的相关参数
        getIntentData(intent);

        // 进行播放
        goPlay();
    }


    private void goPlay() {
        if (isFromLocal) { // 播放本地视频
            mYoukuPlayer.playLocalVideo(local_vid);
        } else { // 播放在线视频
            mYoukuPlayer.playVideo(vid);
        }
    }

    // 通过DownloadManager类实现视频下载
    private void doDownload() {
        DownloadManager d = DownloadManager.getInstance();
        /**
         * 第一个参数为需要下载的视频id 第二个参数为该视频的标题title 第三个对下载视频结束的监听，可以为空null
         */
        d.createDownload("XNzgyODExNDY4", "魔女范冰冰扑倒黄晓明",
                new OnCreateDownloadListener() {

                    @Override
                    public void onfinish(boolean isNeedRefresh) {
                        // TODO Auto-generated method stub

                    }
                });
    }

    private void iniView() {
        btn_standard = (Button) this.findViewById(R.id.biaoqing);
        btn_hight = (Button) this.findViewById(R.id.gaoqing);
        btn_super = (Button) this.findViewById(R.id.chaoqing);
        btn_1080 = (Button) this.findViewById(R.id.most);
        btn_download = (Button) this.findViewById(R.id.download);

        btn_standard.setOnClickListener(listener);
        btn_hight.setOnClickListener(listener);
        btn_super.setOnClickListener(listener);
        btn_1080.setOnClickListener(listener);
        btn_download.setOnClickListener(listener);
    }

    public View.OnClickListener listener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            // TODO Auto-generated method stub
            switch (view.getId()) {
                case R.id.biaoqing: // 切换标清
                    change(VideoQuality.STANDARD);
                    break;
                case R.id.gaoqing: // 切换高清
                    change(VideoQuality.HIGHT);
                    break;
                case R.id.chaoqing: // 切换超清
                    change(VideoQuality.SUPER);
                    break;
                case R.id.most: // 切换为1080P
                    change(VideoQuality.P1080);
                    break;
                case R.id.download: // 下载视频接口测试
                    doDownload();
                    break;
            }

        }
    };



    private void change(VideoQuality quality) {
        try {
            // 通过ApiManager实例更改清晰度设置，返回值（1):成功；（0): 不支持此清晰度
            // 接口详细信息可以参数使用文档
            int result = ApiManager.getInstance().changeVideoQuality(quality, mBasePlayerManager);
            if (result == 0)
                Toast.makeText(VideoPlayerActivity.this, "不支持此清晰度", 2000).show();
        } catch (Exception e) {
            Toast.makeText(VideoPlayerActivity.this, e.getMessage(), 2000).show();
        }
    }


    @Override
    public void onBackPressed() { // android系统调用
        Logger.d("sgh", "onBackPressed before super");
        super.onBackPressed();
        Logger.d("sgh", "onBackPressed");
        mBasePlayerManager.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mBasePlayerManager.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBasePlayerManager.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean managerKeyDown =  mBasePlayerManager.onKeyDown(keyCode, event);
        if ( mBasePlayerManager.shouldCallSuperKeyDown()) {
            return super.onKeyDown(keyCode, event);
        } else {
            return managerKeyDown;
        }

    }

    @Override
    public void onLowMemory() { // android系统调用
        super.onLowMemory();
        mBasePlayerManager.onLowMemory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBasePlayerManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBasePlayerManager.onResume();
    }

    @Override
    public boolean onSearchRequested() { // android系统调用
        return mBasePlayerManager.onSearchRequested();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBasePlayerManager.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBasePlayerManager.onStop();
    }



}
