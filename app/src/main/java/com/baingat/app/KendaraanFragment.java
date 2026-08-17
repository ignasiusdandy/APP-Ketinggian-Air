package com.baingat.app;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KendaraanFragment extends Fragment {

    private RecyclerView recyclerView;

    private LinearLayout btnKembali, btnAdd, scrollView2;

    private TextView tvJumlah;

    private ApiService apiService;
    private ShimmerFrameLayout shimmerLayout;


    public KendaraanFragment() {
        super(R.layout.tampil_kendaraaan_user);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        apiService =
                ApiClient.getClient().create(ApiService.class);

        btnKembali = view.findViewById(R.id.btn_kembali);

        btnAdd = view.findViewById(R.id.btn_tambah);

        tvJumlah = view.findViewById(R.id.tvJumlah);

        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        scrollView2 = view.findViewById(R.id.contentLayout);
        scrollView2.setAlpha(0f);
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();


        loadKendaraan();

        // =====================================
        // TAMBAH KENDARAAN
        // =====================================

        btnAdd.setOnClickListener(v -> {

            PopupTambahKendaraan dialog =
                    new PopupTambahKendaraan(requireContext());

            dialog.show();

            if (dialog.getWindow() != null) {

                dialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

                dialog.getWindow().setDimAmount(0.8f);
                dialog.getWindow()
                        .setBackgroundDrawableResource(
                                android.R.color.transparent
                        );
            }

            dialog.setOnDismissListener(d -> {
                loadKendaraan();
            });
        });
    }

    // =====================================
    // LOAD KENDARAAN
    // =====================================

    private void loadKendaraan() {

        SessionManager session =
                new SessionManager(requireContext());

        String token = "Bearer " + session.getToken();

        apiService.getKendaraanUser(token)
                .enqueue(new Callback<KendaraanUserResponseModel>() {

                    @Override
                    public void onResponse(
                            Call<KendaraanUserResponseModel> call,
                            Response<KendaraanUserResponseModel> response
                    ) {
                        if (!isAdded() || getContext() == null) return;
                        if (response.isSuccessful()
                                && response.body() != null) {

                            List<KendaraanUserResponseModel.DataKendaraanUser>
                                    data = response.body().getData();

                            List<KendaraanTabelPengaturanModel>
                                    list = new ArrayList<>();

                            int jumlah = data.size();

                            if (jumlah == 1) {

                                tvJumlah.setText(
                                        "1 kendaraan terdaftar"
                                );

                            } else {

                                tvJumlah.setText(
                                        jumlah + " kendaraan terdaftar"
                                );
                            }

                            for (KendaraanUserResponseModel
                                    .DataKendaraanUser item : data) {

                                String plat =
                                        item.getPlatKendaraan();

                                String kategori =
                                        item.getJenisMotor();

                                String model =
                                        item.getModelMotor();

                                String idKendaraan =
                                        item.getId();

                                boolean isUtama =
                                        item.isKendaraanUtama();

                                list.add(
                                        new KendaraanTabelPengaturanModel(
                                                idKendaraan,
                                                plat,
                                                kategori,
                                                model,
                                                isUtama
                                        )
                                );
                            }

                            KendaraanTabelPengaturanAdapter adapter =
                                    new KendaraanTabelPengaturanAdapter(
                                            list,
                                            new KendaraanTabelPengaturanAdapter.OnItemAction() {

                                                @Override
                                                public void onSetUtama(
                                                        KendaraanTabelPengaturanModel data
                                                ) {
                                                    setKendaraanUtama(data);
                                                }

                                                @Override
                                                public void onEdit(
                                                        KendaraanTabelPengaturanModel data
                                                ) {

                                                    PopupEditKendaraan dialogEdit =
                                                            new PopupEditKendaraan(
                                                                    requireContext(),
                                                                    data,
                                                                    () -> {
                                                                        loadKendaraan();
                                                                    }
                                                            );

                                                    dialogEdit.show();

                                                    if (dialogEdit.getWindow() != null) {

                                                        dialogEdit.getWindow().setLayout(
                                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                                ViewGroup.LayoutParams.WRAP_CONTENT
                                                        );

                                                        dialogEdit.getWindow().setDimAmount(0.8f);

                                                        dialogEdit.getWindow()
                                                                .setBackgroundDrawableResource(
                                                                        android.R.color.transparent
                                                                );
                                                    }
                                                }

                                                @Override
                                                public void onDelete(
                                                        KendaraanTabelPengaturanModel data
                                                ) {

                                                    if (data.isKendaraanUtama()) {

                                                        Dialog dialogGagal =
                                                                new Dialog(requireContext());

                                                        dialogGagal.setContentView(
                                                                R.layout.popup_gagal_hapus_data_kendaraan
                                                        );

                                                        if (dialogGagal.getWindow() != null) {

                                                            dialogGagal.getWindow().setLayout(
                                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                                            );

                                                            dialogGagal.getWindow().setDimAmount(0.8f);

                                                            dialogGagal.getWindow()
                                                                    .setBackgroundDrawableResource(
                                                                            android.R.color.transparent
                                                                    );
                                                        }

                                                        dialogGagal.show();

                                                        new android.os.Handler()
                                                                .postDelayed(() -> {

                                                                    if (dialogGagal.isShowing()) {

                                                                        dialogGagal.dismiss();
                                                                    }

                                                                }, 5000);

                                                        return;
                                                    }

                                                    showKonfirmasiHapus(data);
                                                }
                                            }
                                    );

                            recyclerView.setAdapter(adapter);
                            showContent();

                        } else {

                            try {

                                String errorBody =
                                        response.errorBody().string();

                                Log.d("API_ERROR", errorBody);

                            } catch (Exception e) {

                                e.printStackTrace();
                            }
                            if (!isAdded() || getContext() == null) return;
                            android.util.Log.d("AppLog", String.valueOf("Gagal Load Kendaraan"));
                            showContent();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<KendaraanUserResponseModel> call,
                            Throwable t
                    ) {

                        android.util.Log.d("AppLog", String.valueOf("Error: " + t.getMessage()));
                        showContent();
                    }
                });
    }

    // =====================================
    // POPUP BERHASIL
    // =====================================

    private void showPopupBerhasil() {

        Dialog dialogBerhasil =
                new Dialog(requireContext());

        dialogBerhasil.setContentView(
                R.layout.popup_berhasil_hapus
        );

        LinearLayout lanjutanBerhasil =
                dialogBerhasil.findViewById(
                        R.id.lanjutanBerhasil
                );

        if (dialogBerhasil.getWindow() != null) {

            dialogBerhasil.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            dialogBerhasil.getWindow()
                    .setBackgroundDrawableResource(
                            android.R.color.transparent
                    );
        }

        dialogBerhasil.show();

        lanjutanBerhasil.setOnClickListener(v -> {

            dialogBerhasil.dismiss();

            loadKendaraan();
        });
    }

    // =====================================
    // HAPUS KENDARAAN
    // =====================================

    private void hapusKendaraan(
            KendaraanTabelPengaturanModel data
    ) {

        SessionManager session =
                new SessionManager(requireContext());

        String token =
                "Bearer " + session.getToken();

        String plat =
                data.getPlat();

        String idKendaraan =
                data.getIdKendaraan();

        apiService.hapusKendaraan(
                        token,
                        idKendaraan,
                        plat
                )
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response
                    ) {
                        if (!isAdded() || getContext() == null) return;
                        if (response.isSuccessful()) {

                            showPopupBerhasil();

                            loadKendaraan();

                        } else {

                            if (!isAdded() || getContext() == null) return;
                            android.util.Log.d("AppLog", String.valueOf("Gagal hapus"));
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ResponseBody> call,
                            Throwable t
                    ) {

                        android.util.Log.d("AppLog", String.valueOf("Error: " + t.getMessage()));
                    }
                });
    }

    // =====================================
    // KONFIRMASI HAPUS
    // =====================================

    private void showKonfirmasiHapus(
            KendaraanTabelPengaturanModel data
    ) {

        Dialog dialog =
                new Dialog(requireContext());

        dialog.setContentView(
                R.layout.popup_konfirmasi_hapus
        );

        LinearLayout btnKonfirmHapus =
                dialog.findViewById(R.id.btnLanjutanHapus);

        LinearLayout btnBatal =
                dialog.findViewById(R.id.btn_batal_hapus);

        if (dialog.getWindow() != null) {

            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            dialog.getWindow()
                    .setBackgroundDrawableResource(
                            android.R.color.transparent
                    );
        }

        btnBatal.setOnClickListener(v -> {
            dialog.dismiss();
        });

        btnKonfirmHapus.setOnClickListener(v -> {

            dialog.dismiss();

            hapusKendaraan(data);
        });

        dialog.show();
    }

    // =====================================
    // SET KENDARAAN UTAMA
    // =====================================

    private void setKendaraanUtama(
            KendaraanTabelPengaturanModel data
    ) {

        SessionManager session =
                new SessionManager(requireContext());

        String token =
                "Bearer " + session.getToken();

        apiService.updateUser(
                        token,
                        null,
                        data.getIdKendaraan()
                )
                .enqueue(new Callback<UpdatePengaturanAkunModel>() {

                    @Override
                    public void onResponse(
                            Call<UpdatePengaturanAkunModel> call,
                            Response<UpdatePengaturanAkunModel> response
                    ) {
                        if (!isAdded() || getContext() == null) return;
                        if (response.isSuccessful()) {
                            loadKendaraan();
                        } else {

                            android.util.Log.d("AppLog", String.valueOf("Gagal set utama"));
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<UpdatePengaturanAkunModel> call,
                            Throwable t
                    ) {
                        if (!isAdded() || getContext() == null) return;
                        android.util.Log.d("AppLog", String.valueOf("Error: " + t.getMessage()));
                    }
                });
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

            scrollView2.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .start();

        }, 1200);
    }
}