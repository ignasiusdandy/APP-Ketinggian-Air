package com.example.skripsi;

import com.google.gson.annotations.SerializedName;

public class Kendaraan {
    @SerializedName("id_kendaraan")
    private String id_kendaraan;

    @SerializedName("jenis_motor")
    private String jenis_motor;

    @SerializedName("model_motor")
    private String model_motor;

    public String getId_kendaraan() {
        return id_kendaraan;
    }

    public String getJenis_motor() {
        return jenis_motor;
    }

    public String getModel_motor() {
        return model_motor;
    }

    @Override
    public String toString(){
        return jenis_motor + " - " + model_motor;
    }
}
