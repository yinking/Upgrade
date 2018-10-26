package test;


import javafx.application.Application;
import main.MainController;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MainController.class)
public class AbstractTest {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
}
