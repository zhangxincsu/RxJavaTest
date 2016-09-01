package com.zx.rxjavatest;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.content.pm.PackageManager.*;
import static rx.Observable.from;

public class MainActivity extends AppCompatActivity {

    private TextView cacheCleanView;
    private Button cacheCleanOnekeyCleanBt;
    private TextView cacheCleanDetail;
    private ScrollView cacheScrollview;
    private GridLayout mAppLayout;

    private PackageManager pkgManager;
    private Method getPackageSizeInfo;
    private PkgSizeObserver mPkgSizeObserver;
    private ArrayList<AppInfo> mCacheInfoList;
    private ArrayList<String> mDelAppInfoList;
    private int tempCount, mTotalcount = 0;
    private long tempSize, mTotalCacheSize = 0;
    private Context mContext;

    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        initData();
        startWork();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
    }

    private void startWork() {
        Subscriber<AppInfo> subscriber = new Subscriber<AppInfo>() {
            @Override
            public void onCompleted() {
                cacheCleanView.setText("扫描完成");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(AppInfo appInfo) {
            }
        };
        subscription = Observable.from(getCacheInfos())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        cacheCleanOnekeyCleanBt.setVisibility(View.VISIBLE);
                        cacheCleanDetail.setVisibility(View.VISIBLE);
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }


    private void setViewAndEvent(final AppInfo info) {
        FrameLayout layout = new FrameLayout(this);
        FrameLayout.LayoutParams ll = new FrameLayout.LayoutParams(Global.dip2px(mContext, 278), Global.dip2px(mContext, 169));
        GridLayout.LayoutParams gl = new GridLayout.LayoutParams(ll);
        layout.setPadding(Global.dip2px(mContext, 19), Global.dip2px(mContext, 16), Global.dip2px(mContext, 19), Global.dip2px(mContext, 16));
        layout.setClipToPadding(false);
        layout.setLayoutParams(gl);

        FrameLayout.LayoutParams tvll = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        final TextView txtView = new TextView(this);
        txtView.setText(info.appName);
        txtView.setTextColor(0xffe9e9e9);
        txtView.setTextSize(23);
        txtView.setSingleLine();
        txtView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        txtView.setGravity(Gravity.CENTER_HORIZONTAL);
        txtView.setLayoutParams(new GridLayout.LayoutParams(tvll));
        Drawable drawable = info.appIcon;
        drawable.setBounds(0, 0, Global.dip2px(mContext, 86), Global.dip2px(mContext, 86));
        txtView.setPadding(Global.dip2px(mContext, 40), Global.dip2px(mContext, 13), Global.dip2px(mContext, 40), Global.dip2px(mContext, 11));
        txtView.setCompoundDrawables(null, drawable, null, null);
        txtView.setBackgroundColor(0x20ffffff);
        layout.addView(txtView);

        final TextView txtDetailView = new TextView(this);
        txtDetailView.setText(info.appName);
        txtDetailView.setTextColor(0xffe9e9e9);
        txtDetailView.setTextSize(20);

        txtDetailView.setText("程序：" + Global.formetFileSize(info.codeSize)
                + "\n数据：" + Global.formetFileSize(info.dataSize) +
                ((info.cacheSize==0) ?"\n缓存：12.00KB":("\n缓存：" + Global.formetFileSize(info.cacheSize))));

        txtDetailView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        txtDetailView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        txtDetailView.setPadding(Global.dip2px(mContext, 50), 0, 0, 0);
        txtDetailView.setBackgroundColor(0xb0000000);
        txtDetailView.setLayoutParams(new GridLayout.LayoutParams(tvll));
        txtDetailView.setVisibility(View.INVISIBLE);
        layout.addView(txtDetailView);

        layout.setFocusable(true);
        layout.setFocusableInTouchMode(true);
        layout.setClickable(true);

        layout.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
//                    view.setTag(R.id.track_view_scale_x, 0.865f);
//                    view.setTag(R.id.track_view_scale_y, 0.816f);
                    txtDetailView.setVisibility(View.VISIBLE);
                    txtDetailView.animate().translationY(0).setDuration(300);
                    txtDetailView.animate().alpha(1.0f).setDuration(300);
                    view.requestLayout();
                } else {
                    txtDetailView.animate().translationY(Global.dip2px(mContext, 169)).setDuration(300);
                    txtDetailView.animate().alpha(0).setDuration(300);
                }
            }
        });

