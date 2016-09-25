package com.youku.player.plugin;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.baseproject.utils.UIUtils;
import com.baseproject.utils.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.youku.player.base.GoplayException;
import com.youku.player.base.Plantform;
import com.youku.player.base.YoukuBasePlayerManager;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.Stat;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.service.DisposableHttpTask;
import com.youku.player.ui.R;
import com.youku.player.ui.interf.IMediaPlayerDelegate;
import com.youku.player.util.DetailMessage;
import com.youku.player.util.DisposableStatsUtils;

public class PluginADPlay extends PluginOverlay implements DetailMessage {

	LayoutInflater mLayoutInflater;
	View containerView;
	TextView endPage;
//	TextView ad_more;
	YoukuBasePlayerManager mBasePlayerManager;
	Activity mActivity;
	IMediaPlayerDelegate mediaPlayerDelegate;
	private TextView mCountUpdateTextView;
	private ImageView mSwitchPlayer;
	// youku控件
	private LinearLayout mCountUpdateWrap;
//	private TextView mAdSkip;
	private LinearLayout mAdSkipBlank;

	// 去详情的父view
	private View mSwitchParent;
	protected String TAG = "PluginADPlay";
	private View seekLoadingContainerView;
//	private ImageButton play_adButton;

	public static final int ADMORE_BACKGROUND_COLOR_YOUKU = 0xcc292929;
	public static final int ADMORE_BACKGROUND_COLOR_TUDOU = 0xffff6600;

	public static int sAdMoreBackgroundColor = ADMORE_BACKGROUND_COLOR_YOUKU;

	private RelativeLayout mAdPageHolder = null;
	// interactive ad
	private RelativeLayout mInteractiveAdContainer = null;
	private RelativeLayout mInteractiveAdGoFull;
	private org.json.JSONObject mCurrentAdData;
	private boolean isInteractiveAdShow = false;
	private boolean isInteractiveAdHide = false;
	private String mInteractiveAdVideoRs = null; //互动广告对应视频素材

	
	
	//暂停广告
	
	
	
	public PluginADPlay(YoukuBasePlayerManager basePlayerManager,
			IMediaPlayerDelegate mediaPlayerDelegate) {
		super(basePlayerManager.getBaseActivity(), mediaPlayerDelegate);
		this.mediaPlayerDelegate = mediaPlayerDelegate;
		mBasePlayerManager = basePlayerManager;
		mActivity = mBasePlayerManager.getBaseActivity();
		mLayoutInflater = LayoutInflater.from(mActivity);
		init(mActivity);
	}

	private void init(Context context) {
//		if (Profile.PLANTFORM == Plantform.YOUKU) {
			containerView = mLayoutInflater.inflate(
					R.layout.yp_player_ad_youku, null);
//		} else {
//			containerView = mLayoutInflater.inflate(
//					R.layout.yp_player_ad_tudou, null);
//		}
		addView(containerView);
		mCountUpdateTextView = (TextView) containerView
				.findViewById(R.id.my_ad_count);
		if (Profile.PLANTFORM == Plantform.YOUKU) {
			mAdPageHolder = (RelativeLayout) containerView
					.findViewById(R.id.ad_page_holder);
			mInteractiveAdContainer = (RelativeLayout) containerView
					.findViewById(R.id.interactive_ad_container);
			mInteractiveAdGoFull = (RelativeLayout) containerView
					.findViewById(R.id.interactive_ad_gofull_layout);
			mInteractiveAdGoFull.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					isInteractiveAdHide = false;

					mInteractiveAdGoFull.setVisibility(View.GONE);
					mInteractiveAdContainer.setVisibility(View.VISIBLE);
					if (mAdPageHolder != null) {
						mAdPageHolder.setVisibility(View.GONE);
					}
					mBasePlayerManager.goFullScreen();
					mBasePlayerManager.setOrientionDisable();
				}
				
			});
			mCountUpdateWrap = (LinearLayout) containerView
					.findViewById(R.id.my_ad_count_wrap);
			mAdSkipBlank = (LinearLayout) containerView
					.findViewById(R.id.my_ad_blank);
