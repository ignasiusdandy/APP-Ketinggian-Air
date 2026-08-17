package com.baingat.app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RiwayatAdapter extends RecyclerView.Adapter<RiwayatAdapter.ViewHolder> {

    private List<RiwayatModel> riwayatList;

    public RiwayatAdapter(List<RiwayatModel> riwayatList) {
        this.riwayatList = riwayatList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_riwayat, parent, false);
        return new ViewHolder(view);
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "-";
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
            inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date date = inputFormat.parse(rawDate);
            
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", new java.util.Locale("id", "ID"));
            return outputFormat.format(date) + " WIB";
        } catch (Exception e) {
            return rawDate;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RiwayatModel riwayat = riwayatList.get(position);

        String formattedDate = formatDate(riwayat.getTanggal());

        holder.tvTipeLaporan.setText(riwayat.getTipeLaporan());
        holder.tvTanggal.setText(formattedDate);
        holder.tvDeskripsiLaporan.setText(riwayat.getDeskripsi());
        
        String namaPelapor = riwayat.getNamaPelapor();
        if (holder.llPelapor != null && holder.tvNamaPelapor != null) {
            if (namaPelapor != null && !namaPelapor.trim().isEmpty()) {
                holder.llPelapor.setVisibility(View.VISIBLE);
                holder.tvNamaPelapor.setText("Oleh: " + namaPelapor);
            } else {
                holder.llPelapor.setVisibility(View.GONE);
            }
        }

        String tipe = riwayat.getTipeLaporan();
        String status = riwayat.getStatus();

        // --- Ikon dan warna circle berdasarkan jenis laporan ---
        if (tipe != null && tipe.toLowerCase().contains("kendaraan")) {
            if (status.equalsIgnoreCase("Diterima") || status.equalsIgnoreCase("Selesai")) {
                // Lingkaran hijau solid dengan centang
                holder.flIconContainer.setBackgroundResource(R.drawable.bg_icon_green_solid);
                holder.ivIconLaporan.setImageResource(R.drawable.berhasil_icon);
                holder.ivIconLaporan.setImageTintList(
                        ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            } else {
                // Lingkaran merah solid dengan X
                holder.flIconContainer.setBackgroundResource(R.drawable.bg_icon_red_soft);
                holder.ivIconLaporan.setImageResource(R.drawable.kesalahan_icon);
                holder.ivIconLaporan.setImageTintList(
                        ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            }
        } else if (tipe != null && tipe.toLowerCase().contains("lokasi")) {
            // Lingkaran biru untuk permintaan lokasi
            holder.flIconContainer.setBackgroundResource(R.drawable.bg_icon_blue_soft);
            holder.ivIconLaporan.setImageResource(R.drawable.maps_icon);
            holder.ivIconLaporan.setImageTintList(
                    ColorStateList.valueOf(Color.parseColor("#2563EB")));
        } else {
            // Default: Lingkaran oranye untuk kondisi jalan
            holder.flIconContainer.setBackgroundResource(R.drawable.bg_icon_orange_soft);
            holder.ivIconLaporan.setImageResource(R.drawable.peringatan_icon);
            holder.ivIconLaporan.setImageTintList(
                    ColorStateList.valueOf(Color.parseColor("#EA6C0A")));
        }

        // --- Warna teks status ---
        holder.tvStatusLaporan.setText(status);
        if (status.equalsIgnoreCase("Diterima") || status.equalsIgnoreCase("Selesai")) {
            holder.tvStatusLaporan.setTextColor(Color.parseColor("#10B981")); // Hijau
        } else if (status.equalsIgnoreCase("Diproses")) {
            holder.tvStatusLaporan.setTextColor(Color.parseColor("#F59E0B")); // Oranye
        } else if (status.equalsIgnoreCase("Dibatalkan") || status.equalsIgnoreCase("Ditolak")) {
            holder.tvStatusLaporan.setTextColor(Color.parseColor("#EF4444")); // Merah
        } else {
            holder.tvStatusLaporan.setTextColor(Color.parseColor("#6B7280")); // Abu-abu (Menunggu)
        }
        
        // --- Klik Tombol Detail ---
        if (holder.btnDetail != null) {
            holder.btnDetail.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), DetailRiwayatActivity.class);
                intent.putExtra("idLaporan", riwayat.getIdLaporan());
                intent.putExtra("tipe", tipe);
                intent.putExtra("tanggal", formattedDate);
                intent.putExtra("deskripsi", riwayat.getDeskripsi());
                intent.putExtra("status", status);
                intent.putExtra("pesan", riwayat.getPesanAdmin());
                intent.putExtra("foto", riwayat.getFoto());
                intent.putExtra("lokasi", riwayat.getLokasi());
                intent.putExtra("namaPelapor", riwayat.getNamaPelapor());
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return riwayatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipeLaporan, tvTanggal, tvDeskripsiLaporan, tvStatusLaporan, tvNamaPelapor;
        FrameLayout flIconContainer;
        ImageView ivIconLaporan;
        View btnDetail, llPelapor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipeLaporan = itemView.findViewById(R.id.tvTipeLaporan);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvDeskripsiLaporan = itemView.findViewById(R.id.tvDeskripsiLaporan);
            tvStatusLaporan = itemView.findViewById(R.id.tvStatusLaporan);
            flIconContainer = itemView.findViewById(R.id.flIconContainer);
            ivIconLaporan = itemView.findViewById(R.id.ivIconLaporan);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            tvNamaPelapor = itemView.findViewById(R.id.tvNamaPelapor);
            llPelapor = itemView.findViewById(R.id.llPelapor);
        }
    }
}
