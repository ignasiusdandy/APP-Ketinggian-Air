package com.example.skripsi;

import java.util.List;

public class StatusUtamaResponseModel {
    private boolean status;
    private Lokasi datang;
    private Lokasi pulang;

    public boolean isStatus() { return status; }
    public Lokasi getDatang() { return datang; }
    public Lokasi getPulang() { return pulang; }

    public static class Lokasi {
        private double batas_kendaraan;
        private Data data;

        public double getBatasKendaraan() { return batas_kendaraan; }
        public Data getData() { return data; }
    }

    public static class Data {
        private double tinggi;
        private double persen;
        private String status;
        private double score;
        private double kecepatan;
        private String risiko;
        private String lastUpdate;
        private String kendaraan;


        public double getTinggi() { return tinggi; }
        public double getPersen() { return persen; }
        public String getStatus() { return status; }
        public double getScore() { return score; }
        public double getKecepatan() { return kecepatan; }
        public String getLastUpdate() { return lastUpdate; }

        public String getRisiko() { return risiko; }
        public String getKendaraan() { return kendaraan; }
    }
}
