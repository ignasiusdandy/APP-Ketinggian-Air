package com.example.skripsi;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import org.w3c.dom.Text;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailAlatDialog {
    // ini untuk popup detail alat dashboard admin

    public interface OnStatusUpdated {
        void onUpdated();
    }
    public static void show(Context context,
                            FragmentManager fragmentManager,
                            String title,
                            boolean isAktif,
                            String koordinat,
                            String tanggal,
                            String idAlat,
                            String token,
                            ApiService apiService,
                            OnStatusUpdated listener) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_detail_alat);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setDimAmount(0.5f);
            dialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);
        }

        // ===== INIT VIEW =====
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvStatus = dialog.findViewById(R.id.tvStatus);
        TextView tvKoordinat = dialog.findViewById(R.id.tvKoordinat);
        TextView tvWaktu = dialog.findViewById(R.id.tvWaktu);
        TextView tvBtnAktif = dialog.findViewById(R.id.tvBtnAktif);
        TextView tvBtnNonAktif = dialog.findViewById(R.id.tvBtnNonAktif);


        LinearLayout btnAktif = dialog.findViewById(R.id.btnAktif);
        LinearLayout btnNonAktif = dialog.findViewById(R.id.btnNonAktif);
        LinearLayout iconContainer = dialog.findViewById(R.id.iconContainer);
        LinearLayout cardInfo = dialog.findViewById(R.id.cardInfo);
        LinearLayout btnKalibrasi = dialog.findViewById(R.id.btnKalibrasi);

        ImageView imgIcon = dialog.findViewById(R.id.imgIcon);

        TextView btnClose = dialog.findViewById(R.id.btnClose);

        // ===== SET DATA =====
        tvTitle.setText(title);
        tvKoordinat.setText("Koordinat: " + koordinat);
        tvWaktu.setText("Terakhir Aktif: " + tanggal);

        updateUI(
                context,
                isAktif,
                tvStatus,
                iconContainer,
                cardInfo,
                imgIcon,
                btnAktif,
                btnNonAktif,
                tvBtnAktif,
                tvBtnNonAktif,
                btnKalibrasi
        );

        btnAktif.setOnClickListener(v -> {

            btnAktif.setEnabled(false);

            UpdateStatusAlatRequestModel request = new UpdateStatusAlatRequestModel(idAlat, "ST001");

            apiService.updateStatusAlat(token, request)
                    .enqueue(new Callback<ResponseStatusAlatModel>() {

                        @Override
                        public void onResponse(Call<ResponseStatusAlatModel> call, Response<ResponseStatusAlatModel> response) {

                            btnAktif.setEnabled(true);

                            if (response.isSuccessful() && response.body() != null && response.body().status) {

                                updateUI(
                                        context,
                                        true,
                                        tvStatus,
                                        iconContainer,
                                        cardInfo,
                                        imgIcon,
                                        btnAktif,
                                        btnNonAktif,
                                        tvBtnAktif,
                                        tvBtnNonAktif,
                                        btnKalibrasi
                                );
                                if (listener != null) {
                                    listener.onUpdated();
                                }

                            } else {
                                Toast.makeText(context, "Gagal update status", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseStatusAlatModel> call, Throwable t) {

                            btnAktif.setEnabled(true);

                            Toast.makeText(context, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnNonAktif.setOnClickListener(v -> {

            btnNonAktif.setEnabled(false);

            UpdateStatusAlatRequestModel request = new UpdateStatusAlatRequestModel(idAlat, "ST002");

            apiService.updateStatusAlat(token, request)
                    .enqueue(new Callback<ResponseStatusAlatModel>() {

                        @Override
                        public void onResponse(Call<ResponseStatusAlatModel> call, Response<ResponseStatusAlatModel> response) {

                            btnNonAktif.setEnabled(true);

                            if (response.isSuccessful() && response.body() != null && response.body().status) {

                                updateUI(
                                        context,
                                        false,
                                        tvStatus,
                                        iconContainer,
                                        cardInfo,
                                        imgIcon,
                                        btnAktif,
                                        btnNonAktif,
                                        tvBtnAktif,
                                        tvBtnNonAktif,
                                        btnKalibrasi
                                );

                                if (listener != null) {
                                    listener.onUpdated();
                                }

                            } else {
                                Toast.makeText(context, "Gagal update status", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseStatusAlatModel> call, Throwable t) {

                            btnNonAktif.setEnabled(true);

                            Toast.makeText(context, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnKalibrasi.setOnClickListener(v -> {
            dialog.dismiss();

            Dialog dialogKalibrasi = new Dialog(context);
            dialogKalibrasi.setContentView(R.layout.popup_kalibrasi_alat);

            if (dialogKalibrasi.getWindow() != null) {
                dialogKalibrasi.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                dialogKalibrasi.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialogKalibrasi.getWindow().setDimAmount(0.5f);
            }

            TextView tvKetinggian = dialogKalibrasi.findViewById(R.id.tvKetinggian);
            TextView tvKalibrasi = dialogKalibrasi.findViewById(R.id.tvKalibrasi);
            TextView tvWaktuKalibrasi = dialogKalibrasi.findViewById(R.id.tvWaktuKalibrasi);
            EditText etKalibrasi = dialogKalibrasi.findViewById(R.id.etKalibrasi);

//            ImageView btnCloseKalibrasi = dialogKalibrasi.findViewById(R.id.btnCloseKalibrasi);
            TextView btnBatal = dialogKalibrasi.findViewById(R.id.btnBatal);
            TextView btnSimpan = dialogKalibrasi.findViewById(R.id.btnSimpan);
            TextView tvEstimasi = dialogKalibrasi.findViewById(R.id.estimasiKalibrasi);

            // loading state
            tvKetinggian.setText("Loading...");
            tvKalibrasi.setText("-");

            apiService.getKalibrasiDetail(token, idAlat)
                    .enqueue(new Callback<KalibrasiResponseModel>() {

                        @Override
                        public void onResponse(Call<KalibrasiResponseModel> call,
                                               Response<KalibrasiResponseModel> response) {
                            Log.d("KALIBRASI", "Response: " + response.body());

                            if (response.isSuccessful() && response.body() != null && response.body().data != null) {

                                KalibrasiResponseModel.Data data = response.body().data;

                                tvKetinggian.setText(
                                        data.ketinggian != null ? data.ketinggian + " cm" : "-"
                                );

                                tvKalibrasi.setText(data.kalibrasi + " cm");

                                tvWaktuKalibrasi.setText(
                                        data.waktu_kalibrasi != null
                                                ? "Dikalibrasi pada " + data.waktu_kalibrasi
                                                : "-"
                                );

                                etKalibrasi.setText(String.valueOf(0));
                                double input = 0;
                                String inputStr = etKalibrasi.getText().toString();

                                if (!inputStr.isEmpty()) {
                                    try {
                                        input = Double.parseDouble(inputStr);
                                    } catch (Exception e) {
                                        input = 0;
                                    }
                                }

                                double ketinggian = data.ketinggian != null ? data.ketinggian : 0;
                                double kalibrasi = data.kalibrasi;

                                etKalibrasi.setText("0");

                                etKalibrasi.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void afterTextChanged(Editable s) {

                                        double input = 0;

                                        try {
                                            if (!s.toString().isEmpty()) {
                                                input = Double.parseDouble(s.toString());
                                            }
                                        } catch (Exception e) {
                                            input = 0;
                                        }

                                        double estimasi = ketinggian + input;

                                        String hasil = String.format("%.1f", estimasi);
                                        tvEstimasi.setText("Estimasi Ketinggian: " + hasil + " cm");
                                        tvEstimasi.setTranslationY(10f);
                                        tvEstimasi.setAlpha(0f);

                                        tvEstimasi.animate()
                                                .translationY(0f)
                                                .alpha(1f)
                                                .setDuration(200)
                                                .start();
                                    }

                                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<KalibrasiResponseModel> call, Throwable t) {
                            Toast.makeText(context, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                        }
                    });

//            btnCloseKalibrasi.setOnClickListener(v1 -> dialogKalibrasi.dismiss());
            btnBatal.setOnClickListener(v1 -> dialogKalibrasi.dismiss());

            btnSimpan.setOnClickListener(v1 -> {

                String inputStr = etKalibrasi.getText().toString();

                if (inputStr.isEmpty()) {
                    GagalDialog dialog2 = new GagalDialog();
                    dialog2.show(fragmentManager, "GagalDialog");
                    return;
                }

                double input;

                try {
                    input = Double.parseDouble(inputStr);
                } catch (Exception e) {
                    Toast.makeText(context, "Input tidak valid", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnSimpan.setEnabled(false); //

                UpdateKalibrasiRequest request = new UpdateKalibrasiRequest(idAlat, input);

                apiService.updateKalibrasi(token, request)
                        .enqueue(new Callback<ResponseUpdateKalibrasiModel>() {

                            @Override
                            public void onResponse(Call<ResponseUpdateKalibrasiModel> call, Response<ResponseUpdateKalibrasiModel> response) {

                                btnSimpan.setEnabled(true);

                                if (response.isSuccessful() && response.body() != null) {

                                    BerhasilDialog dialog3 = new BerhasilDialog();
                                    dialog3.show(fragmentManager, "BerhasilDialog");
                                    dialogKalibrasi.dismiss();

                                    if (listener != null) {
                                        listener.onUpdated();
                                    }

                                } else {
                                    Toast.makeText(context, "Gagal simpan kalibrasi", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseUpdateKalibrasiModel> call, Throwable t) {

                                btnSimpan.setEnabled(true);

                                GagalDialog dialog2 = new GagalDialog();
                                dialog2.show(dialog2.getParentFragmentManager(), "GagalDialog");
                            }
                        });
            });

            dialogKalibrasi.show();
        });

        // CLOSE
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

    }




    private static void updateUI(
            Context context,
            boolean isAktif,
            TextView tvStatus,
            LinearLayout iconContainer,
            LinearLayout cardInfo,
            ImageView imgIcon,
            LinearLayout btnAktif,
            LinearLayout btnNonAktif,
            TextView tvBtnAktif,
            TextView tvBtnNonAktif,
            LinearLayout btnKalibrasi
    ) {

        tvStatus.animate().alpha(0f).setDuration(100).withEndAction(() -> {
            tvStatus.setAlpha(1f);
        });

        // animasi icon
        imgIcon.setScaleX(0.8f);
        imgIcon.setScaleY(0.8f);
        imgIcon.animate().scaleX(1f).scaleY(1f).setDuration(200).start();

        // animasi card
        cardInfo.setAlpha(0.7f);
        cardInfo.animate().alpha(1f).setDuration(200).start();


        if (isAktif) {
            // HIJAU
            btnKalibrasi.setEnabled(true);
            btnKalibrasi.setAlpha(1f);
            tvStatus.setText("Aktif");
            tvStatus.setTextColor(context.getColor(R.color.hijautulisanaman));
            tvStatus.setBackground(context.getDrawable(R.drawable.bgtebal_icon_hijau));

            iconContainer.setBackground(context.getDrawable(R.drawable.bgtebal_icon_hijau));
            cardInfo.setBackground(context.getDrawable(R.drawable.bg_icon_hijau));
            imgIcon.setImageResource(R.drawable.cpu_icon);

            btnAktif.setBackground(context.getDrawable(R.drawable.bg_btn_hijau));
            tvBtnAktif.setTextColor(context.getColor(R.color.white));

            btnNonAktif.setBackground(context.getDrawable(R.drawable.bg_input));
            tvBtnNonAktif.setTextColor(context.getColor(R.color.tulisanabu));

        } else {
            // MERAH
            btnKalibrasi.setEnabled(false);
            btnKalibrasi.setAlpha(0.4f);
            tvStatus.setText("Non Aktif");
            tvStatus.setTextColor(context.getColor(R.color.peringatan));
            tvStatus.setBackground(context.getDrawable(R.drawable.bg_icon_merah));

            iconContainer.setBackground(context.getDrawable(R.drawable.bgtebal_icon_merah));
            cardInfo.setBackground(context.getDrawable(R.drawable.bg_icon_merah));
            imgIcon.setImageResource(R.drawable.cpu_merah);

            btnNonAktif.setBackground(context.getDrawable(R.drawable.bg_btn_merah));
            tvBtnNonAktif.setTextColor(context.getColor(R.color.white));

            btnAktif.setBackground(context.getDrawable(R.drawable.bg_input));
            tvBtnAktif.setTextColor(context.getColor(R.color.tulisanabu));
        }
    }
}
