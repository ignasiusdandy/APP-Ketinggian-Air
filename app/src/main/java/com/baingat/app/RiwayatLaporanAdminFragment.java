package com.baingat.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import android.widget.TextView;
import android.graphics.Color;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class RiwayatLaporanAdminFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvSemuaRiwayat;
    private RiwayatAdapter riwayatAdapter;
    private List<RiwayatModel> allRiwayatList;
    private List<RiwayatModel> displayList;
    
    private TextView chipSemua, chipDiproses, chipDiterima, chipDitolak;
    private TextView activeChip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_riwayat_laporan_admin, container, false);

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        // Setup Chips
        chipSemua = view.findViewById(R.id.chipSemua);

        chipDiproses = view.findViewById(R.id.chipDiproses);
        chipDiterima = view.findViewById(R.id.chipDiterima);
        chipDitolak = view.findViewById(R.id.chipDitolak);
        
        activeChip = chipSemua;
        
        setupChip(chipSemua, "Semua");

        setupChip(chipDiproses, "Diproses");
        setupChip(chipDiterima, "Diterima");
        setupChip(chipDitolak, "Ditolak");

        // Setup RecyclerView
        rvSemuaRiwayat = view.findViewById(R.id.rvSemuaRiwayat);
        rvSemuaRiwayat.setLayoutManager(new LinearLayoutManager(requireContext()));

        allRiwayatList = new ArrayList<>();
        displayList = new ArrayList<>();
        riwayatAdapter = new RiwayatAdapter(displayList);
        rvSemuaRiwayat.setAdapter(riwayatAdapter);

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                fetchRiwayat();
            });
        }

        fetchRiwayat();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchRiwayat();
    }
    
    private void setupChip(TextView chip, String filterType) {
        chip.setOnClickListener(v -> {
            // Deactivate old chip
            if (activeChip != null) {
                activeChip.setBackgroundResource(R.drawable.bg_chip_inactive);
                activeChip.setTextColor(Color.parseColor("#6B7280"));
            }
            // Activate new chip
            activeChip = chip;
            activeChip.setBackgroundResource(R.drawable.bg_chip_active);
            activeChip.setTextColor(Color.WHITE);
            
            // Filter list
            filterList(filterType);
        });
    }
    
    private void filterList(String filterType) {
        displayList.clear();
        if (filterType.equals("Semua")) {
            displayList.addAll(allRiwayatList);
        } else {
            for (RiwayatModel r : allRiwayatList) {
                if (r.getStatus() != null && r.getStatus().equalsIgnoreCase(filterType)) {
                    displayList.add(r);
                } else if (filterType.equals("Diterima") && r.getStatus() != null && r.getStatus().equalsIgnoreCase("Selesai")) {
                    displayList.add(r); // fallback untuk data lama
                } else if (filterType.equals("Ditolak") && r.getStatus() != null && r.getStatus().equalsIgnoreCase("Dibatalkan")) {
                    displayList.add(r); // fallback untuk data lama
                }
            }
        }
        riwayatAdapter.notifyDataSetChanged();
    }

    private void fetchRiwayat() {
        if (!isAdded() || getContext() == null) {
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            return;
        }
        SessionManager sessionManager = new SessionManager(requireContext());
        String token = "Bearer " + sessionManager.getToken();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        allRiwayatList.clear();
        displayList.clear();
        riwayatAdapter.notifyDataSetChanged();

        java.util.concurrent.atomic.AtomicInteger pending = new java.util.concurrent.atomic.AtomicInteger(3);

        Runnable checkDone = () -> {
            if (pending.decrementAndGet() == 0) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (!isAdded() || getActivity() == null) return;
                java.util.Collections.sort(allRiwayatList, (r1, r2) -> {
                    if (r1.getTanggal() == null || r2.getTanggal() == null) return 0;
                    return r2.getTanggal().compareTo(r1.getTanggal());
                });
                getActivity().runOnUiThread(() -> {
                    // Refilter active chip
                    String activeFilter = "Semua";

                    if (activeChip == chipDiproses) activeFilter = "Diproses";
                    else if (activeChip == chipDiterima) activeFilter = "Diterima";
                    else if (activeChip == chipDitolak) activeFilter = "Ditolak";
                    
                    filterList(activeFilter);
                });
            }
        };

        apiService.getSemuaLaporanJalanAdmin(token).enqueue(new retrofit2.Callback<LaporanJalanResponse>() {
            @Override
            public void onResponse(retrofit2.Call<LaporanJalanResponse> call, retrofit2.Response<LaporanJalanResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    for (LaporanJalanResponse.Data d : response.body().getData()) {
                        allRiwayatList.add(new RiwayatModel(d.getId(), "Kondisi Jalan", d.getKeterangan(), d.getCreatedAt() != null ? d.getCreatedAt() : "", d.getStatusLaporan() != null ? d.getStatusLaporan() : "Diproses", d.getCatatanAdmin() != null ? d.getCatatanAdmin() : "Sedang diproses admin", d.getFoto(), d.getNamaUser()));
                    }
                }
                checkDone.run();
            }
            @Override
            public void onFailure(retrofit2.Call<LaporanJalanResponse> call, Throwable t) { checkDone.run(); }
        });

        apiService.getSemuaPermintaanLokasiAdmin(token).enqueue(new retrofit2.Callback<PermintaanLokasiResponse>() {
            @Override
            public void onResponse(retrofit2.Call<PermintaanLokasiResponse> call, retrofit2.Response<PermintaanLokasiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    for (PermintaanLokasiResponse.Data d : response.body().getData()) {
                        allRiwayatList.add(new RiwayatModel(d.getId(), "Permintaan Lokasi", d.getKeterangan(), d.getCreatedAt() != null ? d.getCreatedAt() : "", d.getStatusLaporan() != null ? d.getStatusLaporan() : "Diproses", d.getCatatanAdmin() != null ? d.getCatatanAdmin() : "Sedang diproses admin", d.getFoto(), d.getNamaUser()));
                    }
                }
                checkDone.run();
            }
            @Override
            public void onFailure(retrofit2.Call<PermintaanLokasiResponse> call, Throwable t) { checkDone.run(); }
        });

        apiService.getSemuaLaporanKendaraanAdmin(token).enqueue(new retrofit2.Callback<LaporanKendaraanResponse>() {
            @Override
            public void onResponse(retrofit2.Call<LaporanKendaraanResponse> call, retrofit2.Response<LaporanKendaraanResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    for (LaporanKendaraanResponse.Data d : response.body().getData()) {
                        allRiwayatList.add(new RiwayatModel(d.getId(), "Kendaraan", d.getKeterangan(), d.getCreatedAt() != null ? d.getCreatedAt() : "", d.getStatusLaporan() != null ? d.getStatusLaporan() : "Diproses", d.getCatatanAdmin() != null ? d.getCatatanAdmin() : "Sedang diproses admin", d.getFoto(), d.getNamaUser()));
                    }
                }
                checkDone.run();
            }
            @Override
            public void onFailure(retrofit2.Call<LaporanKendaraanResponse> call, Throwable t) { checkDone.run(); }
        });
    }
}
