package com.example.proyectofinalgasparcurso.adaptadores;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.proyectofinalgasparcurso.R;
import com.example.proyectofinalgasparcurso.modelos.Playlist;

import java.io.File;
import java.util.ArrayList;

public class PlaylistAdaptador extends BaseAdapter {

    private Context context;

    private ArrayList<Playlist> listaOriginal;

    private ArrayList<Playlist> listaFiltrada;

    public PlaylistAdaptador(Context context, ArrayList<Playlist> lista) {

        this.context = context;

        // Guardamos una copia de la lista original
        this.listaOriginal = new ArrayList<>(lista);

        // Esta lista se usa para mostrar datos en el ListView
        this.listaFiltrada = new ArrayList<>(lista);
    }

    @Override
    public int getCount() {

        return listaFiltrada.size();
    }

    @Override
    public Object getItem(int position) {

        return listaFiltrada.get(position);
    }

    @Override
    public long getItemId(int position) {

        return listaFiltrada.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Reusamos la vista si existe si no la creamos
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);}

        TextView txtNombre = convertView.findViewById(R.id.txtNombrePlaylist);
        ImageView imgPlaylist = convertView.findViewById(R.id.imgPlaylist);

        // Obtenemos la playlist actual
        Playlist p = listaFiltrada.get(position);

        // Mostramos el nombre
        txtNombre.setText(p.nombre);

        // Comprobamos si tiene imagen guardada
        if (p.imagen != null && !p.imagen.equals("null") && !p.imagen.isEmpty()) {

            File archivo = new File(p.imagen);

            // Si el archivo existe mostramos la imagen
            if (archivo.exists()) {
                imgPlaylist.setImageURI(Uri.fromFile(archivo));
            } else {
                // Si no existe mostramos el icono por defecto
                imgPlaylist.setImageResource(R.drawable.ic_playlist);
            }

        } else {
            // Si no hay imagen mostramos el icono por defecto
            imgPlaylist.setImageResource(R.drawable.ic_playlist);
        }

        return convertView;
    }

    // Este metodo sirve para filtrar playlists por nombre
    public void filtrar(String texto) {
        texto = texto.toLowerCase();
        listaFiltrada.clear();
        // Si no hay texto mostramos todo
        if (texto.isEmpty()) {
            listaFiltrada.addAll(listaOriginal);
        } else {
            // Filtramos por nombre
            for (Playlist p : listaOriginal) {
                if (p.nombre.toLowerCase().contains(texto)) {
                    listaFiltrada.add(p);
                }
            }
        }
        // Actualizamos la lista en pantalla
        notifyDataSetChanged();
    }
}