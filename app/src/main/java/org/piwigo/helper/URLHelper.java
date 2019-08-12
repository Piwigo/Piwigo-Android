package org.piwigo.helper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

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
        url[0] = url[0].replaceAll("https://", "").replaceAll("http://", "");
        try {
            if (isHttpsWebsite(url[0]))
                return ("https://" + url[0]);
            else if (isHttpWebsite(url[0]))
                return ("http://" + url[0]);
            else
                return (null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return (null);
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
            e.printStackTrace();
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
            e.printStackTrace();
            return (false);
        }
        httpCon.disconnect();
        if (responseCode == 200)
            return (true);
        return (false);
    }
}
