package com.example.proyectofinalgasparcurso.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.proyectofinalgasparcurso.R;
import com.example.proyectofinalgasparcurso.adaptadores.CancionAdaptador;
import com.example.proyectofinalgasparcurso.bbdd.AdminSQLite;
import com.example.proyectofinalgasparcurso.modelos.Cancion;

import java.io.File;
import java.util.ArrayList;

public class PlaylistDetalleActivity extends AppCompatActivity {

    ListView lista;
    CardView btnAgregar;
    ArrayList<Cancion> cancionesPlaylist;
    CancionAdaptador adapter;
    int idPlaylist;
    int usuarioId;
    ImageView btnOpciones;
    View menu, mini;
    TextView contador, nombrePlaylist, tituloTopBar;
    ImageButton btnHome, btnCanciones, btnFavoritos, btnPlaylists, btnHistorial;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detalle);

        menu = findViewById(R.id.menuInferior);
        btnHome = menu.findViewById(R.id.btnMenuPrincipal);
        btnCanciones = menu.findViewById(R.id.btnMenuCanciones);
        btnFavoritos = menu.findViewById(R.id.btnMenuFavoritos);
        btnPlaylists = menu.findViewById(R.id.btnMenuPlaylists);
        btnHistorial = menu.findViewById(R.id.btnMenuHistorial);
        lista = findViewById(R.id.listaCancionesPlaylist);
        contador = findViewById(R.id.txtContadorCanciones);

        lista = findViewById(R.id.listaCancionesPlaylist);
        btnAgregar = findViewById(R.id.btnAgregarCancion);
        ImageView btnVolver = findViewById(R.id.btnVolver);

        // Recibimos el id de la playlist que fue enviado desde la activity anterior
        idPlaylist = getIntent().getIntExtra("idPlaylist", -1);

        // Obtenemos el id del usuario guardado en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("usuario", MODE_PRIVATE);
        usuarioId = prefs.getInt("id", -1);

        // Buscamos el mini player en el layout y lo mostramos
        mini = findViewById(R.id.miniPlayerContainer);
        MiniPlayerManager.mostrarMiniPlayer(mini, this);

        // Cargamos las canciones que pertenecen a esta playlist
        cargarCancionesPlaylist();

        // Cargamos la informacion de la playlist como el nombre, la imagen, etc
        cargarInfoPlaylist();

        // Creamos el adaptador con la lista de canciones
        adapter = new CancionAdaptador(this, cancionesPlaylist);
        lista.setAdapter(adapter);

        // Mostramos u ocultamos la lista segun si hay canciones o no
        actualizarVisibilidad();


        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtenemos la cancion seleccionada desde el adapter
                Cancion c = (Cancion) adapter.getItem(position);
                // Creamos el intent para abrir el reproductor
                Intent i = new Intent(PlaylistDetalleActivity.this, PlayerActivity.class);
                // Enviamos el id de la cancion
                i.putExtra("idCancion", c.id);
                // Abrimos la activity del reproductor
                startActivity(i);
            }
        });


        lista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtenemos la cancion seleccionada segun la posicion
                Cancion c = (Cancion) adapter.getItem(position);
                AdminSQLite admin = new AdminSQLite(PlaylistDetalleActivity.this);
                // Eliminamos la cancion de la playlist usando su id
                admin.eliminarCancionDePlaylist(idPlaylist, c.id);
                // Recargamos la lista de canciones de la playlist
                cargarCancionesPlaylist();
                // Volvemos a crear el adaptador con los datos actualizados
                adapter = new CancionAdaptador(PlaylistDetalleActivity.this, cancionesPlaylist);
                // Asignamos el nuevo adaptador a la lista
                lista.setAdapter(adapter);
                return true;
            }
        });

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Abrimos la pantalla para añadir canciones a la playlist
        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creamos el intent para abrir la activity de agregar canciones
                Intent i = new Intent(PlaylistDetalleActivity.this, AgregarCancionPlaylistActivity.class);
                // Enviamos el id de la playlist para saber a cual agregar canciones
                i.putExtra("idPlaylist", idPlaylist);
                // Iniciamos la nueva pantalla
                startActivity(i);
            }
        });

        btnOpciones = findViewById(R.id.btnOpciones);

        btnOpciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creamos un popup que se mostrara al pulsar el boton
                PopupMenu popup = new PopupMenu(PlaylistDetalleActivity.this, btnOpciones);
                // Añadimos opciones al menu
                popup.getMenu().add(0, 1, 0, "Editar nombre");
                popup.getMenu().add(0, 2, 0, "Eliminar playlist");
                // Detectamos cuando el usuario pulsa una opcion de nuestro menu
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case 1:
                                // Editamos el nombre de la playlist
                                // aqui va la edicion
                                return true;
                            case 2:
                                // Eliminamos playlist de la base de datos
                                AdminSQLite admin = new AdminSQLite(PlaylistDetalleActivity.this);
                                // Llamamos a la base de datos para eliminar la playlist del usuario
                                boolean borrado = admin.eliminarPlaylist(idPlaylist, usuarioId);
                                // Si se elimina correctamente cerramos la pantalla
                                if (borrado) {
                                    finish();
                                }
                                return true;
                        }
                        return false;
                    }
                });
                // Mostramos el menu en pantalla
                popup.show();
            }
        });
        //Configuramos los botones para navegar
        configurarMenuInferior();
    }


    private void cargarCancionesPlaylist() {
        AdminSQLite admin = new AdminSQLite(this);
        cancionesPlaylist = admin.obtenerCancionesDePlaylist(idPlaylist);
    }


    private void configurarMenuInferior() {
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PlaylistDetalleActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnCanciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PlaylistDetalleActivity.this, ListaCancionesActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnFavoritos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PlaylistDetalleActivity.this, FavoritosActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnPlaylists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PlaylistDetalleActivity.this, PlaylistsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PlaylistDetalleActivity.this, HistorialActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }


    /*Este metodo revisa si la playlist esta vacia
    -si no hay canciones esconde la lista y si hay canciones la mostramos
    */
    private void actualizarVisibilidad() {
        // Si la playlist no tiene canciones vamos a ocultar la lista
        if (cancionesPlaylist.isEmpty()) {
            lista.setVisibility(View.GONE);
        } else {
            // Si tiene canciones mostramos la lista
            lista.setVisibility(View.VISIBLE);
        }
    }


    // Carga la informacion de la playlist desde la base de datos
    private boolean cargarInfoPlaylist() {

        // Creamos el acceso a la base de datos
        AdminSQLite admin = new AdminSQLite(this);
        SQLiteDatabase db = admin.getReadableDatabase();

        // Buscamos la playlist por su id y obtenemos osea el
        // usuario creador, nombre, imagen
        Cursor cursor = db.rawQuery(
                "SELECT usuario_id, nombre, imagen FROM playlist WHERE id=?",
                new String[]{String.valueOf(idPlaylist)}
        );
        // Si no existe la playlist cerramos tdo y salimos
        if (!cursor.moveToFirst()) {
            cursor.close();
            db.close();
            return false;
        }
        // Guardamos el id del usuario que creo la playlist
        // osea la primera columna del select
        int usuario_id = cursor.getInt(0);
        // Si el usuario actual no es el dueño cerramos la pantalla esto es por seguridad
        if (usuario_id != usuarioId) {
            cursor.close();
            db.close();
            finish();
            return false;
        }
        // Obtenemos los datos de la playlist
        String nombre = cursor.getString(1);
        String imagen = cursor.getString(2);

        cursor.close();
        db.close();
        // Actualizamos la interfaz con los datos obtenidos arriba
        actualizarUI(nombre, imagen);
        return true;
    }

    // Este metodo actualiza la interfaz de la pantalla con los datos de la playlist
    private void actualizarUI(String nombre, String imagen) {
        nombrePlaylist = findViewById(R.id.txtNombrePlaylist);
        tituloTopBar = findViewById(R.id.txtTituloTopBar);

        // Ponemos el nombre a ambos textView
        nombrePlaylist.setText(nombre);
        tituloTopBar.setText(nombre);

        ImageView portada = findViewById(R.id.imgPortadaPlaylist);
        // Si existe una imagen la cargamos desde la ruta del archivo
        if (imagen != null && !imagen.isEmpty() && !imagen.equals("null")) {
            // Creamos el archivo usando la ruta de la imagen que tenemos guardada
            File file = new File(imagen);
            // Convertimos ese archivo a Uri porque Android necesita este formato para poder mostrar imagenes
            Uri uri = Uri.fromFile(file);
            // Mostramos la imagen en el imageView usando esa ruta
            portada.setImageURI(uri);
        } else {
            // Si no hay imagen mostramos el icono por defecto
            portada.setImageResource(R.drawable.ic_music_note);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Recargamos las canciones de la playlist desde la base de datos
        cargarCancionesPlaylist();
        // Recargamos la informacion de la playlist como el nombre, imagen, etc
        cargarInfoPlaylist();
        // Creamos de nuevo el adaptador con la lista actualizada
        adapter = new CancionAdaptador(this, cancionesPlaylist);
        // Asignamos el adaptador a la lista
        lista.setAdapter(adapter);
        // Mostramos u podemos ocultar el estado vacio segun si hay canciones o no
        actualizarVisibilidad();

        mini = findViewById(R.id.miniPlayerContainer);
        // Actualizamos el mini player con la cancion actual
        MiniPlayerManager.mostrarMiniPlayer(mini, this);
    }


}