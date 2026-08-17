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

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.gms.tasks.Task;
import android.content.IntentSender;

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

        checkAppUpdate();
    }

    private AppUpdateManager appUpdateManager;
    private static final int UPDATE_REQUEST_CODE = 1001;

    private void checkAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                // Arahkan ke halaman custom UpdateActivity
                startActivity(new Intent(SplashActivity.this, UpdateActivity.class));
                finish();
            } else {
                proceedToNextActivity();
            }
        }).addOnFailureListener(e -> {
            Log.e("AppUpdate", "Gagal cek update", e);
            proceedToNextActivity();
        });
    }

    private void proceedToNextActivity() {
        SessionManager sessionManager = new SessionManager(this);

        if (sessionManager.isFirstTimeLaunch()) {
            startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
        } else if (sessionManager.isLoggedIn()) {
            String role = sessionManager.getUserDetails().get(SessionManager.KEY_ROLE);
            if ("Admin".equals(role)) {
                startActivity(new Intent(SplashActivity.this, NavbarAdminActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, navbar_utama.class));
            }
        } else {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appUpdateManager != null) {
            appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    startActivity(new Intent(SplashActivity.this, UpdateActivity.class));
                    finish();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.e("AppUpdate", "Update flow failed! Result code: " + resultCode);
                // Force close app because update is required
                finishAffinity();
            }
        }
    }
}