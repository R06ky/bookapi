package bookapi.service;

import bookapi.Application;
import bookapi.domain.Book;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
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
            Application.logger.error("Jsoup cannot access-->" + url, e);
        }
        return doc;
    }

    public String getJsoupHTML(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        String json = null;
        try {
            json = Jsoup.connect(url).ignoreContentType(true).execute().body();
        } catch (Exception e) {
            Application.logger.error(url, e);
        }

        return json;
    }


    public String getJsonString(String sUrl) {
        if (sUrl == null) {
            return StringUtils.EMPTY;
        }

        HttpURLConnection connection;
        BufferedReader reader;
        try {
            URL url = new URL(sUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return StringUtils.EMPTY;
            }
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), Charset.forName("UTF-8")));

            StringBuilder html = new StringBuilder();
            String lines = reader.readLine();
            while (lines!= null) {
                html.append(lines);
                lines = reader.readLine();
            }
            reader.close();

            return html.toString();
        } catch (Exception e) {
            Application.logger.error("URL --> " + sUrl, e);
            return StringUtils.EMPTY;
        }
    }

    public boolean validated() {
        return getBook().getId() >= 0;
    }

}
