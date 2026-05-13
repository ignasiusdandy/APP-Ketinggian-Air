package com.example.skripsi;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    // MENU
    LinearLayout btnPassword, btnKendaraan, layoutPassword,
            btnLogout, btnSavePassword;

    ImageView iconExpand;

    boolean isPasswordOpen = false;

    // PASSWORD
    EditText edtPasswordOld,
            edtPasswordNew,
            edtPasswordConfirm;

    TextView wrongOld,
            wrongNew,
            wrongNewConf;

    ApiService apiService;
    SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile,
                container,
                false);

        // API
        apiService = ApiClient.getClient().create(ApiService.class);

        sessionManager = new SessionManager(requireContext());

        TextView tvNama = view.findViewById(R.id.tvNama);
        TextView tvEmail = view.findViewById(R.id.tvEmail);

        HashMap<String, String> user = sessionManager.getUserDetails();

        String nama = user.get(SessionManager.KEY_NAMA);
        String email = user.get(SessionManager.KEY_EMAIL);

        tvNama.setText(nama);
        tvEmail.setText(email);

        // MENU
        btnPassword = view.findViewById(R.id.ubah_kata_sandi);

        btnKendaraan = view.findViewById(R.id.kendaraanUser);

        btnLogout = view.findViewById(R.id.btnLogout);

        // EXPAND PASSWORD
        layoutPassword = view.findViewById(R.id.layoutPassword);

        iconExpand = view.findViewById(R.id.iconExpand);

        // EDITTEXT
        edtPasswordOld = view.findViewById(R.id.edtPasswordOld);

        edtPasswordNew = view.findViewById(R.id.edtPasswordNew);

        edtPasswordConfirm = view.findViewById(R.id.edtPasswordConfirm);

        // ERROR TEXT
        wrongOld = view.findViewById(R.id.wrongPass);

        wrongNew = view.findViewById(R.id.wrongNewPass);

        wrongNewConf = view.findViewById(R.id.wrongNewPassConf);

        // BUTTON
        btnSavePassword = view.findViewById(R.id.btnSavePassword);

        // DEFAULT
        layoutPassword.setVisibility(View.GONE);

        // HIDE ERROR WHEN TYPE
        hideErrorOnType(edtPasswordOld, wrongOld);

        hideErrorOnType(edtPasswordNew, wrongNew);

        hideErrorOnType(edtPasswordConfirm, wrongNewConf);

        // DAFTAR KENDARAAN
        btnKendaraan.setOnClickListener(v -> {

            Intent intent = new Intent(getActivity(),
                    DaftarKendaraanUserActivity.class);

            startActivity(intent);

        });

        // EXPAND PASSWORD
        btnPassword.setOnClickListener(v -> {

            if (isPasswordOpen) {

                layoutPassword.setVisibility(View.GONE);

                iconExpand.animate()
                        .rotation(0)
                        .setDuration(200)
                        .start();

                isPasswordOpen = false;

            } else {

                layoutPassword.setVisibility(View.VISIBLE);

                iconExpand.animate()
                        .rotation(90)
                        .setDuration(200)
                        .start();

                isPasswordOpen = true;

            }

        });

        // SAVE PASSWORD
        btnSavePassword.setOnClickListener(v -> {

            wrongOld.setVisibility(View.GONE);

            wrongNew.setVisibility(View.GONE);

            wrongNewConf.setVisibility(View.GONE);

            String etSandiLama =
                    edtPasswordOld.getText().toString();

            String etSandiBaru =
                    edtPasswordNew.getText().toString();

            String etSandiBaruConf =
                    edtPasswordConfirm.getText().toString();

            boolean isValid = true;

            // VALIDASI OLD PASSWORD
            if (etSandiLama.isEmpty()) {

                wrongOld.setVisibility(View.VISIBLE);

                isValid = false;

            }

            // VALIDASI NEW PASSWORD
            if (etSandiBaru.isEmpty()) {

                wrongNew.setVisibility(View.VISIBLE);

                isValid = false;

            } else if (etSandiBaru.length() < 8) {

                wrongNew.setText("Password Baru minimal 8 karakter");

                wrongNew.setVisibility(View.VISIBLE);

                isValid = false;

            } else if (!etSandiBaru.equals(etSandiBaruConf)
                    && !etSandiBaruConf.isEmpty()) {

                wrongNew.setText("Password tidak sama");

                wrongNew.setVisibility(View.VISIBLE);

                isValid = false;

            }

            // VALIDASI KONFIRMASI
            if (etSandiBaruConf.isEmpty()) {

                wrongNewConf.setVisibility(View.VISIBLE);

                isValid = false;

            } else if (etSandiBaruConf.length() < 8) {

                wrongNewConf.setText(
                        "Konfirmasi password minimal 8 karakter");

                wrongNewConf.setVisibility(View.VISIBLE);

                isValid = false;

            } else if (!etSandiBaru.equals(etSandiBaruConf)
                    && !etSandiBaruConf.isEmpty()) {

                wrongNewConf.setText("Password tidak sama");

                wrongNewConf.setVisibility(View.VISIBLE);

                isValid = false;

            }

            // API
            if (isValid) {

                String token = sessionManager.getToken();

                GantiKataSandiModel request =
                        new GantiKataSandiModel(
                                etSandiLama,
                                etSandiBaru
                        );

                apiService.changePassword(
                                "Bearer " + token,
                                request
                        )
                        .enqueue(new Callback<GantiKataSandiModel>() {

                            @Override
                            public void onResponse(
                                    Call<GantiKataSandiModel> call,
                                    Response<GantiKataSandiModel> response) {

                                if (response.isSuccessful()) {

                                    Dialog dialog =
                                            new Dialog(requireContext());

                                    dialog.setContentView(
                                            R.layout.popup_berhasil);

                                    dialog.getWindow().setLayout(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                    );

                                    dialog.getWindow()
                                            .setDimAmount(0.8f);

                                    dialog.getWindow()
                                            .setBackgroundDrawableResource(
                                                    android.R.color.transparent
                                            );

                                    LinearLayout lanjut =
                                            dialog.findViewById(
                                                    R.id.lanjutanBerhasil
                                            );

                                    lanjut.setOnClickListener(v -> {

                                        dialog.dismiss();

                                        edtPasswordOld.setText("");

                                        edtPasswordNew.setText("");

                                        edtPasswordConfirm.setText("");

                                        layoutPassword.setVisibility(
                                                View.GONE
                                        );

                                        iconExpand.animate()
                                                .rotation(0)
                                                .setDuration(200)
                                                .start();

                                        isPasswordOpen = false;

                                    });

                                    dialog.show();

                                } else {

                                    Dialog dialog =
                                            new Dialog(requireContext());

                                    dialog.setContentView(
                                            R.layout.popup_gagal);

                                    dialog.getWindow().setLayout(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                    );

                                    dialog.getWindow()
                                            .setDimAmount(0.8f);

                                    dialog.getWindow()
                                            .setBackgroundDrawableResource(
                                                    android.R.color.transparent
                                            );

                                    LinearLayout lanjut =
                                            dialog.findViewById(
                                                    R.id.lanjutanGagal
                                            );

                                    lanjut.setOnClickListener(v -> {

                                        dialog.dismiss();

                                    });

                                    dialog.show();

                                }

                            }

                            @Override
                            public void onFailure(
                                    Call<GantiKataSandiModel> call,
                                    Throwable t) {

                                Toast.makeText(
                                        getContext(),
                                        "Error: " + t.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();

                            }

                        });

            }

        });

        // LOGOUT
        btnLogout.setOnClickListener(v -> {

        });

        return view;
    }

    private void hideErrorOnType(EditText editText,
                                 TextView errorView) {

        editText.addTextChangedListener(
                new android.text.TextWatcher() {

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

                        errorView.setVisibility(View.GONE);

                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s) {
                    }

                });

    }

}