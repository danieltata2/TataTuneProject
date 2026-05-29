package com.example.proyectofinalgasparcurso.activities;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.proyectofinalgasparcurso.R;
import com.example.proyectofinalgasparcurso.bbdd.AdminSQLite;
import com.example.proyectofinalgasparcurso.modelos.Cancion;
import java.util.ArrayList;
import android.os.Handler;

public class PlayerActivity extends AppCompatActivity {

    TextView txtTitulo, txtArtista, totalTimeTv, currentTimeTv;
    ImageView btnPlayPause, btnShuffle, btnRepeat;
    SeekBar seekBar;
    ImageView imgCover;
    MediaPlayer mediaPlayer;
    String rutaCancion, tituloCancion, artistaCancion;
    boolean modoAleatorio = false;
    Handler handler = new Handler();
    Handler timeHandler = new Handler();
    Runnable timeRunnable;
    int idCancionActual;
    int modoRepetir = 0;
    ArrayList<Cancion> listaActual;
    int indiceActual = 0;
    int idCancionInicial;
    ImageButton btnSleep, backBtn, btnNext, btnPrev, btnFav, btnEqualizer;
    CountDownTimer sleepTimer = null;
    boolean sleepActivo = false;
    int usuarioId;
    View root;
    public static PlayerActivity instancia;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //Guardo esta pantalla para poder llamarla desde cualquier otro sitio despues
        //instancia = this; sirve para guardar la pantalla actual del playeractivity en una variable global
        //para poder acceder a esa misma pantalla desde otras partes de la aplicacion
        // sin tener que volver a abrirla con un Intent
        //Entonces si la activity esta abierta no se crea otra nueva sino que
        // se reutiliza la misma y se puede controlar desde fuera
        //Por ejemplo: para pausar la musica, cambiar el boton play/pause o actualizar la interfaz
        // desde el mini player o la notificacion
        //Osea basicamente es una autoreferencia porque la clase se esta guardando a si
        //misma para poder ser usada desde otros sitios sin volver a crearla
        instancia = this;

        txtTitulo = findViewById(R.id.txtTituloPlayer);
        txtArtista = findViewById(R.id.txtArtistaPlayer);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnRepeat = findViewById(R.id.btnRepeat);
        backBtn = findViewById(R.id.backBtn);
        currentTimeTv = findViewById(R.id.currentTimeTv);
        totalTimeTv = findViewById(R.id.totalTimeTv);
        seekBar = findViewById(R.id.seekBar);
        btnSleep = findViewById(R.id.btnSleep);
        imgCover = findViewById(R.id.imgCover);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnFav = findViewById(R.id.btnFav);
        btnEqualizer = findViewById(R.id.btnEqualizer);
        root = findViewById(R.id.playerRoot);

        //Se crea el objeto de base de datos pasando la esta activity como contexto
        // para que pueda funcionar dentro de esa pantalla
        AdminSQLite bbdd = new AdminSQLite(this);

        //SharedPreferences es el almacenamiento interno de android
        //que sirve para guardar datos pequeños
        //Busca dentro de ese archivo el valor guardado (id)
        //El valor por defecto es  -1 si no se encuentra el valor guardado
        SharedPreferences prefs = getSharedPreferences("usuario", MODE_PRIVATE);
        usuarioId = prefs.getInt("id", -1);

        // Obtiene el id de la cancion enviado desde la activity anterior por intent
        // Si no viene el dato usa -1 como valor por defecto
        idCancionInicial = getIntent().getIntExtra("idCancion", -1);
        // Asigna ese id como la cancion actual que se va a reproducir
        //Sirve para no perder la cancion con la que entra el usuario
        // mientras va cambiando de cancion
        idCancionActual = idCancionInicial;
        //Obtiene la ruta del archivo de audio enviada desde la activity anterior
        rutaCancion = getIntent().getStringExtra("ruta");
        //Obtiene el titulo de la cancion enviado desde la activity anterior
        tituloCancion = getIntent().getStringExtra("titulo");
        //Obtiene el nombre del artista enviado desde la activity anterior
        artistaCancion = getIntent().getStringExtra("artista");

        //  Cargamos la lista
        cargarListaCompleta();

        // Si la lista de canciones esta vacia
        if (listaActual == null || listaActual.size() == 0) {
            //Cierra esta activity porque no hay nada que reproducir
            finish();
            return;
        }

        //Esto asegura que siempre haya algo que reproducir y
        //evita que nos quedemos sin escuchar una cancion
        //si no se recibio ninguna cancion
        if (idCancionInicial == -1) {
            //Toma la primera cancion de la lista como cancion por defecto
            idCancionInicial = listaActual.get(0).id;
            //asigna tambien esa cancion como la cancion a reproducir
            idCancionActual = idCancionInicial;
        }

        //buscamos el indice actual
        buscarIndiceActual();

        // Guardamos la lista actual de canciones en una variable global
        // para que pueda ser usada desde otras partes
        // y asi evitamos errores
        ReproductorGlobal.lista = listaActual;

