package com.example.skripsi;

public class EditKendaraanAdminRequestModel {
    private String jenis_motor;
    private String model_motor;
    private double batas_kendaraan;

    public EditKendaraanAdminRequestModel(String jenis, String model, double batas) {
        this.jenis_motor = jenis;
        this.model_motor = model;
        this.batas_kendaraan = batas;
    }
}
