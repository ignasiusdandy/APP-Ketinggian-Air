package com.example.skripsi;

import com.google.gson.annotations.SerializedName;

public class EditKendaraanRequestModel {
    @SerializedName("id_kendaraan_baru")
    private String idKendaraanBaru;

    @SerializedName("plat_kendaraan")
    private String plat;

    public EditKendaraanRequestModel(String idKendaraanBaru, String pemilikKendaraan) {
        this.idKendaraanBaru = idKendaraanBaru;
        this.plat = plat;
    }

    public String getIdKendaraanBaru() {
        return idKendaraanBaru;
    }

    public String getPlat() {
        return plat;
    }
}
