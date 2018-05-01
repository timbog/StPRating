package com.company.diploma;

import com.company.diploma.entities.GeoParam;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParamsParser {

    public List<GeoParam> loadParams(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        List<String> contents = null;
        try {
            contents = FileUtils.readLines(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<GeoParam> result = new ArrayList<>();
        for (String line : contents) {
            String[] params = line.split("  ");
            result.add(new GeoParam(params[0], params[1], params[2]));
        }
        return result;
    }
}
