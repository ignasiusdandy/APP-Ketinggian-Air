package com.baingat.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {
    private SessionManager sessionManager;
    private TextView tvPerkenalanNama, tvKendaraan, tvSatuanKiri;
    private ApiService apiService;
    private LineChart lineChart;
    private ImageView btnKeluar;
    private RecyclerView rvStatusJalan;
    private LinearLayout layoutEmptyState;
    private StatusJalanAdapter statusAdapter;
    private String selectedIdLokasi = "";
    private List<String> activeLokasiIds = new ArrayList<>();
    private Map<String, LokasiResponseModel.Data> activeLokasiMap = new HashMap<>();
    boolean isDebugMode = true;
    private ShimmerFrameLayout shimmerLayout;
    private ScrollView scrollView2;
    long startTime;

    // Ini untuk refresh tiap 5 menit
    private final android.os.Handler handler = new android.os.Handler();
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("REFRESH_DEBUG", "Runnable jalan");
            refreshData();
            loadChartData();
            fetchActiveLokasiAndLoadStatus();

            // ulangi setiap 10 menit
            handler.postDelayed(this, 10 * 60 * 1000);

            // // debug
            // handler.postDelayed(this, 5000);
        }
    };

    public DashboardFragment() {
        super(R.layout.fragment_dashboard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = ApiClient.getClient().create(ApiService.class);
        tvSatuanKiri = view.findViewById(R.id.tvSatuanKiri);

        // ini untuk debug
        startTime = System.currentTimeMillis();

        // Untuk mendapatkan session managernya
        sessionManager = new SessionManager(requireContext());
        String nama = sessionManager.getUserDetails().get(SessionManager.KEY_NAMA);
        String token = sessionManager.getToken();
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        scrollView2 = view.findViewById(R.id.scrollView2);
        scrollView2.setAlpha(0f);
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();

        androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                refreshData();
                loadChartData();
                fetchActiveLokasiAndLoadStatus();
                new android.os.Handler().postDelayed(() -> {
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            });
        }

        // ini untuk perkenalan nama
        tvPerkenalanNama = view.findViewById(R.id.haiNamaUser);
        if (tvPerkenalanNama != null)
            tvPerkenalanNama.setText(nama);

        // ini untuk waktu sekarang
        TextView tvselamatwaktu = view.findViewById(R.id.selamatWaktu);
        if (tvselamatwaktu != null) {
            Calendar calendar = Calendar.getInstance();
            int jam = calendar.get(Calendar.HOUR_OF_DAY);

            String ucapan;

            if (jam >= 4 && jam < 10) {
                ucapan = "Selamat Pagi";
            } else if (jam >= 10 && jam < 15) {
                ucapan = "Selamat Siang";
            } else if (jam >= 15 && jam < 18) {
                ucapan = "Selamat Sore";
            } else if (jam >= 18 && jam < 19) {
                ucapan = "Selamat Petang";
            } else {
                ucapan = "Selamat Malam";
            }
            tvselamatwaktu.setText(ucapan);
        }

        // ini untuk kendaraaan utama
        tvKendaraan = view.findViewById(R.id.kendaraanUtama);
        apiService.getKendaraanUtama("Bearer " + token)
                .enqueue(new Callback<KendaraanUtamaResponseModel>() {
                    @Override
                    public void onResponse(Call<KendaraanUtamaResponseModel> call,
                            Response<KendaraanUtamaResponseModel> response) {
                        if (response.isSuccessful() && response.body() != null) {

                            KendaraanUtamaResponseModel res = response.body();

                            if (res.isStatus() && res.getData() != null) {
                                String kendaraan = res.getData().getJenisMotor() + " " +
                                        res.getData().getModelMotor();

                                tvKendaraan.setText(kendaraan);
                            } else {
                                tvKendaraan.setText(res.getMessage());
                            }

                        } else {
                            tvKendaraan.setText("Tidak ada kendaraan utama");
                        }
                    }

                    @Override
                    public void onFailure(Call<KendaraanUtamaResponseModel> call, Throwable t) {
                        tvKendaraan.setText("Error koneksi");
                    }
                });

        // ini untuk chart
        lineChart = view.findViewById(R.id.lineChart);
        loadChartData();

        // ini untuk on click ke detail status (dihapus karena dinamis lewat adapter)

        rvStatusJalan = view.findViewById(R.id.rvStatusJalan);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        rvStatusJalan.setLayoutManager(new LinearLayoutManager(requireContext()));
        statusAdapter = new StatusJalanAdapter(requireContext(), new ArrayList<>());
        rvStatusJalan.setAdapter(statusAdapter);

        // Ini untuk Pilih Lokasi Filter
        LinearLayout btnPilihLokasi = view.findViewById(R.id.btnPilihLokasi);
        TextView tvLokasiTerpilih = view.findViewById(R.id.tvLokasiTerpilih);
        TextView tvInfoBanner = view.findViewById(R.id.tvInfoBanner);

        if (btnPilihLokasi != null) {
            btnPilihLokasi.setOnClickListener(v -> {
                showLocationPicker(tvLokasiTerpilih, tvInfoBanner);
            });
        }

        fetchActiveLokasiAndLoadStatus();

        btnKeluar = view.findViewById(R.id.btnLogout);
        if (btnKeluar != null) {
            btnKeluar.setOnClickListener(v -> {
                // Ambil token dan KTP HP dari SessionManager
                String jwtToken = "Bearer " + sessionManager.getToken();
                String deviceId = sessionManager.getDeviceId();

                ApiService apiService = ApiClient.getClient().create(ApiService.class);

                // Kirim permintaan hapus token ke Backend
                apiService.logoutUser(jwtToken, deviceId).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        selesaikanLogout();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("LOGOUT_API", "Gagal koneksi ke server: " + t.getMessage());
                        selesaikanLogout();
                    }
                });
            });
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        handler.removeCallbacks(refreshRunnable);
        handler.postDelayed(refreshRunnable, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshRunnable);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            refreshData();
            loadChartData();
            fetchActiveLokasiAndLoadStatus();
        }
    }

    private void refreshData() {
        SessionManager sm = new SessionManager(requireContext());
        String namaBaru = sm.getUserDetails().get(SessionManager.KEY_NAMA);
        tvPerkenalanNama.setText(namaBaru);

        String token = sm.getToken();
        // ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getKendaraanUtama("Bearer " + token)
                .enqueue(new Callback<KendaraanUtamaResponseModel>() {
                    @Override
                    public void onResponse(Call<KendaraanUtamaResponseModel> call,
                            Response<KendaraanUtamaResponseModel> response) {
                        if (response.isSuccessful() && response.body() != null &&
                                response.body().isStatus() && response.body().getData() != null) {

                            String kendaraan = response.body().getData().getJenisMotor() + " " +
                                    response.body().getData().getModelMotor();

                            tvKendaraan.setText(kendaraan);
                        }
                    }

                    @Override
                    public void onFailure(Call<KendaraanUtamaResponseModel> call, Throwable t) {
                        tvKendaraan.setText("Error koneksi");
                    }
                });
    }

    private void loadChartData() {
        apiService.getChartData().enqueue(new Callback<ChartAllResponseModel>() {
            @Override
            public void onResponse(Call<ChartAllResponseModel> call, Response<ChartAllResponseModel> response) {
                if (!isAdded() || getContext() == null)
                    return;
                if (response.isSuccessful() && response.body() != null) {
                    List<ChartItem> datang = response.body().getDataChartAll().getJalandatang();
                    List<ChartItem> pulang = response.body().getDataChartAll().getJalanpulang();

                    if (datang.isEmpty() && pulang.isEmpty()) {
                        lineChart.clear();
                        lineChart.setNoDataText("Tidak Ada Data");
                        tvSatuanKiri.setVisibility(View.GONE);
                        return;
                    }

                    List<ChartItem> filterDatang;
                    List<ChartItem> filterPulang;

                    if (datang.size() > 6) {

                        filterDatang = datang.subList(
                                datang.size() - 6,
                                datang.size());

                    } else {

                        filterDatang = datang;

                    }

                    if (pulang.size() > 6) {

                        filterPulang = pulang.subList(
                                pulang.size() - 6,
                                pulang.size());

                    } else {

                        filterPulang = pulang;

                    }

                    setupChart(filterDatang, filterPulang);
                }
            }

            @Override
            public void onFailure(Call<ChartAllResponseModel> call, Throwable t) {
                if (!isAdded() || getContext() == null)
                    return;
                lineChart.setNoDataText("Gagal Ambil Data");
            }
        });
    }

    private void setupChart(List<ChartItem> datang, List<ChartItem> pulang) {
        tvSatuanKiri.setVisibility(View.VISIBLE);
        ArrayList<Entry> dataDatang = new ArrayList<>();
        ArrayList<Entry> dataPulang = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        // gabung waktu datang
        for (ChartItem d : datang) {
            if (!labels.contains(d.getWaktu())) {
                labels.add(d.getWaktu());
            }
        }

        // gabung waktu pulang
        for (ChartItem p : pulang) {
            if (!labels.contains(p.getWaktu())) {
                labels.add(p.getWaktu());
            }
        }

        // urutkan waktu
        Collections.sort(labels);

        Collections.sort(labels);

        // ambil 6 waktu terakhir
        if (labels.size() > 6) {
            labels = new ArrayList<>(
                    labels.subList(labels.size() - 6, labels.size()));
        }

        // mapping data datang
        for (ChartItem d : datang) {

            int index = labels.indexOf(d.getWaktu());

            if (index != -1) {
                dataDatang.add(
                        new Entry(index, d.getNilai()));
            }
        }

        // mapping data pulang
        for (ChartItem p : pulang) {

            int index = labels.indexOf(p.getWaktu());

            if (index != -1) {
                dataPulang.add(
                        new Entry(index, p.getNilai()));
            }
        }

        ArrayList<LineDataSet> dataSets = new ArrayList<>();

        // =========================
        // BLUE LINE
        // =========================
        if (!dataDatang.isEmpty()) {
            LineDataSet set1 = new LineDataSet(dataDatang,
                    "Jalan Datang");

            set1.setColor(Color.parseColor("#2563FF"));

            set1.setLineWidth(3f);

            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            set1.setDrawValues(false);

            set1.setDrawFilled(true);

            set1.setFillColor(Color.parseColor("#2563FF"));

            set1.setFillAlpha(25);

            set1.setDrawCircles(false);

            set1.setHighLightColor(Color.TRANSPARENT);

            set1.setDrawHorizontalHighlightIndicator(false);

            set1.setDrawVerticalHighlightIndicator(false);

            // titik terakhir
            set1.setDrawCircles(true);

            set1.setCircleRadius(4f);

            set1.setCircleColor(Color.parseColor("#2563FF"));
            dataSets.add(set1);
        }

        // =========================
        // PURPLE LINE
        // =========================

        if (!dataPulang.isEmpty()) {

            LineDataSet set2 = new LineDataSet(dataPulang,
                    "Jalan Pulang");

            set2.setColor(Color.parseColor("#C026FF"));

            set2.setLineWidth(3.5f);

            set2.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            set2.setDrawValues(false);

            set2.setDrawFilled(true);

            set2.setFillColor(Color.parseColor("#C026FF"));

            set2.setFillAlpha(18);

            set2.setDrawCircles(true);

            set2.setCircleColor(Color.parseColor("#C026FF"));
            set2.setHighLightColor(Color.TRANSPARENT);

            set2.setDrawHorizontalHighlightIndicator(false);

            set2.setDrawVerticalHighlightIndicator(false);
            set2.setCircleRadius(4f);
            dataSets.add(set2);
        }

        if (dataSets.isEmpty()) {
            lineChart.clear();
            lineChart.setNoDataText("Tidak Ada Data");
            return;
        }

        // =========================
        // CHART
        // =========================

        lineChart.setBackgroundColor(Color.WHITE);

        lineChart.setDrawGridBackground(false);

        lineChart.setDrawBorders(false);

        lineChart.getDescription().setEnabled(false);

        lineChart.setTouchEnabled(false);

        lineChart.setDragEnabled(false);

        lineChart.setScaleEnabled(false);

        lineChart.setPinchZoom(false);

        lineChart.setExtraTopOffset(12f);
        lineChart.setMinOffset(0f);

        // lineChart.setExtraLeftOffset(8f);
        //
        // lineChart.setExtraRightOffset(8f);

        lineChart.setExtraBottomOffset(16f);

        // =========================
        // X AXIS
        // =========================

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(labels.size() - 1);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setDrawGridLines(false);

        xAxis.setDrawAxisLine(false);

        xAxis.setTextColor(Color.parseColor("#7B8190"));

        xAxis.setTextSize(10f);
        xAxis.setYOffset(10f);

        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);

        xAxis.setLabelCount(labels.size(), true);
        xAxis.setCenterAxisLabels(false);
        xAxis.setAvoidFirstLastClipping(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        xAxis.setValueFormatter(
                new IndexAxisValueFormatter(labels));

        // =========================
        // LEFT AXIS
        // =========================

        lineChart.getAxisLeft().setTextColor(
                Color.parseColor("#7B8190"));

        lineChart.getAxisLeft().setTextSize(11f);

        lineChart.getAxisLeft().setDrawAxisLine(false);

        lineChart.getAxisLeft().setGridColor(
                Color.parseColor("#EEF1F6"));

        lineChart.getAxisLeft().setGridLineWidth(1f);

        lineChart.getAxisLeft().setAxisMinimum(0f);

        // =========================
        // RIGHT AXIS
        // =========================

        lineChart.getAxisRight().setEnabled(false);
        lineChart.setExtraRightOffset(12f);

        lineChart.getAxisRight().setTextColor(
                Color.parseColor("#7B8190"));

        // =========================
        // LEGEND
        // =========================

        Legend legend = lineChart.getLegend();

        legend.setEnabled(true);

        legend.setTextSize(12f);

        legend.setTextColor(Color.parseColor("#111827"));

        legend.setForm(Legend.LegendForm.CIRCLE);

        legend.setFormSize(12f);

        legend.setXEntrySpace(28f);

        legend.setHorizontalAlignment(
                Legend.LegendHorizontalAlignment.RIGHT);

        legend.setVerticalAlignment(
                Legend.LegendVerticalAlignment.BOTTOM);

        legend.setOrientation(
                Legend.LegendOrientation.HORIZONTAL);

        legend.setDrawInside(false);
        legend.setYOffset(12f);

        // =========================
        // SET DATA
        // =========================

        LineData lineData = new LineData();

        for (LineDataSet set : dataSets) {
            lineData.addDataSet(set);
        }

        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private void fetchActiveLokasiAndLoadStatus() {
        Log.d("DASHBOARD_DEBUG", "Mulai fetch getLokasi() untuk cek alat yang aktif...");
        String tokenJwt = "Bearer " + sessionManager.getToken();
        apiService.getLokasi(tokenJwt).enqueue(new Callback<LokasiResponseModel>() {
            @Override
            public void onResponse(Call<LokasiResponseModel> call, Response<LokasiResponseModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    activeLokasiIds.clear();
                    activeLokasiMap.clear();
                    if (response.body().getData() != null) {
                        for (LokasiResponseModel.Data d : response.body().getData()) {
                            Log.d("DASHBOARD_DEBUG",
                                    "Cek lokasi: " + d.getNamaLokasi() + " | Status: " + d.getStatusAlat());
                            if ("aktif".equalsIgnoreCase(d.getStatusAlat())) {
                                activeLokasiIds.add(d.getIdLokasi());
                                activeLokasiMap.put(d.getIdLokasi(), d);
                                Log.d("DASHBOARD_DEBUG", ">> Masuk daftar aktif: " + d.getIdLokasi());
                            }
                        }
                    }
                    Log.d("DASHBOARD_DEBUG", "Total lokasi aktif: " + activeLokasiIds.size());
                } else {
                    Log.d("DASHBOARD_DEBUG", "Gagal load getLokasi, code: " + response.code());
                }
                loadStatus();
            }

            @Override
            public void onFailure(Call<LokasiResponseModel> call, Throwable t) {
                Log.d("DASHBOARD_DEBUG", "Error getLokasi: " + t.getMessage());
                loadStatus(); // Tetap load status meskipun fetch lokasi gagal
            }
        });
    }

    private void loadStatus() {
        String token = "Bearer " + sessionManager.getToken();
        apiService.getStatusUtama(token, selectedIdLokasi).enqueue(new Callback<StatusUtamaResponseModel>() {
            @Override
            public void onResponse(Call<StatusUtamaResponseModel> call, Response<StatusUtamaResponseModel> response) {
                if (!isAdded() || getContext() == null)
                    return;
                
                List<StatusUtamaResponseModel.Data> validData = new ArrayList<>();
                Map<String, StatusUtamaResponseModel.Data> responseMap = new HashMap<>();

                if (response.isSuccessful() && response.body() != null) {
                    StatusUtamaResponseModel res = response.body();
                    if (res.isStatus() && res.getData() != null) {
                        for (StatusUtamaResponseModel.Data d : res.getData()) {
                            if (d != null && d.getIdLokasi() != null) {
                                responseMap.put(d.getIdLokasi(), d);
                            }
                        }
                    }
                }

                // Tampilkan semua lokasi yang status alatnya "aktif"
                for (String idLok : activeLokasiIds) {
                    // Jika ada filter lokasi terpilih, lewati yang tidak sesuai
                    if (!selectedIdLokasi.isEmpty() && !selectedIdLokasi.contains(idLok)) {
                        continue;
                    }

                    if (responseMap.containsKey(idLok)) {
                        validData.add(responseMap.get(idLok));
                    } else {
                        // Jika alat aktif tapi belum ada data sensor dari getStatusUtama, buat data placeholder
                        LokasiResponseModel.Data lok = activeLokasiMap.get(idLok);
                        if (lok != null) {
                            StatusUtamaResponseModel.Data dummy = new StatusUtamaResponseModel.Data();
                            dummy.setIdLokasi(idLok);
                            dummy.setNamaLokasi(lok.getNamaLokasi());
                            dummy.setLatitude(lok.getLatitude());
                            dummy.setLongitude(lok.getLongitude());
                            dummy.setTinggi(0.0);
                            dummy.setKecepatan(0.0);
                            dummy.setRisiko("Menunggu Data");
                            dummy.setLastUpdate("--:--");
                            validData.add(dummy);
                        }
                    }
                }

                if (!validData.isEmpty()) {
                    statusAdapter.updateData(validData);
                    rvStatusJalan.setVisibility(View.VISIBLE);
                    layoutEmptyState.setVisibility(View.GONE);
                } else {
                    statusAdapter.updateData(new ArrayList<>());
                    rvStatusJalan.setVisibility(View.GONE);
                    layoutEmptyState.setVisibility(View.VISIBLE);
                }
                showContent();
            }

            @Override
            public void onFailure(Call<StatusUtamaResponseModel> call, Throwable t) {
                if (!isAdded() || getContext() == null)
                    return;
                Log.d("DASHBOARD", "Error: " + t.getMessage());
                statusAdapter.updateData(new ArrayList<>());
                if (rvStatusJalan != null)
                    rvStatusJalan.setVisibility(View.GONE);
                if (layoutEmptyState != null)
                    layoutEmptyState.setVisibility(View.VISIBLE);
                showContent();
            }
        });
    }

    private void showContent() {
        new Handler().postDelayed(() -> {
            shimmerLayout.stopShimmer();
            shimmerLayout.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction(() -> {
                        shimmerLayout.setVisibility(View.GONE);
                    })
                    .start();
            scrollView2.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .start();
        }, 150); // Dikurangi dari 1200ms ke 150ms agar jauh lebih cepat
    }

    private void selesaikanLogout() {
        sessionManager.logoutUser();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private String getKecamatanFallback(String idLokasi) {
        if (idLokasi == null) return "Lainnya";
        switch (idLokasi) {
            case "LOC001": return "Banjarmasin Utara";
            case "LOC002": return "Banjarmasin Selatan";
            default: return "Lainnya";
        }
    }

    private void showLocationPicker(TextView tvLokasiTerpilih, TextView tvInfoBanner) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.dialog_pilih_lokasi, null);
        dialog.setContentView(bottomSheetView);

        RecyclerView rvLokasi = bottomSheetView.findViewById(R.id.rvLokasi);
        rvLokasi.setLayoutManager(new LinearLayoutManager(requireContext()));

        String token = "Bearer " + sessionManager.getToken();
        apiService.getLokasi(token).enqueue(new Callback<LokasiResponseModel>() {
            @Override
            public void onResponse(Call<LokasiResponseModel> call, Response<LokasiResponseModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LokasiResponseModel res = response.body();
                    if (res.isStatus() && res.getData() != null) {
                        Map<String, List<LokasiModel>> mapKecamatan = new HashMap<>();

                        for (LokasiResponseModel.Data d : res.getData()) {
                            // Filter agar hanya lokasi dengan sensor aktif yang masuk dialog
                            if (!"aktif".equalsIgnoreCase(d.getStatusAlat()))
                                continue;

                            boolean isSelected = false;
                            if (selectedIdLokasi.isEmpty() || selectedIdLokasi.contains(d.getIdLokasi())) {
                                isSelected = true;
                            }
                            LokasiModel jalan = new LokasiModel(d.getIdLokasi(), d.getNamaLokasi(), isSelected);
                            
                            // Baca Kecamatan dari backend, gunakan fallback jika null
                            String namaKec = (d.getKecamatan() != null && !d.getKecamatan().isEmpty()) 
                                             ? d.getKecamatan() 
                                             : getKecamatanFallback(d.getIdLokasi());

                            if (!mapKecamatan.containsKey(namaKec)) {
                                mapKecamatan.put(namaKec, new ArrayList<>());
                            }
                            mapKecamatan.get(namaKec).add(jalan);
                        }
                        
                        List<WilayahModel> listWilayah = new ArrayList<>();
                        int idWilayah = 1;
                        for (Map.Entry<String, List<LokasiModel>> entry : mapKecamatan.entrySet()) {
                            listWilayah.add(new WilayahModel(idWilayah++, entry.getKey(), entry.getValue()));
                        }
                        LokasiAdapter adapter = new LokasiAdapter(listWilayah);
                        rvLokasi.setAdapter(adapter);

                        bottomSheetView.findViewById(R.id.btnTerapkanFilter).setOnClickListener(v -> {
                            List<LokasiModel> selected = adapter.getSelectedLocations();
                            if (selected.isEmpty()) {
                                tvLokasiTerpilih.setText("Belum ada lokasi dipilih");
                                tvInfoBanner.setText("Pilih minimal 1 lokasi pantauan.");
                                selectedIdLokasi = "0"; // none
                                statusAdapter.updateData(new ArrayList<>());
                                rvStatusJalan.setVisibility(View.GONE);
                                layoutEmptyState.setVisibility(View.VISIBLE);
                            } else {
                                if (selected.size() == 1) {
                                    tvLokasiTerpilih.setText(selected.get(0).getNamaJalan());
                                } else {
                                    tvLokasiTerpilih.setText(selected.size() + " Lokasi Terpilih");
                                }

                                StringBuilder info = new StringBuilder("Menampilkan data untuk: ");
                                StringBuilder ids = new StringBuilder();
                                for (int i = 0; i < selected.size(); i++) {
                                    info.append(selected.get(i).getNamaJalan());
                                    ids.append(selected.get(i).getId());
                                    if (i < selected.size() - 1) {
                                        info.append(", ");
                                        ids.append(",");
                                    }
                                }
                                tvInfoBanner.setText(info.toString());
                                selectedIdLokasi = ids.toString();

                                // Fetch data
                                shimmerLayout.setVisibility(View.VISIBLE);
                                shimmerLayout.startShimmer();
                                scrollView2.setAlpha(0f);
                                fetchActiveLokasiAndLoadStatus();
                            }
                            dialog.dismiss();
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<LokasiResponseModel> call, Throwable t) {
            }
        });

        bottomSheetView.findViewById(R.id.btnCloseDialog).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}