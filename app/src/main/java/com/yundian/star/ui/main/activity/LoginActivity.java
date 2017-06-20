package com.yundian.star.ui.main.activity;

import android.graphics.Point;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.cache.DataCacheManager;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.yundian.star.R;
import com.yundian.star.app.AppApplication;
import com.yundian.star.base.BaseActivity;
import com.yundian.star.been.EventBusMessage;
import com.yundian.star.been.LoginReturnInfo;
import com.yundian.star.been.RegisterReturnWangYiBeen;
import com.yundian.star.helper.CheckHelper;
import com.yundian.star.listener.OnAPIListener;
import com.yundian.star.networkapi.NetworkAPIException;
import com.yundian.star.networkapi.NetworkAPIFactoryImpl;
import com.yundian.star.ui.wangyi.DemoCache;
import com.yundian.star.ui.wangyi.config.preference.Preferences;
import com.yundian.star.ui.wangyi.config.preference.UserPreferences;
import com.yundian.star.utils.LogUtils;
import com.yundian.star.utils.MD5Util;
import com.yundian.star.utils.SharePrefUtil;
import com.yundian.star.utils.ToastUtils;
import com.yundian.star.utils.ViewConcurrencyUtils;
import com.yundian.star.widget.CheckException;
import com.yundian.star.widget.WPEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Null on 2017/5/7.
 * 登录
 */

public class LoginActivity extends BaseActivity {
    @Bind(R.id.userNameEditText)
    WPEditText userNameEditText;
    @Bind(R.id.passwordEditText)
    WPEditText passwordEditText;
    @Bind(R.id.loginButton)
    Button loginButton;
    @Bind(R.id.registerText)
    TextView registerText;
    @Bind(R.id.tv_retrieve_password)
    TextView tv_retrieve_password;
    @Bind(R.id.tv_weixin_login)
    TextView tv_weixin_login;
    private CheckHelper checkHelper = new CheckHelper();
    private AbortableFuture<LoginInfo> loginRequest;
    private long exitNow;
    boolean flag = true;

    @Override
    public int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void initView() {
        if (flag) {
            EventBus.getDefault().register(this); // EventBus注册广播()
            flag = false;//更改标记,使其不会再进行多次注册
        }
        WindowManager.LayoutParams p = getWindow().getAttributes();// 获取对话框当前的参值
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        p.width = (int) (size.x * 0.85);
        getWindow().setAttributes(p); // 设置生效
        userNameEditText.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        checkHelper.checkButtonState(loginButton, userNameEditText, passwordEditText);
        String phoneNum = SharePrefUtil.getInstance().getLoginPhone();
        if (!TextUtils.isEmpty(phoneNum)) {
            userNameEditText.getEditText().setText(phoneNum);
        }
    }

    @OnClick(R.id.loginButton)
    public void loging() {
        ViewConcurrencyUtils.preventConcurrency();  //防止并发
        CheckException exception = new CheckException();
        LogUtils.loge(MD5Util.MD5(passwordEditText.getEditTextString()));
        if (checkHelper.checkMobile(userNameEditText.getEditTextString(), exception)
                && checkHelper.checkPassword(passwordEditText.getEditTextString(), exception)) {
            NetworkAPIFactoryImpl.getUserAPI().login(userNameEditText.getEditTextString(), MD5Util.MD5(passwordEditText.getEditTextString()), new OnAPIListener<LoginReturnInfo>() {
                @Override
                public void onError(Throwable ex) {
                    LogUtils.logd("登录失败_____" + ex.toString());
                    ToastUtils.showShort("登录失败");
                }

                @Override
                public void onSuccess(final LoginReturnInfo loginReturnInfo) {
                    if (loginReturnInfo.getResult() == -301) {
                        ToastUtils.showShort("用户不存在,请先注册");
                        return;
                    } else if (loginReturnInfo.getResult() == -302) {
                        ToastUtils.showShort("账号或密码错误");
                        return;
                    } else if (loginReturnInfo.getResult() == -303) {
                        ToastUtils.showShort("登录信息失效，请重新登录");
                        return;
                    } else if (loginReturnInfo.getResult() == -304) {
                        ToastUtils.showShort("用户已存在");
                        return;
                    } else if (loginReturnInfo != null && loginReturnInfo.getUserinfo() != null) {
                        LogUtils.logd("登录成功" + loginReturnInfo.toString());
                        //网易云注册
                        NetworkAPIFactoryImpl.getUserAPI().registerWangYi(userNameEditText.getEditTextString(), userNameEditText.getEditTextString(), userNameEditText.getEditTextString(), new OnAPIListener<RegisterReturnWangYiBeen>() {
                            @Override
                            public void onError(Throwable ex) {
                                LogUtils.logd("网易云注册失败" + ex.toString());
                                ToastUtils.showShort("网易云注册失败");
                            }

                            @Override
                            public void onSuccess(RegisterReturnWangYiBeen registerReturnWangYiBeen) {
                                LogUtils.logd("网易云注册成功" + registerReturnWangYiBeen.getResult_value() + "网易云token" + registerReturnWangYiBeen.getToken_value());
                                loginWangYi(loginReturnInfo, registerReturnWangYiBeen);
                            }
                        });
                    }

                }
            });
        } else {
            showLongToast(exception.getErrorMsg());
        }


    }