/*			mAdSkip = (TextView) containerView.findViewById(R.id.my_ad_skip);
			mAdSkip.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						Intent intent = new Intent();
						intent.setClassName(mActivity.getPackageName(),
								"com.youku.phone.vip.activity.VipProductActivity");
						intent.putExtra("from", 1001);
						intent.putExtra("isVip", false);
						intent.putExtra("video_id",
								mediaPlayerDelegate.videoInfo.getVid());
						intent.putExtra("isFromLocal",
								mediaPlayerDelegate.videoInfo.playType
										.equals(StaticsUtil.PLAY_TYPE_LOCAL));
						intent.putExtra("playlist_id",
								mediaPlayerDelegate.videoInfo.playlistId);
						mActivity.startActivity(intent);
					} catch (Exception e) {

					} finally {
						mActivity.finish();
					}
				}
			});*/
			
			mAdPageHolder.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					 final AdvInfo curAdvInfo = mediaPlayerDelegate.videoInfo.videoAdvInfo.VAL.get(0);
					 String clickUrl = curAdvInfo.CU;

					mediaPlayerDelegate.pauseForVideoAdv();
						
					 showInBrowser(clickUrl,new OnShowAdvInBrowserResult(){

							@Override
							public void onFail() {
								//继续播放
								if (!mMediaPlayerDelegate.isAdvShowFinished()) {
									startPlay();
								}
							}

							@Override
							public void onSuccess() {
								// 发送统计
								DisposableStatsUtils.disposeCUM(curAdvInfo);
							}});// 展示详情
						
					}
	        });
		}
		mSwitchPlayer = (ImageView) containerView
				.findViewById(R.id.gofullscreen);
		mSwitchParent = containerView.findViewById(R.id.gofulllayout);
/*		ad_more = (TextView) containerView.findViewById(R.id.ad_more);
		ad_more.setBackgroundColor(sAdMoreBackgroundColor);*/
