package bookapi.service;

import bookapi.Application;
import bookapi.domain.Book;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;

/**
 * Created by rockyren on 3/21/16.
 */
public abstract class AbstractAPIService {
    private Book book;
    private String url;
    private Map<String, String> requestParams;

    public AbstractAPIService(Map<String, String> requestParams) {
        this.requestParams = requestParams;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getRequestParams() {
        return requestParams;
    }

    public Document getDocument(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        Document doc = null;
        try {
            doc = Jsoup.connect(url)
                    .data("query", "Java")
                    .userAgent("Mozilla")
                    .cookie("auth", "token")
                    .timeout(3000)
                    .post();
        } catch (IOException e) {
            Application.logger.error("Jsoup cannot access-->" + url, e.getCause());
        }
        return doc;
    }

    public String elements2String(Elements elements) {
        if (elements == null) {
            return StringUtils.EMPTY;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for(Element element : elements) {
                stringBuilder.append(element.text()).append("\n");
            }
            return stringBuilder.toString();
        }
    }

    public boolean validated() {
        return getBook().getId() >= 0;
    }

}
