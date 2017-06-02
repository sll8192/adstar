package com.yundian.star.ui.main.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.Toast;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.netease.nim.uikit.LoginSyncDataStatusObserver;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.permission.MPermission;
import com.netease.nim.uikit.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.mixpush.MixPushService;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.yundian.star.R;
import com.yundian.star.app.AppConstant;
import com.yundian.star.base.BaseActivity;
import com.yundian.star.been.TabEntity;
import com.yundian.star.ui.im.fragment.DifferAnswerFragment;
import com.yundian.star.ui.main.fragment.MarketFragment;
import com.yundian.star.ui.main.fragment.NewsInfoFragment;
import com.yundian.star.ui.main.fragment.UserInfoFragment;
import com.yundian.star.ui.wangyi.chatroom.helper.ChatRoomHelper;
import com.yundian.star.ui.wangyi.config.preference.UserPreferences;
import com.yundian.star.utils.LogUtils;
import com.yundian.star.utils.SharePrefUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;


public class MainActivity extends BaseActivity {
    @Bind(R.id.tab_bottom_layout)
    CommonTabLayout tabLayout ;
    private String[] mTitles = {"资讯", "行情","分答","我的"};
    private int[] mIconUnselectIds = {
            R.drawable.message_no_ok,R.drawable.market_no_ok,R.drawable.differ_answer_no_ok,R.drawable.me_no_ok};
    private int[] mIconSelectIds = {
            R.drawable.message_ok,R.drawable.market_ok, R.drawable.differ_answer_ok,R.drawable.me_ok};
    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private NewsInfoFragment newsInfoFragment;
    private MarketFragment marketFragment;
    private DifferAnswerFragment differAnswerFragment;
    private UserInfoFragment userInfoFragment;
    private final int BASIC_PERMISSION_REQUEST_CODE = 100;
    public static int CHECHK_LOGIN = 0;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0 :

