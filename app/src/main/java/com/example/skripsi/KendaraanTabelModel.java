package com.example.skripsi;

public class KendaraanTabelModel {
    String pemilik, kategori, model, status;

    public KendaraanTabelModel(String pemilik,String kategori,String model,String status){
        this.pemilik = pemilik;
        this.kategori = kategori;
        this.model = model;
        this.status = status;
    }

    public String getPemilik() { return pemilik; }
    public String getKategori() { return kategori; }
    public String getModel() { return model; }
    public String getStatus() { return status; }
}