//        layout.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mDelAppInfoList.clear();
//                mDelAppInfoList.add(info.pkgName);
//                myThreadPool.submit(mCacheRunnable);
//                mTotalCacheSize -= info.cacheSize;
//                mAppLayout.removeView(view);
//                mCacheInfoList.remove(info);
//                mTotalcount--;
//                updateTips();
//            }
//        });

        mAppLayout.addView(layout);
        mAppLayout.invalidate();
    }

    private void initData() {
        mContext = getApplicationContext();
        mCacheInfoList = new ArrayList<>();
        pkgManager = getPackageManager();
    }

    private void findViews() {
        cacheCleanView = (TextView) findViewById(R.id.cache_clean_view);
        cacheCleanOnekeyCleanBt = (Button) findViewById(R.id.cache_clean_onekey_clean_bt);
        cacheCleanDetail = (TextView) findViewById(R.id.cache_clean_detail);
        cacheScrollview = (ScrollView) findViewById(R.id.cache_scrollview);
        mAppLayout = (GridLayout) findViewById(R.id.cache_list_container);
        cacheCleanView.setText("正在扫描...");

        cacheCleanOnekeyCleanBt.setVisibility(View.GONE);
        cacheCleanDetail.setVisibility(View.GONE);
        cacheScrollview.setVisibility(View.GONE);
        RxView.clicks(cacheCleanDetail)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        cacheScrollview.setVisibility(View.VISIBLE);
                        cacheCleanView.setVisibility(View.GONE);
                        cacheCleanDetail.setVisibility(View.GONE);
                        cacheCleanOnekeyCleanBt.setVisibility(View.GONE);
                        Observable.from(mCacheInfoList)
                                .subscribe(new Action1<AppInfo>() {
                                    @Override
                                    public void call(AppInfo appInfo) {
                                        setViewAndEvent(appInfo);
                                    }
                                });
                    }
                });
        RxView.clicks(cacheCleanOnekeyCleanBt)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        Observable.from(mCacheInfoList)
                                .subscribe(new Action1<AppInfo>() {
                                    @Override
                                    public void call(AppInfo appInfo) {
                                        cleanCache(appInfo.pkgName);
                                    }
                                });
                    }
                });
    }

    public void cleanCache(final String packageName) {
        File path = null;
        path = new File("/data/data/"+ packageName + "/cache");
        if (null != path) {
            Log.v("XIN", packageName + "\t被清理");
            // #rm -r xxx 删除名字为xxx的文件夹及其里面的所有文件
//            Command.doSuperCommand(Command.RM + path.toString());
        }

    }

    private ArrayList<AppInfo> getCacheInfos() {
        mCacheInfoList.clear();
        List<ApplicationInfo> applicationInfos = pkgManager.getInstalledApplications(GET_UNINSTALLED_PACKAGES);
        for(ApplicationInfo applicationInfo: applicationInfos) {
            AppInfo info = new AppInfo();
            info.appIcon = applicationInfo.loadIcon(pkgManager);
            info.appName = applicationInfo.loadLabel(pkgManager).toString();
            info.pkgName = applicationInfo.packageName;
            try {
                queryPacakgeSize(info);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mCacheInfoList;
    }

    public void  queryPacakgeSize(AppInfo info) throws Exception{
        if ( info.pkgName != null){
            try {
                if(null == getPackageSizeInfo) {
                    getPackageSizeInfo = PackageManager.class.getMethod(
                            "getPackageSizeInfo", new Class[] { String.class,
                                    IPackageStatsObserver.class });
                }
                if(null == mPkgSizeObserver ) {
                    mPkgSizeObserver = new PkgSizeObserver(info);
                } else {
                    mPkgSizeObserver.setAppInfo(info);
                }
                getPackageSizeInfo.invoke(pkgManager, info.pkgName, mPkgSizeObserver);
            }
            catch(Exception ex){
                ex.printStackTrace() ;
                throw ex ;  // 抛出异常
            }
        }
    }

    private boolean traversalList(String pkgName) {
        if(mCacheInfoList.size()==0){
            return false;
        }
        for(AppInfo temp: mCacheInfoList) {
            if(pkgName.equals(temp.pkgName))
                return true;
        }
        return false;
    }

    // aidl文件形成的Bindler机制服务类
    class PkgSizeObserver extends IPackageStatsObserver.Stub {
        AppInfo info;
        public PkgSizeObserver(AppInfo info) {
            super();
            this.info = info;
        }

        public void setAppInfo(AppInfo info) {
            this.info = info;
        }

        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                throws RemoteException {
            if(traversalList(info.pkgName)) {
                return;
            }
            info.cacheSize = pStats.cacheSize; // 缓存大小
            info.dataSize = pStats.dataSize; // 数据大小
            info.codeSize = pStats.codeSize; // 应用程序大小

            boolean isCacheExist = false;
            File path = null;
            path = new File("/data/data/"+ info.pkgName + "/cache");
            if(path.exists()){
                isCacheExist = true;
            }else{
                isCacheExist = false;
            }
            if(info.cacheSize > 1024 && isCacheExist ){
                mCacheInfoList.add(info);
                mTotalcount++;
                mTotalCacheSize += pStats.cacheSize;
            }
        }
    }

}
