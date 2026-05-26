package com.baingat.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        FirebaseApp.initializeApp(this);
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        new android.os.Handler().postDelayed(() -> {

            FirebaseMessaging.getInstance().getToken()
                    .addOnSuccessListener(token -> {
                        Log.d("FCM_SUCCESS", token);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FCM_FAIL", e.toString());
                    });

        }, 3000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        Window window = getWindow();

        window.setStatusBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }

        SessionManager sessionManager =
                new SessionManager(this);

        new Handler().postDelayed(() -> {

            if (sessionManager.isLoggedIn()) {

                startActivity(
                        new Intent(
                                SplashActivity.this,
                                navbar_utama.class
                        )
                );

            } else {

                startActivity(
                        new Intent(
                                SplashActivity.this,
                                MainActivity.class
                        )
                );
            }

            finish();

        }, 2500);
    }
}