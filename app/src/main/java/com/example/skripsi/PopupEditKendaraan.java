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

public class PopupEditKendaraan extends Dialog {

    private Context context;

    private CustomSpinner spinnerJenis, spinnerModel, spinnerPemilik;
    private ArrayAdapter<String> adapterJenis, adapterModel;

    private List<String> listJenis = new ArrayList<>();
    private List<String> listModel = new ArrayList<>();

    private String selectedIdKendaraan = null;
//    private KendaraanUserResponseModel.DataKendaraanUser dataKendaraan;

    KendaraanTabelPengaturanModel data;
    private boolean isFirstLoad = true;
    private OnEditListener listener;


    private LinearLayout btnUpdate;
    private ImageView btnClose;
    private TextView wrongPlat, wrongKategori, wrongModel;
    private EditText etPlatKendaraan;
    private boolean isPlatValid = false;

    private LinearLayout layoutPlat;
    private ImageView imgInfo, imgStatus;

    private TextView tvCounter, tvInfo;

    public PopupEditKendaraan(Context context,
                              KendaraanTabelPengaturanModel data,
                              OnEditListener listener) {
        super(context);
        this.context = context;
        this.data = data;
        this.listener = listener;
    }

    public interface OnEditListener {
        void onBerhasilEdit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_edit_kendaraan_user);

        initView();
        etPlatKendaraan.setText(data.getPlat());
        String platAwal = data.getPlat().trim().toUpperCase();
        String regex =
                "^[A-Z]{1,2}\\s\\d{1,4}\\s[A-Z]{1,3}$";

