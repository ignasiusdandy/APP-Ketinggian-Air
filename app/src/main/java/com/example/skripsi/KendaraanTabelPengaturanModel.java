package com.example.skripsi;

public class KendaraanTabelPengaturanModel {
    String pemilik, kategori, model;

    public KendaraanTabelPengaturanModel(String pemilik,String kategori,String model){
        this.pemilik = pemilik;
        this.kategori = kategori;
        this.model = model;
    }

    public String getPemilik() { return pemilik; }
    public String getKategori() { return kategori; }
    public String getModel() { return model; }
}
