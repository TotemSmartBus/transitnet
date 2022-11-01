package whu.edu.cs.transitnet.service;

import org.gavaghan.geodesy.GeodeticCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Beans {
    @Bean
    public GeodeticCalculator getGeodeticCalculator() {
        return new GeodeticCalculator();
    }
}
