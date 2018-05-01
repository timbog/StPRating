package com.company.diploma.controller;

import com.company.diploma.ParamsParser;
import com.company.diploma.RouteDirection;
import com.company.diploma.entities.GeoParam;
import com.company.diploma.entities.PointDistance;
import com.company.diploma.service.GeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class RestController {

    private static final List<GeoParam> geoParams = new ParamsParser().loadParams("districts.txt");

    @Autowired
    GeoService geoService;

    @RequestMapping(value = "/rest/load", method = RequestMethod.GET)
    @ResponseBody
    public List<PointDistance> load(@RequestParam("point") String point,
                                    @RequestParam(value = "timestamp", required = false) Long timestamp,
                                    @RequestParam("direction") String direction) {
        return geoService.getDurationsOnRoutes(point, timestamp, RouteDirection.valueOf(direction));
    }

    @RequestMapping(value = "/rest/loadaggr", method = RequestMethod.GET)
    @ResponseBody
    public List<PointDistance> loadAggr(@RequestParam("point") String point,
                                    @RequestParam("direction") String direction) {

        return geoService.getAggregatedDurationsOnRoutes(point, RouteDirection.valueOf(direction));
    }

}
