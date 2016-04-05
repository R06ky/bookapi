package bookapi.domain;

import bookapi.Application;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by rockyren on 3/9/16.
 */
public class Book {
    private int id = 0;
    private int doubanId = 0;
    private String title = null;
    private String isbn10 = null;
    private String isbn13 = null;
    private List<String> images = null; // the first one is the largest one.
    private List<String> author = null;
    private List<String> translator = null;
    private String publisher = null;
    private String pubdate = null;
    private String authorIntro = null;
    private String summary = null;
    private String catalog = null;

    private String categroy = null;

    public static final Book DEFAULT_BOOK = new Book(-1, "not found", "404");

    public Book(){}

    public Book(int id, String title, String isbn10) {
        this.id = id;
        this.title = title;
        this.isbn10 = isbn10;
    }

    public Book(int doubanId, String title, String isbn10, String isbn13, List images, List author, List translator, String publisher, String pubdate, String authorIntro, String summary, String catalog) {
        this.doubanId = doubanId;
        this.title = title;
        this.isbn10 = isbn10;
        this.isbn13 = isbn13;
        this.images = images;
        this.author = author;
        this.translator = translator;
        this.publisher = publisher;
        this.pubdate = pubdate;
        this.authorIntro = authorIntro;
        this.summary = summary;
        this.catalog = catalog;
    }

    public static Book newBook(JSONObject jsonObject) {

        ArrayList author = new ArrayList();
        ArrayList translator = new ArrayList();
        ArrayList images = new ArrayList();

        Iterator iAuthor = jsonObject.getJSONArray("author").iterator();
        while (iAuthor.hasNext()) {
            author.add(iAuthor.next());
        }

        Iterator iTranslator = jsonObject.getJSONArray("translator").iterator();
        while (iAuthor.hasNext()) {
            translator.add(iTranslator.next());
        }

        images.add(jsonObject.getJSONObject("images").getString("large"));
        images.add(jsonObject.getJSONObject("images").getString("medium"));
        images.add(jsonObject.getJSONObject("images").getString("small"));

        return new Book(jsonObject.getInt("id"), jsonObject.getString("title"),jsonObject.getString("isbn10"),
                jsonObject.getString("isbn13"), images, author, translator,
                jsonObject.getString("publisher"), jsonObject.getString("pubdate"), jsonObject.getString("author_intro"),
                jsonObject.getString("summary"), jsonObject.getString("catalog"));
    }


    public static Book combineBooks(List<Book> books) {
        if (books.isEmpty()) {
            return DEFAULT_BOOK;
        }
        Book combinedBook = new Book();
        Field[] fields = combinedBook.getClass().getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (StringUtils.equalsIgnoreCase(fieldName, "DEFAULT_BOOK")) {
                continue;
            }
            fieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            for (Book book : books) {
                Method getM;
                try {
                    getM = book.getClass().getMethod("get" + fieldName);
                    Object o = getM.invoke(book);
                    if (o != null) {
                        Method setM = combinedBook.getClass().getMethod("set" + fieldName, field.getType());
                        setM.invoke(combinedBook, o);
                        break;
                    }
                } catch (Exception e) {
                    Application.logger.warn(e.getMessage());
                }
            }
        }
        return combinedBook;
    }



    @JsonIgnore
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn10() {
        return isbn10;
    }

    public void setIsbn10(String isbn10) {
        this.isbn10 = isbn10;
    }

    @JsonIgnore
    public boolean isDBBook() {
        return getDoubanId() > 0;
    }

    @JsonIgnore
    public int getDoubanId() {
        return doubanId;
    }

    public void setDoubanId(int doubanId) {
        this.doubanId = doubanId;
    }

    public String getIsbn13() {
        return isbn13;
    }

    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }

    public List<String> getAuthor() {
        return author;
    }

    public void setAuthor(List author) {
        this.author = author;
    }

    public List<String> getTranslator() {
        return translator;
    }

    public void setTranslator(List translator) {
        this.translator = translator;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPubdate() {
        return pubdate;
    }

    public void setPubdate(String pubdate) {
        this.pubdate = pubdate;
    }

    public String getAuthorIntro() {
        return authorIntro;
    }

    public void setAuthorIntro(String authorIntro) {
        this.authorIntro = authorIntro;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getCategroy() {
        return categroy;
    }

    public void setCategroy(String categroy) {
        this.categroy = categroy;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
