package com.example.skripsi;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ProfileFragment extends Fragment {
    LinearLayout btnAkun, btnPassword, btnKendaraan;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        btnAkun = view.findViewById(R.id.pengaturan_akun);
        btnPassword = view.findViewById(R.id.ubah_kata_sandi);
        btnKendaraan = view.findViewById(R.id.kendaraanUser);

        loadInnerFragment(new PengaturanAkunFragment());
        setSelected(btnAkun);

        btnAkun.setOnClickListener(v -> {
            setSelected(btnAkun);
            loadInnerFragment(new PengaturanAkunFragment());
        });

        btnKendaraan.setOnClickListener(v -> {
//                setSelected(btnAkun);
//                loadInnerFragment(new KendaraanAkunFragment());
            Intent intent = new Intent(getActivity(), DaftarKendaraanUserActivity.class);
            startActivity(intent);
        });

        btnPassword.setOnClickListener(v -> {
            setSelected(btnPassword);
            loadInnerFragment(new GantiKataSandiFragment());
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
        btnKendaraan.setSelected(false);

        selected.setSelected(true);
    }

    }