package com.example.skripsi;

public class KendaraanTabelPengaturanModel {
    String id_kendaraan, plat, kategori, model;
    boolean isUtama;


    public KendaraanTabelPengaturanModel(String id_kendaraan, String plat,String kategori,String model, boolean isUtama){
        this.id_kendaraan = id_kendaraan;
        this.plat = plat;
        this.kategori = kategori;
        this.model = model;
        this.isUtama = isUtama;
    }

    public String getPlat() { return plat; }
    public String getKategori() { return kategori; }
    public String getModel() { return model; }
    public String getIdKendaraan() { return id_kendaraan; }

    public boolean isKendaraanUtama() {
        return isUtama;
    }
}
