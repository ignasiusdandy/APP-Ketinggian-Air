package com.example.skripsi;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    public DashboardFragment() {
        super(R.layout.fragment_dashboard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LineChart lineChart = view.findViewById(R.id.lineChart);

        // ================= DATA =================
        ArrayList<Entry> datang = new ArrayList<>();
        datang.add(new Entry(0, 160));
        datang.add(new Entry(1, 150));
        datang.add(new Entry(2, 170));
        datang.add(new Entry(3, 240));
        datang.add(new Entry(4, 230));
        datang.add(new Entry(5, 120));

        ArrayList<Entry> pulang = new ArrayList<>();
        pulang.add(new Entry(0, 20));
        pulang.add(new Entry(1, 140));
        pulang.add(new Entry(2, 120));
        pulang.add(new Entry(3, 60));
        pulang.add(new Entry(4, 10));
        pulang.add(new Entry(5, 40));

        // ================= DATASET =================
        LineDataSet setDatang = new LineDataSet(datang, "Jalan Datang");
        setDatang.setColor(Color.BLUE);
        setDatang.setLineWidth(3f);
        setDatang.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        setDatang.setDrawCircles(false);
        setDatang.setDrawValues(false);

        LineDataSet setPulang = new LineDataSet(pulang, "Jalan Pulang");
        setPulang.setColor(Color.parseColor("#B42D9B"));
        setPulang.setLineWidth(3f);
        setPulang.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        setPulang.setDrawCircles(false);
        setPulang.setDrawValues(false);

        // ================= SET DATA =================
        LineData data = new LineData(setDatang, setPulang);
        lineChart.setData(data);

        // ================= STYLE =================
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);

        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getAxisLeft().setAxisMaximum(260f);
        lineChart.getAxisLeft().setGranularity(50f);

        lineChart.setDrawBorders(false);
        lineChart.setDrawGridBackground(false);

        // ================= X AXIS =================
        String[] waktu = {"21:00","22:00","23:00","00:00","01:00","02:00"};

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(waktu));
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // ================= LEGEND =================
        Legend legend = lineChart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);

        // ================= ANIMASI =================
        lineChart.animateX(800);
        lineChart.invalidate();
    }
}