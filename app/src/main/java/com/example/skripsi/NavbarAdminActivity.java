package com.example.skripsi;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavbarAdminActivity extends AppCompatActivity {
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_navbar);

        bottomNav = findViewById(R.id.bottomNav);

        // default fragment
        loadFragment(new DashboardAdminFragment());

        bottomNav.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new DashboardAdminFragment();

            } else if (item.getItemId() == R.id.nav_kendaraan) {
                selectedFragment = new KendaraanAdminFragment();

            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragmentAdmin();
            }

            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
