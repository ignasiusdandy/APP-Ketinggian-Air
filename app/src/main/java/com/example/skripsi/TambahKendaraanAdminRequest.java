package com.example.skripsi;

public class TambahKendaraanAdminRequest {
    private String jenis_motor;
    private String model_motor;
    private double batas_kendaraan;

    public TambahKendaraanAdminRequest(String jenis_motor,
                                  String model_motor,
                                  double batas_kendaraan) {
        this.jenis_motor = jenis_motor;
        this.model_motor = model_motor;
        this.batas_kendaraan = batas_kendaraan;
    }

    public String getJenis_motor() {
        return jenis_motor;
    }

    public String getModel_motor() {
        return model_motor;
    }

    public double getBatas_kendaraan() {
        return batas_kendaraan;
    }
}
