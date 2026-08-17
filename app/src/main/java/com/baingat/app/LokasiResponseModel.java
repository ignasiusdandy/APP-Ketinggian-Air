package com.baingat.app;

import java.util.List;

public class LokasiResponseModel {
    private boolean status;
    private String message;
    private int total;
    private List<Data> data;

    public boolean isStatus() { return status; }
    public String getMessage() { return message; }
    public int getTotal() { return total; }
    public List<Data> getData() { return data; }

    public static class Data {
        private String id_lokasi;
        private String nama_lokasi;
        private String latitude;
        private String longitude;
        private String id_alat;
        private String nama_alat;
        private String id_status_alat;
        private String status_alat;
        private String kecamatan;

        public String getIdLokasi() { return id_lokasi; }
        public String getNamaLokasi() { return nama_lokasi; }
        public String getLatitude() { return latitude; }
        public String getLongitude() { return longitude; }
        public String getIdAlat() { return id_alat; }
        public String getNamaAlat() { return nama_alat; }
        public String getIdStatusAlat() { return id_status_alat; }
        public String getStatusAlat() { return status_alat; }
        public String getKecamatan() { return kecamatan; }
    }
}
