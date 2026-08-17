package com.baingat.app;

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
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.widget.Toast;
import android.location.LocationManager;
import android.content.Context;
import android.os.SystemClock;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsFragment extends Fragment {
    private MapView map;
    String lokasi = "";
    String title = "";
    private long lastClickTime = 0;

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

        map.getController().setZoom(19.0);
        // Set default center agar peta tidak ngeblank di awal
        GeoPoint defaultCenter = new GeoPoint(-3.2967022, 114.5837564);
        map.getController().setCenter(defaultCenter);

        // Fetch dynamic points
        loadMapMarkers();

        // Location overlay
        MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), map);
        myLocationOverlay.enableMyLocation();
        map.getOverlays().add(myLocationOverlay);

        FloatingActionButton fabMyLocation = view.findViewById(R.id.fab_my_location);
        fabMyLocation.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - lastClickTime < 2000) {
                return; // Cegah klik berulang terlalu cepat
            }
            lastClickTime = SystemClock.elapsedRealtime();

            LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGpsEnabled && !isNetworkEnabled) {
                new android.app.AlertDialog.Builder(requireContext())
                    .setMessage("Lokasi Anda belum aktif. Mohon aktifkan lokasi di pengaturan untuk menggunakan fitur ini.")
                    .setPositiveButton("Pengaturan", (paramDialogInterface, paramInt) -> {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    })
                    .setNegativeButton("Batal", null)
                    .show();
                return;
            }

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            } else {
                if (myLocationOverlay.getMyLocation() != null) {
                    map.getController().animateTo(myLocationOverlay.getMyLocation());
                    map.getController().setZoom(19.0);
                } else {
                    android.util.Log.d("AppLog", String.valueOf("Mencari lokasi..."));
                    myLocationOverlay.enableFollowLocation();
                }
            }
        });

        // Hilang saat klik map
        View legendLayout = view.findViewById(R.id.legend_layout);
        map.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                org.osmdroid.views.overlay.infowindow.InfoWindow.closeAllInfoWindowsOn(map);
                if (legendLayout != null) {
                    legendLayout.animate().alpha(1f).setDuration(200).start();
                }
            }
            return false;
        });

        return view;
    }

    private void loadMapMarkers() {
        if (!isAdded() || getContext() == null) return;
        SessionManager session = new SessionManager(requireContext());
        String token = "Bearer " + session.getToken();
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getStatusUtama(token, "").enqueue(new Callback<StatusUtamaResponseModel>() {
            @Override
            public void onResponse(Call<StatusUtamaResponseModel> call1, Response<StatusUtamaResponseModel> responseStatus) {
                if (!isAdded() || getContext() == null) return;
                
                java.util.Map<String, String> riskMap = new java.util.HashMap<>();
                if (responseStatus.isSuccessful() && responseStatus.body() != null) {
                    StatusUtamaResponseModel resStatus = responseStatus.body();
                    if (resStatus.isStatus() && resStatus.getData() != null) {
                        for (StatusUtamaResponseModel.Data d : resStatus.getData()) {
                            riskMap.put(d.getIdLokasi(), d.getRisiko() != null ? d.getRisiko().toLowerCase() : "");
                        }
                    }
                }

                api.getLokasi(token).enqueue(new Callback<LokasiResponseModel>() {
                    @Override
                    public void onResponse(Call<LokasiResponseModel> call, Response<LokasiResponseModel> response) {
                        if (!isAdded() || getContext() == null) return;

                        if (response.isSuccessful() && response.body() != null) {
                            LokasiResponseModel res = response.body();

                            if (res.isStatus() && res.getData() != null && !res.getData().isEmpty()) {
                                boolean isFirst = true;

                                for (LokasiResponseModel.Data data : res.getData()) {
                                    if ("aktif".equalsIgnoreCase(data.getStatusAlat()) && data.getLatitude() != null && data.getLongitude() != null) {
                                        try {
                                            double lat = Double.parseDouble(data.getLatitude());
                                            double lng = Double.parseDouble(data.getLongitude());
                                            GeoPoint geoPoint = new GeoPoint(lat, lng);

                                            if (isFirst) {
                                                map.getController().setCenter(geoPoint);
                                                isFirst = false;
                                            }

                                            Marker marker = new Marker(map);
                                            marker.setPosition(geoPoint);
                                            
                                            String risiko = riskMap.get(data.getIdLokasi());
                                            if (risiko == null) risiko = "";
                                            if (risiko.contains("bahaya") || risiko.contains("tinggi")) {
                                                marker.setIcon(getResources().getDrawable(R.drawable.maps_point_red));
                                            } else if (risiko.contains("waspada") || risiko.contains("sedang") || risiko.contains("rendah")) {
                                                marker.setIcon(getResources().getDrawable(R.drawable.maps_point_yellow));
                                            } else if (risiko.contains("aman")) {
                                                marker.setIcon(getResources().getDrawable(R.drawable.maps_point_green));
                                            } else {
                                                marker.setIcon(getResources().getDrawable(R.drawable.maps_point_icon));
                                            }

                                            marker.setInfoWindow(new InfoWindow(R.layout.info_window_custom, map) {
                                                @Override
                                                public void onOpen(Object item) {
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

                                                    tvtittle.setText(data.getNamaLokasi());
                                                    
                                                    // Image placeholder sesuai lokasi
                                                    if (data.getNamaLokasi().toLowerCase().contains("pulang")) {
                                                        if (imageStatus != null) imageStatus.setImageResource(R.drawable.jalan_pulang);
                                                    } else {
                                                        if (imageStatus != null) imageStatus.setImageResource(R.drawable.jalan_datang);
                                                    }

                                                    loadStatus(tvinfotinggi, tvStatus, iconStatus, data.getIdLokasi(), tvWaktu, shimmer, content);

                                                    btndetail.setOnClickListener(v -> {
                                                        Intent intent = new Intent(getActivity(), DetailStatusActivity.class);
                                                        intent.putExtra("ID_LOKASI", data.getIdLokasi());
                                                        intent.putExtra("NAMA_LOKASI", data.getNamaLokasi());
                                                        intent.putExtra("LATITUDE", String.valueOf(lat));
                                                        intent.putExtra("LONGITUDE", String.valueOf(lng));
                                                        startActivity(intent);
                                                    });
                                                }

                                                @Override
                                                public void onClose() {}
                                            });

                                            marker.setOnMarkerClickListener((m, mapView) -> {
                                                InfoWindow.closeAllInfoWindowsOn(map);
                                                
                                                View legend = getView() != null ? getView().findViewById(R.id.legend_layout) : null;
                                                if (legend != null) {
                                                    legend.animate().alpha(0f).setDuration(200).start();
                                                }
                                                
                                                GeoPoint posisi = m.getPosition();
                                                GeoPoint offset = new GeoPoint(
                                                        posisi.getLatitude() + 0.0007,
                                                        posisi.getLongitude()
                                                );

                                                map.getController().animateTo(offset);
                                                m.getInfoWindow().getView().setBackground(null);
                                                m.getInfoWindow().getView().setPadding(0, 0, 0, 0);
                                                m.getInfoWindow().getView().setBackgroundColor(android.graphics.Color.TRANSPARENT);
                                                m.showInfoWindow();
                                                return true;
                                            });
                                            map.getOverlays().add(marker);

                                        } catch (NumberFormatException e) {
                                            Log.e("MAPS", "Format kordinat salah: " + e.getMessage());
                                        }
                                    }
                                }
                                map.invalidate();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<LokasiResponseModel> call, Throwable t) {
                        Log.e("MAPS", "Gagal load lokasi dinamis");
                    }
                });
            }
            @Override
            public void onFailure(Call<StatusUtamaResponseModel> call1, Throwable t) {
                Log.e("MAPS", "Gagal load status utama");
            }
        });
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


    private void loadStatus(TextView tvinfotinggi, TextView tvStatus, ImageView iconStatus, String idLokasi, TextView tvWaktu, ShimmerFrameLayout shimmer, View content){
        if (!isAdded() || getContext() == null) return;
        SessionManager session = new SessionManager(requireContext());
        String token = "Bearer " + session.getToken();
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getStatusUtama(token, "").enqueue(new Callback<StatusUtamaResponseModel>() {
            @Override
            public void onResponse(Call<StatusUtamaResponseModel> call,
                                   Response<StatusUtamaResponseModel> response) {

                if (!isAdded() || getContext() == null) return;
                if(response.isSuccessful() && response.body() != null){

                    StatusUtamaResponseModel res = response.body();

                    if(res.isStatus() && res.getData() != null && !res.getData().isEmpty()){
                        showContent(shimmer, content);
                        
                        StatusUtamaResponseModel.Data d = null;
                        for(StatusUtamaResponseModel.Data data : res.getData()) {
                            if(data.getIdLokasi() != null && data.getIdLokasi().equals(idLokasi)) {
                                d = data;
                                break;
                            }
                        }
                        if (d == null) d = res.getData().get(0);
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
                if (!isAdded() || getContext() == null) return;
                Log.e("API_ERROR", "Gagal Ambil Response Status");
            }
        });
    }

    private void showContent(
            ShimmerFrameLayout shimmer,
            View content
    ){

        new Handler().postDelayed(() -> {
            if (!isAdded() || getContext() == null) return;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                android.util.Log.d("AppLog", String.valueOf("Izin lokasi diberikan, silakan klik tombol lokasi lagi"));
                Toast.makeText(requireContext(), "Izin lokasi diberikan, silakan klik tombol lokasi lagi", Toast.LENGTH_SHORT).show();
            } else {
                android.util.Log.d("AppLog", String.valueOf("Izin lokasi ditolak"));
                new android.app.AlertDialog.Builder(requireContext())
                    .setMessage("Aplikasi membutuhkan izin akses lokasi untuk menemukan posisi Anda. Mohon aktifkan izin lokasi di Pengaturan Aplikasi.")
                    .setPositiveButton("Pengaturan", (dialog, which) -> {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        android.net.Uri uri = android.net.Uri.fromParts("package", requireContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .setNegativeButton("Batal", null)
                    .show();
            }
        }
    }
}