package com.example.skripsi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.Dialog;
import android.content.Intent; // Import wajib untuk pindah halaman
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {
    LinearLayout pesanValidasi, bgValidasi;
    TextView tvMessage;
    ImageView iconValidasi;
    ProgressBar progressBar;
    Dialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        pesanValidasi = findViewById(R.id.topMessage);
        tvMessage = findViewById(R.id.tvMessage);
        bgValidasi = findViewById(R.id.bg_validasi);
        iconValidasi = findViewById(R.id.icon_validasi);
        TextView buttonDaftar = findViewById(R.id.textView8);
        Button buttonLogin = findViewById(R.id.btn_login);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);

        SessionManager sessionManager = new SessionManager(this);
        if (getIntent().getBooleanExtra("REGISTER_SUKSES", false)) {
            showTrue("Data berhasil disimpan");
        }

        buttonDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty()) {
                    showError("Email wajib diisi");

                } else if (password.isEmpty()) {
                    showError("Password wajib diisi");

                } else {
                    apiService.loginUser(email, password).enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                    LoginResponse res = response.body();
                                    if(res.getUser() != null){
                                        sessionManager.createLoginSession(
                                                res.getUser().getId(),
                                                res.getUser().getNama(),
                                                res.getUser().getEmail(),
                                                res.getToken(),
                                                res.getUser().getRole()
                                        );

                                        String role = res.getUser().getRole();
                                        if (role.equals("User")){
                                            startActivity(new Intent(MainActivity.this, navbar_utama.class));
                                            finish();
                                        }
                                } else {
                                    showError(response.body().getMessage());
                                }

                            } else {
                                try {
                                    String error = response.errorBody().string();
                                    Log.d("API_ERROR", error);
                                    showError("Email/password salah");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    showError("Terjadi kesalahan");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<LoginResponse> call, Throwable t) {
                            showError("Tidak bisa konek ke server");

                        }
                    });
                }
            }
        });

    }

    private void showError(String message) {
        pesanValidasi.clearAnimation();
        pesanValidasi.animate().cancel();
        pesanValidasi.setVisibility(View.VISIBLE);
        tvMessage.setText(message);

        // animasi turun
        pesanValidasi.setTranslationY(-300f);
        pesanValidasi.animate().translationY(0).setDuration(300).start();

        // hilang setelah 5 detik
        new Handler().postDelayed(() -> {
            pesanValidasi.animate().translationY(-300f).setDuration(300).withEndAction(() -> {
                pesanValidasi.setVisibility(View.GONE);
            }).start();
        }, 5000);
    }

    private void showTrue(String message) {
        pesanValidasi.clearAnimation();
        pesanValidasi.animate().cancel();
        pesanValidasi.setVisibility(View.VISIBLE);
        tvMessage.setText(message);
        bgValidasi.setBackgroundColor(getResources().getColor(R.color.backgroundvalidasibenar));
        tvMessage.setTextColor(getResources().getColor(R.color.hijautulisanaman));
        iconValidasi.setImageResource(R.drawable.berhasil_icon);

        // animasi turun
        pesanValidasi.setTranslationY(-300f);
        pesanValidasi.animate().translationY(0).setDuration(300).start();

        // hilang setelah 5 detik
        new Handler().postDelayed(() -> {
            pesanValidasi.animate().translationY(-300f).setDuration(300).withEndAction(() -> {
                pesanValidasi.setVisibility(View.GONE);
            }).start();
        }, 5000);
    }


}