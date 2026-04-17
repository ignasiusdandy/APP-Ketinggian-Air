package com.example.skripsi;

import com.google.gson.annotations.SerializedName;

public class EditKendaraanRequestModel {
    @SerializedName("id_kendaraan_baru")
    private String idKendaraanBaru;

    @SerializedName("pemilik_kendaraan")
    private String pemilikKendaraan;

    public EditKendaraanRequestModel(String idKendaraanBaru, String pemilikKendaraan) {
        this.idKendaraanBaru = idKendaraanBaru;
        this.pemilikKendaraan = pemilikKendaraan;
    }

    public String getIdKendaraanBaru() {
        return idKendaraanBaru;
    }

    public String getPemilikKendaraan() {
        return pemilikKendaraan;
    }
}
