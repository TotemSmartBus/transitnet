package whu.edu.cs.transitnet.indicator;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class MatricsIndicator implements InfoContributor {
    @Override
    public void contribute(Info.Builder builder) {

    }
}
