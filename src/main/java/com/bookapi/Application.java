package bookapi;


import bookapi.web.BookAPIController;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class Application {

    public static final org.slf4j.Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        Application application = new Application();
        String path = application.getClass().getResource("").getPath();
        File file = new File(path);
        //set the log file path...
        System.setProperty ("WORKDIR", file.getParent());
        SpringApplication.run(Application.class, args);
    }
}