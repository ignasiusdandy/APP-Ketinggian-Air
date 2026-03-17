package com.example.skripsi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_user);

        LineChart lineChart = findViewById(R.id.lineChart);

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

        ImageView mapsmenu = findViewById(R.id.maps_menu);

        mapsmenu.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MapsActivity.class);
            startActivity(intent);

            //animasi perpindahan
            overridePendingTransition(R.anim.slide_kanan_masuk, R.anim.slide_kiri_keluar);
        });
    }
}
