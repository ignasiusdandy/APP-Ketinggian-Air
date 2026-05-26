package com.baingat.app;

import java.util.List;

public class KendaraanResponse {
    boolean status;
    String message;
    List<Kendaraan> data;

    public List<Kendaraan> getData() { return data; }
}
