package org.piwigo.helper;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;

public class URLHelper extends AsyncTask<String, Void, String> {

    private AsyncUrlResponse delegate;

    public URLHelper(AsyncUrlResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(String... url) {
        String newUrl = url[0];
        newUrl = getPiwigoBaseFor(newUrl);

        newUrl = newUrl.replaceAll("https://", "").replaceAll("http://", "");
        try {
            if (isHttpsWebsite(newUrl))
                return ("https://" + newUrl);
            else if (isHttpWebsite(newUrl))
                return ("http://" + newUrl);
            else
                return ("https://" + newUrl);
        } catch (IOException e) {
            Log.e("URLHelper", "IOException", e);
            return ("https://" + newUrl);
        }
    }

    /**
     * Remove trailing URL parts specify piwigo-internal pages
     *
     * @param url the url to stip
     * @return the base address of a piwigo URL
     */
    private String getPiwigoBaseFor(String url) {
        String newUrl;

        newUrl = url.replaceAll("(^.*)((?:about|admin|comments|feed|index|notification|picture|profile|ws).php(?:[?]\\/)?(?:.*))$", "$1");

        return newUrl;
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }

    private boolean isHttpsWebsite(String siteUrl) throws IOException {
        URL url = new URL("https://" + siteUrl);
        HttpsURLConnection httpsCon = (HttpsURLConnection) url.openConnection();
        int responseCode = 0;

        if (httpsCon == null)
            return (false);
        try {
            responseCode = httpsCon.getResponseCode();
        } catch (SSLException e) {
            Log.e("URLHelper", "SSLException", e.getCause());
            return (false);
        } catch (UnknownHostException e1) {
            Log.e("URLHelper", "UnknownHostException", e1.getCause());
            return (false);
        }
        httpsCon.disconnect();
        return (responseCode == 200);
    }

    private boolean isHttpWebsite(String siteUrl) throws IOException {
        URL url = new URL("http://" + siteUrl);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
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
        return (responseCode == 200);
    }


    public interface AsyncUrlResponse {
        void processFinish(String newUrl);
    }
}
