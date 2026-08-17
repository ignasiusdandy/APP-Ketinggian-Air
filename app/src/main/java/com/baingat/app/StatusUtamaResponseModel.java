package com.baingat.app;

import java.util.List;

public class StatusUtamaResponseModel {
    private boolean status;
    private List<Data> data;

    public boolean isStatus() { return status; }
    public List<Data> getData() { return data; }

    public static class Data {
        private String id_lokasi;
        private String nama_lokasi;
        private String latitude;
        private String longitude;
        private double tinggi;
        private double persen;
        private double score;
        private double kecepatan;
        private String risiko;
        private String lastUpdate;
        private String kendaraan;

        public String getIdLokasi() { return id_lokasi; }
        public String getNamaLokasi() { return nama_lokasi; }
        public String getLatitude() { return latitude; }
        public String getLongitude() { return longitude; }
        public double getTinggi() { return tinggi; }
        public double getPersen() { return persen; }
        public double getScore() { return score; }
        public double getKecepatan() { return kecepatan; }
        public String getLastUpdate() { return lastUpdate; }
        public String getRisiko() { return risiko; }
        public String getKendaraan() { return kendaraan; }

        public void setIdLokasi(String id_lokasi) { this.id_lokasi = id_lokasi; }
        public void setNamaLokasi(String nama_lokasi) { this.nama_lokasi = nama_lokasi; }
        public void setLatitude(String latitude) { this.latitude = latitude; }
        public void setLongitude(String longitude) { this.longitude = longitude; }
        public void setTinggi(double tinggi) { this.tinggi = tinggi; }
        public void setPersen(double persen) { this.persen = persen; }
        public void setScore(double score) { this.score = score; }
        public void setKecepatan(double kecepatan) { this.kecepatan = kecepatan; }
        public void setLastUpdate(String lastUpdate) { this.lastUpdate = lastUpdate; }
        public void setRisiko(String risiko) { this.risiko = risiko; }
        public void setKendaraan(String kendaraan) { this.kendaraan = kendaraan; }
    }
}
