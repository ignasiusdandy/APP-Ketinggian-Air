package com.example.skripsi;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {
    private SessionManager sessionManager;
    private TextView tvPerkenalanNama;
    private TextView tvKendaraan;

    public DashboardFragment() {
        super(R.layout.fragment_dashboard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

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
        LineChart lineChart = view.findViewById(R.id.lineChart);
        ArrayList<Entry> data1 = new ArrayList<>();
        data1.add(new Entry(0, 160));
        data1.add(new Entry(1, 150));
        data1.add(new Entry(2, 170));
        data1.add(new Entry(3, 240));
        data1.add(new Entry(4, 230));
        data1.add(new Entry(5, 120));

        ArrayList<Entry> data2 = new ArrayList<>();
        data2.add(new Entry(0, 20));
        data2.add(new Entry(1, 140));
        data2.add(new Entry(2, 120));
        data2.add(new Entry(3, 60));
        data2.add(new Entry(4, 10));
        data2.add(new Entry(5, 40));

        LineDataSet set1 = new LineDataSet(data1, "Jalan Datang");
        set1.setColor(Color.BLUE);
        set1.setLineWidth(2f);

        LineDataSet set2 = new LineDataSet(data2, "Jalan Pulang");
        set2.setColor(Color.parseColor("#B42D9B"));
        set2.setLineWidth(2f);

        // garis smooth
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set2.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // hilangkan lingkaran titik
        set1.setDrawCircles(false);
        set2.setDrawCircles(false);

        // hilangkan angka di titik
        set1.setDrawValues(false);
        set2.setDrawValues(false);

        // ketebalan garis
        set1.setLineWidth(3f);
        set2.setLineWidth(3f);

        LineData lineData = new LineData(set1, set2);
        lineChart.setData(lineData);

        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getAxisLeft().setAxisMaximum(260f);
        lineChart.getAxisLeft().setGranularity(50f);
        lineChart.setDrawBorders(false);
        lineChart.setDrawGridBackground(false);
        lineChart.animateX(1000);
        lineChart.setExtraBottomOffset(15f);


        String[] waktu = {"21:00","22:00","23:00","00:00","01:00","02:00"};

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(waktu));
        xAxis.setGranularity(1f);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(11f);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // memidahkan keterangan dibawah jalan datang dan pulang
        Legend legend = lineChart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setXEntrySpace(25f);

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            refreshData();
        }
    }

    private void refreshData() {
        SessionManager sm = new SessionManager(requireContext());
        String namaBaru = sm.getUserDetails().get(SessionManager.KEY_NAMA);
        tvPerkenalanNama.setText("Hai " + namaBaru);

        String token = sm.getToken();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

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
}