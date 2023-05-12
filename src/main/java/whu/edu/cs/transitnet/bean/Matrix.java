package whu.edu.cs.transitnet.bean;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Matrix {
    @Bean
    public MeterRegistry defaultMeterRegistry() {
        MeterRegistry registry = new SimpleMeterRegistry();
        Metrics.addRegistry(registry);
        return registry;
    }
}
