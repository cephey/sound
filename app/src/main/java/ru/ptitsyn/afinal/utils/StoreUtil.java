package ru.ptitsyn.afinal.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import ru.ptitsyn.afinal.models.Author;
import ru.ptitsyn.afinal.models.Book;
import ru.ptitsyn.afinal.models.BookAuthor;
import ru.ptitsyn.afinal.models.BookFile;
import ru.ptitsyn.afinal.models.Bookset;
import ru.ptitsyn.afinal.models.Genre;
import ru.ptitsyn.afinal.models.Niche;


public class StoreUtil {

    public static String fetch(URL url) throws InterruptedException {
        if (url == null) {
            return "";
        }

        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;
        String data = "";

        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Authorization", AuthUtil.token);
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            data = buffer.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static List<Integer> items_for_update(List<Integer> itemIds, String table_name, SQLiteDatabase db) {

        String select_item_ids = itemIds.toString().substring(1, itemIds.toString().length() - 1);
        Cursor c = db.rawQuery(
                "SELECT id FROM " + table_name + " WHERE id IN (" + select_item_ids + ")",
                new String[] {}
        );

        List<Integer> update_ids = new ArrayList<Integer>();
        if (c.moveToFirst()) {
            do {
                update_ids.add(
                        c.getInt(c.getColumnIndex("id"))
                );
            } while (c.moveToNext());
        }
        c.close();

        return update_ids;
    }

    public static void storeBooks(List<Book> items, String table_name, SQLiteDatabase db) {

        List<Integer> itemIds = new ArrayList<>(items.size());

        for (int i=0; i < items.size(); i++) {
            itemIds.add(items.get(i).id);
        }
        List<Integer> update_ids = StoreUtil.items_for_update(itemIds, table_name, db);

        String name_case = null;
        String cover_case = null;
        String annotation_case = null;
        String background_color_case = null;
        String font_color_case = null;
        String link_color_case = null;

        if ( !update_ids.isEmpty() ) {
            name_case = "name = CASE id";
            cover_case = "cover = CASE id";
            annotation_case = "annotation = CASE id";
            background_color_case = "background_color = CASE id";
            font_color_case = "font_color = CASE id";
            link_color_case = "link_color = CASE id";
        }

        String insert_tuples = "";

        for (Book item : items) {
            if (update_ids.contains(item.id)) {
                // update
                name_case += " WHEN " + item.id + " THEN \"" + item.name + "\"";
                cover_case += " WHEN " + item.id + " THEN \"" + item.cover + "\"";
                annotation_case += " WHEN " + item.id + " THEN \"" + item.annotation + "\"";
                background_color_case += " WHEN " + item.id + " THEN \"" + item.backgroundColor + "\"";
                font_color_case += " WHEN " + item.id + " THEN \"" + item.fontColor + "\"";
                link_color_case += " WHEN " + item.id + " THEN \"" + item.linkColor + "\"";
            } else {
                // insert
                insert_tuples += " (" + item.id + ", \"" + item.name + "\", \"" + item.cover + "\", \"" + item.annotation + "\", \"" + item.backgroundColor + "\", \"" + item.fontColor + "\", \"" + item.linkColor + "\"),";
            }
        }

        if ( !update_ids.isEmpty() ) { // если есть данные для обновления

            // закрываю все CASE
            name_case += " END";
            cover_case += " END";
            annotation_case += " END";
            background_color_case += " END";
            font_color_case += " END";
            link_color_case += " END";

            // обновляю
            String update_item_ids = update_ids.toString().substring(1, update_ids.toString().length() - 1);
            db.execSQL(
                    "UPDATE " + table_name + " SET " + name_case + ", " + cover_case + ", " + annotation_case + ", " + background_color_case + ", " + font_color_case + ", " + link_color_case + " WHERE id IN (" + update_item_ids + ")",
                    new String[] {}
            );
        }
        if ( !insert_tuples.isEmpty() ) { // если есть данные для вставки

            // удаляю последнюю запятую
            insert_tuples = insert_tuples.substring(0, insert_tuples.length() - 1);

            // вставляю
            db.execSQL(
                    "INSERT INTO " + table_name + " (id, name, cover, annotation, background_color, font_color, link_color) VALUES" + insert_tuples,
                    new String[] {}
            );
        }

    }

    public static void storeAuthors(List<Author> items, String table_name, SQLiteDatabase db) {

        List<Integer> itemIds = new ArrayList<Integer>(items.size());

        for (int i=0; i < items.size(); i++) {
            itemIds.add(items.get(i).id);
        }
        List<Integer> update_ids = StoreUtil.items_for_update(itemIds, table_name, db);

        String cover_name_case = null;

        if ( !update_ids.isEmpty() ) {
            cover_name_case = "cover_name = CASE id";
        }

        String insert_tuples = "";

        for (Author item : items) {
            if (update_ids.contains(item.id)) {
                // update
                cover_name_case += " WHEN " + item.id + " THEN \"" + item.cover_name + "\"";
            } else {
                // insert
                insert_tuples += " (" + item.id + ", \"" + item.cover_name + "\"),";
            }
        }

        if ( !update_ids.isEmpty() ) { // если есть данные для обновления

            // закрываю все CASE
            cover_name_case += " END";

            // обновляю
            String update_item_ids = update_ids.toString().substring(1, update_ids.toString().length() - 1);
            db.execSQL(
                    "UPDATE " + table_name + " SET " + cover_name_case + " WHERE id IN (" + update_item_ids + ")",
                    new String[] {}
            );
        }
        if ( !insert_tuples.isEmpty() ) { // если есть данные для вставки

            // удаляю последнюю запятую
            insert_tuples = insert_tuples.substring(0, insert_tuples.length() - 1);

            // вставляю
            db.execSQL(
                    "INSERT INTO " + table_name + " (id, cover_name) VALUES" + insert_tuples,
                    new String[] {}
            );
        }

    }

    public static void storeBookAuthor(List<BookAuthor> items, String table_name, SQLiteDatabase db) {

        // строю SQL условие WHERE по которому буду делать SELECT
        String select_item_ids = "";
        for (int i=0; i < items.size(); i++) {
            select_item_ids += " (book_id = " + items.get(i).book_id + " AND author_id = " + items.get(i).author_id + ") OR";
        }
        select_item_ids = select_item_ids.substring(0, select_item_ids.length() - 3); // remove last ' OR'

        // вытаскиваю из базы уже существующие свяки
        Cursor c = db.rawQuery(
                "SELECT book_id, author_id FROM " + table_name + " WHERE" + select_item_ids,
                new String[] {}
        );
        class RelItem {
            private Integer bookId;
            private Integer authorId;
            private RelItem(Integer book_id, Integer author_id) {
                bookId = book_id;
                authorId = author_id;
            }
        }
        List<RelItem> exists_tuples = new ArrayList<RelItem>();
        if (c.moveToFirst()) {
            do {
                exists_tuples.add(
                        new RelItem(c.getInt(c.getColumnIndex("book_id")), c.getInt(c.getColumnIndex("author_id")))
                );
            } while (c.moveToNext());
        }
        c.close();

        // выфильтровываю только те записи которых в базе не оказалось, мы их будем вставлять
        String insert_tuples = "";
        Boolean match;
        for (BookAuthor item : items) {
            match = false;
            for (RelItem et : exists_tuples) {
                if (item.book_id == et.bookId && item.author_id == et.authorId) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                insert_tuples += " (" + item.book_id + ", '" + item.author_id + "'),";
            }
        }

        if ( !insert_tuples.isEmpty() ) { // если есть данные для вставки

            // удаляю последнюю запятую
            insert_tuples = insert_tuples.substring(0, insert_tuples.length() - 1);

            // вставляю
            db.execSQL(
                    "INSERT INTO " + table_name + " (book_id, author_id) VALUES" + insert_tuples,
                    new String[] {}
            );
        }

    }

    public static void storeBookFiles(List<BookFile> items, String table_name, SQLiteDatabase db) {

        List<Integer> itemIds = new ArrayList<Integer>(items.size());

        for (int i=0; i < items.size(); i++) {
            itemIds.add(items.get(i).id);
        }
        List<Integer> update_ids = StoreUtil.items_for_update(itemIds, table_name, db);

        String url_case = null;
        String seconds_case = null;
        String bytes_case = null;
        String order_case = null;
        String bookId_case = null;

        if ( !update_ids.isEmpty() ) {
            url_case = "url = CASE id";
            seconds_case = "seconds = CASE id";
            bytes_case = "bytes = CASE id";
            order_case = "_order = CASE id";
            bookId_case = "book_id = CASE id";
        }

        String insert_tuples = "";

        for (BookFile item : items) {
            if (update_ids.contains(item.id)) {
                // update
                url_case += " WHEN " + item.id + " THEN \"" + item.url + "\"";
                seconds_case += " WHEN " + item.id + " THEN " + item.seconds;
                bytes_case += " WHEN " + item.id + " THEN " + item.bytes;
                order_case += " WHEN " + item.id + " THEN " + item.order;
                bookId_case += " WHEN " + item.id + " THEN " + item.book_id;
            } else {
                // insert
                insert_tuples += " (" + item.id + ", \"" + item.url + "\", " + item.seconds + ", " + item.bytes + ", " + item.order + ", " + item.book_id + "),";
            }
        }

        if ( !update_ids.isEmpty() ) { // если есть данные для обновления

            // закрываю все CASE
            url_case += " END";
            seconds_case += " END";
            bytes_case += " END";
            order_case += " END";
            bookId_case += " END";

            // обновляю
            String update_item_ids = update_ids.toString().substring(1, update_ids.toString().length() - 1);
            db.execSQL(
                    "UPDATE " + table_name + " SET " + url_case + ", " + seconds_case + ", " + bytes_case + ", " + order_case + ", " + bookId_case + " WHERE id IN (" + update_item_ids + ")",
                    new String[] {}
            );
        }
        if ( !insert_tuples.isEmpty() ) { // если есть данные для вставки

            // удаляю последнюю запятую
            insert_tuples = insert_tuples.substring(0, insert_tuples.length() - 1);

            // вставляю
            db.execSQL(
                    "INSERT INTO " + table_name + " (id, url, seconds, bytes, _order, book_id) VALUES" + insert_tuples,
                    new String[] {}
            );
        }

    }

    public static void storeNiches(List<Niche> items, String table_name, SQLiteDatabase db) {

        List<Integer> itemIds = new ArrayList<Integer>(items.size());

        for (int i=0; i < items.size(); i++) {
            itemIds.add(items.get(i).id);
        }
        List<Integer> update_ids = StoreUtil.items_for_update(itemIds, table_name, db);

        String name_case = null;
        String order_case = null;
        String image_case = null;
        String book_count_case = null;

        if ( !update_ids.isEmpty() ) {
            name_case = "name = CASE id";
            order_case = "_order = CASE id";
            image_case = "image = CASE id";
            book_count_case = "book_count = CASE id";
        }

        String insert_tuples = "";

        for (Niche item : items) {
            if (update_ids.contains(item.id)) {
                // update
                name_case += " WHEN " + item.id + " THEN \"" + item.name + "\"";
                order_case += " WHEN " + item.id + " THEN " + item.order;
                image_case += " WHEN " + item.id + " THEN \"" + item.image + "\"";
                book_count_case += " WHEN " + item.id + " THEN " + item.book_count;
            } else {
                // insert
                insert_tuples += " (" + item.id + ", \"" + item.name + "\", " + item.order + ", \"" + item.image + "\", " + item.book_count + "),";
            }
        }

        if ( !update_ids.isEmpty() ) { // если есть данные для обновления

            // закрываю все CASE
            name_case += " END";
            order_case += " END";
            image_case += " END";
            book_count_case += " END";

            // обновляю
            String update_item_ids = update_ids.toString().substring(1, update_ids.toString().length() - 1);
            db.execSQL(
                    "UPDATE " + table_name + " SET " + name_case + ", " + order_case + ", " + image_case + ", " + book_count_case + " WHERE id IN (" + update_item_ids + ")",
                    new String[] {}
            );
        }
        if ( !insert_tuples.isEmpty() ) { // если есть данные для вставки

            // удаляю последнюю запятую
            insert_tuples = insert_tuples.substring(0, insert_tuples.length() - 1);

            // вставляю
            db.execSQL(
                    "INSERT INTO " + table_name + " (id, name, _order, image, book_count) VALUES" + insert_tuples,
                    new String[] {}
            );
        }
    }

    public static void storeGenres(List<Genre> items, String table_name, SQLiteDatabase db) {

        final int batch = 100;

        if (items.size() > batch) {
            for (int i=0; i < items.size(); i+=batch) {
                int top = i + batch;
                if (i + batch > items.size()) {
                    top = items.size();
                }
                storeGenres(items.subList(i, top), table_name, db);
            }
            return;
        }

        List<Integer> itemIds = new ArrayList<Integer>(items.size());

        for (int i=0; i < items.size(); i++) {
            itemIds.add(items.get(i).id);
        }
        List<Integer> update_ids = StoreUtil.items_for_update(itemIds, table_name, db);

        String name_case = null;
        String nicheId_case = null;
        String book_count_case = null;

        if ( !update_ids.isEmpty() ) {
            name_case = "name = CASE id";
            nicheId_case = "niche_id = CASE id";
            book_count_case = "book_count = CASE id";
        }

        String insert_tuples = "";

        for (Genre item : items) {
            if (update_ids.contains(item.id)) {
                // update
                name_case += " WHEN " + item.id + " THEN \"" + item.name + "\"";
                nicheId_case += " WHEN " + item.id + " THEN " + item.niche_id;
                book_count_case += " WHEN " + item.id + " THEN " + item.book_count;
            } else {
                // insert
                insert_tuples += " (" + item.id + ", \"" + item.name + "\", " + item.niche_id + ", " + item.book_count + "),";
            }
        }

        if ( !update_ids.isEmpty() ) { // если есть данные для обновления

            // закрываю все CASE
            name_case += " END";
            nicheId_case += " END";
            book_count_case += " END";

            // обновляю
            String update_item_ids = update_ids.toString().substring(1, update_ids.toString().length() - 1);
            db.execSQL(
                    "UPDATE " + table_name + " SET " + name_case + ", " + nicheId_case + ", " + book_count_case + " WHERE id IN (" + update_item_ids + ")",
                    new String[] {}
            );
        }
        if ( !insert_tuples.isEmpty() ) { // если есть данные для вставки

            // удаляю последнюю запятую
            insert_tuples = insert_tuples.substring(0, insert_tuples.length() - 1);

            // вставляю
            db.execSQL(
                    "INSERT INTO " + table_name + " (id, name, niche_id, book_count) VALUES" + insert_tuples,
                    new String[] {}
            );
        }
    }

    public static void storeBooksets(List<Bookset> items, String table_name, SQLiteDatabase db) {

        List<Integer> itemIds = new ArrayList<Integer>(items.size());

        for (int i=0; i < items.size(); i++) {
            itemIds.add(items.get(i).id);
        }
        List<Integer> update_ids = StoreUtil.items_for_update(itemIds, table_name, db);

        String name_case = null;
        String description_case = null;
        String image_case = null;
        String book_count_case = null;

        if ( !update_ids.isEmpty() ) {
            name_case = "name = CASE id";
            description_case = "description = CASE id";
            image_case = "image = CASE id";
            book_count_case = "book_count = CASE id";
        }

        String insert_tuples = "";

        for (Bookset item : items) {
            if (update_ids.contains(item.id)) {
                // update
                name_case += " WHEN " + item.id + " THEN \"" + item.name + "\"";
                description_case += " WHEN " + item.id + " THEN \"" + item.description + "\"";
                image_case += " WHEN " + item.id + " THEN \"" + item.image + "\"";
                book_count_case += " WHEN " + item.id + " THEN " + item.book_count;
            } else {
                // insert
                insert_tuples += " (" + item.id + ", \"" + item.name + "\", \"" + item.description + "\", \"" + item.image + "\", " + item.book_count + "),";
            }
        }

        if ( !update_ids.isEmpty() ) { // если есть данные для обновления

            // закрываю все CASE
            name_case += " END";
            description_case += " END";
            image_case += " END";
            book_count_case += " END";

            // обновляю
            String update_item_ids = update_ids.toString().substring(1, update_ids.toString().length() - 1);
            db.execSQL(
                    "UPDATE " + table_name + " SET " + name_case + ", " + description_case + ", " + image_case + ", " + book_count_case + " WHERE id IN (" + update_item_ids + ")",
                    new String[] {}
            );
        }
        if ( !insert_tuples.isEmpty() ) { // если есть данные для вставки

            // удаляю последнюю запятую
            insert_tuples = insert_tuples.substring(0, insert_tuples.length() - 1);

            // вставляю
            db.execSQL(
                    "INSERT INTO " + table_name + " (id, name, description, image, book_count) VALUES" + insert_tuples,
                    new String[] {}
            );
        }
    }

}
