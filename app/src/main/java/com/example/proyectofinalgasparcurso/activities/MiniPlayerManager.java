package com.example.proyectofinalgasparcurso.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.proyectofinalgasparcurso.R;
import com.example.proyectofinalgasparcurso.modelos.Cancion;

public class MiniPlayerManager {



    public static MediaPlayer mediaPlayerGlobal = null;

    public static int idCancionActual = -1;
    public static String tituloActual = "";
    public static String artistaActual = "";

    private static ImageView imgMiniCover;


    //Guarda la vista del mini player para poder actualizarlo
    //osea el layout visual del mini reproductor
    public static View ultimoMiniPlayerView = null;

    //Guarda el contexto actual(Activity)
    //por ejemplo PlayerActivity.this
    public static Context ultimoContexto = null;

    public static String rutaActual = "";

    public static Equalizer equalizerGlobal;
    public static BassBoost bassGlobal;
    public static Virtualizer virtualizerGlobal;

    public static final int NOTIFICATION_ID = 1;




    //Este metodo sirve para configurar y actualizar el mini player para que siempre muestre
    // la cancion actual y nos permita controlar la musica desde cualquier activity
    public static void mostrarMiniPlayer(View miniPlayer, Context context) {
        // Si no hay vista o no hay reproductor no hacemos nada
        if (miniPlayer == null || mediaPlayerGlobal == null) {
            return;
        }
        // Guardamos referencias para poder actualizar el mini player despues
        ultimoMiniPlayerView = miniPlayer;
        ultimoContexto = context;
        // Si no hay cancion seleccionada ocultamos el mini player
        if (idCancionActual == -1) {
            miniPlayer.setVisibility(View.GONE);
            return;
        }
        // Mostramos el mini player
        miniPlayer.setVisibility(View.VISIBLE);
        // Hacemos las referencias a los textos y botones del layout
        TextView txtTitulo = miniPlayer.findViewById(R.id.miniTitulo);
        TextView txtArtista = miniPlayer.findViewById(R.id.miniArtista);
        ImageView btnPlayPause = miniPlayer.findViewById(R.id.miniPlayPause);
        ImageView btnPrev = miniPlayer.findViewById(R.id.miniPrev);
        ImageView btnNext = miniPlayer.findViewById(R.id.miniNext);
        imgMiniCover = miniPlayer.findViewById(R.id.imgMiniCover);

        // Mostramos titulo y artista
        txtTitulo.setText(tituloActual);
        txtArtista.setText(artistaActual);
        // Cargamos la caratula de la cancion
        if (rutaActual == null || rutaActual.isEmpty()) {
            imgMiniCover.setImageResource(R.drawable.ic_music_note);
        } else {
            actualizarMiniCover(rutaActual);
        }
        // Comprobamos si la musica esta reproduciendose
        boolean reproduciendo = mediaPlayerGlobal.isPlaying();
        // Cambiamos el icono del boton segun el estado de play o pause
        if (reproduciendo) {
            btnPlayPause.setImageResource(R.drawable.ic_pause);
        } else {
            btnPlayPause.setImageResource(R.drawable.ic_play);
        }
        // Si hacemos click en play/pause cambia estado de la musica
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MiniPlayerManager.togglePlayPause(context);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MiniPlayerManager.siguiente();
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MiniPlayerManager.anterior();
            }
        });
        // Si hacemos click en el mini player abrira pantalla del reproductor
        miniPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PlayerActivity.class);

                i.putExtra("idCancion", idCancionActual);
                i.putExtra("ruta", rutaActual);
                i.putExtra("titulo", tituloActual);
                i.putExtra("artista", artistaActual);

                // necesario porque este context no siempre es una Activity
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(i);
            }
        });
    }

    // Actualizamos el mini player desde cualquier parte de la app
    public static void actualizarMiniPlayer(Context context) {
        // Si el mini player esta activo en pantalla
        if (ultimoMiniPlayerView != null) {
            // Recargamos la informacion del mini player
            mostrarMiniPlayer(ultimoMiniPlayerView, context);
        }
    }

    //Este metodo sirve para cargar y mostrar la caratula de la cancion en el mini player
    //como los colores y la imagen
    public static void actualizarMiniCover(String path) {
        // Si no existe la imagen o el mini player no hacemos nada
        if (imgMiniCover == null || ultimoMiniPlayerView == null) {
            return;
        }
        // Si no hay ruta mostramos icono por defecto
        if (path == null || path.isEmpty()) {
            imgMiniCover.setImageResource(R.drawable.ic_music_note);
            return;
        }
        try {
            // Creamos el lector de metadatos para el archivo de audio
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            // Le indicamos la ruta del archivo de musica
            retriever.setDataSource(path);
            // Obtenemos la imagen que viene dentro del archivo mp3
            byte[] data = retriever.getEmbeddedPicture();
            // y liberamos recursos para no gastar memoria
            retriever.release();
            // Si la cancion no tiene imagen usamos icono por defecto
            if (data == null) {
                imgMiniCover.setImageResource(R.drawable.ic_music_note);
                return;
            }
            // Convertimos los datos en imagen
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            // Mostramos la caratula
            imgMiniCover.setImageBitmap(bitmap);
            // Cambiamos colores del mini player segun la imagen
            aplicarColores(bitmap);
        } catch (Exception e) {
            // Si ocurre un error mostramos icono por defecto
            imgMiniCover.setImageResource(R.drawable.ic_music_note);
        }
    }


    // Aplicamos colores al mini player relacionado con la imagen de la cancion
    private static void aplicarColores(Bitmap bitmap) {
        // Generamos una paleta de colores a partir de la imagen
        androidx.palette.graphics.Palette.from(bitmap).generate(palette -> {
            // Obtienemos el color principal de la imagen
            // Si no lo encuentra usa un color por defecto
            int color = palette.getDominantColor(
                    android.graphics.Color.parseColor("#1A1A1A")
            );
            // Cambiamos el color de fondo del mini player
            ((com.google.android.material.card.MaterialCardView) ultimoMiniPlayerView)
                    .setCardBackgroundColor(color);
            // Referenciamos a los textos del mini player
            TextView titulo = ultimoMiniPlayerView.findViewById(R.id.miniTitulo);
            TextView artista = ultimoMiniPlayerView.findViewById(R.id.miniArtista);
            // Cambiamos colores del texto para que se vean bien
            titulo.setTextColor(android.graphics.Color.WHITE);
            artista.setTextColor(android.graphics.Color.LTGRAY);
        });
    }


    public static void siguiente() {
        // Si el PlayerActivity esta abierto usamos su metodo
        // para pasar a la siguiente cancion desde el reproductor
        if (PlayerActivity.instancia != null) {
            PlayerActivity.instancia.siguienteDesdeMiniPlayer();
        }
        // Si el reproductor no esta abierto usamos
        // el reproductor global
        else {
            ReproductorGlobal.siguiente();
        }
        // Obtenemos la nueva cancion actual
        Cancion c = ReproductorGlobal.lista.get(ReproductorGlobal.indice);
        // Guardamos los datos de la cancion actual
        idCancionActual = c.id;
        tituloActual = c.titulo;
        artistaActual = c.artista;
        rutaActual = c.ruta;
        // Actualizamos el mini player si existe contexto
        if (ultimoContexto != null) {
            actualizarMiniPlayer(ultimoContexto);
        }
        // Actualizamos la notificacion de musica
        if (ultimoContexto != null) {
            MusicService.actualizarNotificacion(ultimoContexto);
        }
    }


    public static void anterior() {
        // Si el reproductor esta abierto usamos su metodo
        if (PlayerActivity.instancia != null) {
            PlayerActivity.instancia.anteriorDesdeMiniPlayer();
        }
        // Si no esta abierta usamos el reproductor global
        else {
            ReproductorGlobal.anterior();
        }
        // Obtenemos la nueva cancion actual
        Cancion c = ReproductorGlobal.lista.get(ReproductorGlobal.indice);

        idCancionActual = c.id;
        tituloActual = c.titulo;
        artistaActual = c.artista;
        rutaActual = c.ruta;

        // Actualizamos mini player
        if (ultimoContexto != null) {
            actualizarMiniPlayer(ultimoContexto);
        }
        // Actualizamos notificacion
        if (ultimoContexto != null) {
            MusicService.actualizarNotificacion(ultimoContexto);
        }
    }


    //Este metodo sirve para alternar entre play y pause de la musica y  actualizar tdo como
    // lanotificacion, el mini player y la pantalla del reproductor para que tdo se mantenga sincronizado en la app

    public static void togglePlayPause(Context context) {
        // Si no hay reproductor no hacemos nada
        if (mediaPlayerGlobal == null) {
            return;
        }
        // Si esta sonando la pausamos
        if (mediaPlayerGlobal.isPlaying()) {
            mediaPlayerGlobal.pause();
        }
        // Si esta pausada la reproducimos
        else {
            mediaPlayerGlobal.start();
        }
        // Actualizamos la notificacion del servicio
        Intent servicio = new Intent(context, MusicService.class);
        servicio.putExtra("titulo", tituloActual);
        servicio.putExtra("artista", artistaActual);
        servicio.putExtra("idCancion", idCancionActual);
        servicio.putExtra("ruta", rutaActual);
        // Iniciamos el servicio segun la version de Android
        // esto es importante ya que en Android 8+ nos puede dar errores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(servicio);
        } else {
            context.startService(servicio);
        }
        // Actualizamos el mini player en pantalla
        actualizarMiniPlayer(context);
        // Si el reproductor esta abierto actualizamos su boton play/pause
        if (PlayerActivity.instancia != null) {
            PlayerActivity.instancia.actualizarPlayPauseUI();
        }
    }
}