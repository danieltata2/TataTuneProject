package com.example.proyectofinalgasparcurso.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinalgasparcurso.R;
import com.example.proyectofinalgasparcurso.adaptadores.CancionAdaptador;
import com.example.proyectofinalgasparcurso.bbdd.AdminSQLite;
import com.example.proyectofinalgasparcurso.modelos.Cancion;
import com.example.proyectofinalgasparcurso.activities.PlayerActivity;

import android.widget.SearchView;
import android.widget.TextView;

import java.security.Principal;
import java.util.ArrayList;

public class ListaCancionesActivity extends AppCompatActivity {

    ListView lista;
    ArrayList<Cancion> canciones;
    TextView txtContador,txt;
    View plate,mini,menu;
    CancionAdaptador adapter;
    SearchView buscador;
    ImageView icon;
    ImageButton btnHome,btnCanciones,btnFavoritos,btnPlaylists,btnHistorial;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_canciones);
        lista = findViewById(R.id.listaCanciones);
        menu = findViewById(R.id.menuInferior);
        btnHome = menu.findViewById(R.id.btnMenuPrincipal);
        btnCanciones = menu.findViewById(R.id.btnMenuCanciones);
        btnFavoritos = menu.findViewById(R.id.btnMenuFavoritos);
        btnPlaylists = menu.findViewById(R.id.btnMenuPlaylists);
        btnHistorial = menu.findViewById(R.id.btnMenuHistorial);
        mini = findViewById(R.id.miniPlayerContainer);
        txtContador = findViewById(R.id.txtContador);
        buscador = findViewById(R.id.buscador);

        // Buscamos el texto interno del SearchView
        int id = buscador.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);

        // Obtenemos esa vista de texto
        txt = buscador.findViewById(id);
        // Cambiamos el color del texto que escribe el usuario
        txt.setTextColor(getResources().getColor(R.color.text_primary));
        // Cambiamos el color del hint (texto de ayuda)
        txt.setHintTextColor(getResources().getColor(R.color.text_secondary));
        // Cambiamos el tamaño del texto
        txt.setTextSize(16);

        // Cambiamos el icono de la lupa
        int idIcon = buscador.getContext().getResources()
                .getIdentifier("android:id/search_mag_icon", null, null);
        icon = buscador.findViewById(idIcon);
        // Con esto quitamos el tint basicamente esa para tocar diseño del searchView
        icon.setColorFilter(null);
        // Lo cambiamos por mi icono
        icon.setImageResource(R.drawable.ic_search);

        // Con esto se quita  linea inferior fea del serachView
        int idPlate = buscador.getContext().getResources()
                .getIdentifier("android:id/search_plate", null, null);
        plate = buscador.findViewById(idPlate);
        // Lo ponemos a transparente porque la linea blanca queda mal
        plate.setBackgroundColor(Color.TRANSPARENT);

        //Mostramos el mini reproductor
        MiniPlayerManager.mostrarMiniPlayer(mini, this);

        cargarCanciones();

        txtContador.setText(canciones.size() + " canciones");
        adapter = new CancionAdaptador(this, canciones);
        lista.setAdapter(adapter);


        // Escuchamos lo que el usuario escribe en el buscador
        buscador.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Se ejecuta cuando el usuario presiona buscar
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                // Aqui filtramos la lista mientras el usuario escribe
                adapter.filtrar(newText);
                return true;
            }
        });

        /*Aqui detectamos cuando el usuario toca una cancion en la lista
        obtiene esa cancion desde el adapter y crea un Intent para abrir playerActivity
        y envia los datos para reproducir la cancion en especifico*/
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long itemId) {
                // Obtenemos la cancion seleccionada
                Cancion c = (Cancion) adapter.getItem(position);
                // Abrimos el reproductor
                Intent i = new Intent(ListaCancionesActivity.this, PlayerActivity.class);
                // Enviamos los datos de la cancion para que no
                // se reproduzca otra cancion
                i.putExtra("idCancion", c.id);
                i.putExtra("titulo", c.titulo);
                i.putExtra("artista", c.artista);
                i.putExtra("ruta", c.ruta);
                // Abrimos la Activity
                startActivity(i);
            }
        });

        //Configuramos el menu para poder navegar
        configurarMenuInferior();

    }

    //Cargamos canciones
    private void cargarCanciones() {
        AdminSQLite admin = new AdminSQLite(this);
        canciones = admin.obtenerTodasLasCanciones();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Se ejecuta cuando volvemos a esta pantalla
        // obtenemos el searchView
        buscador = findViewById(R.id.buscador);
        // guardamos el texto del buscador
        String texto = buscador.getQuery().toString();
        // Si hay texto escrito vamos a filtrar la lista otra vez
        if (!texto.isEmpty()) {
            adapter.filtrar(texto);
        }
        // Buscamos el mini player
        mini = findViewById(R.id.miniPlayerContainer);
        // Mostramos el mini player en esta Activity
        MiniPlayerManager.mostrarMiniPlayer(mini, this);
    }

    private void configurarMenuInferior() {
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ListaCancionesActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnCanciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ListaCancionesActivity.this, ListaCancionesActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnFavoritos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ListaCancionesActivity.this, FavoritosActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnPlaylists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ListaCancionesActivity.this, PlaylistsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ListaCancionesActivity.this, HistorialActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }
}