package com.example.proyectofinalgasparcurso.modelos;

public class Usuario {
    public int id;
    public String nombre;
    public String email;
    public String password;

    public String foto;

    public Usuario(int id, String nombre, String email, String password, String foto) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.foto = foto;
    }
}
