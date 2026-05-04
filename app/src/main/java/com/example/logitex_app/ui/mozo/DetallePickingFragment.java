package com.example.logitex_app.ui.mozo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.logitex_app.R;

public class DetallePickingFragment extends Fragment {

    public DetallePickingFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalle_picking, container, false);

        TextView tvPalet = view.findViewById(R.id.tvDetallePalet);
        TextView tvUbicacion = view.findViewById(R.id.tvDetalleUbicacion);
        Button btnEscanear = view.findViewById(R.id.btnEscanearPalet);

        // 1. Recollir les dades del palet seleccionat
        if (getArguments() != null) {
            String paletId = getArguments().getString("palet_id", "#00000");
            String ubicacion = getArguments().getString("palet_ub", "Desconeguda");

            tvPalet.setText(paletId);
            tvUbicacion.setText(ubicacion);
        }

        // 2. Lògica del botó Escàner
        btnEscanear.setOnClickListener(v -> {
            // Anem al fragment de l'escàner per confirmar que hem agafat el palet
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new UbicacionFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}