package com.yundian.star.ui.main.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.netease.nimlib.jsbridge.util.LogUtil;
import com.yundian.star.R;
import com.yundian.star.app.AppConstant;
import com.yundian.star.base.BaseActivity;
import com.yundian.star.been.StarListReturnBean;
import com.yundian.star.been.TradingStatusBeen;
import com.yundian.star.listener.OnAPIListener;
import com.yundian.star.networkapi.NetworkAPIFactoryImpl;
import com.yundian.star.utils.DisplayUtil;
import com.yundian.star.utils.ImageLoaderUtils;
import com.yundian.star.utils.LogUtils;
import com.yundian.star.utils.SharePrefUtil;
import com.yundian.star.utils.TimeUtil;
import com.yundian.star.widget.BiliDanmukuParser;
import com.yundian.star.widget.CenteredImageSpan;
import com.yundian.star.widget.CircleDrawable;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.Duration;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;

/**
 * Created by Administrator on 2017/7/18.
 * 明星时间交易
 */

public class StarTimeDealActivity extends BaseActivity implements View.OnClickListener {

    private IDanmakuView mDanmakuView;
    private DanmakuContext mDanmakuContext;
    private TextView tv_back;
    private TextView tv_title;
    private TextView tv_right;
    private TextView tv_transfer;
    private TextView tv_ask_to_buy;
    private BaseDanmakuParser mParser;
    private int widthPixels;
    private int heightPixels;
    private ArrayList<String> list;
    private MyHandler myHandler;
    private int temporary = 0;
    private int[] random_bg = {
            R.drawable.bg_1, R.drawable.bg_2, R.drawable.bg_3, R.drawable.bg_4, R.drawable.bg_5
            , R.drawable.bg_6, R.drawable.bg_7, R.drawable.bg_8, R.drawable.bg_9, R.drawable.bg_10
            , R.drawable.bg_11
    };
    private String starTypeInfo[] = {"网红", "娱乐明星", "体育明星", "艺人", "海外知名人士", "测试"};
    private StarListReturnBean.SymbolInfoBean symbolInfoBean;
    private TextView tv_time;
    private ImageView qiu;

    @Override
    public int getLayoutId() {
        return R.layout.activity_star_time_deal;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void initView() {
        symbolInfoBean = getIntent().getParcelableExtra(AppConstant.SYMBOL_INFO_BEAN);
        getKuanGao();
        initFindById();
        setSize();
        getData();
    }

    private void getData() {
        list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add("i");
        }
        if (list.size() > 0) {
            qiu.setImageAlpha(125);
        }
        initTradingStatus(false);
        //myHandler.sendEmptyMessage(MyHandler.GRT_DATA);

    }

    private void initFindById() {
        mDanmakuView = (IDanmakuView) findViewById(R.id.sv_danmaku);
        FrameLayout fl_cont = (FrameLayout) findViewById(R.id.fl_cont);
        ViewGroup.LayoutParams params = fl_cont.getLayoutParams();
        params.height = 2*heightPixels;
        fl_cont.setLayoutParams(params);
        qiu = (ImageView) findViewById(R.id.qiu);
        tv_back = (TextView) findViewById(R.id.tv_back);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_right = (TextView) findViewById(R.id.tv_right);
        tv_transfer = (TextView) findViewById(R.id.tv_transfer);
        tv_ask_to_buy = (TextView) findViewById(R.id.tv_ask_to_buy);
        tv_title.setText(getString(R.string.time_deal));
        tv_right.setText(getString(R.string.flea_market));
        ImageView img_head = (ImageView) findViewById(R.id.img_head);
        TextView tv_name = (TextView) findViewById(R.id.tv_name);
        TextView tv_info = (TextView) findViewById(R.id.tv_info);
        TextView tv_price = (TextView) findViewById(R.id.tv_preice);
        tv_time = (TextView) findViewById(R.id.tv_time);
        ImageLoaderUtils.displaySmallPhoto(mContext, img_head, symbolInfoBean.getPic());
        tv_name.setText(symbolInfoBean.getName());
        tv_info.setText(starTypeInfo[symbolInfoBean.getStar_type()]);
        tv_price.setText(String.format("%.2f", symbolInfoBean.getCurrentPrice()));
        RelativeLayout rl_bg = (RelativeLayout) findViewById(R.id.rl_bg);
        int i = new Random().nextInt(11);
        rl_bg.setBackgroundResource(random_bg[i]);
        tv_back.setOnClickListener(this);
        tv_right.setOnClickListener(this);
        tv_transfer.setOnClickListener(this);
        tv_ask_to_buy.setOnClickListener(this);
        mDanmakuContext = DanmakuContext.create();
        if (myHandler == null) {
            myHandler = new MyHandler(this);
        }
        initDanmakuView();
    }

