package com.example.proyectofinalgasparcurso.modelos;

public class Cancion {
    public int id;
    public String titulo;
    public String artista;
    public String ruta;

    public Cancion(int id, String titulo, String artista, String ruta) {
        this.id = id;
        this.titulo = titulo;
        this.artista = artista;
        this.ruta = ruta;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getArtista() { return artista; }
    public String getRuta() { return ruta; }


}
