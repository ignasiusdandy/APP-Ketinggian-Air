package com.baingat.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import java.net.URLEncoder;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.app.ProgressDialog;

public class RegisterActivity extends AppCompatActivity {

    private Dialog laporDialog;
    private ProgressDialog pd;

    private List<Kendaraan> listSemuaKendaraan = new ArrayList<>();

    // Adapter Spinner
    private ArrayAdapter<String> adapterJenis;
    private ArrayAdapter<String> adapterModel;

    // List String untuk ditampilkan di Spinner
    private final List<String> listNamaJenis = new ArrayList<>();
    private final List<String> listNamaModel = new ArrayList<>();
    ScrollView scrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrasi_akun);

        Window window = getWindow();

        window.setStatusBarColor(Color.TRANSPARENT);

        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        EditText etNama = findViewById(R.id.et_nama);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        EditText etPasswordConf = findViewById(R.id.et_passwordConfirm);
        EditText etPlatKendaraan = findViewById(R.id.et_plat_kendaraan);
        LinearLayout layoutPlat = findViewById(R.id.layoutPlat);
        TextView tvInfo = findViewById(R.id.tvInfoPlat);
        TextView tvCounter = findViewById(R.id.tvCounterPlat);
        ImageView imgStatus = findViewById(R.id.imgStatusPlat);
        ImageView imgInfo = findViewById(R.id.imgInfoPlat);
        MaterialButton btnDaftar = findViewById(R.id.btn_daftar);

        // Ketika salah
        TextView wrongNama = findViewById(R.id.wrongNama);
        TextView wrongEmail = findViewById(R.id.wrongEmail);
        TextView wrongPass = findViewById(R.id.wrongPass);
        TextView wrongPassConf = findViewById(R.id.wrongPassConf);
        TextView wrongJenis = findViewById(R.id.wrongJenis);
        TextView wrongModel = findViewById(R.id.wrongModel);
        hideErrorOnType(etNama, wrongNama);
        hideErrorOnType(etEmail, wrongEmail);
        hideErrorOnType(etPassword, wrongPass);
        hideErrorOnType(etPasswordConf, wrongPassConf);
        initUppercase();
        setupPlatValidation(
                etPlatKendaraan,
                layoutPlat,
                tvInfo,
                tvCounter,
                imgStatus,
                imgInfo
        );


        // Ini untuk view saat keyboard
        scrollView = findViewById(R.id.scrollView);
        etEmail.setOnFocusChangeListener((v3, hasFocus) -> {
            if (hasFocus) scrollToView(v3);
        });

        etPassword.setOnFocusChangeListener((v3, hasFocus) -> {
            if (hasFocus) scrollToView(v3);
        });
        etPasswordConf.setOnFocusChangeListener((v3, hasFocus) -> {
            if (hasFocus) scrollToView(v3);
        });

        View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {

            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);

            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            // kalau keyboard muncul
            if (keypadHeight > screenHeight * 0.15) {

                ViewGroup.MarginLayoutParams params =
                        (ViewGroup.MarginLayoutParams) btnDaftar.getLayoutParams();

                params.bottomMargin = dpToPx(320); // 🔥 naikkan margin
                btnDaftar.setLayoutParams(params);

            } else {
                // keyboard hilang
                ViewGroup.MarginLayoutParams params =
                        (ViewGroup.MarginLayoutParams) btnDaftar.getLayoutParams();

                params.bottomMargin = dpToPx(120); // balik normal
                btnDaftar.setLayoutParams(params);
            }
        });

        CustomSpinner spinnerMotor = findViewById(R.id.spinner_motor);
        CustomSpinner spinnerModel = findViewById(R.id.spinner_model_motor);
        final ImageView arrowMotor = findViewById(R.id.iv_arrow_motor);
        final ImageView arrowModel = findViewById(R.id.iv_arrow_model);
        
        TextView tvLaporKendaraan = findViewById(R.id.tv_lapor_kendaraan);
        tvLaporKendaraan.setOnClickListener(v -> {
            showLaporAnonimDialog();
        });

        listNamaJenis.add("Pilih Jenis Motor"); // Placeholder saat loading
        adapterJenis = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listNamaJenis);
        adapterJenis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMotor.setAdapter(adapterJenis);

        // Animasi Panah Spinner 1
        spinnerMotor.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened(androidx.appcompat.widget.AppCompatSpinner spinner) {
                arrowMotor.animate().rotation(0).setDuration(300).start();
            }
            @Override
            public void onSpinnerClosed(androidx.appcompat.widget.AppCompatSpinner spinner) {
                arrowMotor.animate().rotation(-90).setDuration(300).start();
            }
        });

        listNamaModel.add("Pilih Jenis Dulu");
        adapterModel = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listNamaModel);
        adapterModel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModel.setAdapter(adapterModel);

        // Animasi Panah Spinner 2
        spinnerModel.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened(androidx.appcompat.widget.AppCompatSpinner spinner) {
                arrowModel.animate().rotation(0).setDuration(300).start();
            }
            @Override
            public void onSpinnerClosed(androidx.appcompat.widget.AppCompatSpinner spinner) {
                arrowModel.animate().rotation(-90).setDuration(300).start();
            }
        });

        spinnerMotor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String jenisTerpilih = spinnerMotor.getSelectedItem().toString();
                if (!jenisTerpilih.contains("Pilih") && !jenisTerpilih.contains("Loading")) {
                    wrongJenis.setVisibility(View.GONE);
                }
                filterModelBerdasarkanJenis(jenisTerpilih);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String modelTerpilih = parent.getItemAtPosition(position).toString();

                if (!modelTerpilih.contains("Pilih")) {
                    wrongModel.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ambilDataKendaraanDariServer();


        btnDaftar.setOnClickListener(v -> {
            String nama = etNama.getText().toString();
            String email = etEmail.getText().toString();
            String pass = etPassword.getText().toString();
            String plat = etPlatKendaraan.getText().toString();
            String passConf = etPasswordConf.getText().toString();
            String motor = spinnerMotor.getSelectedItem().toString();
            String model = spinnerModel.getSelectedItem().toString();
            wrongNama.setVisibility(View.GONE);
            wrongEmail.setVisibility(View.GONE);
            wrongPass.setVisibility(View.GONE);
            wrongPassConf.setVisibility(View.GONE);
            wrongJenis.setVisibility(View.GONE);
            wrongModel.setVisibility(View.GONE);
            View firstErrorView = null;

            if (nama.isEmpty()) {
                wrongNama.setVisibility(View.VISIBLE);
                if (firstErrorView == null) firstErrorView = etNama;
            }

            if (email.isEmpty()) {
                wrongEmail.setVisibility(View.VISIBLE);
                if (firstErrorView == null) firstErrorView = etEmail;
            }

            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                wrongEmail.setText("Format email tidak valid");
                wrongEmail.setVisibility(View.VISIBLE);
                if (firstErrorView == null) firstErrorView = etEmail;
            }

            if (pass.isEmpty()) {
                wrongPass.setVisibility(View.VISIBLE);
                if (firstErrorView == null) firstErrorView = etPassword;
            } else if(pass.length() < 8){
                wrongPass.setText("Password minimal 8 karakter");
                wrongPass.setVisibility(View.VISIBLE);
                if (firstErrorView == null) firstErrorView = etPassword;
            } else if (!pass.equals(passConf) && !passConf.isEmpty()) {
                wrongPass.setText("Password tidak sama");
                wrongPass.setVisibility(View.VISIBLE);
                if (firstErrorView == null) firstErrorView = etPassword;
            }

            if (passConf.isEmpty()) {
                wrongPassConf.setVisibility(View.VISIBLE);
                if (firstErrorView == null) firstErrorView = etPasswordConf;
            } else if (passConf.length() < 8) {
                wrongPassConf.setText("Konfirmasi password minimal 8 karakter");
                wrongPassConf.setVisibility(View.VISIBLE);
                if (firstErrorView == null) firstErrorView = etPasswordConf;
            } else if (!pass.equals(passConf) && !pass.isEmpty()) {
                wrongPassConf.setText("Password tidak sama");
                wrongPassConf.setVisibility(View.VISIBLE);
                if (firstErrorView == null) firstErrorView = etPasswordConf;
            }

            String regex =
                    "^[A-Z]{1,2}\\s\\d{1,4}\\s[A-Z]{1,3}$";

            if (!plat.matches(regex)) {

                if (firstErrorView == null) {
                    firstErrorView = etPlatKendaraan;
                }
            }

            if (motor.contains("Pilih")) {
                wrongJenis.setVisibility(View.VISIBLE);
                if (firstErrorView == null) firstErrorView = (View) spinnerMotor.getParent();
            }

            if (model.contains("Pilih")) {
                wrongModel.setVisibility(View.VISIBLE);
                if (firstErrorView == null) firstErrorView = (View) spinnerModel.getParent();
            }

            ScrollView scrollView = findViewById(R.id.scrollView);

            if (firstErrorView != null) {
                firstErrorView.requestFocus();

                return;
            }

            String idKendaraanYangAkanDikirim = null;
            for (Kendaraan k : listSemuaKendaraan) {
                if (k.getJenis_motor().equals(motor) && k.getModel_motor().equals(model)) {
                    idKendaraanYangAkanDikirim = k.getId_kendaraan();
                    break;
                }
            }

            if (idKendaraanYangAkanDikirim == null) {
                android.util.Log.d("AppLog", String.valueOf("Data kendaraan tidak valid!"));
                return;
            }
            prosesRegister(nama, email, pass, idKendaraanYangAkanDikirim, plat, motor, model);
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void scrollToView(View view) {
        scrollView.post(() -> {
            scrollView.smoothScrollTo(0, view.getTop() + 400);
        });
    }


    private void initUppercase(){
        EditText etPlatKendaraan = findViewById(R.id.et_plat_kendaraan);

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

    private void setupPlatValidation(
            EditText etPlat,
            LinearLayout layoutPlat,
            TextView tvInfo,
            TextView tvCounter,
            ImageView imgStatus,
            ImageView imgInfo
    ) {

        etPlat.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start,
                                          int count,
                                          int after) {}

            @Override
            public void onTextChanged(CharSequence s,
                                      int start,
                                      int before,
                                      int count) {

                String plat = s.toString()
                        .trim()
                        .toUpperCase();

                String cleanPlat =
                        plat.replace(" ", "");

                tvCounter.setText(
                        cleanPlat.length() + "/9"
                );

                String regex =
                        "^[A-Z]{1,2}\\s\\d{1,4}\\s[A-Z]{1,3}$";

                // NORMAL
                if (plat.isEmpty()) {

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
                            Color.parseColor("#2563EB")
                    );

                    tvInfo.setText(
                            "Format: 2 huruf - 4 angka - 3 huruf"
                    );
                }

                // VALID
                else if (plat.matches(regex)) {

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
                            Color.parseColor("#22C55E")
                    );

                    tvInfo.setText(
                            "Format plat kendaraan valid"
                    );
                }

                // ERROR
                else {

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
                            Color.parseColor("#EF4444")
                    );

                    tvInfo.setText(
                            "Format plat tidak valid. Gunakan format: DA 1234 XYZ"
                    );
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


    private void ambilDataKendaraanDariServer() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<KendaraanResponse> call = apiService.getDataKendaraan();

        call.enqueue(new Callback<KendaraanResponse>() {
            @Override
            public void onResponse(Call<KendaraanResponse> call, Response<KendaraanResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listSemuaKendaraan = response.body().getData();

                    Set<String> unikJenis = new HashSet<>();
                    for (Kendaraan k : listSemuaKendaraan) {
                        unikJenis.add(k.getJenis_motor());
                    }

                    List<String> listJenisSort = new ArrayList<>(unikJenis);
                    Collections.sort(listJenisSort);

                    listNamaJenis.clear();
                    listNamaJenis.add("Pilih Jenis Motor"); // Default
                    listNamaJenis.addAll(listJenisSort);

                    // Refresh Spinner
                    adapterJenis.notifyDataSetChanged();

                } else {
                    android.util.Log.d("AppLog", String.valueOf("Gagal ambil data motor"));
                }
            }

            @Override
            public void onFailure(Call<KendaraanResponse> call, Throwable t) {
                listNamaJenis.clear();
                listNamaJenis.add("Pilih Jenis Motor");
                adapterJenis.notifyDataSetChanged();
            }
        });
    }
    private void filterModelBerdasarkanJenis(String jenis) {
        listNamaModel.clear();

        if (jenis.equals("Pilih Jenis Motor") || jenis.equals("Loading data...")) {
            listNamaModel.add("Pilih Jenis Dulu");
        } else {
            listNamaModel.add("Pilih Model");
            List<String> tempModel = new ArrayList<>();
            for (Kendaraan k : listSemuaKendaraan) {
                if (k.getJenis_motor().equals(jenis)) {
                    tempModel.add(k.getModel_motor());
                }
            }

            Collections.sort(tempModel);
            listNamaModel.addAll(tempModel);
        }
        adapterModel.notifyDataSetChanged();
        // Reset pilihan ke posisi 0
        CustomSpinner spinnerModel = findViewById(R.id.spinner_model_motor);
        spinnerModel.setSelection(0);
    }
    private void prosesRegister(String nama, String email, String pass, String idKendaraan, String plat, String motorText, String modelText){
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Call<ResponseBody> call = apiService.registerUser(nama, email, pass, idKendaraan, plat);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful() && response.body() != null){
                    Dialog dialog = new Dialog(RegisterActivity.this);
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
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.putExtra("REGISTER_SUKSES", true);
                        startActivity(intent);
                    });
                    dialog.show();
                }
                else {
                    try {
                        String errorBody = response.errorBody().string();
                        TextView wrongEmail = findViewById(R.id.wrongEmail);
                        ScrollView scrollView = findViewById(R.id.scrollView);
                        TextView etEmail = findViewById(R.id.et_email);
                        // cek kalau error dari email
                        if (errorBody.toLowerCase().contains("email")) {
                            wrongEmail.setText("Email sudah terdaftar");
                            wrongEmail.setVisibility(View.VISIBLE);
                            etEmail.requestFocus();

                            scrollView.post(() -> {
                                scrollView.smoothScrollTo(0, etEmail.getTop());
                            });

                            return;
                        } else {
                            android.util.Log.d("AppLog", String.valueOf("Gagal: " + response.code()));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                android.util.Log.d("AppLog", String.valueOf("Eror Koneksi :" + t.getMessage()));
                Log.e("Retrofit Error", t.getMessage());
            }
        });

    }

    // Ini menghide jika user sudah edit
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

    private void showLaporAnonimDialog() {
        if (isFinishing() || isDestroyed()) return;

        laporDialog = new Dialog(this);
        laporDialog.setContentView(R.layout.popup_lapor_anonim);
        Window window = laporDialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.7f); // Menggelapkan background sebanyak 70%
        }
        
        EditText etDeskripsi = laporDialog.findViewById(R.id.etDeskripsiKendaraan);
        MaterialButton btnKirim = laporDialog.findViewById(R.id.btnKirimKendaraan);
        ImageView btnClose = laporDialog.findViewById(R.id.btnClose);
        
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                if (laporDialog != null && laporDialog.isShowing()) laporDialog.dismiss();
            });
        }

        btnKirim.setOnClickListener(v -> {
            String deskripsi = etDeskripsi.getText().toString();
            if (deskripsi.isEmpty()) {
                etDeskripsi.setError("Deskripsi tidak boleh kosong");
                return;
            }

            btnKirim.setEnabled(false);
            
            pd = new ProgressDialog(this);
            pd.setMessage("Mengirim laporan...");
            pd.setCancelable(false);
            if (!isFinishing() && !isDestroyed()) {
                pd.show();
            }

            String token = "Bearer USERANOM";
            String fullKeterangan = deskripsi;
            okhttp3.RequestBody ketBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), fullKeterangan);
            okhttp3.MultipartBody.Part fotoPart = null;

            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            apiService.laporKendaraan(token, ketBody, fotoPart).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (pd != null && pd.isShowing()) pd.dismiss();
                    
                    if (response.isSuccessful()) {
                        if (laporDialog != null && laporDialog.isShowing()) laporDialog.dismiss();
                        showStatusPopup("Berhasil", "Laporan berhasil dikirim! Terima kasih.", true);
                    } else {
                        btnKirim.setEnabled(true);
                        showStatusPopup("Gagal", "Gagal mengirim laporan", false);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    if (pd != null && pd.isShowing()) pd.dismiss();
                    btnKirim.setEnabled(true);
                    showStatusPopup("Error", "Error Jaringan: " + t.getMessage(), false);
                }
            });
        });

        if (!isFinishing() && !isDestroyed()) {
            laporDialog.show();
        }
    }

    private void showStatusPopup(String title, String message, boolean isSuccess) {
        if (isFinishing() || isDestroyed()) return;
        
        Dialog statusDialog = new Dialog(this);
        statusDialog.setContentView(isSuccess ? R.layout.popup_berhasil_tambah : R.layout.popup_gagal);
        
        Window window = statusDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        
        TextView tvPesan = statusDialog.findViewById(isSuccess ? R.id.tvPesanBerhasil : R.id.tvPesanGagal);
        if (tvPesan != null) tvPesan.setText(message);
        
        View btnLanjut = statusDialog.findViewById(isSuccess ? R.id.lanjutanBerhasil : R.id.lanjutanGagal);
        if (btnLanjut != null) {
            btnLanjut.setOnClickListener(v -> statusDialog.dismiss());
        }
        
        statusDialog.show();
    }

    @Override
    protected void onDestroy() {
        if (laporDialog != null && laporDialog.isShowing()) {
            laporDialog.dismiss();
        }
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
        super.onDestroy();
    }
}