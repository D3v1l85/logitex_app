package com.example.logitex_app.ui.perfil;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.logitex_app.R;
import com.example.logitex_app.api.ApiService;
import com.example.logitex_app.api.RetrofitClient;
import com.example.logitex_app.models.Usuari;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private TextView tvProfileName, tvProfileRole, tvProfilePhone, tvProfileEmail;
    private ProgressBar pbLoading;

    public PerfilFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // Referencies als camps de la interficie
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileRole = view.findViewById(R.id.tvProfileRole);
        tvProfilePhone = view.findViewById(R.id.tvProfilePhone);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        pbLoading = view.findViewById(R.id.pbProfileLoading);

        // Traduccions dinamiques de les etiquetes
        TextView tvHeader = view.findViewById(R.id.tvProfileHeader);
        TextView tvLabelName = view.findViewById(R.id.tvLabelName);
        TextView tvLabelRole = view.findViewById(R.id.tvLabelRole);
        TextView tvLabelPhone = view.findViewById(R.id.tvLabelPhone);
        TextView tvLabelEmail = view.findViewById(R.id.tvLabelEmail);

        if (tvHeader != null) tvHeader.setText(com.example.logitex_app.utils.TranslationHelper.profileHeader(getContext()));
        if (tvLabelName != null) tvLabelName.setText(com.example.logitex_app.utils.TranslationHelper.profileLabelName(getContext()));
        if (tvLabelRole != null) tvLabelRole.setText(com.example.logitex_app.utils.TranslationHelper.profileLabelRole(getContext()));
        if (tvLabelPhone != null) tvLabelPhone.setText(com.example.logitex_app.utils.TranslationHelper.profileLabelPhone(getContext()));
        if (tvLabelEmail != null) tvLabelEmail.setText(com.example.logitex_app.utils.TranslationHelper.profileLabelEmail(getContext()));

        // Omplir dades locals preliminars de les preferencies per si el servidor triga
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String localNom = prefs.getString("USER_NOM", "Usuari");
        String localRol = prefs.getString("ROL_USUARIO", "Desconegut");
        int userId = prefs.getInt("USER_ID", 0);
        String token = prefs.getString("TOKEN_AUTH", "");

        tvProfileName.setText(localNom);
        tvProfileRole.setText(localRol.toUpperCase());
        tvProfilePhone.setText("...");
        tvProfileEmail.setText("...");

        if (userId > 0 && !token.isEmpty()) {
            cargarPerfilDelServidor(token, userId);
        }

        return view;
    }

    private void cargarPerfilDelServidor(String token, int userId) {
        pbLoading.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Usuari> call = apiService.getUsuariById("Bearer " + token, userId);

        call.enqueue(new Callback<Usuari>() {
            @Override
            public void onResponse(@NonNull Call<Usuari> call, @NonNull Response<Usuari> response) {
                if (getContext() == null) return;
                pbLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    Usuari user = response.body();

                    // Omplim el nom real l'email i el telefon
                    tvProfileName.setText(user.getNom() != null ? user.getNom() : "N/A");
                    
                    String phone = (user.getTelefon() != null && !user.getTelefon().isEmpty()) ? user.getTelefon() : com.example.logitex_app.utils.TranslationHelper.profileNotProvided(getContext());
                    String email = (user.getEmail() != null && !user.getEmail().isEmpty()) ? user.getEmail() : com.example.logitex_app.utils.TranslationHelper.profileNotProvided(getContext());

                    tvProfilePhone.setText(phone);
                    tvProfileEmail.setText(email);
                } else {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.profileErrorFetching(getContext()), Toast.LENGTH_SHORT).show();
                    Log.e("API_PERFIL", "Error fetching user profile. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Usuari> call, @NonNull Throwable t) {
                if (getContext() == null) return;
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.profileErrorFetching(getContext()), Toast.LENGTH_SHORT).show();
                Log.e("API_PERFIL", "Red fallback: " + t.getMessage());
            }
        });
    }
}
