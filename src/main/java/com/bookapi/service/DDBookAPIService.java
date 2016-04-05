package bookapi.service;

import bookapi.Application;
import bookapi.domain.Book;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by rockyren on 3/14/16.
 */
public class DDBookAPIService extends AbstractAPIService implements BookAPIService{
    final static String DD_COLON = "：";
    final static String BOOK_URL = "http://product.dangdang.com/%s.html";
    final static String SEARCH_BY_TITLE = "http://search.dangdang.com/?medium=01&key1=%s";
    final static String SEARCH_BY_ISBN = "http://search.dangdang.com/?medium=01&key4=%s";
    private Book validatedBook = null;

    public DDBookAPIService(Map<String,String> requestParams) {
        super(requestParams);
    }

    public void setUrl() {
        String sUrl = null;
        String bookName = this.getRequestParams().get("name");
        String isbn = this.getRequestParams().get("isbn");

        if (!StringUtils.isEmpty(isbn) && isbn.length() < 13) {
            if (validateUrl(getDocument(String.format(BOOK_URL, isbn)), isbn)) {
                setUrl(String.format(BOOK_URL, isbn));
                return;
            }
        }

        if (StringUtils.isEmpty(sUrl)) {
            if (StringUtils.isEmpty(isbn)) {
                bookName = bookName.replace(" ", "+");
                sUrl = String.format(SEARCH_BY_TITLE, bookName);
            } else {
                sUrl = String.format(SEARCH_BY_ISBN, isbn);
            }
        }

        setUrl(getUrlBySearch(sUrl));
    }

    private String getUrlBySearch(String url) {
        Document document = getDocument(url);
        Element searchList = document.getElementsByClass("line1").first();

        if (searchList == null) {
            return StringUtils.EMPTY;
        }
        Element bookUrl = searchList.select("a").first();
        if (bookUrl == null) {
            return StringUtils.EMPTY;
        }
        return bookUrl.attr("href");
    }

    public boolean buildBook() {
        setUrl();
        if (getBook() == null) {
            Document document = getDocument(getUrl());
            setBook(document2Book(document));
        }
        return validated();
    }

    public boolean validated () {
        boolean validtion = getBook() != null && super.validated() && !(StringUtils.isEmpty(getBook().getIsbn10()) && StringUtils.isEmpty(getBook().getIsbn13()));
        if (!validtion) {
            Application.logger.warn("DD cannot get the book, URL --> " + getUrl());
        }
        return validtion;
    }

    private Book document2Book(Document doc) {
        if (doc == null) {
            return Book.DEFAULT_BOOK;
        }
        Book book = new Book();
        book.setTitle(doc.getElementsByClass("name_info").select("h1").attr("title"));
        if (StringUtils.isEmpty(book.getTitle())) {
            book.setTitle(doc.getElementsByClass("head").select("h1").text());
        }
        Elements proContent = doc.getElementsByClass("pro_content");
        if (proContent != null) {
            Elements elements = proContent.select("ul").select("li");
            Iterator iterator = elements.iterator();
            while (iterator.hasNext()) {
                Element element = (Element) iterator.next();
                String content = element.select("li").text();
                if (StringUtils.contains(content,"印刷时间")) {
                    book.setPubdate(StringUtils.split(content, DD_COLON).length > 1 ? StringUtils.split(content, DD_COLON)[1] : StringUtils.EMPTY);
                }
                if (StringUtils.contains(content,"ISBN")) {
                    String isbn = StringUtils.split(content, DD_COLON).length > 1 ? StringUtils.split(content, DD_COLON)[1] : StringUtils.EMPTY;
                    if (isbn.length() > 10) {
                        book.setIsbn13(isbn);
                    } else {
                        book.setIsbn10(isbn);
                    }
                }
            }
        }
        //shit dd

        ArrayList authorList = new ArrayList<String>();

        ArrayList images = new ArrayList();
        if (doc.getElementById("largePic") != null) {
            String imageUrl = doc.getElementById("largePic").attr("src");
            if (!StringUtils.startsWith(imageUrl,"http")) {
                imageUrl = doc.getElementById("largePic").attr("wsrc");
            }
            images.add(imageUrl);
        }

        Elements imageEles = doc.getElementsByClass("dp_slide").select("img");
        for (Element imageEle : imageEles) {
            images.add(imageEle == null ? StringUtils.EMPTY : imageEle.attr("src"));
        }
        book.setImages(images);

        ArrayList translatorList = new ArrayList<String>();
        Element authorEle = doc.getElementById("author");
        if (authorEle != null) {
            for (Element a : authorEle.select("a")) {
                authorList.add(a.text());
            }
        }
        book.setAuthor(authorList);
        book.setTranslator(translatorList);
        book.setPublisher(doc.getElementsByAttributeValue("dd_name","出版社").select("a").text());
        book.setAuthorIntro(doc.getElementById("authorintro_show") == null ?
                StringUtils.EMPTY : doc.getElementById("authorintro_show").text());
        String summary = doc.getElementById("content_all") == null ? StringUtils.EMPTY : doc.getElementById("content_all").parent().select("textarea").text();
        Document document = Jsoup.parse(summary);
        book.setSummary(StringUtils.remove(document.select("p").html(),"<span></span>"));
        book.setCatalog(doc.getElementById("catalog_all") == null ? StringUtils.EMPTY : doc.getElementById("catalog_all").parent().select("textarea").text());

        Elements as = doc.getElementsByClass("breadcrumb").select("a");
        StringBuilder categroy = new StringBuilder();
        for (int i = 0; i < as.size(); i ++) {
            categroy.append(as.get(i).text());
            if (i < as.size() - 1) {
                categroy.append("|");
            }
        }
        book.setCategroy(categroy.toString());

        Element elements = doc.getElementsByClass("book_messbox").first();
        if (elements != null) {
            for(Element element : elements.children()) {
                if (element.getElementsByClass("show_info_left").first().text().contains("作")) {
                    authorList.add(element.getElementsByClass("show_info_right").first().select("a").text());
                    book.setAuthor(authorList);
                }
                if (element.getElementsByClass("show_info_left").first().text().contains("社")) {
                    book.setPublisher(element.getElementsByClass("show_info_right").first().select("a").text());
                }
                if (element.getElementsByClass("show_info_left").first().text().contains("出版时间")) {
                    book.setPubdate(element.getElementsByClass("show_info_right").first().text());
                }
                if (element.getElementsByClass("show_info_left").first().text().contains("ＩＳＢＮ")) {
                    String isbn = element.getElementsByClass("show_info_right").first().text();
                    if (!StringUtils.isEmpty(isbn)) {
                        if (isbn.length() > 10) {
                            book.setIsbn13(isbn);
                        } else {
                            book.setIsbn10(isbn);
                        }

                    }
                }
            }
        }

        return book;
    }

    public boolean validateUrl(Document doc, String isbn) {
        if (doc == null) {
            return false;
        }

        Book book = document2Book(doc);
        if (!StringUtils.isEmpty(book.getIsbn10()) || !StringUtils.isEmpty(book.getIsbn13())) {
            setBook(book);
            return StringUtils.equals(book.getIsbn10().trim(), isbn) || StringUtils.equals(book.getIsbn13().trim(), isbn);
        }

        return false;
    }



}
