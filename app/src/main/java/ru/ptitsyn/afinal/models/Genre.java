package ru.ptitsyn.afinal.models;

public class Genre {

    public final int id;
    public final String name;
    public final int niche_id;
    public final int book_count;

    public Genre(int id, String name, int niche_id, int book_count) {
        this.id = id;
        this.name = name;
        this.niche_id = niche_id;
        this.book_count = book_count;
    }

    @Override
    public String toString() {
        return "id=" + id + " name=" + name + " niche_id=" + niche_id + " book_count=" + book_count;
    }

}
