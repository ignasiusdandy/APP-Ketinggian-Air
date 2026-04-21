package com.example.skripsi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

public class navbar_utama extends AppCompatActivity {

    View highlight;
    LinearLayout menuDashboard, menuMaps, menuProfile, navbar;

    TextView textDashboard, textMaps, textProfile;
    ImageView dashboardIcon, mapsIcon, profileIcon;
    LinearLayout layoutNoInternet;
    MaterialButton btnReconnect;

    Fragment dashboard = new DashboardFragment();
    Fragment maps = new MapsFragment();
    Fragment profile = new ProfileFragment();
    Fragment active = dashboard;
    InternetHandler internetHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navbar_utama);

        // INIT
        highlight = findViewById(R.id.highlight);
        menuDashboard = findViewById(R.id.menu_dashboard);
        menuMaps = findViewById(R.id.menu_maps);
        menuProfile = findViewById(R.id.menu_profile);

        textDashboard = findViewById(R.id.text_dashboard);
        textMaps = findViewById(R.id.text_maps);
        textProfile = findViewById(R.id.text_profile);

        dashboardIcon = findViewById(R.id.dashboard_icon);
        mapsIcon = findViewById(R.id.maps_icon);
        profileIcon = findViewById(R.id.profile_icon);

        // Cek internet
//        navbar = findViewById(R.id.bottomNav);
        internetHandler = new InternetHandler(
                this,
                findViewById(R.id.layoutNoInternet),
                findViewById(R.id.btn_reconnect),
                findViewById(R.id.progressReconnect)
        );
        internetHandler.checkInternet();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, profile, "3").hide(profile)
                .add(R.id.fragment_container, maps, "2").hide(maps)
                .add(R.id.fragment_container, dashboard, "1")
                .commit();

        setActiveMenu(menuDashboard, textDashboard, dashboardIcon);

        highlight.post(() -> moveHighlight(menuDashboard));

        // CLICK
        menuDashboard.setOnClickListener(v -> {
            animateClick(v);
            setActiveMenu(menuDashboard, textDashboard, dashboardIcon);
            switchFragment(dashboard);
        });

        menuMaps.setOnClickListener(v -> {
            animateClick(v);
            setActiveMenu(menuMaps, textMaps, mapsIcon);
            switchFragment(maps);
        });

        menuProfile.setOnClickListener(v -> {
            animateClick(v);
            setActiveMenu(menuProfile, textProfile, profileIcon);
            switchFragment(profile);
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        internetHandler.startAutoCheck();
    }

    @Override
    protected void onPause() {
        super.onPause();
        internetHandler.stopAutoCheck();
    }

    private void switchFragment(Fragment target) {
        if (active == target) return;

        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(
                        R.anim.fade_in_smooth,
                        R.anim.fade_out_smooth
                )
                .hide(active)
                .show(target)
                .commit();

        active = target;
    }

    private void moveHighlight(View target) {
        target.post(() -> {
            ViewGroup.LayoutParams params = highlight.getLayoutParams();
            params.width = target.getWidth();
            highlight.setLayoutParams(params);

            highlight.animate()
                    .x(target.getX())
                    .setDuration(350)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        });
    }

    private void setActiveMenu(LinearLayout selectedMenu, TextView selectedText, ImageView selectedIcon) {

        textDashboard.setVisibility(View.GONE);
        textMaps.setVisibility(View.GONE);
        textProfile.setVisibility(View.GONE);

        dashboardIcon.setImageResource(R.drawable.dashboard_icon_putih);
        mapsIcon.setImageResource(R.drawable.maps_icon_putih);
        profileIcon.setImageResource(R.drawable.profile_icon_putih);

        animateFade(selectedText);
        selectedText.setVisibility(View.VISIBLE);

        if (selectedMenu == menuDashboard) {
            dashboardIcon.setImageResource(R.drawable.dashboard_icon);
        } else if (selectedMenu == menuMaps) {
            mapsIcon.setImageResource(R.drawable.maps_icon);
        } else {
            profileIcon.setImageResource(R.drawable.profile_icon);
        }

        moveHighlight(selectedMenu);
    }

    private void animateFade(View view) {
        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .setDuration(200)
                .start();
    }

    private void animateClick(View v) {
        v.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction(() ->
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100)
                )
                .start();
    }
}