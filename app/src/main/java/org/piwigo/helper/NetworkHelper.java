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
        //The usage of getActiveNetworkInfo() is deprecated since API 28
        //The proper way would be to use getActiveNetwork, but it is only supported since API 23
        //So for now we may want to keep it like this
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
        return (-1); //The symbolic name for "0" is MOBILE, if we end this return it means that there is no internet connection so it should be -1
    }
}
