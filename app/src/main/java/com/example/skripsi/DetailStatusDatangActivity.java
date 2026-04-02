package com.example.skripsi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class DetailStatusDatangActivity extends AppCompatActivity {
    private MapView map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_status_datang);

        Configuration.getInstance().load(
                this,
                getSharedPreferences("osmdroid", 0)
        );

        map = findViewById(R.id.mapDetailDatang);
        map.setMultiTouchControls(false);
        map.setClickable(false);
        map.setEnabled(false);
        map.setFocusable(false);
        map.setFocusableInTouchMode(false);
        map.setBuiltInZoomControls(false);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // posisi
        GeoPoint jalanDatang = new GeoPoint(-3.296702248237609, 114.58375641904426);

        map.getController().setZoom(19.0);
        map.getController().setCenter(jalanDatang);
        Marker marker = new Marker(map);
        marker.setPosition(jalanDatang);
        marker.setInfoWindow(null);
        marker.setIcon(getResources().getDrawable(R.drawable.maps_point_icon));
        map.getOverlays().add(marker);

        RecyclerView tabelKendaraan = findViewById(R.id.rvKendaraan);
        tabelKendaraan.setLayoutManager(new LinearLayoutManager(this));

        List<KendaraanTabelModel> list = new ArrayList<>();
        list.add(new KendaraanTabelModel("Pribadi", "Motor", "Honda Beat 150", "Aman"));
        list.add(new KendaraanTabelModel("Orang Lain", "Motor", "Yamaha NMAX 150 XU 450", "Resiko Sedang"));

        KendaraanTabelAdapter adapter = new KendaraanTabelAdapter(list);
        tabelKendaraan.setAdapter(adapter);


        // Bagian Tambah kendaraan
        LinearLayout btnInput = findViewById(R.id.btn_input);
        btnInput.setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.pilih_edit_kendaraan_user);
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            //bikin agak gelap
            dialog.getWindow().setDimAmount(0.8f);

            dialog.show();

        });

    }
}