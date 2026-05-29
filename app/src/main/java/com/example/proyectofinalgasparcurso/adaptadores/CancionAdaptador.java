package com.example.proyectofinalgasparcurso.adaptadores;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyectofinalgasparcurso.R;
import com.example.proyectofinalgasparcurso.modelos.Cancion;

import java.util.ArrayList;
import java.util.HashMap;

public class CancionAdaptador extends BaseAdapter {

    private Context context;

    // Lista original completa de canciones
    private ArrayList<Cancion> listaOriginal;

    // Lista que se muestra filtrada en pantalla
    private ArrayList<Cancion> listaFiltrada;

    // Guardamos las portadas ya cargadas en memoria
    // para no volver a leerlas del archivo mp3 cada vez
    // esto hace que la lista cargue mas rapido y use menos recursos
    private HashMap<String, Bitmap> cachePortadas = new HashMap<>();

    public CancionAdaptador(Context context, ArrayList<Cancion> lista) {

        this.context = context;

        // Guardamos la copia de la lista original
        this.listaOriginal = new ArrayList<>(lista);

        // Lista filtrada que se mostrara en pantalla
        this.listaFiltrada = new ArrayList<>(lista);
    }

    @Override
    public int getCount() {

        // Devuelve cantidad de canciones
        return listaFiltrada.size();
    }

    @Override
    public Object getItem(int position) {

        // Devuelve la cancion seleccionada
        return listaFiltrada.get(position);
    }

    @Override
    public long getItemId(int position) {

        // Devuelve el id de la cancion
        return listaFiltrada.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Inflamos el layout del item de cancion
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_cancion, parent, false);

        // Estas lineas hacen que el click funcione bien en toda la fila del ListView y
        // evitamos errores al tocar los elementos como botones o imagenes
        view.setClickable(false);
        view.setFocusable(false);
        view.setFocusableInTouchMode(false);

        ImageView img = view.findViewById(R.id.imgCover);
        TextView titulo = view.findViewById(R.id.txtTitulo);
        TextView artista = view.findViewById(R.id.txtArtista);

        // Obtenemos la cancion actual
        Cancion c = listaFiltrada.get(position);

        // Mostramos el titulo y artista
        titulo.setText(c.titulo);
        artista.setText(c.artista);

        // Permitimos el efecto marquee si el texto es muy largo
        // esto es para diseño basicamente
        titulo.setSelected(true);

        // Cargamos la portada de la cancion
        cargarPortada(c.ruta, img);

        return view;
    }

    // Metodo para filtrar canciones por titulo o artista
    public void filtrar(String texto) {

        texto = texto.toLowerCase();

        // Limpiamos lista actual
        listaFiltrada.clear();

        // Si no hay texto mostramos todas las canciones
        if (texto.isEmpty()) {
            listaFiltrada.addAll(listaOriginal);
        } else {
            // Filtramos por el titulo o artista
            for (Cancion c : listaOriginal) {
                if (c.titulo.toLowerCase().contains(texto) ||
                        c.artista.toLowerCase().contains(texto)) {
                    listaFiltrada.add(c);
                }
            }
        }

        // Actualizamos la liistView
        notifyDataSetChanged();
    }

    // Metodo para cargar la portada del mp3
    private void cargarPortada(String ruta, ImageView img) {

        // Si ya existe usamos la imagen guardada
        if (cachePortadas.containsKey(ruta)) {
            img.setImageBitmap(cachePortadas.get(ruta));
            return;
        }

        try {
            //Los metadatos son informacion interna de un archivo de musica
            // como el titulo, artista o duracion que se puede leer sin reproducir el audio
            // Leemos metadatos del archivo mp3
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();retriever.setDataSource(ruta);
            // Obtenemos la portada
            byte[] cover = retriever.getEmbeddedPicture();
            if (cover != null) {
                // Convertimos de bytes a bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
                // Guardamos la portada
                cachePortadas.put(ruta, bitmap);
                // Mostramos la imagen
                img.setImageBitmap(bitmap);
            } else {
                // Ponemos la magen por defecto si no hay portada
                img.setImageResource(R.drawable.ic_music_2);
            }
            // Liberamos la memoria
            retriever.release();
        } catch (Exception e) {
            // Si ocurre un error mostramos imagen por defecto
            img.setImageResource(R.drawable.ic_music_2);
        }
    }

}