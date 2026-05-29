package com.example.proyectofinalgasparcurso.activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.proyectofinalgasparcurso.R;

public class MusicService extends Service {


    //El CHANNEL_ID es importante porque en Android 8 o superior las notificaciones necesitan
    // un canal para poder mostrarlo en segundo plano sino la notificacion no funciona
    // ya que no sabria como manejar la notificacion
    public static final String CHANNEL_ID = "music_channel";

    //El NOTIFICATION_ID es como el nombre o numero de la notificacion para que Android la reconozca
    // ya que sin este id las notificaciones se duplicarian y generaria problemas como errores
    // de actualizacion, etc. Puede ser cualquier numero pero ese numero debe ser unico
    public static final int NOTIFICATION_ID = 1001;


    // No uso onBind() porque mi servicio no necesita comunicarse directamente con la activity
    // como en apps tipo whatsapp
    // Mi app mantiene la musica sonando en segundo plano
    // sin conexion directa con la pantalla osea solo escuchar musica en segundo plano
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*Este metodo se ejecuta cuando el servicio de musica empieza
    -recibimos los datos de la cancion y  creamos la notificacion del reproductor y
    -le dice a android que el servicio debe seguir funcionando aunque cierres la app*/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Si no llega informacion desde la activity no hacemos nada
        if (intent == null) {
            return START_NOT_STICKY;
        }
        // Obtenemos el titulo de la cancion enviado desde la activity
        String titulo = intent.getStringExtra("titulo");
        // Obtenemos el nombre del artista enviado desde la activity
        String artista = intent.getStringExtra("artista");
        // Si falta informacion importante no continuamos
        if (titulo == null || artista == null) {
            return START_NOT_STICKY;
        }
        // Creamos el canal de notificacion que es obligatorio en Android 8+ para mostrar las notificaciones
        crearCanal();
        // Mostramos la notificacion del reproductor de musica
        mostrarNotificacion();
        // Le decimos a Android que el servicio debe seguir activo
        // aunque la app se cierre o se elimine de recientes
        return START_STICKY;
    }

    //Este metodo es un metodo crea un canal de notificaciones
    // que es muy necesario en android para poder mostrar las notificaciones
    private void crearCanal() {
        // Solo se ejecuta en Android 8 o superiores
        // porque en esas versiones las notificaciones necesitan un canal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Creamos el canal de notificacion
            // CHANNEL_ID = identificador del canal
            // Musica = nombre que ve el sistema
            // IMPORTANCE_LOW = notificacion sin sonido ni interrupciones
            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID,
                    "Musica",
                    NotificationManager.IMPORTANCE_LOW
            );
            // Obtenemos el sistema de notificaciones de Android
            NotificationManager manager = getSystemService(NotificationManager.class);
            // Registramos el canal en el sistema
            manager.createNotificationChannel(canal);
        }
    }

    // Meotodo para mostrar la notificaion
    private void mostrarNotificacion() {
        String titulo = MiniPlayerManager.tituloActual;
        String artista = MiniPlayerManager.artistaActual;
        // Comprobamos si la musica esta reproduciendose en este momento
        boolean reproduciendo = MiniPlayerManager.mediaPlayerGlobal != null &&
                        MiniPlayerManager.mediaPlayerGlobal.isPlaying();
        // Variable que guarda el icono del boton play/pause
        int iconoPlayPause;
        // Si la musica esta reproduciendose mostramos el icono de pausa
        if (reproduciendo) {
            iconoPlayPause = R.drawable.ic_pause;
        }
        // Si la musica esta en pausa mostramos el icono de play
        else {
            iconoPlayPause = R.drawable.ic_play;
        }
        // Construimos la notificacion del reproductor de musica
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                // Titulo de la cancion
                .setContentTitle(titulo)
                // Nombre del artista
                .setContentText(artista)
                // Icono pequeño de la notificacion
                .setSmallIcon(R.drawable.ic_music_note)
                // Botones para poder controlarlos como previous, play/pause, next
                .addAction(R.drawable.ic_previous, "Prev", action("PREV", 1))
                .addAction(iconoPlayPause, "Play/Pause", action("PLAY_PAUSE", 2))
                .addAction(R.drawable.ic_next, "Next", action("NEXT", 3))
                // Esto hace que los botones se vean arriba
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2))
                // Evitamos que el usuario pueda cerrar la notificacion facilmente
                .setOngoing(true)
                // Evita sonidos o vibraciones al actualizar la notificacion
                .setOnlyAlertOnce(true)
                // Construimos la notificacion final
                .build();

        // Iniciamos el servicio en primer plano con la notificacion
        startForeground(NOTIFICATION_ID, notification);
    }

    //Sin el pending intent los botones no se ejecutarian ni harian nada
    private PendingIntent action(String accion, int requestCode) {
        // Creamos un Intent que apunta al notificationReceiver
        // aqui es donde se reciben las acciones de la notificacion
        Intent intent = new Intent(this, NotificationReceiver.class);
        // Le asignamos la accion que queremos enviar como prev, play, next, etc
        intent.setAction(accion);
        // Convertimos el Intent en un PendingIntent
        // Esto permite que la notificacion pueda ejecutar la accion
        // aunque la app este en segundo plano o cerrada
        return PendingIntent.getBroadcast(this, requestCode,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    public static void actualizarNotificacion(Context context) {
        Intent intent = new Intent(context, MusicService.class);
        intent.putExtra("titulo", MiniPlayerManager.tituloActual);
        intent.putExtra("artista", MiniPlayerManager.artistaActual);
        intent.putExtra("idCancion", MiniPlayerManager.idCancionActual);
        intent.putExtra("ruta", MiniPlayerManager.rutaActual);

        context.startService(intent);
    }

    /*Este metodo se ejecuta cuando el usuario cierra la app desde el menu de recientes
    Sirve para apagar la musica, liberar el mediaPlayer, quitar la notificacion y
    detener el servicio para que no siga funcionando en segundo plano
    */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // Se ejecuta cuando el usuario elimina la app de recientes
        // Paramos la musica si esta reproduciendose
        if (MiniPlayerManager.mediaPlayerGlobal != null) {
            MiniPlayerManager.mediaPlayerGlobal.stop();
            MiniPlayerManager.mediaPlayerGlobal.release();
            MiniPlayerManager.mediaPlayerGlobal = null;
        }
        // Eliminamos la notificacion en primer plano
        stopForeground(true);
        // Detenemos completamente el servicio
        stopSelf();
    }

}