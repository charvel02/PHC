package gov.cdc.phc;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PHC_webapp extends AppCompatActivity {
    private WebView mWebView;
    private String PHC_URL;
    private String PHC_LOGOFF_URL;

    private SiteCatalystController siteCatalystController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        siteCatalystController = new SiteCatalystController();
        siteCatalystController.trackNavigationEvent(Constants.SC_PAGE_TITLE_WEB_APP, Constants.SC_SECTION_WEB_APP);

        setContentView(R.layout.activity_phc_webapp);

        //hide action bar
        hide();

        if(MainActivity.preferences.getString(PHCPreferences.LOCATION, Constants.OFF_SITE_LOCATION).equals(Constants.ON_SITE_LOCATION)){
            PHC_URL = Constants.ON_SITE_URL;
            PHC_LOGOFF_URL = Constants.ON_SITE_LOGOFF_URL;
        } else {
            PHC_URL = Constants.OFF_SITE_URL;
            PHC_LOGOFF_URL = Constants.OFF_SITE_LOGOFF_URL;
        }

        mWebView = (WebView) findViewById(R.id.phc_webview);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //Log.d("PHCWebApp", "view attempting to load URL: " +url);
                //Log.d("PHCWebApp", "check for 'extrainfo' in url");
                //Log.d("PHCWebApp", "url contains 'extrainfo'? " +url.toLowerCase().contains("extrainfo"));
                if(url.toLowerCase().contains("extrainfo")){
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }
                else return false;
            }
        });
        //Log.d("PHC|Webapp",PHC_URL);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        mWebView.loadUrl(PHC_URL);
    }

    private void promptForExit(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        logoffAndExit();
                        dialog.dismiss();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.cancel();
                        break;
                }
            }
        };
        AlertDialog.Builder exitAlert = new AlertDialog.Builder(PHC_webapp.this);
        exitAlert.setMessage("Would you like to quit?").setCancelable(true)
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener);
        AlertDialog alert = exitAlert.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void logoffAndExit(){
        mWebView.loadUrl(PHC_LOGOFF_URL);
        finishAffinity();
    }

    @Override
    public void onBackPressed() {
        promptForExit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        logoffAndExit();
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }
}
