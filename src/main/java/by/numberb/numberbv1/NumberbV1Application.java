package by.numberb.numberbv1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication
public class NumberbV1Application {

  public static void main(String[] args) {
    ApiContextInitializer.init();
    SpringApplication.run(NumberbV1Application.class, args);
  }

}
