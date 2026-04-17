package com.example.skripsi;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PopupPilihEditKendaraan extends Dialog {
    private Context context;
    private CustomSpinner spinnerKendaraanUser;
    private ImageView arrowMotor, btnBatalPilih;
    private LinearLayout btnHapus, btnKonfirmHapus, btnBatal, btnEdit;
    private ApiService apiService;
    private ArrayAdapter<String> adapter;
    private String selectedIdKendaraan = null;
    private OnHapusListener listener;
    private KendaraanUserResponseModel.DataKendaraanUser selectedKendaraan = null;
    List<KendaraanUserResponseModel.DataKendaraanUser> listKendaraan = new ArrayList<>();




    public interface OnHapusListener{
        void onBerhasilHapus();
    }

    public PopupPilihEditKendaraan(Context context, OnHapusListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pilih_edit_kendaraan_user);

        initView();
        setupSpinner();
        btnBatalPilih.setOnClickListener(v -> {
            dismiss();
        });
        btnHapus.setOnClickListener(v -> {
            int position = spinnerKendaraanUser.getSelectedItemPosition();
            if(position <= 0){
                Toast.makeText(context, "Pilih Kendaraan terlebih dahulu!", Toast.LENGTH_SHORT).show();
                return;
            }
            KendaraanUserResponseModel.DataKendaraanUser selected = listKendaraan.get(position - 1);
            if (selected.isKendaraanUtama()){
                Dialog dialogGagal = new Dialog(context);
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

            showKonfirmasiHapus();
        });

        btnEdit.setOnClickListener(v -> {
            int position = spinnerKendaraanUser.getSelectedItemPosition();

            if (position <= 0) {
                Toast.makeText(context, "Pilih Kendaraan terlebih dahulu!", Toast.LENGTH_SHORT).show();
                return;
            }

            KendaraanUserResponseModel.DataKendaraanUser selected = listKendaraan.get(position - 1);

            PopupEditKendaraan dialogEdit = new PopupEditKendaraan(
                    context,
                    selected,
                    () -> {
                        if (listener != null) {
                            listener.onBerhasilHapus();
                        }
                    }
            );

            dialogEdit.show();

            dialogEdit.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            dialogEdit.getWindow().setDimAmount(0.8f);
            dialogEdit.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            dismiss();
        });
    }

    private void initView(){
        spinnerKendaraanUser = findViewById(R.id.spinner_kendaraan_user);
        arrowMotor = findViewById(R.id.iv_arrow_motor);
        apiService = ApiClient.getClient().create(ApiService.class);
        btnHapus = findViewById(R.id.btn_hapus);
        btnBatalPilih = findViewById(R.id.btn_batal_pilih);
        btnEdit = findViewById(R.id.edit_kendaraan);
    }

    private void setupSpinner(){
        SessionManager session = new SessionManager(context);
        String token = "Bearer " + session.getToken();

        // init adapter kosong
        adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                new ArrayList<>()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKendaraanUser.setAdapter(adapter);
        // animasi arrow
        spinnerKendaraanUser.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened(androidx.appcompat.widget.AppCompatSpinner spinner) {
                arrowMotor.animate().rotation(0).setDuration(300).start();
            }

            @Override
            public void onSpinnerClosed(androidx.appcompat.widget.AppCompatSpinner spinner) {
                arrowMotor.animate().rotation(-90).setDuration(300).start();
            }
        });

        apiService.getKendaraanUser(token)
                .enqueue(new Callback<KendaraanUserResponseModel>() {
                    @Override
                    public void onResponse(Call<KendaraanUserResponseModel> call, Response<KendaraanUserResponseModel> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            listKendaraan.clear();
                            listKendaraan.addAll(response.body().getData());

                            List<String> namaKendaraan = new ArrayList<>();
                            namaKendaraan.add("Pilih Kendaraan");
                            int selectedIndex = 0;

                            for (int i = 0; i < listKendaraan.size(); i++) {
                                KendaraanUserResponseModel.DataKendaraanUser k = listKendaraan.get(i);
                                Log.d("CEK_KENDARAAN",
                                        k.getNamaLengkapMotor() + " | utama: " + k.isKendaraanUtama());
                                String nama = k.getNamaLengkapMotor();
                                if (nama == null) nama = "Tidak diketahui";
                                namaKendaraan.add(nama);

//                                spinnerKendaraanUser.setSelection(0);
                            }

                            adapter.clear();
                            adapter.addAll(namaKendaraan);
                            adapter.notifyDataSetChanged();

                            spinnerKendaraanUser.setSelection(selectedIndex);
                        }
                    }

                    @Override
                    public void onFailure(Call<KendaraanUserResponseModel> call, Throwable t) {
                        Toast.makeText(getContext(), "Gagal load kendaraan", Toast.LENGTH_SHORT).show();
                    }
                });

        spinnerKendaraanUser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    selectedIdKendaraan = null;
                    return;
                }

                KendaraanUserResponseModel.DataKendaraanUser selected = listKendaraan.get(position-1);
                selectedKendaraan = selected;
                selectedIdKendaraan = selected.getId();
                Log.d("SLECTED_ID", selectedIdKendaraan);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }

    private void hapusKendaraan() {

        SessionManager session = new SessionManager(context);
        String token = "Bearer " + session.getToken();

        apiService.hapusKendaraan(token, selectedIdKendaraan)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        if (response.isSuccessful()) {
                            dismiss();

                            if (listener != null){
                                listener.onBerhasilHapus();
                            }
                        } else {
                            Toast.makeText(context, "Gagal hapus", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void showKonfirmasiHapus(){
        dismiss();
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_konfirmasi_hapus);
        btnKonfirmHapus = dialog.findViewById(R.id.btnLanjutanHapus);
        btnBatal = dialog.findViewById(R.id.btn_batal_hapus);

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
            hapusKendaraan();
        });

        dialog.show();
    }


}



















