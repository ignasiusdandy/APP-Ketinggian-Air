package com.example.skripsi;

public class KalibrasiResponseModel {
    public boolean status;
    public String message;
    public Data data;

    public static class Data {
        public double kalibrasi;
        public Double ketinggian;
        public Double tinggi_final;
        public String waktu_kalibrasi;
        public String waktu_sensor;
    }
}
