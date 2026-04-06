package com.example.skripsi;

public class KendaraanUtamaResponseModel {
    boolean status;
    String message;
    Data data;

    public boolean isStatus() { return status; }
    public String getMessage() { return message; }
    public Data getData() { return data; }

    public class Data {
        String jenis_motor;
        String model_motor;

        public String getJenisMotor() { return jenis_motor; }
        public String getModelMotor() { return model_motor; }
    }
}
