package com.example.skripsi;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent; // Import wajib untuk pindah halaman
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView buttonDaftar = findViewById(R.id.textView8);

//        sessionManager = new SessionManager(this);
//
//        // Cek apakah user sudah login?
//        if (!sessionManager.isLoggedIn()) {
//            // Kalau belum login, lempar balik ke Login/Register
//            // Intent intent = new Intent(this, RegisterActivity.class);
//            // startActivity(intent);
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
    }
}