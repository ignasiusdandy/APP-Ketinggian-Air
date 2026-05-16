package com.example.skripsi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

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