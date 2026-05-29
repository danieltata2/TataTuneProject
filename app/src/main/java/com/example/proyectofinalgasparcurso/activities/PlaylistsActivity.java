package com.example.proyectofinalgasparcurso.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinalgasparcurso.R;
import com.example.proyectofinalgasparcurso.adaptadores.PlaylistAdaptador;
import com.example.proyectofinalgasparcurso.bbdd.AdminSQLite;
import com.example.proyectofinalgasparcurso.modelos.Playlist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class PlaylistsActivity extends AppCompatActivity {

    ListView lista;
    ImageView btnCrear;
    ArrayList<Playlist> playlists;
    TextView txtContador;
    PlaylistAdaptador adapter;
    SearchView buscador;
    View mini,menu,searchPlate;
    int usuarioId;
    ImageButton btnHome,btnCanciones,btnFavoritos,btnPlaylists,btnHistorial;;
    private String rutaImagenSeleccionada = null;
    private View dialogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);

        menu = findViewById(R.id.menuInferior);
        btnHome = menu.findViewById(R.id.btnMenuPrincipal);
        btnCanciones = menu.findViewById(R.id.btnMenuCanciones);
        btnFavoritos = menu.findViewById(R.id.btnMenuFavoritos);
        btnPlaylists = menu.findViewById(R.id.btnMenuPlaylists);
        btnHistorial = menu.findViewById(R.id.btnMenuHistorial);
        lista = findViewById(R.id.listaPlaylists);
        btnCrear = findViewById(R.id.btnCrearPlaylist);
        mini = findViewById(R.id.miniPlayerContainer);
        buscador = findViewById(R.id.buscador);
        txtContador = findViewById(R.id.txtContador);

        // Obtenemos los datos guardados del usuario desde sharedPreferences
        SharedPreferences prefs = getSharedPreferences("usuario", MODE_PRIVATE);
        usuarioId = prefs.getInt("id", -1);

        // Obtenemos el elemento interno del searchView osea
        // esto es para quitar la linea porque queda muy fea
        int idS = buscador.getContext().getResources()
                .getIdentifier("android:id/search_plate", null, null);

        // Referenciamos a la vista interna del buscador
        searchPlate = buscador.findViewById(idS);

        if (searchPlate != null) {
            // Quitamos el fondo para que no se vea la linea del searchView
            searchPlate.setBackgroundColor(Color.TRANSPARENT);
        }

        // Mostramos el mini player en la pantalla actual
        MiniPlayerManager.mostrarMiniPlayer(mini, this);

        // Cargamos las playlists desde la base de datos
        cargarPlaylists();

        // Mostramos la cantidad de playlists en pantalla
        txtContador.setText(playlists.size() + " playlists");

        // Creamos el adaptador para mostrar la lista
        adapter = new PlaylistAdaptador(this, playlists);
        lista.setAdapter(adapter);

        // Este es el filtro de busqueda del SearchView
        buscador.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filtra la lista mientras el usuario escribe
                adapter.filtrar(newText);
                return true;
            }
        });

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtenemos la playlist seleccionada desde el adapter
                Playlist p = (Playlist) adapter.getItem(position);
                // Abrimos la pantalla de detalle de la playlist
                Intent i = new Intent(PlaylistsActivity.this, PlaylistDetalleActivity.class);
                // Enviamos el id de la playlist seleccionada
                i.putExtra("idPlaylist", p.id);
                // Iniciamos la activity
                startActivity(i);
            }
        });

        lista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtenemos la playlist que el usuario mantuvo presionada
                Playlist p = (Playlist) adapter.getItem(position);
                // Mostramos un dialogo para confirmar si quiere eliminarla
                new AlertDialog.Builder(PlaylistsActivity.this)
                        .setTitle("Eliminar playlist")
                        .setMessage("Seguro que quieres eliminar esta playlist?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Usamos el adminSQLite para eliminar la playlist y sus canciones
                                AdminSQLite admin = new AdminSQLite(PlaylistsActivity.this);
                                admin.eliminarPlaylist(p.id, usuarioId);
                                // Recargamos la lista de playlists despues de eliminar
                                cargarPlaylists();
                                // Actualizamos el contador de playlists
                                txtContador.setText(playlists.size() + " playlists");
                                // Volvemos a asignar el adaptador actualizado
                                adapter = new PlaylistAdaptador(PlaylistsActivity.this, playlists);
                                lista.setAdapter(adapter);
                            }
                        })
                        // Si elegimos el no simplemente cerramos el dialogo
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
        });

        // Boton para crear la nueva playlist
        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cuando el usuario toca el boton abrimos el dialogo para crear la playlist
                mostrarDialogoCrear();
            }
        });


        // Configuramos el menu inferior de navegacion
        configurarMenuInferior();
    }

    private void cargarPlaylists() {
        AdminSQLite admin = new AdminSQLite(this);
        playlists = admin.obtenerPlaylists(usuarioId);
    }

    /*Este metodo sirve para abrir un dialogo donde el usuario puede crear
    una playlist nueva escribiendo un nombre y eligiendo una imagen
    luego guarda la playlist en la base de datos y
    actualiza la lista de la pantalla y cierra el dialogo
     */

    private void mostrarDialogoCrear() {

        // Reiniciamos la imagen seleccionada
        rutaImagenSeleccionada = null;

        // Cargamos el layout del dialogo
        dialogView = getLayoutInflater().inflate(R.layout.dialog_crear_playlist, null);

        EditText input = dialogView.findViewById(R.id.txtNombrePlaylist);
        View btnElegirImagen = dialogView.findViewById(R.id.btnElegirImagen);
        View btnTexto = dialogView.findViewById(R.id.btnTextoElegirImagen);
        View btnCerrar = dialogView.findViewById(R.id.btnCerrar);
        Button btnCrear = dialogView.findViewById(R.id.btnCrearPlaylist);

        // Creamos el dialogo
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Cerramos el dialogo
        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Abrimos la galeria
        View.OnClickListener elegirImagen = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 200);
            }
        };

        // Los 2 botones abren la galeria
        btnElegirImagen.setOnClickListener(elegirImagen);
        btnTexto.setOnClickListener(elegirImagen);

        // Creamos la  playlist
        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nombre = input.getText().toString().trim();

                // Validamos el nombre
                if (nombre.isEmpty()) {
                    input.setError("Escribe un nombre");
                    return;
                }
                // Guardamos la playlist en la bbdd
                AdminSQLite admin = new AdminSQLite(PlaylistsActivity.this);
                admin.crearPlaylist(usuarioId, nombre, rutaImagenSeleccionada);
                // Recargamos la lista
                cargarPlaylists();
                // tambien actualizamos el contador para saber el numero
                // de playlists
                txtContador.setText(playlists.size() + " playlists");

                adapter = new PlaylistAdaptador(PlaylistsActivity.this, playlists);
                lista.setAdapter(adapter);
                // Cerramos el dialogo
                dialog.dismiss();
            }
        });
        // Mostramos el dialogo
        dialog.show();
    }


    /*Este metodo sirve para recibir el resultado de otra pantalla que abro con un intent
    en este caso la galeria de imagenes y aqui se obtiene la imagen seleccionada por el usuario
    para guardarla y convertirla en un archivo interno y mostrarla
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Comprobamos que viene del selector de imagen y que tdo es correcto
        if (requestCode == 200 && resultCode == RESULT_OK && dialogView != null) {
            // Obtenemos la imagen seleccionada
            Uri uri = data.getData();
            // Guardamos la imagen en almacenamiento interno y obtenemos su ruta
            rutaImagenSeleccionada = copiarImagenLocal(uri);
            // Referenciamos a las vistas del dialogo
            ImageView imgPreview = dialogView.findViewById(R.id.imgPreview);
            View iconDefault = dialogView.findViewById(R.id.iconDefaultArtwork);
            // Mostramos la imagen seleccionada
            imgPreview.setImageURI(Uri.fromFile(new File(rutaImagenSeleccionada)));
            // Ocultamos el icono por defecto
            imgPreview.setVisibility(View.VISIBLE);
            iconDefault.setVisibility(View.GONE);
        }
    }


    private String copiarImagenLocal(Uri uri) {
        try {
            // Abrimos el archivo de la imagen seleccionada
            InputStream input = getContentResolver().openInputStream(uri);
            // Creamos una carpeta interna para guardar playlists
            File carpeta = new File(getFilesDir(), "playlists");
            // Si no existe la carpeta la creamos
            if (!carpeta.exists()) carpeta.mkdirs();
            // Creamos el archivo donde se va a guardar la imagen
            File archivo = new File(carpeta, "playlist_" + System.currentTimeMillis() + ".jpg");
            // Preparamos salida para escribir el archivo
            OutputStream output = new FileOutputStream(archivo);
            // Buffer para copiar la imagen
            byte[] buffer = new byte[1024];
            int length;
            // Copiamos la imagen del input al archivo
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            input.close();
            output.close();
            // Devolvemos la ruta del archivo guardado
            return archivo.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void configurarMenuInferior() {

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PlaylistsActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnCanciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PlaylistsActivity.this, ListaCancionesActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnFavoritos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PlaylistsActivity.this, FavoritosActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnPlaylists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PlaylistsActivity.this, PlaylistsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PlaylistsActivity.this, HistorialActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargamos las playlists cada vez que la pantalla vuelve a estar visible
        cargarPlaylists();
        // Actualizamos el contador con el numero de playlists
        txtContador.setText(playlists.size() + " playlists");
        // Creamos de nuevo el adaptador con la lista actualizada
        adapter = new PlaylistAdaptador(this, playlists);
        lista.setAdapter(adapter);
        // Referenciamos el mini player
        mini = findViewById(R.id.miniPlayerContainer);
        //Mostramos el mini player en pantalla
        MiniPlayerManager.mostrarMiniPlayer(mini, this);
    }

}
