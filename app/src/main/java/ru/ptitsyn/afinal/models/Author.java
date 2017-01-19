package ru.ptitsyn.afinal.models;

public class Author {

    public final int id;
    public final String cover_name;

    public Author(int id, String cover_name) {
        this.id = id;
        this.cover_name = cover_name;
    }

    @Override
    public String toString() {
        return "id=" + id + " cover_name=" + cover_name;
    }

}
