package com.baingat.app;

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

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class DashboardAdminFragment extends Fragment {
    private SessionManager sessionManager;
    private String token;
    private ApiService apiService;


    private SwipeRefreshLayout swipeRefresh;

    private TextView tvTotal, tvAktif, tvNonAktif;
    private androidx.recyclerview.widget.RecyclerView rvDaftarAlat;
    private AlatDashboardAdapter adapter;

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
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvAktif = view.findViewById(R.id.tvAktif);
        tvNonAktif = view.findViewById(R.id.tvNonAktif);
        rvDaftarAlat = view.findViewById(R.id.rvDaftarAlat);
        rvDaftarAlat.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        
        swipeRefresh.setOnRefreshListener(() -> {
            loadData();
        });

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
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {

                    StatusAlatResponseModel.Data data = response.body().data;

                    // summary
                    tvTotal.setText(String.valueOf(data.total));
                    tvAktif.setText(String.valueOf(data.aktif));
                    tvNonAktif.setText(String.valueOf(data.non_aktif));

                    // alat list
                    java.util.List<StatusAlatResponseModel.Alat> listAlat = data.getAlatList();
                    if (listAlat != null && !listAlat.isEmpty()) {
                        if (adapter == null) {
                            adapter = new AlatDashboardAdapter(getContext(), listAlat, alat -> {
                                DetailAlatDialog.show(
                                        requireContext(),
                                        getParentFragmentManager(),
                                        alat.getDisplayName(),
                                        alat.aktif,
                                        alat.koordinat,
                                        alat.tanggal,
                                        alat.id_alat,
                                        token,
                                        apiService,
                                        () -> loadData()
                                );
                            });
                        } else {
                            adapter.updateData(listAlat);
                        }
                        
                        if (rvDaftarAlat.getAdapter() == null) {
                            rvDaftarAlat.setAdapter(adapter);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<StatusAlatResponseModel> call, Throwable t) {

                isLoading = false;
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);

                t.printStackTrace();
            }
        });
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