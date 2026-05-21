package com.example.logitex_app.ui.home;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.logitex_app.R;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Constructor buit requerit
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView tvNombre = view.findViewById(R.id.tvNombreUsuario);

        // Recuperem el nom que es passa des de MainActivity
        if (getArguments() != null) {
            String nombre = getArguments().getString("arg_nombre");
            tvNombre.setText(nombre);
        }

        return view;
    }
}