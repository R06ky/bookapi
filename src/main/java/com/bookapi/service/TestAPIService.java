package bookapi.service;

import bookapi.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by rockyren on 6/3/16.
 */
@Service
public class TestAPIService {
    @Autowired
    Config config;
    public TestAPIService() {
    }
}
