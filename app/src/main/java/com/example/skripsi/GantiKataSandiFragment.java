package com.example.skripsi;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.time.temporal.Temporal;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GantiKataSandiFragment extends Fragment {
    public GantiKataSandiFragment() {
        super(R.layout.fragment_ganti_kata_sandi);
    }
    EditText kataSandiLama, kataSandiBaru, kataSandiBaruConf;
    TextView wrongOld, wrongNew, wrongNewConf;
    MaterialButton btn_ganti;
    ApiService apiService;
    SessionManager sessionManager;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(requireContext());
        HashMap<String, String> user = sessionManager.getUserDetails();
        String token = sessionManager.getToken();
        kataSandiLama = view.findViewById(R.id.kata_sandi);
        kataSandiBaru = view.findViewById(R.id.kata_sandi_baru);
        kataSandiBaruConf = view.findViewById(R.id.password_konfirmasi);
        wrongOld = view.findViewById(R.id.wrongPass);
        wrongNew = view.findViewById(R.id.wrongNewPass);
        wrongNewConf = view.findViewById(R.id.wrongNewPassConf);
        btn_ganti = view.findViewById(R.id.btn_update_pengaturan);
        hideErrorOnType(kataSandiLama, wrongOld);
        hideErrorOnType(kataSandiBaru, wrongNew);
        hideErrorOnType(kataSandiBaruConf, wrongNewConf);


        btn_ganti.setOnClickListener(v -> {
            wrongOld.setVisibility(View.GONE);
            wrongNew.setVisibility(View.GONE);
            wrongNewConf.setVisibility(View.GONE);
            String etSandiLama = kataSandiLama.getText().toString();
            String etSandiBaru = kataSandiBaru.getText().toString();
            String etSandiBaruConf = kataSandiBaruConf.getText().toString();
            boolean isValid = true;
            if(etSandiLama.isEmpty()){
                wrongOld.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if(etSandiBaru.isEmpty()){
                wrongNew.setVisibility(View.VISIBLE);
                isValid = false;
            } else if (etSandiBaru.length() < 8) {
                wrongNew.setText("Password Baru minimal 8 karakter");
                wrongNew.setVisibility(View.VISIBLE);
                isValid = false;
            } else if (!etSandiBaru.equals(etSandiBaruConf) && !etSandiBaruConf.isEmpty()) {
                wrongNew.setText("Password tidak sama");
                wrongNew.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if(etSandiBaruConf.isEmpty()){
                wrongNewConf.setVisibility(View.VISIBLE);
                isValid = false;
            } else if (etSandiBaruConf.length() < 8) {
                wrongNewConf.setText("Konfirmasi password minimal 8 karakter");
                wrongNewConf.setVisibility(View.VISIBLE);
                isValid = false;
            } else if (!etSandiBaru.equals(etSandiBaruConf) && !etSandiBaruConf.isEmpty()) {
                wrongNewConf.setText("Password tidak sama");
                wrongNewConf.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if(isValid){
                GantiKataSandiModel request = new GantiKataSandiModel(
                        etSandiLama,
                        etSandiBaru
                );
                apiService.changePassword("Bearer "+ token, request).enqueue(new Callback<GantiKataSandiModel>() {
                    @Override
                    public void onResponse(Call<GantiKataSandiModel> call, Response<GantiKataSandiModel> response) {
                        btn_ganti.setEnabled(true);
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
                                kataSandiLama.setText("");
                                kataSandiBaru.setText("");
                                kataSandiBaruConf.setText("");
                            });
                            dialog.show();
                        } else{
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
                            lanjutanGagal.setOnClickListener(v -> {
                                dialog.dismiss();
                                kataSandiLama.setText("");
                                kataSandiBaru.setText("");
                                kataSandiBaruConf.setText("");
                            });
                            dialog.show();
                        }
                    }

                    @Override
                    public void onFailure(Call<GantiKataSandiModel> call, Throwable t) {
                        btn_ganti.setEnabled(true);
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

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
}