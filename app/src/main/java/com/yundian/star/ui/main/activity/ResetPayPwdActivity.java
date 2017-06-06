package com.yundian.star.ui.main.activity;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yundian.star.R;
import com.yundian.star.app.Constant;
import com.yundian.star.base.BaseActivity;
import com.yundian.star.been.RegisterVerifyCodeBeen;
import com.yundian.star.been.RequestResultBean;
import com.yundian.star.helper.CheckHelper;
import com.yundian.star.listener.OnAPIListener;
import com.yundian.star.networkapi.NetworkAPIException;
import com.yundian.star.networkapi.NetworkAPIFactoryImpl;
import com.yundian.star.networkapi.socketapi.SocketReqeust.SocketAPINettyBootstrap;
import com.yundian.star.utils.CountUtil;
import com.yundian.star.utils.LogUtils;
import com.yundian.star.utils.MD5Util;
import com.yundian.star.utils.ToastUtils;
import com.yundian.star.widget.CheckException;
import com.yundian.star.widget.NormalTitleBar;
import com.yundian.star.widget.WPEditText;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/5/10.
 */

public class ResetPayPwdActivity extends BaseActivity {

    @Bind(R.id.nt_title)
    NormalTitleBar nt_title;
    @Bind(R.id.phoneEditText)
    WPEditText phoneEditText;
    @Bind(R.id.msgEditText)
    WPEditText msgEditText;
    @Bind(R.id.pwdEditText1)
    WPEditText pwdEditText1;
    @Bind(R.id.pwdEditText2)
    WPEditText pwdEditText2;
    @Bind(R.id.okButton)
    Button okButton;

    private RegisterVerifyCodeBeen verifyCodeBeen;

    private CheckHelper checkHelper = new CheckHelper();
    private boolean isResetPayPwd = false;

