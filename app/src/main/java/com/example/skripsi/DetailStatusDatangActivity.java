package com.example.skripsi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailStatusDatangActivity extends AppCompatActivity {
    private MapView map;
    private LineChart lineChart;
    private LinearLayout bgRekomendasi, weightResiko;

    private TextView tvTinggi, tvKecepatan, tvStatus, tvWaktu, tvStatusJam, tvRekomendasi, tvDeskripsiRekomendasi;
    private ImageView bulatStatus, arrowKecepatan, arrowTinggi, iconRekomendasi;
    InternetHandler internetHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_status_datang);

        Configuration.getInstance().load(
                this,
                getSharedPreferences("osmdroid", 0)
        );

        // cek internet
        internetHandler = new InternetHandler(
                this,
                findViewById(R.id.layoutNoInternet),
                findViewById(R.id.btn_reconnect),
                findViewById(R.id.progressReconnect)
        );
        internetHandler.checkInternet();

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
        loadKendaraan(tabelKendaraan);


        // bagian kembali
        ImageView btnKembali = findViewById(R.id.btn_kembali);
        btnKembali.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        // ini untuk chart
        lineChart = findViewById(R.id.lineChart);
        loadChartData();


        // Ini untuk tinggi dan status
        tvTinggi = findViewById(R.id.tvTinggiDetail);
        tvKecepatan = findViewById(R.id.tvKecepatanDetail);
        tvStatus = findViewById(R.id.tvStatusDetail);
        tvWaktu = findViewById(R.id.tvWaktuDetail);
        bulatStatus = findViewById(R.id.bulatStatusDetail);
        arrowKecepatan = findViewById(R.id.arrowKecepatanDetail);
        arrowTinggi = findViewById(R.id.arrowTinggiDetail);
        tvStatusJam = findViewById(R.id.statusJam);
        bgRekomendasi = findViewById(R.id.bgRekomendasi);
        iconRekomendasi = findViewById(R.id.iconRekomendasi);
        tvRekomendasi = findViewById(R.id.textRekomendasi);
        tvDeskripsiRekomendasi = findViewById(R.id.deskripsiRekomendasi);
        weightResiko = findViewById(R.id.weightResiko);
        loadStatusDatang();
    }


    @Override
    protected void onResume(){
        super.onResume();
        internetHandler.startAutoCheck();
    }

    @Override
    protected void onPause() {
        super.onPause();
        internetHandler.stopAutoCheck();
    }


    private void loadKendaraan(RecyclerView tabelKendaraan) {

        SessionManager session = new SessionManager(this);
        String token = "Bearer " + session.getToken();

        ApiService api = ApiClient.getClient().create(ApiService.class);
        String lokasi = "LOC001";

        api.getKendaraanUserSPK(token, lokasi).enqueue(new Callback<KendaraanUserResponseModel>() {
            @Override
            public void onResponse(Call<KendaraanUserResponseModel> call, Response<KendaraanUserResponseModel> response) {

                if (response.isSuccessful() && response.body() != null) {

                    List<KendaraanUserResponseModel.DataKendaraanUser> data =
                            response.body().getData();

                    List<KendaraanTabelModel> list = new ArrayList<>();

                    for (KendaraanUserResponseModel.DataKendaraanUser item : data) {

                        String plat = item.getPlatKendaraan();
                        String kategori = item.getJenisMotor();
                        String model = item.getModelMotor();
                        String status = item.getStatus();

                        list.add(new KendaraanTabelModel(
                                plat,
                                kategori,
                                model,
                                status
                        ));
                    }

                    KendaraanTabelAdapter adapter = new KendaraanTabelAdapter(list);
                    tabelKendaraan.setAdapter(adapter);

                } else {
                    Toast.makeText(DetailStatusDatangActivity.this,
                            "Gagal Load Kendaraan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<KendaraanUserResponseModel> call, Throwable t) {
                Toast.makeText(DetailStatusDatangActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadChartData(){
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getChartData().enqueue(new Callback<ChartAllResponseModel>() {
            @Override
            public void onResponse(Call<ChartAllResponseModel> call, Response<ChartAllResponseModel> response) {
                if(response.isSuccessful() && response.body() != null ){
                    List<ChartItem> datang = response.body().getDataChartAll().getJalandatang();

                    if(datang.isEmpty()){
                        lineChart.clear();
                        lineChart.setNoDataText("Tidak Ada Data");
                        return;
                    }

                    setupChart(datang);
                }
            }

            @Override
            public void onFailure(Call<ChartAllResponseModel> call, Throwable t) {
                lineChart.setNoDataText("Gagal Ambil Data");
            }
        });
    }

    private void setupChart(List<ChartItem> datang){
        ArrayList<Entry> dataDatang = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        // MAP untuk sinkronisasi waktu
        Map<String, Float> mapDatang = new HashMap<>();
        List<String> allWaktu = new ArrayList<>();

        // isi data datang
        for (ChartItem d : datang) {
            mapDatang.put(d.getWaktu(), d.getNilai());
            if (!allWaktu.contains(d.getWaktu())) {
                allWaktu.add(d.getWaktu());
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

        }


        LineDataSet set1 = new LineDataSet(dataDatang, "Jalan Datang");
        set1.setColor(Color.BLUE);
        set1.setDrawCircles(false);
        set1.setDrawValues(false);
        set1.setLineWidth(3f);
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);

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


        LineData lineData = new LineData(set1);
        lineChart.setData(lineData);

        // memidahkan keterangan dibawah jalan datang
        Legend legend = lineChart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setXEntrySpace(25f);

    }

    private void showPopupBerhasil(){
        Dialog dialogBerhasil = new Dialog(this);
        dialogBerhasil.setContentView(R.layout.popup_berhasil_hapus);
        LinearLayout lanjutanBerhasil = dialogBerhasil.findViewById(R.id.lanjutanBerhasil);
        RecyclerView tabelKendaraan = findViewById(R.id.rvKendaraan);
        tabelKendaraan.setLayoutManager(new LinearLayoutManager(this));

        dialogBerhasil.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        dialogBerhasil.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialogBerhasil.show();

        lanjutanBerhasil.setOnClickListener(v -> {
            dialogBerhasil.dismiss();
            loadKendaraan(tabelKendaraan);
        });
    }


    private void loadStatusDatang(){

        SessionManager session = new SessionManager(this);
        String token = "Bearer " + session.getToken();

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getStatusUtama(token).enqueue(new Callback<StatusUtamaResponseModel>() {
            @Override
            public void onResponse(Call<StatusUtamaResponseModel> call,
                                   Response<StatusUtamaResponseModel> response) {

                if(response.isSuccessful() && response.body() != null){

                    StatusUtamaResponseModel.Lokasi datang = response.body().getDatang();

                    if(datang != null && datang.getData() != null){

                        StatusUtamaResponseModel.Data d = datang.getData();

                        double tinggi = d.getTinggi();
                        double kecepatan = d.getKecepatan();
                        String risiko = d.getRisiko();
                        String waktu = d.getLastUpdate();
                        String kendaraan = d.getKendaraan();

                        tvTinggi.setText((double) tinggi + " cm");
                        tvKecepatan.setText((double) kecepatan + " cm/h");
                        tvWaktu.setText(waktu + " WITA");
                        tvWaktu.setTextColor(getResources().getColor(R.color.blue6));
                        Log.d("Resiko: ", risiko);

                        setStatusUI(risiko);
                        setArrow(kecepatan);
                        setDeskripsiStatus(kendaraan, risiko);

                    }
                }
            }

            @Override
            public void onFailure(Call<StatusUtamaResponseModel> call, Throwable t) {
                Log.e("API_ERROR", "Gagal Ambil Response Status");
            }
        });
    }

    private void setStatusUI(String risiko){

        if(risiko == null) return;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) weightResiko.getLayoutParams();

        if(risiko.toLowerCase().contains("aman")){
            tvStatusJam.setText("Aman");
            tvStatusJam.setTextColor(getResources().getColor(R.color.hijauaman));
            tvStatus.setText("Aman");
            tvStatus.setTextColor(getResources().getColor(R.color.hijauaman));
            bulatStatus.setImageResource(R.drawable.bulathijaukecil);
            bgRekomendasi.setBackgroundColor(getResources().getColor(R.color.hijaubackgroundaman));
            iconRekomendasi.setImageResource(R.drawable.aman_icon);
            tvRekomendasi.setText("Motor Honda Beat 150 anda Aman untuk melintasi jalur ini");
            tvDeskripsiRekomendasi.setText("Tetap hati-hati dan gunakan kecepatan rendah saat melintas");
            tvDeskripsiRekomendasi.setTextColor(getResources().getColor(R.color.hijauaman));
        } else if(risiko.toLowerCase().contains("resiko rendah")){
            tvStatusJam.setText("Resiko Sedang");
            tvStatusJam.setTextColor(getResources().getColor(R.color.kuningrendah));
            tvStatus.setText("Resiko Rendah");
            tvStatus.setTextColor(getResources().getColor(R.color.kuningrendah));
            bulatStatus.setImageResource(R.drawable.bulatkuningkecil);
            bgRekomendasi.setBackgroundColor(getResources().getColor(R.color.kuningbackgroundrendah));
            iconRekomendasi.setImageResource(R.drawable.resikorendah_icon);
            tvRekomendasi.setText("Motor Honda Beat 150 anda Beresiko Rendah untuk melintasi jalur ini");
            tvDeskripsiRekomendasi.setText("Kondisi diperkiran akan surut. Disarankan menunggu hingga kondisi lebih aman");
            tvDeskripsiRekomendasi.setTextColor(getResources().getColor(R.color.kuningrendah));
            params.weight = 0.6f;
        } else if(risiko.toLowerCase().contains("resiko sedang")){
            tvStatusJam.setText("Resiko Sedang");
            tvStatusJam.setTextColor(getResources().getColor(R.color.orensedang));
            tvStatus.setText("Resiko Sedang");
            tvStatus.setTextColor(getResources().getColor(R.color.orensedang));
            bulatStatus.setImageResource(R.drawable.bulatorenkecil);
            bgRekomendasi.setBackgroundColor(getResources().getColor(R.color.orenbackgroundsedang));
            iconRekomendasi.setImageResource(R.drawable.resikosedang_icon);
            tvRekomendasi.setText("Motor Honda Beat 150 anda Beresiko Sedang untuk melintasi jalur ini");
            tvDeskripsiRekomendasi.setText("Kondisi berpotensi berbahaya, Ketinggian air tidak menunjukkan penurunan");
            tvDeskripsiRekomendasi.setTextColor(getResources().getColor(R.color.orensedang));
            params.weight = 0.6f;

        } else if(risiko.toLowerCase().contains("resiko tinggi")){
            tvStatusJam.setText("Resiko Tinggi");
            tvStatusJam.setTextColor(getResources().getColor(R.color.peringatan));
            tvStatus.setText("Resiko Tinggi");
            tvStatus.setTextColor(getResources().getColor(R.color.peringatan));
            bulatStatus.setImageResource(R.drawable.bulatmerahkecil);
            bgRekomendasi.setBackgroundColor(getResources().getColor(R.color.merahbackgroundtinggi));
            iconRekomendasi.setImageResource(R.drawable.resikotinggi_icon);
            tvRekomendasi.setText("Motor Honda Beat 150 anda Beresiko Tinggi untuk melintasi jalur ini ");
            tvDeskripsiRekomendasi.setText("Kondisi sangat berbahaya, Ketinggian air sangat berisiko menyebabkan motor mogok dan bahkan risiko kerusakan pada kendaraan");
            tvDeskripsiRekomendasi.setTextColor(getResources().getColor(R.color.peringatan));
            params.weight = 0.6f;

        }
    }


    private void setArrow(double kecepatan){

        if(kecepatan > 0){
            arrowTinggi.setImageResource(R.drawable.up_arrow);
            arrowKecepatan.setImageResource(R.drawable.up_arrow);
        } else if(kecepatan < 0){
            arrowKecepatan.setImageResource(R.drawable.down_arrow);
            arrowTinggi.setImageResource(R.drawable.down_arrow);
        } else{
            arrowKecepatan.setImageResource(R.drawable.arrow_stabil);
            arrowTinggi.setImageResource(R.drawable.arrow_stabil);
            tvTinggi.setTextColor(getResources().getColor(R.color.black));
            tvKecepatan.setTextColor(getResources().getColor(R.color.black));
        }
    }

    private void setDeskripsiStatus(String kendaraan, String risiko){
        if (risiko.equals("Aman")){
            risiko = "Aman";
        } else if(risiko.equals("Resiko Rendah")){
            risiko = "Beresiko Rendah";
        } else if(risiko.equals("Resiko Sedang")){
            risiko = "Beresiko Sedang";
        } else if(risiko.equals("Resiko Tinggi")){
            risiko = "Beresiko Tinggi";
        }
        String fullText = "Motor " + kendaraan + " anda " + risiko + " untuk melintasi jalur ini";
        SpannableString spannable = new SpannableString(fullText);

        // warna kendaraan
        int startKendaraan = fullText.indexOf(kendaraan);
        int endKendaraan = startKendaraan + kendaraan.length();
        spannable.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.blue6)),
                startKendaraan,
                endKendaraan,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // warna risiko
        int startRisiko = fullText.indexOf(risiko);
        int endRisiko = startRisiko + risiko.length();

        int warna;
        if (risiko.toLowerCase().contains("resiko tinggi")){
            warna = getResources().getColor(R.color.peringatan);
        } else if (risiko.toLowerCase().contains("resiko sedang")){
            warna = getResources().getColor(R.color.orensedang);
        } else if (risiko.toLowerCase().contains("resiko rendah")){
            warna = getResources().getColor(R.color.kuningrendah);
        } else {
            warna = getResources().getColor(R.color.hijauaman);
        }

        spannable.setSpan(
                new ForegroundColorSpan(warna),
                startRisiko,
                endRisiko,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        tvRekomendasi.setText(spannable);
    }
}