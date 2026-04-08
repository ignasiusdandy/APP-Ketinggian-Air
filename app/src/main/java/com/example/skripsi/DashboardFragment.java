package com.example.skripsi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {
    private SessionManager sessionManager;
    private TextView tvPerkenalanNama;
    private TextView tvKendaraan;
    private ApiService apiService;
    private LineChart lineChart;

    public DashboardFragment() {
        super(R.layout.fragment_dashboard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = ApiClient.getClient().create(ApiService.class);

        // Untuk mendapatkan session managernya
        sessionManager = new SessionManager(requireContext());
        String nama = sessionManager.getUserDetails().get(SessionManager.KEY_NAMA);
        String token = sessionManager.getToken();
//        Log.d("SESSION_DEBUG", "ID: " +user.get(SessionManager.KEY_ID));
//        Log.d("SESSION_DEBUG", "Nama: " + user.get(SessionManager.KEY_NAMA));
//        Log.d("SESSION_DEBUG", "Email: " + user.get(SessionManager.KEY_EMAIL));
//        Log.d("SESSION_DEBUG", "Token: " + user.get(SessionManager.KEY_TOKEN));
//        Log.d("SESSION_DEBUG", "Role: " + user.get(SessionManager.KEY_ROLE));

        //ini untuk perkenalan nama
        tvPerkenalanNama = view.findViewById(R.id.haiNamaUser);
        tvPerkenalanNama.setText("Hai " + nama);

        // ini untuk waktu sekarang
        TextView tvselamatwaktu = view.findViewById(R.id.selamatWaktu);
        Calendar calendar = Calendar.getInstance();
        int jam = calendar.get(Calendar.HOUR_OF_DAY);

        String ucapan;

        if (jam > 5 && jam < 12 ){
            ucapan = "Selamat Pagi";
        } else if (jam > 12 && jam < 16 ){
            ucapan = "Selamat Sore";
        } else {
            ucapan = "Selamat Malam";
        }
        tvselamatwaktu.setText(ucapan);

        // ini untuk kendaraaan utama
        tvKendaraan = view.findViewById(R.id.kendaraanUtama);
        apiService.getKendaraanUtama("Bearer " + token)
                .enqueue(new Callback<KendaraanUtamaResponseModel>() {
                    @Override
                    public void onResponse(Call<KendaraanUtamaResponseModel> call, Response<KendaraanUtamaResponseModel> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            KendaraanUtamaResponseModel res = response.body();

                            if (res.isStatus() && res.getData() != null) {
                                String kendaraan =
                                        res.getData().getJenisMotor() + " " +
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

        // ini untuk on click ke detail status
        LinearLayout layoutDatang = view.findViewById(R.id.layoutJalanDatang);
        LinearLayout layoutPulang = view.findViewById(R.id.layoutJalanPulang);

        layoutDatang.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), DetailStatusDatangActivity.class));
        });

        layoutPulang.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), DetailStatusPulangActivity.class));
        });

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            refreshData();
            loadChartData();

        }
    }

    private void refreshData() {
        SessionManager sm = new SessionManager(requireContext());
        String namaBaru = sm.getUserDetails().get(SessionManager.KEY_NAMA);
        tvPerkenalanNama.setText("Hai " + namaBaru);

        String token = sm.getToken();
//        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getKendaraanUtama("Bearer " + token)
                .enqueue(new Callback<KendaraanUtamaResponseModel>() {
                    @Override
                    public void onResponse(Call<KendaraanUtamaResponseModel> call, Response<KendaraanUtamaResponseModel> response) {
                        if (response.isSuccessful() && response.body() != null &&
                                response.body().isStatus() && response.body().getData() != null) {

                            String kendaraan =
                                    response.body().getData().getJenisMotor() + " " +
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

    private void loadChartData(){
        apiService.getChartData().enqueue(new Callback<ChartAllResponseModel>() {
            @Override
            public void onResponse(Call<ChartAllResponseModel> call, Response<ChartAllResponseModel> response) {
                if(response.isSuccessful() && response.body() != null ){
                    List<ChartItem> datang = response.body().getDataChartAll().getJalandatang();
                    List<ChartItem> pulang = response.body().getDataChartAll().getJalanpulang();

                    if(datang.isEmpty() && pulang.isEmpty()){
                        lineChart.clear();
                        lineChart.setNoDataText("Tidak Ada Data");
                        return;
                    }

                    setupChart(datang,pulang);
                }
            }

            @Override
            public void onFailure(Call<ChartAllResponseModel> call, Throwable t) {
                lineChart.setNoDataText("Gagal Ambil Data");
            }
        });
    }

    private void setupChart(List<ChartItem> datang, List<ChartItem> pulang){
        ArrayList<Entry> dataDatang = new ArrayList<>();
        ArrayList<Entry> dataPulang = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        // MAP untuk sinkronisasi waktu
        Map<String, Float> mapDatang = new HashMap<>();
        Map<String, Float> mapPulang = new HashMap<>();
        List<String> allWaktu = new ArrayList<>();

        // isi data datang
        for (ChartItem d : datang) {
            mapDatang.put(d.getWaktu(), d.getNilai());
            if (!allWaktu.contains(d.getWaktu())) {
                allWaktu.add(d.getWaktu());
            }
        }

        // isi data pulang
        for (ChartItem p : pulang) {
            mapPulang.put(p.getWaktu(), p.getNilai());
            if (!allWaktu.contains(p.getWaktu())) {
                allWaktu.add(p.getWaktu());
            }
        }

        // URUTKAN waktu
        Collections.sort(allWaktu);

        // mapping ke Entry
        for (int i = 0; i < allWaktu.size(); i++) {
            String waktu = allWaktu.get(i);
            labels.add(waktu);

            if (mapDatang.containsKey(waktu)) {
                dataDatang.add(new Entry(i, mapDatang.get(waktu)));
            }

            if (mapPulang.containsKey(waktu)) {
                dataPulang.add(new Entry(i, mapPulang.get(waktu)));
            }
        }


        LineDataSet set1 = new LineDataSet(dataDatang, "Jalan Datang");
        set1.setColor(Color.BLUE);
        set1.setDrawCircles(false);
        set1.setDrawValues(false);
        set1.setLineWidth(3f);
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineDataSet set2 = new LineDataSet(dataPulang, "Jalan Pulang");
        set2.setColor(Color.parseColor("#B42D9B"));
        set2.setDrawValues(false);
        set2.setDrawCircles(false);
        set2.setLineWidth(3f);
        set2.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(-0.5f);

        // paksa semuanya agar tampil dan textnya menyesuaikan
        xAxis.setLabelCount(labels.size(), true);
        xAxis.setTextSize(9f);
        xAxis.setLabelCount(10, false);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.animateX(1000);
        lineChart.invalidate();
        lineChart.animateX(1000);
        lineChart.setExtraBottomOffset(15f);


        LineData lineData = new LineData(set1, set2);
        lineChart.setData(lineData);

        // memidahkan keterangan dibawah jalan datang dan pulang
        Legend legend = lineChart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setXEntrySpace(25f);

    }
}