package com.baingat.app;

public class LokasiModel {
    private String id;
    private String namaJalan;
    private boolean isSelected;

    public LokasiModel(String id, String namaJalan, boolean isSelected) {
        this.id = id;
        this.namaJalan = namaJalan;
        this.isSelected = isSelected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNamaJalan() {
        return namaJalan;
    }

    public void setNamaJalan(String namaJalan) {
        this.namaJalan = namaJalan;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
