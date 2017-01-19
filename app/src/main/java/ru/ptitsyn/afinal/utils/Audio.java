package ru.ptitsyn.afinal.utils;

import java.io.Serializable;

public class Audio implements Serializable {

    private String url;
    private int fileId;
    private int duration;

    private int bookId;
    private String bookName;
    private String bookAuthorName;
    private String bookCover;

    public Audio(String url, int fileId, int duration, int bookId, String bookName, String bookAuthorName, String bookCover) {
        this.url = url;
        this.fileId = fileId;
        this.duration = duration;

        this.bookId = bookId;
        this.bookName = bookName;
        this.bookAuthorName = bookAuthorName;
        this.bookCover = bookCover;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookAuthorName() {
        return bookAuthorName;
    }

    public void setBookAuthorName(String bookAuthorName) {
        this.bookAuthorName = bookAuthorName;
    }

    public String getBookCover() {
        return bookCover;
    }

    public void setBookCover(String bookCover) {
        this.bookCover = bookCover;
    }

}
