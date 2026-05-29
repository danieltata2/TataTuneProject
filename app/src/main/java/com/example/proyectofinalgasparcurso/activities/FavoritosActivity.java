package com.example.proyectofinalgasparcurso.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyectofinalgasparcurso.R;
import com.example.proyectofinalgasparcurso.adaptadores.CancionAdaptador;
import com.example.proyectofinalgasparcurso.bbdd.AdminSQLite;
import com.example.proyectofinalgasparcurso.modelos.Cancion;

import java.util.ArrayList;

public class FavoritosActivity extends AppCompatActivity {

    ListView listaFavs;
    ImageButton btnHome, btnCanciones, btnFavoritos, btnPlaylists, btnHistorial;
    TextView txtContador;
    View mini,menu;
    ArrayList<Cancion> favoritos;
    SearchView buscador;
    int usuarioId;
    CancionAdaptador adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritos);

        listaFavs = findViewById(R.id.listaFavoritos);
        mini = findViewById(R.id.miniPlayerContainer);
        buscador = findViewById(R.id.buscador);
        txtContador = findViewById(R.id.txtContador);
        menu = findViewById(R.id.menuInferior);
        btnHome = menu.findViewById(R.id.btnMenuPrincipal);
        btnCanciones = menu.findViewById(R.id.btnMenuCanciones);
        btnFavoritos = menu.findViewById(R.id.btnMenuFavoritos);
        btnPlaylists = menu.findViewById(R.id.btnMenuPlaylists);
        btnHistorial = menu.findViewById(R.id.btnMenuHistorial);

        // Abrimos las preferencias donde guardamos los datos del usuario
        SharedPreferences prefs = getSharedPreferences("usuario", MODE_PRIVATE);
        // Obtenemos el id del usuario guardado en sharedPreferences y
        // si no existe devuelve -1 por defecto
        usuarioId = prefs.getInt("id", -1);


        // Buscamos el contenedor (osea donde escribimos) del searchView
        int idS = buscador.getContext().getResources()
                .getIdentifier("android:id/search_plate", null, null);
        // Obtenemos esa vista
        View searchPlate = buscador.findViewById(idS);
        // Le quitamos el fondo para que quede transparente
        // (esta parte solo es para quitar linea de diseño)
        if (searchPlate != null) {
            //La linea que aparece por defecto desaparece
            searchPlate.setBackgroundColor(Color.TRANSPARENT);
        }
        // Mostramos el mini reproductor
        MiniPlayerManager.mostrarMiniPlayer(mini, this);
        // Cargamos las canciones favoritas
        cargarFavoritos();
        // Mostramos cuantas canciones hay en favoritos
        txtContador.setText(favoritos.size() + " canciones");
        // Creamos el adaptador con la lista de favoritos
        adapter = new CancionAdaptador(this, favoritos);
        // Conectamos nuestro adaptador con el listView
        listaFavs.setAdapter(adapter);

        //Sirve para detectar lo que el usuario escribe en el buscador
        buscador.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Cuando el usuario le da buscar en el teclado
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                // Cada vez que el usuario escribe o borra algo
                // filtramos la lista
                adapter.filtrar(newText);
                return true;
            }
        });

        //Sirve para detectar un click normal sobre un item de la lista
        listaFavs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtiene la cancion seleccionada desde el adaptador
                Cancion cancion = (Cancion) adapter.getItem(position);
                // Crea un intent para abrir la pantalla del reproductor
                Intent i = new Intent(FavoritosActivity.this, PlayerActivity.class);
                // Enviamos el id de la cancion seleccionada al playerActivity
                i.putExtra("idCancion", cancion.id);
                // iniciamos la actividad del reproductor
                startActivity(i);
            }
        });

        //Sirve para detectar una pulsacion larga
        listaFavs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtiene la cancion seleccionada desde el adaptador
                Cancion c = (Cancion) adapter.getItem(position);
                // Creamos la conexion con sqlite
                AdminSQLite admin = new AdminSQLite(FavoritosActivity.this);
                // Eliminamos la cancion de favoritos usando el id del usuario y de la cancion
                admin.eliminarFavoritoPorUsuarioYCancion(usuarioId, c.id);
                // Recargamos la lista de favoritos actualizada
                cargarFavoritos();
                // Creamos nuevamente el adaptador con la lista actualizada
                adapter = new CancionAdaptador(FavoritosActivity.this, favoritos);
                // Actualizamos el listView con el nuevo adaptador
                listaFavs.setAdapter(adapter);
                return true;
            }
        });

        // Configuramos los botones de abajo para poder navegar
        configurarMenuInferior();
    }

    //Cargamos la lista de favoritos y lo agregamos a la lista
    private void cargarFavoritos() {
        AdminSQLite admin = new AdminSQLite(this);
        favoritos = admin.obtenerFavoritosDeUsuario(usuarioId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se ejecutara cada vez que la pantalla vuelve a estar activa
        // por ejemplo cuando regresamos del reproductor
        // Recargamos la lista de favoritos para actualizar los cambios
        cargarFavoritos();
        // Actualizamos el contador de canciones
        txtContador.setText(favoritos.size() + " canciones");
        // Volvemos a crear el adaptador con la lista actualizada
        adapter = new CancionAdaptador(this, favoritos);
        // Lo conectamos otra vez al ListView
        listaFavs.setAdapter(adapter);
        // Buscamos el mini player en la pantalla y lo conectamos
        mini = findViewById(R.id.miniPlayerContainer);
        // Mostramos el mini player en esta activity
        MiniPlayerManager.mostrarMiniPlayer(mini, this);
    }

    private void configurarMenuInferior() {
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(FavoritosActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnCanciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(FavoritosActivity.this, ListaCancionesActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnFavoritos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(FavoritosActivity.this, FavoritosActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnPlaylists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(FavoritosActivity.this, PlaylistsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(FavoritosActivity.this, HistorialActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }


}