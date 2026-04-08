package com.example.skripsi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KendaraanCache {
    private static List<Kendaraan> list = null;
    private static Map<String, List<Kendaraan>> mapJenis = new HashMap<>();
    private static Map<String, String> mapModelToId = new HashMap<>();

    public static void setData(List<Kendaraan> data) {

        list = data;
        mapJenis.clear();
        mapModelToId.clear();

        for (Kendaraan k : data) {

            String jenis = k.getJenis_motor();
            String model = k.getModel_motor();

            if (!mapJenis.containsKey(jenis)) {
                mapJenis.put(jenis, new ArrayList<>());
            }
            mapJenis.get(jenis).add(k);

            mapModelToId.put(model, k.getId_kendaraan());
        }
    }

    public static boolean isAvailable() {
        return list != null;
    }

    public static List<String> getJenisList() {
        List<String> listJenis = new ArrayList<>(mapJenis.keySet());
        Collections.sort(listJenis);
        return listJenis;
    }

    public static List<String> getModelList(String jenis) {
        List<String> result = new ArrayList<>();

        List<Kendaraan> listK = mapJenis.get(jenis);
        if (listK != null) {
            for (Kendaraan k : listK) {
                result.add(k.getModel_motor());
            }
        }
        return result;
    }

    public static String getIdByModel(String model) {
        return mapModelToId.get(model);
    }
}
