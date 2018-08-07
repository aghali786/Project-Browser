package browser.projektb.com.rikoshae;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.os.Bundle;

import com.aerserv.sdk.AerServConfig;
import com.aerserv.sdk.*;
import com.loopj.android.http.*;

import android.app.Activity;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.net.Uri;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import browser.projektb.com.rikoshae.adp.BookmarkAdapter;
import browser.projektb.com.rikoshae.adp.HistoryAdapter;
import browser.projektb.com.rikoshae.db.ProjectBDB;
import browser.projektb.com.rikoshae.model.HistoryModel;
import browser.projektb.com.rikoshae.util.BookmarkPopUp;
import browser.projektb.com.rikoshae.util.CustomDialog;
import browser.projektb.com.rikoshae.util.DialogButtonListener;
import browser.projektb.com.rikoshae.util.LoadingDialog;
import browser.projektb.com.rikoshae.util.Util;
import cz.msebera.android.httpclient.Header;

public class BrowserApp extends Activity {

    boolean loadingFinished = true;
    boolean redirect = false;
    int pagecounter = 0;
    int arrayActiveIndex = 0;
    public int arrayBannerActiveIndex = 0;
    String combine;
    String deviceId;
    boolean bannerAdsLoopActive = false;
    boolean activeUser = false;

    private WebView webView;

    Context mContext = BrowserApp.this;
    SharedPreferences appPreferences;
    boolean isAppInstalled = false;
    AdArrayClass[] something_else;
    JSONArray something;
    String deviceIMEI;
    String deviceUID;
    LinearLayout tabLayout;
    String title = "";
    TextView selectedTabTextView,recommendationsPlaceholder;
    View selectedParentView;
    HorizontalScrollView horizontalScrollBarView;
    LinearLayout historyLinearLayout;
    int tabIndex = 0;
    int historyTabIndex = 0;
    int bookmarkTabIndex = 0;
    int aerservfailcount = 0;
    ListView historyListView;
    ArrayList<HistoryModel> historyList = new ArrayList<>();
    Button clearBrowsingDataButton, removeSelectedItemButton,deviceoffline,updateAppButton,registerdevice;
    ArrayList<HistoryModel> selectedHistoryList = new ArrayList<>();
    HistoryAdapter histAdapter;
    BookmarkAdapter bookAdapter;
    ArrayList<HistoryModel> bookmarkList = new ArrayList<>();
    String loadedUrl = "";
    EditText urlString;
    LinearLayout loadWebViewLinearLayout;
    ImageButton bookmarkAddButton;
    HashMap<View, WebView> webViewList = new HashMap<>();
    LoadingDialog p;
    AerServBanner banner;
    ImageView bannerAdImgViewVar1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser_app);
        p = new LoadingDialog(this);
        // webView = (WebView) findViewById(R.id.webView);
        loadWebViewLinearLayout = (LinearLayout) findViewById(R.id.loadWebViewLinearLayout);
        horizontalScrollBarView = (HorizontalScrollView) findViewById(R.id.horizontalScrollBarView);
        tabLayout = (LinearLayout) findViewById(R.id.tabLayout);
        urlString = (EditText) findViewById(R.id.urlText);
        bookmarkAddButton = (ImageButton) findViewById(R.id.bookmarkAddButton);
        historyLinearLayout = (LinearLayout) findViewById(R.id.historyLinearLayout);
        recommendationsPlaceholder = (TextView) findViewById(R.id.recommendationsPlaceholder);
        deviceoffline = (Button) findViewById(R.id.deviceoffline);
        getBannerAdArray();


        // for added new tab by default
        addTab("New Tab");

        // For added new tab when we click on add button
        ((ImageButton) findViewById(R.id.add_tab)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                historyLinearLayout.setVisibility(View.GONE);
                tabIndex++;
                addTab("New Tab");
                //  addTab();
            }
        });

        AerServEventListener listener = new AerServEventListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onAerServEvent(AerServEvent event, List params) {
                switch (event) {
                    case AD_LOADED:
                        // Ad loaded
                        Log.d("AerServ", "** - ad loaded success- **|| aerserv event = "+event);
                        aerserSuccFn();
                        //recommendationsPlaceholder.setVisibility(View.GONE);
//                        recommendationsPlaceholder
                        break;
                    case AD_FAILED:
                        // Ad failed
                        Log.d("AerServ", "** - ad loaded failed - ** || aerserv event = "+event);
                        aerserfailfn();
                        break;
                }
            }
        };

        AerServConfig config = new AerServConfig(BrowserApp.this, "1017548").setEventListener(listener);
        banner = (AerServBanner) findViewById(R.id.banner);
        banner.configure(config).show();
        banner.play();

        boolean online = isOnline(getApplicationContext());
        Log.i("Rikoshae", " CREATE :: -||- :: ANDROID || online = "+online);

        if (online) {

            Log.i("Rikoshae", " device online ");
            getUpdateStatus();
            getIMEInUID();
//            checkAppUpdateStatus();
//            checkAppLockscreenUpdateStatus();

        }
        else {

            Log.i("Rikoshae", " device offline ");
            banner.pause();
            recommendationsPlaceholder.setVisibility(View.GONE);
            deviceoffline.setVisibility(View.VISIBLE);
//            mainbannerad.setVisibility(View.GONE);
//            mulaahBannerAdImg.setVisibility(View.GONE);
//            registerdevice.setVisibility(View.VISIBLE);
//            registerdevice.setText("This device is currently offline.");
            activeUser = false;

        }

