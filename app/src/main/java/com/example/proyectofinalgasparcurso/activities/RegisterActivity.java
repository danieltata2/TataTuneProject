package com.example.proyectofinalgasparcurso.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import java.security.MessageDigest;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinalgasparcurso.R;
import com.example.proyectofinalgasparcurso.bbdd.AdminSQLite;

import java.io.File;
import java.io.FileOutputStream;


public class RegisterActivity extends AppCompatActivity {

    EditText txtNombre, txtEmail, txtPassword, txtConfirmar;
    Button btnRegistrar;
    ImageView imgPerfil;
    String rutaFoto = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txtNombre = findViewById(R.id.txtNombre);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfirmar = findViewById(R.id.txtConfirmar);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        imgPerfil = findViewById(R.id.imgPerfil);

        imgPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarFoto();
            }
        });

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrar();
            }
        });

    }

    private void seleccionarFoto() {
        //Este metodo es importante ya que lanza la accion
        // crea un Intent con la accion ACTION_PICK
        // esto le dice a android abre una app para que el usuario elija un archivo
        Intent i = new Intent(Intent.ACTION_PICK);

        // Filtrara el tipo de archivo que se puede seleccionar
        // como se necesita una foto se debera seleccionar una imagen jpg,png,etc
        i.setType("image/*");

        // Abrira la galeria o fotos y espera un resultado
        // Cuando el usuario selecciona una imagen o cancele android
        // llamara automaticamente a onActivityResult()
        startActivityForResult(i, 20);
    }


    /*Este metodo comprueba que el usuario ha seleccionado una imagen correctamente
    -obtiene la URI de la imagen, la convierte a bitmap, la guarda en la memoria interna
    -de la app como archivo, guarda la ruta del archivo para usarla despues y nos
    -muestra la imagen en pantalla en el ImageView del perfil
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Este metodo es el que recibe la accion que se lanzo
        // El requestCode debe ser 100 (esto viene del metodo de arriba osea de seleccionar foto)
        // Si el usuario selecciono una imagen el resultado sera RESULT_OK
        // Si el Intent trae datos validos
        // Si el usuario cancela la galeria devuelve nada

        if (requestCode == 20 && resultCode == RESULT_OK && data != null) {

            // Esto es necesario ya que Android no deja
            // entrar directamente a las carpetas del telefono
            // Obtiene la URI de la imagen seleccionada en la galeria
            // Ejemplo:
            // content://media/external/images/media/12345 (esta es una direccion que viene del sistema)
            Uri uri = data.getData();

            try {
                // Convierte la direccion obtenida (URI) en un Bitmap (la imagen real)
                //MediaStore es el catalogo de fotos,videos ,etc que existe en el telefono
                //Cuando se pasa la URI, Mediastore va a buscar la imagen y la devuelve como bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                // Crea un archivo dentro de la memoria interna de mi app donde se guardara la imagen
                // Este archivo sera la foto de perfil permanente
                // En esta carpeta especial se accede con el getFilesDir()
                // Normalmente aqui se guarda configuracion,foto de perfil,archivos importantes
                // /data/data/x_paquete/files/perfil.jpg
                File file = new File(getFilesDir(), "perfil.jpg");

                // El fileoutputstream lo necesitamos para poder
                // escribir la imagen dentro del archivo
                FileOutputStream fos = new FileOutputStream(file);

                // Va a comprimir el Bitmap a formato .jpg
                // y lo va a guardar dentro del archivo creado
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);

                fos.close();

                // Se guarda la ruta del archivo para almacenarla en la bbdd
                // y esta sirve para poder asociarla al usuario
                rutaFoto = file.getAbsolutePath();

                // Se muestra la imagen en el ImageView del layout
                imgPerfil.setImageBitmap(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    private void registrar() {
        String nombre = txtNombre.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();
        String pass = txtPassword.getText().toString().trim();
        String confirm = txtConfirmar.getText().toString().trim();

        // Validamos los campos vacios
        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validamos el tamaño minimo del nombre
        if (nombre.length() < 3) {
            Toast.makeText(this, "El nombre debe tener minimo 3 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validamos el formato del email
        if (!email.contains("@") || !email.contains(".")) {
            Toast.makeText(this, "Introduce un email valido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validamos el tamaño de la contraseña
        if (pass.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener minimo 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validamos que las contraseñas si coincidan
        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si el usuario no selecciona foto usamos una por defecto
        if (rutaFoto == null) {
            rutaFoto = "default";
        }

        AdminSQLite admin = new AdminSQLite(this);

        // Registramos al usuario en la base de datos
        if (admin.registrarUsuario(nombre, email, pass, rutaFoto)) {
            Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "El email ya existe", Toast.LENGTH_SHORT).show();
        }
    }
}
