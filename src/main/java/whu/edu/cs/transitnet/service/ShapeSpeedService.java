package whu.edu.cs.transitnet.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.Torch.base.visualization.TrajJsonModel;
import whu.edu.cs.transitnet.dao.SplitShapeDao;
import whu.edu.cs.transitnet.dao.SplitShapeSpeedDao;
import whu.edu.cs.transitnet.pojo.SplitShapeEntity;
import whu.edu.cs.transitnet.pojo.SplitShapeSpeedEntity;
import whu.edu.cs.transitnet.vo.RouteShapeSpeedVo;
import whu.edu.cs.transitnet.vo.ShapePointVo;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShapeSpeedService {
    @Autowired
    SplitShapeSpeedDao splitShapeSpeedDao;
    @Autowired
    SplitShapeDao splitShapeDao;

    public List<SplitShapeSpeedEntity> getAllSplitShapeSpeedByShapeId(String shapeId) {
        return splitShapeSpeedDao.findAllByShapeId(shapeId);
    }

    public List<RouteShapeSpeedVo> getAllRouteShapeWithSpeed2() {
        List<RouteShapeSpeedVo> routeShapeSpeedVos = new ArrayList<>();
        List<SplitShapeSpeedEntity> splitShapeSpeedEntities = splitShapeSpeedDao.findAll();
        List<SplitShapeEntity> splitShapeEntities = splitShapeDao.findAll();

        List<TrajJsonModel> trajJsonModels = new ArrayList<>();
        List<Double> speeds = new ArrayList<>();
        String curRouteId = splitShapeSpeedEntities.get(0).getRouteId();
        String curShapeId = splitShapeSpeedEntities.get(0).getShapeId();
        Integer shapePointIdx = 0;
        Integer allPointCount = splitShapeEntities.size();

        for(SplitShapeSpeedEntity splitShapeSpeedEntity: splitShapeSpeedEntities) {
            if(!splitShapeSpeedEntity.getShapeId().equals(curShapeId)){
                routeShapeSpeedVos.add(new RouteShapeSpeedVo(curRouteId, curShapeId, trajJsonModels, speeds));
                curRouteId = splitShapeSpeedEntity.getRouteId();
                curShapeId = splitShapeSpeedEntity.getShapeId();
                trajJsonModels = new ArrayList<>();
                speeds = new ArrayList<>();
            }
            Integer curShapeSpeedSplitIdx = splitShapeSpeedEntity.getSplitId();
            List<ShapePointVo> shapePointVos = new ArrayList<>();
            while(shapePointIdx < allPointCount && splitShapeEntities.get(shapePointIdx).getSplitId() == curShapeSpeedSplitIdx) {
                SplitShapeEntity splitShapeEntity = splitShapeEntities.get(shapePointIdx);
                shapePointVos.add(new ShapePointVo(splitShapeEntity.getLat(), splitShapeEntity.getLon()));
                shapePointIdx += 1;
            }
            speeds.add(splitShapeSpeedEntity.getSpeed());
            TrajJsonModel trajJsonModel = new TrajJsonModel(shapePointVos);
            trajJsonModels.add(trajJsonModel);
        }
        routeShapeSpeedVos.add(new RouteShapeSpeedVo(curRouteId, curShapeId, trajJsonModels, speeds));
        return routeShapeSpeedVos;
    }

    public List<RouteShapeSpeedVo> getAllRouteShapeWithSpeed() {
        List<RouteShapeSpeedVo> routeShapeSpeedVos = new ArrayList<>();
        List<String> shapeIdList = splitShapeDao.findAllShapeIdOne();
        for(String shapeId: shapeIdList) {
            List<SplitShapeSpeedEntity> splitShapeSpeedEntities = splitShapeSpeedDao.findAllByShapeId(shapeId);
            List<TrajJsonModel> trajs = new ArrayList<>();
            List<Double> speeds = new ArrayList<>();
            Integer splitCount = splitShapeSpeedEntities.size();
            for(Integer splitId = 0; splitId < splitCount; splitId ++) {
                List<ShapePointVo> shapePoints = splitShapeDao.findAllShapePointsByShapeIdAndSplitId(shapeId, splitId);
                TrajJsonModel trajJsonModel = new TrajJsonModel(shapePoints);
                speeds.add(splitShapeSpeedEntities.get(splitId).getSpeed());
                trajs.add(trajJsonModel);
            }
            RouteShapeSpeedVo curRouteShape = new RouteShapeSpeedVo(splitShapeSpeedEntities.get(0).getRouteId(), splitShapeSpeedEntities.get(0).getShapeId(), trajs,speeds);
            routeShapeSpeedVos.add(curRouteShape);
        }
        return routeShapeSpeedVos;
    }
}
