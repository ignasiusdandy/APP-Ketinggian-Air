package com.example.skripsi;

public class KendaraanAdminModel {

    private String idKendaraan;
    private String namaKendaraan;
    private String jenisKendaraan;
    private double batasKendaraan;


    // 🔥 CONSTRUCTOR
    public KendaraanAdminModel(String idKendaraan,
                               String namaKendaraan,
                               String jenisKendaraan,
                               Double batasKendaraan) {
        this.idKendaraan = idKendaraan;
        this.namaKendaraan = namaKendaraan;
        this.jenisKendaraan = jenisKendaraan;
        this.batasKendaraan = batasKendaraan;
    }

    // 🔹 GETTER
    public String getIdKendaraan() {
        return idKendaraan;
    }

    public String getNamaKendaraan() {
        return namaKendaraan;
    }


    public String getJenisKendaraan() {
        return jenisKendaraan;
    }

    public double getBatasKendaraan() {
        return batasKendaraan;
    }

    // 🔹 SETTER (opsional, tapi bagus untuk edit nanti)
    public void setNamaKendaraan(String namaKendaraan) {
        this.namaKendaraan = namaKendaraan;
    }

    public void setJenisKendaraan(String jenisKendaraan) {
        this.jenisKendaraan = jenisKendaraan;
    }
}