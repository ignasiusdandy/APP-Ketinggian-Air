package com.example.skripsi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.time.temporal.Temporal;
import java.util.List;

public class KendaraanTabelAdapter extends RecyclerView.Adapter<KendaraanTabelAdapter.ViewHolder> {
    List<KendaraanTabelModel> list;

    public KendaraanTabelAdapter(List<KendaraanTabelModel> list){
        this.list = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPemilik, tvKategori, tvModel, tvStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            tvPemilik = itemView.findViewById(R.id.tvPemilik);
            tvKategori = itemView.findViewById(R.id.tvKategori);
            tvModel = itemView.findViewById(R.id.tvModel);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_kendaraan, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            KendaraanTabelModel k = list.get(position);

            holder.tvPemilik.setText(k.getPemilik());
            holder.tvKategori.setText(k.getKategori());
            holder.tvModel.setText(k.getModel());
            holder.tvStatus.setText( k.getStatus());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
}
