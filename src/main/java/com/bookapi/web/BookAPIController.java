package bookapi.web;

import bookapi.Application;
import bookapi.Config;
import bookapi.domain.Book;
import bookapi.service.APIFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import bookapi.service.BookAPIService;

import java.util.Map;

@RestController
public class BookAPIController {
    @Autowired
    Config config;
    @RequestMapping("/book")
    public Book book(@RequestParam Map<String,String> requestParams) {
        BookAPIService bookAPIService = APIFactory.getService(requestParams, config);
        return bookAPIService.getBook();
    }

    @RequestMapping("/cbook")
    public Book cbook(@RequestParam Map<String,String> requestParams) {
        return APIFactory.getCombinedBook(requestParams, config);
    }

    @ExceptionHandler(value = Exception.class)
    public Book handleAnyException (Exception ex){
        Application.logger.error("Controller", ex);
        return Book.DEFAULT_BOOK;
    }

}
