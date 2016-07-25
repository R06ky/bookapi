package bookapi.service;

import bookapi.Config;
import bookapi.domain.Book;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by rockyren on 3/28/16.
 */
public class APIFactory {
    static BookAPIService DEFAULT_BOOK_API_SERVICE = new BookAPIService() {
        @Override
        public boolean buildBook() {
            return false;
        }

        @Override
        public Book getBook() {
            return Book.DEFAULT_BOOK;
        }

        @Override
        public boolean validated() {
            return false;
        }
    };

    /*
     * dangdang.com->jd.com->douban.com
     */
    public static List<BookAPIService> regAPIServices(Map<String,String> requestParams, Config config) {
        List<BookAPIService> apiServices = new ArrayList<>();
        if (requestParams.get("target") == null) {
            apiServices.add(new JDBookAPIService(requestParams, config));
            apiServices.add(new DDBookAPIService(requestParams));
            apiServices.add(new DBBookAPIService(requestParams));
        } else {
            switch (requestParams.get("target")) {
                case "dd" :
                    apiServices.add(new DDBookAPIService(requestParams));
                    break;
                case "db" :
                    apiServices.add(new DBBookAPIService(requestParams));
                    break;
                case "jd" :
                    apiServices.add(new JDBookAPIService(requestParams, config));
                    break;
                default:
                    apiServices.add(new DBBookAPIService(requestParams));
            }
        }
        return apiServices;
    }

    public static BookAPIService getService(Map<String,String> requestParams, Config config) {
        return getServiceHelper(regAPIServices(requestParams, config));
    }

    public static BookAPIService getServiceHelper (List<BookAPIService> apiServices) {
        if (apiServices == null || apiServices.isEmpty()) {
            return DEFAULT_BOOK_API_SERVICE;
        }

        for (BookAPIService bookAPIService : apiServices) {
            if (bookAPIService.buildBook()) {
                return bookAPIService;
            }
        }

        return DEFAULT_BOOK_API_SERVICE;
    }

    public static Book getCombinedBook (Map<String,String> requestParams, Config config) {
        List<Book> books = new ArrayList<>();
        for (BookAPIService bookAPIService : regAPIServices(requestParams, config)) {
            if (bookAPIService.buildBook()) {
                books.add(bookAPIService.getBook());
            }
        }
        return Book.combineBooks(books);
    }

}
