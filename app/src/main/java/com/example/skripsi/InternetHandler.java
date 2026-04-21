package com.example.skripsi;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.view.View;

import com.google.android.material.button.MaterialButton;

// Handler ini kita gunakan untuk digunakan saat tidak ada internet
// classnya itu dari layout_no_internet
public class InternetHandler {
    private Context context;
    private View layoutNoInternet, progress;
    private MaterialButton btnReconnect;

    private Handler handler = new Handler();
    public InternetHandler(Context context, View layoutNoInternet, MaterialButton btnReconnect, View progress){
        this.context = context;
        this.layoutNoInternet = layoutNoInternet;
        this.btnReconnect = btnReconnect;
        this.progress = progress;

        setupButton();
    }

    public boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm != null){
            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                return capabilities != null &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            } else{
                android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            }
        }
        return false;
    }

    public void checkInternet(){
        if(!isConnected()){
            layoutNoInternet.setVisibility(View.VISIBLE);
//            navbar.setVisibility(View.GONE);
        } else{
            layoutNoInternet.setVisibility(View.GONE);
//            navbar.setVisibility(View.VISIBLE);
        }
    }

    private void setupButton() {
        btnReconnect.setOnClickListener(v -> {
            progress.setVisibility(View.VISIBLE);
            btnReconnect.setVisibility(View.GONE);

            new Handler().postDelayed(() -> {
                checkInternet();
                progress.setVisibility(View.GONE);
                btnReconnect.setVisibility(View.VISIBLE);
                btnReconnect.setEnabled(true);
            }, 2000);
        });
    }

    public void startAutoCheck() {
        handler.post(checkRunnable);
    }

    public void stopAutoCheck() {
        handler.removeCallbacks(checkRunnable);
    }

    private Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            checkInternet();
            handler.postDelayed(this, 3000);
        }
    };
}
