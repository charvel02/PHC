package gov.cdc.phc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class MenuItemDetailsActivity extends AppCompatActivity {
    private String title;
    private Button close;
    private TextView content;
    private TextView version;
    private ArrayList<String> titles = new ArrayList<String>(Arrays.asList("About", "Disclaimer", "Privacy Policy", "FAQs"));
    private ArrayList<Integer> contentIDs = new ArrayList<Integer>(Arrays.asList(R.string.about_content, R.string.disclaimer_content, R.string.privacy_policy_content, R.string.faq_content));

    public static Intent newIntent(Context packageContext, String title) {

        Intent intent = new Intent(packageContext, MenuItemDetailsActivity.class);
        intent.putExtra("title", title);
        return intent;

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_item_details);

        // Get the message from the intent
        Intent intent = getIntent();
        title = intent.getStringExtra("title");

        content = (TextView) findViewById(R.id.menu_item_details_content);
        version = (TextView) findViewById(R.id.version);
        version.setVisibility(View.GONE);

        if(title.equals("About")){
            version.setVisibility(View.VISIBLE);
            version.setText("PHC Version " +getApplicationVersionName());
        }
        content.setText(contentIDs.get(titles.indexOf(title)));

        //Get display size and set size of Activity to a percentage of window size.
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = (int) (dm.heightPixels * .9);
        params.width = (int)(dm.widthPixels * .60);

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

    public String getApplicationVersionName() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ex) {} catch(Exception e){}
        return "";
    }

}
