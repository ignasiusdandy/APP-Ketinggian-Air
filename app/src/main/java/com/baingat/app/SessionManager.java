package com.baingat.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.UUID;

public class SessionManager {
    private static final String PREF_NAME = "SkripsiSession";

    // Mode privat agar hanya bisa diakses aplikasi ini
    private static final int PRIVATE_MODE = Context.MODE_PRIVATE;

    public static final String KEY_IS_LOGIN = "IsLoggedIn";
    public static final String KEY_IS_FIRST_TIME = "IsFirstTimeLaunch";
    public static final String KEY_ID = "id_user";
    public static final String KEY_NAMA = "nama";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_ROLE = "role";
    private static final String LAST_NOTIF_DATANG = "last_notif_datang";
    private static final String LAST_NOTIF_PULANG = "last_notif_pulang";
    public static final String KEY_DEVICE_ID = "device_id";


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

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(KEY_IS_FIRST_TIME, isFirstTime);
        editor.apply();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(KEY_IS_FIRST_TIME, true);
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

    public String getDeviceId() {
        String deviceId = pref.getString(KEY_DEVICE_ID, null);

        // Jika belum ada (misal baru pertama kali install), buat baru!
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            editor.putString(KEY_DEVICE_ID, deviceId);
            editor.apply();
        }

        return deviceId;
    }

    // Fungsi Logout (Hapus data)
    public void logoutUser() {
        // Simpan dulu deviceId sebelum semuanya dihapus
        String currentDeviceId = getDeviceId();
        editor.clear();
        editor.commit();

        // Kembalikan deviceId ke dalam SharedPreferences
        editor.putString(KEY_DEVICE_ID, currentDeviceId);
        editor.apply();
    }

    public void setLastNotifTime(long time) {
        editor.putLong("last_notif_time", time);
        editor.apply();
    }

    public long getLastNotifTime() {
        return pref.getLong("last_notif_time", 0);
    }

    public void setLastNotifDatang(long time){
        editor.putLong(LAST_NOTIF_DATANG, time);
        editor.apply();
    }

    public long getLastNotifDatang(){
        return pref.getLong(LAST_NOTIF_DATANG, 0);
    }

    public void setLastNotifPulang(long time){
        editor.putLong(LAST_NOTIF_PULANG, time);
        editor.apply();
    }

    public long getLastNotifPulang(){
        return pref.getLong(LAST_NOTIF_PULANG, 0);
    }
}
