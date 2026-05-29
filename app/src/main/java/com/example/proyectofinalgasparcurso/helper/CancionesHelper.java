package com.example.proyectofinalgasparcurso.helper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.proyectofinalgasparcurso.modelos.Cancion;

import java.util.ArrayList;
import java.util.List;

public class CancionesHelper {

    // Este metodo obtiene todas las canciones del telefono
    public static List<Cancion> obtenerCancionesDelTelefono(Context context) {

        // Esta es la lista donde guardaremos las canciones encontradas
        List<Cancion> lista = new ArrayList<>();

        // Uri del sistema donde Android guarda la musica
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        // Columnas que queremos obtener de cada cancion
        String[] columnas = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA
        };

        // Consultamos al sistema para obtener las canciones del dispositivo
        Cursor cursor = context.getContentResolver().query(uri, columnas, MediaStore.Audio.Media.IS_MUSIC + "!= 0",
                null,
                null
        );

        // Si hay resultados los recorremos
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // Creamos un objeto Cancion por cada resultado osea
                // el id,titulo,artista, y la ruta
                lista.add(new Cancion(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)
                ));
            }

            cursor.close();
        }

        return lista;
    }
}