package org.piwigo.helper;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
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

    /* return the (estimated) networkspeed in kbps */
    public int getNetworkSpeed(Context context) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkCapabilities nc = null;
            nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return nc.getLinkUpstreamBandwidthKbps();
        }else{
            NetworkInfo currentNetwork = getCurrentNetwork(context);
            if(currentNetwork == null) {
                return 1;
            }else {
                int speed = 1;
                switch(currentNetwork.getType()){
                    case ConnectivityManager.TYPE_WIFI:
                        speed = 100000;
                        break;
                    case ConnectivityManager.TYPE_MOBILE:
                        speed = 250;
                        break;
                }
                return speed;
            }
        }

    }
}
