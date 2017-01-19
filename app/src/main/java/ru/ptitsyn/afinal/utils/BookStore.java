package ru.ptitsyn.afinal.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ru.ptitsyn.afinal.models.Author;
import ru.ptitsyn.afinal.models.Book;
import ru.ptitsyn.afinal.models.BookAuthor;
import ru.ptitsyn.afinal.models.BookFile;

public class BookStore {

    public static List<BookItem> fetchBookList(String path, Context context) {

        if (path == null) {
            return fetchUserBookList(context);
        }

        String field = "results";
        if (path.contains("search")) {
            field = "books";
        }

        List<BookItem> bookItems = new ArrayList<>();

        List<Book> booksDB = new ArrayList<>();
        HashSet<Integer> book_ids = new HashSet<>();

        List<Author> authorsDB = new ArrayList<>();
        HashSet<Integer> author_ids = new HashSet<>();

        List<BookAuthor> bookAuthorDB = new ArrayList<>();

        URL url = null;
        try {
            url = new URL(AuthUtil.domain + path);
        } catch (MalformedURLException e) {
            Log.e("EA_DEMO","Error build search url", e);
        }

        try{
            JSONObject response = new JSONObject(StoreUtil.fetch(url));

            JSONArray books = response.getJSONArray(field);

            for (int i = 0; i < books.length(); i++) {
                JSONObject book = books.getJSONObject(i);

                int book_id = book.getInt("id");
                String book_name = book.getString("name");
                String book_cover = book.getString("cover");
                String book_annotation = "";

                String book_background_color = book.getString("background_color");
                String book_font_color = book.getString("font_color");
                String book_link_color = book.getString("link_color");

                JSONObject main_author = book.getJSONObject("main_author");
                int author_id = main_author.getInt("id");
                String author_cover_name = main_author.getString("cover_name");

                bookItems.add(new BookItem(book_id, book_name, book_cover, book_annotation, author_cover_name));

                if (!book_ids.contains(book_id)) {
                    booksDB.add(new Book(book_id, book_name, book_cover, book_annotation,
                            book_background_color, book_font_color, book_link_color));
                    book_ids.add(book_id);
                }
                if (!author_ids.contains(author_id)) {
                    authorsDB.add(new Author(author_id, author_cover_name));
                    author_ids.add(author_id);
                }
                bookAuthorDB.add(new BookAuthor(book_id, author_id));
            }

        } catch (Exception e){
            Log.e("EA_DEMO","Error fetching book list", e);
        }

        // создаем объект для создания и управления версиями БД
        DBHelper dbHelper = new DBHelper(context);
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if (booksDB.size() > 0) {
            StoreUtil.storeBooks(booksDB, "book", db);
        }
        if (authorsDB.size() > 0) {
            StoreUtil.storeAuthors(authorsDB, "author", db);
        }
        if (bookAuthorDB.size() > 0) {
            StoreUtil.storeBookAuthor(bookAuthorDB, "book_author", db);
        }

        return bookItems;
    }

    public static BookItem fetchBookDetail(String path, Context context) {

        BookItem bookItem = null;

        List<BookFile> bookFilesDB = new ArrayList<>();
        HashSet<Integer> bookfile_ids = new HashSet<>();

        URL url = null;
        try {
            url = new URL(AuthUtil.domain + path);
        } catch (MalformedURLException e) {
            Log.e("EA_DEMO","Error build book detail url", e);
        }

        try{
            JSONObject book = new JSONObject(StoreUtil.fetch(url));

            int id = book.getInt("id");
            String name = book.getString("name");
            String cover = book.getString("cover");
            String annotation = book.getString("annotation");

            JSONObject main_author = book.getJSONObject("main_author");
            String author_name = main_author.getString("cover_name");

            JSONArray files = book.getJSONArray("files");
            for (int i = 0; i < files.length(); i++) {
                JSONObject file = files.getJSONObject(i);

                int file_id = file.getInt("id");
                String file_url = file.getString("url");
                int file_seconds = file.getInt("seconds");
                int file_bytes = file.getInt("bytes");
                int file_order = file.getInt("order");
                if (!bookfile_ids.contains(file_id)) {
                    bookFilesDB.add(new BookFile(file_id, file_url, file_seconds, file_bytes, file_order, id));
                    bookfile_ids.add(file_id);
                }
            }

            bookItem = new BookItem(id, name, cover, annotation, author_name);

        }catch (Exception e){
            Log.e("EA_DEMO", "Error fetching book", e);
        }

        // создаем объект для создания и управления версиями БД
        DBHelper dbHelper = new DBHelper(context);
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if (bookFilesDB.size() > 0) {
            StoreUtil.storeBookFiles(bookFilesDB, "bookfile", db);
        }

        return bookItem;
    }

    private static List<BookItem> fetchUserBookList(Context context) {

        List<BookItem> items = new ArrayList<>();

        // создаем объект для создания и управления версиями БД
        DBHelper dbHelper = new DBHelper(context);
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // вытаскиваю из базы id книг, которые я читаю
        Cursor c = db.rawQuery(
                "SELECT book.id as book_id, book.name as book_name, book.cover as book_cover, author.cover_name as author_cover_name FROM book " +
                        "INNER JOIN autobookmark ON book.id = autobookmark.book_id " +
                        "INNER JOIN book_author ON book.id = book_author.book_id " +
                        "INNER JOIN author ON book_author.author_id = author.id",
                new String[] {}
        );
        if (c.moveToFirst()) {
            do {
                items.add(
                        new BookItem(c.getInt(c.getColumnIndex("book_id")),
                                c.getString(c.getColumnIndex("book_name")),
                                c.getString(c.getColumnIndex("book_cover")),
                                "",
                                c.getString(c.getColumnIndex("author_cover_name")))
                );
            } while (c.moveToNext());
        }
        c.close();

        return items;
    }

    public static Book fetchBookDetailShort(int id, Context context) {

        Book item = null;

        DBHelper dbHelper = new DBHelper(context);
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor c = db.rawQuery(
                "SELECT * FROM book where id = " + id,
                new String[] {}
        );
        if (c.moveToFirst()) {
            item = new Book(
                    c.getInt(c.getColumnIndex("id")),
                    c.getString(c.getColumnIndex("name")),
                    c.getString(c.getColumnIndex("cover")),
                    c.getString(c.getColumnIndex("annotation")),
                    c.getString(c.getColumnIndex("background_color")),
                    c.getString(c.getColumnIndex("font_color")),
                    c.getString(c.getColumnIndex("link_color"))
            );
        }
        c.close();

        return item;
    }

}
