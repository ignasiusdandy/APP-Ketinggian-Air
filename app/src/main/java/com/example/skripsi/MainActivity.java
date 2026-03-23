package com.example.skripsi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent; // Import wajib untuk pindah halaman
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView buttonDaftar = findViewById(R.id.textView8);
        Button buttonLogin = findViewById(R.id.btn_login);

        SessionManager sessionManager = new SessionManager(this);

        // Cek apakah user sudah login?
//        if (!SessionManager.isLoggedIn()) {
//            // Kalau belum login, lempar balik ke Login/Register
//             Intent intent = new Intent(this, RegisterActivity.class);
//             startActivity(intent);
//            // finish();
//        } else {
//            // Kalau sudah login, ambil datanya
//            HashMap<String, String> user = sessionManager.getUserDetails();
//            String namaUser = user.get(SessionManager.KEY_NAMA);
//            String motorUser = user.get(SessionManager.KEY_MOTOR);
//
//            // Tampilkan ke TextView, misal: "Halo, Dandy!"
//            // tvNama.setText("Halo, " + namaUser);
//        }

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
                Intent intent = new Intent(MainActivity.this, navbar_utama.class);
                startActivity(intent);
                finish();
            }
        });
    }
}