        // Guardamos el indice de la cancion actual dentro de esa lista global
        // para saber en que posicion esta la reproduccion actualmente
        ReproductorGlobal.indice = indiceActual;


        // Evita errores si el indice se sale de la lista
        // y si pasa eso volvemos al inicio osea a la cancion 0
        if (indiceActual < 0 || indiceActual >= listaActual.size()) {
            indiceActual = 0;
        }

        // Si faltan datos de la cancion actual
        // los obtenemos directamente de la lista
        // esto es importante por si no se llega a
        // mapear los datos
        if (rutaCancion == null || tituloCancion == null || artistaCancion == null) {
            // Obtenemos la cancion actual de la lista
            Cancion c = listaActual.get(indiceActual);
            // Guardamos sus datos
            rutaCancion = c.ruta;
            tituloCancion = c.titulo;
            artistaCancion = c.artista;
        }

        // Actualizamos el icono al iniciar
        // para mostrar si la cancion esta en favoritos o no
        actualizarIconoFavorito(btnFav, bbdd, usuarioId, idCancionActual);

        // Evento del boton favoritos
        btnFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Si ya esta en favoritos la eliminamos
                if (bbdd.esFavorito(usuarioId, idCancionActual)) {
                    bbdd.eliminarFavorito(usuarioId, idCancionActual);
                }

                // Si no esta en favoritos la agregamos
                else {
                    bbdd.agregarFavorito(usuarioId, idCancionActual);
                }

