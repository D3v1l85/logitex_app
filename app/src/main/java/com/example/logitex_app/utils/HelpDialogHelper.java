package com.example.logitex_app.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.logitex_app.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class HelpDialogHelper {

    public static void mostrarAjuda(Context context, String titol, String[] seccionsTitol, String[] seccionsDescripcio) {
        if (context == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_help_bottom_sheet, null);

        TextView tvTitol = view.findViewById(R.id.tvHelpTitle);
        LinearLayout llContingut = view.findViewById(R.id.llHelpContent);
        Button btnTancar = view.findViewById(R.id.btnHelpClose);

        tvTitol.setText(titol);

        // Infla els elements dinamicament
        llContingut.removeAllViews();
        for (int i = 0; i < seccionsTitol.length; i++) {
            View item = LayoutInflater.from(context).inflate(R.layout.item_help_section, llContingut, false);
            TextView tvSecTitol = item.findViewById(R.id.tvHelpSectionTitle);
            TextView tvSecDesc = item.findViewById(R.id.tvHelpSectionDesc);

            tvSecTitol.setText(seccionsTitol[i]);
            tvSecDesc.setText(seccionsDescripcio[i]);

            llContingut.addView(item);
        }

        btnTancar.setText(com.example.logitex_app.utils.TranslationHelper.get(context, "TANCAR", "CLOSE"));
        btnTancar.setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(view);
        dialog.show();
    }
}
