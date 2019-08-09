package org.piwigo.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkHelper
{

    public static NetworkHelper INSTANCE;

    public NetworkHelper()
    {
        INSTANCE = this;
    }

    public boolean hasInternet(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo currentNetwork = cm.getActiveNetworkInfo();

        if (currentNetwork != null) {
            if (currentNetwork.isConnected())
                return (true);
        }
        return (false);
    }
}
