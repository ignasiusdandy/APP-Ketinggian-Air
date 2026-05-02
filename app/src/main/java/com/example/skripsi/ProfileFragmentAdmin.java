package com.example.skripsi;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.HashMap;

public class ProfileFragmentAdmin extends Fragment {

    private TextView tvName, tvEmail;
    private LinearLayout btnChangePassword;

    private LinearLayout layoutChangePassword;
    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private LinearLayout btnCancelPassword, btnSavePassword, btnLogout;

    // 🔥 ERROR TEXT
    private TextView tvErrorOld, tvErrorNew, tvErrorConfirm;
    ApiService apiService;
    SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile_admin, container, false);
        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(requireContext());
        String token = sessionManager.getToken();

        // INIT VIEW
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnLogout = view.findViewById(R.id.btnLogout);

        layoutChangePassword = view.findViewById(R.id.layoutChangePassword);
        etOldPassword = view.findViewById(R.id.etOldPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);

        btnCancelPassword = view.findViewById(R.id.btnCancelPassword);
        btnSavePassword = view.findViewById(R.id.btnSavePassword);

        // 🔥 ERROR INIT
        tvErrorOld = view.findViewById(R.id.tvErrorKataSandi);
        tvErrorNew = view.findViewById(R.id.tvErrorKataSandiBaru);
        tvErrorConfirm = view.findViewById(R.id.tvErrorKonfirmasiSandi);

        // 🔥 HIDE ERROR SAAT NGETIK (INI YANG PENTING)
        hideErrorOnType(etOldPassword, tvErrorOld);
        hideErrorOnType(etNewPassword, tvErrorNew);
        hideErrorOnType(etConfirmPassword, tvErrorConfirm);

        // DATA
        HashMap<String, String> user = sessionManager.getUserDetails();

        String nama = user.get(SessionManager.KEY_NAMA);
        String email = user.get(SessionManager.KEY_EMAIL);

        tvName.setText(nama != null ? nama : "-");
        tvEmail.setText(email != null ? email : "-");

        TextView tvAvatar = view.findViewById(R.id.tvAvatar);

        if (nama != null && !nama.trim().isEmpty()) {
            String initial = nama.trim().substring(0, 1).toUpperCase();
            tvAvatar.setText(initial);
        } else {
            tvAvatar.setText("?");
        }

        // TOGGLE FORM
        btnChangePassword.setOnClickListener(v -> {
            if (layoutChangePassword.getVisibility() == View.GONE) {
                layoutChangePassword.setVisibility(View.VISIBLE);
                layoutChangePassword.setAlpha(0f);
                layoutChangePassword.animate().alpha(1f).setDuration(200);
            } else {
                layoutChangePassword.setVisibility(View.GONE);
            }
        });

        btnCancelPassword.setOnClickListener(v -> {
            layoutChangePassword.setVisibility(View.GONE);
        });

        // 🔥 VALIDASI (VERSI UPGRADE)
        btnSavePassword.setOnClickListener(v -> {

            tvErrorOld.setVisibility(View.GONE);
            tvErrorNew.setVisibility(View.GONE);
            tvErrorConfirm.setVisibility(View.GONE);

            String oldPass = etOldPassword.getText().toString();
            String newPass = etNewPassword.getText().toString();
            String confirmPass = etConfirmPassword.getText().toString();

            boolean isValid = true;

            if (oldPass.isEmpty()) {
                tvErrorOld.setText("Masukkan kata sandi lama");
                tvErrorOld.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (newPass.isEmpty()) {
                tvErrorNew.setText("Masukkan kata sandi baru");
                tvErrorNew.setVisibility(View.VISIBLE);
                isValid = false;
            } else if (newPass.length() < 8) {
                tvErrorNew.setText("Minimal 8 karakter");
                tvErrorNew.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (confirmPass.isEmpty()) {
                tvErrorConfirm.setText("Masukkan konfirmasi password");
                tvErrorConfirm.setVisibility(View.VISIBLE);
                isValid = false;
            } else if (!newPass.equals(confirmPass)) {
                tvErrorConfirm.setText("Password tidak sama");
                tvErrorConfirm.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (!isValid) return;

            // 🔥 DISABLE BUTTON (biar ga double klik)
            btnSavePassword.setEnabled(false);

            GantiKataSandiModel request = new GantiKataSandiModel(oldPass, newPass);

            apiService.changePassword("Bearer " + token, request)
                    .enqueue(new retrofit2.Callback<GantiKataSandiModel>() {

                        @Override
                        public void onResponse(retrofit2.Call<GantiKataSandiModel> call,
                                               retrofit2.Response<GantiKataSandiModel> response) {

                            btnSavePassword.setEnabled(true);

                            if (response.isSuccessful()) {

                                Dialog dialog = new Dialog(requireContext());
                                dialog.setContentView(R.layout.popup_berhasil);
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
                                    etOldPassword.setText("");
                                    etNewPassword.setText("");
                                    etConfirmPassword.setText("");
                                });
                                layoutChangePassword.setVisibility(View.GONE);
                                dialog.show();

                            } else {
                                tvErrorOld.setText("Password lama salah");
                                tvErrorOld.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<GantiKataSandiModel> call, Throwable t) {
                            btnSavePassword.setEnabled(true);
                            Dialog dialog = new Dialog(requireContext());
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
                            lanjutanGagal.setOnClickListener(v2 -> {
                                dialog.dismiss();
                                etOldPassword.setText("");
                                etNewPassword.setText("");
                                etConfirmPassword.setText("");
                            });
                            dialog.show();
                        }
                    });
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }

    // 🔥 METHOD DARI CONTOH KAMU (INI KUNCI UX BAGUS)
    private void hideErrorOnType(EditText editText, TextView errorView) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                errorView.setVisibility(View.GONE);
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }
}