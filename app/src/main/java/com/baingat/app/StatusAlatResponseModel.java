package com.baingat.app;

public class StatusAlatResponseModel {
    public boolean status;
    public String message;
    public Data data;

    public static class Data {
        public int total;
        public int aktif;
        public int non_aktif;
        
        @com.google.gson.annotations.SerializedName(value="alat", alternate={"data_alat", "daftar_alat", "perangkat", "list_alat", "data", "kendaraan", "alat_list"})
        public java.util.List<Alat> alat;
        
        public Alat jalan_datang;
        public Alat jalan_pulang;
        
        public java.util.List<Alat> getAlatList() {
            if (alat != null && !alat.isEmpty()) {
                return alat;
            }
            java.util.List<Alat> list = new java.util.ArrayList<>();
            if (jalan_datang != null) {
                if (jalan_datang.nama_alat == null) jalan_datang.nama_alat = "Jalan Datang";
                if (jalan_datang.id_alat == null) jalan_datang.id_alat = "ALT001";
                list.add(jalan_datang);
            }
            if (jalan_pulang != null) {
                if (jalan_pulang.nama_alat == null) jalan_pulang.nama_alat = "Jalan Pulang";
                if (jalan_pulang.id_alat == null) jalan_pulang.id_alat = "ALT002";
                list.add(jalan_pulang);
            }
            return list;
        }
    }

    public static class Alat {
        @com.google.gson.annotations.SerializedName(value="id_alat", alternate={"id"})
        public String id_alat;
        @com.google.gson.annotations.SerializedName(value="nama_alat", alternate={"nama", "keterangan"})
        public String nama_alat;
        public String nama_lokasi;
        public boolean aktif;
        public String koordinat;
        public String tanggal;
        public String last_update;
        
        public String getDisplayName() {
            if (nama_lokasi != null && !nama_lokasi.trim().isEmpty()) {
                return nama_lokasi;
            }
            if (nama_alat != null && !nama_alat.trim().isEmpty()) {
                return nama_alat;
            }
            return "Alat";
        }
    }
}
