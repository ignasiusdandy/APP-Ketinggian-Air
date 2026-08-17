package com.baingat.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RiwayatLaporanActivity extends AppCompatActivity {

    private RecyclerView rvSemuaRiwayat;
    private RiwayatAdapter riwayatAdapter;
    private List<RiwayatModel> riwayatList;
    private com.facebook.shimmer.ShimmerFrameLayout shimmerSemuaRiwayat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_laporan);

        // Header back button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Setup RecyclerView
        rvSemuaRiwayat = findViewById(R.id.rvSemuaRiwayat);
        shimmerSemuaRiwayat = findViewById(R.id.shimmerSemuaRiwayat);
        
        rvSemuaRiwayat.setLayoutManager(new LinearLayoutManager(this));

        riwayatList = new ArrayList<>();
        riwayatAdapter = new RiwayatAdapter(riwayatList);
        rvSemuaRiwayat.setAdapter(riwayatAdapter);

        // fetchRiwayat(); dipanggil di onResume saja untuk menghindari duplikasi
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchRiwayat();
    }

    private void fetchRiwayat() {
        SessionManager sessionManager = new SessionManager(this);
        String token = "Bearer " + sessionManager.getToken();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        riwayatList.clear();
        riwayatAdapter.notifyDataSetChanged();
        
        if (shimmerSemuaRiwayat != null) {
            shimmerSemuaRiwayat.setVisibility(View.VISIBLE);
            shimmerSemuaRiwayat.startShimmer();
            rvSemuaRiwayat.setVisibility(View.GONE);
        }

        java.util.concurrent.atomic.AtomicInteger pending = new java.util.concurrent.atomic.AtomicInteger(3);

        Runnable checkDone = () -> {
            if (pending.decrementAndGet() == 0) {
                java.util.Collections.sort(riwayatList, (r1, r2) -> {
                    if (r1.getTanggal() == null || r2.getTanggal() == null) return 0;
                    return r2.getTanggal().compareTo(r1.getTanggal());
                });
                runOnUiThread(() -> {
                    if (shimmerSemuaRiwayat != null) {
                        shimmerSemuaRiwayat.stopShimmer();
                        shimmerSemuaRiwayat.setVisibility(View.GONE);
                    }
                    rvSemuaRiwayat.setVisibility(View.VISIBLE);
                    riwayatAdapter.notifyDataSetChanged();
                });
            }
        };

        apiService.getSemuaLaporanJalan(token).enqueue(new retrofit2.Callback<LaporanJalanResponse>() {
            @Override
            public void onResponse(retrofit2.Call<LaporanJalanResponse> call, retrofit2.Response<LaporanJalanResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    for (LaporanJalanResponse.Data d : response.body().getData()) {
                        riwayatList.add(new RiwayatModel(d.getId(), "Kondisi Jalan", d.getKeterangan(), d.getCreatedAt() != null ? d.getCreatedAt() : "", d.getStatusLaporan() != null ? d.getStatusLaporan() : "Diproses", d.getCatatanAdmin() != null ? d.getCatatanAdmin() : "Sedang diproses admin", d.getFoto(), d.getNamaUser()));
                    }
                }
                checkDone.run();
            }
            @Override
            public void onFailure(retrofit2.Call<LaporanJalanResponse> call, Throwable t) { checkDone.run(); }
        });

        apiService.getSemuaPermintaanLokasi(token).enqueue(new retrofit2.Callback<PermintaanLokasiResponse>() {
            @Override
            public void onResponse(retrofit2.Call<PermintaanLokasiResponse> call, retrofit2.Response<PermintaanLokasiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    for (PermintaanLokasiResponse.Data d : response.body().getData()) {
                        riwayatList.add(new RiwayatModel(d.getId(), "Permintaan Lokasi", d.getKeterangan(), d.getCreatedAt() != null ? d.getCreatedAt() : "", d.getStatusLaporan() != null ? d.getStatusLaporan() : "Diproses", d.getCatatanAdmin() != null ? d.getCatatanAdmin() : "Sedang diproses admin", d.getFoto(), d.getNamaUser()));
                    }
                }
                checkDone.run();
            }
            @Override
            public void onFailure(retrofit2.Call<PermintaanLokasiResponse> call, Throwable t) { checkDone.run(); }
        });

        apiService.getSemuaLaporanKendaraan(token).enqueue(new retrofit2.Callback<LaporanKendaraanResponse>() {
            @Override
            public void onResponse(retrofit2.Call<LaporanKendaraanResponse> call, retrofit2.Response<LaporanKendaraanResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    for (LaporanKendaraanResponse.Data d : response.body().getData()) {
                        riwayatList.add(new RiwayatModel(d.getId(), "Kendaraan", d.getKeterangan(), d.getCreatedAt() != null ? d.getCreatedAt() : "", d.getStatusLaporan() != null ? d.getStatusLaporan() : "Diproses", d.getCatatanAdmin() != null ? d.getCatatanAdmin() : "Sedang diproses admin", d.getFoto(), d.getNamaUser()));
                    }
                }
                checkDone.run();
            }
            @Override
            public void onFailure(retrofit2.Call<LaporanKendaraanResponse> call, Throwable t) { checkDone.run(); }
        });
    }
}
