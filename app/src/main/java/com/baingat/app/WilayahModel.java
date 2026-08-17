package com.baingat.app;

import java.util.List;

public class WilayahModel {
    private int id;
    private String namaKecamatan;
    private List<LokasiModel> listJalan;
    private boolean isExpanded;

    public WilayahModel(int id, String namaKecamatan, List<LokasiModel> listJalan) {
        this.id = id;
        this.namaKecamatan = namaKecamatan;
        this.listJalan = listJalan;
        this.isExpanded = true; // default expanded
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNamaKecamatan() {
        return namaKecamatan;
    }

    public void setNamaKecamatan(String namaKecamatan) {
        this.namaKecamatan = namaKecamatan;
    }

    public List<LokasiModel> getListJalan() {
        return listJalan;
    }

    public void setListJalan(List<LokasiModel> listJalan) {
        this.listJalan = listJalan;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public boolean isAllLocationsChecked() {
        if (listJalan == null || listJalan.isEmpty()) return false;
        for (LokasiModel lokasi : listJalan) {
            if (!lokasi.isSelected()) {
                return false;
            }
        }
        return true;
    }
}
