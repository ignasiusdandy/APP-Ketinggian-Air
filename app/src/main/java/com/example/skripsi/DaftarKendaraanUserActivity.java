package com.example.skripsi;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Ini bagian daftar kendaraan ketika diklik dipengaturan akun
public class DaftarKendaraanUserActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    LinearLayout btnKembali, btnAdd;
    TextView tvJumlah;
    private ApiService apiService;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tampil_kendaraaan_user);
        btnKembali = findViewById(R.id.btn_kembali);
        apiService = ApiClient.getClient().create(ApiService.class);
        tvJumlah = findViewById(R.id.tvJumlah);


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadKendaraan();
        btnKembali.setOnClickListener(v -> finish());

        btnAdd = findViewById(R.id.btn_tambah);
        btnAdd.setOnClickListener(v -> {
            PopupTambahKendaraan dialog = new PopupTambahKendaraan(this);
            dialog.show();
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            //bikin agak gelap
            dialog.getWindow().setDimAmount(0.8f);

            // bikin agar bisa direfresh
            dialog.setOnDismissListener(d -> {
                loadKendaraan();
            });
        });
    }

    private void loadKendaraan() {

        SessionManager session = new SessionManager(this);
        String token = "Bearer " + session.getToken();
        ApiService api = ApiClient.getClient().create(ApiService.class);


        api.getKendaraanUser(token).enqueue(new Callback<KendaraanUserResponseModel>() {
            @Override
            public void onResponse(Call<KendaraanUserResponseModel> call, Response<KendaraanUserResponseModel> response) {

                if (response.isSuccessful() && response.body() != null) {

                    List<KendaraanUserResponseModel.DataKendaraanUser> data =
                            response.body().getData();

                    List<KendaraanTabelPengaturanModel> list = new ArrayList<>();

                    int jumlah = data.size();

                    if (jumlah == 1) {
                        tvJumlah.setText("1 kendaraan terdaftar");
                    } else {
                        tvJumlah.setText(jumlah + " kendaraan terdaftar");
                    }

                    for (KendaraanUserResponseModel.DataKendaraanUser item : data) {

                        String plat = item.getPlatKendaraan();
                        String kategori = item.getJenisMotor();
                        String model = item.getModelMotor();
                        String idKendaraan = item.getId();
                        boolean isUtama = item.isKendaraanUtama();

                        list.add(new KendaraanTabelPengaturanModel(
                                idKendaraan,
                                plat,
                                kategori,
                                model,
                                isUtama
                        ));
                    }

                    KendaraanTabelPengaturanAdapter adapter =
                            new KendaraanTabelPengaturanAdapter(list, new KendaraanTabelPengaturanAdapter.OnItemAction() {
                                @Override
                                public void onSetUtama(KendaraanTabelPengaturanModel data) {
                                    setKendaraanUtama(data);
                                }

                                @Override
                                public void onEdit(KendaraanTabelPengaturanModel data) {
                                    PopupEditKendaraan dialogEdit = new PopupEditKendaraan(
                                            DaftarKendaraanUserActivity.this,
                                            data,
                                            () -> {
                                                loadKendaraan();
                                            }
                                    );

                                    dialogEdit.show();

                                    dialogEdit.getWindow().setLayout(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                    );

                                    dialogEdit.getWindow().setDimAmount(0.8f);
                                    dialogEdit.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                }

                                @Override
                                public void onDelete(KendaraanTabelPengaturanModel data) {
                                    if (data.isKendaraanUtama()){
                                        Dialog dialogGagal = new Dialog(DaftarKendaraanUserActivity.this);
                                        dialogGagal.setContentView(R.layout.popup_gagal_hapus_data_kendaraan);
                                        dialogGagal.getWindow().setLayout(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.WRAP_CONTENT
                                        );
                                        dialogGagal.getWindow().setDimAmount(0.8f);
                                        dialogGagal.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                        dialogGagal.show();
                                        new android.os.Handler().postDelayed(() -> {
                                            if (dialogGagal.isShowing()) {
                                                dialogGagal.dismiss();
                                            }
                                        }, 5000);
                                        return;
                                    }

                                    showKonfirmasiHapus(data);
                                }
                            });

                    recyclerView.setAdapter(adapter);

                } else {
                    Toast.makeText(DaftarKendaraanUserActivity.this,
                            "Gagal Load Kendaraan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<KendaraanUserResponseModel> call, Throwable t) {
                Toast.makeText(DaftarKendaraanUserActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showPopupBerhasil(){
        Dialog dialogBerhasil = new Dialog(this);
        dialogBerhasil.setContentView(R.layout.popup_berhasil_hapus);

        LinearLayout lanjutanBerhasil =
                dialogBerhasil.findViewById(R.id.lanjutanBerhasil);

        dialogBerhasil.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        dialogBerhasil.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialogBerhasil.show();

        lanjutanBerhasil.setOnClickListener(v -> {
            dialogBerhasil.dismiss();
            loadKendaraan();
        });
    }

    private void hapusKendaraan(KendaraanTabelPengaturanModel data) {

        SessionManager session = new SessionManager(this);
        String token = "Bearer " + session.getToken();
        String plat = data.getPlat();
        String idKendaraan = data.getIdKendaraan();
        Log.d("id:", idKendaraan);

        apiService.hapusKendaraan(token, idKendaraan, plat)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        if (response.isSuccessful()) {
                            showPopupBerhasil();
                            loadKendaraan();
                        } else {
                            Toast.makeText(DaftarKendaraanUserActivity.this,
                                    "Gagal hapus", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(DaftarKendaraanUserActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void showKonfirmasiHapus(KendaraanTabelPengaturanModel data){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_konfirmasi_hapus);
        LinearLayout btnKonfirmHapus = dialog.findViewById(R.id.btnLanjutanHapus);
        LinearLayout btnBatal = dialog.findViewById(R.id.btn_batal_hapus);

        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        btnBatal.setOnClickListener(v -> {
            dialog.dismiss();
        });

        btnKonfirmHapus.setOnClickListener(v -> {
            dialog.dismiss();
            hapusKendaraan(data);
        });

        dialog.show();
    }

    private void setKendaraanUtama(KendaraanTabelPengaturanModel data){

        SessionManager session = new SessionManager(this);
        String token = "Bearer " + session.getToken();

        apiService.updateUser(
                token,
                null,
                data.getIdKendaraan()
        ).enqueue(new Callback<UpdatePengaturanAkunModel>() {

            @Override
            public void onResponse(Call<UpdatePengaturanAkunModel> call, Response<UpdatePengaturanAkunModel> response) {

                if (response.isSuccessful()) {
                    loadKendaraan();

                } else {
                    Toast.makeText(DaftarKendaraanUserActivity.this,
                            "Gagal set utama", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdatePengaturanAkunModel> call, Throwable t) {
                Toast.makeText(DaftarKendaraanUserActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}