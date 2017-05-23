package com.example.hussain.mapmylocation;

/**
 * Created by Hussain on 21-May-17.
 */

        import android.app.AlertDialog;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.provider.Settings;
        import android.widget.Toast;

/**
 * checks for network change
 */

public class NetworkChangeReceiver extends BroadcastReceiver {


    int netnum=0;
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                netnum=1;
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                netnum=2;
            }
        }
        else{
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Unable to connect to internet");
            alert.setMessage("Check if you are connected to a network");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface alert, int which) {
                    alert.dismiss();
                }
            });
            alert.show();
        }
    }
}