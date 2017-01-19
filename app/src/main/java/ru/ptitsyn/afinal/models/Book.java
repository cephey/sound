package ru.ptitsyn.afinal.models;

public class Book {

    public final int id;
    public final String name;
    public final String cover;
    public final String annotation;

    public final String backgroundColor;
    public final String fontColor;
    public final String linkColor;

    public Book(int id, String name, String cover, String annotation,
                String backgroundColor, String fontColor, String linkColor) {
        this.id = id;
        this.name = name;
        this.cover = cover;
        this.annotation = annotation;

        this.backgroundColor = backgroundColor;
        this.fontColor = fontColor;
        this.linkColor = linkColor;
    }

    @Override
    public String toString() {
        return "id=" + id + " name=" + name + " cover=" + cover + " annotation=" + annotation + " backgroundColor=" + backgroundColor + " fontColor=" + fontColor + " linkColor=" + linkColor;
    }

}
