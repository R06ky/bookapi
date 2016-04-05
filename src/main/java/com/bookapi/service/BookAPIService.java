package bookapi.service;

import bookapi.domain.Book;

/**
 * Created by rockyren on 3/4/16.
 */
public interface BookAPIService {
    boolean buildBook();
    Book getBook();
    boolean validated();
}