                // Actualizamos el icono despues del cambio
                // para poder confirmar que este marcado
                actualizarIconoFavorito(btnFav, bbdd, usuarioId, idCancionActual);
            }
        });

        // Evento para abrir el ecualizador
        btnEqualizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Abrimos pantalla del equalizer
                Intent i = new Intent(PlayerActivity.this, EqualizerActivity.class);
                startActivity(i);
            }
        });

        // Evento del boton volver
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Minimiza el reproductor
                minimizarPlayer();
            }
        });

        // Boton siguiente cancion
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                siguienteCancion();
            }
        });

        // Boton cancion anterior
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anteriorCancion();
            }
        });

        // Evento del boton sleep timer
        btnSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mostramos opciones del temporizador
                mostrarOpcionesSleep();
            }
        });

        // Cargamos la cancion actual al abrir el player
        cargarCancion();

        // Verificamos si no existe reproductor
        // si no existe usamos el reproductor global
        if (mediaPlayer == null) {
            mediaPlayer = MiniPlayerManager.mediaPlayerGlobal;
        }

        //Actualizamos el mini‑player para que muestre la cancion que esta sonando
        MiniPlayerManager.idCancionActual = idCancionActual;
        MiniPlayerManager.rutaActual = rutaCancion;
        MiniPlayerManager.tituloActual = tituloCancion;
        MiniPlayerManager.artistaActual = artistaCancion;

        // Configuramos los botones y el seekbar
        configurarBotones();
        configurarSeekBar();

    }

    /*Este metodo obtiene la cancion actual desde la base de datos usando su id y
    carga sus datos como el titulo, artista y ruta en la pantalla.
    Luego usamos el MediaPlayer global del MiniPlayerManager:
    -si no existe lo crea y si la cancion es nueva la reproduce
    -si ya esta sonando solo actualiza la interfaz
    -al final sincroniza la informacion con el mini player y
    la notificacion para que toda la app muestre la misma cancion
     */
    private void cargarCancion() {

        // Creamos la conexion con la base de datos
        AdminSQLite admin = new AdminSQLite(this);
        // Se obtiene la cancion actual usando su id
        Cancion c = admin.obtenerCancionPorId(idCancionActual);
        // Si no existe la cancion salimos del metodo
        if (c == null) {
            return;
        }
        // Guardamos los datos de la cancion en variables
        tituloCancion = c.titulo;
        artistaCancion = c.artista;
        rutaCancion = c.ruta;
        // Mostramos el titulo y artista en pantalla
        txtTitulo.setText(tituloCancion);
        txtArtista.setText(artistaCancion);
        // Si no hay caratula ponemos el icono por defecto
        if (rutaCancion == null || rutaCancion.isEmpty()) {
            imgCover.setImageResource(R.drawable.ic_music_note);
        } else {
            // Si hay archivo cargamos la caratula
            // para que se vea bien el diseño
            cargarCaratula(rutaCancion);
        }
        // Obtenemos el reproductor global
        // osea lo que ya estaba sonando en el miniplayer
        // para actualizarlo despues
        MediaPlayer globalPlayer = MiniPlayerManager.mediaPlayerGlobal;
        // Usamos ese reproductor en esta pantalla
        mediaPlayer = globalPlayer;
        // Si no hay reproductor o la cancion es diferente
        if (globalPlayer == null || MiniPlayerManager.idCancionActual != idCancionActual) {
            // Si no existe lo creamos
            if (globalPlayer == null) {
                MiniPlayerManager.mediaPlayerGlobal = new MediaPlayer();
            }
            // Asignamos el reproductor global
            mediaPlayer = MiniPlayerManager.mediaPlayerGlobal;
            // Reproducimos la cancion
            reproducir();
        } else {
            // Si ya esta sonando actualizamos el boton
            if (mediaPlayer.isPlaying()) {
                btnPlayPause.setImageResource(R.drawable.ic_pause);
            } else {
                btnPlayPause.setImageResource(R.drawable.ic_play);
            }
            // Actualizamos barra de progreso y tiempo
            actualizarSeekBar();
            startUpdatingTime();
        }
        // Sincronizamos datos con el mini player
        MiniPlayerManager.idCancionActual = idCancionActual;
        MiniPlayerManager.tituloActual = tituloCancion;
        MiniPlayerManager.artistaActual = artistaCancion;
        MiniPlayerManager.rutaActual = rutaCancion;
        //Actualizamos la notificacion para saber los datos de la musica
        actualizarNotificacion();
        // Actualizamos el icono de favorito
        actualizarIconoFavorito(btnFav, admin, usuarioId, idCancionActual);
    }


    /*Este metodo configura los botones del reproductor.
    -primero aseguramos que existe un MediaPlayer o usamos el global si fuese necesario
    -luego asigna las acciones de los botones:
    -el boton play/pause alterna la reproduccion
    -el boton shuffle activa o desactiva el modo aleatorio cambiando su icono y
    -el boton repeat cambia entre tres modos: sin repetir, repetir una y repetir todas
    -tambien define el comportamiento cuando una cancion termina, decidiendo
    -si pasa a una cancion aleatoria o si avanza a la siguiente segun el modo seleccionado
     */

    private void configurarBotones() {

        // Si el reproductor no esta abierto o activo usamos el global del mini player
        if (mediaPlayer == null) {
            {
                mediaPlayer = MiniPlayerManager.mediaPlayerGlobal;
            }
            // Si aun asi no existe salimos
            if (mediaPlayer == null) {
                return;
            }
        }

        // boton play o pause
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Alternamos entre reproducir y pausar con un toggle
                // Un toggle es una linea que cambia un valor booleano entre true y false.
                MiniPlayerManager.togglePlayPause(PlayerActivity.this);
            }
        });

        // boton shuffle
        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cambia entre activado y desactivado
                modoAleatorio = !modoAleatorio;
                // Cambia el icono segun el estado
                if (modoAleatorio) {
                    btnShuffle.setImageResource(R.drawable.ic_shuffle_on);
                } else {
                    btnShuffle.setImageResource(R.drawable.ic_shuffle_off);
                }
            }
        });
        // Boton repeat (tiene 3 modos)
        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cambia entre 0, 1 y 2
                modoRepetir = (modoRepetir + 1) % 3;
                // Actualiza icono segun el modo
                switch (modoRepetir) {
                    case 0:
                        btnRepeat.setImageResource(R.drawable.ic_repeat_off);
                        break;
                    case 1:
                        btnRepeat.setImageResource(R.drawable.ic_repeat_one);
                        break;
                    case 2:
                        btnRepeat.setImageResource(R.drawable.ic_repeat_all);
                        break;
                }
            }
        });

        // Cuando la cancion termina
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Si esta en repetir una sola cancion
                if (modoRepetir == 1) {
                    reproducir();
                    return;
                }
                // Si esta en modo aleatorio
                if (modoAleatorio) {
                    reproducirCancionAleatoria();
                    return;
                }
                // Si esta en repetir todas
                if (modoRepetir == 2) {
                    siguienteCancion();
                    return;
                }
                // Por defecto pasa a la siguiente cancion
                siguienteCancion();
            }
        });
    }

    /*Este metodo se encarga de reproducir una cancion usando el MediaPlayer global
    -primero asegura que el reproductor exista y si no lo crea
    -luego carga la ruta de la cancion, reinicia el reproductor y empieza la reproduccion
    -actualiza la interfaz con el tiempo total y la barra de progreso y
    -activa la actualizacion del tiempo en la pantalla
    -tambien obtiene la sesion del audio osea su id para aplicar los efectos
    -como equalizer, bass boost y virtualizer
    -despues guarda la cancion en el historial y sincroniza los datos con el mini player
    -actualiza la notificacion iniciando el servicio en primer plano y
    -cambia el boton a pause
     */

    private void reproducir() {

        try {
            // si no existe el reproductor global lo creamos
            if (MiniPlayerManager.mediaPlayerGlobal == null) {
                MiniPlayerManager.mediaPlayerGlobal = new MediaPlayer();
            }
            // usamos el reproductor global de toda la app
            mediaPlayer = MiniPlayerManager.mediaPlayerGlobal;
            // si no hay ruta salimos
            if (rutaCancion == null || rutaCancion.isEmpty()) {
                return;
            }
            // reiniciamos el reproductor para cargar nueva cancion
            mediaPlayer.reset();
            // cargamos el archivo de audio
            mediaPlayer.setDataSource(rutaCancion);
            // preparamos la reproduccion
            mediaPlayer.prepare();
            // empezamos a reproducir
            mediaPlayer.start();
            // actualizamos el tiempo total en pantalla
            totalTimeTv.setText(formatTime(mediaPlayer.getDuration()));
            // ajustamos la barra de progreso
            seekBar.setMax(mediaPlayer.getDuration());
            // iniciamos la actualizacion del tiempo
            startUpdatingTime();
            // obtenemos la sesion de audio para efectos
            int sessionId = mediaPlayer.getAudioSessionId();
            // activamos el ecualizador
            //el priority 0 significa que el efecto se crea con prioridad normal
            //sin privilegios especiales sobre otros efectos de audio
            MiniPlayerManager.equalizerGlobal = new Equalizer(0, sessionId);
            MiniPlayerManager.equalizerGlobal.setEnabled(true);
            // activamos el bass boost
            MiniPlayerManager.bassGlobal = new BassBoost(0, sessionId);
            MiniPlayerManager.bassGlobal.setEnabled(true);
            // activamos el virtualizer
            MiniPlayerManager.virtualizerGlobal = new Virtualizer(0, sessionId);
            MiniPlayerManager.virtualizerGlobal.setEnabled(true);
            // guardamos en historial
            guardarHistorial();
            // sincronizamos los datos con el mini player
            MiniPlayerManager.idCancionActual = idCancionActual;
            MiniPlayerManager.tituloActual = tituloCancion;
            MiniPlayerManager.artistaActual = artistaCancion;
            MiniPlayerManager.rutaActual = rutaCancion;
            //Actualizamos la notificaciones
            actualizarNotificacion();
            // iniciamos el servicio de notificacion en segundo plano
            Intent servicio = new Intent(this, MusicService.class);
            servicio.putExtra("titulo", tituloCancion);
            servicio.putExtra("artista", artistaCancion);
            servicio.putExtra("idCancion", idCancionActual);
            servicio.putExtra("ruta", rutaCancion);
            // Si el Android es version 8  o superior
            // usamos startForegroundService porque Android obliga a mostrar una notificacion
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(servicio);
            } else {
                // En versiones antiguas se puede iniciar el servicio normal sin restricciones
                startService(servicio);
            }
            // cambiamos el boton a pause
            btnPlayPause.setImageResource(R.drawable.ic_pause);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Este metodo elige una cancion aleatoria de la base de datos y la reproduce
     -primero consulta la base de datos para obtener una cancion random y si no existe sale
     -del metodo. Luego guarda sus datos como el id, titulo, artista y ruta y
     -busca en la lista actual la posicion de esa cancion
     -despues actualiza la interfaz con el titulo y artista y
     -carga la caratula y sincroniza los datos con el mini player
     -por ultimo reproduce la cancion y actualiza la notificacion y refresca
     -el mini player en la pantalla
     */

    private void reproducirCancionAleatoria() {
        // Obtenemos una cancion aleatoria de la base de datos
        AdminSQLite admin = new AdminSQLite(this);
        Cancion c = admin.obtenerCancionAleatoria();
        // Si no hay cancion salimos del metodo
        if (c == null) {
            return;
        }
        // Guardamos los datos de la cancion seleccionada
        idCancionActual = c.id;
        tituloCancion = c.titulo;
        artistaCancion = c.artista;
        rutaCancion = c.ruta;

        //recorremes toda la lista de canciones hasta encontrar la que tiene
        // el mismo id que la cancion actual y cuando la encuentra
        // guarda su posicion
        for (int i = 0; i < listaActual.size(); i++) {
            if (listaActual.get(i).id == idCancionActual) {
                indiceActual = i;
                break;
            }
        }
        // Actualizamos los textos en la pantalla
        // con el titulo y el artista de la cancion
        txtTitulo.setText(tituloCancion);
        txtArtista.setText(artistaCancion);
        // Guardamos la ruta en el mini player y cargamos la caratula
        MiniPlayerManager.rutaActual = rutaCancion;
        cargarCaratula(rutaCancion);
        // Sincronizamos los datos con el mini player
        MiniPlayerManager.idCancionActual = idCancionActual;
        MiniPlayerManager.tituloActual = tituloCancion;
        MiniPlayerManager.artistaActual = artistaCancion;
        // Reproducimos la cancion
        reproducir();
        // Actualizamos la notificacion
        actualizarNotificacion();
    }


    /*Este metodo guarda la cancion actual en el historial del usuario
    -primero verifica si hay un usuario valido y si no lo hay sale del metodo
    -luego crea la conexion con la base de datos y registra el id del usuario
    -junto con el id de la cancion que se esta reproduciendo
     */
    private void guardarHistorial() {
        // Si no hay usuario valido salimos del metodo
        if (usuarioId == -1) {
            return;
        }

        // Creamos el acceso a la base de datos
        AdminSQLite admin = new AdminSQLite(this);

        // Guardamos en el historial el usuario y la cancion actual
        admin.guardarHistorial(usuarioId, idCancionActual);
    }


    // Configura la barra de progreso de la cancion
    // osea el tiempo de la cancion
    private void configurarSeekBar() {
        // Escuchamos los cambios de la barra de progreso
        // esto es importante para la barra
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Si el usuario esta moviendo la barra y el reproductor existe
                if (fromUser && mediaPlayer != null) {
                    // Movemos la cancion a esa posicion
                    // por ejemplo del minuto 1 al minuto 2
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /*Este metodo configura y mantiene actualizada la barra de progreso de la cancion
     primero ajustara la barra segun la duracion total de la musica y
     muestrara ese tiempo en pantalla. Luego usa un handler para ejecutar
     una tarea que se repite cada 0.5 segundos y actualiza la barra para que
     avance al mismo tiempo que suena la musica
     */

    private void actualizarSeekBar() {
        // Ajustamos el maximo de la barra segun la duracion de la cancion
        seekBar.setMax(mediaPlayer.getDuration());
        // Mostramos el tiempo total de la cancion
        totalTimeTv.setText(formatTime(mediaPlayer.getDuration()));
        // Creamos una tarea que se repite cada cierto tiempo
        // El Handler sirve para actualizar la barra de la musica cada 0.5 segundos
        // y asi mantenerla sincronizada con la cancion mientras suena
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // Actualizamos la posicion actual de la cancion en la barra
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    // Volvemos a ejecutar esto cada 0.5 segundo para que se actualice
                    handler.postDelayed(this, 500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 500);
    }

    //onDestroy sirve para limpiar y cerrar tdo cuando la pantalla se destruye
    //o se cierra para liberar espacio
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cuando la activity se destruye eliminamos la instancia
        // para evitar usar una pantalla que ya no existe
        // asi liberamos espacio
        instancia = null;
    }


    /*Este metodo actualiza el icono del boton de favoritos segun si la cancion esta guardada o no en la base de datos
    -primero usa la base de datos para comprobar si la cancion del usuario es favorita y si
    lo es cambia el icono del boton a favorito activado.
    Si no lo es lo cambia a favorito desactivado. El ImageView se pasa como parametro para poder modificar directamente ese boton desde el metodo
    */
    private void actualizarIconoFavorito(ImageView btnFav, AdminSQLite admin, int usuarioId, int idCancion) {
        // Comprobamos si la cancion esta en favoritos del usuario
        if (admin.esFavorito(usuarioId, idCancion)) {
            // Si es favorito mostramos icono activado
            btnFav.setImageResource(R.drawable.ic_fav_on);
        } else {
            // Si no es favorito mostramos icono desactivado
            btnFav.setImageResource(R.drawable.ic_fav_off);
        }
    }

    /*Este metodo carga todas las canciones desde la base de datos y las guarda en la lista del reproductor
    -primero crea una conexion con la base de datos usando AdminSQLite y
    luego obtiene la lista completa de canciones. Guarda esa lista en listaActual para
    poder usarla en el reproductor como siguiente, anterior, aleatorio, etc
    */
    private void cargarListaCompleta() {
        AdminSQLite admin = new AdminSQLite(this);
        listaActual = admin.obtenerListaCompleta();
    }


    /*Este metodo busca en que posicion de la lista esta la cancion inicial
    -Recorremos toda la lista de canciones y comparamos el id de cada una con
    el id de la cancion que se quiere reproducir.Cuando lo encuentramos guardamos
    su posicion en la variable indiceActual y se detiene el bucle para no seguir buscando*/
    private void buscarIndiceActual() {
        // Recorremos la lista de canciones
        for (int i = 0; i < listaActual.size(); i++) {
            // Si encontramos la cancion inicial en la lista
            if (listaActual.get(i).id == idCancionInicial) {
                // Guardamos su posicion
                indiceActual = i;
                // Salimos del bucle porque ya la encontramos
                break;
            }
        }
    }

    /*Este metodo reproduce una cancion concreta y actualiza toda la interfaz del reproductor
    -primero detiene la actualizacion del tiempo anterior para evitar que siga corriendo el
    contador de la cancion vieja. Luego guardamos los datos de la nueva cancion como el id,
    titulo, artista y ruta y actualizamos los textos y la caratula en la pantalla
    -reiniciamos los tiempos a 00:00 y reproducimos la nueva cancion y
    -actualizamos la notificacion. Tambien sincronizamos los datos con el mini player
     y actualizamos el icono de favorito y vuelve a iniciar la actualizacion del tiempo
     para la nueva cancion*/

    private void reproducirCancion(Cancion c) {
        // paramos la actualizacion del tiempo anterior si estaba activa
        if (timeHandler != null && timeRunnable != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
        // guardamos los datos de la cancion actual
        idCancionActual = c.id;
        tituloCancion = c.titulo;
        artistaCancion = c.artista;
        rutaCancion = c.ruta;
        // actualizamos los textos de la pantalla
        txtTitulo.setText(tituloCancion);
        txtArtista.setText(artistaCancion);
        // cargamos la imagen de la cancion
        cargarCaratula(rutaCancion);
        // reiniciamos el tiempo en pantalla
        totalTimeTv.setText("00:00");
        currentTimeTv.setText("00:00");
        // empezamos a reproducir la cancion
        reproducir();
        // actualizamos la notificacion
        actualizarNotificacion();
        // sincronizamos datos con el mini player
        MiniPlayerManager.idCancionActual = idCancionActual;
        MiniPlayerManager.tituloActual = tituloCancion;
        MiniPlayerManager.artistaActual = artistaCancion;
        MiniPlayerManager.rutaActual = rutaCancion;
        // actualizamos el icono de favorito
        ImageView btnFav = findViewById(R.id.btnFav);
        actualizarIconoFavorito(btnFav, new AdminSQLite(this), usuarioId, idCancionActual);
        // reiniciamos la actualizacion del tiempo en pantalla
        startUpdatingTime();
    }


    /*Este metodo pasa a la siguiente cancion de la lista
    -primero detiene la actualizacion del tiempo para evitar errores
    -luego aumentamos el indice de la cancion y si llega al final vuelve al inicio
    -despues reproduce la nueva cancion y actualiza la notificacion y
    -cambia la caratula. Por ultimo sincronizamos el indice con el reproductor
    -global y reinicia la actualizacion del tiempo en pantalla.
    */
    private void siguienteCancion() {
        // detenemos la actualizacion del tiempo si estaba activa
        if (timeHandler != null && timeRunnable != null) {
            // cancelamos la tarea que actualiza la barra y el tiempo
            timeHandler.removeCallbacks(timeRunnable);
        }
        // avanzamos al siguiente indice
        indiceActual++;
        // si llegamos al final volvemos al inicio
        if (indiceActual >= listaActual.size()) {
            indiceActual = 0;
        }
        // reproducimos la siguiente cancion
        reproducirCancion(listaActual.get(indiceActual));
        // actualizamos la notificacion
        actualizarNotificacion();
        // cargamos la caratula de la nueva cancion
        cargarCaratula(listaActual.get(indiceActual).ruta);
        // sincronizamos el indice con el reproductor global
        ReproductorGlobal.indice = indiceActual;
        // reiniciamos la actualizacion del tiempo en pantalla
        startUpdatingTime();
    }

    /*Este metodo va a la cancion anterior de la lista
    -primero detenemos la actualizacion del tiempo para evitar errores
    -luego reduce el indice de la cancion y si esta en la primera
     pasara a la ultima para que la lista sea circular y
     despues reproducira la cancion anterior y actualiza la notificacion y
     cambia la caratula. Por ultimo sincronizamos tambien el indice con el reproductor
     global y reiniciamos la actualizacion del tiempo
     */
    private void anteriorCancion() {
        // detenemos la actualizacion del tiempo si estaba activa
        if (timeHandler != null && timeRunnable != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
        // retrocedemos al indice anterior
        indiceActual--;
        // si estamos en la primera cancion volvemos a la ultima
        if (indiceActual < 0) {
            indiceActual = listaActual.size() - 1;
        }
        // reproducimos la cancion anterior
        reproducirCancion(listaActual.get(indiceActual));
        actualizarNotificacion();
        // cargamos la caratula de la cancion
        cargarCaratula(listaActual.get(indiceActual).ruta);
        // sincronizamos el indice con el reproductor global
        ReproductorGlobal.indice = indiceActual;
        // reiniciamos la actualizacion del tiempo en pantalla
        startUpdatingTime();
    }


    /*Estos metodos sirven para controlar el reproductor desde
    el mini player
    -simplemente llaman a los metodos de siguiente y anterior
    cancion para cambiar la musica sin necesidad de abrir
    otra pantalla
     */
    public void siguienteDesdeMiniPlayer() {
        siguienteCancion();
    }

    public void anteriorDesdeMiniPlayer() {
        anteriorCancion();
    }

    private void activarSleepTimer(int minutos) {

        // Si ya hay un temporizador activo lo cancelamos
        // para evitar que haya dos timers funcionando a la vez
        if (sleepTimer != null) {
            sleepTimer.cancel();
        }

        // Convertimos los minutos a milisegundos
        long tiempo = minutos * 60 * 1000;

        // Creamos un CountDownTimer que cuenta hacia atras cada 1 segundo
        sleepTimer = new CountDownTimer(tiempo, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                // Aqui se obtiene el tiempo restante en minutos y segundos
                // aunque en este codigo no se este mostrando en pantalla
                long m = millisUntilFinished / 60000;
                long s = (millisUntilFinished % 60000) / 1000;
            }

            @Override
            public void onFinish() {

                // Cuando el temporizador termina desactivamos el estado
                sleepActivo = false;

                try {
                    // Detenemos el reproductor global si existe
                    // y liberamos recursos
                    if (MiniPlayerManager.mediaPlayerGlobal != null) {
                        MiniPlayerManager.mediaPlayerGlobal.stop();
                        MiniPlayerManager.mediaPlayerGlobal.release();
                        MiniPlayerManager.mediaPlayerGlobal = null;
                    }

                    // Detenemos el servicio de musica en segundo plano
                    stopService(new Intent(PlayerActivity.this, MusicService.class));

                    // Cerramos la pantalla del reproductor
                    finish();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        // Marcamos que el sleep timer esta activo
        sleepActivo = true;
    }


    /*Este metodo actualiza la notificacion del reproductor enviando al servicio
    en segundo plano los datos de la cancion como el titulo, artista y ruta
     y dependiendo de la version de android vamos a iniciar
     el servicio como servicio normal para mantener la notificacion activa y actualizada
     */
    private void actualizarNotificacion() {

        // Creamos un intent para comunicarnos con
        // el servicio de musica en segundo plano
        Intent servicio = new Intent(this, MusicService.class);

        // Enviamos los datos de la cancion actual al servicio
        // para que la notificacion muestre la informacion correcta
        servicio.putExtra("titulo", tituloCancion);
        servicio.putExtra("artista", artistaCancion);
        servicio.putExtra("ruta", rutaCancion);

        // Dependiendo de la version de Android usamos un metodo u otro
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // En Android 8 o superior es obligatorio usar foreground service
            startForegroundService(servicio);
        } else {
            // En versiones antiguas se puede iniciar como servicio normal
            startService(servicio);
        }
    }


    //Este metodo sirve para cancelar el time sleep
    private void cancelarSleepTimer() {
        // Si existe un temporizador activo lo cancelamos
        if (sleepTimer != null) {
            sleepTimer.cancel();
            sleepTimer = null;
        }
        // Marcamos que el sleep timer ya no esta activo
        sleepActivo = false;
    }

    /*Este metodo muestra un dialogo con varias opciones de tiempo para el temporizador de apagado
     -segun la opcion seleccionada iniciara un sleep timer con diferentes
     duraciones o lo cancela si el usuario lo elige
     */
    private void mostrarOpcionesSleep() {

        // esta es la lista de opciones que se mostraran en el dialogo
        String[] opciones = {
                "5 minutos",
                "10 minutos",
                "20 minutos",
                "30 minutos",
                "Cancelar temporizador"
        };

        // Creamos un dialogo con las opciones del sleep timer
        new AlertDialog.Builder(this)
                .setTitle("Temporizador de apagado")
                .setItems(opciones, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Segun la opcion seleccionada ejecutaremos la accion
                        switch (which) {

                            case 0:
                                activarSleepTimer(5);
                                break;

                            case 1:
                                activarSleepTimer(10);
                                break;

                            case 2:
                                activarSleepTimer(20);
                                break;

                            case 3:
                                activarSleepTimer(30);
                                break;

                            case 4:
                                cancelarSleepTimer();
                                break;
                        }
                    }
                })
                .show();
    }

    /*Este metodo carga la caratula de la cancion desde el archivo de audio
     -si la cancion tiene imagen la convierte en bitmap y la muestra en pantalla
     y usa sus colores para cambiar el fondo del reproductor
      -si no hay caratula o ocurre un error mostrara la imagen por defecto y tambien un color por defecto
     */
    private void cargarCaratula(String path) {
        try {
            // Crea un lector de informacion del archivo de audio
            // MediaMetadataRetriever es una clase que sirve para leer
            // informacion de archivos multimedia como canciones o videos.
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            // Carga la ruta de la cancion
            retriever.setDataSource(path);
            // Obtiene la imagen dentro del mp3
            byte[] data = retriever.getEmbeddedPicture();
            // Si la cancion tiene caratula
            if (data != null) {
                // Convierte los bytes de la imagen en un Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                // Muestra la caratula en el ImageView
                imgCover.setImageBitmap(bitmap);
                // Obtenemos el color dominante de la imagen
                androidx.palette.graphics.Palette.from(bitmap).generate(palette -> {
                    int colorDominante = palette.getDominantColor(
                            // Color por defecto si no encuentra uno
                            getColor(R.color.accent_orange_soft)
                    );
                    // Cambia el fondo usando el color dominante de la caratula
                    root.setBackgroundColor(colorDominante);
                });
            } else {
                // Si no hay caratula nos muestra la imagen por defecto
                imgCover.setImageResource(R.drawable.ic_music_note);
                // Coloca un color de fondo por defecto
                root.setBackgroundColor(getColor(R.color.accent_orange_soft));
            }
            // Libera memoria del retriever
            retriever.release();
        } catch (Exception e) {
            // Si ocurre algun error, pone imagen por defecto
            imgCover.setImageResource(R.drawable.ic_music_note);
            // Y tambien coloca fondo por defecto
            root.setBackgroundColor(getColor(R.color.accent_orange_soft));
        }
    }


    /*Este metodo se ejecuta cuando la pantalla del reproductor vuelve a estar activa
    -sirve para sincronizar la interfaz con el estado actual de la musica esto actualiza
     el boton de play/pause, el icono de favoritos, el modo aleatorio y el modo de repeticion
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Vuelve a usar el reproductor que ya estaba reproduciendo musica
        mediaPlayer = MiniPlayerManager.mediaPlayerGlobal;
        // Cambia el icono dependiendo si la musica esta sonando o pausada
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            // Muestra el icono de pause
            btnPlayPause.setImageResource(R.drawable.ic_pause);
        } else {
            // Muestra el icono de play
            btnPlayPause.setImageResource(R.drawable.ic_play);
        }

        // Abrimos la conexion con sqlite
        AdminSQLite admin = new AdminSQLite(this);

        // Se actualiza el icono de favorito
        actualizarIconoFavorito(btnFav, admin, usuarioId, idCancionActual);

        // Si el modo aleatorio esta activado mostramos icono encendido
        // si no mostramos icono apagado
        if (modoAleatorio) {
            btnShuffle.setImageResource(R.drawable.ic_shuffle_on);
        } else {
            btnShuffle.setImageResource(R.drawable.ic_shuffle_off);
        }

        // Actualiza el icono repeat segun el modo seleccionado
        switch (modoRepetir) {

            case 0:
                btnRepeat.setImageResource(R.drawable.ic_repeat_off);
                break;

            case 1:
                btnRepeat.setImageResource(R.drawable.ic_repeat_one);
                break;

            case 2:
                btnRepeat.setImageResource(R.drawable.ic_repeat_all);
                break;
        }
    }

    //Este metodo sirve para que el reproductor pueda:
    //Cambiar de cancion sin abrir otra pantalla desde mini player, notificacion, otra Activity
    //tambien recibe acciones como next/previous mientras ya esta abierto
    //nueva cancion directa,etc
    //Sin este metodo al abrir una nueva musica se generarian nuevos playerActivity
    //lo cual nos consumiria memoria e iria la aplicacion mas lenta
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Reemplaza el intent anterior por el nuevo
        setIntent(intent);
        // Obtenemos el id de la cancion que viene en el intent
        int nuevoId = intent.getIntExtra("idCancion", -1);
        // Si viene una cancion y es diferente a la actual
        if (nuevoId != -1 && nuevoId != idCancionActual) {
            // Actualiza la cancion actual
            idCancionActual = nuevoId;
            // Recarga la cancion nueva en el player
            cargarCancion();
            return;
        }
        // Obtenemos la accion (next o prev) desde el mini player o notificacion
        String accion = intent.getStringExtra("accion");
        // Si la accion es siguiente
        if ("next".equals(accion)) {
            siguienteDesdeMiniPlayer();
        }
        // Si la accion es anterior
        if ("prev".equals(accion)) {
            anteriorDesdeMiniPlayer();
        }
    }


    //Este metodo sirve para cerrar el reproductor
    // volver a la pantalla anterior y hacerlo sin animacion
    private void minimizarPlayer() {
        // Se cierra el playerActivity y vuelve a la anterior
        finish();
        // Esto elimina la animacion al cerrar la pantalla basicamente
        //es una transicion sin efecto tipo youtube music
        overridePendingTransition(0, 0);
    }


    //Este metodo hace que el tiempo de la cancion se vaya moviendo en pantalla
    //osea que la barra de progreso avance sola
    // y tdo se actualice sin hacer nada
    private void startUpdatingTime() {
        // Si no hay MediaPlayer activo no se puede actualizar el tiempo
        if (mediaPlayer == null) {
            return;
        }

        // Creamos una tarea que se ejecutara en bucle
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    //Obtiene la posicion actual de reproduccion en milisegundos
                    int pos = mediaPlayer.getCurrentPosition();
                    // Convierte ese tiempo a formato MM:SS y lo muestra en pantalla
                    currentTimeTv.setText(formatTime(pos));
                    // Actualiza la posicion del seekbar
                    seekBar.setProgress(pos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Vuelve a ejecutar este mismo Runnable cada 0.5 segundos
                timeHandler.postDelayed(this, 500);
            }
        };

        // Inicia la ejecucion de la tarea por primera vez
        timeHandler.post(timeRunnable);
    }

    //Este metodo toma el tiempo en milisegundos que usa android y lo transforma a
    //minutos y segundos
    private String formatTime(int millis) {
        // Convierte milisegundos a minutos por ejemplo 1 min = 60 segundos
        int minutes = (millis / 1000) / 60;
        // Obtiene los segundos restantes despues de calcular los minutos
        int seconds = (millis / 1000) % 60;
        // Devuelve el tiempo en formato MM:SS por ejemeplo 03:45
        return String.format("%02d:%02d", minutes, seconds);
    }

    //Este metodo cambia el icono del boton play/pause segun si
    //la musica esta reproduciendose o no
    //y para que el boton siempre este sincronizado con la musica
    // Por ejemplo:
    //Como volver a la pantalla
    //Usar el mini player
    //Cambiar de cancion
    public void actualizarPlayPauseUI() {
        // Si el reproductor existe y esta reproduciendo musica
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            // Cambia el boton a icono de pause
            btnPlayPause.setImageResource(R.drawable.ic_pause);
        } else {
            // Si no esta reproduciendo muestra icono de play
            btnPlayPause.setImageResource(R.drawable.ic_play);
        }
    }
}