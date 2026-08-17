package com.baingat.app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StatusJalanAdapter extends RecyclerView.Adapter<StatusJalanAdapter.ViewHolder> {

    private List<StatusUtamaResponseModel.Data> listStatus;
    private Context context;

    public StatusJalanAdapter(Context context, List<StatusUtamaResponseModel.Data> listStatus) {
        this.context = context;
        this.listStatus = listStatus;
    }

    public void updateData(List<StatusUtamaResponseModel.Data> newList) {
        this.listStatus = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status_jalan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StatusUtamaResponseModel.Data data = listStatus.get(position);

        holder.tvNamaJalan.setText(data.getNamaLokasi() != null ? data.getNamaLokasi() : "Nama Jalan");
        holder.tvTinggi.setText(data.getTinggi() + " cm");
        
        String lastUpdate = data.getLastUpdate() != null ? data.getLastUpdate() : "--:--";
        holder.tvWaktuUpdate.setText(lastUpdate + " WITA");

        double kecepatan = data.getKecepatan();
        if (kecepatan > 0) {
            holder.iconTren.setImageResource(R.drawable.up_arrow);
            holder.tvTren.setText("Naik");
            holder.tvTren.setTextColor(android.graphics.Color.parseColor("#10B981"));
            holder.tvTrenDesc.setText("Air sedang surut");
        } else if (kecepatan < 0) {
            holder.iconTren.setImageResource(R.drawable.down_arrow);
            holder.tvTren.setText("Turun");
            holder.tvTren.setTextColor(android.graphics.Color.parseColor("#EF4444"));
            holder.tvTrenDesc.setText("Air sedang naik");
        } else {
            holder.iconTren.setImageResource(R.drawable.arrow_stabil);
            holder.tvTren.setText("Stabil");
            holder.tvTren.setTextColor(android.graphics.Color.parseColor("#7C3AED"));
            holder.tvTrenDesc.setText("Tidak ada perubahan signifikan");
        }

        String risiko = data.getRisiko() != null ? data.getRisiko() : "";
        holder.tvBadgeStatus.setText(risiko);
        String risikoLower = risiko.toLowerCase();

        if (risikoLower.contains("aman")) {
            holder.tvBadgeStatus.setTextColor(ContextCompat.getColor(context, R.color.hijauaman));
            holder.tvTinggi.setTextColor(ContextCompat.getColor(context, R.color.hijauaman));
            holder.tvTinggiDesc.setText("Batas aman");
            holder.bulatStatus.setImageResource(R.drawable.bulathijaukecil);
            holder.iconBadgeStatus.setImageResource(R.drawable.bulathijaukecil);
            holder.bgBadgeStatus.setBackgroundResource(R.drawable.bg_badge_aman);
        } else if (risikoLower.contains("resiko rendah") || risikoLower.contains("waspada")) {
            holder.tvBadgeStatus.setTextColor(ContextCompat.getColor(context, R.color.kuningrendah));
            holder.tvTinggi.setTextColor(ContextCompat.getColor(context, R.color.kuningrendah));
            holder.tvTinggiDesc.setText("Perlu diwaspadai");
            holder.bulatStatus.setImageResource(R.drawable.bulatkuningkecil);
            holder.iconBadgeStatus.setImageResource(R.drawable.bulatkuningkecil);
            holder.bgBadgeStatus.setBackgroundResource(R.drawable.bg_badge_waspada); // Assuming this exists, fallback later
        } else if (risikoLower.contains("resiko sedang")) {
            holder.tvBadgeStatus.setTextColor(ContextCompat.getColor(context, R.color.orensedang));
            holder.tvTinggi.setTextColor(ContextCompat.getColor(context, R.color.orensedang));
            holder.tvTinggiDesc.setText("Kondisi siaga");
            holder.bulatStatus.setImageResource(R.drawable.bulatorenkecil);
            holder.iconBadgeStatus.setImageResource(R.drawable.bulatorenkecil);
            holder.bgBadgeStatus.setBackgroundResource(R.drawable.bg_badge_waspada); // Fallback
        } else if (risikoLower.contains("resiko tinggi") || risikoLower.contains("bahaya")) {
            holder.tvBadgeStatus.setTextColor(ContextCompat.getColor(context, R.color.merahtinggi));
            holder.tvTinggi.setTextColor(ContextCompat.getColor(context, R.color.merahtinggi));
            holder.tvTinggiDesc.setText("Bahaya banjir!");
            holder.bulatStatus.setImageResource(R.drawable.bulatmerahkecil);
            holder.iconBadgeStatus.setImageResource(R.drawable.bulatmerahkecil);
            holder.bgBadgeStatus.setBackgroundResource(R.drawable.bg_badge_bahaya); // Assuming this exists
        } else {
            holder.tvBadgeStatus.setTextColor(android.graphics.Color.parseColor("#6B7280")); // Gray text
            holder.tvTinggi.setTextColor(android.graphics.Color.parseColor("#2563EB")); // Default blue
            holder.tvTinggiDesc.setText("Menunggu Data");
            holder.bulatStatus.setImageResource(R.drawable.bulatbirukecil);
            holder.iconBadgeStatus.setImageResource(R.drawable.bulatbirukecil);
            holder.bgBadgeStatus.setBackgroundResource(R.drawable.bg_soft_blue_rounded); // Or just gray bg
        }

        holder.layoutJalanItem.setOnClickListener(v -> {
            // Kita bisa arahkan ke DetailStatusActivity 
            // Namun karena DetailActivity mungkin hardcoded Datang/Pulang, 
            // Kita arahkan ke salah satu dulu, atau jika ada DetailStatusActivity dinamis, kirimkan id_lokasi
            // Sebagai placeholder:
            Intent intent = new Intent(context, DetailStatusActivity.class);
            intent.putExtra("ID_LOKASI", data.getIdLokasi());
            intent.putExtra("NAMA_LOKASI", data.getNamaLokasi());
            intent.putExtra("LATITUDE", data.getLatitude());
            intent.putExtra("LONGITUDE", data.getLongitude());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listStatus == null ? 0 : listStatus.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaJalan, tvBadgeStatus, tvTinggi, tvTinggiDesc, tvTren, tvTrenDesc, tvWaktuUpdate;
        ImageView bulatStatus, iconBadgeStatus, iconTren;
        LinearLayout layoutJalanItem, bgBadgeStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutJalanItem = itemView.findViewById(R.id.layoutJalanItem);
            tvNamaJalan = itemView.findViewById(R.id.tvNamaJalan);
            tvBadgeStatus = itemView.findViewById(R.id.tvBadgeStatus);
            bulatStatus = itemView.findViewById(R.id.bulatStatus);
            iconBadgeStatus = itemView.findViewById(R.id.iconBadgeStatus);
            bgBadgeStatus = itemView.findViewById(R.id.bgBadgeStatus);
            tvTinggi = itemView.findViewById(R.id.tvTinggi);
            tvTinggiDesc = itemView.findViewById(R.id.tvTinggiDesc);
            tvTren = itemView.findViewById(R.id.tvTren);
            iconTren = itemView.findViewById(R.id.iconTren);
            tvTrenDesc = itemView.findViewById(R.id.tvTrenDesc);
            tvWaktuUpdate = itemView.findViewById(R.id.tvWaktuUpdate);
        }
    }
}
