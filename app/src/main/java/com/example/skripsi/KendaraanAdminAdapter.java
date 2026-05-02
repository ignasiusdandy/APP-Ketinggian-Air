package com.example.skripsi;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KendaraanAdminAdapter extends RecyclerView.Adapter<KendaraanAdminAdapter.ViewHolder> {

    private List<KendaraanAdminModel> list;
    private Context context;

    public KendaraanAdminAdapter(Context context, List<KendaraanAdminModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_kendaraan_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        KendaraanAdminModel data = list.get(position);

        // 🔹 NAMA KENDARAAN
        holder.tvNama.setText(data.getNamaKendaraan());

        // 🔹 JENIS (opsional kalau mau ditampilkan)
        holder.tvJenis.setText(data.getJenisKendaraan());
        holder.tvBatas.setText("Batas: " + data.getBatasKendaraan() + " cm");

        // 🔹 ICON INISIAL
        if (data.getNamaKendaraan() != null && !data.getNamaKendaraan().isEmpty()) {
            holder.tvIcon.setText(data.getNamaKendaraan().substring(0,1).toUpperCase());
        } else {
            holder.tvIcon.setText("?");
        }

        // 🔥 CLICK EDIT
        holder.btnEdit.setOnClickListener(v -> {
            // nanti sambung ke edit
        });

        // 🔥 CLICK DELETE
        holder.btnDelete.setOnClickListener(v -> {

            int pos = holder.getAdapterPosition();
            KendaraanAdminModel data2 = list.get(pos);

            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.popup_konfirmasi_hapus);

            LinearLayout btnHapus = dialog.findViewById(R.id.btnLanjutanHapus);
            LinearLayout btnBatal = dialog.findViewById(R.id.btn_batal_hapus);

            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();

            // 🔥 BATAL
            btnBatal.setOnClickListener(v1 -> dialog.dismiss());

            // 🔥 KONFIRM HAPUS
            btnHapus.setOnClickListener(v1 -> {

                SessionManager sessionManager = new SessionManager(context);
                ApiService apiService = ApiClient.getClient().create(ApiService.class);

                String token = "Bearer " + sessionManager.getToken();

                apiService.deleteKendaraan(token, data2.getIdKendaraan())
                        .enqueue(new Callback<ResponseBody>() {

                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                dialog.dismiss();

                                if (response.isSuccessful()) {

                                    showPopupBerhasilHapus(context, () -> {
                                        if (listener != null) {
                                            listener.onDataChanged();
                                        }
                                    });
                                    list.remove(pos);
                                    notifyItemRemoved(pos);

                                } else {

                                    try {
                                        String errorBody = response.errorBody().string();
                                        org.json.JSONObject json = new org.json.JSONObject(errorBody);
                                        String message = json.getString("message");

                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

                                    } catch (Exception e) {
                                        Toast.makeText(context, "Gagal hapus", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                dialog.dismiss();
                                Toast.makeText(context,
                                        "Error: " + t.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

            });

        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    // 🔥 VIEW HOLDER
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvNama, tvJenis, tvIcon, tvBatas;
        LinearLayout btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNama = itemView.findViewById(R.id.tvNama);
            tvJenis = itemView.findViewById(R.id.tvJenis);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvBatas = itemView.findViewById(R.id.tvBatas);

            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

        }
    }

    // 🔥 OPTIONAL: update data tanpa recreate adapter
    public void updateData(List<KendaraanAdminModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    private OnDataChangedListener listener;

    public void setOnDataChangedListener(OnDataChangedListener listener) {
        this.listener = listener;
    }


    private void showPopupBerhasilHapus(Context context, Runnable onClose) {

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_berhasil_hapus);

        LinearLayout btnLanjut = dialog.findViewById(R.id.lanjutanBerhasil);

        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        btnLanjut.setOnClickListener(v -> {
            dialog.dismiss();

            if (onClose != null) {
                onClose.run();
            }
        });
    }


}