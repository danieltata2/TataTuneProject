package com.example.proyectofinalgasparcurso.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinalgasparcurso.R;
import com.example.proyectofinalgasparcurso.bbdd.AdminSQLite;
import com.example.proyectofinalgasparcurso.modelos.Usuario;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

public class LoginActivity extends AppCompatActivity {

    EditText txtEmail, txtPassword;
    MaterialButton btnLogin;
    TextView btnIrRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        btnIrRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

            }
        });

    }

    private void login() {
        // Obtenemos el texto escrito por el usuario
        String email = txtEmail.getText().toString();
        String pass = txtPassword.getText().toString();
        // Validamos que los campos no esten vacios
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Creamos la conexion con la base de datos
        AdminSQLite admin = new AdminSQLite(this);
        // Comprobamos si existe un usuario con ese email y contraseña
        Usuario u = admin.loginUsuario(email, pass);
        // Si el usuario existe
        if (u != null) {
            // Guardamos los datos del usuario en SharedPreferences
            // para mantener la sesion iniciada
            SharedPreferences prefs =
                    getSharedPreferences("usuario", MODE_PRIVATE);
            prefs.edit()
                    // Guardamos el id del usuario
                    .putInt("id", u.id)
                    // Guardamos la ruta de la foto de perfil
                    .putString("foto", u.foto)
                    // Hacemos los cambios
                    .apply();

            // Creamos el intent para abrir MainActivity una vez introducimos
            // nuestris datos
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            // Iniciamos la activity
            startActivity(i);
            // Cerramos el login para que no pueda volver atras
            finish();

        } else {

            // Si los datos son incorrectos mostramos error
            Toast.makeText(this,
                    "Datos incorrectos",
                    Toast.LENGTH_SHORT).show();
        }
    }
}