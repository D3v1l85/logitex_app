package com.example.logitex_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.logitex_app.ui.auth.LoginActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Temporizador de 2 segundos (2000 ms)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Pasados los 2 segundos, abrimos la pantalla de Login
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);

            // Cerramos esta pantalla para que el usuario no pueda volver a ella pulsando "Atrás"
            finish();
        }, 2000);
    }
}
