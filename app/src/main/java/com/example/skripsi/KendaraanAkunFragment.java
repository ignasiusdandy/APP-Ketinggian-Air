package com.example.skripsi;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KendaraanAkunFragment extends Fragment {
    public KendaraanAkunFragment() {
        super(R.layout.fragment_kendaraan_user);
    }
    private RecyclerView tabelKendaraan;
    private LinearLayout btnHapus, btnKonfirmHapus, btnBatal;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabelKendaraan = view.findViewById(R.id.rvKendaraan);
        tabelKendaraan.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadKendaraan(tabelKendaraan);

        // Bagian Tambah kendaraan
        LinearLayout btnInput = view.findViewById(R.id.btn_input);
        btnInput.setOnClickListener(v -> {
            PopupTambahKendaraan dialog = new PopupTambahKendaraan(requireContext());
            dialog.show();
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            //bikin agak gelap
            dialog.getWindow().setDimAmount(0.8f);

            // bikin agar bisa direfresh
            dialog.setOnDismissListener(d -> {
                loadKendaraan(tabelKendaraan);
            });
        });

        // bagian btn edit
        LinearLayout btnEdit = view.findViewById(R.id.btn_edit);
        btnEdit.setOnClickListener(v -> {
            PopupPilihEditKendaraan dialog = new PopupPilihEditKendaraan(requireContext(), () -> {
                showPopupBerhasil();
            });
            dialog.show();
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            dialog.getWindow().setDimAmount(0.8f);
            dialog.setOnDismissListener(d -> {
                loadKendaraan(tabelKendaraan);
            });
        });
    }


    private void loadKendaraan(RecyclerView tabelKendaraan) {

        SessionManager session = new SessionManager(requireContext());
        String token = "Bearer " + session.getToken();
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getKendaraanUser(token).enqueue(new Callback<KendaraanUserResponseModel>() {
            @Override
            public void onResponse(Call<KendaraanUserResponseModel> call, Response<KendaraanUserResponseModel> response) {

                if (response.isSuccessful() && response.body() != null) {

                    List<KendaraanUserResponseModel.DataKendaraanUser> data =
                            response.body().getData();

                    List<KendaraanTabelPengaturanModel> list = new ArrayList<>();

                    for (KendaraanUserResponseModel.DataKendaraanUser item : data) {

                        String plat = item.getPlatKendaraan();

                        String kategori = item.getJenisMotor();
                        String model = item.getModelMotor();

//                        list.add(new KendaraanTabelPengaturanModel(
//                                plat,
//                                kategori,
//                                model
//                        ));
                    }

//                    KendaraanTabelPengaturanAdapter adapter = new KendaraanTabelPengaturanAdapter(list);
//                    tabelKendaraan.setAdapter(adapter);

                } else {
                    Toast.makeText(requireContext(),
                            "Gagal Load Kendaraan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<KendaraanUserResponseModel> call, Throwable t) {
                Toast.makeText(requireContext(),
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPopupBerhasil(){
        Dialog dialogBerhasil = new Dialog(requireContext());
        dialogBerhasil.setContentView(R.layout.popup_berhasil_hapus);
        LinearLayout lanjutanBerhasil = dialogBerhasil.findViewById(R.id.lanjutanBerhasil);
        tabelKendaraan.setLayoutManager(new LinearLayoutManager(requireContext()));

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
}
