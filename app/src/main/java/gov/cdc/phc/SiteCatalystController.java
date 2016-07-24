package gov.cdc.phc;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by jason on 7/24/16.
 */
public class SiteCatalystController {
        private String cdcServer = "http://tools.cdc.gov/metrics.aspx?";
        private String localServer = "http://172.16.4.77:8080/metrics?";
        private String commonConstParams = "c8=Mobile+App&c51=Standalone&c52=Positive+Health+Check&c5=eng&channel=IIU";
        private String prodConstParams = "reportsuite=cdcsynd";
        private String debugConstParams = "reportsuite=devcdc";
        private HttpURLConnection urlConnection;

        public void trackEvent(String event, String title, String section){
            String deviceModel, deviceOsName, deviceOsVers, deviceParams;
            String sectionInfo, appVersion, server, appInfoParams, pageName;
            String deviceOnline, constParams, metricUrl, eventInfo;

            Boolean debug = true;
            Boolean debugLocal = false;

            //application info
            appVersion = MainActivity.preferences.getString(PHCPreferences.VERSION, "1.0");
            appInfoParams = "c53=" +encodeString(appVersion);

            server =debugLocal ? localServer : cdcServer;

            //device info
            deviceModel = (Build.MODEL.toLowerCase().startsWith(Build.MANUFACTURER.toLowerCase()) ? Build.MODEL : Build.MANUFACTURER +"-" +Build.MODEL);
            deviceOsName = "Android";
            deviceOsVers = "" +Build.VERSION.RELEASE;
            deviceParams = "c54=" +deviceOsName +"&c55=" +encodeString(deviceOsVers) +"&c56=" +encodeString(deviceModel);

            //set event param
            eventInfo = "c58=" +event;

            //set section param
            sectionInfo = "c59=" +encodeString(section);

            //page information
            pageName = "contenttitle=" +encodeString(title);

            //device online status
            deviceOnline = "c57=1";

            constParams = (debug ? debugConstParams : prodConstParams) + "&" +commonConstParams;

            metricUrl = server + constParams +"&" +deviceParams +"&" +appInfoParams +"&" + deviceOnline
                    +"&" +eventInfo +"&" +sectionInfo +"&" + pageName;

            postSCEvent(metricUrl);
            //Log.d("SCController:", "metric URL =" +metricUrl);


        }
        private String encodeString(String str){
            String encodedString = null;
            try{
                encodedString = URLEncoder.encode(str, "utf-8");
                //Log.d("encode: ", encodedString);
            }
            catch(IOException ioe){
                ioe.printStackTrace();
            }
            return encodedString;
        }
        public void trackAppLaunchEvent(){
            trackEvent(Constants.SC_EVENT_APP_LAUNCH, Constants.SC_PAGE_TITLE_LAUNCH, Constants.SC_PAGE_TITLE_MAIN);
        }
        public void trackNavigationEvent(String pageTitle, String section){
            trackEvent(Constants.SC_EVENT_NAV_SECTION, pageTitle, section);
        }

        private void postSCEvent(String scString){
            AsyncHttpTask at = new AsyncHttpTask();
            at.execute(scString);
        }
        public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

            @Override
            protected Integer doInBackground(String... params) {
                urlConnection = null;
                Integer result = 0;
                try {
                /* forming th java.net.URL object */
                    URL url = new URL(params[0]);
                    if(BuildConfig.APPLICATION_ID.contains("development"))
                        Log.d("SC URL: ", ""+url);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    //Log.d("SC: ", "urlCon status: " +urlConnection);

                 /* optional request header */
                    urlConnection.setRequestProperty("Content-Type", "application/xml; charset=utf-8");

                    //getResponseCode sends the request. Assigned to int for optional logging.
                    int responseCode = urlConnection.getResponseCode();

                    //Log response code
                    //Log.d("SC response: ", "" +responseCode);

                } catch (Exception e) {
                    Log.d("SC:", e.getLocalizedMessage());
                }
                finally {
                    urlConnection.disconnect();
                }
                return result; //"Failed to fetch data!";
            }

            @Override
            protected void onPostExecute(Integer result) {


            }
        }
}
