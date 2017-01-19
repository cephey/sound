package ru.ptitsyn.afinal.models;

public class Niche {

    public final int id;
    public final String name;
    public final int order;
    public final String image;
    public final int book_count;

    public Niche(int id, String name, int order, String image, int book_count) {
        this.id = id;
        this.name = name;
        this.order = order;
        this.image = image;
        this.book_count = book_count;
    }

    @Override
    public String toString() {
        return "id=" + id + " name=" + name + " order=" + order + " image=" + image + " book_count=" + book_count;
    }

}
