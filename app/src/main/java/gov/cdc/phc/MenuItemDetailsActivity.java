package gov.cdc.phc;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Arrays;

public class MenuItemDetailsActivity extends AppCompatActivity {
    private String title;
    private Button close;
    private WebView webView;
    //Following arrays are used to set Activity title, load page, and track metrics based on the String passed in from newIntent()
    private ArrayList<String> titles = new ArrayList<String>(Arrays.asList("About", "Disclaimer", "Privacy Policy", "FAQs"));
    private ArrayList<String> pages = new ArrayList<String>(Arrays.asList("about.html","disclaimer.html","privacy_policy.html","faqs.html"));
    private ArrayList<String> scPageNames = new ArrayList<String>(Arrays.asList(Constants.SC_PAGE_TITLE_ABOUT, Constants.SC_PAGE_TITLE_DISCLAIMER, Constants.SC_PAGE_TITLE_PRIVACY_POLICY, Constants.SC_PAGE_TITLE_FAQS));
    private SiteCatalystController siteCatalystController;

    public static Intent newIntent(Context packageContext, String title) {

        Intent intent = new Intent(packageContext, MenuItemDetailsActivity.class);
        intent.putExtra("title", title);
        return intent;

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_item_details_webview);

        // Get the message from the intent
        Intent intent = getIntent();
        title = intent.getStringExtra("title");

        siteCatalystController = new SiteCatalystController();
        siteCatalystController.trackNavigationEvent(scPageNames.get(titles.indexOf(title)), Constants.SC_EVENT_NAV_SECTION);

        webView = (WebView) findViewById(R.id.menu_item_details_webview);

        webView.setWebChromeClient(new WebChromeClient() {
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }

        });
        webView.getSettings()
                .setJavaScriptEnabled(true);

        // Add Download listener in case user clicks link to a file as most MMWR articles have a link to a PDF file.
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        // Enable zoom controls
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        // Enable webview browser back navigation
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            if (webView.canGoBack()) {
                                webView.goBack();
                                return true;
                            }
                            break;
                    }
                }
                return false;
            }
        });

        //
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.startsWith("http")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                } else return false;
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.loadUrl("file:///android_asset/" +pages.get(titles.indexOf(title)));

        //Get display size and set size of Activity to a percentage of window size.
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = (int) (dm.heightPixels * 0.9);
        params.width = (int)(dm.widthPixels);

        this.getWindow().setAttributes(params);

        setTitle(title);

        close = (Button) findViewById(R.id.menu_item_details_close_btn);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuItemDetailsActivity.this.finish();
            }
        });

        setFinishOnTouchOutside(false);
    }

    public class WebAppInterface{
        Context context;

        WebAppInterface(Context c){
            context = c;
        }
        String version = MainActivity.preferences.getString(PHCPreferences.VERSION, "1.0");

        @JavascriptInterface
        public String getVersion(){
            return version;
        }
    }


}
