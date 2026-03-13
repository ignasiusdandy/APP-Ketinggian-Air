package com.example.skripsi;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
// import android.widget.Toast; // Uncomment jika butuh Toast
// import android.view.View;    // Uncomment jika butuh View

public class RegisterActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:3001/";
    private List<Kendaraan> listSemuaKendaraan = new ArrayList<>();

    // Adapter Spinner
    private ArrayAdapter<String> adapterJenis;
    private ArrayAdapter<String> adapterModel;

    // List String untuk ditampilkan di Spinner
    private List<String> listNamaJenis = new ArrayList<>();
    private List<String> listNamaModel = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrasi_akun);

        EditText etNama = findViewById(R.id.et_nama);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        EditText etPasswordConf = findViewById(R.id.et_passwordConfirm);
        MaterialButton btnDaftar = findViewById(R.id.btn_daftar);

        CustomSpinner spinnerMotor = findViewById(R.id.spinner_motor);
        CustomSpinner spinnerModel = findViewById(R.id.spinner_model_motor);
        final ImageView arrowMotor = findViewById(R.id.iv_arrow_motor);
        final ImageView arrowModel = findViewById(R.id.iv_arrow_model);

        listNamaJenis.add("Loading data..."); // Placeholder saat loading
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
                filterModelBerdasarkanJenis(jenisTerpilih);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ambilDataKendaraanDariServer();


        btnDaftar.setOnClickListener(v -> {
            String nama = etNama.getText().toString();
            String email = etEmail.getText().toString();
            String pass = etPassword.getText().toString();
            String passConf = etPasswordConf.getText().toString();
            String motor = spinnerMotor.getSelectedItem().toString();
            String model = spinnerModel.getSelectedItem().toString();

            if (nama.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Harap isi semua data!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(passConf)) {
                Toast.makeText(this, "Password tidak sama! Harap masukkan ulang", Toast.LENGTH_SHORT).show();
                return;
            }

            if (motor.contains("Pilih") || model.contains("Pilih")) {
                Toast.makeText(this, "Silahkan pilih motor & tahun!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Data kendaraan tidak valid!", Toast.LENGTH_SHORT).show();
                return;
            }
            prosesRegister(nama, email, pass, idKendaraanYangAkanDikirim, motor, model);
        });
    }

    private void ambilDataKendaraanDariServer() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
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

                    listNamaJenis.clear();
                    listNamaJenis.add("Pilih Jenis Motor"); // Default
                    listNamaJenis.addAll(unikJenis);

                    // Refresh Spinner
                    adapterJenis.notifyDataSetChanged();

                } else {
                    Toast.makeText(RegisterActivity.this, "Gagal ambil data motor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<KendaraanResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Error Koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                listNamaJenis.clear();
                listNamaJenis.add("Gagal Load Data");
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
            // Cari model yang cocok dengan jenis motor yang dipilih
            for (Kendaraan k : listSemuaKendaraan) {
                if (k.getJenis_motor().equals(jenis)) {
                    listNamaModel.add(k.getModel_motor());
                }
            }
        }
        adapterModel.notifyDataSetChanged();
        // Reset pilihan ke posisi 0
        CustomSpinner spinnerModel = findViewById(R.id.spinner_model_motor);
        spinnerModel.setSelection(0);
    }
    private void prosesRegister(String nama, String email, String pass, String idKendaraan, String motorText, String modelText){
        Retrofit retrofit =  new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<ResponseBody> call = apiService.registerUser(nama, email, pass, idKendaraan);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful() && response.body() != null){
                    SessionManager sessionManager = new SessionManager(RegisterActivity.this);

                    sessionManager.createLoginSession(nama, email, motorText, modelText);

                    Toast.makeText(RegisterActivity.this, "Berhasil " + response.body().getMessage(), Toast.LENGTH_LONG).show();
                }
                else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(RegisterActivity.this, "Gagal: " + response.code() + " " + errorBody, Toast.LENGTH_LONG).show();
                        Log.e("RegisterError", "Server Error: " + errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Eror Koneksi :" + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Retrofit Error", t.getMessage());
            }
        });

    }
}