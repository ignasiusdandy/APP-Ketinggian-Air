package com.example.skripsi;

import java.util.List;

public class KendaraanUserResponseModel {
    private boolean status;
    private String message;
    List<DataKendaraanUser> data;

    public boolean isStatus() {
        return status;
    }

    public String getMessage(){
        return message;
    }

    public List<DataKendaraanUser> getData(){
        return data;
    }

    public static class DataKendaraanUser{
        private String id_kendaraan;
        private String jenis_motor;
        private String model_motor;
        private int kendaraan_utama;
        private String plat_kendaraan;
        private String status;

        public String getId(){
            return id_kendaraan;
        }

        public String getJenisMotor(){
            return jenis_motor;
        }

        public String getModelMotor(){
            return model_motor;
        }

        public boolean isKendaraanUtama(){
            return kendaraan_utama == 1;
        }

        public String getPlatKendaraan() {
            return plat_kendaraan;
        }
        public String getStatus() { return status; }

        public String getNamaLengkapMotor(){
            return jenis_motor + " " + model_motor;
        }

    }
}



