package com.example.skripsi;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class KendaraanAdminResponseModel {

    @SerializedName("data")
    private List<Data> data;

    public List<Data> getData() {
        return data;
    }

    public static class Data {

        @SerializedName("id_kendaraan")
        private String idKendaraan;

        @SerializedName("model_motor")
        private String namaKendaraan;

        @SerializedName("jenis_motor")
        private String jenisKendaraan;

        @SerializedName("batas_kendaraan")
        private Double batasKendaraan;


        public String getIdKendaraan() {
            return idKendaraan;
        }

        public String getNamaKendaraan() {
            return namaKendaraan;
        }

        public String getJenisKendaraan() {
            return jenisKendaraan;
        }

        public Double getBatasKendaraan() {
            return batasKendaraan;
        }
    }
}