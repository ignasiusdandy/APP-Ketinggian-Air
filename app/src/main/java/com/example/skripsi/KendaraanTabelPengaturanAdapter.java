package com.example.skripsi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class KendaraanTabelPengaturanAdapter extends RecyclerView.Adapter<KendaraanTabelPengaturanAdapter.ViewHolder>{
    List<KendaraanTabelPengaturanModel> list;
    OnItemAction listener;


    public KendaraanTabelPengaturanAdapter(List<KendaraanTabelPengaturanModel> list, OnItemAction listener){
        this.list = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvPlat;
        ImageView btnUtama;
        Button btnEdit, btnDelete;
        LinearLayout layoutStatus;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNama = itemView.findViewById(R.id.tvNama);
            tvPlat = itemView.findViewById(R.id.tvPlat);
            layoutStatus = itemView.findViewById(R.id.layoutStatus);
            btnUtama = itemView.findViewById(R.id.btnUtama);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.daftar_kendaraan_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(KendaraanTabelPengaturanAdapter.ViewHolder holder, int position) {
        KendaraanTabelPengaturanModel k = list.get(position);
        String kategori = k.getKategori();
        String model = k.getModel();
        String nama = kategori+ " " + model;

        holder.tvNama.setText(nama);
        holder.tvPlat.setText(k.getPlat());

        if (k.isKendaraanUtama()) {
            holder.layoutStatus.setVisibility(View.VISIBLE);
            holder.btnUtama.setVisibility(View.GONE);
        } else {
            holder.layoutStatus.setVisibility(View.GONE);
            holder.btnUtama.setVisibility(View.VISIBLE);
        }

        holder.btnUtama.setOnClickListener(v -> {
            listener.onSetUtama(k);
        });

        holder.btnEdit.setOnClickListener(v -> {
            listener.onEdit(k);
        });

        holder.btnDelete.setOnClickListener(v -> {
            listener.onDelete(k);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public interface OnItemAction {
        void onEdit(KendaraanTabelPengaturanModel data);
        void onDelete(KendaraanTabelPengaturanModel data);
        void onSetUtama(KendaraanTabelPengaturanModel data);
    }
}
