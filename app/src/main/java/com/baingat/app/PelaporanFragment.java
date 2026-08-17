package com.baingat.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class PelaporanFragment extends Fragment {

    // Views Container
    private LinearLayout layoutPilihanPengaduan;
    private LinearLayout layoutFormKondisiJalan;
    private LinearLayout layoutFormPermintaanLokasi;
    private LinearLayout layoutFormKendaraan;

    // Header Back
    private View btnBack;
    private TextView tvHeaderTitle;
    private TextView tvHeaderDesc;

    // Opsi Buttons
    private LinearLayout btnOpsiKondisiJalan;
    private LinearLayout btnOpsiPermintaanLokasi;
    private LinearLayout btnOpsiKendaraanTidakAda;
    private LinearLayout btnLihatSemua;

    // Riwayat
    private RecyclerView rvRiwayat;
    private RiwayatAdapter riwayatAdapter;
    private List<RiwayatModel> riwayatList;

    // Lokasi Backend
    private List<LokasiResponseModel.Data> lokasiList = new ArrayList<>();
    private List<String> lokasiNames = new ArrayList<>();
    private ArrayAdapter<String> adapterLokasi;

    // Form Kondisi Jalan
    private Spinner spinnerLokasiJalan;
    private EditText etDeskripsiJalan;
    private LinearLayout btnUploadFotoJalan;
    private Button btnKirimJalan;

    // Form Permintaan Lokasi
    private LinearLayout btnDapatkanLokasi;
    private TextView tvLokasiTerkiniLabel;
    private TextView tvAlamatLokasi;
    private LinearLayout btnUploadFotoLokasi;
    private EditText etDeskripsiPermintaan;
    private Button btnKirimLokasi;

    // Form Kendaraan
    private Spinner spinnerJenisMasalah;
    private EditText etDeskripsiKendaraan;
    private LinearLayout btnUploadFotoKendaraan;
    private Button btnKirimKendaraan;

    // State Tracking
    private int currentState = 0; // 0: Menu, 1: Jalan, 2: Lokasi, 3: Kendaraan
    
    // Previews
    private android.widget.RelativeLayout previewContainerJalan, previewContainerLokasi, previewContainerKendaraan;
    private android.widget.ImageView imgPreviewJalan, imgPreviewLokasi, imgPreviewKendaraan;
    private android.widget.ImageButton btnHapusFotoJalan, btnHapusFotoLokasi, btnHapusFotoKendaraan;

    private Uri selectedImageUri = null;
    private Uri tempImageUri = null;

    
    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    updatePreviewUI();
                }
            });


    
    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && tempImageUri != null) {
                    selectedImageUri = tempImageUri;
                    updatePreviewUI();
                } else {
                    tempImageUri = null;
                }
            });


    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    checkAndFetchLocation();
                } else {
                    android.util.Log.d("AppLog", String.valueOf("Izin lokasi diperlukan"));
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setMessage("Aplikasi membutuhkan izin akses lokasi untuk menemukan posisi Anda. Mohon aktifkan izin lokasi di Pengaturan Aplikasi.")
                        .setPositiveButton("Pengaturan", (dialog, which) -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            android.net.Uri uri = android.net.Uri.fromParts("package", requireContext().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .setNegativeButton("Batal", null)
                        .show();
                }
            });

    public PelaporanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pelaporan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentState != 0) {
                    showMenu();
                } else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                    setEnabled(true);
                }
            }
        });

        initViews(view);
        setupListeners();
        setupSpinners();
        setupRiwayat();
        
        // Show main menu initially
        showMenu();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentState == 0) {
            fetchRiwayat();
        }
    }

    private void setupRiwayat() {
        rvRiwayat.setLayoutManager(new LinearLayoutManager(requireContext()));
        riwayatList = new ArrayList<>();
        riwayatAdapter = new RiwayatAdapter(riwayatList);
        rvRiwayat.setAdapter(riwayatAdapter);
    }

    // Shimmer
    private com.facebook.shimmer.ShimmerFrameLayout shimmerRiwayat;

    private void initViews(View view) {
        layoutPilihanPengaduan = view.findViewById(R.id.layoutPilihanPengaduan);
        layoutFormKondisiJalan = view.findViewById(R.id.layoutFormKondisiJalan);
        layoutFormPermintaanLokasi = view.findViewById(R.id.layoutFormPermintaanLokasi);
        layoutFormKendaraan = view.findViewById(R.id.layoutFormKendaraan);
        
        previewContainerJalan = view.findViewById(R.id.previewContainerJalan);
        imgPreviewJalan = view.findViewById(R.id.imgPreviewJalan);
        btnHapusFotoJalan = view.findViewById(R.id.btnHapusFotoJalan);
        
        previewContainerLokasi = view.findViewById(R.id.previewContainerLokasi);
        imgPreviewLokasi = view.findViewById(R.id.imgPreviewLokasi);
        btnHapusFotoLokasi = view.findViewById(R.id.btnHapusFotoLokasi);
        
        previewContainerKendaraan = view.findViewById(R.id.previewContainerKendaraan);
        imgPreviewKendaraan = view.findViewById(R.id.imgPreviewKendaraan);
        btnHapusFotoKendaraan = view.findViewById(R.id.btnHapusFotoKendaraan);

        btnBack = view.findViewById(R.id.btnBack);
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle);
        tvHeaderDesc = view.findViewById(R.id.tvHeaderDesc);

        btnOpsiKondisiJalan = view.findViewById(R.id.btnOpsiKondisiJalan);
        btnOpsiPermintaanLokasi = view.findViewById(R.id.btnOpsiPermintaanLokasi);
        btnOpsiKendaraanTidakAda = view.findViewById(R.id.btnOpsiKendaraanTidakAda);
        btnLihatSemua = view.findViewById(R.id.btnLihatSemua);
        
        rvRiwayat = view.findViewById(R.id.rvRiwayat);
        shimmerRiwayat = view.findViewById(R.id.shimmerRiwayat);
        View layoutRiwayatKosong = view.findViewById(R.id.layoutRiwayatKosong);

        // Form Jalan
        spinnerLokasiJalan = view.findViewById(R.id.spinnerLokasiJalan);
        etDeskripsiJalan = view.findViewById(R.id.etDeskripsiJalan);
        btnUploadFotoJalan = view.findViewById(R.id.btnUploadFotoJalan);
        btnKirimJalan = view.findViewById(R.id.btnKirimJalan);

        // Form Lokasi
        btnDapatkanLokasi = view.findViewById(R.id.btnDapatkanLokasi);
        tvLokasiTerkiniLabel = view.findViewById(R.id.tvLokasiTerkiniLabel);
        tvAlamatLokasi = view.findViewById(R.id.tvAlamatLokasi);
        btnUploadFotoLokasi = view.findViewById(R.id.btnUploadFotoLokasi);
        etDeskripsiPermintaan = view.findViewById(R.id.etDeskripsiPermintaan);
        btnKirimLokasi = view.findViewById(R.id.btnKirimLokasi);

        // Form Kendaraan
        spinnerJenisMasalah = view.findViewById(R.id.spinnerJenisMasalah);
        etDeskripsiKendaraan = view.findViewById(R.id.etDeskripsiKendaraan);
        btnUploadFotoKendaraan = view.findViewById(R.id.btnUploadFotoKendaraan);
        btnKirimKendaraan = view.findViewById(R.id.btnKirimKendaraan);
    }

    private boolean isFetchingRiwayat = false;

    private void fetchRiwayat() {
        if (isFetchingRiwayat) {
            return; // Sedang fetching, jangan di-double
        }
        isFetchingRiwayat = true;

        SessionManager sessionManager = new SessionManager(requireContext());
        String token = "Bearer " + sessionManager.getToken();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        riwayatList.clear();
        riwayatAdapter.notifyDataSetChanged();

        if (shimmerRiwayat != null) {
            shimmerRiwayat.setVisibility(View.VISIBLE);
            shimmerRiwayat.startShimmer();
            rvRiwayat.setVisibility(View.GONE);
            View layoutKosong = getView() != null ? getView().findViewById(R.id.layoutRiwayatKosong) : null;
            if (layoutKosong != null) layoutKosong.setVisibility(View.GONE);
        }

        java.util.concurrent.atomic.AtomicInteger pending = new java.util.concurrent.atomic.AtomicInteger(3);

        Runnable checkDone = () -> {
            if (pending.decrementAndGet() == 0) {
                java.util.Collections.sort(riwayatList, (r1, r2) -> {
                    if (r1.getTanggal() == null || r2.getTanggal() == null) return 0;
                    return r2.getTanggal().compareTo(r1.getTanggal());
                });
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (shimmerRiwayat != null) {
                            shimmerRiwayat.stopShimmer();
                            shimmerRiwayat.setVisibility(View.GONE);
                        }
                        riwayatAdapter.notifyDataSetChanged();
                        View layoutKosong = getView() != null ? getView().findViewById(R.id.layoutRiwayatKosong) : null;
                        if (layoutKosong != null) {
                            if (riwayatList.isEmpty()) {
                                rvRiwayat.setVisibility(View.GONE);
                                layoutKosong.setVisibility(View.VISIBLE);
                            } else {
                                rvRiwayat.setVisibility(View.VISIBLE);
                                layoutKosong.setVisibility(View.GONE);
                            }
                        }
                        isFetchingRiwayat = false;
                    });
                } else {
                    isFetchingRiwayat = false;
                }
            }
        };

        apiService.getSemuaLaporanJalan(token).enqueue(new retrofit2.Callback<LaporanJalanResponse>() {
            @Override
            public void onResponse(retrofit2.Call<LaporanJalanResponse> call, retrofit2.Response<LaporanJalanResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    for (LaporanJalanResponse.Data d : response.body().getData()) {
                        riwayatList.add(new RiwayatModel(d.getId(), "Kondisi Jalan", d.getKeterangan(), d.getCreatedAt() != null ? d.getCreatedAt() : "", d.getStatusLaporan() != null ? d.getStatusLaporan() : "Diproses", d.getCatatanAdmin() != null ? d.getCatatanAdmin() : "Sedang diproses admin", d.getFoto(), d.getNamaUser()));
                    }
                }
                checkDone.run();
            }
            @Override
            public void onFailure(retrofit2.Call<LaporanJalanResponse> call, Throwable t) { checkDone.run(); }
        });

        apiService.getSemuaPermintaanLokasi(token).enqueue(new retrofit2.Callback<PermintaanLokasiResponse>() {
            @Override
            public void onResponse(retrofit2.Call<PermintaanLokasiResponse> call, retrofit2.Response<PermintaanLokasiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    for (PermintaanLokasiResponse.Data d : response.body().getData()) {
                        riwayatList.add(new RiwayatModel(d.getId(), "Permintaan Lokasi", d.getKeterangan(), d.getCreatedAt() != null ? d.getCreatedAt() : "", d.getStatusLaporan() != null ? d.getStatusLaporan() : "Diproses", d.getCatatanAdmin() != null ? d.getCatatanAdmin() : "Sedang diproses admin", d.getFoto(), d.getNamaUser()));
                    }
                }
                checkDone.run();
            }
            @Override
            public void onFailure(retrofit2.Call<PermintaanLokasiResponse> call, Throwable t) { checkDone.run(); }
        });

        apiService.getSemuaLaporanKendaraan(token).enqueue(new retrofit2.Callback<LaporanKendaraanResponse>() {
            @Override
            public void onResponse(retrofit2.Call<LaporanKendaraanResponse> call, retrofit2.Response<LaporanKendaraanResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    for (LaporanKendaraanResponse.Data d : response.body().getData()) {
                        riwayatList.add(new RiwayatModel(d.getId(), "Kendaraan", d.getKeterangan(), d.getCreatedAt() != null ? d.getCreatedAt() : "", d.getStatusLaporan() != null ? d.getStatusLaporan() : "Diproses", d.getCatatanAdmin() != null ? d.getCatatanAdmin() : "Sedang diproses admin", d.getFoto(), d.getNamaUser()));
                    }
                }
                checkDone.run();
            }
            @Override
            public void onFailure(retrofit2.Call<LaporanKendaraanResponse> call, Throwable t) { checkDone.run(); }
        });
    }

    private void fetchLokasi() {
        SessionManager sessionManager = new SessionManager(requireContext());
        String token = "Bearer " + sessionManager.getToken();
        
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getLokasi(token).enqueue(new retrofit2.Callback<LokasiResponseModel>() {
            @Override
            public void onResponse(retrofit2.Call<LokasiResponseModel> call, retrofit2.Response<LokasiResponseModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    lokasiList.clear();
                    if (response.body().getData() != null) {
                        lokasiList.addAll(response.body().getData());
                    }
                    
                    adapterLokasi.clear();
                    adapterLokasi.add("Pilih Lokasi...");
                    for (LokasiResponseModel.Data l : lokasiList) {
                        if (l != null && l.getNamaLokasi() != null) {
                            adapterLokasi.add(l.getNamaLokasi());
                        }
                    }
                    adapterLokasi.notifyDataSetChanged();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        android.util.Log.e("FetchLokasi", "Failed: " + response.code() + " " + errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onFailure(retrofit2.Call<LokasiResponseModel> call, Throwable t) {
                android.util.Log.e("FetchLokasi", "Network Error: " + t.getMessage());
            }
        });
    }

    private void setupSpinners() {
        // --- Spinner Lokasi Jalan ---
        lokasiNames.add("Pilih Lokasi...");
        adapterLokasi = new ArrayAdapter<String>(requireContext(), R.layout.item_spinner_selected, lokasiNames) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // Non-aktifkan item pertama (hint)
            }

            @Override
            public android.view.View getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Tampilkan hint dengan warna abu
                    tv.setTextColor(android.graphics.Color.parseColor("#9CA3AF"));
                    tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.ITALIC);
                } else {
                    tv.setTextColor(android.graphics.Color.parseColor("#1F2937"));
                    tv.setTypeface(android.graphics.Typeface.DEFAULT);
                }
                return view;
            }
        };
        adapterLokasi.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerLokasiJalan.setAdapter(adapterLokasi);

        // Fetch lokasi from backend to populate spinner
        fetchLokasi();

        // --- Spinner Jenis Masalah Kendaraan ---
        String[] masalahArray = {
                "Pilih jenis masalah",
                "Kendaraan tidak muncul di daftar",
                "Data kendaraan salah",
                "Plat nomor sudah digunakan orang lain",
                "Lainnya"
        };
        ArrayAdapter<String> adapterMasalah = new ArrayAdapter<String>(requireContext(), R.layout.item_spinner_selected, masalahArray) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // Non-aktifkan item pertama (hint)
            }

            @Override
            public android.view.View getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Tampilkan hint dengan warna abu + italic
                    tv.setTextColor(android.graphics.Color.parseColor("#9CA3AF"));
                    tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.ITALIC);
                } else {
                    tv.setTextColor(android.graphics.Color.parseColor("#1F2937"));
                    tv.setTypeface(android.graphics.Typeface.DEFAULT);
                }
                return view;
            }
        };
        adapterMasalah.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerJenisMasalah.setAdapter(adapterMasalah);
    }


    private void setupListeners() {
        btnBack.setOnClickListener(v -> showMenu());

        btnOpsiKondisiJalan.setOnClickListener(v -> showFormKondisiJalan());
        btnOpsiPermintaanLokasi.setOnClickListener(v -> showFormPermintaanLokasi());
        btnOpsiKendaraanTidakAda.setOnClickListener(v -> showFormKendaraan());
        
        if (btnLihatSemua != null) {
            btnLihatSemua.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), RiwayatLaporanActivity.class);
                startActivity(intent);
            });
        }

        // Button Kirim Jalan
        btnKirimJalan.setOnClickListener(v -> {
            if (spinnerLokasiJalan.getSelectedItemPosition() == 0) {
                android.util.Log.d("AppLog", String.valueOf("Silakan pilih lokasi"));
                return;
            }
            int index = spinnerLokasiJalan.getSelectedItemPosition() - 1;
            String idLokasi = String.valueOf(lokasiList.get(index).getIdLokasi());
            String deskripsi = etDeskripsiJalan.getText().toString();

            if (deskripsi.isEmpty()) {
                etDeskripsiJalan.setError("Deskripsi tidak boleh kosong");
                return;
            }
            if (selectedImageUri == null) {
                showPopup("Gagal", "Foto wajib disertakan untuk laporan kondisi jalan", false);
                return;
            }
            uploadLaporJalan(idLokasi, deskripsi);
        });

        // Button Dapatkan Lokasi (Dummy untuk sekarang)
        btnDapatkanLokasi.setOnClickListener(v -> {
            if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                checkAndFetchLocation();
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        // Button Kirim Permintaan Lokasi
        btnKirimLokasi.setOnClickListener(v -> {
            String lokasi = tvLokasiTerkiniLabel.getText().toString();
            String deskripsi = etDeskripsiPermintaan.getText().toString();
            
            if (lokasi.equals("Lokasi belum diambil")) {
                android.util.Log.d("AppLog", String.valueOf("Silakan dapatkan lokasi terkini dulu"));
                return;
            }
            if (deskripsi.isEmpty()) {
                etDeskripsiPermintaan.setError("Keterangan tidak boleh kosong");
                return;
            }
            if (selectedImageUri == null) {
                showPopup("Gagal", "Foto wajib disertakan untuk permintaan lokasi", false);
                return;
            }

            // Extract lat and lng from string if needed, or hardcode dummy
            String lat = "-3.316694";
            String lng = "114.590111";
            try {
                if (lokasi.contains("Lat") && lokasi.contains("Lng")) {
                    String[] parts = lokasi.split(",");
                    lat = parts[0].replaceAll("[^0-9.-]", "");
                    lng = parts[1].replaceAll("[^0-9.-]", "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String finalDeskripsi = deskripsi;
            if (tvAlamatLokasi != null && tvAlamatLokasi.getVisibility() == View.VISIBLE) {
                String alamat = tvAlamatLokasi.getText().toString();
                if (!alamat.isEmpty() && !alamat.equals("Alamat akan muncul di sini")) {
                    finalDeskripsi = "📍 " + alamat + "\n\n" + deskripsi;
                }
            }

            uploadLaporLokasi(lat, lng, finalDeskripsi);
        });

        // Button Kirim Kendaraan
        btnKirimKendaraan.setOnClickListener(v -> {
            String jenisMasalah = spinnerJenisMasalah.getSelectedItem().toString();
            String deskripsi = etDeskripsiKendaraan.getText().toString();

            if (spinnerJenisMasalah.getSelectedItemPosition() == 0) {
                android.util.Log.d("AppLog", String.valueOf("Silakan pilih jenis masalah"));
                return;
            }
            if (deskripsi.isEmpty()) {
                etDeskripsiKendaraan.setError("Deskripsi tidak boleh kosong");
                return;
            }

            uploadLaporKendaraan(jenisMasalah + ": " + deskripsi);
        });
        
        // Setup tombol upload
        View.OnClickListener uploadListener = v -> {
            String[] options = {"Kamera", "Galeri"};
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Pilih Sumber Gambar")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        launchCamera();
                    } else {
                        getContent.launch("image/*");
                    }
                })
                .show();
        };
        btnUploadFotoJalan.setOnClickListener(uploadListener);
        btnUploadFotoLokasi.setOnClickListener(uploadListener);
        btnUploadFotoKendaraan.setOnClickListener(uploadListener);

        View.OnClickListener hapusListener = v -> {
            selectedImageUri = null;
            updatePreviewUI();
        };
        btnHapusFotoJalan.setOnClickListener(hapusListener);
        btnHapusFotoLokasi.setOnClickListener(hapusListener);
        btnHapusFotoKendaraan.setOnClickListener(hapusListener);

    }

    private void hideAllLayouts() {
        layoutPilihanPengaduan.setVisibility(View.GONE);
        layoutFormKondisiJalan.setVisibility(View.GONE);
        layoutFormPermintaanLokasi.setVisibility(View.GONE);
        layoutFormKendaraan.setVisibility(View.GONE);
    }

    
    private void updatePreviewUI() {
        if (selectedImageUri == null) {
            previewContainerJalan.setVisibility(View.GONE);
            btnUploadFotoJalan.setVisibility(View.VISIBLE);
            
            previewContainerLokasi.setVisibility(View.GONE);
            btnUploadFotoLokasi.setVisibility(View.VISIBLE);
            
            previewContainerKendaraan.setVisibility(View.GONE);
            btnUploadFotoKendaraan.setVisibility(View.VISIBLE);
        } else {
            if (currentState == 1) { // Jalan
                btnUploadFotoJalan.setVisibility(View.GONE);
                previewContainerJalan.setVisibility(View.VISIBLE);
                imgPreviewJalan.setImageURI(selectedImageUri);
            } else if (currentState == 2) { // Lokasi
                btnUploadFotoLokasi.setVisibility(View.GONE);
                previewContainerLokasi.setVisibility(View.VISIBLE);
                imgPreviewLokasi.setImageURI(selectedImageUri);
            } else if (currentState == 3) { // Kendaraan
                btnUploadFotoKendaraan.setVisibility(View.GONE);
                previewContainerKendaraan.setVisibility(View.VISIBLE);
                imgPreviewKendaraan.setImageURI(selectedImageUri);
            }
        }
    }

    private void showMenu() {
        currentState = 0; selectedImageUri = null; updatePreviewUI();
        hideAllLayouts();
        layoutPilihanPengaduan.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.GONE);
        tvHeaderTitle.setText("Pusat Pelaporan");
        if (tvHeaderDesc != null) tvHeaderDesc.setVisibility(View.VISIBLE);
        
        // Refresh riwayat saat kembali ke menu utama
        fetchRiwayat();
    }

    private void showFormKondisiJalan() {
        currentState = 1; selectedImageUri = null; updatePreviewUI();
        hideAllLayouts();
        layoutFormKondisiJalan.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        tvHeaderTitle.setText("Kondisi Jalan");
        if (tvHeaderDesc != null) tvHeaderDesc.setVisibility(View.GONE);
    }

    private void showFormPermintaanLokasi() {
        currentState = 2; selectedImageUri = null; updatePreviewUI();
        hideAllLayouts();
        layoutFormPermintaanLokasi.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        tvHeaderTitle.setText("Permintaan Lokasi");
        if (tvHeaderDesc != null) tvHeaderDesc.setVisibility(View.GONE);
    }

    private void showFormKendaraan() {
        currentState = 3; selectedImageUri = null; updatePreviewUI();
        hideAllLayouts();
        layoutFormKendaraan.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        tvHeaderTitle.setText("Lapor Kendaraan");
        if (tvHeaderDesc != null) tvHeaderDesc.setVisibility(View.GONE);
    }

    private void launchCamera() {
        try {
            java.io.File photoFile = new java.io.File(requireContext().getCacheDir(), "IMG_" + System.currentTimeMillis() + ".jpg");
            tempImageUri = androidx.core.content.FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", photoFile);
            takePictureLauncher.launch(tempImageUri);
        } catch (Exception e) {
            android.util.Log.d("AppLog", String.valueOf("Gagal membuka kamera: " + e.getMessage()));
        }
    }

    private okhttp3.MultipartBody.Part getFotoPart() {
        if (selectedImageUri == null) return null;
        try {
            java.io.InputStream is = requireContext().getContentResolver().openInputStream(selectedImageUri);
            java.io.File tempFile = new java.io.File(requireContext().getCacheDir(), "upload.jpg");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) fos.write(buf, 0, len);
            fos.close();
            is.close();
            
            okhttp3.RequestBody reqFile = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/*"), tempFile);
            return okhttp3.MultipartBody.Part.createFormData("foto", tempFile.getName(), reqFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void uploadLaporJalan(String idLokasi, String keterangan) {
        android.app.ProgressDialog pd = new android.app.ProgressDialog(requireContext());
        pd.setMessage("Mengirim laporan...");
        pd.show();
        
        SessionManager sessionManager = new SessionManager(requireContext());
        String token = "Bearer " + sessionManager.getToken();
        
        okhttp3.RequestBody idBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), idLokasi);
        okhttp3.RequestBody ketBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), keterangan);
        okhttp3.MultipartBody.Part fotoPart = getFotoPart();
        
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.laporJalan(token, idBody, ketBody, fotoPart).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                pd.dismiss();
                if (response.isSuccessful()) {
                    showPopup("Berhasil", "Laporan berhasil dikirim", true);
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        showPopup("Gagal", "Gagal mengirim laporan:\nCode: " + response.code() + "\n" + err, false);
                    } catch(Exception e) {
                        showPopup("Gagal", "Gagal mengirim laporan: " + response.code(), false);
                    }
                }
            }
            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                pd.dismiss();
                showPopup("Error Jaringan", t.getMessage(), false);
            }
        });
    }

    private void checkAndFetchLocation() {
        android.location.LocationManager lm = (android.location.LocationManager) requireContext().getSystemService(android.content.Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            new android.app.AlertDialog.Builder(requireContext())
                .setMessage("Lokasi Anda belum aktif. Mohon aktifkan lokasi untuk melanjutkan.")
                .setPositiveButton("Pengaturan", (paramDialogInterface, paramInt) -> {
                    startActivity(new android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                })
                .setNegativeButton("Batal",null)
                .show();
            return;
        }

        if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            android.util.Log.d("AppLog", String.valueOf("Mengambil lokasi..."));
            com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireActivity());
            
            fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, (com.google.android.gms.tasks.CancellationToken) null)
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        String lat = String.valueOf(location.getLatitude());
                        String lng = String.valueOf(location.getLongitude());
                        if (tvLokasiTerkiniLabel != null) {
                            tvLokasiTerkiniLabel.setText("Lokasi Tersimpan: Lat " + lat + ", Lng " + lng);
                        }
                        android.util.Log.d("AppLog", String.valueOf("Lokasi didapatkan"));
                        new Thread(() -> fetchAddress(lat, lng)).start();
                    } else {
                        // Jika gagal, pakai dummy sebagai fallback atau biarkan user mencoba lagi.
                        android.util.Log.d("AppLog", String.valueOf("Gagal mendapatkan lokasi. Pastikan GPS aktif dan coba lagi."));
                    }
                });
        }
    }

    private void fetchAddress(String latStr, String lngStr) {
        if (latStr.isEmpty() || lngStr.isEmpty() || tvAlamatLokasi == null) return;
        try {
            double lat = Double.parseDouble(latStr);
            double lng = Double.parseDouble(lngStr);
            android.location.Geocoder geocoder = new android.location.Geocoder(requireContext(), java.util.Locale.getDefault());
            java.util.List<android.location.Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                android.location.Address obj = addresses.get(0);
                String add = obj.getAddressLine(0);
                requireActivity().runOnUiThread(() -> {
                    tvAlamatLokasi.setText(add);
                    tvAlamatLokasi.setVisibility(View.VISIBLE);
                });
            } else {
                requireActivity().runOnUiThread(() -> tvAlamatLokasi.setVisibility(View.GONE));
            }
        } catch (Exception e) {
            e.printStackTrace();
            requireActivity().runOnUiThread(() -> tvAlamatLokasi.setVisibility(View.GONE));
        }
    }

    private void uploadLaporLokasi(String lat, String lng, String keterangan) {
        android.app.ProgressDialog pd = new android.app.ProgressDialog(requireContext());
        pd.setMessage("Mengirim laporan...");
        pd.show();
        
        SessionManager sessionManager = new SessionManager(requireContext());
        String token = "Bearer " + sessionManager.getToken();
        
        okhttp3.RequestBody latBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), lat);
        okhttp3.RequestBody lngBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), lng);
        okhttp3.RequestBody ketBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), keterangan);
        okhttp3.MultipartBody.Part fotoPart = getFotoPart();
        
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.laporLokasi(token, latBody, lngBody, ketBody, fotoPart).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                pd.dismiss();
                if (response.isSuccessful()) {
                    showPopup("Berhasil", "Permintaan lokasi berhasil dikirim", true);
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        showPopup("Gagal", "Gagal mengirim laporan:\nCode: " + response.code() + "\n" + err, false);
                    } catch(Exception e) {
                        showPopup("Gagal", "Gagal mengirim laporan: " + response.code(), false);
                    }
                }
            }
            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                pd.dismiss();
                showPopup("Error Jaringan", t.getMessage(), false);
            }
        });
    }

    private void uploadLaporKendaraan(String keterangan) {
        android.app.ProgressDialog pd = new android.app.ProgressDialog(requireContext());
        pd.setMessage("Mengirim laporan...");
        pd.show();
        
        SessionManager sessionManager = new SessionManager(requireContext());
        String token = "Bearer " + sessionManager.getToken();
        
        okhttp3.RequestBody ketBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), keterangan);
        okhttp3.MultipartBody.Part fotoPart = getFotoPart();
        
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.laporKendaraan(token, ketBody, fotoPart).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                pd.dismiss();
                if (response.isSuccessful()) {
                    showPopup("Berhasil", "Laporan kendaraan berhasil dikirim", true);
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        showPopup("Gagal", "Gagal mengirim laporan:\nCode: " + response.code() + "\n" + err, false);
                    } catch(Exception e) {
                        showPopup("Gagal", "Gagal mengirim laporan: " + response.code(), false);
                    }
                }
            }
            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                pd.dismiss();
                showPopup("Error Jaringan", t.getMessage(), false);
            }
        });
    }
    
    private void resetForms() {
        selectedImageUri = null;
        updatePreviewUI();

        spinnerLokasiJalan.setSelection(0);
        etDeskripsiJalan.setText("");
        
        if (tvLokasiTerkiniLabel != null) tvLokasiTerkiniLabel.setText("Lokasi belum diambil");
        if (tvAlamatLokasi != null) tvAlamatLokasi.setVisibility(View.GONE);
        etDeskripsiPermintaan.setText("");
        
        spinnerJenisMasalah.setSelection(0);
        etDeskripsiKendaraan.setText("");
        
        selectedImageUri = null;
    }

    private void showPopup(String title, String message, boolean isSuccess) {
        android.app.Dialog dialog = new android.app.Dialog(requireContext());
        dialog.setContentView(isSuccess ? R.layout.popup_berhasil_tambah : R.layout.popup_gagal);
        
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        
        TextView tvPesan = dialog.findViewById(isSuccess ? R.id.tvPesanBerhasil : R.id.tvPesanGagal);
        if (tvPesan != null) tvPesan.setText(message);
        
        View btnLanjut = dialog.findViewById(isSuccess ? R.id.lanjutanBerhasil : R.id.lanjutanGagal);
        if (btnLanjut != null) {
            btnLanjut.setOnClickListener(v -> {
                dialog.dismiss();
                if (isSuccess) {
                    resetForms();
                    showMenu();
                }
            });
        }
        
        dialog.show();
    }
}
