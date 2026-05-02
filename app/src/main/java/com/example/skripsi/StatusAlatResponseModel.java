package com.example.skripsi;

public class StatusAlatResponseModel {
    public boolean status;
    public String message;
    public Data data;

    public static class Data {
        public int total;
        public int aktif;
        public int non_aktif;
        public Lokasi jalan_datang;
        public Lokasi jalan_pulang;
    }

    public static class Lokasi {
        public boolean aktif;
        public String koordinat;
        public String tanggal;
        public String last_update;
    }
}
