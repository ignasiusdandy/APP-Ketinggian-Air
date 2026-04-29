package com.example.skripsi;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
public class SessionManager {
    private static final String PREF_NAME = "SkripsiSession";

    // Mode privat agar hanya bisa diakses aplikasi ini
    private static final int PRIVATE_MODE = Context.MODE_PRIVATE;

    public static final String KEY_IS_LOGIN = "IsLoggedIn";
    public static final String KEY_ID = "id_user";
    public static final String KEY_NAMA = "nama";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_ROLE = "role";


    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    // Fungsi untuk membuat sesi login (Menyimpan data)
    public void createLoginSession(String id, String nama, String email, String token, String role) {
        editor.putBoolean(KEY_IS_LOGIN, true);
        editor.putString(KEY_ID, id);
        editor.putString(KEY_NAMA, nama);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }


    // Cek apakah user sudah login
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGIN, false);
    }

    // Ambil detail user (untuk ditampilkan di Profile/Home)
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();

        user.put(KEY_ID, pref.getString(KEY_ID, null));
        user.put(KEY_NAMA, pref.getString(KEY_NAMA, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        user.put(KEY_TOKEN, pref.getString(KEY_TOKEN, null));
        user.put(KEY_ROLE, pref.getString(KEY_ROLE, null));

        return user;
    }

    public void updateNama(String namaBaru){
        editor.putString(KEY_NAMA, namaBaru);
        editor.apply();
    }


    // ambil token
    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }


    // Fungsi Logout (Hapus data)
    public void logoutUser() {
        editor.clear();
        editor.commit();
    }

    public void setLastNotifTime(long time) {
        editor.putLong("last_notif_time", time);
        editor.apply();
    }

    public long getLastNotifTime() {
        return pref.getLong("last_notif_time", 0);
    }
}