    @Override
    public int getLayoutId() {
        return R.layout.activity_reset_user_pwd;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void initView() {
        initData();
        checkHelper.checkButtonState(okButton, phoneEditText, msgEditText, pwdEditText1, pwdEditText2);
        checkHelper.checkVerificationCode(msgEditText.getRightText(), phoneEditText);
        msgEditText.getRightText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!SocketAPINettyBootstrap.getInstance().isOpen()) {
                    ToastUtils.showShort("网络连接失败,请检查网络");
                    return;
                }
                getCode(msgEditText, view, phoneEditText);
            }
        });


    }

    private void initData() {
        nt_title.setTvLeftVisiable(true);
        phoneEditText.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        String title = getString(R.string.reset_login_pwd);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String resetPwd = bundle.getString("resetPwd");
            if (resetPwd != null && resetPwd.equals(Constant.PAY_PWD)) {  //重置交易密码
                title = getString(R.string.reset_pay_pwd);
                //设置密码为六位数字输入类型
                pwdEditText1.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
                pwdEditText2.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
                pwdEditText1.getEditText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                pwdEditText1.getEditText().setHint("请输入六位数字支付密码");
                pwdEditText2.getEditText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                isResetPayPwd = true;
            } else if (resetPwd != null && resetPwd.equals(Constant.USER_PWD)) {  //重置用户密码

            }
        }
        nt_title.setTitleText(title);
    }

    @OnClick(R.id.okButton)
    public void okButton() {
        LogUtils.logd("此时网络的连接状态是:" + SocketAPINettyBootstrap.getInstance().isOpen());
        CheckException exception = new CheckException();
        if (checkHelper.checkMobile(phoneEditText.getEditTextString(), exception)
                && checkHelper.checkMobile(phoneEditText.getEditTextString(), exception)
                && checkHelper.checkPassword(pwdEditText1.getEditTextString(), exception)
                && checkHelper.checkPassword2(pwdEditText1.getEditTextString(), pwdEditText2.getEditTextString(), exception)) {
            if (isResetPayPwd) {
                resetPayPwd();
            } else {
                resetUserPwd();
            }

        } else {
            showShortToast(exception.getErrorMsg());
        }

    }

    private void resetUserPwd() {
        checkInputInfo();
        NetworkAPIFactoryImpl.getUserAPI().resetPasswd(phoneEditText.getEditTextString(), MD5Util.MD5(pwdEditText2.getEditTextString())
                , new OnAPIListener<Object>() {
                    @Override
                    public void onError(Throwable ex) {
                        ToastUtils.showShort("修改登录密码失败");
                    }

                    @Override
                    public void onSuccess(Object o) {
                        JSONObject object = JSON.parseObject(o.toString());
                        int result = object.getInteger("result");
                        if (result == -301) {
                            ToastUtils.showShort("用户不存在");
                        } else if (result == 1) {
                            ToastUtils.showShort("修改登录密码成功");
                            finish();
                        }
                    }
                });
    }

    private void checkInputInfo() {
        //本地校验验证码   MD5(yd1742653sd + code_time + rand_code + phone)
        if (verifyCodeBeen == null || TextUtils.isEmpty(verifyCodeBeen.getVToken())) {
            ToastUtils.showShort("无效验证码,请重新获取");
            return;
        }
        if (!verifyCodeBeen.getVToken().equals(MD5Util.MD5("yd1742653sd" + verifyCodeBeen.getTimeStamp() + msgEditText.getEditTextString() + phoneEditText.getEditTextString()))) {
            ToastUtils.showShort("验证码错误,请重新输入");
            return;
        }
        //int type = 0;//0：登录密码 1：交易密码，提现密码
        if (!TextUtils.equals(pwdEditText1.getEditTextString(), pwdEditText2.getEditTextString())) {
            ToastUtils.showShort("2次密码不一致");
            return;
        }
    }

    private void getCode(final WPEditText msgEditText, View view, WPEditText phoneEditText) {
        LogUtils.logd("请求网络获取短信验证码------------------------------");
        CheckException exception = new CheckException();
        String phoneEdit = phoneEditText.getEditTextString();
        if (new CheckHelper().checkMobile(phoneEdit, exception)) {
            //Utils.closeSoftKeyboard(view);
            NetworkAPIFactoryImpl.getUserAPI().verifyCode(phoneEdit, new OnAPIListener<RegisterVerifyCodeBeen>() {
                @Override
                public void onError(Throwable ex) {
                    ex.printStackTrace();
                    LogUtils.logd("验证码请求网络错误------------------" + ((NetworkAPIException) ex).getErrorCode());
                }

                @Override
                public void onSuccess(RegisterVerifyCodeBeen o) {
                    verifyCodeBeen = o;
                    new CountUtil((TextView) msgEditText.getRightText()).start();   //收到回调才开启计时
                    LogUtils.logd("获取到--注册短信验证码,时间戳是:" + o.toString());
                }
            });
        } else {
            ToastUtils.showShort(exception.getErrorMsg());
        }
    }


    /**
     * 重置支付密码
     */
    private void resetPayPwd() {
        checkInputInfo();
        String vCode = msgEditText.getEditTextString();
        int type = 1; //0-设置密码  1-重置密码
        String pwd = MD5Util.MD5(pwdEditText2.getEditTextString());  //加密之后
        if (verifyCodeBeen == null) {
            ToastUtils.showShort("验证码需要重新获取");
            return;
        }
        NetworkAPIFactoryImpl.getDealAPI().dealPwd(phoneEditText.getEditTextString(),verifyCodeBeen.getVToken(), vCode, verifyCodeBeen.getTimeStamp(), type, pwd, new OnAPIListener<RequestResultBean>() {
            @Override
            public void onSuccess(RequestResultBean resultBean) {
                LogUtils.logd("支付密码成功回调:" + resultBean.toString());
                if (resultBean.getStatus() == 0) {
                    ToastUtils.showShort("修改支付密码成功");
                    finish();
                } else {
                    ToastUtils.showShort("修改支付密码失败");
                }
            }


            @Override
            public void onError(Throwable ex) {
                ToastUtils.showShort("支付密码重置失败");
            }
        });
    }
}