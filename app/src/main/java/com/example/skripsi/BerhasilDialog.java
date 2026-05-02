package com.example.skripsi;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class BerhasilDialog extends DialogFragment {
    private Runnable onClick;

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.popup_berhasil);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setDimAmount(0.8f);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        LinearLayout lanjutanBerhasil = dialog.findViewById(R.id.lanjutanBerhasil);

        lanjutanBerhasil.setOnClickListener(v -> {
            dialog.dismiss();
            if (onClick != null) onClick.run();
        });

        return dialog;
    }
}