//		play_adButton = (ImageButton) containerView
//				.findViewById(R.id.ib_detail_play_control_ad_play);
//		play_adButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (Util.hasInternet()
//						&& !Util.isWifi()
//						&& !PreferenceManager.getDefaultSharedPreferences(
//								mActivity).getBoolean("allowONline3G", true)) {
//					Toast.makeText(mActivity, "请设置3g/2g允许播放", Toast.LENGTH_SHORT)
//							.show();
//					return;
//				}
//		//接着广告继续播
//				startPlay();
//				play_adButton.setVisibility(View.GONE);
//			}
//		});
		mSwitchParent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mediaPlayerDelegate.isFullScreen) {
					mBasePlayerManager.goSmall();
					if (Profile.PLANTFORM == Plantform.TUDOU) {
						mSwitchPlayer
								.setImageResource(R.drawable.plugin_ad_gofull_tudou);
					} else {
						mSwitchPlayer
								.setImageResource(R.drawable.plugin_ad_gofull_youku);
					}
				} else {
					mBasePlayerManager.goFullScreen();
					if (Profile.PLANTFORM == Plantform.TUDOU) {
						mSwitchPlayer
								.setImageResource(R.drawable.plugin_ad_gosmall_tudou);
					} else {
						mSwitchPlayer
								.setImageResource(R.drawable.plugin_ad_gosmall_youku);
					}
				}
			}
		});

		seekLoadingContainerView = containerView
				.findViewById(R.id.seek_loading_bg);
		initSeekLoading();
		
		initPauseAdvView();
		Logger.d(TAG,"ad setOnSurfaceStatusChangeListener() " );
		mBasePlayerManager
				.addOnSurfaceStatusChangeListener(new OnSurfaceStatusChangeListener() {

					@Override
					public void onSurfaceCreated() {
						Logger.d(
								TAG,
								"ad onSurfaceCreated() isAdvShowFinished = "
										+ mMediaPlayerDelegate
												.isAdvShowFinished());
						if (!mMediaPlayerDelegate.isAdvShowFinished()) {
							// 如果没有显示选择对话框，就继续播放。
							if (!onChooserDlgShow) {
								startPlay();
							}
						}
					}

				});
		
	}
	

    private View imgAdvLayout;//整个图片广告布局
    private ImageView imgAdvContentImgView;//图片内容展示
    private TextView imgAdvHintTxtView;//左下角的广告提示
    private ImageView imgAdvCloseImgView;//右上角的关闭广告
	
	private void initPauseAdvView() {
        imgAdvLayout = findViewById(R.id.img_ad_layout);
        imgAdvContentImgView = (ImageView) findViewById(R.id.adv_img_imgview);
        imgAdvHintTxtView = (TextView) findViewById(R.id.adv_img_hint_textview);
        imgAdvCloseImgView = (ImageView) findViewById(R.id.adv_img_close_imgview);
        
        imgAdvLayout.setVisibility(View.INVISIBLE);
        imgAdvContentImgView.setImageResource(R.drawable.nonedrawable);
        
        //广告点击跳转
        imgAdvContentImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	final VideoAdvInfo pauseAdvInfo = mediaPlayerDelegate.getPauseAdvInfo();
            	boolean isIlligal = checkImgAdv(pauseAdvInfo);
				if (isIlligal) {
					String clickUrl = pauseAdvInfo.VAL.get(0).CU;
					showInBrowser(clickUrl,new OnShowAdvInBrowserResult(){

						@Override
						public void onFail() {
							
						}

						@Override
						public void onSuccess() {
							// 发送统计
							DisposableStatsUtils.disposeCUM(pauseAdvInfo.VAL.get(0));
							endPauseAdvShow();
						}});// 展示详情
					
				}
            }
        });
        //关闭整个广告
        imgAdvCloseImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	endPauseAdvShow();
            }

			
        });
	}
	private void endPauseAdvShow() {
		imgAdvLayout.setVisibility(View.GONE);
    	mBasePlayerManager.isImageADShowing = false;
    	//显示控制条。。
    	mBasePlayerManager.updatePlugin(DetailMessage.PLUGIN_SHOW_NOT_SET);
    	// 发送结束展示统计
		DisposableStatsUtils.disposeSUE(mediaPlayerDelegate.getPauseAdvInfo().VAL.get(0));
	}
	
	private interface OnShowAdvInBrowserResult {
		public void onFail();

		public void onSuccess();
	}
	
	boolean choosedActivity = false;
	boolean onChooserDlgShow = false;//选择打开的应用对话框是否展示中
	
	private void showInBrowser(String targetUrl,
			final OnShowAdvInBrowserResult result) {
		choosedActivity = false;
		if (TextUtils.isEmpty(targetUrl)) {
			Logger.e(TAG, "广告详情无地址");
			if (result != null) {
				result.onFail();
				return;
			}
		}

		try {
			final Intent advIntent = new Intent();
			advIntent.setAction("android.intent.action.VIEW");
			Uri contentUrl = Uri.parse(targetUrl);
			advIntent.setData(contentUrl);

			final List<ResolveInfo> activities = getContext()
					.getPackageManager().queryIntentActivities(advIntent, 0);

			if (activities.size() == 1) {
				getContext().startActivity(advIntent);
				if (result != null) {
					result.onSuccess();
					return;
				}
			}

			List<String> appNames = new ArrayList<String>();
			for (ResolveInfo info : activities) {
				appNames.add(info.loadLabel(getContext().getPackageManager())
						.toString());
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle("选择要打开的应用");
			builder.setItems(
					appNames.toArray(new CharSequence[appNames.size()]),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							choosedActivity = true;
							ResolveInfo info = activities.get(item);
							advIntent.setPackage(info.activityInfo.packageName);
							getContext().startActivity(advIntent);
							if (result != null) {
								result.onSuccess();
								return;
							}
						}
					});
			AlertDialog alert = builder.create();
			alert.setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface arg0) {
					onChooserDlgShow = false;
					if (!choosedActivity) {
						if (result != null) {
							result.onFail();
							return;
						}
					}
				}
			});
			onChooserDlgShow = true;
			alert.show();
		} catch (Exception e) {
			// 比如没有装浏览器，虽然很少见。。
			Logger.e(TAG, "广告点击打开失败");
			if (result != null) {
				result.onFail();
				return;
			}
		}
	}

	private void startPlay() {
		if (null == mMediaPlayerDelegate)
			return;
		if (!mMediaPlayerDelegate.isAdvShowFinished()) {
			mBasePlayerManager.startPlay();
		} else {
			mMediaPlayerDelegate.start();
		}
	}

	@Override
	public void onBufferingUpdateListener(int percent) {

	}

	@Override
	public void onCompletionListener() {

	}

	@Override
	public boolean onErrorListener(int what, int extra) {
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				containerView.setVisibility(View.GONE);
			}
		});
		return false;
	}

	@Override
	public void OnPreparedListener() {

	}

	@Override
	public void OnSeekCompleteListener() {

	}

	@Override
	public void OnVideoSizeChangedListener(int width, int height) {

	}

	@Override
	public void OnTimeoutListener() {

	}

	@Override
	public void OnCurrentPositionChangeListener(int currentPosition) {
	}

	@Override
	public void onLoadedListener() {
		((Activity) mActivity).runOnUiThread(new Runnable() {
			@Override
			public void run() {
//				play_adButton.setVisibility(View.GONE);
				hideLoading();
			}
		});
	}

	@Override
	public void onLoadingListener() {
		((Activity) mActivity).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showLoading();
			}
		});
	}

	@Override
	public void onUp() {

	}

	@Override
	public void onDown() {

	}

	@Override
	public void onFavor() {
	}

	@Override
	public void onUnFavor() {
	}

	@Override
	public void newVideo() {
	}

	@Override
	public void onVolumnUp() {
	}

	@Override
	public void onVolumnDown() {
	}

	@Override
	public void onMute(boolean mute) {
	}

	@Override
	public void onVideoChange() {
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mCountUpdateTextView.setText("");
//				play_adButton.setVisibility(View.GONE);
				mSwitchPlayer.setVisibility(View.GONE);
//				ad_more.setVisibility(View.GONE);
				mSwitchParent.setVisibility(View.GONE);
                if (Profile.PLANTFORM == Plantform.YOUKU) {
//                	mAdSkip.setVisibility(View.GONE);
                	mAdSkipBlank.setVisibility(View.GONE);
                	mCountUpdateWrap.setVisibility(View.GONE);
                }
			}
		});
	}

	boolean isADPluginShowing = false;

	@Override
	public void onVideoInfoGetting() {
		if (isADPluginShowing) {
			/*
			Track.onError(mActivity, mediaPlayerDelegate.nowVid,
					Profile.GUID, mediaPlayerDelegate.videoInfo.playType,
					PlayCode.VIDEO_ADV_RETURN);
					*/
			mBasePlayerManager.interuptAD();
		}
	}

	@Override
	public void onVideoInfoGetted() {
	}

	@Override
	public void onVideoInfoGetFail(boolean needRetry) {
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			isADPluginShowing = true;
			containerView.setVisibility(View.VISIBLE);
		} else {
			isADPluginShowing = false;
			containerView.setVisibility(View.GONE);
		}
	}

	public void notifyUpdate(int count) {

		if (count <= 0) {
			mCountUpdateTextView.setText("");
			mCountUpdateTextView.setVisibility(View.GONE);
			if (Profile.PLANTFORM == Plantform.YOUKU) {
				mCountUpdateWrap.setVisibility(View.GONE);
			}
			return;
		}
		if (mCountUpdateTextView != null) {
			
			if (Profile.PLANTFORM != Plantform.YOUKU) {
				StringBuilder mytext = new StringBuilder("广告剩余时间");
				mytext.append(count).append("秒");
				mCountUpdateTextView.setText(mytext);
				mCountUpdateTextView.setVisibility(View.VISIBLE);
			} else {
				String str = String.valueOf(count);
				mCountUpdateTextView.setText(str);
				mCountUpdateTextView.setVisibility(View.VISIBLE);
				mCountUpdateWrap.setVisibility(View.VISIBLE);
			}
			
		}

		int visibility = mediaPlayerDelegate.isPlayLocalType() ? View.GONE : View.VISIBLE;
		// TODO:要保持“广告剩余时间”和“全屏”,“详细了解”的同步显示，需要把三者处理显示的时机要一致。
		// 目前onStartPlayAD中没有倒计时的参数，故暂时放在这里处理。这些应该在onStartPlayAD方法中处理。
		mSwitchParent.setVisibility(visibility);
		mSwitchPlayer.setVisibility(visibility);

		if (mediaPlayerDelegate.videoInfo.videoAdvInfo != null) {
			AdvInfo advInfo = getAdvInfo();
			if (advInfo == null) {
				Logger.e("PlayFlow", "PlugiADPlay->notifyUpdate    advInfo = null,   return");
				return;
			}

/*			if (TextUtils.isEmpty(advInfo.CU)) {
				ad_more.setVisibility(View.GONE);
			} else {
				if (AdForward.YOUKU_VIDEO == advInfo.CUF) {
					ad_more.setText(R.string.playersdk_ad_descrip_play_youku);
				} else {
					ad_more.setText(R.string.playersdk_ad_descrip_youku);
				}
				ad_more.setVisibility(View.VISIBLE);
			}*/
		}
	}

	@Override
	public void onPluginAdded() {
		super.onPluginAdded();
				
		if (mBasePlayerManager.isImageADShowing) {
			// 处理暂停广告
			final VideoAdvInfo pauseAdvInfo = mediaPlayerDelegate.getPauseAdvInfo();
			boolean canShow = checkImgAdv(pauseAdvInfo);
			if (!canShow) {
				mBasePlayerManager.isImageADShowing = false;
				mBasePlayerManager.updatePlugin(DetailMessage.PLUGIN_SHOW_NOT_SET);
				return;
			}
			
			if(mAdPageHolder != null){
				mAdPageHolder.setVisibility(View.GONE);
			}
			if(mInteractiveAdContainer != null){
				mInteractiveAdContainer.setVisibility(View.GONE);
			}
			mInteractiveAdContainer.setVisibility(View.GONE);
			
			String rs = pauseAdvInfo.VAL.get(0).RS;
			
            ImageLoader.getInstance().displayImage(rs, imgAdvContentImgView, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    Logger.d(TAG, "暂停图片广告播放取消");
	                  ////显示失败，没有统计。。？？
                }

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					Logger.d(TAG, "暂停图片广告播放成功");
                  //显示暂停图片广告
                  imgAdvLayout.setVisibility(VISIBLE);
                  
                  Logger.d(TAG, "暂停图片广告开始展示，发送开始统计");
                  // 统计
//                  //发送开始统计
                  DisposableStatsUtils.disposeSUS(pauseAdvInfo.VAL.get(0));
//                  //发送VC统计
                  DisposableStatsUtils.disposeVC(pauseAdvInfo.VAL.get(0));
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					Logger.d(TAG, "暂停图片广告播放失败");
	                  ////显示失败，没有统计。。？？
				}
            });
			
			return;
		}
		
		if (mediaPlayerDelegate.isFullScreen) {
			if (Profile.PLANTFORM == Plantform.TUDOU) {
				mSwitchPlayer
						.setImageResource(R.drawable.plugin_ad_gosmall_tudou);
			} else {
				mSwitchPlayer.setImageResource(R.drawable.plugin_ad_gosmall_youku);
			}
		} else {
			if (Profile.PLANTFORM == Plantform.TUDOU) {
				mSwitchPlayer
						.setImageResource(R.drawable.plugin_ad_gofull_tudou);
			} else {
				mSwitchPlayer.setImageResource(R.drawable.plugin_ad_gofull_youku);
			}
		}
		if (UIUtils.hasKitKat()) {
			mBasePlayerManager.hideSystemUI(this);
		}
		mBasePlayerManager.setPluginHolderPaddingZero();
		
	}

	private boolean checkImgAdv(VideoAdvInfo pauseAdvInfo) {
		if (pauseAdvInfo == null) {
			return false;
		}

		if (pauseAdvInfo.VAL == null || pauseAdvInfo.VAL.isEmpty()) {
			return false;
		}

		// 暂停广告应该只有一个值
		AdvInfo imgAdvItem = pauseAdvInfo.VAL.get(0);
		if (!"img".equals(imgAdvItem.RST)) {
			// 不是图片形式的广告
			return false;
		}
		String rs = imgAdvItem.RS;
		if(TextUtils.isEmpty(rs))
		{
			// 没有内容
			return false;
		}
		return true;
	}

	/**
	 * 获取广告信息
	 * 
	 * @return
	 */
	private AdvInfo getAdvInfo() {
		try {
			return mediaPlayerDelegate.videoInfo.videoAdvInfo.VAL.get(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 发送广告统计信息
	 * 
	 * @param stat
	 */
	private void sendStat(Stat stat) {
		new DisposableHttpTask(stat.U).start();
	}

	private void initSeekLoading() {
		if (null == seekLoadingContainerView)
			return;
		playLoadingBar = (SeekBar) seekLoadingContainerView
				.findViewById(R.id.loading_seekbar);
		if (null != playLoadingBar)
			playLoadingBar
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {

						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {

						}

						@Override
						public void onProgressChanged(SeekBar seekBar,
								int progress, boolean fromUser) {
							if (fromUser) {
								//Track.setTrackPlayLoading(false);
								return;
							} else {
								seekBar.setProgress(progress);
							}

						}
					});
	}

	private int seekcount = 0;

	public void showLoading() {

		if (null != seekLoadingContainerView) {
			if (seekLoadingContainerView.getVisibility() == View.GONE) {
				seekLoadingContainerView.setVisibility(View.VISIBLE);
				seekcount = 0;
				seekHandler.sendEmptyMessageDelayed(0, 50);

			}
			if (null != mMediaPlayerDelegate
					&& mMediaPlayerDelegate.getCurrentPosition() > 1000) {
				seekendHandler.sendEmptyMessageDelayed(0, 50);
				seekLoadingContainerView.setBackgroundResource(0);
			} else {
				seekLoadingContainerView
						.setBackgroundResource(R.drawable.bg_play);
			}
		}
	}

	public void hideLoading() {
		((Activity) mActivity).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (null != seekLoadingContainerView) {
					seekLoadingContainerView.setVisibility(View.GONE);
					playLoadingBar.setProgress(0);
				}
				if (null != seekHandler)
					seekHandler.removeCallbacksAndMessages(null);
			}
		});
	}

	private Handler seekHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (seekcount < 50) {
				seekcount++;
				playLoadingBar.setProgress(seekcount);
				Thread temp = new Thread(new Runnable() {

					@Override
					public void run() {
						seekHandler.sendEmptyMessageDelayed(0, 50);
					}
				});
				temp.run();
			} else {
				playLoadingBar.setProgress(50);
			}

		}

	};

	private SeekBar playLoadingBar;
	private Handler seekendHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (seekcount < 100) {
				seekcount++;
				playLoadingBar.setProgress(seekcount);
				Thread temp = new Thread(new Runnable() {

					@Override
					public void run() {
						seekHandler.sendEmptyMessageDelayed(0, 10);
					}
				});
				temp.run();
			}

		}

	};

	@Override
	public void onNotifyChangeVideoQuality() {

	}

	@Override
	public void onRealVideoStart() {
	}

	@Override
	public void onADplaying() {
	}

	@Override
	public void onRealVideoStarted() {

	}

	@Override
	public void onStart() {

	}

	@Override
	public void onClearUpDownFav() {

	}

	@Override
	public void onPause() {

	}

	public void showPlayIcon() {
//		play_adButton.setVisibility(View.VISIBLE);
	}

	@Override
	public void back() {
	}

	@Override
	public void onPlayNoRightVideo(GoplayException e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayReleateNoRightVideo() {
		// TODO Auto-generated method stub

	}

	public static void setAdMoreBackgroundColor(boolean isTudouPlatform) {
		if (isTudouPlatform) {
			sAdMoreBackgroundColor = ADMORE_BACKGROUND_COLOR_TUDOU;
			return;
		}
		sAdMoreBackgroundColor = ADMORE_BACKGROUND_COLOR_YOUKU;
	}

	public boolean isCountUpdateVisible() {
		if (mCountUpdateTextView != null) {
			return mCountUpdateTextView.getVisibility() == View.VISIBLE ? true
					: false;
		}
		return false;
	}

	public void setSkipVisible(boolean visible) {
/*		if (MediaPlayerConfiguration.getInstance().showSkipAdButton() && mAdSkip != null) {
			mAdSkip.setVisibility(visible ? View.VISIBLE : View.GONE);
			if (mAdSkipBlank != null) {
				mAdSkipBlank.setVisibility(visible ? View.VISIBLE : View.GONE);
			}
		}*/
	}
}
