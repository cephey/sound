package ru.ptitsyn.afinal.models;

public class BookFile {
    public final int id;
    public final String url;
    public final int seconds;
    public final int bytes;
    public final int order;
    public final int book_id;

    public BookFile(int id, String url, int seconds, int bytes, int order, int book_id) {
        this.id = id;
        this.url = url;
        this.seconds = seconds;
        this.bytes = bytes;
        this.order = order;
        this.book_id = book_id;
    }

    @Override
    public String toString() {
        return "id=" + id + " url=" + url + " seconds=" + seconds + " bytes=" + bytes + " order=" + order + " book_id=" + book_id;
    }
}
