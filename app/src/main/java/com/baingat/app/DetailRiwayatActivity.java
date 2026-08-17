package com.baingat.app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class DetailRiwayatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_riwayat);

        // Header back button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Views
        TextView tvDetailTipe = findViewById(R.id.tvDetailTipe);
        TextView tvDetailTanggal = findViewById(R.id.tvDetailTanggal);
        TextView tvDetailStatus = findViewById(R.id.tvDetailStatus);
        TextView tvDetailDeskripsi = findViewById(R.id.tvDetailDeskripsi);
        
        View layoutCatatan = findViewById(R.id.layoutCatatan);
        TextView tvDetailCatatan = findViewById(R.id.tvDetailCatatan);
        
        View layoutLokasi = findViewById(R.id.layoutLokasi);
        TextView tvDetailLokasi = findViewById(R.id.tvDetailLokasi);

        // Shimmer views
        com.facebook.shimmer.ShimmerFrameLayout shimmerDetailRiwayat = findViewById(R.id.shimmerDetailRiwayat);
        View layoutMainContent = findViewById(R.id.layoutMainContent);

        // Retrieve data from Intent
        String tipe = getIntent().getStringExtra("tipe");
        String tanggal = getIntent().getStringExtra("tanggal");
        String status = getIntent().getStringExtra("status");
        String deskripsi = getIntent().getStringExtra("deskripsi");
        String pesan = getIntent().getStringExtra("pesan");
        String lokasi = getIntent().getStringExtra("lokasi");
        String namaPelapor = getIntent().getStringExtra("namaPelapor");
        
        TextView tvDetailNamaPelapor = findViewById(R.id.tvDetailNamaPelapor);
        if (namaPelapor != null && !namaPelapor.trim().isEmpty()) {
            tvDetailNamaPelapor.setText(namaPelapor);
        } else {
            tvDetailNamaPelapor.setText("Anonim");
        }

        // Set text
        if (tipe != null) tvDetailTipe.setText(tipe);
        if (tanggal != null) tvDetailTanggal.setText(tanggal);
        if (deskripsi != null) tvDetailDeskripsi.setText(deskripsi);
        
        if (lokasi != null && !lokasi.isEmpty()) {
            layoutLokasi.setVisibility(View.VISIBLE);
            tvDetailLokasi.setText(lokasi);
        } else {
            layoutLokasi.setVisibility(View.GONE);
        }

        SessionManager sessionManager = new SessionManager(this);
        String role = sessionManager.getUserDetails().get(SessionManager.KEY_ROLE);
        boolean isAdmin = "admin".equalsIgnoreCase(role);

        View layoutAdminEdit = findViewById(R.id.layoutAdminEdit);
        android.widget.Spinner spinnerEditStatus = findViewById(R.id.spinnerEditStatus);
        android.widget.EditText etEditCatatan = findViewById(R.id.etEditCatatan);
        TextView btnSimpanPerubahan = findViewById(R.id.btnSimpanPerubahan);

        if (isAdmin) {
            tvDetailStatus.setVisibility(View.GONE);
            layoutCatatan.setVisibility(View.GONE);
            layoutAdminEdit.setVisibility(View.VISIBLE);

            String[] statusArray = {"Diproses", "Diterima", "Ditolak"};
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statusArray);
            spinnerEditStatus.setAdapter(adapter);

            if (status != null) {
                for (int i = 0; i < statusArray.length; i++) {
                    if (status.equalsIgnoreCase(statusArray[i]) || (status.equalsIgnoreCase("Selesai") && statusArray[i].equals("Diterima")) || (status.equalsIgnoreCase("Dibatalkan") && statusArray[i].equals("Ditolak"))) {
                        spinnerEditStatus.setSelection(i);
                        break;
                    }
                }
            }

            if (pesan != null && !pesan.isEmpty() && !pesan.equals("Sedang diproses admin")) {
                etEditCatatan.setText(pesan);
            }

            btnSimpanPerubahan.setOnClickListener(v -> {
                String idLaporan = getIntent().getStringExtra("idLaporan");
                if (idLaporan == null || idLaporan.isEmpty()) {
                    android.util.Log.d("AppLog", "ID Laporan tidak ditemukan");
                    return;
                }
                
                String selectedStatus = spinnerEditStatus.getSelectedItem().toString();
                String inputCatatan = etEditCatatan.getText().toString();
                
                android.app.ProgressDialog pd = new android.app.ProgressDialog(this);
                pd.setMessage("Menyimpan perubahan...");
                pd.show();

                String token = "Bearer " + sessionManager.getToken();
                ApiService apiService = ApiClient.getClient().create(ApiService.class);
                
                retrofit2.Call<ResponseBody> call;
                if ("Kondisi Jalan".equalsIgnoreCase(tipe)) {
                    call = apiService.updateStatusLaporanJalan(token, idLaporan, selectedStatus, inputCatatan);
                } else if ("Permintaan Lokasi".equalsIgnoreCase(tipe)) {
                    call = apiService.updateStatusPermintaanLokasi(token, idLaporan, selectedStatus, inputCatatan);
                } else if (tipe != null && tipe.toLowerCase().contains("kendaraan")) {
                    call = apiService.updateStatusLaporanKendaraan(token, idLaporan, selectedStatus, inputCatatan);
                } else {
                    android.util.Log.d("AppLog", "Tipe laporan tidak dikenali");
                    pd.dismiss();
                    return;
                }
                
                call.enqueue(new retrofit2.Callback<ResponseBody>() {
                    @Override
                    public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                        pd.dismiss();
                        if (response.isSuccessful()) {
                            BerhasilDialog dialog = new BerhasilDialog();
                            dialog.show(getSupportFragmentManager(), "BerhasilDialog");
                            new android.os.Handler().postDelayed(() -> {
                                finish();
                            }, 1500);
                        } else {
                            GagalDialog dialog = new GagalDialog();
                            dialog.show(getSupportFragmentManager(), "GagalDialog");
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                        pd.dismiss();
                        android.util.Log.d("AppLog", "Error Jaringan: " + t.getMessage());
                    }
                });
            });

        } else {
            layoutAdminEdit.setVisibility(View.GONE);
            if (status != null) {
                tvDetailStatus.setVisibility(View.VISIBLE);
                tvDetailStatus.setText(status);
                if (status.equalsIgnoreCase("Diterima") || status.equalsIgnoreCase("Selesai")) {
                    tvDetailStatus.setTextColor(Color.parseColor("#10B981")); // Hijau
                } else if (status.equalsIgnoreCase("Diproses")) {
                    tvDetailStatus.setTextColor(Color.parseColor("#F59E0B")); // Oranye
                } else if (status.equalsIgnoreCase("Ditolak") || status.equalsIgnoreCase("Dibatalkan")) {
                    tvDetailStatus.setTextColor(Color.parseColor("#EF4444")); // Merah
                } else {
                    tvDetailStatus.setTextColor(Color.parseColor("#6B7280")); // Abu-abu (Menunggu)
                }
                
                if (pesan != null && !pesan.isEmpty()) {
                    layoutCatatan.setVisibility(View.VISIBLE);
                    tvDetailCatatan.setText(pesan);
                } else {
                    layoutCatatan.setVisibility(View.GONE);
                }
            }
        }
        
        String foto = getIntent().getStringExtra("foto");
        ImageView ivDetailGambar = findViewById(R.id.ivDetailGambar);
        if (foto != null && !foto.isEmpty()) {
            ivDetailGambar.setVisibility(View.VISIBLE);
            final String finalUrlFoto = foto.startsWith("http") ? foto : ApiClient.BASE_URL + foto;
            Glide.with(this)
                .load(finalUrlFoto)
                .into(ivDetailGambar);
                
            ivDetailGambar.setOnClickListener(v -> {
                android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                ImageView fullscreenImage = new ImageView(this);
                fullscreenImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT));
                fullscreenImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                
                Glide.with(this)
                    .load(finalUrlFoto)
                    .into(fullscreenImage);
                    
                fullscreenImage.setOnClickListener(img -> dialog.dismiss());
                dialog.setContentView(fullscreenImage);
                dialog.show();
            });
        } else {
            ivDetailGambar.setVisibility(View.GONE);
        }

        // Tampilkan Shimmer selama 800ms sebelum memunculkan konten
        new android.os.Handler().postDelayed(() -> {
            if (shimmerDetailRiwayat != null) {
                shimmerDetailRiwayat.stopShimmer();
                shimmerDetailRiwayat.setVisibility(View.GONE);
            }
            if (layoutMainContent != null) {
                layoutMainContent.setVisibility(View.VISIBLE);
            }
        }, 800);
    }
}
