package ru.ptitsyn.afinal.models;

public class BookAuthor {

    public final int book_id;
    public final int author_id;

    public BookAuthor(int book_id, int author_id) {
        this.book_id = book_id;
        this.author_id = author_id;
    }

    @Override
    public String toString() {
        return "book_id=" + book_id + " author_id=" + author_id;
    }

}
