package com.example.skripsi;

public class TambahKendaraanRequestModel {
    private String id_kendaraan;
    private String pemilik_kendaraan;

    public TambahKendaraanRequestModel(String id_kendaraan, String pemilik_kendaraan) {
        this.id_kendaraan = id_kendaraan;
        this.pemilik_kendaraan = pemilik_kendaraan;
    }

    public String getId_kendaraan() {
        return id_kendaraan;
    }

    public String getPemilik_kendaraan() {
        return pemilik_kendaraan;
    }
}
