package com.example.skripsi;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class MapsFragment extends Fragment {
    private MapView map;
    double tinggiAir = 20;
    String status = "Bahaya";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        Configuration.getInstance().load(
                requireContext(),
                requireContext().getSharedPreferences("osmdroid", 0)
        );

        map = view.findViewById(R.id.map);
        map.setMultiTouchControls(true);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // posisi
        GeoPoint startPoint = new GeoPoint(-3.296702248237609, 114.58375641904426);

        map.getController().setZoom(18.0);
        map.getController().setCenter(startPoint);

        Marker marker = new Marker(map);
        marker.setPosition(startPoint);
        marker.setInfoWindow(new InfoWindow(R.layout.info_window_custom, map){
            @Override
            public void onOpen(Object item){
                TextView tvtittle = mView.findViewById(R.id.tvTittle);
                TextView tvinfo = mView.findViewById(R.id.tvInfo);
                TextView btndetail = mView.findViewById(R.id.btn_detail);

                tvtittle.setText("Jalan Datang");
                tvinfo.setText("Tinggi Air: " + tinggiAir + "cm\n" +
                        "Status: " + status
                );

                btndetail.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), DashboardActivity.class);
                    startActivity(intent);
                });
            }

            @Override
            public void onClose() {}
        });

        marker.setOnMarkerClickListener((m, mapView) -> {
            m.showInfoWindow();
            return true;
        });
        map.getOverlays().add(marker);

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        map.onPause();
    }
}