package com.example.skripsi;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
public class SessionManager {
    private static final String PREF_NAME = "SkripsiSession";

    // Mode privat agar hanya bisa diakses aplikasi ini
    private static final int PRIVATE_MODE = Context.MODE_PRIVATE;

    public static final String KEY_IS_LOGIN = "IsLoggedIn";
    public static final String KEY_NAMA = "nama";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_MOTOR = "motor";
    public static final String KEY_MODEL = "model";

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    // Fungsi untuk membuat sesi login (Menyimpan data)
    public void createLoginSession(String nama, String email, String motor, String model) {
        editor.putBoolean(KEY_IS_LOGIN, true);
        editor.putString(KEY_NAMA, nama);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_MOTOR, motor);
        editor.putString(KEY_MODEL, model);
        editor.apply(); // Simpan perubahan
    }


    // Cek apakah user sudah login
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGIN, false);
    }

    // Ambil detail user (untuk ditampilkan di Profile/Home)
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_NAMA, pref.getString(KEY_NAMA, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        user.put(KEY_MOTOR, pref.getString(KEY_MOTOR, null));
        user.put(KEY_MODEL, pref.getString(KEY_MODEL, null));
        return user;
    }



    // Fungsi Logout (Hapus data)
    public void logoutUser() {
        editor.clear();
        editor.commit();
    }
}
