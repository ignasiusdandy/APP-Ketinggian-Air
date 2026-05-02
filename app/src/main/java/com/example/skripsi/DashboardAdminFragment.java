package com.example.skripsi;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardAdminFragment extends Fragment {
    private SessionManager sessionManager;
    private String token;
    private ApiService apiService;


    private TextView tvTotal, tvAktif, tvNonAktif;
    private TextView tvStatusDatang, tvStatusPulang;
    private TextView tvWaktuDatang, tvWaktuPulang;
    private ImageView dotDatang, dotPulang, datangAlatIcon, pulangAlatIcon;
    private LinearLayout bgDatangAlat, bgPulangAlat, btnJalanDatang, btnJalanPulang;
    private Handler handler = new Handler();
    private Runnable runnable;
    private final int INTERVAL = 10 * 60 * 1000;
    private boolean isLoading = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_admin, container, false);
        sessionManager = new SessionManager(requireContext());
        token = "Bearer " + sessionManager.getToken();

        apiService = ApiClient.getClient().create(ApiService.class);

        // binding
        tvTotal = view.findViewById(R.id.tvTotal);
        tvAktif = view.findViewById(R.id.tvAktif);
        tvNonAktif = view.findViewById(R.id.tvNonAktif);

        tvStatusDatang = view.findViewById(R.id.tvStatusDatang);
        tvStatusPulang = view.findViewById(R.id.tvStatusPulang);

        tvWaktuDatang = view.findViewById(R.id.tvWaktuDatang);
        tvWaktuPulang = view.findViewById(R.id.tvWaktuPulang);

        dotDatang = view.findViewById(R.id.dotDatang);
        dotPulang = view.findViewById(R.id.dotPulang);

        datangAlatIcon = view.findViewById(R.id.datangAlatIcon);
        pulangAlatIcon = view.findViewById(R.id.pulangAlatIcon);
        bgDatangAlat = view.findViewById(R.id.bgDatangIcon);
        bgPulangAlat = view.findViewById(R.id.bgPulangIcon);
        btnJalanDatang = view.findViewById(R.id.btnJalanDatang);
        btnJalanPulang = view.findViewById(R.id.btnJalanPulang);
        loadData();
        return view;
    }

    private void loadData() {

        if (isLoading) return;
        isLoading = true;

        apiService.getStatusAlat(token).enqueue(new Callback<StatusAlatResponseModel>() {
            @Override
            public void onResponse(Call<StatusAlatResponseModel> call,
                                   Response<StatusAlatResponseModel> response) {

                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {

                    StatusAlatResponseModel.Data data = response.body().data;

                    // summary
                    tvTotal.setText(String.valueOf(data.total));
                    tvAktif.setText(String.valueOf(data.aktif));
                    tvNonAktif.setText(String.valueOf(data.non_aktif));

                    // jalan datang
                    setStatus(tvStatusDatang, dotDatang, data.jalan_datang.aktif, bgDatangAlat, datangAlatIcon);
                    tvWaktuDatang.setText("Terakhir Diperbaharui " + data.jalan_datang.last_update);

                    // jalan pulang
                    setStatus(tvStatusPulang, dotPulang, data.jalan_pulang.aktif, bgPulangAlat, pulangAlatIcon);
                    tvWaktuPulang.setText("Terakhir Diperbaharui " + data.jalan_pulang.last_update);

                    btnJalanDatang.setOnClickListener(v -> {
                        DetailAlatDialog.show(
                                requireContext(),
                                getParentFragmentManager(),
                                "Jalan Datang",
                                data.jalan_datang.aktif,
                                data.jalan_datang.koordinat,
                                data.jalan_datang.tanggal,
                                "ALT001",
                                token,
                                apiService,
                                () -> loadData()
                        );
                    });

                    btnJalanPulang.setOnClickListener(v -> {
                        DetailAlatDialog.show(
                                requireContext(),
                                getParentFragmentManager(),
                                "Jalan Pulang",
                                data.jalan_pulang.aktif,
                                data.jalan_pulang.koordinat,
                                data.jalan_pulang.tanggal,
                                "ALT002",
                                token,
                                apiService,
                                () -> loadData()
                        );
                    });

                }
            }

            @Override
            public void onFailure(Call<StatusAlatResponseModel> call, Throwable t) {

                isLoading = false;

                t.printStackTrace();
            }
        });
    }

    private void setStatus(TextView tv, ImageView dot, boolean aktif, LinearLayout bg, ImageView icon) {
        if (aktif) {
            tv.setText("Status: Aktif");
            tv.setTextColor(getResources().getColor(R.color.hijauaman));
            dot.setImageResource(R.drawable.bulathijaukecil);
            bg.setBackground(getResources().getDrawable(R.drawable.bg_icon_hijau));
            icon.setImageResource(R.drawable.cpu_icon);
        } else {
            tv.setText("Status: Non Aktif");
            tv.setTextColor(getResources().getColor(R.color.peringatan));
            dot.setImageResource(R.drawable.bulatmerahkecil);
            bg.setBackground(getResources().getDrawable(R.drawable.bg_icon_merah));
            icon.setImageResource(R.drawable.cpu_merah);
        }
    }

    private void startAutoRefresh() {
        runnable = new Runnable() {
            @Override
            public void run() {
                loadData();
                handler.postDelayed(this, INTERVAL);
            }
        };
        handler.post(runnable);
    }

    private void stopAutoRefresh() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        startAutoRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoRefresh();
    }
}