    private void loginWangYi(final LoginReturnInfo loginReturnInfos, RegisterReturnWangYiBeen registerReturnWangYiBeen) {
        LogUtils.logd(loginReturnInfos.getUserinfo().getPhone() + "..." + registerReturnWangYiBeen.getToken_value());
        // 登录
        loginRequest = NimUIKit.doLogin(new LoginInfo(loginReturnInfos.getUserinfo().getPhone(), registerReturnWangYiBeen.getToken_value()), new RequestCallback<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo param) {
                LogUtils.logd("网易云登录成功:" + param.toString());
                ToastUtils.showStatusView("登陆成功", true);
                DemoCache.setAccount(param.getAccount());
                saveLoginInfo(param.getAccount(), param.getToken());
                // 初始化消息提醒配置
                initNotificationConfig();
                // 构建缓存
                DataCacheManager.buildDataCacheAsync();
                SharePrefUtil.getInstance().saveLoginUserInfo(loginReturnInfos);
                SharePrefUtil.getInstance().putLoginPhone(loginReturnInfos.getUserinfo().getPhone());
                EventBus.getDefault().postSticky(new EventBusMessage(1));  //登录成功消息
                LoginActivity.this.finish();
                LoginActivity.this.overridePendingTransition(0, R.anim.activity_off_top_out);
            }

            @Override
            public void onFailed(int code) {
                if (code == 302 || code == 404) {
                    LogUtils.logd("网易云登录失败" + R.string.login_failed);
                } else {
                    LogUtils.logd("网易云登录失败" + code);
                }
            }

            @Override
            public void onException(Throwable exception) {
                LogUtils.logd("网易云登录失败" + R.string.login_exception);
            }
        });
    }

    @OnClick(R.id.registerText)
    public void doingReregister() {
        startActivity(RegisterUserActivity.class);
        finish();
        overridePendingTransition(R.anim.activity_open_down_in, R.anim.activity_off_top_out);
    }

    @OnClick(R.id.tv_retrieve_password)
    public void retrievePassword() {
        ViewConcurrencyUtils.preventConcurrency();  //防止并发
        startActivity(ResetUserPwdActivity.class);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            close();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void saveLoginInfo(final String account, final String token) {
        Preferences.saveUserAccount(account);
        Preferences.saveUserToken(token);
    }

    private void initNotificationConfig() {
        // 初始化消息提醒
        NIMClient.toggleNotification(UserPreferences.getNotificationToggle());

        // 加载状态栏配置
        StatusBarNotificationConfig statusBarNotificationConfig = UserPreferences.getStatusConfig();
        if (statusBarNotificationConfig == null) {
            statusBarNotificationConfig = DemoCache.getNotificationConfig();
            UserPreferences.setStatusConfig(statusBarNotificationConfig);
        }
        // 更新配置
        NIMClient.updateStatusBarNotificationConfig(statusBarNotificationConfig);
    }

    @OnClick(R.id.tv_weixin_login)
    public void weixinLogin() {
        ViewConcurrencyUtils.preventConcurrency();  //防止并发
        if (!AppApplication.api.isWXAppInstalled()) {
            ToastUtils.showShort("您还未安装微信客户端");
            return;
        }
        final SendAuth.Req req = new SendAuth.Req();
        ToastUtils.showShort("微信登录");
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test";
        AppApplication.api.sendReq(req);

    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    //接收消息
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void ReciveMessageEventBus(EventBusMessage eventBusMessage) {
        switch (eventBusMessage.Message) {
            case -6:  //成功
                LogUtils.loge("当前是接收到微信登录成功的消息,finish");
                finish();
                break;
        }
    }


    @OnClick(R.id.close)
    public void close() {
        EventBus.getDefault().postSticky(new EventBusMessage(2));  //登录取消消息
        finish();
    }
}
