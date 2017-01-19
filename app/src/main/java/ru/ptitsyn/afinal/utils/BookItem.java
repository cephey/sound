package ru.ptitsyn.afinal.utils;

public class BookItem {

    public final int id;
    public final String name;
    public final String cover;
    public final String annotation;
    public final String author_name;

    public BookItem(int id, String name, String cover, String annotation, String author_name) {
        this.id = id;
        this.name = name;
        this.cover = cover;
        this.annotation = annotation;
        this.author_name = author_name;
    }

    @Override
    public String toString() {
        return "id=" + id + " name=" + name + " cover=" + cover + " annotation=" + annotation + " author=" + author_name;
    }

}