        isPlatValid =
                platAwal.matches(regex);
        setupSpinner();
        initUppercase();
        setupPlatValidation();
        etPlatKendaraan.setText(
                etPlatKendaraan.getText().toString()
        );
        updateButtonState();
//        hideErrorOnType(etPlatKendaraan, wrongPlat);
        ambilDataKendaraan();
        setupAction();
    }

    private void initView() {
        spinnerJenis = findViewById(R.id.spinner_kategori);
        spinnerModel = findViewById(R.id.spinner_model);
        btnUpdate = findViewById(R.id.update_kendaraan);
        etPlatKendaraan = findViewById(R.id.et_plat_kendaraan);
        btnClose = findViewById(R.id.btn_batal);
        wrongKategori = findViewById(R.id.wrongKategori);
        wrongModel = findViewById(R.id.wrongModel);
//        wrongPlat = findViewById(R.id.wrongPlat);
        layoutPlat = findViewById(R.id.layoutPlat);
        tvCounter = findViewById(R.id.tvCounterPlat);
        tvInfo = findViewById(R.id.tvInfoPlat);
        imgStatus = findViewById(R.id.imgStatusPlat);
        imgInfo = findViewById(R.id.imgInfoPlat);
        btnUpdate.setEnabled(false);
        btnUpdate.setAlpha(0.5f);
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

    private void setupPlatValidation() {

        etPlatKendaraan.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s,
                                                  int start,
                                                  int count,
                                                  int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s,
                                              int start,
                                              int before,
                                              int count) {

                        String plat =
                                s.toString()
                                        .trim()
                                        .toUpperCase();

                        String cleanPlat =
                                plat.replace(" ", "");

                        tvCounter.setText(
                                cleanPlat.length() + "/9"
                        );

                        String regex =
                                "^[A-Z]{1,2}\\s\\d{1,4}\\s[A-Z]{1,3}$";

                        // KOSONG
                        if (plat.isEmpty()) {

                            isPlatValid = false;

                            layoutPlat.setBackgroundResource(
                                    R.drawable.bg_plat_normal
                            );

                            imgStatus.setImageResource(
                                    R.drawable.tentang_icon
                            );

                            imgInfo.setImageResource(
                                    R.drawable.tentang_icon
                            );

                            tvInfo.setTextColor(
                                    android.graphics.Color.parseColor("#2563EB")
                            );

                            tvInfo.setText(
                                    "Format: 2 huruf - 4 angka - 3 huruf"
                            );
                        }

                        // VALID
                        else if (plat.matches(regex)) {

                            isPlatValid = true;

                            layoutPlat.setBackgroundResource(
                                    R.drawable.bg_plat_success
                            );

                            imgStatus.setImageResource(
                                    R.drawable.aman_icon
                            );

                            imgInfo.setImageResource(
                                    R.drawable.aman_icon
                            );

                            tvInfo.setTextColor(
                                    android.graphics.Color.parseColor("#22C55E")
                            );

                            tvInfo.setText(
                                    "Format plat kendaraan valid"
                            );
                        }

                        // ERROR
                        else {

                            isPlatValid = false;

                            layoutPlat.setBackgroundResource(
                                    R.drawable.bg_plat_error
                            );

                            imgStatus.setImageResource(
                                    R.drawable.peringatan_icon
                            );

                            imgInfo.setImageResource(
                                    R.drawable.peringatan_icon
                            );

                            tvInfo.setTextColor(
                                    android.graphics.Color.parseColor("#EF4444")
                            );

                            tvInfo.setText(
                                    "Format plat tidak valid. Gunakan format: DA 1234 XYZ"
                            );
                        }

                        updateButtonState();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
    }

    private void updateButtonState() {

        if (spinnerJenis.getSelectedItem() == null
                || spinnerModel.getSelectedItem() == null) {

            btnUpdate.setEnabled(false);
            btnUpdate.setAlpha(0.5f);
            return;
        }

        String kategori =
                spinnerJenis.getSelectedItem().toString();

        String model =
                spinnerModel.getSelectedItem().toString();

        boolean kategoriValid =
                !kategori.equals("Pilih Jenis Motor")
                        && !kategori.equals("Pilih Jenis Dulu")
                        && !kategori.equals("Loading...");

        boolean modelValid =
                !model.equals("Pilih Model")
                        && !model.equals("Pilih Jenis Dulu");

        boolean enableButton =
                isPlatValid
                        && kategoriValid
                        && modelValid;

        btnUpdate.setEnabled(enableButton);

        btnUpdate.setAlpha(
                enableButton ? 1f : 0.5f
        );
    }

    private void setupSpinner() {

        // ini untuk jenis dan model kendaraan
        listJenis.clear();
        listJenis.add("Pilih Jenis Dulu");
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
                updateButtonState();
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
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupJenis() {

        listJenis.clear();
        listJenis.add("Pilih Jenis Motor");
        listJenis.addAll(KendaraanCache.getJenisList());
        adapterJenis.notifyDataSetChanged();
        String jenisTerpilih = data.getKategori();
        int indexJenis = listJenis.indexOf(jenisTerpilih);

        if(indexJenis >= 0){
            spinnerJenis.setSelection(indexJenis);
        }
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
                updateButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (isFirstLoad) {
            String idModel = data.getIdKendaraan();

            for (int i = 0; i < listModel.size(); i++) {
                String model = listModel.get(i);

                String id = KendaraanCache.getIdByModel(model);

                if (id != null && id.equals(idModel)) {
                    spinnerModel.setSelection(i);
                    selectedIdKendaraan = id;
                    break;
                }
            }
        }
    }

    private void setupAction() {

        btnClose.setOnClickListener(v -> dismiss());

        btnUpdate.setOnClickListener(v -> {
            String plat = etPlatKendaraan.getText().toString().trim();

//            wrongPlat.setVisibility(View.GONE);
            wrongKategori.setVisibility(View.GONE);
            wrongModel.setVisibility(View.GONE);

            boolean isValid = true;

//            if (plat.isEmpty()) {
//                wrongPlat.setVisibility(View.VISIBLE);
//                isValid = false;
//            }

            String kategori = spinnerJenis.getSelectedItem().toString();
            if (kategori.equals("Pilih Jenis Motor") || kategori.equals("Pilih Jenis Dulu")) {
                wrongKategori.setVisibility(View.VISIBLE);
                isValid = false;
            }

            String model = spinnerModel.getSelectedItem().toString();
            if (model.equals("Pilih Model") || model.equals("Pilih Jenis Dulu")) {
                wrongModel.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (!isValid) return;

            updateKendaraan(plat);
        });
    }


    private void updateKendaraan(String plat) {
        SessionManager session = new SessionManager(context);
        String token = "Bearer " + session.getToken();
        EditKendaraanRequestModel request =
                new EditKendaraanRequestModel(selectedIdKendaraan, plat);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.updateKendaraan(token, data.getIdKendaraan(),request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                Log.d("EDIT_DEBUG", "CODE: " + response.code());
//                Log.d("EDIT_DEBUG", "MESSAGE: " + response.message());
                if (response.isSuccessful()) {
                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.popup_berhasil);
                    dialog.getWindow().setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

                    //bikin agak gelap
                    dialog.getWindow().setDimAmount(0.8f);
                    // bikin transparan agar bisa diliat corner radiusnya
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialog.show();

                    LinearLayout lanjutanBerhasil = dialog.findViewById(R.id.lanjutanBerhasil);
                    lanjutanBerhasil.setOnClickListener(v -> {
                        dialog.dismiss();
                    });

                    if (listener != null) {
                        listener.onBerhasilEdit();
                    }

                    dismiss();
                } else {
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
                    dismiss();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}