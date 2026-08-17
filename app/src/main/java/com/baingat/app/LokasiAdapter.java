package com.baingat.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LokasiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<WilayahModel> wilayahList;
    private List<Object> flatList;

    public LokasiAdapter(List<WilayahModel> wilayahList) {
        this.wilayahList = wilayahList;
        this.flatList = new ArrayList<>();
        buildFlatList();
    }

    private void buildFlatList() {
        flatList.clear();
        for (WilayahModel wilayah : wilayahList) {
            flatList.add(wilayah);
            if (wilayah.isExpanded() && wilayah.getListJalan() != null) {
                flatList.addAll(wilayah.getListJalan());
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (flatList.get(position) instanceof WilayahModel) ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wilayah, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lokasi_checkbox, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            WilayahModel wilayah = (WilayahModel) flatList.get(position);
            ((HeaderViewHolder) holder).bind(wilayah);
        } else if (holder instanceof ItemViewHolder) {
            LokasiModel lokasi = (LokasiModel) flatList.get(position);
            ((ItemViewHolder) holder).bind(lokasi);
        }
    }

    @Override
    public int getItemCount() {
        return flatList.size();
    }

    public List<LokasiModel> getSelectedLocations() {
        List<LokasiModel> selected = new ArrayList<>();
        for (WilayahModel wilayah : wilayahList) {
            if (wilayah.getListJalan() != null) {
                for (LokasiModel lokasi : wilayah.getListJalan()) {
                    if (lokasi.isSelected()) {
                        selected.add(lokasi);
                    }
                }
            }
        }
        return selected;
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaWilayah;
        ImageView ivExpand;
        CheckBox cbPilihSemua;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaWilayah = itemView.findViewById(R.id.tvNamaWilayah);
            ivExpand = itemView.findViewById(R.id.ivExpandIndicator);
            cbPilihSemua = itemView.findViewById(R.id.cbPilihSemuaWilayah);

            // Expand/collapse logic when clicking the item view
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    WilayahModel wilayah = (WilayahModel) flatList.get(pos);
                    wilayah.setExpanded(!wilayah.isExpanded());
                    buildFlatList();
                    notifyDataSetChanged();
                }
            });

            // Select all logic
            cbPilihSemua.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    WilayahModel wilayah = (WilayahModel) flatList.get(pos);
                    boolean isChecked = cbPilihSemua.isChecked();
                    if (wilayah.getListJalan() != null) {
                        for (LokasiModel lokasi : wilayah.getListJalan()) {
                            lokasi.setSelected(isChecked);
                        }
                    }
                    itemView.post(() -> notifyDataSetChanged());
                }
            });
        }

        void bind(WilayahModel wilayah) {
            tvNamaWilayah.setText(wilayah.getNamaKecamatan());
            ivExpand.setRotation(wilayah.isExpanded() ? 180f : 0f);
            cbPilihSemua.setChecked(wilayah.isAllLocationsChecked());
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbLokasi;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cbLokasi = itemView.findViewById(R.id.cbLokasi);

            cbLokasi.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Object item = flatList.get(pos);
                    if (item instanceof LokasiModel) {
                        ((LokasiModel) item).setSelected(cbLokasi.isChecked());
                        itemView.post(() -> notifyDataSetChanged());
                    }
                }
            });
        }

        void bind(LokasiModel lokasi) {
            cbLokasi.setText(lokasi.getNamaJalan());
            cbLokasi.setChecked(lokasi.isSelected());
        }
    }
}
