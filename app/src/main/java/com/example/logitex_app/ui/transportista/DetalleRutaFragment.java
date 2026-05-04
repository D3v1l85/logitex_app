package com.example.logitex_app.ui.transportista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.logitex_app.R;

public class DetalleRutaFragment extends Fragment {

    public DetalleRutaFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalle_ruta, container, false);

        TextView tvAlbara = view.findViewById(R.id.tvDetalleAlbara);
        TextView tvDireccion = view.findViewById(R.id.tvDetalleDireccion);
        Button btnEscaner = view.findViewById(R.id.btnAbrirEscaner);
        Button btnIncidencia = view.findViewById(R.id.btnAbrirIncidencia);

        // 1. Recollir les dades de la ruta seleccionada
        if (getArguments() != null) {
            String albara = getArguments().getString("albara_id", "#ALB-000");
            String direccio = getArguments().getString("albara_dir", "Direcció Desconeguda");

            tvAlbara.setText("Detall: " + albara);
            tvDireccion.setText("Direcció: " + direccio);
        }

        // 2. Lògica del botó Escàner
        btnEscaner.setOnClickListener(v -> {
            Toast.makeText(getContext(), "FUTUR: Obrint càmera per confirmar l'entrega!", Toast.LENGTH_LONG).show();
            // Aquí en el futur obrirem l'escàner QR
        });

        // 3. Lògica del botó Incidència
        btnIncidencia.setOnClickListener(v -> {
            // Canviem al Fragment d'Incidències
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new IncidenciasFragment())
                    .addToBackStack(null) // Permet tornar enrere
                    .commit();
        });

        return view;
    }
}