package com.example.skripsi;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    String status = "Resiko Sedang";
    String lokasi = "";
    String title = "";

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
        GeoPoint jalanDatang = new GeoPoint(-3.296702248237609, 114.58375641904426);
        GeoPoint jalanPulang = new GeoPoint(-3.2955525424546877, 114.58486568269987);

        map.getController().setZoom(19.0);
        map.getController().setCenter(jalanDatang);
        Marker markerDatang = new Marker(map);
        markerDatang.setPosition(jalanDatang);

        markerDatang.setInfoWindow(new InfoWindow(R.layout.info_window_custom, map){
            @Override
            public void onOpen(Object item){
                TextView tvtittle = mView.findViewById(R.id.tvTittle);
                TextView tvinfotinggi = mView.findViewById(R.id.tvInfoTinggi);
                TextView btndetail = mView.findViewById(R.id.btn_detail);
                tvtittle.setText("Jalan Datang");
                tvinfotinggi.setText(tinggiAir + " Cm");

                btndetail.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), DashboardActivity.class);
                    startActivity(intent);
                });
            }

            @Override
            public void onClose() {}
        });

        markerDatang.setOnMarkerClickListener((m, mapView) -> {
            InfoWindow.closeAllInfoWindowsOn(map);
            map.getController().animateTo(m.getPosition());
            m.showInfoWindow();
            return true;
        });
        map.getOverlays().add(markerDatang);



        Marker markerPulang = new Marker(map);
        markerPulang.setPosition(jalanPulang);

        markerPulang.setInfoWindow(new InfoWindow(R.layout.info_window_custom, map){
            @Override
            public void onOpen(Object item){
                TextView tvtittle = mView.findViewById(R.id.tvTittle);
                TextView tvinfotinggi = mView.findViewById(R.id.tvInfoTinggi);
                TextView btndetail = mView.findViewById(R.id.btn_detail);
                ImageView imageStatus = mView.findViewById(R.id.image_status);

                tvtittle.setText("Jalan Pulang");
                tvinfotinggi.setText(tinggiAir + " Cm");
                imageStatus.setImageResource(R.drawable.jalan_pulang);

                btndetail.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), DashboardActivity.class);
                    startActivity(intent);
                });

            }

            @Override
            public void onClose() {}
        });

        markerPulang.setOnMarkerClickListener((m, mapView) -> {
            InfoWindow.closeAllInfoWindowsOn(map);
            GeoPoint posisi = m.getPosition();
            GeoPoint offset = new GeoPoint(
                    posisi.getLatitude() + 0.0007,
                    posisi.getLongitude()
            );

            map.getController().animateTo(offset);
            m.showInfoWindow();
            return true;
        });
        map.getOverlays().add(markerPulang);

        // Hilang saat klik map
        map.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                org.osmdroid.views.overlay.infowindow.InfoWindow.closeAllInfoWindowsOn(map);
            }
            return false;
        });

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