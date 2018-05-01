package com.company.diploma.service;

import com.company.diploma.*;
import com.company.diploma.entities.GeoParam;
import com.company.diploma.entities.PointDistance;
import com.company.diploma.entities.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.company.diploma.RouteDirection.FORWARD;

@Service
public class GeoService {

    private static final Long MONDAY = 1536568227000L;
    private static final Long NIGHT = 1536550227000L;

    private static final String CENTER = "59.9377079,30.308532";
    private static final String PULKOVO = "59.7998077,30.2697665";
    private static final String STADIUM = "59.9730363,30.2225669";
    private static final String EXPOFORUM = "59.7611708,30.3552256";
    private static final String MOSKOVSKIY = "59.9294222,30.3620868";
    private static final List<String> INTERESTING = List.of(CENTER, PULKOVO, STADIUM, EXPOFORUM, MOSKOVSKIY);

    private YandexApiCaller apiCaller;
    private List<String> points = new ArrayList<>();
    private Map<String, GeoParam> originToParam = new HashMap<>();

    private List<Route> getDirectionOnRoutes(List<String> origins, List<String> destinations, Long departureTime) {
        return apiCaller.getDurationsOnRoutes(origins, destinations, departureTime);
    }

    private Map<String, PointDistance> getDurationsOnRoutesMap(String coordinates, Long timestamp, RouteDirection direction) {
        return getDurationsOnRoutesStream(coordinates, timestamp, direction).collect(Collectors.toMap(el -> el.getMo(), Function.identity()));
    }

    private Stream<PointDistance> getDurationsOnRoutesStream(String coordinates, Long timestamp, RouteDirection direction) {
        List<Route> durationsOnRoutes = direction == FORWARD ? apiCaller.getDurationsOnRoutes(points, List.of(coordinates), timestamp) :
                apiCaller.getDurationsOnRoutes(List.of(coordinates), points, timestamp);
        return durationsOnRoutes.stream().map(el -> {
            GeoParam param = originToParam.get(el.getFrom());
            param = param == null ? originToParam.get(el.getTo()) : param;
            return PointDistance.builder().district(param.getDistrict()).mo(param.getMo()).duration(el.getDuration()).build();
        });
    }

    @Autowired
    public GeoService(@Value("${api.key}") String apiKey) {
        List<GeoParam> geoParams = new ParamsParser().loadParams("districts.txt");
        geoParams.forEach(el -> {
            points.add(el.getCoordinates());
            originToParam.put(el.getCoordinates(), el);
        });
        apiCaller = new YandexApiCaller(apiKey);
    }

    public List<PointDistance> getDurationsOnRoutes(String coordinates, Long timestamp, RouteDirection direction) {
        return getDurationsOnRoutesStream(coordinates, timestamp, direction)
                .sorted(Comparator.comparingLong(PointDistance::getDuration))
                .collect(Collectors.toList());
    }

    public List<PointDistance> getAggregatedDurationsOnRoutes(String coordinates, RouteDirection direction) {
        List<PointDistance> monday = getDurationsOnRoutes(coordinates, MONDAY, direction);
        Map<String, PointDistance> nightMap = getDurationsOnRoutesMap(coordinates, NIGHT, direction);
        return monday.stream().map(el ->
                PointDistance.builder().mo(el.getMo())
                        .district(el.getDistrict())
                        .duration(nightMap.get(el.getMo()).getDuration() + el.getDuration() / 2)
                        .build()).collect(Collectors.toList());
    }

    public List<PointDistance> getAggregatedDurationsOnRoutes() {
        List<Map<String, PointDistance>> maps = INTERESTING.stream().map(el -> {
            List<PointDistance> monday = getDurationsOnRoutes(el, MONDAY, FORWARD);
            Map<String, PointDistance> nightMap = getDurationsOnRoutesMap(el, NIGHT, FORWARD);
            return monday.stream().collect(Collectors.toMap(route -> route.getMo(), route ->
                    PointDistance.builder().mo(route.getMo())
                            .district(route.getDistrict())
                            .duration(nightMap.get(route.getMo()).getDuration() + route.getDuration() / 2)
                            .build()
            ));
        }).collect(Collectors.toList());
        Map<String, PointDistance> templateMap = maps.get(0);
        return templateMap.keySet().stream().map(mo ->
                PointDistance.builder()
                        .mo(mo)
                        .duration(maps.stream()
                                .collect(Collectors.summingLong(map -> map.get(mo).getDuration())).longValue() / maps.size())
                        .district(templateMap.get(mo).getDistrict())
                        .build()
        ).sorted(Comparator.comparingLong(PointDistance::getDuration))
                .collect(Collectors.toList());
    }
}
