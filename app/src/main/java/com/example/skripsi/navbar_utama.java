package com.example.skripsi;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
public class navbar_utama extends AppCompatActivity {

    // =========================================
    // VIEW
    // =========================================

    private View highlight;

    private LinearLayout menuDashboard;
    private LinearLayout menuVehicle;
    private LinearLayout menuMaps;
    private LinearLayout menuProfile;

    private ImageView dashboardIcon;
    private ImageView vehicleIcon;
    private ImageView mapsIcon;
    private ImageView profileIcon;

    // =========================================
    // TEXT BOTTOM
    // =========================================

    private TextView textDashboardBottom;
    private TextView textVehicleBottom;
    private TextView textMapsBottom;
    private TextView textProfileBottom;

    // =========================================
    // TEXT ACTIVE
    // =========================================

    private TextView textDashboardActive;
    private TextView textVehicleActive;
    private TextView textMapsActive;
    private TextView textProfileActive;

    // =========================================
    // FRAGMENT
    // =========================================

    private final Fragment dashboard = new DashboardFragment();
    private final Fragment vehicle = new KendaraanFragment();
    private final Fragment maps = new MapsFragment();
    private final Fragment profile = new ProfileFragment();

    private Fragment active = dashboard;

    // =========================================
    // INTERNET
    // =========================================

