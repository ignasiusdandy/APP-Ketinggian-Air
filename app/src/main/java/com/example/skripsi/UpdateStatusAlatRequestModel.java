package com.example.skripsi;

public class UpdateStatusAlatRequestModel {
    public String id_alat;
    public String status;

    public UpdateStatusAlatRequestModel(String id_alat, String status) {
        this.id_alat = id_alat;
        this.status = status;
    }
}
