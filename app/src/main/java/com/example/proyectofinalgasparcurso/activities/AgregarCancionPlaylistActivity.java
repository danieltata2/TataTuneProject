package com.example.proyectofinalgasparcurso.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinalgasparcurso.R;
import com.example.proyectofinalgasparcurso.adaptadores.CancionAdaptador;
import com.example.proyectofinalgasparcurso.bbdd.AdminSQLite;
import com.example.proyectofinalgasparcurso.modelos.Cancion;

import java.util.ArrayList;

public class AgregarCancionPlaylistActivity extends AppCompatActivity {
    ListView lista;
    ArrayList<Cancion> canciones;
    CancionAdaptador adapter;
    int idPlaylist;
    View menu,mini;
    SearchView buscador;
    ImageButton btnHome,btnCanciones,btnFavoritos,btnPlaylists,btnHistorial;
    ImageView btnAtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_cancion_playlist);

        menu = findViewById(R.id.menuInferior);
        btnHome = menu.findViewById(R.id.btnMenuPrincipal);
        btnCanciones = menu.findViewById(R.id.btnMenuCanciones);
        btnFavoritos = menu.findViewById(R.id.btnMenuFavoritos);
        btnPlaylists = menu.findViewById(R.id.btnMenuPlaylists);
        btnHistorial = menu.findViewById(R.id.btnMenuHistorial);
        btnAtras=findViewById(R.id.btnVolver);
        lista = findViewById(R.id.listaCancionesPlaylist);

        // Obtenemos el id de la playlist enviado desde otra activity
        // Si no llega ningun id usamos -1 como valor por defecto
        idPlaylist = getIntent().getIntExtra("idPlaylist", -1);

        // Cargamos las canciones
        cargarCanciones();
        //  Creamos el adaptador
        adapter = new CancionAdaptador(this, canciones);
        lista.setAdapter(adapter);
        // Activar el SearchView del layout
        buscador = findViewById(R.id.buscador);

        // Buscamos el id interno de la linea del searchView
        int idS = buscador.getContext().getResources()
                .getIdentifier("android:id/search_plate", null, null);
        // Obtiene esa parte interna del searchView
        View searchPlate = buscador.findViewById(idS);
        if (searchPlate != null) {
            // Cuando lo encuentra va a quitar la linea del
            // buscador haciendolo transparente ya que con la linea
            // que aparece por defecto el diseño se ve un poco cutre
            searchPlate.setBackgroundColor(Color.TRANSPARENT);
        }

        // Esto va a detectar los cambios de texto dentro del buscador
        buscador.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Se ejecuta cuando el usuario pulsa buscar o enter
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                // Se ejecuta cada vez que el usuario escribe algo
                // y filtra las canciones segun lo que el usuario escribio
                adapter.filtrar(newText);
                return true;
            }
        });

        btnAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        mini = findViewById(R.id.miniPlayerContainer);
        // Mostramos el mini reproductor
        MiniPlayerManager.mostrarMiniPlayer(mini, this);
        // Detectara cuando el usuario pulsa una cancion de la lista
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Si no existe la playlist cerrara la pantalla
                if (idPlaylist == -1) {
                    // cierra la activity
                    finish();
                    return;
                }
                // Obtenemos la cancion que seleccionamos desde el adaptador filtrado
                // y no desde la lista original porque si no se filtraria
                Cancion c = (Cancion) adapter.getItem(position);
                // Conectamos con sqlite
                AdminSQLite admin = new AdminSQLite(AgregarCancionPlaylistActivity.this);
                // Insertamos la cancion que seleccionamos dentro de la playlist
                admin.insertarCancionEnPlaylist(idPlaylist, c.id);
                // cerramos al terminar
                finish();
            }
        });

        //configuramos los botones para poder navegar
        configurarMenuInferior();
    }

    // Este metodo carga todas las canciones guardadas en sqlite
    private void cargarCanciones() {
        AdminSQLite admin = new AdminSQLite(this);
        // Obtenemos todas las canciones de la bbdd y las guarda en la lista
        canciones = admin.obtenerTodasLasCanciones();
    }

    /*Este metodo configura los botones del menu inferior de la aplicacion
    -Cada boton abre una pantalla diferente como inicio, canciones,
    favoritos, playlists o historial usando intents
    -tambien usamos flags para reutilizar activities ya abiertas y evitar crear
    pantallas duplicadas
    */
    private void configurarMenuInferior() {
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AgregarCancionPlaylistActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnCanciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AgregarCancionPlaylistActivity.this, ListaCancionesActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnFavoritos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AgregarCancionPlaylistActivity.this, FavoritosActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnPlaylists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AgregarCancionPlaylistActivity.this, PlaylistsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AgregarCancionPlaylistActivity.this, HistorialActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }
}