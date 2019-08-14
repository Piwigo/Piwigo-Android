package org.piwigo.helper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.piwigo.ui.login.LoginActivity;
import org.piwigo.ui.main.MainActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

public class URLHelper extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... url) {
        String newUrl = url[0].replaceAll("https://", "").replaceAll("http://", "");
        try {
            if (isHttpsWebsite(newUrl))
                return ("https://" + newUrl);
            else if (isHttpWebsite(newUrl))
                return ("http://" + newUrl);
            else
                return ("https://" + newUrl);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return ("https://" + newUrl);
        }
    }

    private boolean isHttpsWebsite(String siteUrl) throws IOException
    {
        URL url = new URL("https://" + siteUrl);
        HttpsURLConnection httpsCon = (HttpsURLConnection)url.openConnection();
        int responseCode = 0;

        if (httpsCon == null)
            return (false);
        try {
            responseCode = httpsCon.getResponseCode();
        } catch (SSLException e) {
            Log.e("URLHelper", "SSLException", e.getCause());
            return (false);
        }
        catch (UnknownHostException e1) {
            Log.e("URLHelper", "UnknownHostException", e1.getCause());
            return (false);
        }
        httpsCon.disconnect();
        if (responseCode == 200)
            return (true);
        return (false);
    }

    private boolean isHttpWebsite(String siteUrl) throws IOException
    {
        URL url = new URL("http://" + siteUrl);
        HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
        int responseCode = 0;

        if (httpCon == null)
            return (false);
        try {
            responseCode = httpCon.getResponseCode();
        } catch (UnknownHostException e) {
            Log.e("URLHelper", "UnknownHostException", e.getCause());
            return (false);
        }
        httpCon.disconnect();
        if (responseCode == 200)
            return (true);
        return (false);
    }
}