    private InternetHandler internetHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navbar_utama);

        // =========================================
        // STATUS BAR
        // =========================================

        Window window = getWindow();

        window.setStatusBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }

        // =========================================
        // INIT VIEW
        // =========================================

        highlight = findViewById(R.id.highlight);

        menuDashboard = findViewById(R.id.menu_dashboard);
        menuVehicle = findViewById(R.id.menu_vehicle);
        menuMaps = findViewById(R.id.menu_maps);
        menuProfile = findViewById(R.id.menu_profile);

        dashboardIcon = findViewById(R.id.dashboard_icon);
        vehicleIcon = findViewById(R.id.vehicle_icon);
        mapsIcon = findViewById(R.id.maps_icon);
        profileIcon = findViewById(R.id.profile_icon);

        // =========================================
        // TEXT BOTTOM
        // =========================================

        textDashboardBottom =
                findViewById(R.id.text_dashboard_bottom);

        textVehicleBottom =
                findViewById(R.id.text_vehicle_bottom);

        textMapsBottom =
                findViewById(R.id.text_maps_bottom);

        textProfileBottom =
                findViewById(R.id.text_profile_bottom);

        // =========================================
        // TEXT ACTIVE
        // =========================================

        textDashboardActive =
                findViewById(R.id.text_dashboard_active);

        textVehicleActive =
                findViewById(R.id.text_vehicle_active);

        textMapsActive =
                findViewById(R.id.text_maps_active);

        textProfileActive =
                findViewById(R.id.text_profile_active);

        // =========================================
        // INTERNET HANDLER
        // =========================================

        internetHandler = new InternetHandler(
                this,
                findViewById(R.id.layoutNoInternet),
                findViewById(R.id.btn_reconnect),
                findViewById(R.id.progressReconnect)
        );

        internetHandler.checkInternet();

        // =========================================
        // LOAD FRAGMENT
        // =========================================

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container,
                        profile,
                        "4")
                .hide(profile)

                .add(R.id.fragment_container,
                        maps,
                        "3")
                .hide(maps)

                .add(R.id.fragment_container,
                        vehicle,
                        "2")
                .hide(vehicle)

                .add(R.id.fragment_container,
                        dashboard,
                        "1")
                .commit();

        // =========================================
        // DEFAULT ACTIVE
        // =========================================

        setMenuActive(
                menuDashboard,
                dashboardIcon,
                textDashboardBottom,
                textDashboardActive
        );

        highlight.post(() -> moveHighlight(menuDashboard));

        // =========================================
        // CLICK HOME
        // =========================================

        menuDashboard.setOnClickListener(v -> {

            animateClick(v);

            setMenuActive(
                    menuDashboard,
                    dashboardIcon,
                    textDashboardBottom,
                    textDashboardActive
            );

            switchFragment(dashboard);
        });

        // =========================================
        // CLICK VEHICLE
        // =========================================

        menuVehicle.setOnClickListener(v -> {

            animateClick(v);

            setMenuActive(
                    menuVehicle,
                    vehicleIcon,
                    textVehicleBottom,
                    textVehicleActive
            );

            switchFragment(vehicle);
        });

        // =========================================
        // CLICK MAPS
        // =========================================

        menuMaps.setOnClickListener(v -> {

            animateClick(v);

            setMenuActive(
                    menuMaps,
                    mapsIcon,
                    textMapsBottom,
                    textMapsActive
            );

            switchFragment(maps);
        });

        // =========================================
        // CLICK PROFILE
        // =========================================

        menuProfile.setOnClickListener(v -> {

            animateClick(v);

            setMenuActive(
                    menuProfile,
                    profileIcon,
                    textProfileBottom,
                    textProfileActive
            );

            switchFragment(profile);
        });
    }

    // =========================================
    // RESUME
    // =========================================

    @Override
    protected void onResume() {
        super.onResume();
        internetHandler.startAutoCheck();
    }

    // =========================================
    // PAUSE
    // =========================================

    @Override
    protected void onPause() {
        super.onPause();
        internetHandler.stopAutoCheck();
    }

    // =========================================
    // SWITCH FRAGMENT
    // =========================================

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

    // =========================================
    // MOVE HIGHLIGHT
    // =========================================

    private void moveHighlight(View target) {

        target.post(() -> {

            ViewGroup.LayoutParams params =
                    highlight.getLayoutParams();

            params.width = target.getWidth();

            highlight.setLayoutParams(params);

            highlight.animate()
                    .x(target.getX())
                    .setDuration(320)
                    .setInterpolator(
                            new AccelerateDecelerateInterpolator()
                    )
                    .start();
        });
    }

    // =========================================
    // ACTIVE MENU
    // =========================================

    private void setMenuActive(
            LinearLayout menu,
            ImageView icon,
            TextView textBottom,
            TextView textActive
    ){

        // RESET ALL
        resetMenu(
                menuDashboard,
                dashboardIcon,
                textDashboardBottom,
                textDashboardActive,
                R.drawable.dashboard_icon_putih
        );

        resetMenu(
                menuVehicle,
                vehicleIcon,
                textVehicleBottom,
                textVehicleActive,
                R.drawable.kendaraanputih_icon
        );

        resetMenu(
                menuMaps,
                mapsIcon,
                textMapsBottom,
                textMapsActive,
                R.drawable.maps_icon_putih
        );

        resetMenu(
                menuProfile,
                profileIcon,
                textProfileBottom,
                textProfileActive,
                R.drawable.profile_admin_putih
        );

        // ACTIVE STYLE
        menu.setOrientation(LinearLayout.HORIZONTAL);

        textBottom.setVisibility(View.GONE);

        textActive.setVisibility(View.VISIBLE);

        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams)
                        icon.getLayoutParams();

        params.rightMargin = 10;

        params.bottomMargin = 0;

        icon.setLayoutParams(params);

        // ACTIVE ICON
        if(menu == menuDashboard){

            icon.setImageResource(
                    R.drawable.dashboard_icon
            );

        } else if(menu == menuVehicle){

            icon.setImageResource(
                    R.drawable.kendaraanbiru_icon
            );

        } else if(menu == menuMaps){

            icon.setImageResource(
                    R.drawable.maps_icon
            );

        } else {

            icon.setImageResource(
                    R.drawable.profile_biru_icon
            );
        }

        moveHighlight(menu);
    }

    // =========================================
    // RESET MENU
    // =========================================

    private void resetMenu(
            LinearLayout menu,
            ImageView icon,
            TextView textBottom,
            TextView textActive,
            int iconDrawable
    ){

        menu.setOrientation(LinearLayout.VERTICAL);

        textBottom.setVisibility(View.VISIBLE);

        textActive.setVisibility(View.GONE);

        icon.setImageResource(iconDrawable);

        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams)
                        icon.getLayoutParams();

        params.rightMargin = 0;

        params.bottomMargin = 4;

        icon.setLayoutParams(params);
    }

    // =========================================
    // CLICK ANIMATION
    // =========================================

    private void animateClick(View v){

        v.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(90)
                .withEndAction(() ->
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(90)
                )
                .start();
    }
}