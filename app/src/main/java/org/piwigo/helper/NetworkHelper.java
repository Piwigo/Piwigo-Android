package org.piwigo.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkHelper {

    public static NetworkHelper INSTANCE;

    public NetworkHelper() {
        INSTANCE = this;
    }

    public NetworkInfo getCurrentNetwork(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();

        return (network);
    }

    public boolean hasInternet(Context context) {
        NetworkInfo currentNetwork = getCurrentNetwork(context);

        if (currentNetwork != null) {
            return (currentNetwork.isConnected());
        }
        return (false);
    }

    public int getNetworkType(Context context)
    {
        NetworkInfo currentNetwork = getCurrentNetwork(context);

        if (currentNetwork != null) {
            return (currentNetwork.getType());
        }
        return (0);
    }
}
