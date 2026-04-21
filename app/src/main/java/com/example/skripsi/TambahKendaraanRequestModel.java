package com.example.skripsi;

public class TambahKendaraanRequestModel {
    private String id_kendaraan;
    private String plat_kendaraan;

    public TambahKendaraanRequestModel(String id_kendaraan, String plat_kendaraan) {
        this.id_kendaraan = id_kendaraan;
        this.plat_kendaraan = plat_kendaraan;
    }

    public String getId_kendaraan() {
        return id_kendaraan;
    }

    public String getPlat_kendaraan() {
        return plat_kendaraan;
    }
}
