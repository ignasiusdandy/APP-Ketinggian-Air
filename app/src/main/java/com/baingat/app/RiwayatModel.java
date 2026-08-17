package com.baingat.app;

public class RiwayatModel {
    private String idLaporan;
    private String tipeLaporan;
    private String deskripsi;
    private String tanggal;
    private String status; // e.g. "Menunggu", "Diproses", "Selesai"
    private String pesanAdmin;
    private String foto;
    private String namaPelapor;

    public RiwayatModel(String idLaporan, String tipeLaporan, String deskripsi, String tanggal, String status) {
        this.idLaporan = idLaporan;
        this.tipeLaporan = tipeLaporan;
        this.deskripsi = deskripsi;
        this.tanggal = tanggal;
        this.status = status;
        this.pesanAdmin = ""; // default empty
        this.namaPelapor = "";
    }
    
    public RiwayatModel(String idLaporan, String tipeLaporan, String deskripsi, String tanggal, String status, String pesanAdmin, String foto) {
        this.idLaporan = idLaporan;
        this.tipeLaporan = tipeLaporan;
        this.deskripsi = deskripsi;
        this.tanggal = tanggal;
        this.status = status;
        this.pesanAdmin = pesanAdmin;
        this.foto = foto;
        this.namaPelapor = "";
    }
    
    public RiwayatModel(String idLaporan, String tipeLaporan, String deskripsi, String tanggal, String status, String pesanAdmin, String foto, String namaPelapor) {
        this.idLaporan = idLaporan;
        this.tipeLaporan = tipeLaporan;
        this.deskripsi = deskripsi;
        this.tanggal = tanggal;
        this.status = status;
        this.pesanAdmin = pesanAdmin;
        this.foto = foto;
        this.namaPelapor = namaPelapor;
    }
    
    public String getNamaPelapor() { return namaPelapor; }
    public void setNamaPelapor(String namaPelapor) { this.namaPelapor = namaPelapor; }

    public String getIdLaporan() { return idLaporan; }
    public void setIdLaporan(String idLaporan) { this.idLaporan = idLaporan; }

    public String getTipeLaporan() {
        return tipeLaporan;
    }

    public void setTipeLaporan(String tipeLaporan) {
        this.tipeLaporan = tipeLaporan;
    }

    public String getLokasi() {
        if (deskripsi != null) {
            if (deskripsi.startsWith("📍 ")) {
                int newlineIndex = deskripsi.indexOf("\n\n");
                if (newlineIndex != -1) {
                    return deskripsi.substring(2, newlineIndex).trim();
                } else {
                    return deskripsi.substring(2).trim();
                }
            } else if (deskripsi.startsWith("Lokasi: Lokasi Tersimpan: Lat")) {
                int end = deskripsi.indexOf("\n");
                if (end != -1) {
                    return deskripsi.substring(25, end).trim(); // Extract starting after "Lokasi: Lokasi Tersimpan: "
                } else {
                    return deskripsi.substring(25).trim();
                }
            } else if (deskripsi.startsWith("Lokasi: ")) {
                int end = deskripsi.indexOf("\n");
                if (end != -1) {
                    return deskripsi.substring(8, end).trim();
                } else {
                    return deskripsi.substring(8).trim();
                }
            }
        }
        return "";
    }

    public String getDeskripsi() {
        if (deskripsi != null) {
            String cleaned = deskripsi;
            // Hapus format lama maupun baru jika ada
            if (cleaned.startsWith("📍 ")) {
                cleaned = cleaned.replaceFirst("^📍 [^\\n]*[\\n\\s]*", "");
            } else if (cleaned.startsWith("Lokasi: Lokasi Tersimpan: Lat")) {
                cleaned = cleaned.replaceFirst("^Lokasi: Lokasi Tersimpan: Lat [-\\d.]+, Lng [\\d.]+[\\n\\s]*", "");
            } else if (cleaned.startsWith("Lokasi: ")) {
                cleaned = cleaned.replaceFirst("^Lokasi: [^\\n]*[\\n\\s]*", "");
            }
            return cleaned.trim();
        }
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPesanAdmin() {
        return pesanAdmin;
    }
    public String getFoto() {
        return foto;
    }

    public void setPesanAdmin(String pesanAdmin) {
        this.pesanAdmin = pesanAdmin;
        this.foto = foto;
    }
}
