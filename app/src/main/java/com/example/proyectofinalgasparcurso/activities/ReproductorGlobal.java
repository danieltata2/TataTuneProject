package com.example.proyectofinalgasparcurso.activities;

import com.example.proyectofinalgasparcurso.activities.MiniPlayerManager;
import com.example.proyectofinalgasparcurso.modelos.Cancion;

import java.util.ArrayList;

public class ReproductorGlobal {

    public static ArrayList<Cancion> lista;
    public static int indice;

    // Metodo para pasar a la siguiente cancion
    public static void siguiente() {

        // Si no hay canciones salimos
        if (lista == null || lista.isEmpty()) {
            return;
        }

        // Avanzamos una posicion
        indice++;

        // Si llegamos al final volvemos al inicio de la lista
        // para evitar errores
        if (indice >= lista.size()) {
            indice = 0;
        }

        // Obtenemos la nueva cancion actual
        Cancion c = lista.get(indice);

        // Guardamos los datos de la cancion en el mini player global
        // para no perder y reproducir otra cancion
        MiniPlayerManager.idCancionActual = c.id;
        MiniPlayerManager.tituloActual = c.titulo;
        MiniPlayerManager.artistaActual = c.artista;
        MiniPlayerManager.rutaActual = c.ruta;

        try {
            // Reiniciamos el reproductor
            MiniPlayerManager.mediaPlayerGlobal.reset();
            // Cargamos la nueva ruta del audio
            // que elegimos
            MiniPlayerManager.mediaPlayerGlobal.setDataSource(c.ruta);
            // Preparamos el audio que elegimos
            MiniPlayerManager.mediaPlayerGlobal.prepare();
            // Reproducimos la cancion
            MiniPlayerManager.mediaPlayerGlobal.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Actualizamos el mini player de manera visual
        MiniPlayerManager.actualizarMiniPlayer(MiniPlayerManager.ultimoContexto);
    }


    // Metodo para volver a la cancion anterior
    public static void anterior() {

        // Si no hay canciones salimos
        if (lista == null || lista.isEmpty()) {
            return;
        }

        // Retrocedemos una posicion
        indice--;

        // Si bajamos de 0 vamos al final de la lista
        // para evitar errores
        if (indice < 0) {
            indice = lista.size() - 1;
        }

        // Obtenemos la nueva cancion actual
        Cancion c = lista.get(indice);

        // Guardamos los datos de la cancion en el mini player global
        // para no perder y reproducir otra cancion
        MiniPlayerManager.idCancionActual = c.id;
        MiniPlayerManager.tituloActual = c.titulo;
        MiniPlayerManager.artistaActual = c.artista;
        MiniPlayerManager.rutaActual = c.ruta;

        try {
            // Reiniciamos el reproductor
            MiniPlayerManager.mediaPlayerGlobal.reset();
            // Cargamos la nueva ruta del audio
            // que elegimos
            MiniPlayerManager.mediaPlayerGlobal.setDataSource(c.ruta);
            // Preparamos el audio que elegimos
            MiniPlayerManager.mediaPlayerGlobal.prepare();
            // Reproducimos la cancion
            MiniPlayerManager.mediaPlayerGlobal.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Actualizamos mini player de manera visual
        MiniPlayerManager.actualizarMiniPlayer(MiniPlayerManager.ultimoContexto);
    }

}