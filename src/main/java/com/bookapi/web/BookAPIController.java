package bookapi.web;

import bookapi.Application;
import bookapi.domain.Book;
import bookapi.service.APIFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import bookapi.service.BookAPIService;

import java.util.Map;

@RestController
public class BookAPIController {
    @RequestMapping("/book")
    public Book book(@RequestParam Map<String,String> requestParams) {
        BookAPIService bookAPIService = APIFactory.getService(requestParams);
        return bookAPIService.getBook();
    }

    @RequestMapping("/cbook")
    public Book cbook(@RequestParam Map<String,String> requestParams) {
        return APIFactory.getCombinedBook(requestParams);
    }

    @ExceptionHandler(value = Exception.class)
    public Book handleAnyException (Exception ex){
        Application.logger.error(ex.getMessage());
        return Book.DEFAULT_BOOK;
    }

}
