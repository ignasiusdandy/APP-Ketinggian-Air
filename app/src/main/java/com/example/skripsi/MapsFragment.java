package com.example.skripsi;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsFragment extends Fragment {
    private MapView map;
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
        markerDatang.setIcon(getResources().getDrawable(R.drawable.maps_point_icon));

        markerDatang.setInfoWindow(new InfoWindow(R.layout.info_window_custom, map){
            @Override
            public void onOpen(Object item){
                TextView tvtittle = mView.findViewById(R.id.tvTittle);
                TextView tvinfotinggi = mView.findViewById(R.id.tvInfoTinggi);
                TextView tvStatus = mView.findViewById(R.id.tvInfoStatus);
                TextView btndetail = mView.findViewById(R.id.btn_detail);
                TextView tvWaktu = mView.findViewById(R.id.waktu_terakhir);
                ImageView iconStatus = mView.findViewById(R.id.iconStatus);
                ShimmerFrameLayout shimmer = mView.findViewById(R.id.shimmerLayout);
                View content = mView.findViewById(R.id.contentLayout);
                shimmer.setVisibility(View.VISIBLE);
                shimmer.setAlpha(1f);
                content.setAlpha(0f);
                shimmer.startShimmer();

                tvtittle.setText("Jalan Datang");
                loadStatus(tvinfotinggi, tvStatus, iconStatus, "datang", tvWaktu, shimmer, content);

                btndetail.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), DetailStatusDatangActivity.class);
                    startActivity(intent);
                });
            }

            @Override
            public void onClose() {}
        });

        markerDatang.setOnMarkerClickListener((m, mapView) -> {
            InfoWindow.closeAllInfoWindowsOn(map);
            GeoPoint posisi = m.getPosition();
            GeoPoint offset = new GeoPoint(
                    posisi.getLatitude() + 0.0007,
                    posisi.getLongitude()
            );

            map.getController().animateTo(offset);
            m.getInfoWindow().getView().setBackground(null);
            m.getInfoWindow().getView().setPadding(0,0,0,0);
            m.getInfoWindow().getView().setBackgroundColor(android.graphics.Color.TRANSPARENT);
            m.showInfoWindow();
            return true;
        });
        map.getOverlays().add(markerDatang);



        Marker markerPulang = new Marker(map);
        markerPulang.setPosition(jalanPulang);
        markerPulang.setIcon(getResources().getDrawable(R.drawable.maps_point_icon));

        markerPulang.setInfoWindow(new InfoWindow(R.layout.info_window_custom, map){
            @Override
            public void onOpen(Object item){
                TextView tvtittle = mView.findViewById(R.id.tvTittle);
                TextView tvinfotinggi = mView.findViewById(R.id.tvInfoTinggi);
                TextView btndetail = mView.findViewById(R.id.btn_detail);
                ImageView imageStatus = mView.findViewById(R.id.image_status);
                TextView tvStatus = mView.findViewById(R.id.tvInfoStatus);
                TextView tvWaktu = mView.findViewById(R.id.waktu_terakhir);
                ImageView iconStatus = mView.findViewById(R.id.iconStatus);
                ShimmerFrameLayout shimmer = mView.findViewById(R.id.shimmerLayout);
                View content = mView.findViewById(R.id.contentLayout);
                shimmer.setVisibility(View.VISIBLE);
                shimmer.setAlpha(1f);
                content.setAlpha(0f);
                shimmer.startShimmer();


                tvtittle.setText("Jalan Pulang");
                imageStatus.setImageResource(R.drawable.jalan_pulang);
                loadStatus(tvinfotinggi, tvStatus, iconStatus,"pulang", tvWaktu, shimmer, content);

                btndetail.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), DetailStatusPulangActivity.class);
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
            m.getInfoWindow().getView().setBackground(null);
            m.getInfoWindow().getView().setPadding(0,0,0,0);
            m.getInfoWindow().getView().setBackgroundColor(android.graphics.Color.TRANSPARENT);
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


    private void loadStatus(TextView tvinfotinggi, TextView tvStatus, ImageView iconStatus, String tipe, TextView tvWaktu, ShimmerFrameLayout shimmer, View content){
        SessionManager session = new SessionManager(requireContext());
        String token = "Bearer " + session.getToken();
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getStatusUtama(token).enqueue(new Callback<StatusUtamaResponseModel>() {
            @Override
            public void onResponse(Call<StatusUtamaResponseModel> call,
                                   Response<StatusUtamaResponseModel> response) {

                if(response.isSuccessful() && response.body() != null){

                    StatusUtamaResponseModel.Lokasi lokasiData;
                    if (tipe.equals("datang")) {
                        lokasiData = response.body().getDatang();
                    } else {
                        lokasiData = response.body().getPulang();
                    }

                    if(lokasiData != null && lokasiData.getData() != null){
                        showContent(shimmer, content);
                        StatusUtamaResponseModel.Data d = lokasiData.getData();
                        String lastUpdate = d.getLastUpdate();

                        double tinggi = d.getTinggi();
                        String risiko = d.getRisiko();
                        tvinfotinggi.setText(tinggi + " Cm");
                        tvStatus.setText(risiko);
                        tvWaktu.setText("Update terakhir: " + lastUpdate + " WITA");


                        if (risiko.toLowerCase().equals("aman")){
                            tvStatus.setTextColor(getResources().getColor(R.color.hijauaman));
                            iconStatus.setImageResource(R.drawable.aman_icon);
                        } else if (risiko.toLowerCase().equals("waspada")){
                            tvStatus.setTextColor(getResources().getColor(R.color.kuningrendah));
                            iconStatus.setImageResource(R.drawable.resikorendah_icon);
                        }else if (risiko.toLowerCase().equals("resiko rendah")){
                            tvStatus.setTextColor(getResources().getColor(R.color.kuningrendah));
                            iconStatus.setImageResource(R.drawable.resikorendah_icon);
                        } else if (risiko.toLowerCase().equals("resiko sedang")){
                            tvStatus.setTextColor(getResources().getColor(R.color.orensedang));
                            iconStatus.setImageResource(R.drawable.resikosedang_icon);
                        } else if (risiko.toLowerCase().equals("resiko tinggi")){
                            tvStatus.setTextColor(getResources().getColor(R.color.merahtinggi));
                            iconStatus.setImageResource(R.drawable.resikotinggi_icon);
                        }else if (risiko.toLowerCase().equals("bahaya")){
                            tvStatus.setTextColor(getResources().getColor(R.color.merahtinggi));
                            iconStatus.setImageResource(R.drawable.resikotinggi_icon);
                        } else{
                            Log.e("risiko", "Risiko tidak sama");
                        }
                    } else{
                        showContent(shimmer, content);
                        String risiko;
                        risiko = "-";
                        tvinfotinggi.setText("- Cm");
                        tvStatus.setText(risiko);
                        tvWaktu.setText("Update terakhir: -");
                    }
                }
            }

            @Override
            public void onFailure(Call<StatusUtamaResponseModel> call, Throwable t) {
                showContent(shimmer, content);
                Log.e("API_ERROR", "Gagal Ambil Response Status");
            }
        });
    }

    private void showContent(
            ShimmerFrameLayout shimmer,
            View content
    ){

        new Handler().postDelayed(() -> {
            shimmer.stopShimmer();
            shimmer.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction(() -> {

                        shimmer.setVisibility(View.GONE);

                    })
                    .start();

            content.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .start();

        }, 1000);
    }
}