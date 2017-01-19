package ru.ptitsyn.afinal.models;

public class Bookset {

    public final int id;
    public final String name;
    public final String description;
    public final String image;
    public final int book_count;

    public Bookset(int id, String name, String description, String image, int book_count) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.image = image;
        this.book_count = book_count;
    }

    @Override
    public String toString() {
        return "id=" + id + " name=" + name + " description=" + description + " image=" + image + " book_count=" + book_count;
    }

}
