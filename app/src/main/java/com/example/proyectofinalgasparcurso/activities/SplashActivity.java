package com.example.proyectofinalgasparcurso.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

//Android nos muestra una advertencia porque el android moderno ya incorpora
// un sistema de splash screen pero en este proyecto uso una splashActivity sencilla
// para comprobar si el usuario tiene la sesion iniciada
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtenemos los datos guardados del usuario
        SharedPreferences prefs = getSharedPreferences("usuario", MODE_PRIVATE);

        // Intentamos obtener el id del usuario guardado
        // si no existe el valor por defecto sera -1
        int id = prefs.getInt("id", -1);

        // Si no hay usuario guardado abrimos el login
        if (id == -1) {
            startActivity(new Intent(this, LoginActivity.class));
        }
        // Si existe un usuario abrimos el menu principal
        else {
            startActivity(new Intent(this, MainActivity.class));
        }

        finish();
    }
}