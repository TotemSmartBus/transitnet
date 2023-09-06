package whu.edu.cs.transitnet.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import whu.edu.cs.transitnet.dao.RoutesDao;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class RouteCache {

    public List<String> routes;
    @Autowired
    RoutesDao routesDao;

    @PostConstruct
    public void init() {
        routes = routesDao.findDistinctRouteID();
    }
}