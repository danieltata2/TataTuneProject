package com.example.proyectofinalgasparcurso.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinalgasparcurso.R;
import com.example.proyectofinalgasparcurso.adaptadores.CancionAdaptador;
import com.example.proyectofinalgasparcurso.bbdd.AdminSQLite;
import com.example.proyectofinalgasparcurso.modelos.Cancion;

import java.util.ArrayList;

public class HistorialActivity extends AppCompatActivity {
    ListView lista;
    ArrayList<Cancion> historial;
    ArrayList<Integer> idsHistorial;
    TextView txtContador;
    CancionAdaptador adapter;
    int usuarioId;
    View menu,mini,searchPlate;
    SearchView buscador;
    ImageButton btnHome, btnCanciones, btnFavoritos, btnPlaylists, btnHistorial;
    ImageView btnEliminarHistorial;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        lista = findViewById(R.id.listaHistorial);
        txtContador = findViewById(R.id.txtContador);
        lista = findViewById(R.id.listaHistorial);
        menu = findViewById(R.id.menuInferior);
        btnHome = menu.findViewById(R.id.btnMenuPrincipal);
        btnCanciones = menu.findViewById(R.id.btnMenuCanciones);
        btnFavoritos = menu.findViewById(R.id.btnMenuFavoritos);
        btnPlaylists = menu.findViewById(R.id.btnMenuPlaylists);
        btnHistorial = menu.findViewById(R.id.btnMenuHistorial);
        btnEliminarHistorial = findViewById(R.id.btnEliminarHistorial);
        buscador = findViewById(R.id.buscador);
        // Abrimos las preferencias donde guardamos los datos del usuario
        SharedPreferences prefs = getSharedPreferences("usuario", MODE_PRIVATE);
        // Obtenemos el id del usuario guardado en sharedPreferences y
        // si no existe devuelve -1 por defecto
        usuarioId = prefs.getInt("id", -1);

        // Buscamos el contenedor ,osea donde escribimos para buscar la cancion, del searchView
        int idS = buscador.getContext().getResources()
                .getIdentifier("android:id/search_plate", null, null);
        // Obtenemos esa vista
        searchPlate = buscador.findViewById(idS);
        // Le quitamos el fondo para que quede transparente
        // en esta parte solo es para quitar linea de diseño
        if (searchPlate != null) {
            //La linea que aparece por defecto desaparece
            searchPlate.setBackgroundColor(Color.TRANSPARENT);
        }

        mini = findViewById(R.id.miniPlayerContainer);
        // Mostramos el mini reproductor
        MiniPlayerManager.mostrarMiniPlayer(mini, this);

        //cargamos el historial
        cargarHistorial();

        adapter = new CancionAdaptador(this, historial);
        lista.setAdapter(adapter);

        // Este metodo recoge lo que el usuario escribe en el buscador
        buscador.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Se ejecuta cuando el usuario presiona buscar
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                // Filtramos la lista mientras el usuario escribe
                adapter.filtrar(newText);
                return true;
            }
        });

        //Este metodo sirve para detectar cuando el usuario pulsa una cancion
        // de la lista del historial
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtenemos la cancion seleccionada
                Cancion c = (Cancion) adapter.getItem(position);
                // Abrimos el playerActivity
                Intent i = new Intent(HistorialActivity.this, PlayerActivity.class);
                // Enviamos el id de la cancion
                i.putExtra("idCancion", c.id);
                // Iniciamos la activity que nos lleva al player
                startActivity(i);
            }
        });

        // Detectamos cuando el usuario toca el boton de eliminar historial
        // para borrar tdo el historial
        btnEliminarHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoEliminarHistorial();
            }
        });
        //Configuramos el menu inferior para poder navegar
        configurarMenuInferior();
    }


    //Este metodo nos muestra un dialogo con opciones
    //este dialogo es especificamente para mostrar el dialog
    //con las opciones de eliminacion del historial
    private void mostrarDialogoEliminarHistorial() {
        // Creamos un cuadro de confirmacion
        new AlertDialog.Builder(HistorialActivity.this)
                // Este es el titulo del dialogo
                .setTitle("Eliminar historial")
                // Mensaje que veremos
                .setMessage("¿Seguro que quieres borrar todo el historial?")
                // Este es el boton para confirmar la eliminacion del historial
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Abrimos la conexion con sqlite
                        AdminSQLite admin = new AdminSQLite(HistorialActivity.this);
                        // Borramos el historial
                        admin.borrarHistorialCompleto(usuarioId);
                        // Recargamos el historial
                        cargarHistorial();
                        // Actualizamos la interfaz
                        actualizarUI();
                    }
                })
                // Boton de cancelar que basicamente no hace nada
                .setNegativeButton("Cancelar", null)
                // Mostramos el dialogo y seleccionamos una opcion
                .show();

    }

    //Cargamos el historial de la base de datos
    private void cargarHistorial() {
        idsHistorial = new ArrayList<>();
        AdminSQLite admin = new AdminSQLite(this);
        historial = admin.obtenerHistorial(usuarioId, idsHistorial);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Se ejecuta cada vez que volvemos a esta pantalla
        // y recargamos el historial
        cargarHistorial();
        // Creamos nuevamente el adapter con las canciones del historial
        adapter = new CancionAdaptador(this, historial);
        // Conectamos el adapter con la lista
        lista.setAdapter(adapter);
        // Buscamos el mini player
        mini = findViewById(R.id.miniPlayerContainer);
        // Mostramos el mini player en esta Activity
        MiniPlayerManager.mostrarMiniPlayer(mini, this);
    }

    //metodo para configurar los botones y su funcionalidad
    private void configurarMenuInferior() {
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(HistorialActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnCanciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(HistorialActivity.this, ListaCancionesActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnFavoritos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(HistorialActivity.this, FavoritosActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnPlaylists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(HistorialActivity.this, PlaylistsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btnHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(HistorialActivity.this, HistorialActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }


    /*Este metodo actualiza la pantalla mostrando cuantas canciones hay en el historial
    -oculta la lista si esta vacia y la muestra si tiene canciones */
    private void actualizarUI() {
        // Mostramos cuantas canciones hay en el historial
        txtContador.setText(historial.size() + " canciones");
        // Si el historial esta vacio ocultamos la lista
        if (historial.isEmpty()) {
            lista.setVisibility(View.GONE);
        } else {
            // Si hay canciones mostramos la lista
            lista.setVisibility(View.VISIBLE);
        }
        // Actualizamos el adapter para refrescar los cambios en pantalla
        if (adapter != null) {
            //notifyDataSetChanged() se usa para avisarle al adapter
            //que la lista cambio y debe actualizar lo que se muestra en pantalla
            adapter.notifyDataSetChanged();
        }
    }

}