//        createShortCut();
//        getIMEI();
//        getUserInformation();
//        getFullAdArray();
//        getBannerAdArray();

        // Enable Javascript
        //WebSettings webSettings = webView.getSettings();
        // webSettings.setJavaScriptEnabled(true);

        registerdevice = (Button) findViewById(R.id.registerdevice);
        registerdevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setPackage("com.technilyze.mulauncher");
                PackageManager pm = getApplicationContext().getPackageManager();
                List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
                Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));

                if (resolveInfos.size() > 0) {
                    ResolveInfo launchable = resolveInfos.get(0);
                    ActivityInfo activity = launchable.activityInfo;
                    ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                            activity.name);
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.putExtra("url", "register");

                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    i.setComponent(name);

                    getApplicationContext().startActivity(i);
                }

            }
        });


        updateAppButton = (Button) findViewById(R.id.updateAppButton);
        updateAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateAppFn();

            }
        });

        ImageButton closeBlowUpAdImgBtn = (ImageButton) findViewById(R.id.closeLargeAdView);
        android.view.ViewGroup.LayoutParams mParams = closeBlowUpAdImgBtn.getLayoutParams();
        mParams.height = closeBlowUpAdImgBtn.getWidth();
        closeBlowUpAdImgBtn.setLayoutParams(mParams);

        closeBlowUpAdImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                closeLargeAdView();

            }
        });

        bannerAdImgViewVar1 = (ImageView) findViewById(R.id.bannerImgView);
        bannerAdImgViewVar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bannerAdClickedFn();

            }
        });

        ImageView blowUpAdImgViewVar1 = (ImageView) findViewById(R.id.blowUpAdImgView);
        blowUpAdImgViewVar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                blowUpAdClickedFn();

            }
        });

        Button backButtonImgBtn = (Button) findViewById(R.id.backTextButton);
        backButtonImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("debug", "** - backButtonImgBtn - **");
                WebView webView = webViewList.get(selectedParentView);//(WebView) findViewById(R.id.webView);
                if (webView != null) {
                    selectedTabTextView.setText(webView.getTitle());
                    webView.goBack();
                }

            }
        });

        ImageButton refreshButtonVar = (ImageButton) findViewById(R.id.refreshImgButton);
        refreshButtonVar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Log.d("debug", "** - refreshButtonVar - **");
                Toast.makeText(BrowserApp.this, "REFRESH", Toast.LENGTH_SHORT).show();
                WebView webView = webViewList.get(selectedParentView);//(WebView) findViewById(R.id.webView);
                if (webView != null) {
                    selectedTabTextView.setText(webView.getTitle());
                    webView.reload();
                }
            }
        });

        // We can see all history list
        ImageButton historyTextButton = (ImageButton) findViewById(R.id.historyTextButton);
        historyTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyList.clear();
                historyList.addAll(ProjectBDB.get_instance(BrowserApp.this).getSearchedHistory());
                if (historyList.size() > 0) {
                    historyLinearLayout.setVisibility(View.VISIBLE);
                    addTab("History");
                    setHistoryAndBookmarkLayout("history");
                } else {
                    Toast.makeText(BrowserApp.this, "History not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // We can see all bookmarked list
        ImageButton bookmarkListButton = (ImageButton) findViewById(R.id.bookmarkListButton);
        bookmarkListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookmarkList.clear();
                bookmarkList.addAll(ProjectBDB.get_instance(BrowserApp.this).getBookmarkData());
                if (bookmarkList.size() > 0) {
                    historyLinearLayout.setVisibility(View.VISIBLE);
                    addTab("Bookmark");
                    setHistoryAndBookmarkLayout("bookmark");
                } else {
                    Toast.makeText(BrowserApp.this, "Bookmark not available", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // For add bookmark url
        bookmarkAddButton.setClickable(false);
        bookmarkAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!title.equalsIgnoreCase("") && !loadedUrl.equalsIgnoreCase("")) {
                    new BookmarkPopUp(BrowserApp.this, title, loadedUrl, new DialogButtonListener() {
                        @Override
                        public void onButtonClicked(String text) {
                            if (text.equalsIgnoreCase("done")) {
                                ProjectBDB.get_instance(BrowserApp.this).insertBookmarkUrl(title, loadedUrl);
                            }
                        }
                    }).show();
                }
            }
        });
        /*webView.setWebViewClient(new MyAppWebViewClient() {

            public void onPageStarted(WebView view, String url) {
                loadingFinished = false;
                //SHOW LOADING IF IT ISNT ALREADY VISIBLE
//                ProgressDialog dialog = ProgressDialog.show(BrowserApp.this, "",
//                        "Loading. Please wait...", true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                if (urlString != null)
                    urlString.setText(url);
                pagecounter = pagecounter + 1;

                title = view.getTitle();
                // if (selectedTabTextView != null)
                //    selectedTabTextView.setText(title);
                if (!redirect) {
                    loadingFinished = true;
                }

                if (loadingFinished && !redirect) {
                    //HIDE LOADING IT HAS FINISHED
                } else {
                    redirect = false;
                }

                if (pagecounter == 4) {

                    ImageView blowUpAdImgViewVar = (ImageView) findViewById(R.id.blowUpAdImgView);
                    blowUpAdImgViewVar.setVisibility(View.VISIBLE);

                    ImageButton blowUpAdImgCloseViewVar = (ImageButton) findViewById(R.id.closeLargeAdView);
                    blowUpAdImgCloseViewVar.setVisibility(View.VISIBLE);

                    pagecounter = 0;

                    if (jsonArray != null)
                        getNewFullPageAd();

                }
                HistoryModel model = new HistoryModel();
                model.date = Util.getCurrentDate();
                model.title = title;
                model.url = view.getUrl();
                model.time = Util.getCurrentTime();
                model.icon = "";
                ProjectBDB.get_instance(BrowserApp.this).insertSearchUrlHistory(model);
                if (url.equalsIgnoreCase("https://www.search.com/")) {
                    bookmarkAddButton.setClickable(false);
                } else {
                    loadedUrl = url;
                    bookmarkAddButton.setClickable(true);
                }
            }

        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        if (getIntent().getData() != null) {//check if intent is not null

            Log.d("debug", "** - refreshButtonVar - **");
            Log.d("debug", String.valueOf(getIntent()));

            Uri data = getIntent().getData();//set a variable for the Intent
            String scheme = data.getScheme();//get the scheme (http,https)
            String fullPath = data.getEncodedSchemeSpecificPart();//get the full path -scheme - fragments
            //webView.setLayoutParams(new ActionBar.LayoutParams(width, height));
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
//            webView.setLayoutParams(params);

            combine = scheme + "://" + fullPath; //combine to get a full URI

            String url = null;//declare variable to hold final URL
            if (combine != null) {//if combine variable is not empty then navigate to that full path
                url = combine;
            } else {//else open main page
                url = "https://www.search.com/";
            }
            webView.loadUrl(url);

        } else {
            webView.loadUrl("https://www.search.com/");
        }*/

        Button buttonOne = (Button) findViewById(R.id.goToUrl);
        buttonOne.setOnClickListener(new Button.OnClickListener() {


            public void onClick(View v) {


                getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                );

                //Do stuff he
                WebView webViewVar = webViewList.get(selectedParentView);//(WebView) findViewById(R.id.webView);
                if (webViewVar != null) {
                    p.show();
                    selectedTabTextView.setText(webViewVar.getTitle());

                    String url = urlString.getText().toString();

                    webViewVar.getSettings().setLoadsImagesAutomatically(true);
                    webViewVar.getSettings().setJavaScriptEnabled(true);
                    webViewVar.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

                    if (!url.startsWith("www.") && !url.startsWith("http://")) {
                        url = "www." + url;
                    }
                    if (!url.startsWith("http://")) {
                        url = "http://" + url;
                    }
                    if (!url.endsWith(".com/")) {
                        url = url + ".com";
                    }
                    Log.w("app", url);

                    webViewVar.loadUrl(url);
                }

            }
        });

    }

    public void createShortCut() {
        /**
         * check if application is running first time, only then create shorcut
         */
        appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isAppInstalled = appPreferences.getBoolean("isAppInstalled", false);
        if (isAppInstalled == false) {
            /**
             * create short code
             */
            Intent shortcutIntent = new Intent(getApplicationContext(), BrowserApp.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Rikoshae");
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher));
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            getApplicationContext().sendBroadcast(intent);
            /**
             * Make preference true
             */
            SharedPreferences.Editor editor = appPreferences.edit();
            editor.putBoolean("isAppInstalled", true);
            editor.commit();
        }
    }

    public void getBannerAdArray() {

        String dataUrl = "http://api.mulaah.com/getweb_Ads_banner"; // ?dUID="+deviceUID+"&dImei="+deviceIMEI;
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(dataUrl, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String content = null;

                try {

                    content = new String(responseBody, "UTF-8");
                    // content is json response that can be parsed.
                    //Toast.makeText(BrowserApp.this, content , Toast.LENGTH_LONG).show();
                    parseBannerAdResponseAmount(content, 10); //operate with response
                    // JSONArray jsonArrayItem = new JSONArray(content);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
//                Toast.makeText(BrowserApp.this,
//                        "onFailure request", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
//                Toast.makeText(BrowserApp.this,
//                        "onRetry request", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void getFullAdArray() {

        // String dataUrl = "http://api.mulaah.com/getweb_ad?dUID="+deviceUID+"&dImei="+deviceIMEI;
        String dataUrl = "http://api.mulaah.com/getweb_Ads_full";

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(dataUrl, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {


                String content = null;

                try {

                    content = new String(responseBody, "UTF-8");
                    // content is json response that can be parsed.

//                    Toast.makeText(BrowserApp.this, " SUCCESS " +content.toString(), Toast.LENGTH_LONG).show();

                    parseResponseAmount(content, 10); //operate with response
                    // JSONArray jsonArrayItem = new JSONArray(content);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                //Toast.makeText(BrowserApp.this, " FAIL " , Toast.LENGTH_LONG).show();
                String content = null;
                try {
                    content = new String(responseBody, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // Toast.makeText(BrowserApp.this, " 4***| FAILURE " + content + " |***4 " , Toast.LENGTH_LONG).show();

            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
                //Toast.makeText(BrowserApp.this, " RETRY " , Toast.LENGTH_LONG).show();
            }

        });

    }

    // on ad click and follow link call this function to post ad click to our server
    public void postAdClick(String adId, String type) {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("RikoshaePrefs", MODE_PRIVATE);
        String uid = pref.getString("rikoshae_prefs_uid", null);

        String dataUrl = "http://api.mulaah.com/ad_click?user_id=3&ad_id=" + adId; // ..."+uid+"&ad_id="+adId;

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(dataUrl, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                try {
                    String content = new String(responseBody, "UTF-8");
                    //content is json response that can be parsed.
                } catch (UnsupportedEncodingException e1) {
//                    userList.clear();
//                    userList.add("Some encoding problems occured");
                    e1.printStackTrace();
                }

            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
//                Toast.makeText(BrowserApp.this,"Retry Post request *** ", Toast.LENGTH_LONG).show();
            }

        });

    }

    public void postAdImpressions(String adId, String type) {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("RikoshaePrefs", MODE_PRIVATE);
        String uid = pref.getString("rikoshae_prefs_uid", null);

        String dataUrl = "http://api.mulaah.com/ad_impressions?user_id="+uid+"&ad_id=" + adId;

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(dataUrl, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                try {

                    String content = new String(responseBody, "UTF-8");
                    // Toast.makeText(BrowserApp.this, " %%% impressions post content || key %%% "+content , Toast.LENGTH_SHORT).show();
                    // content is json response that can be parsed.

                } catch (UnsupportedEncodingException e1) {
//                    userList.clear();
//                    userList.add("Some encoding problems occured");
                    e1.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                try {
                    String content = new String(responseBody, "UTF-8");
                    //content is json response that can be parsed.
                } catch (UnsupportedEncodingException e1) {
//                    userList.clear();
//                    userList.add("Some encoding problems occured");
                    e1.printStackTrace();
                }

            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
//                Toast.makeText(BrowserApp.this,"Retry Post request *** ", Toast.LENGTH_LONG).show();
            }

        });

    }

    JSONArray jsonArray;

    private void parseResponseAmount(String response, int amount) {

        //Toast.makeText(BrowserApp.this, "&&& "+response , Toast.LENGTH_LONG).show();

        JSONArray readerArray = null;
        try {

            //Toast.makeText(BrowserApp.this, "SHIT" , Toast.LENGTH_SHORT).show();
            JSONObject jObjAds = new JSONObject(response);
            Iterator x = jObjAds.keys();
            jsonArray = null;
            jsonArray = new JSONArray();

            while (x.hasNext()) {

                String key = (String) x.next();
                //Toast.makeText(BrowserApp.this, " %%% jObjAds || ARRAY %%% "+jObjAds.get(key).toString() , Toast.LENGTH_LONG).show();
                jsonArray.put(jObjAds.get(key));

            }

            if (jsonArray != null)
                getNewFullPageAd();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    JSONArray bannerArray;

    private final int interval = 25000; // 5 Second
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        public void run() {

            Log.i("Rikoshae", "handler runnable fn called :: curr arrayBannerActiveIndex == "+arrayBannerActiveIndex);

            int newTemp = 1 + arrayBannerActiveIndex;
            arrayBannerActiveIndex = newTemp;

            if (arrayBannerActiveIndex < bannerArray.length()) {

                getNewBannerPageAd();

            }
            else {

                arrayBannerActiveIndex = 10;
                banner.play();

            }
        }
    };

    private void parseBannerAdResponseAmount(String response, int amount) {

        //Toast.makeText(BrowserApp.this, "&&& "+response , Toast.LENGTH_LONG).show();

        JSONArray readerArray = null;
        try {

            //Toast.makeText(BrowserApp.this, "SHIT" , Toast.LENGTH_SHORT).show();
            JSONObject jObjAds = new JSONObject(response);
            Iterator x = jObjAds.keys();
            bannerArray = null;
            bannerArray = new JSONArray();

            while (x.hasNext()) {

                String key = (String) x.next();
                //Toast.makeText(BrowserApp.this, " %%% jObjAds || key %%% "+key , Toast.LENGTH_LONG).show();
                bannerArray.put(jObjAds.get(key));

            }

            Log.i("Rikoshae", "GET BANNER ADD AND STORE SUCCESS");

            arrayBannerActiveIndex = 0;
            //getNewBannerPageAd();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String imei = "";
    public String uid = "";

    public void getIMEInUID() {

        TelephonyManager mngr = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        imei = mngr.getDeviceId();

        Log.i("Rikoshae", "0 ** getIMEInUID ** 0 || imei = "+imei);

        String dataUrl = "http://api.mulaah.com/getuserinfo?imei="+imei; //imei+"&device_id="+did;

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(dataUrl, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {

                String content = null;

                try {

                    Log.i("Rikoshae", "4 ** getUserInformation: success" );
                    content = new String(responseBody, "UTF-8");
                    Log.i("Rikoshae", "4 ** getUserInformation: success :: "+content );
                    // content is json response that can be parsed.
                    // content is json response that can be parsed.
                    JSONObject jsonObjectItem = new JSONObject(content);
                    JSONObject jsonObjectNest = jsonObjectItem.getJSONObject("0");
                    uid = jsonObjectNest.getString("user_id");
                    registerdevice.setVisibility(View.GONE);
                    activeUser = true;
                    banner.play();

                    Log.i("Rikoshae", "4 ** getUserInformation: content == " + content );

                } catch (UnsupportedEncodingException e) {
                    Log.i("Rikoshae", "4 ** getUserInformation: success - no content error -- e "+e  );
                    //banner.pause();
                    updateAppButton.setVisibility(View.GONE);
                    deviceoffline.setVisibility(View.GONE);
                    registerdevice.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                } catch (JSONException e) {
                    Log.i("Rikoshae", "4 ** uid error: success - error json -- e "+e );
                    //banner.pause();
                    updateAppButton.setVisibility(View.GONE);
                    deviceoffline.setVisibility(View.GONE);
                    registerdevice.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                Log.i("LockScreen", "5 ** getUserInformation: FAIL " );
                banner.pause();
                updateAppButton.setVisibility(View.GONE);
                deviceoffline.setVisibility(View.GONE);
                registerdevice.setVisibility(View.VISIBLE);
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
                //Toast.makeText(BrowserApp.this,"onRetry request", Toast.LENGTH_LONG).show();
            }
        });

    }

    class AdArrayClass {

        String adName = "";
        String adId = "";
        String adImgUrl = "";
        String adUrl = "";
        int played = 0;
        int win = 0;

        void adNameSet(String name) {
            adName = name;
        }

        void adIdSet(String adId) {
            this.adId = adId;
        }

        void adImgUrlSet(String url) {
            this.adImgUrl = url;
        }

        void adUrlSet(String url) {
            adUrl = url;
        }

//        void played(){
//            played=played+1;
//        }
//        void win(){
//            win=win+1;
//            points();
//        }
//        void draw(){
//            draw=draw+1;
//            points();
//        }
//        void loss(){
//            loss=loss+1;
//        }
//
//        void points(){
//            points = (win*3)+draw;
//        }

    }

    public void getNewFullPageAd() {

//        Toast.makeText(BrowserApp.this, "** getNewFullPageAd ** ", Toast.LENGTH_LONG).show();

        try {

            JSONObject activeAdObject = (JSONObject) jsonArray.get(arrayActiveIndex);

            if (activeAdObject.getString("ad_image") == "") {

//                Toast.makeText(BrowserApp.this, "** CUSTOM AD IMAGE STRING ** "+adImgUrlString , Toast.LENGTH_LONG).show();
                String adImgUrlString = "https://s3.amazonaws.com/mulaah/" + activeAdObject.getString("ad_custom_image_location").toString();
                Log.i("***", "** && ** adImgUrlString " + adImgUrlString);

                String extension = adImgUrlString.substring(adImgUrlString.length() - 3, adImgUrlString.length()); // get last 3 chars from the string
                Log.i("***", "** && ** adImgUrlString || **(())** " + extension);

                if ((extension == "jpeg") || (extension == "png") || (extension == "jpg")) {
                    new DownloadImageTask((ImageView) findViewById(R.id.blowUpAdImgView)).execute(adImgUrlString);

                    ImageView imageView = ((ImageView) findViewById(R.id.blowUpAdImgView));
                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    int pixel = bitmap.getPixel(1, 1);

                    int redValue = Color.red(pixel);
                    int blueValue = Color.blue(pixel);
                    int greenValue = Color.green(pixel);
                    imageView.setBackgroundColor(Color.rgb(redValue, blueValue, greenValue));

                    postAdImpressions(activeAdObject.getString("ad_id"), "full");
                } else {
                    Log.i("***", "** && ** not an image");
                }


            } else {

//                Toast.makeText(BrowserApp.this, "** BLOW UP AD IMAGE STRING ** "+activeAdObject.getString("ad_image") , Toast.LENGTH_LONG).show();
                String adImgUrlString = activeAdObject.getString("ad_image");
                Log.i("***", "** && ** adImgUrlString " + adImgUrlString);

                String extension = adImgUrlString.substring(adImgUrlString.length() - 3, adImgUrlString.length()); // get last 3 chars from the string
                if ((extension == "jpeg") || (extension == "png") || (extension == "jpg")) {
                    new DownloadImageTask((ImageView) findViewById(R.id.blowUpAdImgView)).execute(adImgUrlString);

                    ImageView imageView = ((ImageView) findViewById(R.id.blowUpAdImgView));
                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    int pixel = bitmap.getPixel(1, 1);

                    int redValue = Color.red(pixel);
                    int blueValue = Color.blue(pixel);
                    int greenValue = Color.green(pixel);
                    imageView.setBackgroundColor(Color.rgb(redValue, blueValue, greenValue));

                    postAdImpressions(activeAdObject.getString("ad_id"), "full");

                } else {
                    Log.i("***", "** && ** not an image");
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void getNewBannerPageAd() {

        banner.pause();
        recommendationsPlaceholder.setVisibility(View.GONE);

        if(bannerArray!=null && bannerArray.length()>0){
            System.out.println("GET NEXT AD "+bannerArray.length());

            Log.i("Rikoshae", "** && ** GET NEW BANNER AD ** && **");
            //recommendationsPlaceholder.setVisibility(View.INVISIBLE);
            Log.i("Rikoshae", "** && ** SHOW NEW BANNER SUCCESS || curr index ** "+arrayBannerActiveIndex+" bannerArray ");
            JSONObject activeAdObject = null;
            try {

                activeAdObject = (JSONObject) bannerArray.get(arrayBannerActiveIndex);
                String adImgUrlString = activeAdObject.getString("ad_image");
                Log.i("Rikoshae", "getNewBannerPageAd ** && ** adImgUrlString == "+adImgUrlString);
                //new DownloadImageTask((ImageView) findViewById(R.id.bannerImgView)).execute("https://tpc.googlesyndication.com/simgad/7892228337379652680");
                new DownloadBannerImageTask(bannerAdImgViewVar1).execute(adImgUrlString);
                postAdImpressions(activeAdObject.getString("ad_id"), "banner");
                handler.postAtTime(runnable, System.currentTimeMillis() + interval);
                handler.postDelayed(runnable, interval);


            } catch (JSONException e) {
                Log.i("Rikoshae", "** && ** getNewBannerPageAd JSONexception == ");
                e.printStackTrace();
            }

        }else {
            System.out.println("Go get BANNER");
            getBannerAdArray();
            getNewBannerPageAd();

        }



//        try {
//
//
//            // Toast.makeText(BrowserApp.this, "* arrayBannerActiveIndex "+arrayBannerActiveIndex , Toast.LENGTH_LONG).show();
//
//            // recommendationsPlaceholder.setText("Mulaah banner ads.");
//            //Toast.makeText(BrowserApp.this, " || * STRINGS * || "+activeAdObject.toString() , Toast.LENGTH_LONG).show();
//
////            Toast.makeText(BrowserApp.this, " || * STRINGS * || "+activeAdObject.getString("ad_image") , Toast.LENGTH_LONG).show();
//
//
////            String adIdString = activeAdObject.getString("ad_id");
//
////            Toast.makeText(BrowserApp.this, "** ID STRING "+activeAdObject.getString("ad_id") , Toast.LENGTH_LONG).show();
//
////            String idString = userObject.getString("id");
////            String adUrlString = userObject.getString("url");
//
//            bannerAdsLoopActive = true;
////            new DownloadImageTask((ImageView) findViewById(R.id.bannerImgView)).execute("https://tpc.googlesyndication.com/simgad/7892228337379652680");
//            new DownloadBannerImageTask(bannerAdImgViewVar1).execute(adImgUrlString);
//
////            ImageView imageView = ((ImageView)findViewById(R.id.bannerImgView));
////            Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
////            int pixel = bitmap.getPixel(1,1);
////
////            int redValue = Color.red(pixel);
////            int blueValue = Color.blue(pixel);
////            int greenValue = Color.green(pixel);
////            imageView.setBackgroundColor(Color.rgb(redValue, blueValue, greenValue));
//
////            postAdImpressions(activeAdObject.getString("ad_id"), "banner");
//
//            recommendationsPlaceholder.setVisibility(View.GONE);
//            handler.postAtTime(runnable, System.currentTimeMillis() + interval);
//            handler.postDelayed(runnable, interval);
//
//            bannerAdsLoopActive = false;
//
//        } catch (JSONException e) {
//
//            Log.i("Rikoshae", "** && ** GET BANNER FAILED");
//            e.printStackTrace();
//        }


    }

    public void getUserInformation() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("RikoshaePrefs", MODE_PRIVATE);
        String imei = pref.getString("rikoshae_prefs_imei", null);
        String did = pref.getString("rikoshae_prefs_id", null);

        //Toast.makeText(BrowserApp.this,imei+" "+did, Toast.LENGTH_LONG).show();
        String dataUrl = "http://api.mulaah.com/getuserinfo?imei=574369688cf71515&device_id=351924070829847"; //imei+"&device_id="+did;

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(dataUrl, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // called when response HTTP status is "200 OK"

                String content = null;

                try {

                    content = new String(responseBody, "UTF-8");
                    // content is json response that can be parsed.
                    JSONObject jsonObjectItem = new JSONObject(content);
                    JSONObject jsonObjectNest = jsonObjectItem.getJSONObject("0");
                    deviceUID = jsonObjectNest.getString("user_id");

                    SharedPreferences pref = getApplicationContext().getSharedPreferences("RikoshaePrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("rikoshae_prefs_set", true);           // Saving boolean - true/false
                    editor.putString("rikoshae_prefs_uid", deviceUID);  // Saving string
                    // Save the changes in SharedPreferences
                    editor.commit();
                    //String adLinkDestination = jsonObjectItem.getString("ad_linkDest");

                    //Toast.makeText(BrowserApp.this, "SUCCESS USER ID INFO - ARRAY "+uid , Toast.LENGTH_LONG).show();
                    //Toast.makeText(BrowserApp.this, "SUCCESS USER INFO - ARRAY "+jsonArrayItem.toString() , Toast.LENGTH_LONG).show();

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                //Toast.makeText(BrowserApp.this,"onFailure request", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
                //Toast.makeText(BrowserApp.this,"onRetry request", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void bannerAdClickedFn() {

        JSONObject activeAdObject = null;
        try {

            int sommmm;
            if (arrayBannerActiveIndex != 0) {
                sommmm = arrayBannerActiveIndex - 1;
            } else {
                sommmm = arrayBannerActiveIndex;
            }
            activeAdObject = (JSONObject) bannerArray.get(sommmm);

            String adLinkDestString = activeAdObject.getString("ad_linkDest");
            //Toast.makeText(BrowserApp.this,"c && CHANGE && "+adLinkDestString, Toast.LENGTH_LONG).show();

            WebView webView = (WebView) findViewById(R.id.webView);
            webView.loadUrl(adLinkDestString);
//            webView.reload();
            String adIdString = activeAdObject.getString("ad_id");
            int newTemp = 1 + arrayBannerActiveIndex;
            arrayBannerActiveIndex = newTemp;

            postAdClick(activeAdObject.getString("ad_id"), "banner");


        } catch (JSONException e) {
            e.printStackTrace();

        }

    }

    public void closeLargeAdView() {

        ImageView blowUpAdImgViewVar = (ImageView) findViewById(R.id.blowUpAdImgView);
        blowUpAdImgViewVar.setVisibility(View.INVISIBLE);

        ImageButton blowUpAdImgCloseViewVar = (ImageButton) findViewById(R.id.closeLargeAdView);
        blowUpAdImgCloseViewVar.setVisibility(View.INVISIBLE);

        arrayActiveIndex = arrayActiveIndex + 1;
        if (jsonArray != null && arrayActiveIndex == jsonArray.length()) {
            arrayActiveIndex = 0;
        }

    }

    public void blowUpAdClickedFn() {

        ImageView blowUpAdImgViewVar = (ImageView) findViewById(R.id.blowUpAdImgView);
        blowUpAdImgViewVar.setVisibility(View.INVISIBLE);

        ImageButton blowUpAdImgCloseViewVar = (ImageButton) findViewById(R.id.closeLargeAdView);
        blowUpAdImgCloseViewVar.setVisibility(View.INVISIBLE);

        WebView webView = (WebView) findViewById(R.id.webView);

        JSONObject activeAdObject = null;
        try {

            if (jsonArray != null) {
                activeAdObject = (JSONObject) jsonArray.get(arrayActiveIndex);
                webView.loadUrl(activeAdObject.getString("ad_linkDest"));
                postAdClick(activeAdObject.getString("ad_id"), "full");
            }
        } catch (JSONException e) {
            //webView.loadUrl("http://mulaah.com/");
            e.printStackTrace();
        }

        arrayActiveIndex = arrayActiveIndex + 1;
        if (jsonArray != null && arrayActiveIndex == jsonArray.length()) {
            arrayActiveIndex = 0;
        }

    }

    private class DownloadBannerImageTask extends AsyncTask<String, Void, Bitmap> {

        ImageView bmImage;

        public DownloadBannerImageTask(ImageView bmImage) {
            Log.d("Rikoshae", "** - DownloadBannerImageTask - ** "+bmImage);
            this.bmImage = bmImage;
            this.bmImage.setVisibility(View.VISIBLE);
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            bmImage.setVisibility(View.VISIBLE);
            // recommendationsPlaceholder.setVisibility(View.GONE);
            // bannerAdImgViewVar1.setVisibility(View.VISIBLE);
            Log.d("Rikoshae", "** - onPostExecute - ** "+result);

            if (arrayBannerActiveIndex != 10) {
                int newTemp = 1 + arrayBannerActiveIndex;
                arrayBannerActiveIndex = newTemp;
                if (arrayBannerActiveIndex >= bannerArray.length()) {

//                    //Toast.makeText(BrowserApp.this,"c && CHANGE && ", Toast.LENGTH_LONG).show();
                    arrayBannerActiveIndex = 0;

                }
            }

        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


    // This method is check that tab is already exist or not
    public void addTab(String indicatorTag) {
        String tagId = "android";
        boolean isExist = false;
        if (indicatorTag.equalsIgnoreCase("history")) {
            tabIndex++;
            tagId = indicatorTag;
            if (historyTabIndex > 0) {
                isExist = true;
            }
        } else if (indicatorTag.equalsIgnoreCase("bookmark")) {
            tabIndex++;
            tagId = indicatorTag;
            if (bookmarkTabIndex > 0) {
                isExist = true;
            }
        }

        if (!isExist) {

            setupTab(tabIndex, tagId + "," + tabIndex, indicatorTag);
            horizontalScrollBarView.post(new Runnable() {
                public void run() {
                    horizontalScrollBarView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                }
            });
        } else {
            View v = null;
            for (int i = 0; i < tabLayout.getChildCount(); i++) {
                View view = tabLayout.getChildAt(i);
                view.setBackgroundColor(getResources().getColor(R.color.grey));
                String tag = view.getTag().toString();
                if (tag.equalsIgnoreCase(indicatorTag))
                    v = view;
            }
            if (v != null)
                v.setBackgroundColor(getResources().getColor(R.color.white));

        }
    }


    // This method is used for add new tab with using inflate layout
    protected void setupTab(int id, String tag, String labelText) {
        if (labelText.equalsIgnoreCase("history")) {
            historyTabIndex = id;
        } else if (labelText.equalsIgnoreCase("bookmark")) {
            bookmarkTabIndex = id;
        }
        LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.tab_events, null);
        v.setTag(labelText);
        TextView title = (TextView) v.findViewById(R.id.title);
        ImageButton close = (ImageButton) v.findViewById(R.id.closeButton);
        close.setImageResource(R.mipmap.cross);
        title.setTag(labelText);
        close.setTag(labelText);
        title.setText(labelText);
        //  title.setSelected(true);
        //v.getLayoutParams().width = 100;
        selectedTabTextView = title;
        tabLayout.addView(v);
        for (int i = 0; i < tabLayout.getChildCount(); i++) {
            tabLayout.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.grey));
        }
        v.setBackgroundColor(getResources().getColor(R.color.white));
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    selectedTabFunction(v);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSelectedTabFunction(v);
            }
        });
        selectedParentView = v;
        if (!labelText.equalsIgnoreCase("history") && !labelText.equalsIgnoreCase("bookmark"))
            loadWebview(v, false);
    }

    // it is for highlight of selected tab and show web pages according to selected tab
    private void selectedTabFunction(View v) {
        if (v.getTag().toString().equalsIgnoreCase("history") || v.getTag().toString().equalsIgnoreCase("bookmark")) {
            historyLinearLayout.setVisibility(View.VISIBLE);
            if (v.getTag().toString().equalsIgnoreCase("bookmark")) {
                bookmarkTabIndex = 0;
                setHistoryAndBookmarkLayout("bookmark");
            } else {
                historyTabIndex = 0;
                setHistoryAndBookmarkLayout("history");
            }
        } else
            historyLinearLayout.setVisibility(View.GONE);

        for (int i = 0; i < tabLayout.getChildCount(); i++) {
            tabLayout.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.grey));
        }
        selectedTabTextView = (TextView) v;
        View view = (View) v.getParent();
        selectedParentView = view;
        bookmarkAddButton.setClickable(false);
        view.setBackgroundColor(getResources().getColor(R.color.white));
        loadWebview(view, true);
    }

    // this is for close the tab
    private void closeSelectedTabFunction(View v) {
        if (tabLayout.getChildCount() == 1)
            finish();
        else {

            View parentView = (View) v.getParent();
            tabLayout.removeView(parentView);
            webViewList.remove(parentView);
            if (v.getTag().toString().equalsIgnoreCase("history")) {
                historyTabIndex = 0;
                historyLinearLayout.setVisibility(View.GONE);
            } else if (v.getTag().toString().equalsIgnoreCase("bookmark")) {
                bookmarkTabIndex = 0;
                historyLinearLayout.setVisibility(View.GONE);
            }
            if (parentView == selectedParentView) {
                selectedParentView = tabLayout.getChildAt(tabLayout.getChildCount() - 1);
                ViewGroup row = (ViewGroup) v.getParent();
                for (int itemPos = 0; itemPos < row.getChildCount(); itemPos++) {
                    View view = row.getChildAt(itemPos);
                    if (view instanceof TextView) {
                        selectedTabTextView = (TextView) view; //Found it!
                        break;
                    }
                }
                bookmarkAddButton.setClickable(false);
                selectedParentView.setBackgroundColor(getResources().getColor(R.color.white));
                loadWebview(selectedParentView, true);
            }

        }
    }

    // for show history and bookmark list
    private void setHistoryAndBookmarkLayout(String comeFrom) {
        historyLinearLayout.removeAllViews();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.history_list, null, false);
        historyListView = (ListView) rootView.findViewById(R.id.historyDatesListView);
        clearBrowsingDataButton = (Button) rootView.findViewById(R.id.clearBrowsingDataButton);
        removeSelectedItemButton = (Button) rootView.findViewById(R.id.removeSelectedItemButton);
        if (comeFrom.equalsIgnoreCase("history"))
            setBrowserHistoryListAdapter();
        else if (comeFrom.equalsIgnoreCase("bookmark")) {
            clearBrowsingDataButton.setVisibility(View.GONE);
            removeSelectedItemButton.setVisibility(View.GONE);
            setBrowserBookmarkListAdapter();
        }

        // this is for clear all browser history list
        clearBrowsingDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CustomDialog(BrowserApp.this, getResources().getString(R.string.confirmation), getResources().getString(R.string.clear_all_history_confirmation_msg), new String[]{"Yes", "No"}, new DialogButtonListener() {
                    @Override
                    public void onButtonClicked(String text) {
                        if (text.equalsIgnoreCase("yes")) {
                            ProjectBDB.get_instance(BrowserApp.this).deleteAllHistoryData();
                            historyList.clear();
                            histAdapter.notifyDataSetChanged();
                        }
                    }
                }).show();
            }
        });

        // this is for remove selected browser history
        removeSelectedItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProjectBDB.get_instance(BrowserApp.this).deleteSelectedHistoryData(selectedHistoryList);
                historyList.clear();
                historyList.addAll(ProjectBDB.get_instance(BrowserApp.this).getSearchedHistory());
                histAdapter.notifyDataSetChanged();
            }
        });

        historyLinearLayout.addView(rootView);
    }

    private void setBrowserHistoryListAdapter() {
        histAdapter = new HistoryAdapter(this, historyList);
        historyListView.setAdapter(histAdapter);
    }

    private void setBrowserBookmarkListAdapter() {
        bookAdapter = new BookmarkAdapter(this, bookmarkList);
        historyListView.setAdapter(bookAdapter);
    }


    public void updateHistoryList(HistoryModel mod, int position) {
        historyList.set(position, mod);
        selectedHistoryList.remove(mod);
        selectedHistoryList.add(mod);
        if (selectedHistoryList.size() == 0) {
            removeSelectedItemButton.setClickable(false);
            removeSelectedItemButton.setTextColor(getResources().getColor(R.color.default_color));
        } else {
            removeSelectedItemButton.setClickable(true);
            removeSelectedItemButton.setTextColor(getResources().getColor(R.color.black));
        }
    }

    public void deleteBookmarkData(HistoryModel model) {
        ProjectBDB.get_instance(this).deleteBookmarkData(model);
        bookmarkList.remove(model);
        bookAdapter.notifyDataSetChanged();
    }

    private void loadWebview(View v, boolean isAlreadyAdded) {
        loadWebViewLinearLayout.removeAllViews();
        if (!isAlreadyAdded) {
            WebView webView = new WebView(this);
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);

            webView.setWebViewClient(new MyAppWebViewClient() {

                public void onPageStarted(WebView view, String url) {
                    loadingFinished = false;
                    //SHOW LOADING IF IT ISNT ALREADY VISIBLE
//                ProgressDialog dialog = ProgressDialog.show(BrowserApp.this, "",
//                        "Loading. Please wait...", true);
                }

                @Override
                public void onPageFinished(WebView view, String url) {

                    p.dismiss();
                    if (urlString != null)
                        urlString.setText(url);
                    pagecounter = pagecounter + 1;

                    title = view.getTitle();
                    if (selectedTabTextView != null)
                        selectedTabTextView.setText(title);
                    if (!redirect) {
                        loadingFinished = true;
                    }

                    if (loadingFinished && !redirect) {
                        //HIDE LOADING IT HAS FINISHED
                    } else {
                        redirect = false;
                    }

                    if (pagecounter == 4) {

                        ImageView blowUpAdImgViewVar = (ImageView) findViewById(R.id.blowUpAdImgView);
                        blowUpAdImgViewVar.setVisibility(View.VISIBLE);

                        ImageButton blowUpAdImgCloseViewVar = (ImageButton) findViewById(R.id.closeLargeAdView);
                        blowUpAdImgCloseViewVar.setVisibility(View.VISIBLE);

                        pagecounter = 0;

                        if (jsonArray != null)
                            getNewFullPageAd();

                    }
                    HistoryModel model = new HistoryModel();
                    model.title = title;
                    model.url = view.getUrl();
                    model.icon = "";
                    ProjectBDB.get_instance(BrowserApp.this).insertSearchUrlHistory(model);
                    if (url.equalsIgnoreCase("https://www.search.com/")) {
                        bookmarkAddButton.setClickable(false);
                    } else {
                        loadedUrl = url;
                        bookmarkAddButton.setClickable(true);
                    }
                }

            });

            webView.getSettings().setJavaScriptEnabled(true);
            webView.setVerticalScrollBarEnabled(false);
            webView.setHorizontalScrollBarEnabled(false);

            if (getIntent().getData() != null) {//check if intent is not null

                Log.d("debug", "** - refreshButtonVar - **");
                Log.d("debug", String.valueOf(getIntent()));

                Uri data = getIntent().getData();//set a variable for the Intent
                String scheme = data.getScheme();//get the scheme (http,https)
                String fullPath = data.getEncodedSchemeSpecificPart();//get the full path -scheme - fragments
                //webView.setLayoutParams(new ActionBar.LayoutParams(width, height));
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
//            webView.setLayoutParams(params);

                combine = scheme + "://" + fullPath; //combine to get a full URI

                String url = null;//declare variable to hold final URL
                if (combine != null) {//if combine variable is not empty then navigate to that full path
                    url = combine;
                } else {//else open main page
                    url = "https://www.search.com/";
                }
                p.show();
                webView.loadUrl(url);

            } else {
                p.show();
                webView.loadUrl("https://www.search.com/");
            }
            webViewList.put(v, webView);
            loadWebViewLinearLayout.addView(webView);
        } else {
            WebView webView = webViewList.get(v);
            if (webView != null) {
                selectedTabTextView.setText(webView.getTitle());
                String url = webView.getUrl();
                urlString.setText(url);
                if (url.equalsIgnoreCase("https://www.search.com/")) {
                    bookmarkAddButton.setClickable(false);
                } else {
                    loadedUrl = url;
                    title = webView.getTitle();
                    bookmarkAddButton.setClickable(true);
                }
                loadWebViewLinearLayout.addView(webView);
            }
        }
    }

    public void aerserSuccFn() {

        bannerAdImgViewVar1.setVisibility(View.GONE);

    }



    public void aerserfailfn() {

        Log.i( "Rikoshae", "8 ** aerserfailfn: count:: "+aerservfailcount );

        if (aerservfailcount == 5 || aerservfailcount > 4) {

            Log.i( "Rikoshae", " equal 5 look () () "+aerservfailcount );

            aerservfailcount = 0;

            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            wait(1000);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    recommendationsPlaceholder.setVisibility(View.INVISIBLE);
                                    bannerAdImgViewVar1.setVisibility(View.VISIBLE);
                                    getNewBannerPageAd();

                                }
                            });

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                ;
            };
            thread.start();


        }
        else {

            Log.i( "Rikoshae", " NOT equal 5 look () () "+aerservfailcount );

            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            wait(1000);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    recommendationsPlaceholder.setVisibility(View.VISIBLE);
                                    aerservfailcount = 1 + aerservfailcount;
                                    String count = String.valueOf(aerservfailcount);
                                    recommendationsPlaceholder.setText("Aerserv is running " + count + "x's.");

                                }
                            });

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                ;
            };
            thread.start();


        }


    }

    public String updateurl = "";
    public String version = "1.00.01";
    public Boolean updatedneeded = false;

    public void getUpdateStatus() {

        Log.i("Rikoshae", " 0 ** getUpdateStatus ** 0 || imei = ");

        String dataUrl = "http://api.mulaah.com/getUpdateBrowser"; //imei+"&device_id="+did;

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(dataUrl, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {

                String content = null;

                try {

                    Log.i("Rikoshae", "4 ** getUpdateBrowser: success" );
                    content = new String(responseBody, "UTF-8");
                    Log.i("Rikoshae", "4 ** getUserInformation: success :: "+content );
                    // content is json response that can be parsed.
                    // content is json response that can be parsed.
                    JSONObject jsonObjectItem = new JSONObject(content);
                    JSONObject jsonObjectNest = jsonObjectItem.getJSONObject("0");
                    String updateversion = jsonObjectNest.getString("version");
                    updateurl = jsonObjectNest.getString("base_url") + jsonObjectNest.getString("filename");
                    Log.i("Rikoshae", " || ** updateurl ** || "+updateurl );

                    if (version.equals(updateversion)) {
                        Log.i("Rikoshae", "4 ** getUpdateLockScreen: NO UPDATED NEEDED SAME VERSION updateversion == " + updateversion);
                        updatedneeded = false;
                    }
                    else {
                        Log.i("Rikoshae", "4 ** getUpdateLockScreen: UPDATE NOW NEW VERSION AVAILABLE updateversion == " + updateversion);
                        updatedneeded = true;
                        banner.pause();
//                        banner.setVisibility(View.GONE);
                        Button updateAppButton = (Button) findViewById(R.id.updateAppButton);
                        updateAppButton.setVisibility(View.VISIBLE);
//                        gotodash.setText("Download the updated version of the Mulaah Lockscreen to earn more Mulaah today!");
                    }

                    // Log.i("LockScreen", "4 ** getUpdateLockScreen: content == " + jsonObjectNest );

                } catch (UnsupportedEncodingException e) {
                    Log.i("LockScreen", "4 ** getUpdateLockScreen: success - no content error -- e "+e  );
                    e.printStackTrace();
                } catch (JSONException e) {
                    Log.i("LockScreen", "4 ** getUpdateLockScreen - error json -- e "+e );
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                Log.i("LockScreen", "5 ** getUserInformation: FAIL " );
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
                //Toast.makeText(BrowserApp.this,"onRetry request", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void updateAppFn() {

        Log.i("Rikoshae", " %% updateAppFn %% "+updateurl);

        p.show();
        Button updateAppButton = (Button) findViewById(R.id.updateAppButton);
        updateAppButton.setText("Updating. Please wait...");
        InstallAPK atualizaApp = new InstallAPK();
        atualizaApp.setContext(getApplication());
        atualizaApp.execute(updateurl);

    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i("Rikoshae", " %% onResume %% ");

        boolean online = isOnline(getApplicationContext());
        Log.i("Rikoshae", " CREATE :: -||- :: ANDROID || online = "+online);

        if (online) {

            Log.i("Rikoshae", "resume - device online ");
            getUpdateStatus();
            getIMEInUID();
            deviceoffline.setVisibility(View.GONE);
            recommendationsPlaceholder.setVisibility(View.VISIBLE);

        }
        else {

            Log.i("Rikoshae", "resume - device offline ");
            banner.pause();
            recommendationsPlaceholder.setVisibility(View.GONE);
            deviceoffline.setVisibility(View.VISIBLE);
//            mainbannerad.setVisibility(View.GONE);
//            mulaahBannerAdImg.setVisibility(View.GONE);
//            registerdevice.setVisibility(View.VISIBLE);
//            registerdevice.setText("This device is currently offline.");
            activeUser = false;

        }

    }

}
