package com.nexturninc.biyaheplus_driver;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by imbisibol on 2/17/2016.
 */
public class Common {

    private static ImageLoader ImageLoaderInstance;

    String APIURL = "";
    public void setAPIURL(String url) {
        APIURL = url;
    }

    public String getAPIURL() {
        return APIURL;
    }


    public JSONObject PostAPI(JSONObject jsonParam, String apiMethod) {
        String result = "";
        StringBuilder sb = new StringBuilder();
        JSONObject jsonObject = null;

        URL url = null;
        try {
            url = new URL(APIURL + apiMethod);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.connect();

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(jsonParam.toString());
            out.close();

            int HttpResult = conn.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                jsonObject = new JSONObject(sb.toString());

                System.out.println("" + sb.toString());

            } else {
                System.out.println(conn.getResponseMessage());
            }
        }
        catch (Exception ex) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("success", true);
                jsonObject.put("responseMessage", ex.getMessage());
            }
            catch(Exception ex2)
            {

            }
        }



        return jsonObject;
    }

    public JSONObject PutAPI(JSONObject jsonParam, String apiMethod) {
        String result = "";
        StringBuilder sb = new StringBuilder();
        JSONObject jsonObject = null;

        URL url = null;
        try {
            url = new URL(APIURL + apiMethod);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.connect();

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(jsonParam.toString());
            out.close();

            int HttpResult = conn.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                jsonObject = new JSONObject(sb.toString());

                System.out.println("" + sb.toString());

            } else {
                System.out.println(conn.getResponseMessage());
            }
        }
        catch (Exception ex) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("success", true);
                jsonObject.put("responseMessage", ex.getMessage());
            }
            catch(Exception ex2)
            {

            }
        }

        return jsonObject;
    }

    public JSONObject GetAPI(String apiMethod) {
        String result = "";
        StringBuilder sb = new StringBuilder();
        JSONObject jsonObject = null;

        URL url = null;
        try {
            url = new URL(APIURL + apiMethod);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.connect();

            int HttpResult = conn.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                jsonObject = new JSONObject(sb.toString());

                System.out.println("" + sb.toString());

            } else {
                System.out.println(conn.getResponseMessage());
            }
        }
        catch (Exception ex) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("success", true);
                jsonObject.put("responseMessage", ex.getMessage());
            }
            catch(Exception ex2)
            {

            }
        }

        return jsonObject;
    }

    public static ImageLoader getImageLoader(Context context) {

        if(ImageLoaderInstance == null) {
            DisplayImageOptions defaultOptions =
                    new DisplayImageOptions.Builder()
                            .cacheOnDisk(true)
                            .cacheInMemory(true)
                            .build();

            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).defaultDisplayImageOptions(defaultOptions).build();
            ImageLoaderInstance = ImageLoader.getInstance();

            ImageLoaderInstance.init(config);
        }

        return ImageLoaderInstance;
    }

    public static boolean GetInternetConnectivity(ConnectivityManager connectivityManager){

        boolean connected = false;
        //ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;


        return connected;

    }
}
