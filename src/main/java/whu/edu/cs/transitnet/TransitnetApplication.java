package whu.edu.cs.transitnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class TransitnetApplication {
    public static void main(String[] args) {
        try {
            ConfigurableApplicationContext ctx = SpringApplication.run(TransitnetApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("Error while start:" + e);
        }
    }


}
