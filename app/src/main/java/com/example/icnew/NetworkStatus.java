package com.example.icnew;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;

public final class NetworkStatus {
    private NetworkStatus() { }

    public static boolean isOnline(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnected();
    }
}
