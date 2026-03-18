package com.example.skripsi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class navbar_utama extends AppCompatActivity {

    View highlight;
    LinearLayout menuDashboard, menuMaps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navbar_utama);
        highlight = findViewById(R.id.highlight);
        menuDashboard = findViewById(R.id.menu_dashboard);
        menuMaps = findViewById(R.id.menu_maps);
        TextView textDashboard = findViewById(R.id.text_dashboard);
        TextView textMaps = findViewById(R.id.text_maps);
        ImageView dashboardIcon = findViewById(R.id.dashboard_icon);
        ImageView mapsIcon = findViewById(R.id.maps_icon);

        textDashboard.setVisibility(View.VISIBLE);
        textMaps.setVisibility(View.GONE);


        // default fragment
        loadFragment(new DashboardFragment());

        // tunggu layout siap baru set posisi highlight
        highlight.post(() -> moveHighlight(menuDashboard));

        menuDashboard.setOnClickListener(v -> {
            textDashboard.setVisibility(View.VISIBLE);
            textMaps.setVisibility(View.GONE);
            dashboardIcon.setImageResource(R.drawable.dashboard_icon);
            mapsIcon.setImageResource(R.drawable.maps_icon_putih);

            moveHighlight(menuDashboard);
            loadFragment(new DashboardFragment());
        });

        menuMaps.setOnClickListener(v -> {
            textDashboard.setVisibility(View.GONE);
            textMaps.setVisibility(View.VISIBLE);
            dashboardIcon.setImageResource(R.drawable.dashboard_icon_putih);
            mapsIcon.setImageResource(R.drawable.maps_icon);

            moveHighlight(menuMaps);
            loadFragment(new MapsFragment());
        });
    }

    private void loadFragment(androidx.fragment.app.Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void moveHighlight(View target) {

        target.post(() -> {
            ViewGroup.LayoutParams params = highlight.getLayoutParams();
            params.width = target.getWidth();
            highlight.setLayoutParams(params);

            highlight.animate()
                    .x(target.getX())
                    .setDuration(200)
                    .start();
        });
    }
}