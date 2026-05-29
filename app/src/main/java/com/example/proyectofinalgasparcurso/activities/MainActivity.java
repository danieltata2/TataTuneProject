package com.example.proyectofinalgasparcurso.activities;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.proyectofinalgasparcurso.adaptadores.CancionAdaptador;
import com.example.proyectofinalgasparcurso.bbdd.AdminSQLite;
import com.example.proyectofinalgasparcurso.R;
import com.example.proyectofinalgasparcurso.helper.CancionesHelper;
import com.example.proyectofinalgasparcurso.modelos.Cancion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    View mini, menu;
    ImageButton btnHome, btnCanciones, btnFavoritos, btnPlaylists, btnHistorial;

    CardView cardCanciones, cardFavoritos, cardPlaylists, cardHistorial;

    ImageView btnMenu, imgUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mini = findViewById(R.id.miniPlayerContainer);
        MiniPlayerManager.mostrarMiniPlayer(mini, this);
        menu = findViewById(R.id.menuInferior);
        btnHome = menu.findViewById(R.id.btnMenuPrincipal);
        btnCanciones = menu.findViewById(R.id.btnMenuCanciones);
        btnFavoritos = menu.findViewById(R.id.btnMenuFavoritos);
        btnPlaylists = menu.findViewById(R.id.btnMenuPlaylists);
        btnHistorial = menu.findViewById(R.id.btnMenuHistorial);
        cardCanciones = findViewById(R.id.cardCanciones);
        cardFavoritos = findViewById(R.id.cardFavoritos);
        cardPlaylists = findViewById(R.id.cardPlaylists);
        cardHistorial = findViewById(R.id.cardHistorial);
        imgUsuario = findViewById(R.id.imgUsuario);
        btnMenu = findViewById(R.id.btnMenu);


        // Obtiene la foto del usuario la foto guardada
        // Obtiene el archivo SharedPreferences llamado "usuario"
        // Aqui guarda la ruta de la foto cuando el usuario inicia sesion
        SharedPreferences prefs = getSharedPreferences("usuario", MODE_PRIVATE);

        // Recupera la ruta de la foto guardada
        // Si no existe devuelve "default"
        String rutaFoto = prefs.getString("foto", "default");


        // Si la ruta es default entocnces significa que el usuario no tiene foto personalizada
        if (rutaFoto.equals("default")) {
            // Entonces mostramos el icono por defecto
            imgUsuario.setImageResource(R.drawable.ic_music_note);
        } else {
            // Creamos un objeto file con la ruta guardada
            File file = new File(rutaFoto);
            // Verificamos que el archivo realmente exista en el almacenamiento interno
            if (file.exists()) {
                // Si existe cargamos la imagen desde ese archivo
                imgUsuario.setImageURI(Uri.fromFile(file));
            } else {
                // Si el archivo no existe o por ejemplo fue borrado
                // se muestra la imagen por defecto
                imgUsuario.setImageResource(R.drawable.ic_music_note);
            }
        }
        //Pedimos permisos para que la aplicacion funcione correctamente
        pedirPermiso();

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creamos un menu flotante pegado al boton btnMenu
                PopupMenu popup = new PopupMenu(MainActivity.this, btnMenu);
                // Agregamos una opcion al menu
                popup.getMenu().add("Cerrar sesion");
                // Detectamos cuando el usuario toca una opcion del menu
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(android.view.MenuItem item) {
                        // Si el usuario toca cerrar sesion el metodo se ejecuta
                        if (item.getTitle().equals("Cerrar sesion")) {
                            cerrarSesion();
                        }
                        return true;
                    }
                });
                // Mostramos el menu en pantalla
                popup.show();
            }
        });


        cardCanciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ListaCancionesActivity.class);
                startActivity(i);
            }
        });

        cardFavoritos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, FavoritosActivity.class);
                startActivity(i);
            }
        });

        cardPlaylists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, PlaylistsActivity.class);
                startActivity(i);
            }
        });

        cardHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, HistorialActivity.class);
                startActivity(i);
            }
        });

        //configuramos los botones del menu inferior
        configurarMenuInferior();
    }

    //este metodo es importante ya que se va a encargar
    // de pedir permisos para poder tener acceso a la
    // memoria interna
    private void pedirPermiso() {

        List<String> permisos = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //comprueba si falta el permiso de leer audio
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                permisos.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
            //comprueba si falta el permiso de notificaciones
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permisos.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        //si no esta vacia la lista abrira los cuadros para poder aceptarlo
        if (!permisos.isEmpty()) {
            requestPermissions(permisos.toArray(new String[0]), 1);
        } else {
            //si estan todos los permisos se cargan canciones
            cargarCanciones();
        }
    }


    //Este metodo sirve para detectar si el usuario acepto el permiso de leer musica y
    // si lo hizo cargara las canciones del telefono
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Llamamos al metodo original de la clase padre
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Comprobamos si la respuesta corresponde al permiso que pedimos osea al codigo 1
        if (requestCode == 1) {
            // Recorremos todos los permisos que respondio el usuario
            for (int i = 0; i < permissions.length; i++) {
                // Si el permiso es READ_MEDIA_AUDIO fue aceptado carga las canciones
                if (permissions[i].equals(Manifest.permission.READ_MEDIA_AUDIO)
                        && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    // cargar las canciones del telefono
                    cargarCanciones();
                }
            }
        }
    }


    private void cargarCanciones() {

        List<Cancion> lista = CancionesHelper.obtenerCancionesDelTelefono(this);

        AdminSQLite admin = new AdminSQLite(this);
        admin.insertarCancionesDelTelefono(lista);

    }

    //este metodo sirve para la navegacion del menu
    private void configurarMenuInferior() {

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        btnCanciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ListaCancionesActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        btnFavoritos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, FavoritosActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        btnPlaylists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, PlaylistsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        btnHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, HistorialActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

    }

    private void cerrarSesion() {

        // Paramos el reproductor si esta activo
        if (MiniPlayerManager.mediaPlayerGlobal != null) {

            if (MiniPlayerManager.mediaPlayerGlobal.isPlaying()) {
                MiniPlayerManager.mediaPlayerGlobal.stop();
            }

            MiniPlayerManager.mediaPlayerGlobal.release();
            MiniPlayerManager.mediaPlayerGlobal = null;
        }

        // Detenemos el servicio de musica
        stopService(new Intent(this, MusicService.class));

        // Limpiamos datos globales
        MiniPlayerManager.tituloActual = "";
        MiniPlayerManager.artistaActual = "";
        MiniPlayerManager.rutaActual = "";
        MiniPlayerManager.idCancionActual = -1;

        // Limpiamos la sesion del usuario
        SharedPreferences prefs = getSharedPreferences("usuario", MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Vamos al login
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mini = findViewById(R.id.miniPlayerContainer);
        MiniPlayerManager.mostrarMiniPlayer(mini, this);
    }


}