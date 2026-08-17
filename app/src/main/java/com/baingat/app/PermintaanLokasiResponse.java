package com.baingat.app;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PermintaanLokasiResponse {
    private boolean status;
    private List<Data> data;

    public boolean isStatus() { return status; }
    public List<Data> getData() { return data; }

    public static class Data {
        @com.google.gson.annotations.SerializedName(value = "id", alternate = {"id_permintaan"})
        private String id;
        @com.google.gson.annotations.SerializedName("nama_user")
        private String namaUser;
        private String keterangan;

        public String getId() { return id; }
        public String getNamaUser() { return namaUser; }
        
        @SerializedName("created_at")
        private String createdAt;
        
        @SerializedName(value = "status_laporan", alternate = {"status"})
        private String statusLaporan;
        
        @SerializedName(value = "catatan_admin", alternate = {"catatan"})
        private String catatanAdmin;
        
        private String foto;

        public String getKeterangan() { return keterangan; }
        public String getCreatedAt() { return createdAt; }
        public String getStatusLaporan() { return statusLaporan; }
        public String getCatatanAdmin() { return catatanAdmin; }
        public String getFoto() { return foto; }
    }
}
