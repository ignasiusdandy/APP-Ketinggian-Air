package com.example.skripsi;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PengaturanAkunFragment extends Fragment {

    List<KendaraanUserResponseModel.DataKendaraanUser> listKendaraan = new ArrayList<>();
    ArrayAdapter<String> adapter;

    public PengaturanAkunFragment() {
        super(R.layout.fragment_pengaturan_akun);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView TVnamaAkun = view.findViewById(R.id.nama_pengaturan_akun);
        CustomSpinner spinner = view.findViewById(R.id.spinner_kendaraan_utama);
        MaterialButton btnUpdate = view.findViewById(R.id.btn_update_pengaturan);
        ImageView arrowMotor = view.findViewById(R.id.iv_arrow_motor);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        SessionManager sessionManager = new SessionManager(requireContext());

        HashMap<String, String> user = sessionManager.getUserDetails();
        String token = sessionManager.getToken();

        // set nama awal
        TVnamaAkun.setText(user.get(SessionManager.KEY_NAMA));

        // init adapter kosong
        adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // animasi arrow
        spinner.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened(androidx.appcompat.widget.AppCompatSpinner spinner) {
                arrowMotor.animate().rotation(0).setDuration(300).start();
            }

            @Override
            public void onSpinnerClosed(androidx.appcompat.widget.AppCompatSpinner spinner) {
                arrowMotor.animate().rotation(-90).setDuration(300).start();
            }
        });

        // load data awal
        loadKendaraanUser(apiService, token, spinner);

        // tombol update
        btnUpdate.setOnClickListener(v -> {

            String nama = TVnamaAkun.getText().toString().trim();
            int posisi = spinner.getSelectedItemPosition();

            if (posisi < 0 || posisi >= listKendaraan.size()) {
                Toast.makeText(getContext(), "Pilih kendaraan dulu", Toast.LENGTH_SHORT).show();
                return;
            }

            String idKendaraan = listKendaraan.get(posisi).getId();
            String jenis = listKendaraan.get(posisi).getJenisMotor();
            String model = listKendaraan.get(posisi).getModelMotor();


            btnUpdate.setEnabled(false);

            apiService.updateUser("Bearer " + token, nama, idKendaraan)
                    .enqueue(new Callback<UpdatePengaturanAkunModel>() {
                        @Override
                        public void onResponse(Call<UpdatePengaturanAkunModel> call, Response<UpdatePengaturanAkunModel> response) {

                            btnUpdate.setEnabled(true);

                            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {

                                Dialog dialog = new Dialog(requireContext());
                                dialog.setContentView(R.layout.popup_berhasil);
                                dialog.getWindow().setLayout(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                );

                                //bikin agak gelap
                                dialog.getWindow().setDimAmount(0.8f);
                                // bikin transparan agar bisa diliat corner radiusnya
                                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                LinearLayout lanjutanBerhasil = dialog.findViewById(R.id.lanjutanBerhasil);
                                lanjutanBerhasil.setOnClickListener(v -> {
                                    dialog.dismiss();
                                });

                                dialog.show();


                                // update nama session
                                sessionManager.updateNama(
                                        nama
                                );

                                // refresh data
                                loadKendaraanUser(apiService, token, spinner);
                            } else {
                                Dialog dialog = new Dialog(requireContext());
                                dialog.setContentView(R.layout.popup_gagal);
                                dialog.getWindow().setLayout(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                );

                                //bikin agak gelap
                                dialog.getWindow().setDimAmount(0.8f);
                                // bikin transparan agar bisa diliat corner radiusnya
                                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                LinearLayout lanjutanGagal = dialog.findViewById(R.id.lanjutanGagal);
                                lanjutanGagal.setOnClickListener(v -> {
                                    dialog.dismiss();
                                });

                                dialog.show();

                            }
                        }

                        @Override
                        public void onFailure(Call<UpdatePengaturanAkunModel> call, Throwable t) {
                            btnUpdate.setEnabled(true);
                            Toast.makeText(getContext(), "Error koneksi", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden && getView() != null) {
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            SessionManager sessionManager = new SessionManager(requireContext());
            String token = sessionManager.getToken();

            CustomSpinner spinner = getView().findViewById(R.id.spinner_kendaraan_utama);

            refreshData(apiService, token, spinner);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getView() == null) return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        SessionManager sessionManager = new SessionManager(requireContext());
        String token = sessionManager.getToken();

        CustomSpinner spinner = getView().findViewById(R.id.spinner_kendaraan_utama);

        refreshData(apiService, token, spinner);
    }

    private void refreshData(ApiService apiService, String token, CustomSpinner spinner) {

        // reset adapter
        adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // load ulang
        loadKendaraanUser(apiService, token, spinner);
    }

    private void loadKendaraanUser(ApiService apiService, String token, CustomSpinner spinner) {

        apiService.getKendaraanUser("Bearer " + token)
                .enqueue(new Callback<KendaraanUserResponseModel>() {
                    @Override
                    public void onResponse(Call<KendaraanUserResponseModel> call, Response<KendaraanUserResponseModel> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            listKendaraan.clear();
                            listKendaraan.addAll(response.body().getData());

                            List<String> namaKendaraan = new ArrayList<>();

                            int selectedIndex = 0;

                            for (int i = 0; i < listKendaraan.size(); i++) {
                                KendaraanUserResponseModel.DataKendaraanUser k = listKendaraan.get(i);
                                Log.d("CEK_KENDARAAN",
                                        k.getNamaLengkapMotor() + " | utama: " + k.isKendaraanUtama());

                                namaKendaraan.add(k.getNamaLengkapMotor());

                                if (k.isKendaraanUtama()) {
                                    selectedIndex = i;
                                }
                            }

                            adapter.clear();
                            adapter.addAll(namaKendaraan);
                            adapter.notifyDataSetChanged();

                            spinner.setSelection(selectedIndex);
                        }
                    }

                    @Override
                    public void onFailure(Call<KendaraanUserResponseModel> call, Throwable t) {
                        Toast.makeText(getContext(), "Gagal load kendaraan", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}