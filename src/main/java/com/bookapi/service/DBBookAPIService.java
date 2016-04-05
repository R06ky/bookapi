package bookapi.service;

import bookapi.Application;
import bookapi.domain.Book;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by rockyren on 3/14/16.
 */
public class DBBookAPIService extends AbstractAPIService implements BookAPIService {

    private final String DB_ID_PREFILL = "https://api.douban.com/v2/book/%s";
    private final String DB_ISBN_PREFILL = "https://api.douban.com/v2/book/isbn/%s";
    private final String DB_SEARCH_BY_NAME_PREFILL = "https://api.douban.com/v2/book/search?q=%s&fields=id,title";

    public DBBookAPIService(Map<String, String> requestParams) {
        super(requestParams);
    }

    public void setUrl() {
        String name = this.getRequestParams().get("name");
        String isbn = this.getRequestParams().get("isbn");
        String url;
        if (StringUtils.isEmpty(name)&& StringUtils.isEmpty(isbn)) {
            url = StringUtils.EMPTY;
        } else {
            if (StringUtils.isEmpty(name)) {
                url = String.format(DB_ISBN_PREFILL, isbn);
            } else {
                name = name.replace(" ", "+");
                url = String.format(DB_ID_PREFILL, getDBIDBYName(name));
            }
        }

        setUrl(url);
    }

    public boolean buildBook() {
        Book book;
        try {
            String json = getJsonString(getDBUrl());
            if (StringUtils.isEmpty(json)) {
                return false;
            }
            book = Book.newBook(new JSONObject(json));
        } catch (Exception e) {
            e.printStackTrace();
            book = Book.DEFAULT_BOOK;
        }

        setBook(book);
        return validated();
    }

    public String getDBUrl() {
        setUrl();
        return getUrl();
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
            Application.logger.error("URL --> " + sUrl + "\n" + e.getMessage());
            return StringUtils.EMPTY;
        }
    }

    public boolean validated () {
        return getBook() != null && super.validated() && getBook().isDBBook();
    }

    public String getDBIDBYName (String name) {
        String sUrl = String.format(DB_SEARCH_BY_NAME_PREFILL, name);
        String json = null;
        try {
            json = getJsonString(sUrl);
            // It returns book list.
            JSONObject jsonObject = new JSONObject(json);
            if(jsonObject.getInt("total") == 0) {
                return StringUtils.EMPTY;
            }
            JSONArray jsonArray = jsonObject.getJSONArray("books");
            if (jsonArray.length() > 0) {
                return jsonArray.getJSONObject(0).getString("id");
            }
        } catch (Exception e) {
            Application.logger.error("URL --> " + sUrl + "\n JSON->" + json);
        }

        return StringUtils.EMPTY;
    }
}
