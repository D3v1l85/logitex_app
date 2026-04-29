package com.example.logitex_app.ui.mozo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.logitex_app.R;

public class UbicacionFragment extends Fragment {

    public UbicacionFragment() {
        // Constructor buit necessari
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ubicacion, container, false);

        // Enllacem els elements del disseny
        Button btnEscanear = view.findViewById(R.id.btnEscanear);
        Button btnConfirmarManual = view.findViewById(R.id.btnConfirmarManual);
        EditText etCodigoManual = view.findViewById(R.id.etCodigoManual);

        // Botó per obrir la càmera (Això ho farem real més endavant)
        btnEscanear.setOnClickListener(v -> {
            Toast.makeText(getContext(), "FUTUR: Aquí demanarem permisos i obrirem la càmera!", Toast.LENGTH_LONG).show();
        });

        // Botó per introduir codi a mà (Molt útil si falla el QR)
        btnConfirmarManual.setOnClickListener(v -> {
            String codi = etCodigoManual.getText().toString().trim();

            if (!codi.isEmpty()) {
                Toast.makeText(getContext(), "Codi validat correctament: " + codi, Toast.LENGTH_SHORT).show();
                etCodigoManual.setText(""); // Netegem el camp després de confirmar
            } else {
                Toast.makeText(getContext(), "Si us plau, introdueix un codi vàlid", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}