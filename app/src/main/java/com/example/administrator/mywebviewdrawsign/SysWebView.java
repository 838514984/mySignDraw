package com.example.administrator.mywebviewdrawsign;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Stack;

/**
 * Created by Administrator on 2017/4/10 0010.
 */

public class SysWebView extends WebView {

    public static final String TAG = "KavaWebView";

    public WebViewClient viewClient;

    //This is for the polyfill history
    private final Stack<String> urls = new Stack<String>();

    public boolean useBrowserHistory = false;

    private final boolean handleButton = false;

    /** custom view created by the browser (a video player for example) */
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    public boolean doClearHistoryForOneTime = false;
    public boolean disableBackspace = false;

    private final Context mContext;

    class ActivityResult {

        int request;
        int result;
        Intent incoming;

        public ActivityResult(int req, int res, Intent intent) {
            request = req;
            result = res;
            incoming = intent;
        }


    }

    static final FrameLayout.LayoutParams COVER_SCREEN_GRAVITY_CENTER =
            new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER);

    /**
     * Constructor.
     *
     * @param context
     */
    public SysWebView(Context context) {
        super(context);
        mContext = context;
        this.setup();
    }

    public SysWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        this.setup();
    }

    public SysWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        this.setup();
    }

    /**
     * Initialize webview.
     */
    @SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
    private void setup() {
        this.setInitialScale(0);
        this.setVerticalScrollBarEnabled(true);
        this.setHorizontalScrollBarEnabled(true);
        this.requestFocusFromTouch();
        // Enable JavaScript
        WebSettings settings = this.getSettings();

        settings.setBuiltInZoomControls(false);// 隐藏缩放按钮
        settings.setUseWideViewPort(false);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setAllowFileAccess(true);
        settings.setAppCacheMaxSize(1024*1024*32);
        settings.setAppCachePath(mContext.getFilesDir().getPath()+"/cache");
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // Set Cache Mode: LOAD_NO_CACHE is noly for debug
        //settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //enablePageCache(settings,5);
        //enableWorkers(settings);

        // Enable database
        settings.setDatabaseEnabled(true);
        String databasePath = mContext.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setDatabasePath(databasePath);

        // Enable DOM storage
        settings.setDomStorageEnabled(true);

        // Enable built-in geolocation
        settings.setGeolocationEnabled(true);

        // Improve render performance
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        if (Build.VERSION.SDK_INT >= 21) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    private void enablePageCache(WebSettings settings, int size) {
        Class<? extends WebSettings> websettings=settings.getClass();
        Field mPageCacheCapacity;
        try {
            mPageCacheCapacity = websettings.getDeclaredField("mPageCacheCapacity");
            mPageCacheCapacity.setAccessible(true);
            mPageCacheCapacity.setInt(settings, size);
        } catch (Exception e) {
            LogUtils.e("enablePageCache failed.");
        }
    }

    private void enableWorkers(WebSettings settings)
    {
        Class<? extends WebSettings> websettings=settings.getClass();
        Method setWorkersEnabled;
        try {
            setWorkersEnabled = websettings.getDeclaredMethod("setWorkersEnabled", boolean.class);
            setWorkersEnabled.setAccessible(true);
            setWorkersEnabled.invoke(settings, true);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("enableWorkers failed.");
        }
    }

    /**
     * Set the WebViewClient.
     *
     * @param client
     */
    public void setWebViewClient(WebViewClient client) {
        this.viewClient = client;
        super.setWebViewClient(client);
    }

    /**
     * Set the WebChromeClient.
     *
     * @param //client
     */
    /*public void setWebChromeClient(SysChromeClient client) {
        super.setWebChromeClient(client);
    }*/

    /*public void injectJavascript(String assetFilename) {
        String result=null;
        AssetManager am = mContext.getAssets();
        try {
            InputStream inputStream = am.open(assetFilename);
            int lenght = inputStream.available();
            byte[]  buffer = new byte[lenght];
            inputStream.read(buffer);
            result = EncodingUtils.getString(buffer, "utf-8");
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!result.isEmpty()){
            //TODO.addCustomJavascript(result);
        }else{
            LogUtils.e("requested javascript file is not existed!");
        }
    }*/

    @Override
    public void loadUrl(String url) {
        if (url.startsWith("file://") || url.startsWith("http://") || url.startsWith("https://") ||
                url.startsWith("javascript:") || url.startsWith("about:")) {
            super.loadUrl(url);
        }
    }

    /**
     * Returns the top url on the stack without removing it from
     * the stack.
     */
    public String peekAtUrlStack() {
        if (this.urls.size() > 0) {
            return this.urls.peek();
        }
        return "";
    }

    /**
     * Add a url to the stack
     *
     * @param url
     */
    public void pushUrl(String url) {
        this.urls.push(url);
    }

    /**
     * Go to previous page in history.  (We manage our own history)
     *
     * @return true if we went back, false if we are already at top
     */
    public boolean backHistory() {

        // Check webview first to see if there is a history
        // This is needed to support curPage#diffLink, since they are added to appView's history, but not our history url array (JQMobile behavior)
        if (super.canGoBack()) {
            super.goBack();
            return true;
        }

        // If our managed history has prev url
        if (this.urls.size() > 1 && !this.useBrowserHistory) {
            this.urls.pop();                // Pop current url
            String url = this.urls.peek();   // Pop prev url that we want to load, since it will be added back by loadUrl()

            this.loadUrl(url);
            return true;
        }

        return false;
    }

    /**
     * Return true if there is a history item.
     *
     * @return
     */
    @Override
    public boolean canGoBack() {
        if (super.canGoBack()) {
            return true;
        }
        if (this.urls.size() > 1) {
            return true;
        }
        return false;
    }

    /**
     * Load the specified URL in the Cordova webview or a new browser instance.
     *
     * NOTE: If openExternal is false, only URLs listed in whitelist can be loaded.
     *
     * @param url           The url to load.
     * @param openExternal  Load url in browser instead of Cordova webview.
     * @param clearHistory  Clear the history stack, so new page becomes top of history
     * @param params        DroidGap parameters for new app
     */
    public void showWebPage(String url, boolean openExternal, boolean clearHistory, HashMap<String, Object> params) {
        LogUtils.d(String.format("showWebPage(%s, %b, %b, HashMap", url, openExternal, clearHistory));

        // If clearing history
        if (clearHistory) {
            this.clearHistory();
        }

        // If loading into our webview
        if (!openExternal) {

            // Make sure url is in whitelist
            if (url.startsWith("file://") || url.startsWith("http://") || url.startsWith("https://")) {
                // TODO: What about params?

                // Clear out current url from history, since it will be replacing it
                if (clearHistory) {
                    this.urls.clear();
                }

                // Load new URL
                this.loadUrl(url);
            }
            // Load in default viewer if not
            else {
                LogUtils.w("showWebPage: Cannot load URL into webview since it is not in white list.  Loading into browser instead. (URL=" + url + ")");
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    mContext.startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                   // LogUtils.e("Error loading url " + url, e);
                }
            }
        }
    }


    public void handlePause(boolean keepRunning)
    {
        // If app doesn't want to run in background
        if (!keepRunning) {
            // Pause JavaScript timers (including setInterval)
            this.pauseTimers();
        }
    }

    public void handleResume(boolean keepRunning, boolean activityResultKeepRunning)
    {
        // Resume JavaScript timers (including setInterval)
        this.resumeTimers();
    }

    public void handleDestroy()
    {
        // Load blank page so that JavaScript onunload is called
        this.loadUrl("about:blank");
    }

    public boolean hadKeyEvent() {
        return handleButton;
    }

    //Can Go Back is BROKEN!
    public boolean startOfHistory()
    {
        WebBackForwardList currentList = this.copyBackForwardList();
        WebHistoryItem item = currentList.getItemAtIndex(0);
        String url = item.getUrl();
        String currentUrl = this.getUrl();
        LogUtils.d("The current URL is: " + currentUrl);
        LogUtils.d("The URL at item 0 is:" + url);
        return currentUrl.equals(url);
    }

    private Drawable background;
    private Object[] objects;// 播放页面的其他控件
    public void setOtherWidgets(Object ...objects) {
        this.objects = objects;
    }
    public void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        // This code is adapted from the original Android Browser code, licensed under the Apache License, Version 2.0
        LogUtils.d("showing Custom View");
        // if a view already exists then immediately terminate the new one
        if (mCustomView != null) {
            callback.onCustomViewHidden();
            return;
        }

        // Store the view and its callback for later (to kill it properly)
        mCustomView = view;
        mCustomViewCallback = callback;

        // Add the custom view to its container.
        ViewGroup parent = (ViewGroup) this.getParent();
        background = parent.getBackground();
        parent.setBackground(new ColorDrawable(Color.BLACK));
        parent.addView(view, COVER_SCREEN_GRAVITY_CENTER);

        // Hide the content view.
        this.setVisibility(View.GONE);

        // Finally show the custom view container.
        parent.setVisibility(View.VISIBLE);
        parent.bringToFront();
        if (objects != null && objects.length > 0) {
            for (Object obj : objects) {
                if (obj instanceof View) {
                    ((View) obj).setVisibility(View.GONE);
                }else if (obj instanceof Fragment) {
                    ((Fragment) obj).getView().setVisibility(View.GONE);
                }
            }
        }
        ((Activity)mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ((Activity)mContext).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void hideCustomView() {
        // This code is adapted from the original Android Browser code, licensed under the Apache License, Version 2.0
        LogUtils.d("Hidding Custom View");
        if (mCustomView == null) {
            return;
        }

        // Hide the custom view.
        mCustomView.setVisibility(View.GONE);

        // Remove the custom view from its container.
        ViewGroup parent = (ViewGroup) this.getParent();
        if (background != null) {
            parent.setBackground(background);
        }
        parent.removeView(mCustomView);
        mCustomView = null;
        mCustomViewCallback.onCustomViewHidden();

        // Show the content view.
        this.setVisibility(View.VISIBLE);
        if (objects != null && objects.length > 0) {
            for (Object obj : objects) {
                if (obj instanceof View) {
                    ((View) obj).setVisibility(View.VISIBLE);
                }else if (obj instanceof Fragment) {
                    ((Fragment) obj).getView().setVisibility(View.VISIBLE);
                }
            }
        }
        ((Activity)mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        final WindowManager.LayoutParams attrs = ((Activity)mContext).getWindow().getAttributes();
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ((Activity)mContext).getWindow().setAttributes(attrs);
        ((Activity)mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    /**
     * if the video overlay is showing then we need to know
     * as it effects back button handling
     *
     * @return
     */
    public boolean isCustomViewShowing() {
        return mCustomView != null;
    }

    public void evaluatingJavaScript(String jscode){
        Method stringByEvaluatingJavaScriptFromString = null;
        Object webViewCore = null;
        Object browserFrame = null;
        Object webViewObject = null;
        Class<WebView> webViewClass = WebView.class;
        try {
            Field provider = webViewClass.getDeclaredField("mProvider");
            provider.setAccessible(true);
            webViewObject = provider.get(this);
            Field wc = webViewObject.getClass().getDeclaredField("mWebViewCore");
            wc.setAccessible(true);
            webViewCore = wc.get(webViewObject);
            if (webViewCore != null) {
                Field bf= webViewCore.getClass().getDeclaredField("mBrowserFrame");
                bf.setAccessible(true);
                browserFrame = bf.get(webViewCore);
                stringByEvaluatingJavaScriptFromString = browserFrame.getClass().getDeclaredMethod("stringByEvaluatingJavaScriptFromString", String.class);
                stringByEvaluatingJavaScriptFromString.setAccessible(true);
                stringByEvaluatingJavaScriptFromString.invoke(browserFrame, jscode);
            }
        } catch (Exception e) {
            LogUtils.e("evaluatingJavaScript failed.");
        }
    }
    /**************webview的滑动监听********************************************************/
    /**************add by songlei********************************************************/
    public ScrollInterface mScrollInterface;

    /**
     * webview的滑动监听
     * @param l
     * @param t
     * @param oldl
     * @param oldt
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {

        super.onScrollChanged(l, t, oldl, oldt);

        if (mScrollInterface!=null) {
            mScrollInterface.onSChanged(l, t, oldl, oldt);
        }

    }

    public void setOnCustomScroolChangeListener(ScrollInterface scrollInterface) {

        this.mScrollInterface = scrollInterface;

    }

    public interface ScrollInterface {

        public void onSChanged(int l, int t, int oldl, int oldt);

    }
    /**************webview的滑动监听********************************************************/
}