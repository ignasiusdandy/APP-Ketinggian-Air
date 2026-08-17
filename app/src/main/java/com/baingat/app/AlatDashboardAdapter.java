package com.baingat.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AlatDashboardAdapter extends RecyclerView.Adapter<AlatDashboardAdapter.AlatViewHolder> {

    private Context context;
    private List<StatusAlatResponseModel.Alat> alatList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(StatusAlatResponseModel.Alat alat);
    }

    public AlatDashboardAdapter(Context context, List<StatusAlatResponseModel.Alat> alatList, OnItemClickListener listener) {
        this.context = context;
        this.alatList = alatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alat_dashboard, parent, false);
        return new AlatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlatViewHolder holder, int position) {
        StatusAlatResponseModel.Alat alat = alatList.get(position);

        holder.tvNamaAlat.setText(alat.getDisplayName());
        holder.tvWaktuUpdate.setText("Terakhir Diperbaharui " + (alat.last_update != null ? alat.last_update : ""));

        if (alat.aktif) {
            holder.tvStatusAlat.setText("Status: Aktif");
            holder.tvStatusAlat.setTextColor(context.getResources().getColor(R.color.hijauaman));
            holder.ivDotStatus.setImageResource(R.drawable.bulathijaukecil);
            holder.bgAlatIcon.setBackground(context.getResources().getDrawable(R.drawable.bg_icon_hijau));
            holder.ivAlatIcon.setImageResource(R.drawable.cpu_icon);
        } else {
            holder.tvStatusAlat.setText("Status: Non Aktif");
            holder.tvStatusAlat.setTextColor(context.getResources().getColor(R.color.peringatan));
            holder.ivDotStatus.setImageResource(R.drawable.bulatmerahkecil);
            holder.bgAlatIcon.setBackground(context.getResources().getDrawable(R.drawable.bg_icon_merah));
            holder.ivAlatIcon.setImageResource(R.drawable.cpu_merah);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(alat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return alatList != null ? alatList.size() : 0;
    }

    public void updateData(List<StatusAlatResponseModel.Alat> newAlatList) {
        this.alatList = newAlatList;
        notifyDataSetChanged();
    }

    public static class AlatViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaAlat, tvStatusAlat, tvWaktuUpdate;
        ImageView ivAlatIcon, ivDotStatus;
        LinearLayout bgAlatIcon;

        public AlatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaAlat = itemView.findViewById(R.id.tvNamaAlat);
            tvStatusAlat = itemView.findViewById(R.id.tvStatusAlat);
            tvWaktuUpdate = itemView.findViewById(R.id.tvWaktuUpdate);
            ivAlatIcon = itemView.findViewById(R.id.ivAlatIcon);
            ivDotStatus = itemView.findViewById(R.id.ivDotStatus);
            bgAlatIcon = itemView.findViewById(R.id.bgAlatIcon);
        }
    }
}
