package ru.ptitsyn.afinal.utils;

public class NicheItem {

    public final int id;
    public final String name;
    public final String image;

    public NicheItem(int id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    @Override
    public String toString() {
        return "id=" + id + " name=" + name + " image=" + image;
    }
}
