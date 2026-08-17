package com.baingat.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailStatusActivity extends AppCompatActivity {
    private MapView map;
    private LineChart lineChart;
    private LinearLayout bgRekomendasi;

    private TextView tvTinggi, tvKecepatan, tvStatus, tvWaktu, tvStatusJam, tvRekomendasi, tvDeskripsiRekomendasi, tvKetTinggi, tvKetTren;
    private ImageView bulatStatus, arrowKecepatan, iconRekomendasi;
    private ShimmerFrameLayout shimmerLayout;
    private ScrollView contentScroll;
    InternetHandler internetHandler;
    private Marker mapMarker;

    // Ini untuk refresh
    Handler handler = new Handler();
    Runnable runnable;
    int interval = 10 * 60 * 1000;
    // Variabel Dinamis
    private String idLokasi;
    private String namaLokasi;
    private double latitude = 0.0;
    private double longitude = 0.0;
    
    private String currentKendaraan = "-";
    private String currentRisiko = "Tidak Diketahui";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_status_datang);

        // Ambil Data dari Intent
        idLokasi = getIntent().getStringExtra("ID_LOKASI");
        namaLokasi = getIntent().getStringExtra("NAMA_LOKASI");
        String strLat = getIntent().getStringExtra("LATITUDE");
        String strLng = getIntent().getStringExtra("LONGITUDE");
        if (strLat != null && !strLat.isEmpty()) latitude = Double.parseDouble(strLat);
        if (strLng != null && !strLng.isEmpty()) longitude = Double.parseDouble(strLng);

        // Jika tidak ada parameter (misal buka paksa), beri default
        if (idLokasi == null) idLokasi = "LOC001";
        if (namaLokasi == null) namaLokasi = "Nama Lokasi";

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
        shimmerLayout = findViewById(R.id.shimmerLayout);
        contentScroll = findViewById(R.id.contentScroll);
        contentScroll.setAlpha(0f);
        shimmerLayout.startShimmer();

        Window window = getWindow();

        window.setStatusBarColor(Color.TRANSPARENT);

        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        map = findViewById(R.id.mapDetailDatang);
        map.setMultiTouchControls(false);
        map.setClickable(false);
        map.setEnabled(false);
        map.setFocusable(false);
        map.setFocusableInTouchMode(false);
        map.setBuiltInZoomControls(false);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // posisi
        GeoPoint titikLokasi = new GeoPoint(latitude != 0 ? latitude : -3.2967022, longitude != 0 ? longitude : 114.5837564);

        map.getController().setZoom(19.0);
        map.getController().setCenter(titikLokasi);
        mapMarker = new Marker(map);
        mapMarker.setPosition(titikLokasi);
        mapMarker.setInfoWindow(null);
        mapMarker.setIcon(getResources().getDrawable(R.drawable.maps_point_icon));
        map.getOverlays().add(mapMarker);

        TextView tvTitle = findViewById(R.id.tvTitleDetail);
        if(tvTitle != null) {
            tvTitle.setText("Detail " + namaLokasi);
        }

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

        tvStatusJam = findViewById(R.id.statusJam);
        bgRekomendasi = findViewById(R.id.bgRekomendasi);
        iconRekomendasi = findViewById(R.id.iconRekomendasi);
        tvRekomendasi = findViewById(R.id.textRekomendasi);
        tvDeskripsiRekomendasi = findViewById(R.id.deskripsiRekomendasi);
        tvKetTinggi = findViewById(R.id.tvKetTinggi);
        tvKetTren = findViewById(R.id.tvKetTren);
        loadStatusData();
    }




    @Override
    protected void onResume(){
        super.onResume();
        internetHandler.startAutoCheck();
        startAutoRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        internetHandler.stopAutoCheck();
        stopAutoRefresh();
    }


    private void startAutoRefresh() {
        runnable = new Runnable() {
            @Override
            public void run() {

                loadStatusData();
                loadChartData();
                handler.postDelayed(this, interval);
            }
        };

        handler.post(runnable);
    }

    private void stopAutoRefresh() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }


    private void loadKendaraan(RecyclerView tabelKendaraan) {

        SessionManager session = new SessionManager(this);
        String token = "Bearer " + session.getToken();

        ApiService api = ApiClient.getClient().create(ApiService.class);
        String lokasi = idLokasi != null ? idLokasi : "LOC001";

        api.getKendaraanUserSPK(token, lokasi).enqueue(new Callback<KendaraanUserResponseModel>() {
            @Override
            public void onResponse(Call<KendaraanUserResponseModel> call, Response<KendaraanUserResponseModel> response) {

                if (response.isSuccessful() && response.body() != null) {

                    List<KendaraanUserResponseModel.DataKendaraanUser> data =
                            response.body().getData();

                    List<KendaraanTabelModel> list = new ArrayList<>();

                    for (KendaraanUserResponseModel.DataKendaraanUser item : data) {
                        
                        if (item.isKendaraanUtama()) {
                            currentKendaraan = item.getNamaLengkapMotor();
                            if (!currentRisiko.equals("Tidak Diketahui")) {
                                setStatusUI(currentRisiko, currentKendaraan);
                                setDeskripsiStatus(currentKendaraan, currentRisiko);
                            }
                        }

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
                    android.util.Log.d("AppLog", String.valueOf("Gagal Load Kendaraan"));
                }
            }

            @Override
            public void onFailure(Call<KendaraanUserResponseModel> call, Throwable t) {
                android.util.Log.d("AppLog", String.valueOf("Error: " + t.getMessage()));
            }
        });
    }


    private void loadChartData(){
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getChartDataDetail(idLokasi).enqueue(new Callback<ChartResponseModel>() {
            @Override
            public void onResponse(Call<ChartResponseModel> call, Response<ChartResponseModel> response) {
                if(response.isSuccessful() && response.body() != null ){
                    List<ChartItem> dataChart = response.body().getData();

                    if(dataChart == null || dataChart.isEmpty()){
                        lineChart.clear();
                        lineChart.setNoDataText("Tidak Ada Data");
                        return;
                    }

                    setupChart(dataChart);
                }
            }

            @Override
            public void onFailure(Call<ChartResponseModel> call, Throwable t) {
                lineChart.setNoDataText("Gagal Ambil Data");
            }
        });
    }

    private void setupChart(List<ChartItem> dataChart){

        ArrayList<Entry> dataDatang = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int size = dataChart.size();

        for (int i = 0; i < size; i++) {

            ChartItem d = dataChart.get(i);
            // titik biru
            dataDatang.add(
                    new Entry(i, d.getNilai())
            );

            // label waktu
            labels.add(d.getWaktu());
        }

        // =========================
        // BLUE LINE
        // =========================

        LineDataSet set1 =
                new LineDataSet(dataDatang,
                        namaLokasi != null ? namaLokasi : "Data Lokasi");

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
                new IndexAxisValueFormatter(labels)
        );

        // =========================
        // LEFT AXIS
        // =========================

        lineChart.getAxisLeft().setTextColor(
                Color.parseColor("#7B8190")
        );

        lineChart.getAxisLeft().setTextSize(11f);

        lineChart.getAxisLeft().setDrawAxisLine(false);

        lineChart.getAxisLeft().setGridColor(
                Color.parseColor("#EEF1F6")
        );

        lineChart.getAxisLeft().setGridLineWidth(1f);

        lineChart.getAxisLeft().setAxisMinimum(0f);

        // =========================
        // RIGHT AXIS
        // =========================

        lineChart.getAxisRight().setEnabled(false);
        lineChart.setExtraRightOffset(12f);

        lineChart.getAxisRight().setTextColor(
                Color.parseColor("#7B8190")
        );


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
                Legend.LegendHorizontalAlignment.RIGHT
        );

        legend.setVerticalAlignment(
                Legend.LegendVerticalAlignment.BOTTOM
        );

        legend.setOrientation(
                Legend.LegendOrientation.HORIZONTAL
        );

        legend.setDrawInside(false);
        legend.setYOffset(12f);

        // =========================
        // SET DATA
        // =========================

        LineData lineData = new LineData(set1);

        lineChart.setData(lineData);

        lineChart.animateX(800);

        lineChart.invalidate();
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


    private void loadStatusData(){

        SessionManager session = new SessionManager(this);
        String token = "Bearer " + session.getToken();

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getStatusUtama(token, idLokasi).enqueue(new Callback<StatusUtamaResponseModel>() {
            @Override
            public void onResponse(Call<StatusUtamaResponseModel> call,
                                   Response<StatusUtamaResponseModel> response) {

                if(response.isSuccessful() && response.body() != null){
                    StatusUtamaResponseModel res = response.body();

                    if(res.isStatus() && res.getData() != null && !res.getData().isEmpty()){
                        StatusUtamaResponseModel.Data d = null;
                        for(StatusUtamaResponseModel.Data data : res.getData()) {
                            if(data.getIdLokasi() != null && data.getIdLokasi().equals(idLokasi)) {
                                d = data;
                                break;
                            }
                        }
                        // Fallback jika tidak match id (harusnya tidak terjadi)
                        if (d == null) d = res.getData().get(0);

                        double tinggi = d.getTinggi();
                        double kecepatan = d.getKecepatan();
                        String risiko = d.getRisiko();
                        String waktu = d.getLastUpdate();
                        String kendaraan = d.getKendaraan();

                        if (kendaraan != null && !kendaraan.trim().isEmpty() && !kendaraan.equals("-")) {
                            currentKendaraan = kendaraan;
                        }
                        if (risiko != null) {
                            currentRisiko = risiko;
                        }

                        tvTinggi.setText((double) tinggi + " cm");
                        tvWaktu.setText(waktu + " WITA");
                        tvWaktu.setTextColor(getResources().getColor(R.color.blue6));
                        Log.d("Resiko: ", currentRisiko);

                        setStatusUI(currentRisiko, currentKendaraan);
                        setArrow(kecepatan);
                        setDeskripsiStatus(currentKendaraan, currentRisiko);

                        showContent();
                    }
                }
            }

            @Override
            public void onFailure(Call<StatusUtamaResponseModel> call, Throwable t) {
                Log.e("API_ERROR", "Gagal Ambil Response Status");
                showContent();
            }
        });
    }

    private void setStatusUI(String risiko, String kendaraan){

        if(risiko == null) return;
        if (kendaraan == null) kendaraan = "Kendaraan Anda";
        
        if(risiko.toLowerCase().contains("aman")){
            tvStatusJam.setText("Aman");
            tvStatusJam.setTextColor(getResources().getColor(R.color.hijauaman));
            tvStatus.setText("Aman");
            tvStatus.setTextColor(getResources().getColor(R.color.hijauaman));
            bulatStatus.setImageResource(R.drawable.bulathijaukecil);
            bgRekomendasi.setBackgroundColor(getResources().getColor(R.color.hijaubackgroundaman));
            iconRekomendasi.setImageResource(R.drawable.aman_icon);
            tvRekomendasi.setText("Motor " + kendaraan + " aman untuk melintasi jalur ini");
            tvDeskripsiRekomendasi.setText("Tetap hati-hati dan gunakan kecepatan rendah saat melintas");
            tvDeskripsiRekomendasi.setTextColor(getResources().getColor(R.color.hijauaman));
            tvTinggi.setTextColor(getResources().getColor(R.color.hijauaman));
            if(tvKetTinggi != null) tvKetTinggi.setText("Batas aman");
            if(mapMarker != null) mapMarker.setIcon(getResources().getDrawable(R.drawable.maps_point_green));
        } else if(risiko.toLowerCase().contains("resiko rendah")){
            tvStatusJam.setText("Resiko Sedang");
            tvStatusJam.setTextColor(getResources().getColor(R.color.kuningrendah));
            tvStatus.setText("Resiko Rendah");
            tvStatus.setTextColor(getResources().getColor(R.color.kuningrendah));
            bulatStatus.setImageResource(R.drawable.bulatkuningkecil);
            bgRekomendasi.setBackgroundColor(getResources().getColor(R.color.kuningbackgroundrendah));
            iconRekomendasi.setImageResource(R.drawable.resikorendah_icon);
            tvRekomendasi.setText("Motor " + kendaraan + " beresiko rendah untuk melintasi jalur ini");
            tvDeskripsiRekomendasi.setText("Kondisi diperkiran akan surut. Disarankan menunggu hingga kondisi lebih aman");
            tvDeskripsiRekomendasi.setTextColor(getResources().getColor(R.color.kuningrendah));
            tvTinggi.setTextColor(getResources().getColor(R.color.kuningrendah));
            if(tvKetTinggi != null) tvKetTinggi.setText("Perlu diwaspadai");
            if(mapMarker != null) mapMarker.setIcon(getResources().getDrawable(R.drawable.maps_point_yellow));
        } else if(risiko.toLowerCase().contains("waspada")){
            tvStatusJam.setText("Waspada");
            tvStatusJam.setTextColor(getResources().getColor(R.color.kuningrendah));
            tvStatus.setText("Waspada");
            tvStatus.setTextColor(getResources().getColor(R.color.kuningrendah));
            bulatStatus.setImageResource(R.drawable.bulatkuningkecil);
            bgRekomendasi.setBackgroundColor(getResources().getColor(R.color.kuningbackgroundrendah));
            iconRekomendasi.setImageResource(R.drawable.resikorendah_icon);
            tvRekomendasi.setText("Motor " + kendaraan + " beresiko untuk melintasi jalur ini");
            tvDeskripsiRekomendasi.setText("Kondisi berpotensi berbahaya. Disarankan menunggu hingga kondisi lebih aman atau gunakan alternatif lain");
            tvDeskripsiRekomendasi.setTextColor(getResources().getColor(R.color.kuningrendah));
            tvTinggi.setTextColor(getResources().getColor(R.color.kuningrendah));
            if(tvKetTinggi != null) tvKetTinggi.setText("Perlu diwaspadai");
            if(mapMarker != null) mapMarker.setIcon(getResources().getDrawable(R.drawable.maps_point_yellow));
        } else if(risiko.toLowerCase().contains("resiko sedang")){
            tvStatusJam.setText("Resiko Sedang");
            tvStatusJam.setTextColor(getResources().getColor(R.color.orensedang));
            tvStatus.setText("Resiko Sedang");
            tvStatus.setTextColor(getResources().getColor(R.color.orensedang));
            bulatStatus.setImageResource(R.drawable.bulatorenkecil);
            bgRekomendasi.setBackgroundColor(getResources().getColor(R.color.orenbackgroundsedang));
            iconRekomendasi.setImageResource(R.drawable.resikosedang_icon);
            tvRekomendasi.setText("Motor " + kendaraan + " beresiko sedang untuk melintasi jalur ini");
            tvDeskripsiRekomendasi.setText("Kondisi berpotensi berbahaya, Ketinggian air tidak menunjukkan penurunan");
            tvDeskripsiRekomendasi.setTextColor(getResources().getColor(R.color.orensedang));
            tvTinggi.setTextColor(getResources().getColor(R.color.orensedang));
            if(tvKetTinggi != null) tvKetTinggi.setText("Kondisi siaga");
            if(mapMarker != null) mapMarker.setIcon(getResources().getDrawable(R.drawable.maps_point_yellow));
        } else if(risiko.toLowerCase().contains("resiko tinggi")){
            tvStatusJam.setText("Resiko Tinggi");
            tvStatusJam.setTextColor(getResources().getColor(R.color.peringatan));
            tvStatus.setText("Resiko Tinggi");
            tvStatus.setTextColor(getResources().getColor(R.color.peringatan));
            bulatStatus.setImageResource(R.drawable.bulatmerahkecil);
            bgRekomendasi.setBackgroundColor(getResources().getColor(R.color.merahbackgroundtinggi));
            iconRekomendasi.setImageResource(R.drawable.resikotinggi_icon);
            tvRekomendasi.setText("Motor " + kendaraan + " beresiko tinggi untuk melintasi jalur ini");
            tvDeskripsiRekomendasi.setText("Kondisi sangat berbahaya, Ketinggian air sangat berisiko menyebabkan motor mogok dan bahkan risiko kerusakan pada kendaraan");
            tvDeskripsiRekomendasi.setTextColor(getResources().getColor(R.color.peringatan));
            tvTinggi.setTextColor(getResources().getColor(R.color.peringatan));
            if(tvKetTinggi != null) tvKetTinggi.setText("Bahaya banjir!");
            if(mapMarker != null) mapMarker.setIcon(getResources().getDrawable(R.drawable.maps_point_red));
        } else if(risiko.toLowerCase().contains("bahaya")){
            tvStatusJam.setText("Bahaya");
            tvStatusJam.setTextColor(getResources().getColor(R.color.peringatan));
            tvStatus.setText("Bahaya");
            tvStatus.setTextColor(getResources().getColor(R.color.peringatan));
            bulatStatus.setImageResource(R.drawable.bulatmerahkecil);
            bgRekomendasi.setBackgroundColor(getResources().getColor(R.color.merahbackgroundtinggi));
            iconRekomendasi.setImageResource(R.drawable.resikotinggi_icon);
            tvRekomendasi.setText("Motor " + kendaraan + " berbahaya untuk melintasi jalur ini");
            tvDeskripsiRekomendasi.setText("Kondisi sangat berbahaya, sangat tidak dianjurkan untuk melewati tempat ini. Ketinggian air sangat berisiko menyebabkan motor mogok dan bahkan berisiko kerusakan pada kendaraan");
            tvDeskripsiRekomendasi.setTextColor(getResources().getColor(R.color.peringatan));
            tvTinggi.setTextColor(getResources().getColor(R.color.peringatan));
            if(tvKetTinggi != null) tvKetTinggi.setText("Bahaya banjir!");
            if(mapMarker != null) mapMarker.setIcon(getResources().getDrawable(R.drawable.maps_point_red));
        }
        if(map != null) map.invalidate();
    }


    private void setArrow(double kecepatan){

        if(kecepatan > 0){
            arrowKecepatan.setImageResource(R.drawable.up_arrow);
            tvKecepatan.setText("Naik");
            tvKecepatan.setTextColor(getResources().getColor(R.color.hijauaman));
            if(tvKetTren != null) tvKetTren.setText("Ketinggian air menurun");
        } else if(kecepatan < 0){
            arrowKecepatan.setImageResource(R.drawable.down_arrow);
            tvKecepatan.setText("Turun");
            tvKecepatan.setTextColor(getResources().getColor(R.color.peringatan));
            if(tvKetTren != null) tvKetTren.setText("Ketinggian air meningkat");
        } else{
            arrowKecepatan.setImageResource(R.drawable.arrow_stabil);
            tvKecepatan.setTextColor(android.graphics.Color.parseColor("#8B5CF6"));
            tvKecepatan.setText("Stabil");
            if(tvKetTren != null) tvKetTren.setText("Tidak ada perubahan signifikan");
        }
    }

    private void setDeskripsiStatus(String kendaraan, String risiko){
        if (kendaraan == null) kendaraan = "-";
        if (risiko == null) risiko = "Tidak Diketahui";

        if (risiko.equals("Aman")){
            risiko = "Aman";
        } else if(risiko.equals("Waspada")){
            risiko = "Beresiko";
        }else if(risiko.equals("Resiko Rendah")){
            risiko = "Beresiko Rendah";
        } else if(risiko.equals("Resiko Sedang")){
            risiko = "Beresiko Sedang";
        } else if(risiko.equals("Resiko Tinggi")){
            risiko = "Beresiko Tinggi";
        } else if(risiko.equals("Bahaya")){
            risiko = "Berbahaya";
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
        } else if (risiko.toLowerCase().contains("beresiko")){
            warna = getResources().getColor(R.color.kuningrendah);
        }else if (risiko.toLowerCase().contains("bahaya")){
            warna = getResources().getColor(R.color.peringatan);
        }else {
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

    private void showContent(){
        new Handler().postDelayed(() -> {
            shimmerLayout.stopShimmer();
            shimmerLayout.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction(() -> {
                        shimmerLayout.setVisibility(View.GONE);
                    })
                    .start();

            contentScroll.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .start();

        }, 1200);
    }
}