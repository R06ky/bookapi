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
 * Created by rockyren on 4/1/16.
 */
public class JDBookAPIService extends AbstractAPIService implements BookAPIService {

    final static String SEARCH_BY_TITLE = "http://search.jd.com/bookadvsearch?keyword=%s&enc=utf-8";
    final static String SEARCH_BY_ISBN = "http://search.jd.com/bookadvsearch?isbn=%s";

    public JDBookAPIService(Map<String, String> requestParams) {
        super(requestParams);
    }

    private void setUrl() {
        String sUrl = null;
        String bookName = this.getRequestParams().get("name");
        String isbn = this.getRequestParams().get("isbn");

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

    public String getUrlBySearch(String url) {
        Document document = getDocument(url);
        Element JDGoodsList = document.getElementById("J_goodsList");

        if (JDGoodsList == null) {
            return StringUtils.EMPTY;
        }
        Element bookUrl = JDGoodsList.select("a").first();
        if (bookUrl == null) {
            return StringUtils.EMPTY;
        }
        return "http:" + bookUrl.attr("href");
    }

    @Override
    public boolean buildBook() {
        setUrl();
        Document document = getDocument(getUrl());
        setBook(document2Book(document));
        return validated();
    }

    private Book document2Book(Document doc) {
        if (doc == null) {
            return Book.DEFAULT_BOOK;
        }
        Book book = new Book();
        Element name = doc.getElementById("name");
        book.setTitle(name == null ? StringUtils.EMPTY : name.select("h1").text());
        Elements proContent = doc.getElementsByClass("p-parameter");
        if (proContent != null) {
            Elements elements = proContent.select("ul").select("li");
            Iterator iterator = elements.iterator();
            while (iterator.hasNext()) {
                Element element = (Element) iterator.next();
                String content = element.select("li").text();
                if (StringUtils.contains(content,"出版社")) {
                    book.setPublisher(element.select("li").attr("title"));
                }
                if (StringUtils.contains(content,"出版时间")) {
                    book.setPubdate(StringUtils.split(content, DDBookAPIService.DD_COLON).length > 0 ? StringUtils.split(content, DDBookAPIService.DD_COLON)[1] : StringUtils.EMPTY);
                }
                if (StringUtils.contains(content,"ISBN")) {
                    String isbn = StringUtils.split(content, DDBookAPIService.DD_COLON).length > 0 ? StringUtils.split(content, DDBookAPIService.DD_COLON)[1] : StringUtils.EMPTY;
                    if (isbn.length() > 10) {
                        book.setIsbn13(isbn);
                    } else {
                        book.setIsbn10(isbn);
                    }
                }
            }
        }
        ArrayList images = new ArrayList();

        Element imageEles = doc.getElementById("preview");
        if (imageEles != null) {

            for (Element imageEle : imageEles.select("img")) {
                images.add(imageEle == null ? StringUtils.EMPTY : "http:" + imageEle.attr("src"));
            }
        }
        book.setImages(images);

        ArrayList authorList = new ArrayList<String>();
        ArrayList translatorList = new ArrayList<String>();
        Element authorEle = doc.getElementById("p-author");
        if (authorEle != null) {
            for(Element a : authorEle.select("a")) {
                authorList.add(a.text());
            }
        }
        book.setAuthor(authorList);
        book.setTranslator(translatorList);

        Element contentsEle = doc.getElementById("J-detail-content");
        if (contentsEle != null) {
            Element authorIntroEle = contentsEle.attr("text","作者简介");
            book.setAuthorIntro(elements2String(authorIntroEle.children()));
            Element summaryEle = contentsEle.attr("text","内容简介");
            book.setSummary(elements2String(summaryEle.children()));
            Element catalogEle = contentsEle.attr("text","目录");
            book.setCatalog(elements2String(catalogEle.children()));
        }

        Elements as = doc.getElementsByClass("breadcrumb").select("a");
        StringBuilder categroy = new StringBuilder();
        for (int i = 0; i < as.size() - 1; i ++) {
            categroy.append(as.get(i).text());
            if (i < as.size() - 2) {
                categroy.append("|");
            }
        }
        book.setCategroy(categroy.toString());

        return book;
    }

    @Override
    public boolean validated() {
        boolean validtion = getBook() != null && super.validated() && getBook().getImages() != null;
        if (!validtion) {
            Application.logger.warn("JD cannot get the book, URL --> " + getUrl());
        }
        return validtion;
    }
}
