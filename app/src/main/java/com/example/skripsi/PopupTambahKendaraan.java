package com.example.skripsi;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PopupTambahKendaraan extends Dialog {

    private Context context;

    private CustomSpinner spinnerJenis, spinnerModel;
    private ArrayAdapter<String> adapterJenis, adapterModel;

    private List<String> listJenis = new ArrayList<>();
    private List<String> listModel = new ArrayList<>();

    private String selectedIdKendaraan = null;

    private LinearLayout btnTambah;
    private ImageView btnClose;
    private EditText etPlatKendaraan;
    private TextView wrongPlat, wrongKategori, wrongModel;

    public PopupTambahKendaraan(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tambah_kendaraan_user);

        initView();
        initUppercase();
        hideErrorOnType(etPlatKendaraan, wrongPlat);
        setupSpinner();
        ambilDataKendaraan();
        setupAction();
    }

    private void initView() {
        spinnerJenis = findViewById(R.id.spinner_kategori);
        spinnerModel = findViewById(R.id.spinner_model);
        etPlatKendaraan = findViewById(R.id.et_plat_kendaraan);
        wrongKategori = findViewById(R.id.wrongKategori);
        wrongModel = findViewById(R.id.wrongModel);
        wrongPlat = findViewById(R.id.wrongPlat);

        btnTambah = findViewById(R.id.tambah_kendaraan);
        btnClose = findViewById(R.id.btn_batal);
    }

    private void initUppercase(){
        etPlatKendaraan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String upper = s.toString().toUpperCase();

                if (!s.toString().equals(upper)) {
                    etPlatKendaraan.removeTextChangedListener(this);
                    etPlatKendaraan.setText(upper);
                    etPlatKendaraan.setSelection(upper.length());
                    etPlatKendaraan.addTextChangedListener(this);
                }
            }
        });
    }

    private void hideErrorOnType(EditText editText, TextView errorView) {
        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                errorView.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void setupSpinner() {
        // ini untuk jenis dan model kendaraan
        listJenis.clear();
        listJenis.add("Loading...");
        adapterJenis = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item,
                listJenis);
        spinnerJenis.setAdapter(adapterJenis);

        listModel.clear();
        listModel.add("Pilih Jenis Dulu");
        adapterModel = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item,
                listModel);
        spinnerModel.setAdapter(adapterModel);

        spinnerJenis.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String jenis = spinnerJenis.getSelectedItem().toString();
                loadModel(jenis);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void ambilDataKendaraan() {

        if (KendaraanCache.isAvailable()) {
            setupJenis();
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getDataKendaraan().enqueue(new Callback<KendaraanResponse>() {
            @Override
            public void onResponse(Call<KendaraanResponse> call, Response<KendaraanResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    KendaraanCache.setData(response.body().getData());
                    setupJenis();

                } else {
                    Toast.makeText(context, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<KendaraanResponse> call, Throwable t) {
                Log.e("API_FAILURE", "Error getKendaraan: ", t);
            }
        });
    }

    private void setupJenis() {

        listJenis.clear();
        listJenis.add("Pilih Jenis Motor");
        listJenis.addAll(KendaraanCache.getJenisList());

        adapterJenis.notifyDataSetChanged();
    }

    private void loadModel(String jenis) {

        listModel.clear();

        if (jenis.equals("Pilih Jenis Motor") || jenis.equals("Loading...")) {
            listModel.add("Pilih Jenis Dulu");
        } else {

            listModel.add("Pilih Model");
            listModel.addAll(KendaraanCache.getModelList(jenis));

            if (listModel.size() > 1) {
                Collections.sort(listModel.subList(1, listModel.size()));
            }
        }

        adapterModel.notifyDataSetChanged();
        spinnerModel.setSelection(0);

        spinnerModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String model = listModel.get(position);

                if (!model.equals("Pilih Model")) {
                    selectedIdKendaraan = KendaraanCache.getIdByModel(model);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupAction() {

        btnClose.setOnClickListener(v -> dismiss());

        btnTambah.setOnClickListener(v -> {

            String plat = etPlatKendaraan.getText().toString().trim();
            wrongPlat.setVisibility(View.GONE);
            wrongKategori.setVisibility(View.GONE);
            wrongModel.setVisibility(View.GONE);
            boolean isValid = true;

            // cek plat
            if (plat.isEmpty()) {
                wrongPlat.setVisibility(View.VISIBLE);
                isValid = false;
            }

            // cek kategori
            String kategori = spinnerJenis.getSelectedItem().toString();
            if (kategori.equals("Pilih Jenis Motor") || kategori.equals("Loading...")) {
                wrongKategori.setVisibility(View.VISIBLE);
                isValid = false;
            }

            // cek model
            String model = spinnerModel.getSelectedItem().toString();
            if (model.equals("Pilih Model") || model.equals("Pilih Jenis Dulu")) {
                wrongModel.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (!isValid) return;

            tambahKendaraan(plat);
        });
    }

    private void tambahKendaraan(String plat) {

        SessionManager session = new SessionManager(context);
        String token = "Bearer " + session.getToken();
        Log.d("ID:", selectedIdKendaraan);
        Log.d("PLAT:", plat);


        TambahKendaraanRequestModel request =
                new TambahKendaraanRequestModel(selectedIdKendaraan, plat);

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.tambahKendaraan(token, request).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.popup_berhasil_tambah);
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
                    dismiss();
                } else {
                    try {
                        Log.e("STATUS_CODE", "Code: " + response.code());
                        Log.e("ERROR_BODY", response.errorBody().string());
                    } catch (Exception e) {
                        Log.e("ERROR_PARSE", e.getMessage());
                    }
                    Dialog dialog = new Dialog(context);
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
                    dismiss();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("API_FAILURE", "Error responseBody: ", t);
            }
        });
    }
}
