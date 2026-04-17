package com.example.skripsi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class KendaraanTabelPengaturanAdapter extends RecyclerView.Adapter<KendaraanTabelPengaturanAdapter.ViewHolder>{
    List<KendaraanTabelPengaturanModel> list;

    public KendaraanTabelPengaturanAdapter(List<KendaraanTabelPengaturanModel> list){
        this.list = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPemilik, tvKategori, tvModel;

        public ViewHolder(View itemView) {
            super(itemView);
            tvPemilik = itemView.findViewById(R.id.tvPemilik);
            tvKategori = itemView.findViewById(R.id.tvKategori);
            tvModel = itemView.findViewById(R.id.tvModel);
        }
    }

    @Override
    public KendaraanTabelPengaturanAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kendaraan_pengaturan, parent, false);
        return new KendaraanTabelPengaturanAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(KendaraanTabelPengaturanAdapter.ViewHolder holder, int position) {
        KendaraanTabelPengaturanModel k = list.get(position);

        holder.tvPemilik.setText(k.getPemilik());
        holder.tvKategori.setText(k.getKategori());
        holder.tvModel.setText(k.getModel());

        if(k.getPemilik().equals("Orang Lain")){
            holder.tvPemilik.setTextColor(
                    holder.itemView.getContext().getColor(R.color.tulisanabu)
            );
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
