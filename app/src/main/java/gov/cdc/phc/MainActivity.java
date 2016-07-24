package gov.cdc.phc;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

public class MainActivity extends AppCompatActivity {
    public static SharedPreferences preferences;
    public static SharedPreferences.Editor prefEditor;
    private Button getStarted;
    private Drawer drawer;
    private Toolbar toolbar;
    private SiteCatalystController siteCatalystController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        siteCatalystController = new SiteCatalystController();

        preferences = getSharedPreferences(PHCPreferences.PREFS_NAME, 0);
        prefEditor = preferences.edit();

        if(!preferences.contains(PHCPreferences.LOCATION)){
            prefEditor.putString(PHCPreferences.LOCATION, Constants.OFF_SITE_LOCATION);
            prefEditor.commit();
        }
        prefEditor.putString(PHCPreferences.VERSION, getApplicationVersionName()).commit();
        siteCatalystController.trackAppLaunchEvent();
        siteCatalystController.trackNavigationEvent(Constants.SC_PAGE_TITLE_MAIN, Constants.SC_SECTION_MAIN);


        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getStarted = (Button) findViewById(R.id.getStarted);

        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PHC_webapp.class));
            }
        });
        initNavDrawer();
        if(preferences.getBoolean(PHCPreferences.SHOW_BANDWIDTH_ALERT, true)){
            bandwidthAlert();
        }
    }

    private void initNavDrawer(){
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.about).withSelectable(false);
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.disclaimer).withSelectable(false);
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.privacy_policy).withSelectable(false);
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(4).withName(R.string.select_location).withSelectable(false)
                .withDescription(R.string.select_location_subtext);
        PrimaryDrawerItem item5 = new PrimaryDrawerItem().withIdentifier(5).withName(R.string.faqs).withSelectable(false);

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        item1,
                        item2,
                        item3,
                        item4,
                        item5
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch((int)drawerItem.getIdentifier()){
                            case 1:
                                //About clicked
                                startActivity(MenuItemDetailsActivity.newIntent(getApplicationContext(), "About"));
                                break;
                            case 2:
                                //Disclaimer clicked
                                startActivity(MenuItemDetailsActivity.newIntent(getApplicationContext(), "Disclaimer"));
                                break;
                            case 3:
                                //Privacy Policy clicked
                                startActivity(MenuItemDetailsActivity.newIntent(getApplicationContext(), "Privacy Policy"));
                                break;
                            case 4:
                                //Select Location clicked
                                promptForPassword();
                                break;
                            case 5:
                                //FAQs clicked
                                startActivity(MenuItemDetailsActivity.newIntent(getApplicationContext(), "FAQs"));
                                break;

                        }
                        return false;
                    }
                })
                .withHeader(R.layout.nav_header_main)
                .withSelectedItem(-1)
                .build();
    }

    private void promptForPassword(){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View passwordEntry = layoutInflater.inflate(R.layout.select_location_alert, null);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        String pw = "PHC RCT";
                        String userInput = null;
                        EditText passwordField = (EditText) passwordEntry.findViewById(R.id.select_location_password);
                        userInput = passwordField.getText().toString();
                        Log.d("User typed: ", userInput);
                        if(userInput.equals(pw)){
                            dialog.dismiss();
                            selectLocation();
                        }
                        else{
                            promptForPassword();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.cancel();
                        break;

                }
            }
        };

        AlertDialog.Builder enterPassword = new AlertDialog.Builder(MainActivity.this);
        enterPassword.setTitle("Please Enter Password")
                .setView(passwordEntry)
                .setPositiveButton("OK", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener);
        AlertDialog alert = enterPassword.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }
    private void selectLocation(){
        siteCatalystController.trackNavigationEvent(Constants.SC_PAGE_TITLE_SELECT_LOCATION, Constants.SC_EVENT_NAV_SECTION);
        final CharSequence[] items = new CharSequence[2];
        items[0] = "Offsite";
        items[1] = "Onsite (Clinic)";
        int checked;
        if(preferences.getString(PHCPreferences.LOCATION, Constants.OFF_SITE_LOCATION).equals(Constants.OFF_SITE_LOCATION)){
            checked = 0;
        } else {
            checked = 1;
        }

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        ListView lw = ((AlertDialog)dialog).getListView();
                        int checkedItem = lw.getCheckedItemPosition();
                        if(checkedItem == 1) {
                            prefEditor.putString(PHCPreferences.LOCATION, Constants.ON_SITE_LOCATION);
                            siteCatalystController.trackEvent(Constants.SC_EVENT_ONSITE_SELECTED, Constants.SC_PAGE_TITLE_SELECT_LOCATION, Constants.SC_EVENT_NAV_SECTION);
                        } else {
                            prefEditor.putString(PHCPreferences.LOCATION, Constants.OFF_SITE_LOCATION);
                            siteCatalystController.trackEvent(Constants.SC_EVENT_OFFSITE_SELECTED, Constants.SC_PAGE_TITLE_SELECT_LOCATION, Constants.SC_EVENT_NAV_SECTION);
                        }
                        prefEditor.commit();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.cancel();
                }
            }
        };
        AlertDialog.Builder selectLocation = new AlertDialog.Builder(MainActivity.this);
        selectLocation.setTitle("Select Location")
                .setSingleChoiceItems(items, checked, null)
                .setPositiveButton("OK", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener);
        AlertDialog alert = selectLocation.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }
    private void bandwidthAlert(){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View bandwidth = layoutInflater.inflate(R.layout.bandwidth_usage_alert, null);

        final AlertDialog.Builder bandwidthAlert = new AlertDialog.Builder(MainActivity.this);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        CheckBox bandwidthCheckBox = (CheckBox) bandwidth.findViewById(R.id.bandwidth_usage_checkbox);
                        if(bandwidthCheckBox.isChecked()){
                            prefEditor.putBoolean(PHCPreferences.SHOW_BANDWIDTH_ALERT, false).commit();
                        }
                        dialog.dismiss();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        finishAffinity();
                        dialog.cancel();
                }
            }
        };
        bandwidthAlert.setTitle(R.string.bandwidth_alert_title)
                .setView(bandwidth)
                .setPositiveButton("OK", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener);
        AlertDialog alert = bandwidthAlert.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }
    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen()){
            drawer.closeDrawer();
        }
        else {
            super.onBackPressed();
        }
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
