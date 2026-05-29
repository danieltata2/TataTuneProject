package com.example.proyectofinalgasparcurso.bbdd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.proyectofinalgasparcurso.modelos.Cancion;
import com.example.proyectofinalgasparcurso.modelos.Playlist;
import com.example.proyectofinalgasparcurso.modelos.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminSQLite extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "music.db";
    private static final int VERSION_BD = 3;

    public AdminSQLite(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        db.execSQL(
                "CREATE TABLE usuarios (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nombre TEXT, " +
                        "email TEXT UNIQUE, " +
                        "password TEXT, " +
                        "foto TEXT)"
        );


        db.execSQL(
                "CREATE TABLE cancion (" +
                        "id INTEGER PRIMARY KEY, " +
                        "titulo TEXT, " +
                        "artista TEXT, " +
                        "ruta TEXT)"
        );


        db.execSQL(
                "CREATE TABLE playlist (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "usuario_id INTEGER, " +
                        "nombre TEXT, " +
                        "imagen TEXT, " +
                        "FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE)"
        );


        db.execSQL(
                "CREATE TABLE playlist_cancion (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "playlist_id INTEGER, " +
                        "cancion_id INTEGER, " +
                        "FOREIGN KEY(playlist_id) REFERENCES playlist(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(cancion_id) REFERENCES cancion(id) ON DELETE CASCADE)"
        );


        db.execSQL(
                "CREATE TABLE favorito (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "usuario_id INTEGER, " +
                        "cancion_id INTEGER, " +
                        "FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(cancion_id) REFERENCES cancion(id) ON DELETE CASCADE)"
        );


        db.execSQL(
                "CREATE TABLE historial (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "usuario_id INTEGER, " +
                        "cancion_id INTEGER, " +
                        "fecha TEXT, " +
                        "FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(cancion_id) REFERENCES cancion(id) ON DELETE CASCADE)"
        );
    }



    /*El metodo onUpgrade se ejecuta cuando cambia la version de la base de datos
    en este caso eliminamos las tablas antiguas y las vuelve a crear para actualizar la estructura
    */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        db.execSQL("DROP TABLE IF EXISTS cancion");
        db.execSQL("DROP TABLE IF EXISTS playlist");
        db.execSQL("DROP TABLE IF EXISTS playlist_cancion");
        db.execSQL("DROP TABLE IF EXISTS favorito");
        db.execSQL("DROP TABLE IF EXISTS historial");

        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Esto activa las claves foraneas en sqlite
        // y permite que se respeten las relaciones entre tablas
        // por ejemplo: si borras un usuario puede afectar a sus playlists o canciones relacionadas
        db.setForeignKeyConstraintsEnabled(true);
    }


    /*Datos importantes:

    -Uso long porque el metodo de insert() de sqlite devuelve ese tipo de dato
    -El Cursor es equivalente al resultSet en java ya que nos permite recorrer los resultados
    de una consulta sql en sqlite
    -El statement es una consulta que se ejecuta directamente sin parametros y
    en android seria aquivalente a:

        db.execSQL("DELETE FROM usuarios WHERE id=1");
        Cursor cursor = db.rawQuery("SELECT * FROM usuarios", null);

    -El preparedStatement es una consulta preparada con parametros y
    en adroid es equivalente a esto:
        Cursor cursor = db.rawQuery(
        "SELECT * FROM usuarios WHERE email=? AND password=?",
        new String[]{email, password});

    -Esto es un array de strings que contiene los valores que sustituyen los ? en la consulta
    new String[]{String.valueOf(usuarioId), String.valueOf(cancionId)}
    Por ejemplo:
    ["5", "10"]

    -El contentValues es un objeto que sirve para guardar datos objeto,valor
    antes de insertarlos en sqlite
    Por ejemplo:
        ContentValues values = new ContentValues();
        values.put("nombre", "Daniel");
        values.put("email", "daniel@gmail.com");
        values.put("password", "123456");
        values.put("foto", "/storage/perfil.jpg");

     */
    public boolean registrarUsuario(String nombre, String email, String password, String foto) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("email", email);
        values.put("password", password);
        values.put("foto", foto);

        long result = db.insert("usuarios", null, values);
        db.close();

        return result != -1;
    }

    public Usuario loginUsuario(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, nombre, email, password, foto FROM usuarios WHERE email=? AND password=?",
                new String[]{email, password}
        );

        if (cursor.moveToFirst()) {
            Usuario u = new Usuario(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4)
            );
            cursor.close();
            db.close();
            return u;
        }

        cursor.close();
        db.close();
        return null;
    }


    public boolean agregarFavorito(int usuarioId, int cancionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("usuario_id", usuarioId);
        values.put("cancion_id", cancionId);
        long result = db.insert("favorito", null, values);
        db.close();
        return result != -1;
    }


    public boolean eliminarFavorito(int usuarioId, int cancionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("favorito",
                "usuario_id=? AND cancion_id=?",
                new String[]{String.valueOf(usuarioId), String.valueOf(cancionId)});
        db.close();
        return result > 0;
    }


    public boolean esFavorito(int usuarioId, int cancionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM favorito WHERE usuario_id=? AND cancion_id=?",
                new String[]{String.valueOf(usuarioId), String.valueOf(cancionId)}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public void borrarHistorialCompleto(int usuarioId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("historial", "usuario_id=?", new String[]{String.valueOf(usuarioId)});
        db.close();
    }




    public void insertarCancionEnPlaylist(int idPlaylist, int idCancion) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO playlist_cancion (playlist_id, cancion_id) VALUES (" +
                idPlaylist + ", " + idCancion + ")");
        db.close();
    }

    public ArrayList<Cancion> obtenerTodasLasCanciones() {
        ArrayList<Cancion> lista = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM cancion", null);

        while (cursor.moveToNext()) {
            lista.add(new Cancion(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            ));
        }

        cursor.close();
        db.close();

        return lista;
    }

    public boolean eliminarFavoritoPorUsuarioYCancion(int usuarioId, int cancionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(
                "favorito",
                "usuario_id=? AND cancion_id=?",
                new String[]{String.valueOf(usuarioId), String.valueOf(cancionId)}
        );
        db.close();
        return result > 0;
    }

    public ArrayList<Cancion> obtenerFavoritosDeUsuario(int usuarioId) {

        ArrayList<Cancion> lista = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT c.id, c.titulo, c.artista, c.ruta " +
                        "FROM cancion c " +
                        "INNER JOIN favorito f ON c.id = f.cancion_id " +
                        "WHERE f.usuario_id=?",
                new String[]{String.valueOf(usuarioId)}
        );

        while (cursor.moveToNext()) {
            lista.add(new Cancion(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            ));
        }

        cursor.close();
        db.close();

        return lista;
    }

    public ArrayList<Cancion> obtenerHistorial(int usuarioId, ArrayList<Integer> idsHistorial) {

        ArrayList<Cancion> historial = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT h.id, c.id, c.titulo, c.artista, c.ruta " +
                        "FROM historial h " +
                        "INNER JOIN cancion c ON h.cancion_id = c.id " +
                        "WHERE h.usuario_id=? " +
                        "ORDER BY h.fecha DESC",
                new String[]{String.valueOf(usuarioId)}
        );

        while (cursor.moveToNext()) {

            idsHistorial.add(cursor.getInt(0));

            historial.add(new Cancion(
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4)
            ));
        }

        cursor.close();
        db.close();

        return historial;
    }

    public ArrayList<Cancion> obtenerCancionesDePlaylist(int idPlaylist) {

        ArrayList<Cancion> lista = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT c.id, c.titulo, c.artista, c.ruta " +
                        "FROM cancion c " +
                        "INNER JOIN playlist_cancion pc ON c.id = pc.cancion_id " +
                        "WHERE pc.playlist_id = ?",
                new String[]{String.valueOf(idPlaylist)}
        );

        while (cursor.moveToNext()) {
            lista.add(new Cancion(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            ));
        }

        cursor.close();
        db.close();

        return lista;
    }

    public ArrayList<Cancion> obtenerListaCompleta() {

        ArrayList<Cancion> lista = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, titulo, artista, ruta FROM cancion",
                null
        );

        while (cursor.moveToNext()) {
            lista.add(new Cancion(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            ));
        }

        cursor.close();
        db.close();

        return lista;
    }


    public void insertarCancionesDelTelefono(List<Cancion> lista) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (Cancion c : lista) {
            db.execSQL("INSERT OR IGNORE INTO cancion (id, titulo, artista, ruta) VALUES (" +
                    c.id + ", '" + c.titulo.replace("'", "''") + "', '" +
                    c.artista.replace("'", "''") + "', '" +
                    c.ruta.replace("'", "''") + "')");
        }

        db.close();
    }

    public boolean eliminarPlaylist(int idPlaylist, int usuarioId) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DELETE FROM playlist_cancion WHERE playlist_id = " + idPlaylist);

        int result = db.delete(
                "playlist",
                "id=? AND usuario_id=?",
                new String[]{String.valueOf(idPlaylist), String.valueOf(usuarioId)}
        );

        db.close();

        return result > 0;
    }

    public boolean eliminarCancionDePlaylist(int idPlaylist, int idCancion) {

        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                "playlist_cancion",
                "playlist_id=? AND cancion_id=?",
                new String[]{String.valueOf(idPlaylist), String.valueOf(idCancion)}
        );

        db.close();

        return result > 0;
    }

    public Cancion obtenerCancionAleatoria() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, titulo, artista, ruta FROM cancion ORDER BY RANDOM() LIMIT 1",
                null
        );

        Cancion c = null;

        if (cursor.moveToFirst()) {
            c = new Cancion(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            );
        }

        cursor.close();
        db.close();

        return c;
    }


    public Cancion obtenerCancionPorId(int idCancion) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, titulo, artista, ruta FROM cancion WHERE id=?",
                new String[]{String.valueOf(idCancion)}
        );

        Cancion c = null;

        if (cursor.moveToFirst()) {
            c = new Cancion(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            );
        }

        cursor.close();
        db.close();

        return c;
    }

    public void guardarHistorial(int usuarioId, int idCancion) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(
                "historial",
                "usuario_id=? AND cancion_id=?",
                new String[]{String.valueOf(usuarioId), String.valueOf(idCancion)}
        );

        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        ContentValues values = new ContentValues();
        values.put("usuario_id", usuarioId);
        values.put("cancion_id", idCancion);
        values.put("fecha", fecha);

        db.insert("historial", null, values);

        db.close();
    }

    public ArrayList<Playlist> obtenerPlaylists(int usuarioId) {

        ArrayList<Playlist> lista = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, nombre, imagen FROM playlist WHERE usuario_id=?",
                new String[]{String.valueOf(usuarioId)}
        );

        while (cursor.moveToNext()) {
            lista.add(new Playlist(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2)
            ));
        }

        cursor.close();
        db.close();

        return lista;
    }


    public void crearPlaylist(int usuarioId, String nombre, String imagen) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO playlist (usuario_id, nombre, imagen) VALUES (" +
                usuarioId + ", '" + nombre + "', '" + imagen + "')");
        db.close();
    }

}