package whu.edu.cs.transitnet.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

@Component
public class Formatter {
    @Bean
    public SimpleDateFormat defaultFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
