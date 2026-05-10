package com.example.skripsi;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KendaraanAdminFragment extends Fragment {

    private RecyclerView rvKendaraan;
    private KendaraanAdminAdapter adapter;

    private LinearLayout layoutFilter, btnTambah;
    private TextView tvJumlah;
    private EditText etSearch;

    private ApiService apiService;
    private SessionManager sessionManager;

    private List<KendaraanAdminModel> originalList = new ArrayList<>();
    private List<KendaraanAdminModel> filteredList = new ArrayList<>();

    private String selectedJenis = "Semua";
    boolean[] isFormatting = {false};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_kendaraan_admin, container, false);

        rvKendaraan = view.findViewById(R.id.rvKendaraan);
        layoutFilter = view.findViewById(R.id.layoutFilter);
        tvJumlah = view.findViewById(R.id.tvJumlah);
        etSearch = view.findViewById(R.id.etSearch);
        btnTambah = view.findViewById(R.id.btnTambah);

        rvKendaraan.setLayoutManager(new LinearLayoutManager(getContext()));

        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(getContext());
        adapter = new KendaraanAdminAdapter(getContext(), filteredList);

        adapter.setOnDataChangedListener(() -> {
            loadKendaraan();
        });

        rvKendaraan.setAdapter(adapter);

        adapter.setOnEditClickListener(data -> {
            showPopupEditKendaraan(data);
        });

        // 🔥 SEARCH
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchData(s.toString());
            }

            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        loadKendaraan();

        btnTambah.setOnClickListener(v -> showPopupTambahKendaraan());

        return view;
    }

    // AUTO REFRESH
    @Override
    public void onResume() {
        super.onResume();
        loadKendaraan();
    }

    private void loadKendaraan() {

        String token = "Bearer " + sessionManager.getToken();

        apiService.getKendaraanAdmin(token)
                .enqueue(new Callback<KendaraanAdminResponseModel>() {

                    @Override
                    public void onResponse(Call<KendaraanAdminResponseModel> call,
                                           Response<KendaraanAdminResponseModel> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            List<KendaraanAdminResponseModel.Data> dataApi =
                                    response.body().getData();

                            // 🔥 CLEAR DATA
                            originalList.clear();

                            for (KendaraanAdminResponseModel.Data item : dataApi) {
                                originalList.add(new KendaraanAdminModel(
                                        item.getIdKendaraan(),
                                        item.getNamaKendaraan(),
                                        item.getJenisKendaraan(),
                                        item.getBatasKendaraan()
                                ));
                            }

                            filteredList.clear();
                            filteredList.addAll(originalList);

                            adapter.updateData(filteredList);
                            tvJumlah.setText(filteredList.size() + " kendaraan terdaftar");
                            setupFilter();

                        } else {
                            Toast.makeText(getContext(),
                                    "Gagal ambil data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<KendaraanAdminResponseModel> call, Throwable t) {
                        Toast.makeText(getContext(),
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void searchData(String keyword) {

        filteredList.clear();

        for (KendaraanAdminModel item : originalList) {

            boolean cocokSearch =
                    (item.getNamaKendaraan() != null &&
                            item.getNamaKendaraan().toLowerCase().contains(keyword.toLowerCase()))
                            ||
                            (item.getJenisKendaraan() != null &&
                                    item.getJenisKendaraan().toLowerCase().contains(keyword.toLowerCase()));

            boolean cocokFilter =
                    selectedJenis.equals("Semua") ||
                            (item.getJenisKendaraan() != null &&
                                    item.getJenisKendaraan().equalsIgnoreCase(selectedJenis));

            if (cocokSearch && cocokFilter) {
                filteredList.add(item);
            }
        }

        adapter.updateData(filteredList);

        tvJumlah.setText(filteredList.size() + " kendaraan terdaftar");
    }

    private void setupFilter() {

        layoutFilter.removeAllViews();

        Set<String> jenisSet = new HashSet<>();

        for (KendaraanAdminModel item : originalList) {
            if (item.getJenisKendaraan() != null) {
                jenisSet.add(item.getJenisKendaraan());
            }
        }

        addChip("Semua", true);

        for (String jenis : jenisSet) {
            addChip(jenis, false);
        }
    }

    // 🔥 CHIP
    private void addChip(String text, boolean isActive) {

        TextView chip = new TextView(getContext());

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dpToPx(32));

        params.setMarginEnd(dpToPx(8));
        chip.setLayoutParams(params);

        chip.setText(text);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dpToPx(12), 0, dpToPx(12), 0);

        if (isActive) {
            chip.setBackgroundResource(R.drawable.bg_chipactive_textview);
            chip.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_textview);
            chip.setTextColor(getResources().getColor(android.R.color.black));
        }

        chip.setOnClickListener(v -> {
            selectedJenis = text;
            searchData(etSearch.getText().toString());
            setActiveChip(chip);
        });

        layoutFilter.addView(chip);
    }

    // 🔥 ACTIVE CHIP
    private void setActiveChip(TextView selected) {

        for (int i = 0; i < layoutFilter.getChildCount(); i++) {

            TextView chip = (TextView) layoutFilter.getChildAt(i);

            chip.setBackgroundResource(R.drawable.bg_chip_textview);
            chip.setTextColor(getResources().getColor(android.R.color.black));
        }

        selected.setBackgroundResource(R.drawable.bg_chipactive_textview);
        selected.setTextColor(getResources().getColor(android.R.color.white));
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void showPopupTambahKendaraan() {

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.popup_tambah_kendaraan_admin);
        dialog.show();

        ScrollView scrollView = dialog.findViewById(R.id.scrollView);

        AutoCompleteTextView etJenis = dialog.findViewById(R.id.etJenis);
        AutoCompleteTextView etModel = dialog.findViewById(R.id.etModel);
        EditText etBatas = dialog.findViewById(R.id.etBatas);

        TextView btnBatal = dialog.findViewById(R.id.btnBatal);
        LinearLayout btnSimpan = dialog.findViewById(R.id.btnSimpan);

        boolean[] isFormatting = {false};
        boolean[] isFromDropdown = {false};
        etModel.setAdapter(null);

        // =========================
        // 🔥 AMBIL DATA
        // =========================
        Set<String> jenisSet = new LinkedHashSet<>();
        Map<String, List<String>> mapModel = new HashMap<>();

        for (KendaraanAdminModel item : originalList) {

            String jenis = item.getJenisKendaraan();
            String model = item.getNamaKendaraan();

            if (jenis != null) {
                jenisSet.add(jenis);
            }

            if (!mapModel.containsKey(jenis)) {
                mapModel.put(jenis, new ArrayList<>());
            }

            mapModel.get(jenis).add(model);
        }

        List<String> jenisList = new ArrayList<>(jenisSet);
        ArrayAdapter<String> adapterJenis =
                new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        jenisList);

        etJenis.setAdapter(adapterJenis);
        etJenis.setOnClickListener(v -> etJenis.showDropDown());

        etJenis.setOnItemClickListener((parent, view, position, id) -> {

            String selectedJenis = (String) parent.getItemAtPosition(position);

            isFromDropdown[0] = true;
            etJenis.setText(selectedJenis, false);
            etJenis.setSelection(etJenis.getText().length());

            List<String> modelList = mapModel.get(selectedJenis);

            if (modelList == null) modelList = new ArrayList<>();

            ArrayAdapter<String> adapterModel =
                    new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            modelList);

        });

        etJenis.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (isFormatting[0]) return;

                if (isFromDropdown[0]) {
                    isFromDropdown[0] = false;
                    return; //
                }

                isFormatting[0] = true;

                int cursor = etJenis.getSelectionStart();

                String formatted = formatText(s.toString());

                if (!formatted.equals(s.toString())) {
                    etJenis.setText(formatted);
                    etJenis.setSelection(Math.min(cursor, formatted.length()));
                }

                isFormatting[0] = false;
                isFromDropdown[0] = false;
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        etModel.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (isFormatting[0]) return;

                if (isFromDropdown[0]) {
                    return;
                }

                isFormatting[0] = true;

                int cursor = etModel.getSelectionStart();

                String formatted = formatText(s.toString());

                if (!formatted.equals(s.toString())) {
                    etModel.setText(formatted);
                    etModel.setSelection(Math.min(cursor, formatted.length()));
                }

                isFormatting[0] = false;
                isFromDropdown[0] = false;
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        btnSimpan.setOnClickListener(v -> {

            TextView tvErrorJenis = dialog.findViewById(R.id.tvErrorJenis);
            TextView tvErrorModel = dialog.findViewById(R.id.tvErrorModel);
            TextView tvErrorBatas = dialog.findViewById(R.id.tvErrorBatas);

            String jenis = etJenis.getText().toString().trim();
            String model = etModel.getText().toString().trim();

            jenis = formatText(jenis);
            model = formatText(model);

            String batasStr = etBatas.getText().toString().trim();

            tvErrorJenis.setVisibility(View.GONE);
            tvErrorModel.setVisibility(View.GONE);
            tvErrorBatas.setVisibility(View.GONE);

            boolean isValid = true;
            View firstError = null;

            if (jenis.isEmpty()) {
                tvErrorJenis.setVisibility(View.VISIBLE);
                firstError = etJenis;
                isValid = false;
            }

            if (model.isEmpty()) {
                tvErrorModel.setVisibility(View.VISIBLE);
                if (firstError == null) firstError = etModel;
                isValid = false;
            }

            if (batasStr.isEmpty()) {
                tvErrorBatas.setVisibility(View.VISIBLE);
                if (firstError == null) firstError = etBatas;
                isValid = false;
            }

            boolean modelSudahAda = false;

            for (KendaraanAdminModel item : originalList) {

                if (item.getJenisKendaraan() != null &&
                        item.getNamaKendaraan() != null &&
                        item.getJenisKendaraan().equalsIgnoreCase(jenis) &&
                        item.getNamaKendaraan().equalsIgnoreCase(model)) {

                    modelSudahAda = true;
                    break;
                }
            }

            if (modelSudahAda) {
                tvErrorModel.setText("Model sudah ada");
                tvErrorModel.setVisibility(View.VISIBLE);

                if (firstError == null) firstError = etModel;

                isValid = false;
            }

            if (!isValid) {
                if (firstError != null) {
                    firstError.requestFocus();
                    final View target = firstError;
                    scrollView.post(() ->
                            scrollView.smoothScrollTo(0, target.getTop())
                    );
                }
                return;
            }

            double batas = Double.parseDouble(batasStr);

            tambahKendaraanAPI(jenis, model, batas, dialog);
        });

        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );
        dialog.show();
    }

    private void tambahKendaraanAPI(String jenis, String model, double batas, Dialog dialog) {

        String token = "Bearer " + sessionManager.getToken();

        TambahKendaraanAdminRequest request =
                new TambahKendaraanAdminRequest(jenis, model, batas);

        apiService.tambahKendaraanAdmin(token, request)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        if (response.isSuccessful()) {

                            dialog.dismiss();

                            Dialog dialogSukses = new Dialog(requireContext());
                            dialogSukses.setContentView(R.layout.popup_berhasil_tambah);

                            if (dialogSukses.getWindow() != null) {
                                dialogSukses.getWindow().setLayout(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                );
                                dialogSukses.getWindow().setDimAmount(0.8f);
                                dialogSukses.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            }

                            LinearLayout lanjutanBerhasil = dialogSukses.findViewById(R.id.lanjutanBerhasil);

                            if (lanjutanBerhasil != null) {
                                lanjutanBerhasil.setOnClickListener(v -> {
                                    dialogSukses.dismiss(); // Menutup dialog sukses
                                    loadKendaraan(); // Refresh list setelah user klik lanjut
                                });
                            }

                            dialogSukses.show();

                        } else {
                            Toast.makeText(getContext(),
                                    "Gagal tambah",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getContext(),
                                t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateKendaraanAPI(
            String id,
            String jenis,
            String model,
            double batas,
            Dialog dialog,
            View loadingOverlay,
            TextView btnText
    ) {

        String token = "Bearer " + sessionManager.getToken();

        EditKendaraanAdminRequestModel request =
                new EditKendaraanAdminRequestModel(jenis, model, batas);

        // 🔥 SHOW LOADING
        showLoading(loadingOverlay, btnText);

        apiService.updateKendaraanAdmin(token, id, request)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        hideLoading(loadingOverlay, btnText);

                        if (response.isSuccessful()) {

                            dialog.dismiss();

                            Dialog dialogSukses = new Dialog(requireContext());
                            dialogSukses.setContentView(R.layout.popup_berhasil_tambah);

                            if (dialogSukses.getWindow() != null) {
                                dialogSukses.getWindow().setLayout(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                );
                                dialogSukses.getWindow().setDimAmount(0.8f);
                                dialogSukses.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            }

                            LinearLayout lanjutanBerhasil = dialogSukses.findViewById(R.id.lanjutanBerhasil);

                            if (lanjutanBerhasil != null) {
                                lanjutanBerhasil.setOnClickListener(v -> {
                                    dialogSukses.dismiss(); // Menutup dialog sukses
                                    loadKendaraan(); // Refresh list setelah user klik lanjut
                                });
                            }

                            dialogSukses.show();

                        } else {

                            try {
                                String error = response.errorBody().string();
                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(getContext(), "Gagal update", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                        hideLoading(loadingOverlay, btnText);

                        Toast.makeText(getContext(),
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(View overlay, TextView btnText) {
        overlay.setAlpha(0f);
        overlay.setVisibility(View.VISIBLE);
        overlay.animate().alpha(1f).setDuration(200).start();

        btnText.setText("Loading...");
    }

    private void hideLoading(View overlay, TextView btnText) {
        overlay.animate().alpha(0f).setDuration(200).withEndAction(() -> {
            overlay.setVisibility(View.GONE);
        }).start();

        btnText.setText("Simpan");
    }

    private void showPopupEditKendaraan(KendaraanAdminModel data) {

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.popup_tambah_kendaraan_admin);
        dialog.show();

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);

        AutoCompleteTextView etJenis = dialog.findViewById(R.id.etJenis);
        AutoCompleteTextView etModel = dialog.findViewById(R.id.etModel);
        EditText etBatas = dialog.findViewById(R.id.etBatas);

        TextView tvErrorJenis = dialog.findViewById(R.id.tvErrorJenis);
        TextView tvErrorModel = dialog.findViewById(R.id.tvErrorModel);
        TextView tvErrorBatas = dialog.findViewById(R.id.tvErrorBatas);

        TextView btnBatal = dialog.findViewById(R.id.btnBatal);
        LinearLayout btnSimpan = dialog.findViewById(R.id.btnSimpan);
        TextView tvBtnSimpan = dialog.findViewById(R.id.tvBtnSimpan);

        View loadingOverlay = dialog.findViewById(R.id.loadingOverlay);
        tvTitle.setText("Edit Kendaraan");

        etJenis.setText(data.getJenisKendaraan());
        etModel.setText(data.getNamaKendaraan());
        etBatas.setText(String.valueOf(data.getBatasKendaraan()));
        etJenis.setEnabled(false);
        etJenis.setFocusable(false);
        etJenis.setClickable(false);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvErrorJenis.setVisibility(View.GONE);
                tvErrorModel.setVisibility(View.GONE);
                tvErrorBatas.setVisibility(View.GONE);
            }

            @Override public void afterTextChanged(Editable s) {}
        };

        etModel.addTextChangedListener(watcher);
        etBatas.addTextChangedListener(watcher);
        btnBatal.setOnClickListener(v -> dialog.dismiss());
        btnSimpan.setOnClickListener(v -> {

            String jenis = etJenis.getText().toString().trim();
            String model = etModel.getText().toString().trim();
            String batasStr = etBatas.getText().toString().trim();

            boolean isValid = true;

            if (jenis.isEmpty()) {
                tvErrorJenis.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (model.isEmpty()) {
                tvErrorModel.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (batasStr.isEmpty()) {
                tvErrorBatas.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (!isValid) return;

            double batas = Double.parseDouble(batasStr);

            showLoading(loadingOverlay, tvBtnSimpan);

            updateKendaraanAPI(
                    data.getIdKendaraan(),
                    jenis,
                    model,
                    batas,
                    dialog,
                    loadingOverlay,
                    tvBtnSimpan
            );
        });
    }

    private String formatText(String input) {

        if (input == null || input.isEmpty()) return input;

        StringBuilder result = new StringBuilder();

        String[] parts = input.split(" ", -1);

        for (int i = 0; i < parts.length; i++) {

            String word = parts[i];

            if (word.isEmpty()) {
                result.append(" ");
                continue;
            }

            String onlyLetters = word.replaceAll("[^a-zA-Z]", "");

            if (onlyLetters.length() <= 3) {
                result.append(word.toUpperCase());
            } else {
                result.append(
                        Character.toUpperCase(word.charAt(0)) +
                                word.substring(1).toLowerCase()
                );
            }

            if (i < parts.length - 1) {
                result.append(" ");
            }
        }

        return result.toString();
    }
}