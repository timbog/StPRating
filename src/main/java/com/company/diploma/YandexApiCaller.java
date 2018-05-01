package com.company.diploma;


import com.company.diploma.entities.Route;
import com.google.gson.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class YandexApiCaller {
    private static final String path = "https://api.routing.yandex.net/v1.0.0/distancematrix";

    private String apiKey;

    @SneakyThrows
    public List<Route> getDurationsOnRoutes(List<String> origins, List<String> destinations, Long departureTime) {
        return parseJson(origins, destinations, sendRequest(origins, destinations, departureTime));
    }

    public String sendRequest(List<String> origins, List<String> destinations, Long departureTime) throws Exception{
        Map<String, String> parameters = new HashMap<>();
        parameters.put("origins", origins.stream().collect(Collectors.joining("|")));
        parameters.put("destinations", destinations.stream().collect(Collectors.joining("|")));
        parameters.put("mode", "driving");
        if (departureTime != null) {
            parameters.put("departure_time", departureTime.toString());
        }
        parameters.put("apikey", apiKey);
        String paramLine = parameters.entrySet().stream().map(el -> {
            try {
                return URLEncoder.encode(el.getKey(), "UTF-8") + "=" + URLEncoder.encode(el.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                return "";
            }
        }).collect(Collectors.joining("&"));
        URL url = new URL(path + "?" + paramLine);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setDoOutput(true);
        con.disconnect();
        return IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8);
    }

    private List<Route> parseJson(List<String> origins, List<String> destinations,  String json) {
        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse(json).getAsJsonObject();
        List<Route> result = new ArrayList<>();
        JsonArray rows = root.getAsJsonArray("rows");
        for (int i = 0; i < rows.size(); i++) {
            JsonObject row = rows.get(i).getAsJsonObject();
            JsonArray elements = row.getAsJsonArray("elements");
            for (int j = 0; j < elements.size(); j++) {
                JsonObject element = elements.get(j).getAsJsonObject();
                Route route = new Route(origins.get(i), destinations.get(j),
                        element.getAsJsonObject("duration").getAsJsonPrimitive("value").getAsLong(),
                        element.getAsJsonObject("distance").getAsJsonPrimitive("value").getAsLong());
                result.add(route);
            }
        }
        return result;
    }
}
