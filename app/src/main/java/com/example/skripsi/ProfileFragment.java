package com.example.skripsi;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ProfileFragment extends Fragment {
    LinearLayout btnAkun, btnPassword, btnKeluar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        btnAkun = view.findViewById(R.id.pengaturan_akun);
        btnPassword = view.findViewById(R.id.ubah_kata_sandi);
        btnKeluar = view.findViewById(R.id.logout);

        loadInnerFragment(new PengaturanAkunFragment());
        setSelected(btnAkun);

        btnAkun.setOnClickListener(v -> {
            setSelected(btnAkun);
            loadInnerFragment(new PengaturanAkunFragment());
        });

        btnPassword.setOnClickListener(v -> {
            setSelected(btnPassword);
            loadInnerFragment(new GantiKataSandiFragment());
        });

        btnKeluar.setOnClickListener(v -> {
            setSelected(btnKeluar);
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        return view;


}

    private void loadInnerFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_profile_detail, fragment)
                .commit();
    }

    private void setSelected(View selected){
        btnAkun.setSelected(false);
        btnPassword.setSelected(false);
        btnKeluar.setSelected(false);

        selected.setSelected(true);
    }

    }