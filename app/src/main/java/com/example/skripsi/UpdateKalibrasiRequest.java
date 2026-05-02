package com.example.skripsi;

public class UpdateKalibrasiRequest {
    public String id_alat;
    public double offset;

    public UpdateKalibrasiRequest(String id_alat, double offset) {
        this.id_alat = id_alat;
        this.offset = offset;
    }
}