    private void initDanmakuView() {
        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5); // 滚动弹幕最大显示5行
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);

        mDanmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3).setDuplicateMergingEnabled(false).setScrollSpeedFactor(1.6f).setScaleTextSize(1.2f)
                .setCacheStuffer(new SpannedCacheStuffer(), mCacheStufferAdapter) // 图文混排使用SpannedCacheStuffer
//        .setCacheStuffer(new BackgroundCacheStuffer())  // 绘制背景使用BackgroundCacheStuffer
                .setMaximumLines(maxLinesPair)
                // .setMarginTop(40)
                .setCacheStuffer(new BackgroundCacheStuffer(), mCacheStufferAdapter)  // 绘制背景使用BackgroundCacheStuffer
                .preventOverlapping(overlappingEnablePair).setDanmakuMargin(40);







        /*// 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5); // 滚动弹幕最大显示5行
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_SPECIAL, true);
        mDanmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_NONE, 3).setDuplicateMergingEnabled(false).setScrollSpeedFactor(0)
                .setScaleTextSize(1.2f).setDanmakuTransparency(0.8f).setSpecialDanmakuVisibility(true)
                .setCacheStuffer(new SpannedCacheStuffer(), new BaseCacheStuffer.Proxy() {
                    @Override
                    public void prepareDrawing(BaseDanmaku danmaku, boolean fromWorkerThread) {

                    }

                    @Override
                    public void releaseResource(BaseDanmaku danmaku) {

                    }
                }) // 图文混排使用SpannedCacheStuffer
                .setCacheStuffer(new BackgroundCacheStuffer(), mCacheStufferAdapter)  // 绘制背景使用BackgroundCacheStuffer
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair).setDanmakuMargin(40);*/
        if (mDanmakuView != null) {
            mParser = createParser(this.getResources().openRawResource(R.raw.comments));
            mDanmakuView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
                @Override
                public void updateTimer(DanmakuTimer timer) {
                }

                @Override
                public void drawingFinished() {

                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {
//                    Log.d("DFM", "danmakuShown(): text=" + danmaku.text);
                }

                @Override
                public void prepared() {
                    mDanmakuView.start();
                }
            });
            mDanmakuView.setOnDanmakuClickListener(new IDanmakuView.OnDanmakuClickListener() {

                @Override
                public boolean onDanmakuClick(IDanmakus danmakus) {
                    Log.d("DFM", "onDanmakuClick: danmakus size:" + danmakus.size());
                    BaseDanmaku latest = danmakus.last();
                    if (null != latest) {
                        Log.d("DFM", "onDanmakuClick: text of latest danmaku:" + latest.text);
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean onDanmakuLongClick(IDanmakus danmakus) {
                    return false;
                }

                @Override
                public boolean onViewClick(IDanmakuView view) {
                    return false;
                }
            });
            mDanmakuView.prepare(mParser, mDanmakuContext);
            mDanmakuView.enableDanmakuDrawingCache(true);
        }
    }

    private void refreshAnim() {
        if (tv_time != null && secondTime >= 0 && myHandler != null && startSunTime) {
            tv_time.setText(TimeUtil.getHMS(secondTime * 1000));
            secondTime--;
            if (myHandler != null) {
                myHandler.sendEmptyMessageDelayed(myHandler.GRT_DATA, 1 * 1000);
            }
        } else if (tv_time != null && secondTime < 0) {
            tv_time.setText("未开始");
        }
        if (list.size() != 0 && list.get(temporary) != null) {
            addDanmaKuShowTextAndImage(list.get(temporary), false);
            if (temporary < list.size() - 1 && myHandler != null) {
                temporary++;
            }
        }
    }


    private void addDanmaKuShowTextAndImage(String content, final boolean islive) {
        //Math.floor(Math.random()*(max-min+1)+min);
        final BaseDanmaku danmaku = mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SPECIAL,mDanmakuContext);
        danmaku.setDuration(new Duration(60*1000));
        float floor = (float) Math.floor(Math.random() * (6 + 4 + 1) - 3);
        float floorY = (float) Math.floor(Math.random() * (10 - 1 + 1) + 1);
        float dH = floor * 100 * display;
        float dY = floorY * 20 * display;
        float d = (widthPixels + dH + dY);
        long time = (long) (d*(display<1.6?24:12));
        Log.e("floor:", floor + "");
        mDanmakuContext.mDanmakuFactory.fillTranslationData(danmaku, 0,
                0, 0, 0, (long) (d*24/display), 0, 1, 1);

        float[][] points = new float[2][2];
            points[0][0] = d;
            points[0][1] = 0;
            points[1][0] = (float) (-2*d);
            points[1][1] = (float) (3*d);
        mDanmakuContext.mDanmakuFactory.fillLinePathData(danmaku,points,1f,1f);

                Log.e("(long)判断:", widthPixels + ".." + heightPixels);
        Log.e("(long)1:", (float) (-d) + "");
        Log.e("(long)2:", (float) (2 * d) + "");
        // Log.e("(long)3:", (long) Math.sqrt(Math.pow(d, 2.0)) * 3 + "");
        Log.e("(long)4:", (long) (Math.sqrt(2) * d * 5 + dH) + "");
        //(long) (7*widthPixels + (floor > 0 ? floor * (widthPixels + dH + dY): floor * 100))
        //mDanmakuContext.mDanmakuFactory.fillAlphaData(danmaku, AlphaValue.MAX * 1, AlphaValue.MAX * 0, 1000 * 30);
        mDanmakuContext.setMaximumVisibleSizeInScreen(30);
        if (danmaku == null || mDanmakuView == null) {
            return;
        }
        danmaku.rotationY = -1;
        danmaku.rotationZ = -45;
        // Drawable drawable = getResources().getDrawable(R.drawable.ic_home_normal);
        //drawable.setBounds(0, 0, DisplayUtil.dip2px(40), DisplayUtil.dip2px(40));
        String url = "http://tva2.sinaimg.cn/crop.0.1.510.510.180/48e837eejw8ex30o7eoylj20e60e8wet.jpg";
        //ImageLoaderUtils.displaySmallPhoto();
        Glide.with(mContext).load(url)
                .asBitmap()
                .placeholder(R.drawable.user_default_head)
                .error(R.drawable.user_default_head)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        Bitmap bitmap = ImageLoaderUtils.getDefaultBitmap(resource, 45, BITMAP_WIDTH, BITMAP_HEIGHT);
                        CircleDrawable drawable = new CircleDrawable(bitmap);
                        //BitmapDrawable drawable=new BitmapDrawable(resource);
                        //Drawable drawable = getResources().getDrawable(R.drawable.ic_home_normal);
                        LogUtil.e("danmaku" + danmaku.getLeft() + "");
                        drawable.setBounds(0, 0, BITMAP_WIDTH, BITMAP_HEIGHT);
                        SpannableStringBuilder spannable = createSpannable(drawable);
                        danmaku.text = spannable;
                        danmaku.padding = DANMU_PADDING;
                        //danmaku.setDuration(new Duration(1000 * 60));
                        danmaku.priority = 1;  // 一定会显示, 一般用于本机发送的弹幕
                        danmaku.isLive = islive;
                        danmaku.setTime(mDanmakuView.getCurrentTime());
                        danmaku.textSize = DANMU_TEXT_SIZE;
                        danmaku.textColor = 0xfafafafa;
                        danmaku.underlineColor = 0;
                        danmaku.textShadowColor = 0; // 重要：如果有图文混排，最好不要设置描边(设textShadowColor=0)，否则会进行两次复杂的绘制导致运行效率降低
                        LogUtil.e("mDanmakuView.addDanmaku(danmaku);:");
                        mDanmakuView.addDanmaku(danmaku);
                    }
                });

    }

    private SpannableStringBuilder createSpannable(Drawable drawable) {
        String text = "bitmap";
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
        //CenteredImageSpan span = new CenteredImageSpan(drawable);//ImageSpan.ALIGN_BOTTOM);
        CenteredImageSpan span = new CenteredImageSpan(drawable);
        //ImageSpan span = new ImageSpan(resource);
        spannableStringBuilder.setSpan(span, 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("   XXX求购 100.00/秒                   ");
        //spannableStringBuilder.setSpan(new TextAppearanceSpan(this, R.style.style_pingjie), 0, spannableStringBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        //spannableStringBuilder.setSpan(new BackgroundColorSpan(Color.parseColor("#fafafa")), 0, spannableStringBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spannableStringBuilder;
    }

    private float display;

    private void getKuanGao() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        widthPixels = dm.widthPixels;
        heightPixels = dm.heightPixels;
        display = widthPixels / 720;
        Log.e("widthPixels", widthPixels + "...heightPixels" + heightPixels);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                LogUtil.e("tv_back点击了");
                finish();
                break;
            case R.id.tv_right:
                startActivity(FleaMarketActivity.class);
                break;
            case R.id.tv_transfer:
                Intent intent = new Intent(this, BuyTransferIndentActivity.class);
                intent.putExtra(AppConstant.BUY_TRANSFER_INTENT_TYPE, 0);
                intent.putExtra(AppConstant.STAR_WID, symbolInfoBean.getWid());
                intent.putExtra(AppConstant.STAR_NAME, symbolInfoBean.getName());
                intent.putExtra(AppConstant.STAR_CODE, symbolInfoBean.getSymbol());
                intent.putExtra(AppConstant.STAR_HEAD_URL, symbolInfoBean.getPic());
                startActivity(intent);
                break;
            case R.id.tv_ask_to_buy:
                Intent intent2 = new Intent(this, BuyTransferIndentActivity.class);
                intent2.putExtra(AppConstant.BUY_TRANSFER_INTENT_TYPE, 1);
                intent2.putExtra(AppConstant.STAR_WID, symbolInfoBean.getWid());
                intent2.putExtra(AppConstant.STAR_NAME, symbolInfoBean.getName());
                intent2.putExtra(AppConstant.STAR_CODE, symbolInfoBean.getSymbol());
                intent2.putExtra(AppConstant.STAR_HEAD_URL, symbolInfoBean.getPic());
                startActivity(intent2);
                break;
        }
    }


    private static class MyHandler extends Handler {
        final private static int GRT_DATA = 111;
        private final WeakReference<StarTimeDealActivity> mActivity;

        public MyHandler(StarTimeDealActivity dealActivity) {
            mActivity = new WeakReference<StarTimeDealActivity>(dealActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            StarTimeDealActivity activity = mActivity.get();
            if (activity != null && activity.isFinishing() == false) {
                switch (msg.what) {
                    case GRT_DATA:
                        activity.refreshAnim();
                        break;
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRefresh();
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startRefresh();
        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            mDanmakuView.resume();
        }
    }

    private BaseDanmakuParser createParser(InputStream stream) {

        if (stream == null) {
            return new BaseDanmakuParser() {

                @Override
                protected Danmakus parse() {
                    return new Danmakus();
                }
            };
        }

        ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);

        try {
            loader.load(stream);
        } catch (IllegalDataException e) {
            e.printStackTrace();
        }
        BaseDanmakuParser parser = new BiliDanmukuParser();
        IDataSource<?> dataSource = loader.getDataSource();
        parser.load(dataSource);
        return parser;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDanmakuView != null) {
            // dont forget release!
            mDanmakuView.release();
            mDanmakuView = null;
        }
    }

    //这两个用来控制两行弹幕之间的间距
    private int BITMAP_WIDTH = 40;//头像的大小
    private int BITMAP_HEIGHT = 40;
    private float DANMU_TEXT_SIZE = 14f;//弹幕字体的大小
    private int DANMU_PADDING = 10;
    private int DANMU_PADDING_INNER = 7;
    private int DANMU_RADIUS = 11;//圆角半径

    /**
     * 绘制背景(自定义弹幕样式)
     */
    private class BackgroundCacheStuffer extends SpannedCacheStuffer {
        // 通过扩展SimpleTextCacheStuffer或SpannedCacheStuffer个性化你的弹幕样式
        Paint paint = new Paint();
        int[] colors = {0xFFE69A17, 0xFFFF3052, 0xFF4FBEDC, 0xFFAA3FBD};

        @Override
        public void measure(BaseDanmaku danmaku, TextPaint paint, boolean fromWorkerThread) {
//            danmaku.padding = 20;  // 在背景绘制模式下增加padding
            super.measure(danmaku, paint, fromWorkerThread);
        }

        @Override
        public void drawBackground(BaseDanmaku danmaku, Canvas canvas, float left, float top) {
            paint.setAntiAlias(true);
            int i = new Random().nextInt(4);
            LinearGradient mLinearGradient = new LinearGradient(0, 0, danmaku.paintWidth, 0, colors[i], 0x00fafafa, Shader.TileMode.CLAMP);
            //new LinearGradient(float x0, float y0, float x1, float y1, int color0, int color1, TileMode tile)
            paint.setShader(mLinearGradient);
            LogUtil.e("danmaku" + left + "");
            canvas.drawRoundRect(new RectF(BITMAP_WIDTH / 2 + left + DANMU_PADDING_INNER, top + DANMU_PADDING_INNER
                            , left + danmaku.paintWidth - DANMU_PADDING_INNER + 6 + BITMAP_WIDTH / 2,
                            top + danmaku.paintHeight - DANMU_PADDING_INNER + 6),//+6 主要是底部被截得太厉害了，+6是增加padding的效果
                    0, 0, paint);
        }

        @Override
        public void drawStroke(BaseDanmaku danmaku, String lineText, Canvas canvas, float left, float top, Paint paint) {
            // 禁用描边绘制
        }

    }

    private BaseCacheStuffer.Proxy mCacheStufferAdapter = new BaseCacheStuffer.Proxy() {

        @Override
        public void prepareDrawing(final BaseDanmaku danmaku, boolean fromWorkerThread) {
//            if (danmaku.text instanceof Spanned) { // 根据你的条件检查是否需要需要更新弹幕
//            }
        }

        @Override
        public void releaseResource(BaseDanmaku danmaku) {
            // TODO 重要:清理含有ImageSpan的text中的一些占用内存的资源 例如drawable
            if (danmaku.text instanceof Spanned) {
                danmaku.text = "";
            }
        }
    };

    /**
     * 对数值进行转换，适配手机，必须在初始化之前，否则有些数据不会起作用
     */
    private void setSize() {
        BITMAP_WIDTH = DisplayUtil.dp2pxConvertInt(BITMAP_HEIGHT);
        BITMAP_HEIGHT = DisplayUtil.dp2pxConvertInt(BITMAP_HEIGHT);
//        EMOJI_SIZE = DpOrSp2PxUtil.dp2pxConvertInt(context, EMOJI_SIZE);
        DANMU_PADDING = DisplayUtil.dp2pxConvertInt(DANMU_PADDING);
        DANMU_PADDING_INNER = DisplayUtil.dp2pxConvertInt(DANMU_PADDING_INNER);
        DANMU_RADIUS = DisplayUtil.dp2pxConvertInt(DANMU_RADIUS);
        DANMU_TEXT_SIZE = DisplayUtil.sp2px(DANMU_TEXT_SIZE);
    }

    private int secondTime = 0;
    private boolean startSunTime = false;

    private void initTradingStatus(final boolean sendHandler) {
        NetworkAPIFactoryImpl.getInformationAPI().getTradingStatus(SharePrefUtil.getInstance().getUserId(), SharePrefUtil.getInstance().getToken(), symbolInfoBean.getSymbol(), new OnAPIListener<TradingStatusBeen>() {
            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onSuccess(TradingStatusBeen tradingStatusBeen) {
                if (tradingStatusBeen != null) {
                    if (tradingStatusBeen.isStatus()) {
                        startSunTime = true;
                        secondTime = tradingStatusBeen.getRemainingTime();
                        if (myHandler != null) {
                            myHandler.removeCallbacksAndMessages(null);
                            myHandler.sendEmptyMessage(myHandler.GRT_DATA);
                        }
                        //startTime(tradingStatusBeen.getRemainingTime());
                    } else {
                        tv_time.setText("未开始");
                        if (sendHandler && myHandler != null) {
                            myHandler.removeCallbacksAndMessages(null);
                            myHandler.sendEmptyMessage(myHandler.GRT_DATA);
                        }
                    }
                }
                LogUtils.loge(tradingStatusBeen.toString());
            }
        });
    }


    private void stopRefresh() {
        if (myHandler != null) {
            startSunTime = false;
            myHandler.removeCallbacksAndMessages(null);
            myHandler = null;
            LogUtils.loge("停止刷新stopRefresh");
        }
    }

    private void startRefresh() {
        if (myHandler != null) {
            LogUtils.loge("刷新startRefresh");
            myHandler.removeCallbacksAndMessages(null);
            initTradingStatus(false);
        } else {
            myHandler = new MyHandler(this);
            myHandler.removeCallbacksAndMessages(null);
            initTradingStatus(false);
        }
    }
}