                    break;
            }
        }
    };
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            overridePendingTransition(R.anim.activity_open_down_in,0);
        }
    };
    Runnable runnablePermission = new Runnable() {
        @Override
        public void run() {
            requestBasicPermission();
        }
    };

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initPresenter() {
    }

    @Override
    public void initView() {
        initTab();
        checkIsLogin();
        checkunReadMsg();
    }

    private void checkunReadMsg() {
        int unreadNum = NIMClient.getService(MsgService.class).getTotalUnreadCount();
        if (unreadNum>0){
            tabLayout.showDot(2);
        }else {
            tabLayout.hideMsg(2);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化frament
        initFragment(savedInstanceState);
        initWangYi();
        registerObservers(true);
    }



    private void initFragment(Bundle savedInstanceState) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        int currentTabPosition = 0;
        if (savedInstanceState != null) {
            newsInfoFragment = (NewsInfoFragment) getSupportFragmentManager().findFragmentByTag("NewsInfoFragment");
            marketFragment = (MarketFragment) getSupportFragmentManager().findFragmentByTag("MarketFragment");
            differAnswerFragment = (DifferAnswerFragment) getSupportFragmentManager().findFragmentByTag("DifferAnswerFragment");
            userInfoFragment = (UserInfoFragment) getSupportFragmentManager().findFragmentByTag("UserInfoFragment");
            currentTabPosition = savedInstanceState.getInt(AppConstant.HOME_CURRENT_TAB_POSITION);
        } else {
            newsInfoFragment = new NewsInfoFragment();
            marketFragment = new MarketFragment();
            differAnswerFragment = new DifferAnswerFragment();
            userInfoFragment = new UserInfoFragment();
            transaction.add(R.id.fl_main, newsInfoFragment, "NewsInfoFragment");
            transaction.add(R.id.fl_main, marketFragment, "MarketFragment");
            transaction.add(R.id.fl_main, differAnswerFragment, "DifferAnswerFragment");
            transaction.add(R.id.fl_main, userInfoFragment, "UserInfoFragment");
        }
        transaction.commit();
        SwitchTo(currentTabPosition);
        tabLayout.setCurrentTab(currentTabPosition);
    }

    /**
     * 入口
     * @param activity
     */
    public static void startAction(Activity activity){
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in,
                R.anim.fade_out);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //奔溃前保存位置
        LogUtils.loge("onSaveInstanceState进来了1");
        if (tabLayout != null) {
            LogUtils.loge("onSaveInstanceState进来了2");
            outState.putInt(AppConstant.HOME_CURRENT_TAB_POSITION, tabLayout.getCurrentTab());
        }
    }

    private void initWangYi() {
        // 等待同步数据完成
        boolean syncCompleted = LoginSyncDataStatusObserver.getInstance().observeSyncDataCompletedEvent(new Observer<Void>() {
            @Override
            public void onEvent(Void v) {

                syncPushNoDisturb(UserPreferences.getStatusConfig());

                DialogMaker.dismissProgressDialog();
            }
        });

        LogUtils.logd("sync completed = " + syncCompleted);
        if (!syncCompleted) {
            DialogMaker.showProgressDialog(MainActivity.this, getString(R.string.prepare_data)).setCanceledOnTouchOutside(false);
        } else {
            syncPushNoDisturb(UserPreferences.getStatusConfig());
        }
        // 聊天室初始化
        ChatRoomHelper.init();
    }
    /**
     * 切换
     */
    private void SwitchTo(int position) {
        LogUtils.logd("主页菜单position" + position);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (position) {
            case 0:
                transaction.hide(marketFragment);
                transaction.hide(differAnswerFragment);
                transaction.hide(userInfoFragment);
                transaction.show(newsInfoFragment);
                transaction.commitAllowingStateLoss();
                break;
            case 1:
                transaction.hide(newsInfoFragment);
                transaction.hide(differAnswerFragment);
                transaction.hide(userInfoFragment);
                transaction.show(marketFragment);
                transaction.commitAllowingStateLoss();
                break;
            case 2:
                transaction.hide(marketFragment);
                transaction.hide(newsInfoFragment);
                transaction.hide(userInfoFragment);
                transaction.show(differAnswerFragment);
                transaction.commitAllowingStateLoss();
                break;
            case 3:
                transaction.hide(marketFragment);
                transaction.hide(newsInfoFragment);
                transaction.hide(differAnswerFragment);
                transaction.show(userInfoFragment);
                transaction.commitAllowingStateLoss();
                break;
            default:
                break;
        }
    }
    /**
     * 初始化tab
     */
    private void initTab() {
        for (int i = 0; i < mTitles.length; i++) {
            mTabEntities.add(new TabEntity(mTitles[i], mIconSelectIds[i], mIconUnselectIds[i]));
        }
        tabLayout.setTabData(mTabEntities);
        //点击监听
        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                SwitchTo(position);
            }
            @Override
            public void onTabReselect(int position) {
            }
        });
    }
    private void checkIsLogin() {
        String phoneNum = SharePrefUtil.getInstance().getPhoneNum();
        String token = SharePrefUtil.getInstance().getToken();
        if (TextUtils.isEmpty(phoneNum)||TextUtils.isEmpty(token)) { // 第一次登录, 需要走登录流程
            handler.postDelayed(runnable,500);
        }
        handler.postDelayed(runnablePermission,1000);
    }

    /**
     * 基本权限管理
     */
    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_NETWORK_STATE
    };

    private void requestBasicPermission() {
        MPermission.printMPermissionResult(true, this, BASIC_PERMISSIONS);
        MPermission.with(MainActivity.this)
                .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS)
                .request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        Toast.makeText(this, "未全部授权，部分功能可能无法正常运行！", Toast.LENGTH_SHORT).show();
        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    /**
     * 若增加第三方推送免打扰（V3.2.0新增功能），则：
     * 1.添加下面逻辑使得 push 免打扰与先前的设置同步。
     * 2.设置界面 com.netease.nim.demo.main.activity.SettingsActivity 以及
     * 免打扰设置界面 com.netease.nim.demo.main.activity.NoDisturbActivity 也应添加 push 免打扰的逻辑
     * <p>
     * 注意：isPushDndValid 返回 false， 表示未设置过push 免打扰。
     */
    private void syncPushNoDisturb(StatusBarNotificationConfig staConfig) {

        boolean isNoDisbConfigExist = NIMClient.getService(MixPushService.class).isPushNoDisturbConfigExist();

        if (!isNoDisbConfigExist && staConfig.downTimeToggle) {
            NIMClient.getService(MixPushService.class).setPushNoDisturbConfig(staConfig.downTimeToggle,
                    staConfig.downTimeBegin, staConfig.downTimeEnd);
        }
    }

    /**
     * ********************** 收消息，处理状态变化 ************************
     */
    private void registerObservers(boolean register) {
        MsgServiceObserve service = NIMClient.getService(MsgServiceObserve.class);
        service.observeRecentContact(messageObserver, register);
    }
    Observer<List<RecentContact>> messageObserver = new Observer<List<RecentContact>>() {
        @Override
        public void onEvent(List<RecentContact> recentContacts) {
            checkunReadMsg();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObservers(false);
    }

}
