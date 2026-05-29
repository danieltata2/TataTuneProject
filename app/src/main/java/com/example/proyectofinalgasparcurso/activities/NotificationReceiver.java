package com.example.proyectofinalgasparcurso.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


public class NotificationReceiver extends BroadcastReceiver {

    //Este metodo sirve para controlar la musica desde los botones de la notificacion
    @Override
    public void onReceive(Context context, Intent intent) {
        // Obtenemos la accion enviada desde la notificacion
        // ya que nos dice que boton presiono el usuario
        String accion = intent.getAction();

        // Si no hay accin salimos
        if (accion == null){
            return;
        }


        // Revisamos que boton toco el usuario
        // y segun lo que toca se cambiara de accion
        switch (accion) {
            case "PLAY_PAUSE":
                // Verifica si hay una cancion cargada
                if (MiniPlayerManager.mediaPlayerGlobal != null) {
                    // Esto cambia el estado de la accion
                    // por ejemplo:
                    // si estaba sonando y toco el boton correspondiente --> pausa la musica
                    // si estaba pausado y toco el boton correspondiente--> reproduce la musica
                    MiniPlayerManager.togglePlayPause(context);
                }
                break;
            case "NEXT":
                // Pasa a la siguiente cancion
                MiniPlayerManager.siguiente();
                MusicService.actualizarNotificacion(context);
                break;
            case "PREV":
                // Vuelve a la cancion anterior
                MiniPlayerManager.anterior();
                MusicService.actualizarNotificacion(context);
                break;
        }
    }
}