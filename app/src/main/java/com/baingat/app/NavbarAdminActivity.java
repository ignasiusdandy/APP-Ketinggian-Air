package com.baingat.app;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavbarAdminActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    // Buat fragment sekali saja
    private final Fragment dashboardFragment = new DashboardAdminFragment();
    private final Fragment kendaraanFragment = new KendaraanAdminFragment();
    private final Fragment laporanFragment = new RiwayatLaporanAdminFragment();
    private final Fragment profileFragment = new ProfileFragmentAdmin();

    private Fragment activeFragment = dashboardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_navbar);
        Log.d("ADMIN", "NavbarAdminActivity Dibuka");

        Window window = getWindow();

        window.setStatusBarColor(Color.TRANSPARENT);

        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        bottomNav = findViewById(R.id.bottomNav);

        // Fragment pertama
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameContainer, dashboardFragment)
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {

            Fragment targetFragment = null;

            if (item.getItemId() == R.id.nav_home) {

                targetFragment = dashboardFragment;

            } else if (item.getItemId() == R.id.nav_kendaraan) {

                targetFragment = kendaraanFragment;

            } else if (item.getItemId() == R.id.nav_laporan) {

                targetFragment = laporanFragment;

            } else if (item.getItemId() == R.id.nav_profile) {

                targetFragment = profileFragment;
            }

            // Jika menu yang sama diklik lagi, abaikan
            if (targetFragment != null &&
                    activeFragment.getClass() == targetFragment.getClass()) {
                return true;
            }

            return switchFragment(targetFragment);
        });
    }

    private boolean switchFragment(Fragment targetFragment) {

        if (targetFragment == null) return false;

        try {

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameContainer, targetFragment)
                    .commit();

            activeFragment = targetFragment;

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }
}