package com.example.skripsi;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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


        // 🔹 INIT VIEW
        rvKendaraan = view.findViewById(R.id.rvKendaraan);
        layoutFilter = view.findViewById(R.id.layoutFilter);
        tvJumlah = view.findViewById(R.id.tvJumlah);
        etSearch = view.findViewById(R.id.etSearch);
        btnTambah = view.findViewById(R.id.btnTambah);

        rvKendaraan.setLayoutManager(new LinearLayoutManager(getContext()));

        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(getContext());

        // 🔥 INIT ADAPTER SEKALI SAJA
        adapter = new KendaraanAdminAdapter(getContext(), filteredList);

        adapter.setOnDataChangedListener(() -> {
            loadKendaraan(); // 🔥 refresh setelah delete
        });

        rvKendaraan.setAdapter(adapter);

        // 🔥 SEARCH
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchData(s.toString());
            }

            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // 🔥 LOAD DATA AWAL
        loadKendaraan();

        btnTambah.setOnClickListener(v -> showPopupTambahKendaraan());

        return view;
    }

    // 🔥 AUTO REFRESH
    @Override
    public void onResume() {
        super.onResume();
        loadKendaraan();
    }

    // 🔥 LOAD DATA DARI API
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

                            // 🔥 COPY KE FILTER LIST
                            filteredList.clear();
                            filteredList.addAll(originalList);

                            // 🔥 UPDATE ADAPTER
                            adapter.updateData(filteredList);

                            // 🔥 JUMLAH DATA
                            tvJumlah.setText(filteredList.size() + " kendaraan terdaftar");

                            // 🔥 SET FILTER
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

    // 🔥 SEARCH + FILTER
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

    // 🔥 SETUP FILTER
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

    // 🔥 DP HELPER
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void showPopupTambahKendaraan() {

        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.popup_tambah_kendaraan_admin);
        ScrollView scrollView = dialog.findViewById(R.id.scrollView);

        AutoCompleteTextView etJenis = dialog.findViewById(R.id.etJenis);
        AutoCompleteTextView etModel = dialog.findViewById(R.id.etModel);

        EditText etJenisBaru = dialog.findViewById(R.id.etJenisBaru);
        EditText etModelBaru = dialog.findViewById(R.id.etModelBaru);
        EditText etBatas = dialog.findViewById(R.id.etBatas);

        boolean[] isFormatting = {false};

        etJenis.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (isFormatting[0]) return;

                isFormatting[0] = true;

                int cursorPos = etJenis.getSelectionStart(); // 🔥 simpan posisi cursor

                String formatted = formatText(s.toString());

                if (!formatted.equals(s.toString())) {

                    etJenis.setText(formatted);

                    int newPos = Math.min(cursorPos, formatted.length());
                    etJenis.setSelection(newPos);
                }

                isFormatting[0] = false;
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        etModel.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (isFormatting[0]) return;

                isFormatting[0] = true;

                int cursorPos = etModel.getSelectionStart();

                String formatted = formatText(s.toString());

                if (!formatted.equals(s.toString())) {

                    etModel.setText(formatted);

                    int newPos = Math.min(cursorPos, formatted.length());
                    etModel.setSelection(newPos);
                }

                isFormatting[0] = false;
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        etJenis.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                etJenis.setText(formatText(etJenis.getText().toString()));
            }
        });

        etModel.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                etModel.setText(formatText(etModel.getText().toString()));
            }
        });

        TextView btnBatal = dialog.findViewById(R.id.btnBatal);
        LinearLayout btnSimpan = dialog.findViewById(R.id.btnSimpan);


        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        // =========================
        // 🔥 AMBIL DATA DARI originalList (TANPA API LAGI)
        // =========================
        Set<String> jenisSet = new HashSet<>();
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
        jenisList.add("Tambah baru...");

        // =========================
        // 🔥 SET DROPDOWN JENIS
        // =========================
        ArrayAdapter<String> adapterJenis =
                new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        jenisList);

        etJenis.setAdapter(adapterJenis);
        etJenis.setOnClickListener(v -> etJenis.showDropDown());

        etJenis.setOnItemClickListener((parent, view, position, id) -> {

            String selectedJenis = jenisList.get(position);

            if (selectedJenis.equals("Tambah baru...")) {
                etJenisBaru.setVisibility(View.VISIBLE);
                etModel.setAdapter(null);
                return;
            }

            etJenisBaru.setVisibility(View.GONE);

            List<String> modelList = mapModel.get(selectedJenis);

            if (modelList == null) modelList = new ArrayList<>();

            modelList.add("Tambah baru...");

            ArrayAdapter<String> adapterModel =
                    new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            modelList);

            etModel.setAdapter(adapterModel);
            etModel.setOnClickListener(v -> etModel.showDropDown());
        });

        // =========================
        // 🔥 MODEL BARU
        // =========================
        etModel.setOnItemClickListener((parent, view, position, id) -> {

            String selected = (String) parent.getItemAtPosition(position);

            if (selected.equals("Tambah baru...")) {
                etModelBaru.setVisibility(View.VISIBLE);
                etModelBaru.requestFocus();
            } else {
                etModelBaru.setVisibility(View.GONE);
            }
        });

        // =========================
        // 🔥 BUTTON
        // =========================
        btnBatal.setOnClickListener(v -> dialog.dismiss());

        btnSimpan.setOnClickListener(v -> {
            TextView tvErrorJenis = dialog.findViewById(R.id.tvErrorJenis);
            TextView tvErrorModel = dialog.findViewById(R.id.tvErrorModel);
            TextView tvErrorBatas = dialog.findViewById(R.id.tvErrorBatas);

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

            etJenis.addTextChangedListener(watcher);
            etModel.addTextChangedListener(watcher);
            etBatas.addTextChangedListener(watcher);

            etJenis.setOnItemClickListener((p,v2,pos,id)-> tvErrorJenis.setVisibility(View.GONE));
            etModel.setOnItemClickListener((p,v2,pos,id)-> tvErrorModel.setVisibility(View.GONE));

            String jenis;
            String model;
            if (etJenisBaru.getVisibility() == View.VISIBLE) {
                jenis = etJenisBaru.getText().toString().trim();
            } else {
                jenis = etJenis.getText().toString().trim();
            }

            if (etModelBaru.getVisibility() == View.VISIBLE) {
                model = etModelBaru.getText().toString().trim();
            } else {
                model = etModel.getText().toString().trim();
            }

            jenis = formatText(jenis);
            model = formatText(model);


            if (etJenisBaru.getVisibility() == View.VISIBLE) {
                jenis = etJenisBaru.getText().toString().trim();
            }

            if (etModelBaru.getVisibility() == View.VISIBLE) {
                model = etModelBaru.getText().toString().trim();
            }

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

            if (!isValid) {

                if (firstError != null) {
                    firstError.requestFocus();

                    final View targetView = firstError;

                    scrollView.post(() ->
                            scrollView.smoothScrollTo(0, targetView.getTop())
                    );
                }

                return;
            }

            double batas = Double.parseDouble(batasStr);

            tambahKendaraanAPI(jenis, model, batas, dialog);
        });
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

                            Toast.makeText(getContext(),
                                    "Berhasil tambah kendaraan",
                                    Toast.LENGTH_SHORT).show();

                            loadKendaraan();

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