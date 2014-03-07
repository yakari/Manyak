package info.yakablog.manyak.item.specialItems;

import java.util.List;

import info.yakablog.manyak.item.GenericItem;
import info.yakablog.manyak.item.entries.Author;

/**
 * Created by krnl7365 on 03/03/14.
 */
public class Book extends GenericItem {
    private String isbn;

    private List<Author> authors;

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public void addAuthor(Author author) {
        authors.add(author);
    }
}
